package org.figuramc.figura.lua.api.data;

import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.LuaNotNil;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.permissions.Permissions;
import org.luaj.vm2.LuaError;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@LuaWhitelist
@LuaTypeDoc(value = "buffer", name = "Buffer")
public class FiguraBuffer {
    private static final int CAPACITY_INCREASE_STEP = 512;
    private final Avatar parent;
    private int length = 0, position = 0;
    private byte[] buf;

    public FiguraBuffer(Avatar parent) {
        this.parent = parent;
        if (CAPACITY_INCREASE_STEP > getMaxCapacity())  {
            parent.noPermissions.add(Permissions.BUFFER_SIZE);
            throw new LuaError("Unable to create buffer because max capacity is less than default buffer size (512)");
        }
        buf = new byte[CAPACITY_INCREASE_STEP];
    }

    public FiguraBuffer(Avatar parent, int cap) {
        this.parent = parent;
        if (cap > getMaxCapacity()) {
            parent.noPermissions.add(Permissions.BUFFER_SIZE);
            throw new LuaError("Unable to create a buffer with capacity %s");
        }
        buf = new byte[cap];
    }

    private void ensureBufCapacity(int cap) {
        if (cap > getMaxCapacity())
            throw new LuaError("Can't increase this buffer capacity to %s, max capacity is %s"
                    .formatted(cap, getMaxCapacity()));
        if (buf.length < cap) {
            buf = Arrays.copyOf(buf, buf.length + CAPACITY_INCREASE_STEP);
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

    @LuaWhitelist
    @LuaMethodDoc(
            value = "buffer.readString",
            overloads = {
                    @LuaMethodOverload(
                            argumentNames = "length",
                            argumentTypes = Integer.class,
                            returnType = String.class
                    ),
                    @LuaMethodOverload(
                            argumentNames = {"length", "encoding"},
                            argumentTypes = {Integer.class, String.class},
                            returnType = String.class
                    )
            }
    )
    public String readString(Integer length, String encoding) {
        length = length == null ? available() : Math.max(Math.min(length, available()), 0);
        Charset charset = encoding == null ? StandardCharsets.UTF_8 : switch (encoding.toLowerCase()) {
            case "utf_16", "utf16" -> StandardCharsets.UTF_16;
            case "utf_16be", "utf16be" -> StandardCharsets.UTF_16BE;
            case "utf_16le", "utf16le" -> StandardCharsets.UTF_16LE;
            case "ascii" -> StandardCharsets.US_ASCII;
            case "iso_8859_1", "iso88591" -> StandardCharsets.ISO_8859_1;
            default -> StandardCharsets.UTF_8;
        };
        byte[] strBuf = new byte[length];
        System.arraycopy(buf, position, strBuf, 0, strBuf.length);
        position += strBuf.length;
        return new String(strBuf, charset);
    }

    private void write(int val) {
        if (length == position) {
            ensureBufCapacity(length++);
        }
        buf[position] = (byte) (val & 0xFF);
        position++;
    }

    private void writeBytes(byte[] bytes) {
        if (length <= position + bytes.length) {
            length += position + bytes.length;
            ensureBufCapacity(length);
        }
        System.arraycopy(bytes, 0, buf, position, bytes.length);
        position += bytes.length;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "buffer.write_byte",
            overloads = @LuaMethodOverload(
                    argumentNames = "val",
                    argumentTypes = Integer.class
            )
    )
    public void writeByte(@LuaNotNil Integer val) {
        write(val);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "buffer.write_short",
            overloads = @LuaMethodOverload(
                    argumentNames = "val",
                    argumentTypes = Integer.class
            )
    )
    public void writeShort(@LuaNotNil Integer val) {
        if (val >= Short.MIN_VALUE && val <= Short.MAX_VALUE) {
            short s = (short) (int) val;
            write((s >> 8) & 0xFF);
            write(s & 0xFF);
        }
        else throw new LuaError("Value %s is out of range [%s; %s]".formatted(val, Short.MIN_VALUE, Short.MAX_VALUE));
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "buffer.write_ushort",
            overloads = @LuaMethodOverload(
                    argumentNames = "val",
                    argumentTypes = Integer.class
            )
    )
    public void writeUShort(@LuaNotNil Integer val) {
        if (val >= 0 && val <= Character.MAX_VALUE) {
            char s = (char) (int) val;
            write((s >> 8) & 0xFF);
            write(s & 0xFF);
        }
        else throw new LuaError("Value %s is out of range [%s; %s]".formatted(val, 0, (int) Character.MAX_VALUE));
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "buffer.write_int",
            overloads = @LuaMethodOverload(
                    argumentNames = "val",
                    argumentTypes = Integer.class
            )
    )
    public void writeInt(@LuaNotNil Integer val) {
        write((val >> 24) & 0xFF);
        write((val >> 16) & 0xFF);
        write((val >> 8) & 0xFF);
        write(val & 0xFF);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "buffer.write_long",
            overloads = @LuaMethodOverload(
                    argumentNames = "val",
                    argumentTypes = Long.class
            )
    )
    public void writeLong(@LuaNotNil Long val) {
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
    @LuaMethodDoc(
            value = "buffer.write_float",
            overloads = @LuaMethodOverload(
                    argumentNames = "val",
                    argumentTypes = Float.class
            )
    )
    public void writeFloat(@LuaNotNil Float val) {
        writeInt(Float.floatToIntBits(val));
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "buffer.write_double",
            overloads = @LuaMethodOverload(
                    argumentNames = "val",
                    argumentTypes = Double.class
            )
    )
    public void writeDouble(@LuaNotNil Double val) {
        writeLong(Double.doubleToLongBits(val));
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "buffer.write_short_le",
            overloads = @LuaMethodOverload(
                    argumentNames = "val",
                    argumentTypes = Integer.class
            )
    )
    public void writeShortLE(@LuaNotNil Integer val) {
        if (val >= Short.MIN_VALUE && val <= Short.MAX_VALUE) {
            short s = (short) (int) val;
            write(s & 0xFF);
            write((s >> 8) & 0xFF);
        }
        else throw new LuaError("Value %s is out of range [%s; %s]".formatted(val, Short.MIN_VALUE, Short.MAX_VALUE));
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "buffer.write_ushort_le",
            overloads = @LuaMethodOverload(
                    argumentNames = "val",
                    argumentTypes = Integer.class
            )
    )
    public void writeUShortLE(@LuaNotNil Integer val) {
        if (val >= 0 && val <= Character.MAX_VALUE) {
            char s = (char) (int) val;
            write(s & 0xFF);
            write((s >> 8) & 0xFF);
        }
        else throw new LuaError("Value %s is out of range [%s; %s]".formatted(val, 0, (int) Character.MAX_VALUE));
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "buffer.write_int_le",
            overloads = @LuaMethodOverload(
                    argumentNames = "val",
                    argumentTypes = Integer.class
            )
    )
    public void writeIntLE(@LuaNotNil Integer val) {
        write(val & 0xFF);
        write((val >> 8) & 0xFF);
        write((val >> 16) & 0xFF);
        write((val >> 24) & 0xFF);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "buffer.write_long_le",
            overloads = @LuaMethodOverload(
                    argumentNames = "val",
                    argumentTypes = Long.class
            )
    )
    public void writeLongLE(@LuaNotNil Long val) {
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
    @LuaMethodDoc(
            value = "buffer.write_float_le",
            overloads = @LuaMethodOverload(
                    argumentNames = "val",
                    argumentTypes = Float.class
            )
    )
    public void writeFloatLE(@LuaNotNil Float val) {
        writeIntLE(Float.floatToIntBits(val));
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "buffer.write_double_le",
            overloads = @LuaMethodOverload(
                    argumentNames = "val",
                    argumentTypes = Double.class
            )
    )
    public void writeDoubleLE(@LuaNotNil Double val) {
        writeLongLE(Double.doubleToLongBits(val));
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "buffer.write_string",
            overloads = {
                    @LuaMethodOverload(
                            argumentTypes = String.class,
                            argumentNames = "val",
                            returnType = Integer.class
                    ),
                    @LuaMethodOverload(
                            argumentTypes = {String.class, String.class},
                            argumentNames = {"val", "encoding"},
                            returnType = Integer.class
                    )
            }
    )
    public int writeString(@LuaNotNil String val, String encoding) {
        Charset charset = encoding == null ? StandardCharsets.UTF_8 : switch (encoding.toLowerCase()) {
            case "utf_16", "utf16" -> StandardCharsets.UTF_16;
            case "utf_16be", "utf16be" -> StandardCharsets.UTF_16BE;
            case "utf_16le", "utf16le" -> StandardCharsets.UTF_16LE;
            case "ascii" -> StandardCharsets.US_ASCII;
            case "iso_8859_1", "iso88591" -> StandardCharsets.ISO_8859_1;
            default -> StandardCharsets.UTF_8;
        };
        byte[] strBytes = val.getBytes(charset);
        writeBytes(strBytes);
        return strBytes.length;
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
    @LuaMethodDoc(
            value = "buffer.set_position",
            overloads = @LuaMethodOverload(
                    argumentNames = "position",
                    argumentTypes = Integer.class
            )
    )
    public void setPosition(@LuaNotNil Integer position) {
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
    @LuaMethodDoc("buffer.get_max_capacity")
    public int getMaxCapacity() {
        return parent.permissions.get(Permissions.BUFFER_SIZE);
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
    public int readFromStream(@LuaNotNil FiguraInputStream stream, Integer amount) {
        if (amount == null) amount = getMaxCapacity()-position;
        else amount = Math.max(Math.min(amount, getMaxCapacity()-position), 0);
        int i;
        try {
            for (i = 0; i < amount; i++) {
                int b = stream.read();
                if (b == -1) break;
                write(b);
            }
        } catch (IOException e) {
            throw new LuaError(e);
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
    public int writeToStream(@LuaNotNil FiguraOutputStream stream, Integer amount) {
        if (amount == null) amount = available();
        else amount = Math.max(Math.min(amount, available()), -1);
        try {
            for (int i = 0; i < amount; i++) {
                stream.write(read());
            }
        } catch (IOException e) {
            throw new LuaError(e);
        }
        return amount;
    }

    @Override
    public String toString() {
        return "Buffer";
    }
}
