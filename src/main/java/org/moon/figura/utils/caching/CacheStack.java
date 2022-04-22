package org.moon.figura.utils.caching;

import java.util.ArrayList;
import java.util.List;

/**
 * A stack which caches values as it grows, modifying them instead of allocating new ones.
 */
public abstract class CacheStack<T extends CachedType, S> {

    private int index = -1;
    private final T defaultVal;
    private final List<T> values = new ArrayList<>();

    private final CacheUtils.Cache<T> cache;

    public CacheStack(CacheUtils.Cache<T> cache) {
        this.cache = cache;
        defaultVal = cache.getFresh();
    }

    /**
     * Should modify the first argument according to the second argument.
     */
    protected abstract void modify(T valueToModify, S modifierArg);

    /**
     * Should copy the information from the first item to the second item.
     */
    protected abstract void copy(T from, T to);

    /**
     * Fully clears the stack, removing any cached elements as well.
     */
    public void fullClear() {
        for(T val : values)
            val.free();
        values.clear();
        index = -1;
    }

    /**
     * Pushes a copy of the previous item on the top of the stack.
     * Then, modifies the item we just pushed according to modifierArg.
     * @param modifierArg An argument we use to modify the new top value
     *                    of the stack.
     */
    public void push(S modifierArg) {
        if (++index == values.size())
            values.add(cache.getFresh());
        if (index > 0)
            copy(values.get(index-1), values.get(index));
        else
            copy(defaultVal, values.get(index));
        modify(values.get(index), modifierArg);
    }

    public T peek() {
        if (index >= 0)
            return values.get(index);
        return defaultVal;
    }

    public T pop() {
        return values.get(index--);
    }

}
