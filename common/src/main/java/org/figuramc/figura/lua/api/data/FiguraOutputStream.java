package org.figuramc.figura.lua.api.data;

import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;

import java.io.IOException;
import java.io.OutputStream;

@LuaWhitelist
@LuaTypeDoc(name = "OutputStream", value = "output_stream")
public class FiguraOutputStream extends OutputStream {

    private final OutputStream destinationStream;

    public FiguraOutputStream(OutputStream destinationStream) {
        this.destinationStream = destinationStream;
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc(
            value = "output_stream.write",
            overloads = @LuaMethodOverload(
                    argumentNames = "b",
                    argumentTypes = Integer.class
            )
    )
    public void write(int b) throws IOException {
        destinationStream.write(b);
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

    @Override
    public String toString() {
        return "OutputStream";
    }
}
