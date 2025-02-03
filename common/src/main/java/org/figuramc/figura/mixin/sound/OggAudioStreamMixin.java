package org.figuramc.figura.mixin.sound;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.audio.OggAudioStream;
import net.minecraft.util.Mth;
import org.chenliang.oggus.opus.*;
import org.concentus.*;
import org.figuramc.figura.FiguraMod;
import org.lwjgl.stb.STBVorbisAlloc;
import org.lwjgl.stb.STBVorbisInfo;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.sound.sampled.AudioFormat;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mixin(OggAudioStream.class)
public abstract class OggAudioStreamMixin {

    @Shadow
    private ByteBuffer buffer;

    @Shadow
    protected abstract void forwardBuffer();

    @Unique
    boolean figura$isOpus = false;
    @Unique
    int figura$sampleRate;
    @Unique
    int figura$channelCount;


    @Inject(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/nio/ByteBuffer;position()I",
                    ordinal = 0,
                    shift = At.Shift.BEFORE
            )
    )
    private void checkForOpusHeader(InputStream inputStream, CallbackInfo ci) {
        byte[] headerBytes = new byte[8];
        int position = this.buffer.position();
        this.buffer.position(0x1C);
        this.buffer.get(headerBytes, 0, Math.min(headerBytes.length, this.buffer.remaining()));
        this.buffer.position(position);

        figura$isOpus = new String(headerBytes, 0, 8).equals("OpusHead");
    }

    @Unique
    OggOpusStream figura$opusStream;

    @Unique
    OpusDecoder figura$decoder = null;

    @Unique
    ArrayList<OpusPacket> figura$packetBuffer = new ArrayList<>(256);

    @WrapOperation(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/lwjgl/stb/STBVorbis;stb_vorbis_open_pushdata(Ljava/nio/ByteBuffer;Ljava/nio/IntBuffer;Ljava/nio/IntBuffer;Lorg/lwjgl/stb/STBVorbisAlloc;)J"
            ),
            remap = false
    )
    private long openOpusStream(ByteBuffer datablock, IntBuffer datablock_memory_consumed_in_bytes, IntBuffer error, STBVorbisAlloc alloc_buffer, Operation<Long> original) throws IOException, OpusException {
        if (figura$isOpus) {
            if (datablock.remaining() == datablock.capacity()) { // Increase buffer size if it's too small
                forwardBuffer();
                FiguraMod.debug("Increased buffer size to " + buffer.capacity());
                return 0;
            }
            byte[] bufferArray = new byte[datablock.remaining()];
            datablock.get(bufferArray);
            figura$opusStream = OggOpusStream.from(new ByteArrayInputStream(bufferArray));

            figura$configureDecoder(figura$opusStream);

            FiguraMod.debug(String.format("Initializing opus @ %d hz (%d channel(s))", figura$sampleRate, figura$channelCount));
            figura$decoder = new OpusDecoder(figura$sampleRate, figura$channelCount);
            return 1;
        } else {
            return original.call(datablock, datablock_memory_consumed_in_bytes, error, alloc_buffer);
        }
    }

    @Unique
    private void figura$configureDecoder(OggOpusStream stream) {
        IdHeader idHeader = stream.getIdHeader();
        figura$sampleRate = (int) idHeader.getInputSampleRate();
        figura$channelCount = idHeader.getChannelCount();
    }

    @WrapOperation(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/nio/IntBuffer;get(I)I",
                    ordinal = 0
            ),
            remap = false
    )
    private int spoofBuffer(IntBuffer instance, int i, Operation<Integer> original) {
        if (figura$isOpus) {
            return 0;
        }
        return original.call(instance, i);
    }

    @WrapWithCondition(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/lwjgl/stb/STBVorbis;stb_vorbis_get_info(JLorg/lwjgl/stb/STBVorbisInfo;)Lorg/lwjgl/stb/STBVorbisInfo;"
            ),
            remap = false
    )
    private boolean getOpusInfo(long f, STBVorbisInfo __result) {
        return !figura$isOpus;
    }

    @WrapOperation(
            method = "<init>",
            at = @At(
                    value = "NEW",
                    target = "(FIIZZ)Ljavax/sound/sampled/AudioFormat;"
            ),
            remap = false
    )
    private AudioFormat createOpusAudioFormat(float sampleRate, int sampleSizeInBits, int channels, boolean signed, boolean bigEndian, Operation<AudioFormat> original) {
        if (figura$isOpus) {
            return original.call((float) figura$sampleRate, sampleSizeInBits, figura$channelCount, signed, bigEndian);
        }
        return original.call(sampleRate, sampleSizeInBits, channels, signed, bigEndian);
    }

    /**
     * Preloads {@link OggAudioStreamMixin#figura$packetBuffer} with Opus packets.
     *
     * @return true if the buffer was successfully preloaded, false otherwise.
     * @throws IOException if {@link OggOpusStream#readAudioPacket()} fails
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    @Unique
    private boolean figura$preloadOpusBuffer() throws IOException {
        if (figura$packetBuffer.isEmpty()) {
            AudioDataPacket p;
            while ((p = figura$opusStream.readAudioPacket()) != null) {
                List<OpusPacket> packets = p.getOpusPackets();
                figura$packetBuffer.addAll(packets);
            }
        }
        return true;
    }

    @Inject(
            method = "readAll",
            at = @At("HEAD"),
            cancellable = true
    )
    private void readAll(CallbackInfoReturnable<ByteBuffer> cir) throws IOException, OpusException {
        if (!figura$isOpus) {
            return;
        }
        OggAudioStream.OutputConcat output = new OggAudioStream.OutputConcat(16384);

        if (!figura$preloadOpusBuffer()) {
            FiguraMod.debug("Failed to preload buffer");
        } else if (!figura$packetBuffer.isEmpty()) {
            OpusPacket packet = figura$packetBuffer.get(0);
            int samples = OpusPacketInfo.getNumSamplesPerFrame(packet.dumpToStandardFormat(), 0, figura$sampleRate);
            short[] decoded = figura$decode(figura$packetBuffer.stream().map(OpusPacket::dumpToStandardFormat).toList(), samples);
            figura$injectShortArray(output, decoded);
        }
        cir.setReturnValue(output.get());
    }

    // If something calls readFrame instead of readAll for some reason
    @Inject(
            method = "readFrame",
            at = @At("HEAD"),
            cancellable = true
    )
    private void readPacket(OggAudioStream.OutputConcat output, CallbackInfoReturnable<Boolean> cir) throws IOException, OpusException {
        if (!figura$isOpus) {
            return;
        }

        if (!figura$preloadOpusBuffer()) {
            FiguraMod.debug("Failed to preload buffer");
        } else if (!figura$packetBuffer.isEmpty()) {
            OpusPacket packet = figura$packetBuffer.remove(0);
            int samples = OpusPacketInfo.getNumSamplesPerFrame(packet.dumpToStandardFormat(), 0, figura$sampleRate);
            short[] decoded = figura$decode(Collections.singletonList(packet.dumpToStandardFormat()), samples);
            figura$injectShortArray(output, decoded);
            cir.setReturnValue(!figura$packetBuffer.isEmpty());
            return;
        }
        cir.setReturnValue(false);
    }

    /**
     * Decodes a list of Opus packets into a ShortBuffer.
     *
     * @param packets     The list of Opus packets to decode.
     * @param samples     The maximum number of samples per frame.
     * @return A ShortBuffer containing the decoded audio samples.
     */
    @Unique
    private short[] figura$decode(List<byte[]> packets, int samples) throws OpusException {
        short[] decoded = new short[samples * packets.size()];
        for (byte[] dataBuffer : packets) {
            int code = figura$decoder.decode(dataBuffer, 0, dataBuffer.length, decoded, 0, samples,false);

            if (code < 0) {
                FiguraMod.debug(CodecHelpers.opus_strerror(code));
            }
        }
        return decoded;
    }

    /**
     * Bypasses the need to call {@link com.mojang.blaze3d.audio.OggAudioStream.OutputConcat#put(float)}
     * and unnecessary float conversions by directly inserting decoded audio samples into the internal
     * {@link ByteBuffer}.
     *
     * @param concat  The {@link com.mojang.blaze3d.audio.OggAudioStream.OutputConcat} to inject the audio samples into.
     * @param decoded The {@link ShortBuffer} containing the decoded audio samples.
     */
    @Unique
    public void figura$injectShortArray(OggAudioStream.OutputConcat concat, short[] decoded) {
        OutputConcatAccessor _concat = (OutputConcatAccessor) concat;
        for (short rawValue : decoded) {

            int clampedValue = Mth.clamp(rawValue, Short.MIN_VALUE, Short.MAX_VALUE);

            if (_concat.getCurrentBuffer().remaining() < 2) {
                _concat.getCurrentBuffer().flip();
                _concat.getBuffers().add(_concat.getCurrentBuffer());
                _concat.makeNewBuf();
            }

            _concat.getCurrentBuffer().putShort((short) clampedValue);
            _concat.setByteCount(_concat.getByteCount() + 2);
        }
    }

    @WrapWithCondition(
            method = "close",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/lwjgl/stb/STBVorbis;stb_vorbis_close(J)V"
            ),
            remap = false
    )
    private boolean close(long f) {
        return !figura$isOpus;
    }
}

