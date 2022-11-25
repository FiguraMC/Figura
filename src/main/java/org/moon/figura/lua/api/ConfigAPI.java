package org.moon.figura.lua.api;

import com.google.gson.*;
import org.luaj.vm2.*;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.lua.LuaNotNil;
import org.moon.figura.lua.LuaWhitelist;
import org.moon.figura.lua.ReadOnlyLuaTable;
import org.moon.figura.lua.docs.LuaMethodDoc;
import org.moon.figura.lua.docs.LuaMethodOverload;
import org.moon.figura.lua.docs.LuaTypeDoc;
import org.moon.figura.math.matrix.FiguraMatrix;
import org.moon.figura.math.vector.FiguraVector;
import org.moon.figura.utils.MathUtils;

import java.io.FileOutputStream;
import java.io.FileReader;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@LuaWhitelist
@LuaTypeDoc(
        name = "ConfigAPI",
        value = "config"
)
public class ConfigAPI {

    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    private enum Type {
        BOOL,
        INT,
        DOUBLE,
        STRING,
        TABLE,
        VECTOR,
        MATRIX
    }

    private final LuaTable luaTable = new LuaTable();
    private final Avatar owner;
    private final boolean isHost;
    private String name;
    private boolean loaded = false;

    public ConfigAPI(Avatar owner) {
        this.owner = owner;
        this.isHost = owner.isHost;
        this.name = owner.name;
    }


    // -- IO -- //


    public static Path getConfigDataDir() {
        Path p = FiguraMod.getFiguraDirectory().resolve("data");
        try {
            Files.createDirectories(p);
        } catch (FileAlreadyExistsException ignored) {
        } catch (Exception e) {
            FiguraMod.LOGGER.error("Failed to create avatar data directory", e);
        }

        return p;
    }

    private Path getPath() {
        try {
            Path dir = getConfigDataDir().toAbsolutePath();
            Path path = dir.resolve(Paths.get(name + ".json")).toAbsolutePath();

            if (dir.compareTo(path.getParent()) != 0)
                throw new Exception();

            return path;
        } catch (Exception ignored) {
            throw new LuaError("Failed to parse file name \"" + name + "\"");
        }
    }

    //write
    private void write() {
        //parse file target
        Path path = getPath();

        //parse the table
        JsonObject root = new JsonObject();
        for (LuaValue key : luaTable.keys())
            root.add(key.toString(), writeArg(luaTable.get(key), new JsonObject()));

        //write file
        try (FileOutputStream fs = new FileOutputStream(path.toFile())) {
            fs.write(GSON.toJson(root).getBytes());
        } catch (Exception e) {
            FiguraMod.LOGGER.error("", e);
            throw new LuaError("Failed to save avatar data file");
        }
    }

    private static JsonElement writeArg(LuaValue val, JsonObject obj) {
        if (val.isboolean()) {
            obj.addProperty("type", Type.BOOL.name());
            obj.addProperty("data", val.checkboolean());
        } else if (val instanceof LuaString) {
            obj.addProperty("type", Type.STRING.name());
            obj.addProperty("data", val.checkjstring());
        } else if (val.isint()) {
            obj.addProperty("type", Type.INT.name());
            obj.addProperty("data", val.checkinteger().v);
        } else if (val.isnumber()) {
            obj.addProperty("type", Type.DOUBLE.name());
            obj.addProperty("data", val.checkdouble());
        } else if (val.istable()) {
            writeTable(val.checktable(), obj);
        } else if (val.isuserdata(FiguraVector.class)) {
            writeVec((FiguraVector<?, ?>) val.checkuserdata(), obj);
        } else if (val.isuserdata(FiguraMatrix.class)) {
            writeMat((FiguraMatrix<?, ?>) val.checkuserdata(), obj);
        } else {
            return JsonNull.INSTANCE;
        }

        return obj;
    }

    private static void writeTable(LuaTable table, JsonObject obj) {
        JsonArray tbl = new JsonArray();
        for (LuaValue key : table.keys()) {
            JsonElement val = writeArg(table.get(key), new JsonObject());
            if (val == JsonNull.INSTANCE)
                continue;

            JsonObject children = new JsonObject();
            children.add("key", writeArg(key, new JsonObject()));
            children.add("value", val);
            tbl.add(children);
        }

        obj.addProperty("type", Type.TABLE.name());
        obj.add("data", tbl);
    }

    private static void writeVec(FiguraVector<?, ?> vector, JsonObject obj) {
        JsonArray vec = new JsonArray();
        for (int i = 0; i < vector.size(); i++)
            vec.add(vector.index(i));

        obj.addProperty("type", Type.VECTOR.name());
        obj.add("data", vec);
    }

