package org.figuramc.figura.lua.api.data;

import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

@LuaWhitelist
@LuaTypeDoc(value = "buffer", name = "Buffer")
public class FiguraBuffer {
    private static final int CAPACITY_GROW_STEP = 512;
    private final Avatar parent;
    private int length = 0, position = 0;
    private byte[] buf;

    public FiguraBuffer(Avatar parent) {
        buf = new byte[CAPACITY_GROW_STEP];
        this.parent = parent;
    }

    public FiguraBuffer(Avatar parent, int cap) {
        buf = new byte[cap];
        this.parent = parent;
    }

    private void ensureBufCapacity(int cap) {
        if (buf.length < cap) {
            buf = Arrays.copyOf(buf, buf.length + CAPACITY_GROW_STEP);
        }
    }

    private int read() {
        if (position >= length) {
            return -1;
        }
        int v = buf[position] & 0xff;
        position++;
        return v;
    }

    private byte[] readNBytes(int count) {
        byte[] arr = new byte[Math.min(count, length-position)];
        System.arraycopy(buf, position, arr, 0, arr.length);
        position += arr.length;
        return arr;
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.read_byte")
    public int readByte() {
        return read();
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.read_short")
    public int readShort() {
        byte[] bytes = readNBytes(2);
        short v = 0;
        for (int i = 0; i < bytes.length; i++) {
            v |= (bytes[((bytes.length - 1) - i)] & 0xFF) << (i * 8);
        }
        return v;
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.read_ushort")
    public int readUShort() {
        byte[] bytes = readNBytes(2);
        int v = 0;
        for (int i = 0; i < bytes.length; i++) {
            v |= (bytes[((bytes.length - 1) - i)] & 0xFF) << (i * 8);
        }
        return v;
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.read_int")
    public int readInt() {
        byte[] bytes = readNBytes(4);
        int v = 0;
        for (int i = 0; i < bytes.length; i++) {
            v |= (bytes[((bytes.length - 1) - i)] & 0xFF) << (i * 8);
        }
        return v;
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.read_long")
    public long readLong() {
        byte[] bytes = readNBytes(8);
        long v = 0;
        for (int i = 0; i < bytes.length; i++) {
            v |= (long) (bytes[((bytes.length - 1) - i)] & 0xFF) << (i * 8);
        }
        return v;
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.read_float")
    public float readFloat() {
        return Float.intBitsToFloat(readInt());
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.read_double")
    public double readDouble() {
        return Double.longBitsToDouble(readLong());
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.read_short_le")
    public int readShortLE() {
        byte[] bytes = readNBytes(2);
        short v = 0;
        for (int i = 0; i < bytes.length; i++) {
            v |= (bytes[i] & 0xFF) << (i * 8);
        }
        return v;
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.read_ushort_le")
    public int readUShortLE() {
        byte[] bytes = readNBytes(2);
        int v = 0;
        for (int i = 0; i < bytes.length; i++) {
            v |= (bytes[i] & 0xFF) << (i * 8);
        }
        return v;
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.read_int_le")
    public int readIntLE() {
        byte[] bytes = readNBytes(4);
        int v = 0;
        for (int i = 0; i < bytes.length; i++) {
            v |= (bytes[i] & 0xFF) << (i * 8);
        }
        return v;
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.read_long_le")
    public long readLongLE() {
        byte[] bytes = readNBytes(8);
        long v = 0;
        for (int i = 0; i < bytes.length; i++) {
            v |= (long) (bytes[i] & 0xFF) << (i * 8);
        }
        return v;
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.read_float_le")
    public float readFloatLE() {
        return Float.intBitsToFloat(readIntLE());
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.read_double_le")
    public double readDoubleLE() {
        return Double.longBitsToDouble(readLongLE());
    }

    private void write(int val) {
        if (length == position) {
            ensureBufCapacity(length++);
        }
        buf[position] = (byte) (val & 0xFF);
        position++;
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.write_byte")
    public void writeByte(int val) {
        write(val);
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.write_short")
    public void writeShort(int val) {
        if (val >= Short.MIN_VALUE && val <= Short.MAX_VALUE) {
            short s = (short) val;
            write((s >> 8) & 0xFF);
            write(s & 0xFF);
        }
        else throw new RuntimeException("Value %s is out of range [%s; %s]".formatted(val, Short.MIN_VALUE, Short.MAX_VALUE));
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.write_ushort")
    public void writeUShort(int val) {
        if (val >= 0 && val <= Character.MAX_VALUE) {
            char s = (char) val;
            write((s >> 8) & 0xFF);
            write(s & 0xFF);
        }
        else throw new RuntimeException("Value %s is out of range [%s; %s]".formatted(val, 0, (int) Character.MAX_VALUE));
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.write_int")
    public void writeInt(int val) {
        write((val >> 24) & 0xFF);
        write((val >> 16) & 0xFF);
        write((val >> 8) & 0xFF);
        write(val & 0xFF);
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.write_long")
    public void writeLong(long val) {
        write((int) ((val >> 56) & 0xFF));
        write((int) ((val >> 48) & 0xFF));
        write((int) ((val >> 40) & 0xFF));
        write((int) ((val >> 32) & 0xFF));
        write((int) ((val >> 24) & 0xFF));
        write((int) ((val >> 16) & 0xFF));
        write((int) ((val >> 8) & 0xFF));
        write((int) (val & 0xFF));
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.write_float")
    public void writeFloat(float val) {
        writeInt(Float.floatToIntBits(val));
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.write_double")
    public void writeDouble(double val) {
        writeLong(Double.doubleToLongBits(val));
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.write_short_le")
    public void writeShortLE(int val) {
        if (val >= Short.MIN_VALUE && val <= Short.MAX_VALUE) {
            short s = (short) val;
            write(s & 0xFF);
            write((s >> 8) & 0xFF);
        }
        else throw new RuntimeException("Value %s is out of range [%s; %s]".formatted(val, Short.MIN_VALUE, Short.MAX_VALUE));
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.write_ushort_le")
    public void writeUShortLE(int val) {
        if (val >= 0 && val <= Character.MAX_VALUE) {
            char s = (char) val;
            write(s & 0xFF);
            write((s >> 8) & 0xFF);
        }
        else throw new RuntimeException("Value %s is out of range [%s; %s]".formatted(val, 0, (int) Character.MAX_VALUE));
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.write_int_le")
    public void writeIntLE(int val) {
        write(val & 0xFF);
        write((val >> 8) & 0xFF);
        write((val >> 16) & 0xFF);
        write((val >> 24) & 0xFF);
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.write_long_le")
    public void writeLongLE(long val) {
        write((int) (val & 0xFF));
        write((int) ((val >> 8) & 0xFF));
        write((int) ((val >> 16) & 0xFF));
        write((int) ((val >> 24) & 0xFF));
        write((int) ((val >> 32) & 0xFF));
        write((int) ((val >> 40) & 0xFF));
        write((int) ((val >> 48) & 0xFF));
        write((int) ((val >> 56) & 0xFF));
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.write_float_le")
    public  void writeFloatLE(float val) {
        writeIntLE(Float.floatToIntBits(val));
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.write_double_le")
    public void writeDoubleLE(double val) {
        writeLongLE(Double.doubleToLongBits(val));
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.get_length")
    public int getLength() {
        return length;
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.get_position")
    public int getPosition() {
        return position;
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.set_position")
    public void setPosition(int position) {
        this.position = Math.max(Math.min(position, length), 0);
    }

    @LuaWhitelist
    @LuaMethodDoc("clear")
    public void clear() {
        position = 0;
        length = 0;
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.available")
    public int available() {
        return length-position;
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.max_capacity")
    public int getMaxCapacity() {
        return Integer.MAX_VALUE; // This is temp, i will change it later
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "buffer.read_from_stream",
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = {FiguraInputStream.class, Integer.class},
                            argumentNames = {"stream", "amount"},
                            returnType = Integer.class
                    )
            }
    )
    public int readFromStream(FiguraInputStream stream, Integer amount) {
        if (amount == null) amount = getMaxCapacity();
        else amount = Math.max(Math.min(amount, getMaxCapacity()), 0);
        int i;
        try {
            for (i = 0; i < amount; i++) {
                int b = stream.read();
                if (b == -1) break;
                write(b);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return i;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "buffer.write_to_stream",
            overloads = {
                    @LuaMethodOverload(
                          argumentTypes = {FiguraOutputStream.class, Integer.class},
                          argumentNames = {"stream", "amount"},
                          returnType = Integer.class
                    )
            }
    )
    public int writeToStream(FiguraOutputStream stream, Integer amount) {
        if (amount == null) amount = available();
        else amount = Math.max(Math.min(amount, available()), -1);
        try {
            for (int i = 0; i < amount; i++) {
                stream.write(read());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return amount;
    }
}
