package org.figuramc.figura.lua.api.data;

import org.figuramc.figura.lua.LuaNotNil;
import org.luaj.vm2.LuaError;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public interface FiguraWritable {
    private static void writeBytes(FiguraWritable writable, byte[] bytes) {
        for (byte aByte : bytes) {
            writable.write(aByte & 0xff);
        }
    }
    void write(@LuaNotNil int val);
    default void writeShort(@LuaNotNil Integer val) {
        if (val >= Short.MIN_VALUE && val <= Short.MAX_VALUE) {
            short s = (short) (int) val;
            write((s >> 8) & 0xFF);
            write(s & 0xFF);
        }
        else throw new LuaError("Value %s is out of range [%s; %s]".formatted(val, Short.MIN_VALUE, Short.MAX_VALUE));
    }
    default void writeUShort(@LuaNotNil Integer val) {
        if (val >= 0 && val <= Character.MAX_VALUE) {
            char s = (char) (int) val;
            write((s >> 8) & 0xFF);
            write(s & 0xFF);
        }
        else throw new LuaError("Value %s is out of range [%s; %s]".formatted(val, 0, (int) Character.MAX_VALUE));
    }
    default void writeInt(@LuaNotNil Integer val) {
        write((val >> 24) & 0xFF);
        write((val >> 16) & 0xFF);
        write((val >> 8) & 0xFF);
        write(val & 0xFF);
    }
    default void writeLong(@LuaNotNil Long val) {
        write((int) ((val >> 56) & 0xFF));
        write((int) ((val >> 48) & 0xFF));
        write((int) ((val >> 40) & 0xFF));
        write((int) ((val >> 32) & 0xFF));
        write((int) ((val >> 24) & 0xFF));
        write((int) ((val >> 16) & 0xFF));
        write((int) ((val >> 8) & 0xFF));
        write((int) (val & 0xFF));
    }
    default void writeFloat(@LuaNotNil Float val) {
        writeInt(Float.floatToIntBits(val));
    }
    default void writeDouble(@LuaNotNil Double val) {
        writeLong(Double.doubleToLongBits(val));
    }
    default void writeShortLE(@LuaNotNil Integer val) {
        if (val >= Short.MIN_VALUE && val <= Short.MAX_VALUE) {
            short s = (short) (int) val;
            write(s & 0xFF);
            write((s >> 8) & 0xFF);
        }
        else throw new LuaError("Value %s is out of range [%s; %s]".formatted(val, Short.MIN_VALUE, Short.MAX_VALUE));
    }
    default void writeUShortLE(@LuaNotNil Integer val) {
        if (val >= 0 && val <= Character.MAX_VALUE) {
            char s = (char) (int) val;
            write(s & 0xFF);
            write((s >> 8) & 0xFF);
        }
        else throw new LuaError("Value %s is out of range [%s; %s]".formatted(val, 0, (int) Character.MAX_VALUE));
    }
    default void writeIntLE(@LuaNotNil Integer val) {
        write(val & 0xFF);
        write((val >> 8) & 0xFF);
        write((val >> 16) & 0xFF);
        write((val >> 24) & 0xFF);
    }
    default void writeLongLE(@LuaNotNil Long val) {
        write((int) (val & 0xFF));
        write((int) ((val >> 8) & 0xFF));
        write((int) ((val >> 16) & 0xFF));
        write((int) ((val >> 24) & 0xFF));
        write((int) ((val >> 32) & 0xFF));
        write((int) ((val >> 40) & 0xFF));
        write((int) ((val >> 48) & 0xFF));
        write((int) ((val >> 56) & 0xFF));
    }
    default void writeFloatLE(@LuaNotNil Float val) {
        writeIntLE(Float.floatToIntBits(val));
    }
    default void writeDoubleLE(@LuaNotNil Double val) {
        writeLongLE(Double.doubleToLongBits(val));
    }
    default int writeString(@LuaNotNil String val, String encoding) {
        Charset charset = encoding == null ? StandardCharsets.UTF_8 : switch (encoding.toLowerCase()) {
            case "utf_16", "utf16" -> StandardCharsets.UTF_16;
            case "utf_16be", "utf16be" -> StandardCharsets.UTF_16BE;
            case "utf_16le", "utf16le" -> StandardCharsets.UTF_16LE;
            case "ascii" -> StandardCharsets.US_ASCII;
            case "iso_8859_1", "iso88591" -> StandardCharsets.ISO_8859_1;
            default -> StandardCharsets.UTF_8;
        };
        byte[] strBytes = val.getBytes(charset);
        writeBytes(this, strBytes);
        return strBytes.length;
    }
}
