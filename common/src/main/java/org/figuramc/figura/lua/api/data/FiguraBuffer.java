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
import java.io.InputStream;
import java.util.Arrays;
import java.util.Stack;

@LuaWhitelist
@LuaTypeDoc(value = "buffer", name = "Buffer")
public class FiguraBuffer implements FiguraReadable, FiguraWritable, AutoCloseable {
    private static final int CAPACITY_INCREASE_STEP = 512;
    private final Avatar parent;
    private int length = 0, position = 0;
    private byte[] buf;
    private boolean isClosed;

    public FiguraBuffer(Avatar parent) {
        this.parent = parent;
        if (parent.openBuffers > getMaxBuffersCount()) {
            parent.noPermissions.add(Permissions.BUFFERS_COUNT);
            throw new LuaError("You have exceed the max amount of open buffers");
        }
        if (CAPACITY_INCREASE_STEP > getMaxCapacity())  {
            parent.noPermissions.add(Permissions.BUFFER_SIZE);
            throw new LuaError("Unable to create buffer because max capacity is less than default buffer size (512)");
        }
        parent.openBuffers++;
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
    @LuaMethodDoc("buffer.read")
    public int read() {
        checkIsClosed();
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
        checkIsClosed();
        return FiguraReadable.super.readShort();
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.read_ushort")
    public int readUShort() {
        checkIsClosed();
        return FiguraReadable.super.readUShort();
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.read_int")
    public int readInt() {
        checkIsClosed();
        return FiguraReadable.super.readInt();
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.read_long")
    public long readLong() {
        checkIsClosed();
        return FiguraReadable.super.readLong();
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.read_float")
    public float readFloat() {
        checkIsClosed();
        return FiguraReadable.super.readFloat();
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.read_double")
    public double readDouble() {
        checkIsClosed();
        return FiguraReadable.super.readDouble();
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.read_short_le")
    public int readShortLE() {
        checkIsClosed();
        return FiguraReadable.super.readShortLE();
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.read_ushort_le")
    public int readUShortLE() {
        checkIsClosed();
        return FiguraReadable.super.readUShortLE();
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.read_int_le")
    public int readIntLE() {
        checkIsClosed();
        return FiguraReadable.super.readIntLE();
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.read_long_le")
    public long readLongLE() {
        checkIsClosed();
        return FiguraReadable.super.readLongLE();
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.read_float_le")
    public float readFloatLE() {
        checkIsClosed();
        return FiguraReadable.super.readFloatLE();
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.read_double_le")
    public double readDoubleLE() {
        checkIsClosed();
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
        checkIsClosed();
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
        checkIsClosed();
        if (length >= position) {
            ensureBufCapacity(length++);
            position = Math.min(position, length);
        }
        buf[position] = (byte) (val & 0xFF);
        position++;
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
        checkIsClosed();
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
        checkIsClosed();
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
        checkIsClosed();
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
        checkIsClosed();
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
        checkIsClosed();
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
        checkIsClosed();
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
        checkIsClosed();
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
        checkIsClosed();
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
        checkIsClosed();
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
        checkIsClosed();
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
        checkIsClosed();
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
        checkIsClosed();
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
        checkIsClosed();
        return FiguraWritable.super.writeString(val, encoding);
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.get_length")
    public int getLength() {
        checkIsClosed();
        return length;
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.get_position")
    public int getPosition() {
        checkIsClosed();
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
        checkIsClosed();
        this.position = Math.max(Math.min(position, length), 0);
    }

    @LuaWhitelist
    @LuaMethodDoc("clear")
    public void clear() {
        checkIsClosed();
        position = 0;
        length = 0;
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.available")
    public int available() {
        checkIsClosed();
        return length-position;
    }

    @LuaWhitelist
    @LuaMethodDoc("buffer.get_max_capacity")
    public int getMaxCapacity() {
        return parent.permissions.get(Permissions.BUFFER_SIZE);
    }

    private int getMaxBuffersCount() {
        return parent.permissions.get(Permissions.BUFFERS_COUNT);
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
        checkIsClosed();
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
        checkIsClosed();
        if (amount == null) amount = available();
        else amount = Math.max(Math.min(amount, available()), -1);
        for (int i = 0; i < amount; i++) {
            stream.write(read());
        }
        return amount;
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(value = "buffer.close")
    public void close() throws Exception {
        if (!isClosed) {
            isClosed = true;
            buf = null;
            parent.openBuffers--;
        }
    }

    @LuaWhitelist
    @LuaMethodDoc(value = "buffer.is_closed")
    public boolean isClosed() {
        return isClosed;
    }

    private void checkIsClosed() {
        if (isClosed) throw new LuaError("This byte buffer is closed and cant be used anymore");
    }

    @Override
    public String toString() {
        return "Buffer";
    }

    public FiguraBufferInputStream asInputStream() {
        return new FiguraBufferInputStream(this);
    }

    public static class FiguraBufferInputStream extends InputStream {
        private final FiguraBuffer parent;
        private final Stack<Mark> marks = new Stack<>();
        public FiguraBufferInputStream(FiguraBuffer figuraBuffer) {
            parent = figuraBuffer;
        }

        @Override
        public int read() throws IOException {
            if (!marks.empty()) {
                Mark m = marks.peek();
                if (parent.getPosition() > m.pos + m.readLimit) return -1;
            }
            return parent.read();
        }

        @Override
        public boolean markSupported() {
            return true;
        }

        @Override
        public void mark(int readlimit) {
            marks.push(new Mark(parent.position, readlimit));
        }

        @Override
        public void reset() throws IOException {
            Mark m = marks.pop();
            parent.setPosition(m.pos);
        }

        @Override
        public long skip(long n) throws IOException {
            long d = Math.min(parent.getLength() - parent.getPosition(), n);
            parent.setPosition((int)(parent.getPosition()+d));
            return d;
        }

        @Override
        public int available() throws IOException {
            return parent.available();
        }

        private record Mark(int pos, int readLimit) {
        }
    }
}
