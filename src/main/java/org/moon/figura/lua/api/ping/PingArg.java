package org.moon.figura.lua.api.ping;

import org.luaj.vm2.*;
import org.moon.figura.FiguraMod;
import org.moon.figura.avatar.Avatar;
import org.moon.figura.math.matrix.FiguraMatrix;
import org.moon.figura.math.vector.FiguraVector;
import org.moon.figura.utils.MathUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PingArg {

    private static final int
            NIL = 0,
            BOOL = 1,
            INT = 2,
            DOUBLE = 3,
            STRING = 4,
            TABLE = 5,
            VECTOR = 6,
            MATRIX = 7;

    private final Varargs args;

    public PingArg(Varargs args) {
        this.args = args;
    }

    // -- writing -- //

    public byte[] toByteArray() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);

            for (int i = 0; i < args.narg(); i++) {
                LuaValue arg = args.arg(i + 1);
                writeArg(arg, dos);
            }

            return baos.toByteArray();
        } catch (Exception e) {
            throw new LuaError("Failed to write ping! " + e.getMessage());
        }
    }

    private static void writeArg(LuaValue val, DataOutputStream dos) throws IOException {
        if (val.isboolean()) {
            dos.writeByte(BOOL);
            dos.writeBoolean(val.checkboolean());
        } else if (val instanceof LuaString) {
            dos.writeByte(STRING);
            dos.writeUTF(val.checkjstring());
        } else if (val.isint()) {
            dos.writeByte(INT);
            dos.writeInt(val.checkinteger().v);
        } else if (val.isnumber()) {
            dos.writeByte(DOUBLE);
            dos.writeDouble(val.checkdouble());
        } else if (val.istable()) {
            writeTable(val.checktable(), dos);
        } else if (val.isuserdata(FiguraVector.class)) {
            writeVec((FiguraVector<?, ?>) val.checkuserdata(), dos);
        } else if (val.isuserdata(FiguraMatrix.class)) {
            writeMat((FiguraMatrix<?, ?>) val.checkuserdata(), dos);
        } else {
            dos.writeByte(NIL);
        }
    }

    private static void writeTable(LuaTable table, DataOutputStream dos) throws IOException {
        dos.writeByte(TABLE);
        dos.writeInt(table.keyCount());

        for (LuaValue key : table.keys()) {
            writeArg(key, dos);
            writeArg(table.get(key), dos);
        }
    }

    private static void writeVec(FiguraVector<?, ?> vector, DataOutputStream dos) throws IOException {
        dos.writeByte(VECTOR);
        dos.writeByte(vector.size());

        for (int i = 0; i < vector.size(); i++)
            dos.writeDouble(vector.index(i));
    }

    private static void writeMat(FiguraMatrix<?, ?> matrix, DataOutputStream dos) throws IOException {
        dos.writeByte(MATRIX);
        dos.writeByte(matrix.cols());

        for (int i = 0; i < matrix.cols(); i++) {
            FiguraVector<?, ?> vec = matrix.getColumn(i + 1);
            writeVec(vec, dos);
        }
    }

    // -- reading -- //

    public static LuaValue[] fromByteArray(byte[] bytes, Avatar owner) {
        try {
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(bytes));

            List<LuaValue> luaValues = new ArrayList<>();
            while (dis.available() > 0)
                luaValues.add(readArg(dis, owner));

            return luaValues.toArray(new LuaValue[0]);
        } catch (Exception e) {
            FiguraMod.LOGGER.warn("Failed to read ping!", e);
            return null;
        }
    }

    private static LuaValue readArg(DataInputStream dis, Avatar owner) throws IOException {
        byte type = dis.readByte();

        return switch (type) {
            case BOOL -> LuaValue.valueOf(dis.readBoolean());
            case INT -> LuaValue.valueOf(dis.readInt());
            case DOUBLE -> LuaValue.valueOf(dis.readDouble());
            case STRING -> LuaValue.valueOf(dis.readUTF());
            case TABLE -> readTable(dis, owner);
            case VECTOR -> owner.luaRuntime.typeManager.javaToLua(readVec(dis)).arg1();
            case MATRIX -> owner.luaRuntime.typeManager.javaToLua(readMat(dis)).arg1();
            default -> LuaValue.NIL;
        };
    }

    private static LuaValue readTable(DataInputStream dis, Avatar owner) throws IOException {
        int size = dis.readInt();
        LuaTable table = new LuaTable();

        for (int i = 0; i < size; i++)
            table.set(readArg(dis, owner), readArg(dis, owner));

        return table;
    }

    private static FiguraVector<?, ?> readVec(DataInputStream dis) throws IOException {
        byte size = dis.readByte();

        double[] array = new double[size];
        for (int i = 0; i < size; i++)
            array[i] = dis.readDouble();

        return MathUtils.sizedVector(array);
    }

    private static FiguraMatrix<?, ?> readMat(DataInputStream dis) throws IOException {
        byte columns = dis.readByte();

        FiguraVector<? ,?>[] vectors = new FiguraVector[columns];
        for (int i = 0; i < columns; i++) {
            dis.readByte(); //vec type - ignored
            vectors[i] = readVec(dis);
        }

        return MathUtils.sizedMat(vectors);
    }
}