    private static void writeMat(FiguraMatrix<?, ?> matrix, JsonObject obj) {
        JsonArray mat = new JsonArray();
        for (int i = 0; i < matrix.cols(); i++) {
            JsonObject vec = new JsonObject();
            writeVec(matrix.getColumn(i + 1), vec);
            mat.add(vec);
        }

        obj.addProperty("type", Type.MATRIX.name());
        obj.add("data", mat);
    }

    //read
    private void init() {
        //read file
        Path path = getPath();
        JsonObject root;

        if (!path.toFile().exists())
            return;

        try (FileReader reader = new FileReader(path.toFile())) {
            JsonElement element = JsonParser.parseReader(reader);
            if (element.isJsonNull())
                return;

            root = element.getAsJsonObject();
            for (String key : root.keySet())
                luaTable.set(key, readArg(root.get(key), owner));
        } catch (Exception e) {
            FiguraMod.LOGGER.error("", e);
            throw new LuaError("Failed to load avatar data file");
        }
    }

    private static LuaValue readArg(JsonElement json, Avatar owner) {
        JsonObject obj = json.getAsJsonObject();
        Type type = Type.valueOf(obj.get("type").getAsString());
        JsonElement data = obj.get("data");
        return switch (type) {
            case BOOL -> LuaBoolean.valueOf(data.getAsBoolean());
            case INT -> LuaInteger.valueOf(data.getAsInt());
            case DOUBLE -> LuaDouble.valueOf(data.getAsDouble());
            case STRING -> LuaString.valueOf(data.getAsString());
            case TABLE -> readTable(data.getAsJsonArray(), owner);
            case VECTOR -> owner.luaRuntime.typeManager.javaToLua(readVec(data.getAsJsonArray())).arg1();
            case MATRIX -> owner.luaRuntime.typeManager.javaToLua(readMat(data.getAsJsonArray())).arg1();
        };
    }

    private static LuaValue readTable(JsonArray arr, Avatar owner) {
        LuaTable table = new LuaTable();
        for (int i = 0; i < arr.size(); i++) {
            JsonObject entry = arr.get(i).getAsJsonObject();
            LuaValue key = readArg(entry.get("key"), owner);
            LuaValue val = readArg(entry.get("value"), owner);
            table.set(key, val);
        }
        return table;
    }

    private static FiguraVector<?, ?> readVec(JsonArray arr) {
        double[] array = new double[arr.size()];
        for (int i = 0; i < arr.size(); i++)
            array[i] = arr.get(i).getAsDouble();

        return MathUtils.sizedVector(array);
    }

    private static FiguraMatrix<?, ?> readMat(JsonArray arr) {
        FiguraVector<? ,?>[] vectors = new FiguraVector[arr.size()];
        for (int i = 0; i < arr.size(); i++) {
            JsonObject vec = arr.get(i).getAsJsonObject();
            vectors[i] = readVec(vec.get("data").getAsJsonArray());
        }

        return MathUtils.sizedMat(vectors);
    }


    // -- lua -- //


    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = String.class,
                    argumentNames = "name"
            ),
            value = "config.name"
    )
    public void name(@LuaNotNil String name) {
        if (!isHost) return;
        this.name = name;
        this.loaded = false;
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = @LuaMethodOverload(
                    argumentTypes = {String.class, LuaValue.class},
                    argumentNames = {"key", "value"}
            ),
            value = "config.save"
    )
    public void save(@LuaNotNil String key, LuaValue val) {
        if (!isHost)
            return;

        if (!loaded) {
            init();
            loaded = true;
        }

        val = val != null && (val.isboolean() || val.isstring() || val.isnumber() || val.istable() || val.isuserdata(FiguraVector.class) || val.isuserdata(FiguraMatrix.class)) ? val : LuaValue.NIL;
        luaTable.set(key, val);
        write();
    }

    @LuaWhitelist
    @LuaMethodDoc(
            overloads = {
                    @LuaMethodOverload(
                            returnType = LuaTable.class
                    ),
                    @LuaMethodOverload(
                            argumentTypes = String.class,
                            argumentNames = "key",
                            returnType = Object.class
                    )
            },
            value = "config.load"
    )
    public Object load(String key) {
        if (!isHost)
            return null;

        if (!loaded) {
            init();
            loaded = true;
        }

        return key != null ? luaTable.get(key) : new ReadOnlyLuaTable(luaTable);
    }

    @Override
    public String toString() {
        return "ConfigAPI";
    }
}
