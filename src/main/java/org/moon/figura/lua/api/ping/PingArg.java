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
            MATRIX = 7,
            VINT = 8;

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
        } else if (val instanceof LuaString valStr) {
            dos.writeByte(STRING);
            writeString(valStr, dos);
        } else if (val.isint()) {
            int value = val.checkint();
            if (value >= -(1 << 20) && value < 1 << 20) {
                dos.writeByte(VINT);
                writeVarInt(value, dos);
            } else {
                dos.writeByte(INT);
                dos.writeInt(value);
            }
        } else if (val.isnumber()) {
            dos.writeByte(DOUBLE);
            dos.writeDouble(val.checkdouble());
        } else if (val.istable()) {
            dos.writeByte(TABLE);
            writeTable(val.checktable(), dos);
        } else if (val.isuserdata(FiguraVector.class)) {
            dos.writeByte(VECTOR);
            writeVec((FiguraVector<?, ?>) val.checkuserdata(), dos);
        } else if (val.isuserdata(FiguraMatrix.class)) {
            dos.writeByte(MATRIX);
            writeMat((FiguraMatrix<?, ?>) val.checkuserdata(), dos);
        } else {
            dos.writeByte(NIL);
            //dos.writeNull();
        }
    }

    private static void writeVarInt(int value, DataOutputStream dos) throws IOException {
        boolean neg = value < 0;
        value = neg ? -value : value;
        dos.writeByte(value & 63 | (neg ? 64 : 0) | (value > 63 ? 128 : 0));

        value >>>= 6;
        if (value == 0)
            return;

        while ((value & -128) != 0) {
            dos.writeByte(value & 127 | 128);
            value >>>= 7;
        }

        dos.writeByte(value);
    }

    private static void writeString(LuaString string, DataOutputStream dos) throws IOException {
        int strLen = Math.min(string.length(), Short.MAX_VALUE * 2 + 1);
        dos.writeShort((short) strLen);
        string.write(dos, 0, strLen);
    }

    private static void writeTable(LuaTable table, DataOutputStream dos) throws IOException {
        writeVarInt(table.keyCount(), dos);

        for (LuaValue key : table.keys()) {
            writeArg(key, dos);
            writeArg(table.get(key), dos);
        }
    }

    private static void writeVec(FiguraVector<?, ?> vector, DataOutputStream dos) throws IOException {
        dos.writeByte(vector.size());

        for (int i = 0; i < vector.size(); i++)
            dos.writeDouble(vector.index(i));
    }

    private static void writeMat(FiguraMatrix<?, ?> matrix, DataOutputStream dos) throws IOException {
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
            case STRING -> LuaValue.valueOf(dis.readNBytes(dis.readUnsignedShort()));
            case TABLE -> readTable(dis, owner);
            case VECTOR -> owner.luaRuntime.typeManager.javaToLua(readVec(dis)).arg1();
            case MATRIX -> owner.luaRuntime.typeManager.javaToLua(readMat(dis)).arg1();
            case VINT -> LuaValue.valueOf(readVarInt(dis));
            default -> LuaValue.NIL;
        };
    }

    private static int readVarInt(DataInputStream dis) throws IOException {
        int value = 0;
        int bytes = 1;
        byte b = dis.readByte();
        boolean neg = (b & 64) == 64;
        value |= b & 63;

        while (bytes <= 4 && (b & 128) == 128) {
            b = dis.readByte();
            value |= (b & 127) << bytes++ * 7 - 1;
        }

        return neg ? -value : value;
    }

    private static LuaValue readTable(DataInputStream dis, Avatar owner) throws IOException {
        int size = readVarInt(dis);
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
        for (int i = 0; i < columns; i++)
            vectors[i] = readVec(dis);

        return MathUtils.sizedMat(vectors);
    }
}
