package org.figuramc.figura.lua.api.data.providers;

import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.api.data.FiguraInputStream;
import org.figuramc.figura.lua.docs.LuaFieldDoc;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.luaj.vm2.LuaValue;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@LuaWhitelist
@LuaTypeDoc(name = "StringProvider", value = "string_provider")
public class StringProvider extends FiguraProvider<String> {

    public static final Providers PROVIDERS = new Providers();
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

    @LuaWhitelist
    @LuaTypeDoc(name = "StringProviders", value = "string_provider.providers")
    public static class Providers {
        private Providers() {}
        @LuaFieldDoc("string_provider.providers.utf_8")
        public static final StringProvider utf_8 = new StringProvider(StandardCharsets.UTF_8);
        @LuaFieldDoc("string_provider.providers.utf_16")
        public static final StringProvider utf_16 = new StringProvider(StandardCharsets.UTF_16);
        @LuaFieldDoc("string_provider.providers.utf_16be")
        public static final StringProvider utf_16be = new StringProvider(StandardCharsets.UTF_16BE);
        @LuaFieldDoc("string_provider.providers.utf_16le")
        public static final StringProvider utf_16le = new StringProvider(StandardCharsets.UTF_16LE);
        @LuaFieldDoc("string_provider.providers.iso_8859_1")
        public static final StringProvider iso_8859_1 = new StringProvider(StandardCharsets.ISO_8859_1);
        @LuaFieldDoc("string_provider.providers.ascii")
        public static final StringProvider ascii = new StringProvider(StandardCharsets.US_ASCII);
        @LuaWhitelist
        public StringProvider __index(LuaValue key) {
            if (!key.isstring()) return null;
            return switch (key.tojstring()) {
                case "utf_8" -> utf_8;
                case "utf_16" -> utf_16;
                case "utf_16be" -> utf_16be;
                case "utf_16le" -> utf_16le;
                case "ascii" -> ascii;
                case "iso_8859_1" -> iso_8859_1;
                default -> null;
            };
        }
    }
}
