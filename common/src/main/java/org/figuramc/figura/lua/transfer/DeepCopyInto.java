package org.figuramc.figura.lua.transfer;

/**
 * Make a copy of this type when transferred between deep-copied.
 */
public interface DeepCopyInto<T> {
    T copy();
}
