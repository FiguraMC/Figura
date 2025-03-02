package org.figuramc.figura.mixin.sound;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.audio.OggAudioStream;
import org.chenliang.oggus.opus.*;
import org.concentus.*;
import org.figuramc.figura.FiguraMod;
import org.lwjgl.stb.STBVorbisAlloc;
import org.lwjgl.stb.STBVorbisInfo;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.sound.sampled.AudioFormat;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.*;
import java.util.*;

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
            method = "refillFromStream",
            at = @At("TAIL")
    )
    private void checkForOpusHeader(CallbackInfoReturnable<Boolean> cir) {
        if (figura$isOpus) { // Technically not needed, but I don't want to deal with potential side effects of checking here instead of in <init>
            return;
        }

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
    private final Queue<OpusPacket> figura$packetQueue = new LinkedList<>();

    @Unique
    private boolean figura$endOfStream = false;

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

    @Unique
    private OpusPacket figura$readNextOpusPacket() throws IOException {
        if (!figura$packetQueue.isEmpty()) {
            return figura$packetQueue.poll();
        }

        if (figura$endOfStream) {
            return null;
        }

        AudioDataPacket audioPacket = figura$opusStream.readAudioPacket();
        if (audioPacket == null) {
            figura$endOfStream = true;
            return null;
        }

        List<OpusPacket> packets = audioPacket.getOpusPackets();
        figura$packetQueue.addAll(packets);

        return figura$packetQueue.isEmpty() ? null : figura$packetQueue.poll();
    }

    /**
     * Reads a number of Opus packets up to a specified limit
     *
     * @param maxPackets Maximum number of packets to read
     * @return List of read packets
     * @throws IOException if reading from the stream fails
     */
    @Unique
    private List<OpusPacket> figura$readOpusPackets(int maxPackets) throws IOException {
        List<OpusPacket> result = new ArrayList<>(maxPackets);

        for (int i = 0; i < maxPackets; i++) {
            OpusPacket packet = figura$readNextOpusPacket();
            if (packet == null) {
                break;
            }
            result.add(packet);
        }
        return result;
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

        final int BATCH_SIZE = 256;

        while (true) {
            short[] decoded = figura$decodeNextBatch(BATCH_SIZE);
            if (decoded == null || decoded.length == 0) {
                break;
            } else {
                figura$injectShortArray(output, decoded);
            }
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

        short[] decoded = figura$decodeNextBatch(1);

        if (decoded == null || decoded.length == 0) {
            cir.setReturnValue(false);
            return;
        }

        figura$injectShortArray(output, decoded);

        cir.setReturnValue(!figura$endOfStream || !figura$packetQueue.isEmpty());
    }

    /**
     * Decodes a batch of Opus packets into PCM audio
     *
     * @param maxPackets Maximum number of packets to process in this batch
     * @return Decoded audio samples, or null if no packets were available
     * @throws IOException if reading from the stream fails
     * @throws OpusException if decoding fails
     */
    @Unique
    private short[] figura$decodeNextBatch(int maxPackets) throws IOException, OpusException {
        List<OpusPacket> packets = figura$readOpusPackets(maxPackets);

        if (packets.isEmpty()) {
            return null;
        }

        byte[] firstPacket = packets.get(0).dumpToStandardFormat();
        int samplesPerFrame = OpusPacketInfo.getNumSamplesPerFrame(firstPacket, 0, figura$sampleRate);
        int totalSamples = samplesPerFrame * packets.size() * figura$channelCount;

        short[] decoded = new short[totalSamples];
        int sampleOffset = 0;

        for (OpusPacket packet : packets) {
            byte[] encodedData = packet.dumpToStandardFormat();
            int code = figura$decoder.decode(
                    encodedData, 0, encodedData.length,
                    decoded, sampleOffset, samplesPerFrame, false
            );

            if (code < 0) {
                FiguraMod.debug("Opus decoding error: " + CodecHelpers.opus_strerror(code));
                continue;
            }

            sampleOffset += code * figura$channelCount;
        }

        // I've never seen any actual sample offset, but I guess it's good practice to account for it
        if (sampleOffset < totalSamples) {
            short[] trimmed = new short[sampleOffset];
            System.arraycopy(decoded, 0, trimmed, 0, sampleOffset);
            return trimmed;
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
        for (short value : decoded) {

            if (_concat.getCurrentBuffer().remaining() < 2) {
                _concat.getCurrentBuffer().flip();
                _concat.getBuffers().add(_concat.getCurrentBuffer());
                _concat.makeNewBuf();
            }

            _concat.getCurrentBuffer().putShort(value);
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

