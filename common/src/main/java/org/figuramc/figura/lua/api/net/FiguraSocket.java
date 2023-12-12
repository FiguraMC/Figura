package org.figuramc.figura.lua.api.net;

import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.api.data.FiguraInputStream;
import org.figuramc.figura.lua.api.data.FiguraOutputStream;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaMethodOverload;
import org.figuramc.figura.lua.docs.LuaTypeDoc;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

@LuaWhitelist
@LuaTypeDoc(value = "socket", name = "Socket")
public class FiguraSocket implements AutoCloseable {
    private final Avatar parent;
    private final Socket parentSocket;
    private final FiguraInputStream socketInputStream;
    private final FiguraOutputStream socketOutputStream;

    public FiguraSocket(String host, int port, Avatar parent) throws IOException {
        this.parent = parent;
        this.parentSocket = new Socket(host, port);
        this.socketInputStream = new FiguraInputStream(parentSocket.getInputStream(), true);
        this.socketOutputStream = new FiguraOutputStream(parentSocket.getOutputStream());
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "socket.get_input_stream",
            overloads = @LuaMethodOverload(
                    returnType = FiguraInputStream.class
            )
    )
    public FiguraInputStream getInputStream() {
        return socketInputStream;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "socket.get_output_stream",
            overloads = @LuaMethodOverload(
                    returnType = FiguraOutputStream.class
            )
    )
    public FiguraOutputStream getOutputStream() {
        return socketOutputStream;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "socket.get_port",
            overloads = @LuaMethodOverload(
                    returnType = int.class
            )
    )
    public int getPort() {
        return parentSocket.getPort();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "socket.get_host",
            overloads = @LuaMethodOverload(
                    returnType = String.class
            )
    )
    public String getHost() {
        return parentSocket.getInetAddress().getHostAddress();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "socket.is_connected",
            overloads = @LuaMethodOverload(
                    returnType = boolean.class
            )
    )
    public boolean isConnected() {
        return parentSocket.isConnected();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            value = "socket.is_closed",
            overloads = @LuaMethodOverload(
                    returnType = boolean.class
            )
    )
    public boolean isClosed() {
        return parentSocket.isClosed();
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc("socket.close")
    public void close() throws IOException {
        baseClose();
        parent.openSockets.remove(this);
    }

    public void baseClose() throws IOException {
        if (!isClosed()) {
            parentSocket.close();
        }
    }

    @Override
    public String toString() {
        InetAddress address = parentSocket.getInetAddress();
        return "Socket(%s;%s)".formatted(address.getHostAddress(), parentSocket.getPort());
    }
}
