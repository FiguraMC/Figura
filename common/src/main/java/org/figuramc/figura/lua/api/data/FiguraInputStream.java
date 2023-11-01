package org.figuramc.figura.lua.api.data;

import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaString;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@LuaWhitelist
@LuaTypeDoc(name = "InputStream", value = "input_stream")
public class FiguraInputStream extends InputStream implements FiguraReadable {
    private final InputStream sourceStream;

    public FiguraInputStream(InputStream sourceStream) {
        this.sourceStream = sourceStream;
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc("input_stream.read")
    public int read() {
        try {
            return sourceStream.read();
        } catch (IOException e) {
            throw new LuaError(e);
        }
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            value = "input_stream.skip",
            overloads = @LuaMethodOverload(
                    argumentNames = "n",
                    argumentTypes = Long.class,
                    returnType = Long.class
            )
    )
    public long skip(long n) throws IOException {
        return sourceStream.skip(n);
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc("input_stream.available")
    public int available() {
        try {
            return sourceStream.available();
        } catch (IOException e) {
            throw new LuaError(e);
        }
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc("input_stream.close")
    public void close() throws IOException {
        sourceStream.close();
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            value = "input_stream.mark",
            overloads = @LuaMethodOverload(
                    argumentNames = "readLimit",
                    argumentTypes = Integer.class
            )
    )
    public void mark(int readlimit) {
        sourceStream.mark(readlimit);
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc("input_stream.reset")
    public void reset() throws IOException {
        sourceStream.reset();
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc("input_stream.mark_supported")
    public boolean markSupported() {
        return sourceStream.markSupported();
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            value = "input_stream.transfer_to",
            overloads = @LuaMethodOverload(
                    argumentNames = "out",
                    argumentTypes = FiguraOutputStream.class,
                    returnType = Long.class
            )
    )
    public long transferTo(OutputStream out) throws IOException {
        return sourceStream.transferTo(out);
    }

    @LuaWhitelist
    @LuaMethodDoc("input_stream.read_short")
    @Override
    public int readShort() {
        return FiguraReadable.super.readShort();
    }

    @LuaWhitelist
    @LuaMethodDoc("input_stream.read_ushort")
    @Override
    public int readUShort() {
        return FiguraReadable.super.readUShort();
    }

    @LuaWhitelist
    @LuaMethodDoc("input_stream.read_int")
    @Override
    public int readInt() {
        return FiguraReadable.super.readInt();
    }

    @LuaWhitelist
    @LuaMethodDoc("input_stream.read_long")
    @Override
    public long readLong() {
        return FiguraReadable.super.readLong();
    }

    @LuaWhitelist
    @LuaMethodDoc("input_stream.read_float")
    @Override
    public float readFloat() {
        return FiguraReadable.super.readFloat();
    }

    @LuaWhitelist
    @LuaMethodDoc("input_stream.read_double")
    @Override
    public double readDouble() {
        return FiguraReadable.super.readDouble();
    }

    @LuaWhitelist
    @LuaMethodDoc("input_stream.read_short_le")
    @Override
    public int readShortLE() {
        return FiguraReadable.super.readShortLE();
    }

    @LuaWhitelist
    @LuaMethodDoc("input_stream.read_ushort_le")
    @Override
    public int readUShortLE() {
        return FiguraReadable.super.readUShortLE();
    }

    @LuaWhitelist
    @LuaMethodDoc("input_stream.read_int_le")
    @Override
    public int readIntLE() {
        return FiguraReadable.super.readIntLE();
    }

    @LuaWhitelist
    @LuaMethodDoc("input_stream.read_long_le")
    @Override
    public long readLongLE() {
        return FiguraReadable.super.readLongLE();
    }

    @LuaWhitelist
    @LuaMethodDoc("input_stream.read_float_le")
    @Override
    public float readFloatLE() {
        return FiguraReadable.super.readFloatLE();
    }

    @LuaWhitelist
    @LuaMethodDoc("input_stream.read_double_le")
    @Override
    public double readDoubleLE() {
        return FiguraReadable.super.readDoubleLE();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "input_stream.read_string",
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
    @Override
    public String readString(Integer length, String encoding) {
        return FiguraReadable.super.readString(length, encoding);
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            value = "input_stream.read_base_64",
            overloads = {
                    @LuaMethodOverload(
                            returnType = String.class
                    ),
                    @LuaMethodOverload(
                            argumentNames = "length",
                            argumentTypes = Integer.class,
                            returnType = String.class
                    )
            }
    )
    public String readBase64(Integer length) {
        return FiguraReadable.super.readBase64(length);
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            value = "input_stream.read_byte_array",
            overloads = {
                    @LuaMethodOverload(
                            returnType = String.class
                    ),
                    @LuaMethodOverload(
                            argumentNames = "length",
                            argumentTypes = Integer.class,
                            returnType = String.class
                    )
            }
    )
    public LuaString readByteArray(Integer length) {
        return FiguraReadable.super.readByteArray(length);
    }

    @Override
    public String toString() {
        return "InputStream";
    }
}
