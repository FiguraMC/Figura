package org.figuramc.figura.lua.api.data.readers;

import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaFieldDoc;
import org.figuramc.figura.lua.docs.LuaMethodDoc;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

@LuaWhitelist
@LuaTypeDoc(name = "StringReader", value = "string_reader")
public class StringReader extends FiguraReader<String> {
    public static final Readers READERS = new Readers();
    private final Charset encodingCharset;

    private StringReader(Charset encodingCharset) {
        this.encodingCharset = encodingCharset;
    }

    @Override
    @LuaWhitelist
    @LuaMethodDoc("string_reader.read_from")
    public String readFrom(InputStream stream) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            stream.transferTo(baos);
            String s = baos.toString(encodingCharset);
            baos.close();
            return s;
        } catch (IOException e) {
            throw new LuaError(e);
        }
    }

    @LuaWhitelist
    @LuaTypeDoc(name = "StringReaders", value = "string_reader.readers")
    public static class Readers {
        private Readers() {}
        @LuaFieldDoc("string_reader.readers.utf_8")
        public static final StringReader utf_8 = new StringReader(StandardCharsets.UTF_8);
        @LuaFieldDoc("string_reader.readers.utf_16")
        public static final StringReader utf_16 = new StringReader(StandardCharsets.UTF_16);
        @LuaFieldDoc("string_reader.readers.utf_16be")
        public static final StringReader utf_16be = new StringReader(StandardCharsets.UTF_16BE);
        @LuaFieldDoc("string_reader.readers.utf_16le")
        public static final StringReader utf_16le = new StringReader(StandardCharsets.UTF_16LE);
        @LuaFieldDoc("string_reader.readers.iso_8859_1")
        public static final StringReader iso_8859_1 = new StringReader(StandardCharsets.ISO_8859_1);
        @LuaFieldDoc("string_reader.readers.ascii")
        public static final StringReader ascii = new StringReader(StandardCharsets.US_ASCII);
        @LuaWhitelist
        public StringReader __index(LuaValue key) {
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

        @Override
        public String toString() {
            return "StringReaders";
        }
    }

    @Override
    public String toString() {
        return "StringReader";
    }
}
