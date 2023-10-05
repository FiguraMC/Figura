package org.figuramc.figura.lua.api.data;

import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.luaj.vm2.LuaError;

import java.io.IOException;
import java.io.OutputStream;

@LuaWhitelist
@LuaTypeDoc(name = "OutputStream", value = "output_stream")
public class FiguraOutputStream extends OutputStream implements FiguraWritable {

    private final OutputStream destinationStream;

    public FiguraOutputStream(OutputStream destinationStream) {
        this.destinationStream = destinationStream;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "output_stream.write",
            overloads = @LuaMethodOverload(
                    argumentNames = "b",
                    argumentTypes = Integer.class
            )
    )
    @Override
    public void write(int b) {
        try {
            destinationStream.write(b);
        } catch (IOException e) {
            throw new LuaError(e);
        }
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc("output_stream.flush")
    public void flush() throws IOException {
        destinationStream.flush();
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc("output_stream.close")
    public void close() throws IOException {
        destinationStream.close();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "output_stream.write_short",
            overloads = @LuaMethodOverload(
                    argumentNames = "val",
                    argumentTypes = Integer.class
            )
    )
    @Override
    public void writeShort(Integer val) {
        FiguraWritable.super.writeShort(val);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "output_stream.write_ushort",
            overloads = @LuaMethodOverload(
                    argumentNames = "val",
                    argumentTypes = Integer.class
            )
    )
    @Override
    public void writeUShort(Integer val) {
        FiguraWritable.super.writeUShort(val);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "output_stream.write_int",
            overloads = @LuaMethodOverload(
                    argumentNames = "val",
                    argumentTypes = Integer.class
            )
    )
    @Override
    public void writeInt(Integer val) {
        FiguraWritable.super.writeInt(val);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "output_stream.write_long",
            overloads = @LuaMethodOverload(
                    argumentNames = "val",
                    argumentTypes = Long.class
            )
    )
    @Override
    public void writeLong(Long val) {
        FiguraWritable.super.writeLong(val);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "output_stream.write_float",
            overloads = @LuaMethodOverload(
                    argumentNames = "val",
                    argumentTypes = Float.class
            )
    )
    @Override
    public void writeFloat(Float val) {
        FiguraWritable.super.writeFloat(val);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "output_stream.write_double",
            overloads = @LuaMethodOverload(
                    argumentNames = "val",
                    argumentTypes = Double.class
            )
    )
    @Override
    public void writeDouble(Double val) {
        FiguraWritable.super.writeDouble(val);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "output_stream.write_short_le",
            overloads = @LuaMethodOverload(
                    argumentNames = "val",
                    argumentTypes = Integer.class
            )
    )
    @Override
    public void writeShortLE(Integer val) {
        FiguraWritable.super.writeShortLE(val);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "output_stream.write_ushort_le",
            overloads = @LuaMethodOverload(
                    argumentNames = "val",
                    argumentTypes = Integer.class
            )
    )
    @Override
    public void writeUShortLE(Integer val) {
        FiguraWritable.super.writeUShortLE(val);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "output_stream.write_int_le",
            overloads = @LuaMethodOverload(
                    argumentNames = "val",
                    argumentTypes = Integer.class
            )
    )
    @Override
    public void writeIntLE(Integer val) {
        FiguraWritable.super.writeIntLE(val);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "output_stream.write_long_le",
            overloads = @LuaMethodOverload(
                    argumentNames = "val",
                    argumentTypes = Long.class
            )
    )
    @Override
    public void writeLongLE(Long val) {
        FiguraWritable.super.writeLongLE(val);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "output_stream.write_float_le",
            overloads = @LuaMethodOverload(
                    argumentNames = "val",
                    argumentTypes = Float.class
            )
    )
    @Override
    public void writeFloatLE(Float val) {
        FiguraWritable.super.writeFloatLE(val);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "output_stream.write_double_le",
            overloads = @LuaMethodOverload(
                    argumentNames = "val",
                    argumentTypes = Double.class
            )
    )
    @Override
    public void writeDoubleLE(Double val) {
        FiguraWritable.super.writeDoubleLE(val);
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "output_stream.write_string",
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
    @Override
    public int writeString(String val, String encoding) {
        return FiguraWritable.super.writeString(val, encoding);
    }

    @Override
    public String toString() {
        return "OutputStream";
    }
}
