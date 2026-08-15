package org.figuramc.figura.lua.api.data;

import org.figuramc.figura.lua.LuaTypeManager;
import org.figuramc.figura.lua.transfer.DeepCopyInto;
import org.figuramc.figura.lua.transfer.LuaTransformer;
import org.jetbrains.annotations.NotNull;
import org.luaj.vm2.*;

import java.util.IdentityHashMap;
import java.util.Locale;

/**
 * Simple deep copy with optional metatable and some userdata support
 * @see DeepCopyInto
 */
public class DeepCopyTransformer implements LuaTransformer {
    private final LuaTypeManager types;
    private final MetatableRule metatables;
    private final boolean copyExtra;
    private final IdentityHashMap<LuaTable, LuaTable> inProgress = new IdentityHashMap<>();

    public enum MetatableRule {
        LINK(true),
        LINK_SOFT(false),
        COPY(true),
        DISCARD(false);

        public final boolean requiresMetatable;

        MetatableRule(boolean requiresMetatable) {
            this.requiresMetatable = requiresMetatable;
        }

        public static final String hint;

        static {
            StringBuilder s = new StringBuilder();
            int i = 0;
            for (MetatableRule value : MetatableRule.values()) {
                if (i++ > 0) s.append(", ");
                s.append(value.name());
            }
            hint = s.toString();
        }

        public static MetatableRule getFor(String input) {
            try {
                return MetatableRule.valueOf(input.toUpperCase(Locale.ENGLISH));
            } catch (IllegalArgumentException e) {
                throw new LuaError(String.format(
                        "Unknown metatable clone rule '%s', acceptable values are %s",
                        input,
                        MetatableRule.hint
                ));
            }
        }
    }

    public DeepCopyTransformer(LuaTypeManager types, MetatableRule metatables, boolean copyExtra) {
        this.types = types;
        this.metatables = metatables;
        this.copyExtra = copyExtra;
    }

    @Override
    public @NotNull LuaValue userdata(LuaUserdata usr) {
        if (copyExtra && usr.m_instance instanceof DeepCopyInto<?> copyable)
            return types.wrap(copyable.copy());
        return usr;
    }

    @Override
    public @NotNull LuaValue table(LuaTable t) {
        if (inProgress.containsKey(t)) return inProgress.get(t);

        LuaTable copy = new LuaTable();
        copy.presize(t.rawlen());
        inProgress.put(t, copy);
        for (Varargs x = t.next(LuaValue.NIL); !x.isnil(1); x = t.next(x.arg1())) {
            copy.rawset(visit(x.arg(1)), visit(x.arg(2)));
        }

        LuaValue locked = t.metatag(LuaValue.METATABLE);
        if (!locked.isnil() && metatables.requiresMetatable) {
            throw new LuaError("Cannot copy metatable: is locked");
        }
        switch (metatables) {
            case LINK_SOFT:
                if (!locked.isnil()) break;
                // fall through
            case LINK:
                copy.setmetatable(t.getmetatable());
                break;
            case COPY:
                copy.setmetatable(visit(t.getmetatable()));
                break;
            case DISCARD:
                break;
        }
        inProgress.remove(t);
        return copy;
    }
}
