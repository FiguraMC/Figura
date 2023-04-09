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
            BOOL_TRUE = 1,
            BOOL_FALSE = 2,
            DOUBLE = 3,
            STRING = 4,
            TABLE = 5,
            VECTOR = 6,
            MATRIX = 7,
            INT_1B = 8,
            INT_2B = 9,
            INT_3B = 10,
            INT_4B = 11;

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
            writeBool(val.checkboolean(), dos);
        } else if (val instanceof LuaString valStr) {
            dos.writeByte(STRING);
            writeString(valStr, dos);
        } else if (val.isint()) {
            writeInt(val.checkint(), dos);
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

    private static void writeBool(boolean value, DataOutputStream dos) throws IOException {
        dos.writeByte(value ? BOOL_TRUE : BOOL_FALSE);
    }

    private static void writeInt(int value, DataOutputStream dos) throws IOException {
        if (Byte.MIN_VALUE <= value && value <= Byte.MAX_VALUE) {
            dos.writeByte(INT_1B);
            dos.writeByte((byte) value);
        } else if (Short.MIN_VALUE <= value && value <= Short.MAX_VALUE) {
            dos.writeByte(INT_2B);
            dos.writeShort((short) value);
        } else if (-0x800000 <= value && value < 0x800000) {
            dos.writeByte(INT_3B);
            dos.writeShort((short) (value >> 8));
            dos.writeByte((byte) (value & 0xFF));
        } else {
            dos.writeByte(INT_4B);
            dos.writeInt(value);
        }
    }

    private static void writeString(LuaString string, DataOutputStream dos) throws IOException {
        int strLen = Math.min(string.length(), Short.MAX_VALUE * 2 + 1);
        dos.writeShort((short) strLen);
        string.write(dos, 0, strLen);
    }

    private static void writeTable(LuaTable table, DataOutputStream dos) throws IOException {
        writeInt(table.keyCount(), dos);

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
            case BOOL_TRUE -> LuaValue.valueOf(true);
            case BOOL_FALSE -> LuaValue.valueOf(false);
            case INT_1B, INT_2B, INT_3B, INT_4B -> LuaValue.valueOf(readInt(dis, type));
            case DOUBLE -> LuaValue.valueOf(dis.readDouble());
            case STRING -> LuaValue.valueOf(dis.readNBytes(dis.readUnsignedShort()));
            case TABLE -> readTable(dis, owner);
            case VECTOR -> owner.luaRuntime.typeManager.javaToLua(readVec(dis)).arg1();
            case MATRIX -> owner.luaRuntime.typeManager.javaToLua(readMat(dis)).arg1();
            default -> LuaValue.NIL;
        };
    }

    private static int readInt(DataInputStream dis, byte type) throws IOException {
        return switch (type) {
            case INT_1B -> dis.readByte();
            case INT_2B -> dis.readShort();
            case INT_3B -> (int) dis.readShort() << 8 | dis.readByte() & 0xFF;
            case INT_4B -> dis.readInt();
            default -> 0;
        };
    }

    private static LuaValue readTable(DataInputStream dis, Avatar owner) throws IOException {
        int size = readInt(dis, dis.readByte());
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

        FiguraVector<?, ?>[] vectors = new FiguraVector[columns];
        for (int i = 0; i < columns; i++)
            vectors[i] = readVec(dis);

        return MathUtils.sizedMat(vectors);
    }
}
