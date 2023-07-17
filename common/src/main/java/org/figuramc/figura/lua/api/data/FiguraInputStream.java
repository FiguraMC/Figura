package org.figuramc.figura.lua.api.data;

import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaTypeDoc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

@LuaWhitelist
@LuaTypeDoc(name = "InputStream", value = "input_stream")
public class FiguraInputStream extends InputStream {
    private final InputStream sourceStream;

    public FiguraInputStream(InputStream sourceStream) {
        this.sourceStream = sourceStream;
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc("input_stream.read")
    public int read() throws IOException {
        return sourceStream.read();
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc("input_stream.skip")
    public long skip(long n) throws IOException {
        return sourceStream.skip(n);
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc("input_stream.available")
    public int available() throws IOException {
        return sourceStream.available();
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc("input_stream.close")
    public void close() throws IOException {
        sourceStream.close();
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc("input_stream.mark")
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
    @LuaMethodDoc("input_stream.transfer_to")
    public long transferTo(OutputStream out) throws IOException {
        return sourceStream.transferTo(out);
    }
}
