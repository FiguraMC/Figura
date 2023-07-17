package org.figuramc.figura.lua.api.data.providers;

import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.api.data.FiguraInputStream;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaTypeDoc;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@LuaWhitelist
@LuaTypeDoc(name = "StringProvider", value = "string_provider")
public class StringProvider extends FiguraProvider<String> {

    public static final StringProvider UTF_8_PROVIDER = new StringProvider(StandardCharsets.UTF_8);
    public static final StringProvider UTF_16_PROVIDER = new StringProvider(StandardCharsets.UTF_16);
    public static final StringProvider UTF_16BE_PROVIDER = new StringProvider(StandardCharsets.UTF_16BE);
    public static final StringProvider UTF_16LE_PROVIDER = new StringProvider(StandardCharsets.UTF_16LE);
    public static final StringProvider ISO_8859_1_PROVIDER = new StringProvider(StandardCharsets.ISO_8859_1);
    public static final StringProvider US_ASCII_PROVIDER = new StringProvider(StandardCharsets.US_ASCII);
    
    private final Charset encodingCharset;

    public StringProvider(Charset encodingCharset) {
        this.encodingCharset = encodingCharset;
    }

    @Override
    @LuaMethodDoc("string_provider.get_stream")
    public FiguraInputStream getStream(String data) {
        ByteArrayInputStream bais = new ByteArrayInputStream(data.getBytes(encodingCharset));
        return new FiguraInputStream(bais);
    }
}
