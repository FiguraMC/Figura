package org.figuramc.figura.utils;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.ptr.PointerByReference;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;

public interface Opus extends Library {
    Opus OPUS = Native.load(System.getProperty("opus.lib"), Opus.class);

    String opus_strerror(int error);

    String opus_get_version_string();

    PointerByReference opus_decoder_create(int Fs, int channels, IntBuffer error);

    int opus_decode(PointerByReference st, byte data[], int len, ShortBuffer pcm, int frame_size, int decode_fec);

    void opus_decoder_destroy(PointerByReference st);

    int opus_packet_get_samples_per_frame(byte data[], int Fs);
}

