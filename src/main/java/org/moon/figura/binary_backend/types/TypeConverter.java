package org.moon.figura.binary_backend.types;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class TypeConverter {

    //https://wiki.vg/Data_types#VarInt_and_VarLong
    private static final int SEGMENT_BITS = 0b01111111;
    private static final int CONTINUE_BIT = 0b10000000;

    public static void writeVarInt(ByteArrayOutputStream baos, int val) {
        while (true) {
            if ((val & ~SEGMENT_BITS) == 0) {
                baos.write(val);
                return;
            }
            baos.write((val & SEGMENT_BITS) | CONTINUE_BIT);
            val >>>= 7;
        }
    }

    public static int readVarInt(ByteBuffer buffer) throws IOException {
        int result = 0;
        int position = 0;
        byte nextByte;

        do {
            if (position >= 32)
                throw new IOException("VarInt was too big!");
            nextByte = buffer.get();
            //Use the last 7 bytes of the number.
            result |= (nextByte & SEGMENT_BITS) << position;
            position += 7;
        } while ((nextByte & CONTINUE_BIT) != 0);

        return result;
    }




}
