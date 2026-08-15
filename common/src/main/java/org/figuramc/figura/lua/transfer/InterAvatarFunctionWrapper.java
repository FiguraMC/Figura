package org.figuramc.figura.lua.transfer;

import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.lua.FiguraLuaRuntime;
import org.jetbrains.annotations.NotNull;
import org.luaj.vm2.*;
import org.luaj.vm2.lib.VarArgFunction;

/**
 * try to be transparent challenge (very difficult)
 */
public class InterAvatarFunctionWrapper extends VarArgFunction {
    /*
     * Implementation notes: This only extends to functions *directly passed* as arguments.
     * If you put it in a table, it won't work. This is by design to prevent weird
     * two-way transform semantics, which is not great performance and would
     * require a proxy table, which is very annoying to do.
     */

    protected class TransformIn implements LuaTransformer {
        @Override
        public @NotNull LuaValue function(LuaFunction f) {
            if (f instanceof InterAvatarFunctionWrapper inter) {
                // this may unwrap in the case of ex. a double callback
                return inter.transfer(author);
            }
            // switch places of author and invoker
            return new InterAvatarFunctionWrapper(invoker, author, f);
        }
    }
    protected class TransformOut implements LuaTransformer {
        @Override
        public @NotNull LuaValue function(LuaFunction f) {
            if (f instanceof InterAvatarFunctionWrapper inter) {
                return inter.transfer(invoker);
            }
            return new InterAvatarFunctionWrapper(author, invoker, f);
        }
    }

    private final Avatar author;
    private final Avatar invoker;
    private final LuaFunction wrap;

    public InterAvatarFunctionWrapper(Avatar author, Avatar invoker, LuaFunction wrap) {
        this.author = author;
        this.invoker = invoker;
        this.wrap = wrap;
    }

    @Override
    public Varargs invoke(Varargs args) {
        LuaTransformer transformIn = new TransformIn();
        LuaValue[] transformedArgs = new LuaValue[args.narg()];
        for (int i = 1; i <= args.narg(); i++)
            transformedArgs[i - 1] = transformIn.visit(args.arg(i));

        Varargs rets;
        try (FiguraLuaRuntime.AvatarContext ignored = FiguraLuaRuntime.context(author)) {
            rets = wrap.invoke(transformedArgs);
        }

        LuaTransformer transformOut = new TransformOut();
        LuaValue[] transformedRets = new LuaValue[rets.narg()];
        for (int i = 1; i <= rets.narg(); i++)
            transformedRets[i - 1] = transformOut.visit(rets.arg(i));

        return varargsOf(transformedRets);
    }

    @Override
    public LuaValue eq(LuaValue val) {
        if (!(val instanceof InterAvatarFunctionWrapper other)) return FALSE;
        return wrap.eq(other.wrap);
    }

    public LuaFunction transfer(Avatar newInvoker) {
        if (newInvoker == this.author) return wrap;
        return new InterAvatarFunctionWrapper(author, newInvoker, wrap);
    }
}
