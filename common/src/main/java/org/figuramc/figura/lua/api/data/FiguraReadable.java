package org.figuramc.figura.lua.api.data;

import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public interface FiguraReadable {
    int read();
    int available();
    private static byte[] readNBytes(FiguraReadable readable, int count) {
        byte[] arr = new byte[count];
        int b;
        int i = 0;
        for (; i < count && (b = readable.read()) != -1; i++) {
            arr[i] = (byte) b;
        }
        if (i < count) {
            byte[] newArr = new byte[i];
            System.arraycopy(arr, 0, newArr ,0, i);
            arr = newArr;
        }
        return arr;
    }
    default int readShort() {
        byte[] bytes = readNBytes(this, 2);
        short v = 0;
        for (int i = 0; i < bytes.length; i++) {
            v |= (short) ((bytes[((bytes.length - 1) - i)] & 0xFF) << (i * 8));
        }
        return v;
    }
    default int readUShort() {
        byte[] bytes = readNBytes(this, 2);
        int v = 0;
        for (int i = 0; i < bytes.length; i++) {
            v |= (bytes[((bytes.length - 1) - i)] & 0xFF) << (i * 8);
        }
        return v;
    }
    default int readInt() {
        byte[] bytes = readNBytes(this, 4);
        int v = 0;
        for (int i = 0; i < bytes.length; i++) {
            v |= (bytes[((bytes.length - 1) - i)] & 0xFF) << (i * 8);
        }
        return v;
    }
    default long readLong() {
        byte[] bytes = readNBytes(this,8);
        long v = 0;
        for (int i = 0; i < bytes.length; i++) {
            v |= (long) (bytes[((bytes.length - 1) - i)] & 0xFF) << (i * 8);
        }
        return v;
    }
    default float readFloat() {
        return Float.intBitsToFloat(readInt());
    }
    default double readDouble() {
        return Double.longBitsToDouble(readLong());
    }
    default int readShortLE() {
        byte[] bytes = readNBytes(this, 2);
        short v = 0;
        for (int i = 0; i < bytes.length; i++) {
            v |= (short) ((bytes[i] & 0xFF) << (i * 8));
        }
        return v;
    }
    default int readUShortLE() {
        byte[] bytes = readNBytes(this, 2);
        int v = 0;
        for (int i = 0; i < bytes.length; i++) {
            v |= (bytes[i] & 0xFF) << (i * 8);
        }
        return v;
    }
    default int readIntLE() {
        byte[] bytes = readNBytes(this,4);
        int v = 0;
        for (int i = 0; i < bytes.length; i++) {
            v |= (bytes[i] & 0xFF) << (i * 8);
        }
        return v;
    }
    default long readLongLE() {
        byte[] bytes = readNBytes(this,8);
        long v = 0;
        for (int i = 0; i < bytes.length; i++) {
            v |= (long) (bytes[i] & 0xFF) << (i * 8);
        }
        return v;
    }
    default float readFloatLE() {
        return Float.intBitsToFloat(readIntLE());
    }
    default double readDoubleLE() {
        return Double.longBitsToDouble(readLongLE());
    }
    default String readString(Integer length, String encoding) {
        length = length == null ? 1024 : Math.max(length, 0);
        Charset charset = encoding == null ? StandardCharsets.UTF_8 : switch (encoding.toLowerCase()) {
            case "utf_16", "utf16" -> StandardCharsets.UTF_16;
            case "utf_16be", "utf16be" -> StandardCharsets.UTF_16BE;
            case "utf_16le", "utf16le" -> StandardCharsets.UTF_16LE;
            case "ascii" -> StandardCharsets.US_ASCII;
            case "iso_8859_1", "iso88591" -> StandardCharsets.ISO_8859_1;
            default -> StandardCharsets.UTF_8;
        };
        byte[] strBuf = readNBytes(this, length);
        return new String(strBuf, charset);
    }

    default String readBase64(Integer length) {
        length = length == null ? 1024 : Math.max(length, 0);
        byte[] strBuf = readNBytes(this, length);
        return Base64.getEncoder().encodeToString(strBuf);
    }

    default LuaString readByteArray(Integer length) {
        length = length == null ? 1024 : Math.max(length, 0);
        byte[] strBuf = readNBytes(this, length);
        return LuaString.valueOf(strBuf);
    }
}
