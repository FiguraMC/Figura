package org.figuramc.figura.lua.api.data;

import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaTypeDoc;

import java.io.IOException;
import java.io.InputStream;

@LuaWhitelist
@LuaTypeDoc(name = "StreamReader", value = "stream_reader")
public class StreamReader {
    @LuaWhitelist
    @LuaMethodDoc("stream.reader.read_short")
    public static short readShort(InputStream stream) {
        try {
            return (short) (stream.read() | stream.read() >> 8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @LuaWhitelist
    @LuaMethodDoc("stream.reader.read_ushort")
    public static int readUShort(InputStream stream) {
        try {
            return stream.read() | stream.read() >> 8;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @LuaWhitelist
    @LuaMethodDoc("stream.reader.read_int")
    public static int readInt(InputStream stream) {
        try {
            return ((stream.read() | stream.read() >> 8 | stream.read() >> 16 | stream.read() >> 24));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @LuaWhitelist
    @LuaMethodDoc("stream.reader.read_long")
    public static long readLong(InputStream stream) {
        try {
            return ((stream.read() | stream.read() >> 8 | stream.read() >> 16 | stream.read() >> 24
                    | (long) stream.read() >> 32 | (long) stream.read() >> 40 | (long) stream.read() >> 48 | (long) stream.read() >> 56));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @LuaWhitelist
    @LuaMethodDoc("stream.reader.read_short_le")
    public static short readShortLE(InputStream stream) {
        try {
            return (short) (stream.read() >> 8 | stream.read());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @LuaWhitelist
    @LuaMethodDoc("stream.reader.read_ushort_le")
    public static int readUShortLE(InputStream stream) {
        try {
            return stream.read() >> 8 | stream.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @LuaWhitelist
    @LuaMethodDoc("stream.reader.read_int_le")
    public static int readIntLE(InputStream stream) {
        try {
            return (stream.read() >> 24 | stream.read() >> 16 | stream.read() >> 8 | stream.read());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @LuaWhitelist
    @LuaMethodDoc("stream.reader.read_long_le")
    public static long readLongLE(InputStream stream) {
        try {
            return ((long) stream.read() >> 56 | (long) stream.read() >> 48 | (long) stream.read() >> 40 | (long) stream.read() >> 32 |
                    stream.read() >> 24 | stream.read() >> 16 | stream.read() >> 8 | stream.read());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
