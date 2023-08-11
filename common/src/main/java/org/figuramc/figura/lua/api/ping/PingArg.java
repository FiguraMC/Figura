package org.figuramc.figura.lua.api.ping;

import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.math.matrix.FiguraMatrix;
import org.figuramc.figura.math.vector.FiguraVector;
import org.figuramc.figura.utils.MathUtils;
import org.luaj.vm2.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PingArg {

    private static final int
            NIL = 0,
            BOOL_TRUE = 1, BOOL_FALSE = 2,
            DOUBLE = 3,
            STRING = 4,
            TABLE = 5,
            VECTOR_2 = 6, VECTOR_3 = 7, VECTOR_4 = 8,
            MATRIX_2 = 9, MATRIX_3 = 10, MATRIX_4 = 11,
            INT_1B = 12, INT_2B = 13, INT_3B = 14, INT_4B = 15;

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
            writeVec((FiguraVector<?, ?>) val.checkuserdata(), dos);
        } else if (val.isuserdata(FiguraMatrix.class)) {
            writeMat((FiguraMatrix<?, ?>) val.checkuserdata(), dos);
        } else {
            dos.writeByte(NIL);
            // dos.writeNull();
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
        dos.writeByte(switch (vector.size()) {
            case 2 -> VECTOR_2;
            case 3 -> VECTOR_3;
            case 4 -> VECTOR_4;
            default -> throw new UnsupportedOperationException("Cannot write ping for vector size of " + vector.size());
        });

        for (int i = 0; i < vector.size(); i++)
            dos.writeDouble(vector.index(i));
    }

    private static void writeMat(FiguraMatrix<?, ?> matrix, DataOutputStream dos) throws IOException {
        dos.writeByte(switch (matrix.cols()) {
            case 2 -> MATRIX_2;
            case 3 -> MATRIX_3;
            case 4 -> MATRIX_4;
            default -> throw new UnsupportedOperationException("Cannot write ping for matrix column of size " + matrix.cols());
        });

        for (int i = 0; i < matrix.cols(); i++) {
            FiguraVector<?, ?> vec = matrix.getColumn(i + 1);
            for (int o = 0; o < matrix.cols(); o++) {
                dos.writeDouble(vec.index(o));
            }
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
            FiguraMod.LOGGER.warn("Failed to read " + owner.owner + " ping!", e);
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
            case VECTOR_2, VECTOR_3, VECTOR_4 -> owner.luaRuntime.typeManager.javaToLua(readVec(dis, type)).arg1();
            case MATRIX_2, MATRIX_3, MATRIX_4 -> owner.luaRuntime.typeManager.javaToLua(readMat(dis, type)).arg1();
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

    private static FiguraVector<?, ?> readVec(DataInputStream dis, byte type) throws IOException {
        byte size = switch (type) {
            case VECTOR_2 -> 2;
            case VECTOR_3 -> 3;
            case VECTOR_4 -> 4;
            default -> throw new UnsupportedOperationException("Cannot read vector of unknown type " + type);
        };

        double[] array = new double[size];
        for (int i = 0; i < size; i++)
            array[i] = dis.readDouble();

        return MathUtils.sizedVector(array);
    }

    private static FiguraMatrix<?, ?> readMat(DataInputStream dis, byte type) throws IOException {
        byte size = switch (type) {
            case MATRIX_2 -> 2;
            case MATRIX_3 -> 3;
            case MATRIX_4 -> 4;
            default -> throw new UnsupportedOperationException("Cannot read matrix of unknown type " + type);
        };

        FiguraVector<?, ?>[] vectors = new FiguraVector[size];
        for (int i = 0; i < size; i++) {
            double[] array = new double[size];
            for (int o = 0; o < size; o++)
                array[o] = dis.readDouble();
            vectors[i] = MathUtils.sizedVector(array);
        }

        return MathUtils.sizedMat(vectors);
    }
}
