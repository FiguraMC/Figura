package org.figuramc.figura.lua.api.data;

import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaTypeDoc;

import java.io.IOException;
import java.io.InputStream;

@LuaWhitelist
@LuaTypeDoc(name = "ReadUtils", value = "read_utils")
public class ReadUtils {
    @LuaWhitelist
    @LuaMethodDoc("read_utils.read_short")
    public static int readShort(InputStream stream) throws IOException {
        byte[] bytes = stream.readNBytes(2);
        short v = 0;
        for (int i = 0; i < bytes.length; i++) {
            v |= (bytes[((bytes.length - 1) - i)] & 0xFF) << (i * 8);
        }
        return v;
    }

    @LuaWhitelist
    @LuaMethodDoc("read_utils.read_ushort")
    public static int readUShort(InputStream stream) throws IOException {
        byte[] bytes = stream.readNBytes(2);
        int v = 0;
        for (int i = 0; i < bytes.length; i++) {
            v |= (bytes[((bytes.length - 1) - i)] & 0xFF) << (i * 8);
        }
        return v;
    }

    @LuaWhitelist
    @LuaMethodDoc("read_utils.read_int")
    public static int readInt(InputStream stream) throws IOException {
        byte[] bytes = stream.readNBytes(4);
        int v = 0;
        for (int i = 0; i < bytes.length; i++) {
            v |= (bytes[((bytes.length - 1) - i)] & 0xFF) << (i * 8);
        }
        return v;
    }

    @LuaWhitelist
    @LuaMethodDoc("read_utils.read_long")
    public static long readLong(InputStream stream) throws IOException {
        byte[] bytes = stream.readNBytes(8);
        long v = 0;
        for (int i = 0; i < bytes.length; i++) {
            v |= (long) (bytes[((bytes.length - 1) - i)] & 0xFF) << (i * 8);
        }
        return v;
    }

    @LuaWhitelist
    @LuaMethodDoc("read_utils.read_float")
    public static float readFloat(InputStream stream) throws IOException {
        return Float.intBitsToFloat(readInt(stream));
    }

    @LuaWhitelist
    @LuaMethodDoc("read_utils.read_double")
    public static double readDouble(InputStream stream) throws IOException {
        return Double.longBitsToDouble(readLong(stream));
    }

    @LuaWhitelist
    @LuaMethodDoc("read_utils.read_short_le")
    public static int readShortLE(InputStream stream) throws IOException {
        byte[] bytes = stream.readNBytes(2);
        short v = 0;
        for (int i = 0; i < bytes.length; i++) {
            v |= (bytes[i] & 0xFF) << (i * 8);
        }
        return v;
    }

    @LuaWhitelist
    @LuaMethodDoc("read_utils.read_ushort_le")
    public static int readUShortLE(InputStream stream) throws IOException {
        byte[] bytes = stream.readNBytes(2);
        int v = 0;
        for (int i = 0; i < bytes.length; i++) {
            v |= (bytes[i] & 0xFF) << (i * 8);
        }
        return v;
    }

    @LuaWhitelist
    @LuaMethodDoc("read_utils.read_int_le")
    public static int readIntLE(InputStream stream) throws IOException {
        byte[] bytes = stream.readNBytes(4);
        int v = 0;
        for (int i = 0; i < bytes.length; i++) {
            v |= (bytes[i] & 0xFF) << (i * 8);
        }
        return v;
    }

    @LuaWhitelist
    @LuaMethodDoc("read_utils.read_long_le")
    public static long readLongLE(InputStream stream) throws IOException {
        byte[] bytes = stream.readNBytes(8);
        long v = 0;
        for (int i = 0; i < bytes.length; i++) {
            v |= (long) (bytes[i] & 0xFF) << (i * 8);
        }
        return v;
    }

    @LuaWhitelist
    @LuaMethodDoc("read_utils.read_float_le")
    public static float readFloatLE(InputStream stream) throws IOException {
        return Float.intBitsToFloat(readIntLE(stream));
    }

    @LuaWhitelist
    @LuaMethodDoc("read_utils.read_double_le")
    public static double readDoubleLE(InputStream stream) throws IOException {
        return Double.longBitsToDouble(readLongLE(stream));
    }
}
