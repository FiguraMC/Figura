package org.moon.figura.binary_backend.types;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public interface ByteArraySerializable<T extends ByteArraySerializable<T>> {
    /**
     * Return a new instance of the class, created by reading data from the buffer.
     */
    T read(ByteBuffer buffer);

    /**
     * Write the data needed for this object to the output stream.
     * @param baos
     */
    void write(ByteArrayOutputStream baos);
}
