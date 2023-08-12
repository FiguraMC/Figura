package org.figuramc.figura.lua.api.data;

import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaTypeDoc;

import java.io.IOException;
import java.io.OutputStream;

@LuaWhitelist
@LuaTypeDoc(name = "WriteUtils", value = "write_utils")
public class WriteUtils {
    @LuaWhitelist
    @LuaMethodDoc("write_utils.write_short")
    public static void writeShort(OutputStream stream, int val) throws IOException {
        if (val >= Short.MIN_VALUE && val <= Short.MAX_VALUE) {
            short s = (short) val;
            stream.write((s >> 8) & 0xFF);
            stream.write(s & 0xFF);
        }
        else throw new RuntimeException("Value %s is out of range [%s; %s]".formatted(val, Short.MIN_VALUE, Short.MAX_VALUE));
    }

    @LuaWhitelist
    @LuaMethodDoc("write_utils.write_ushort")
    public static void writeUShort(OutputStream stream, int val) throws IOException {
        if (val >= 0 && val <= Character.MAX_VALUE) {
            char s = (char) val;
            stream.write((s >> 8) & 0xFF);
            stream.write(s & 0xFF);
        }
        else throw new RuntimeException("Value %s is out of range [%s; %s]".formatted(val, 0, (int) Character.MAX_VALUE));
    }

    @LuaWhitelist
    @LuaMethodDoc("write_utils.write_int")
    public static void writeInt(OutputStream stream, int val) throws IOException {
        stream.write((val >> 24) & 0xFF);
        stream.write((val >> 16) & 0xFF);
        stream.write((val >> 8) & 0xFF);
        stream.write(val & 0xFF);
    }

    @LuaWhitelist
    @LuaMethodDoc("write_utils.write_long")
    public static void writeLong(OutputStream stream, long val) throws IOException {
        stream.write((int) ((val >> 56) & 0xFF));
        stream.write((int) ((val >> 48) & 0xFF));
        stream.write((int) ((val >> 40) & 0xFF));
        stream.write((int) ((val >> 32) & 0xFF));
        stream.write((int) ((val >> 24) & 0xFF));
        stream.write((int) ((val >> 16) & 0xFF));
        stream.write((int) ((val >> 8) & 0xFF));
        stream.write((int) (val & 0xFF));
    }

    @LuaWhitelist
    @LuaMethodDoc("write_utils.write_float")
    public static void writeFloat(OutputStream stream, float val) throws IOException {
        writeInt(stream, Float.floatToIntBits(val));
    }

    @LuaWhitelist
    @LuaMethodDoc("write_utils.write_double")
    public static void writeDouble(OutputStream stream, double val) throws IOException {
        writeLong(stream, Double.doubleToLongBits(val));
    }

    @LuaWhitelist
    @LuaMethodDoc("write_utils.write_short_le")
    public static void writeShortLE(OutputStream stream, int val) throws IOException {
        if (val >= Short.MIN_VALUE && val <= Short.MAX_VALUE) {
            short s = (short) val;
            stream.write(s & 0xFF);
            stream.write((s >> 8) & 0xFF);
        }
        else throw new RuntimeException("Value %s is out of range [%s; %s]".formatted(val, Short.MIN_VALUE, Short.MAX_VALUE));
    }

    @LuaWhitelist
    @LuaMethodDoc("write_utils.write_ushort_le")
    public static void writeUShortLE(OutputStream stream, int val) throws IOException {
        if (val >= 0 && val <= Character.MAX_VALUE) {
            char s = (char) val;
            stream.write(s & 0xFF);
            stream.write((s >> 8) & 0xFF);
        }
        else throw new RuntimeException("Value %s is out of range [%s; %s]".formatted(val, 0, (int) Character.MAX_VALUE));
    }

    @LuaWhitelist
    @LuaMethodDoc("write_utils.write_int_le")
    public static void writeIntLE(OutputStream stream, int val) throws IOException {
        stream.write(val & 0xFF);
        stream.write((val >> 8) & 0xFF);
        stream.write((val >> 16) & 0xFF);
        stream.write((val >> 24) & 0xFF);
    }

    @LuaWhitelist
    @LuaMethodDoc("write_utils.write_long_le")
    public static void writeLongLE(OutputStream stream, long val) throws IOException {
        stream.write((int) (val & 0xFF));
        stream.write((int) ((val >> 8) & 0xFF));
        stream.write((int) ((val >> 16) & 0xFF));
        stream.write((int) ((val >> 24) & 0xFF));
        stream.write((int) ((val >> 32) & 0xFF));
        stream.write((int) ((val >> 40) & 0xFF));
        stream.write((int) ((val >> 48) & 0xFF));
        stream.write((int) ((val >> 56) & 0xFF));
    }

    @LuaWhitelist
    @LuaMethodDoc("write_utils.write_float_le")
    public static void writeFloatLE(OutputStream stream, float val) throws IOException {
        writeIntLE(stream, Float.floatToIntBits(val));
    }

    @LuaWhitelist
    @LuaMethodDoc("write_utils.write_double_le")
    public static void writeDoubleLE(OutputStream stream, double val) throws IOException {
        writeLongLE(stream, Double.doubleToLongBits(val));
    }
}
