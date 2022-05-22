package org.moon.figura.lua.types;

import org.terasology.jnlua.LuaState;

import java.util.AbstractList;

/**
 * 1-Indexed, lua-owned implementation of a list. Has implementations for basic methods
 * that are used by the Events API. Works based off of a LuaOwnedTable
 * implementation in the back.
 * @param <T>
 */
public class LuaOwnedList<T> extends AbstractList<T> {

    private final LuaOwnedTable<Integer> impl;

    private int size = 0;
    private final Class<T> tClazz;

    public LuaOwnedList(LuaState state, String key, Class<T> clazz) {
        impl = new LuaOwnedTable<>(state, key);
        tClazz = clazz;
    }

    @Override
    public boolean add(T element) {
        impl.putValue(size+1, element);
        size++;
        return true;
    }

    @Override
    public T get(int index) {
        return impl.getValue(index, tClazz);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public T remove(int index) {
        if (index > size || index <= 0)
            throw new ArrayIndexOutOfBoundsException("Index " + index + " out of bounds for LuaOwnedList. Remember 1-indexing!");
        T result = get(index);
        while (index < size) {
            impl.putValue(index, get(index + 1));
            index++;
        }
        impl.putValue(index, null);
        size--;
        return result;
    }

    @Override
    public void clear() {
        for (int i = 1; i <= size; i++)
            impl.putValue(i, null);
        size = 0;
    }

}
