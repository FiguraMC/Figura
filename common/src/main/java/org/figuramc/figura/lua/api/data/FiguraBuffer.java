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
public class FiguraBuffer implements FiguraReadable, FiguraWritable {
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

    @LuaWhitelist
    @LuaMethodDoc("buffer.read_byte")
    public int read() {
        if (position >= length) {
            return -1;
        }
        int v = buf[position] & 0xff;
        position++;
        return v;
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.read_short")
    public int readShort() {
        return FiguraReadable.super.readShort();
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.read_ushort")
    public int readUShort() {
        return FiguraReadable.super.readUShort();
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.read_int")
    public int readInt() {
        return FiguraReadable.super.readInt();
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.read_long")
    public long readLong() {
        return FiguraReadable.super.readLong();
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.read_float")
    public float readFloat() {
        return FiguraReadable.super.readFloat();
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.read_double")
    public double readDouble() {
        return FiguraReadable.super.readDouble();
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.read_short_le")
    public int readShortLE() {
        return FiguraReadable.super.readShortLE();
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.read_ushort_le")
    public int readUShortLE() {
        return FiguraReadable.super.readUShortLE();
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.read_int_le")
    public int readIntLE() {
        return FiguraReadable.super.readIntLE();
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.read_long_le")
    public long readLongLE() {
        return FiguraReadable.super.readLongLE();
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.read_float_le")
    public float readFloatLE() {
        return FiguraReadable.super.readFloatLE();
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.read_double_le")
    public double readDoubleLE() {
        return FiguraReadable.super.readDoubleLE();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "buffer.read_string",
            overloads = {
                    @LuaMethodOverload(
                            returnType = String.class
                    ),
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
        return FiguraReadable.super.readString(length, encoding);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "buffer.write_byte",
            overloads = @LuaMethodOverload(
                    argumentNames = "val",
                    argumentTypes = Integer.class
            )
    )
    public void write(@LuaNotNil int val) {
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
            value = "buffer.write_short",
            overloads = @LuaMethodOverload(
                    argumentNames = "val",
                    argumentTypes = Integer.class
            )
    )
    public void writeShort(@LuaNotNil Integer val) {
        FiguraWritable.super.writeShort(val);
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
        FiguraWritable.super.writeUShort(val);
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
        FiguraWritable.super.writeInt(val);
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
        FiguraWritable.super.writeLong(val);
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
        FiguraWritable.super.writeFloat(val);
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
        FiguraWritable.super.writeDouble(val);
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
        FiguraWritable.super.writeShortLE(val);
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
        FiguraWritable.super.writeUShortLE(val);
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
        FiguraWritable.super.writeIntLE(val);
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
        FiguraWritable.super.writeLongLE(val);
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
        FiguraWritable.super.writeFloatLE(val);
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
        FiguraWritable.super.writeDoubleLE(val);
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
        return FiguraWritable.super.writeString(val, encoding);
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
        for (i = 0; i < amount; i++) {
            int b = stream.read();
            if (b == -1) break;
            write(b);
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
        for (int i = 0; i < amount; i++) {
            stream.write(read());
        }
        return amount;
    }

    @Override
    public String toString() {
        return "Buffer";
    }
}
