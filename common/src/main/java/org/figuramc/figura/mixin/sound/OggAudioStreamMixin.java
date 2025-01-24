package org.figuramc.figura.mixin.sound;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.audio.OggAudioStream;
import com.sun.jna.ptr.PointerByReference;
import net.minecraft.util.Mth;
import org.chenliang.oggus.opus.*;
import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.utils.Opus;
import org.figuramc.figura.utils.OpusUtils;
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
    private long handle;

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
    private void checkForOpusHeader(InputStream inputStream, CallbackInfo ci) throws IOException {
        byte[] headerBytes = new byte[8];
        int position = this.buffer.position();
        this.buffer.position(0x1C);
        this.buffer.get(headerBytes, 0, Math.min(headerBytes.length, this.buffer.remaining()));
        this.buffer.position(position);

        if (new String(headerBytes, 0, 8).equals("OpusHead")) {
            FiguraMod.debug("Opus file detected");

            figura$isOpus = true;

            if (!OpusUtils.isLoaded()) {
                FiguraMod.debug("Opus library not loaded, loading now");
                OpusUtils.loadBundled();
            } else {
                FiguraMod.debug("Opus library already loaded");
            }
        }
    }

    @Unique
    OggOpusStream figura$opusStream;

    @Unique
    IntBuffer figura$error = IntBuffer.allocate(1);

    @Unique
    PointerByReference figura$decoder = null;

    @Unique
    ArrayList<OpusPacket> figura$packetBuffer = new ArrayList<>(128);

    @WrapOperation(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/lwjgl/stb/STBVorbis;stb_vorbis_open_pushdata(Ljava/nio/ByteBuffer;Ljava/nio/IntBuffer;Ljava/nio/IntBuffer;Lorg/lwjgl/stb/STBVorbisAlloc;)J"
            ),
            remap = false
    )
    private long openOpusStream(ByteBuffer datablock, IntBuffer datablock_memory_consumed_in_bytes, IntBuffer error, STBVorbisAlloc alloc_buffer, Operation<Long> original) throws IOException {
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

            FiguraMod.debug("Initializing " + Opus.OPUS.opus_get_version_string() + " @ " + figura$sampleRate + "hz (" + figura$channelCount + " channel(s))");
            figura$decoder = Opus.OPUS.opus_decoder_create(figura$sampleRate, figura$channelCount, figura$error);
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

    @WrapOperation(
            method = "<init>",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/lwjgl/stb/STBVorbis;stb_vorbis_get_info(JLorg/lwjgl/stb/STBVorbisInfo;)Lorg/lwjgl/stb/STBVorbisInfo;"
            ),
            remap = false
    )
    private STBVorbisInfo getOpusInfo(long f, STBVorbisInfo info, Operation<STBVorbisInfo> original) {
        if (figura$isOpus) {
            return null; // Avoid calling stb_vorbis_get_info
        } else {
            return original.call(handle, info);
        }
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
            FiguraMod.debug("Opus file detected, returning custom audio format");
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
    private void readAll(CallbackInfoReturnable<ByteBuffer> cir) throws IOException {
        if (!figura$isOpus) {
            return;
        }
        FiguraMod.debug("Reading Opus packet");
        OggAudioStream.OutputConcat output = new OggAudioStream.OutputConcat(16384);

        if (!figura$preloadOpusBuffer()) {
            FiguraMod.debug("Failed to preload buffer");
        } else if (!figura$packetBuffer.isEmpty()) {
            OpusPacket packet = figura$packetBuffer.get(0);
            int samples = Opus.OPUS.opus_packet_get_samples_per_frame(packet.dumpToStandardFormat(), figura$sampleRate);
            ShortBuffer decoded = figura$decode(figura$decoder, figura$packetBuffer.stream().map(OpusPacket::dumpToStandardFormat).toList(), samples);
            figura$injectShortBuffer(output, decoded);
            cir.setReturnValue(output.get());
            return;
        }
        cir.setReturnValue(output.get());
    }

    // If something calls readFrame instead of readAll for some reason
    @Inject(
            method = "readFrame",
            at = @At("HEAD"),
            cancellable = true
    )
    private void readPacket(OggAudioStream.OutputConcat output, CallbackInfoReturnable<Boolean> cir) throws IOException {
        if (!figura$isOpus) {
            return;
        }

        if (!figura$preloadOpusBuffer()) {
            FiguraMod.debug("Failed to preload buffer");
        } else if (!figura$packetBuffer.isEmpty()) {
            OpusPacket packet = figura$packetBuffer.remove(0);
            int samples = Opus.OPUS.opus_packet_get_samples_per_frame(packet.dumpToStandardFormat(), figura$sampleRate);
            ShortBuffer decoded = figura$decode(figura$decoder, Collections.singletonList(packet.dumpToStandardFormat()), samples);
            figura$injectShortBuffer(output, decoded);
            cir.setReturnValue(!figura$packetBuffer.isEmpty());
            return;
        }
        cir.setReturnValue(false);
    }

    /**
     * Decodes a list of Opus packets into a ShortBuffer.
     *
     * @param opusDecoder        The Opus decoder reference.
     * @param packets            The list of Opus packets to decode.
     * @param samples The maximum number of samples per frame.
     * @return A ShortBuffer containing the decoded audio samples.
     */
    @Unique
    private ShortBuffer figura$decode(PointerByReference opusDecoder, List<byte[]> packets, int samples) {
        // ~40% faster than ShortBuffer.allocate(...) on my system
        ShortBuffer decoded = ByteBuffer.allocateDirect(samples * packets.size() * Short.BYTES)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer();

        for (byte[] dataBuffer : packets) {
            int initialPosition = decoded.position();
            int code = Opus.OPUS.opus_decode(opusDecoder, dataBuffer, dataBuffer.length, decoded, samples, 0);

            if (code < 0) {
                FiguraMod.debug(Opus.OPUS.opus_strerror(code));
                decoded.position(initialPosition);
                continue;
            }
            decoded.position(decoded.position() + code);
        }
        decoded.flip();
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
    public void figura$injectShortBuffer(OggAudioStream.OutputConcat concat, ShortBuffer decoded) {
        OutputConcatAccessor _concat = (OutputConcatAccessor) concat;
        while (decoded.hasRemaining()) {
            int rawValue = decoded.get();

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

    @WrapOperation(
            method = "close",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/lwjgl/stb/STBVorbis;stb_vorbis_close(J)V"
            ),
            remap = false
    )
    private void closeOpusStream(long f, Operation<Void> original) {
        if (figura$isOpus) {
            FiguraMod.debug("Destroying Opus decoder");
            Opus.OPUS.opus_decoder_destroy(figura$decoder);
        } else {
            original.call(f);
        }
    }
}

