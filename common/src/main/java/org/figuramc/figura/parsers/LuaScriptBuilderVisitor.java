package org.figuramc.figura.parsers;

import org.figuramc.figura.FiguraMod;
import org.figuramc.figura.config.Configs;
import org.luaj.vm2.Lua;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.ast.*;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class LuaScriptBuilderVisitor extends Visitor {
    private static final char[] chars = new char[63];

    static {
        chars[0] = '_';
        for (int i = 0; i < 26; i++)
            chars[1 + i] = (char) ('a' + i);
        for (int i = 0; i < 26; i++)
            chars[27 + i] = (char) ('A' + i);
        for (int i = 0; i < 10; i++)
            chars[53 + i] = (char) ('0' + i);
    }

    private final StringBuilder builder;
    private final Map<Variable, String> vars = new HashMap<>();
    private final Stack<NameScope> scopes = new Stack<>();
    private boolean blockStandalone = false;

    public LuaScriptBuilderVisitor() {
        this(new StringBuilder());
    }

    public LuaScriptBuilderVisitor(StringBuilder builder) {
        this.builder = builder;
    }

    private static String makeName(int count) {
        StringBuilder res = new StringBuilder();
        int pow = 52, i = 0;
        if (count > pow) {
            pow++;
            while (count >= pow) {
                count -= pow;
                pow *= 63;
                i++;
            }
        }
        for (int j = 0; j < i; j++) {
            res.insert(0, chars[count % 63]);
            count /= 63;
        }
        res.insert(0, chars[count]);
        return res.toString();
    }

    private void pushScope(NameScope scope) {
        a: for (Variable variable : scope.namedVariables.values())
            if(variable.isLocal()) {
                for (Variable var : vars.keySet().stream().sorted(Comparator.comparing(var -> var.name)).toList()) {
                    if (var == variable) continue a;
                    if (var.name.equals(variable.name)) {
                        vars.put(variable, vars.get(var));
                        continue a;
                    }
                }
                vars.putIfAbsent(variable, makeName(vars.size()));
            }
        scopes.push(scope);
    }

    private void popScope() {
        NameScope scope = scopes.pop();
        for (Variable variable : scope.namedVariables.values())
            if (variable.definingScope == scope)
                vars.remove(variable);
    }

    @Override
    public void visit(NameScope scope) {
        pushScope(scope);
    }

    @Override
    public void visit(Block block) {
        boolean thisStandalone = blockStandalone;
        blockStandalone = true;
        if(thisStandalone)
            newlineIfName("do");
        try (ScopedBody b = new ScopedBody(block.scope)) {
            if (block.stats != null)
                for (Stat element : block.stats)
                    element.accept(this);
        }
        if(thisStandalone)
            newlineIfName("end");
    }

    @Override
    public void visit(Stat.Assign stat) {
        visitVars(stat.vars);
        builder.append('=');
        visitExps(stat.exps);
    }

    @Override
    public void visit(Stat.Break breakstat) {
        newlineIfName("break");
    }

    @Override
    public void visit(Stat.FuncDef stat) {
        newlineIfName("function");
        if (stat.name.name != null)
            visit(stat.name.name);
        if (stat.name.dots != null) {
            for (String s : stat.name.dots) {
                builder.append(".").append(s);
            }
        }
        if (stat.name.method != null) {
            builder.append(":").append(stat.name.method);
        }
        stat.body.accept(this);
    }

    @Override
    public void visit(Stat.GenericFor stat) {
        try (ScopedBody b = new ScopedBody(stat.scope)) {
            newlineIfName("for");
            visitNames(stat.names);
            spaceIfName("in");
            visitExps(stat.exps);
            spaceIfName("do");
            blockStandalone = false;
            stat.block.accept(this);
            newlineIfName("end");
        }
    }

    @Override
    public void visit(Stat.IfThenElse stat) {
        newlineIfName("if");
        stat.ifexp.accept(this);
        spaceIfName("then");
        blockStandalone = false;
        stat.ifblock.accept(this);
        if (stat.elseifblocks != null) {
            for (int i = 0, n = stat.elseifblocks.size(); i < n; i++) {
                newlineIfName("elseif");
                stat.elseifexps.get(i).accept(this);
                spaceIfName("then");
                blockStandalone = false;
                stat.elseifblocks.get(i).accept(this);
            }
        }
        if (stat.elseblock != null) {
            newlineIfName("else");
            blockStandalone = false;
            stat.elseblock.accept(this);
        }
        newlineIfName("end");
    }

    @Override
    public void visit(Stat.LocalAssign stat) {
        newlineIfName("local");
        visitNames(stat.names);
        if(stat.values != null)
            builder.append('=');
        visitExps(stat.values);
    }

    @Override
    public void visit(Stat.LocalFuncDef stat) {
        newlineIfName("local function");
        super.visit(stat);
    }

    @Override
    public void visit(Stat.NumericFor stat) {
        try (ScopedBody b = new ScopedBody(stat.scope)) {
            newlineIfName("for ");
            visit(stat.name);
            builder.append("=");
            stat.initial.accept(this);
            builder.append(",");
            stat.limit.accept(this);
            if (stat.step != null) {
                builder.append(",");
                stat.step.accept(this);
            }
            spaceIfName("do");
            blockStandalone = false;
            stat.block.accept(this);
            newlineIfName("end");
        }
    }

    @Override
    public void visit(Stat.RepeatUntil stat) {
        newlineIfName("repeat");
        blockStandalone = false;
        stat.block.accept(this);
        newlineIfName("until");
        stat.exp.accept(this);
    }

    @Override
    public void visit(Stat.Return stat) {
        newlineIfName("return");
        super.visit(stat);
    }

    @Override
    public void visit(Stat.WhileDo stat) {
        newlineIfName("while");
        stat.exp.accept(this);
        spaceIfName("do");
        blockStandalone = false;
        stat.block.accept(this);
        newlineIfName("end");
    }

    @Override
    public void visit(FuncBody body) {
        try (ScopedBody b = new ScopedBody(body.scope)) {
            body.parlist.accept(this);
            blockStandalone = false;
            body.block.accept(this);
            newlineIfName("end");
        }
    }

    @Override
    public void visit(FuncArgs args) {
        List<Exp> exps = args.exps;
        if (exps != null && exps.size() == 1 && exps.get(0) instanceof Exp.Constant constant && constant.value instanceof LuaString) {
            constant.accept(this);
        } else {
            builder.append("(");
            super.visit(args);
            builder.append(")");
        }
    }

    @Override
    public void visit(TableField field) {
        if (field.name != null || field.index != null) {
            if (field.name != null) {
                visit(field.name);
            } else {
                builder.append("[");
                field.index.accept(this);
                builder.append("]");
            }
            builder.append('=');
        }
        field.rhs.accept(this);
    }

    @Override
    public void visit(Exp.AnonFuncDef exp) {
        newlineIfName("function");
        super.visit(exp);
    }

    @Override
    public void visit(Exp.BinopExp exp) {
        exp.lhs.accept(this);
        switch (exp.op) {
            case Lua.OP_ADD    -> builder.append("+");
            case Lua.OP_SUB    -> builder.append("-");
            case Lua.OP_GT     -> builder.append(">");
            case Lua.OP_GE     -> builder.append(">=");
            case Lua.OP_LT     -> builder.append("<");
            case Lua.OP_LE     -> builder.append("<=");
            case Lua.OP_EQ     -> builder.append("==");
            case Lua.OP_NEQ    -> builder.append("~=");
            case Lua.OP_MUL    -> builder.append("*");
            case Lua.OP_DIV    -> builder.append("/");
            case Lua.OP_MOD    -> builder.append("%");
            case Lua.OP_POW    -> builder.append("^");
            case Lua.OP_AND    -> spaceIfName("and");
            case Lua.OP_OR     -> spaceIfName("or");
            case Lua.OP_CONCAT -> builder.append("..");
            default -> throw new IllegalStateException("unhandled operator: " + exp.op);
        }
        exp.rhs.accept(this);
    }

    @Override
    public void visit(Exp.Constant exp) {
        LuaValue value = exp.value;
        if (value instanceof LuaString str) {
            String input = new String(str.m_bytes, StandardCharsets.UTF_8);
            int sdq = 0;
            for (char c : input.toCharArray()) {
                if (c == '\'') sdq--;
                if (c == '\"') sdq++;
            }
            char quote = sdq <= 0 ? '"' : '\'';
            input = input.replaceAll("\\r(?=\\n)", "");
            input = input.replaceAll("\\r", "\n");
            input = input.replaceAll("\\\\", "\\\\\\\\");
            input = input.replaceAll("\\n", "\\\\n");
            input = input.replaceAll(String.valueOf(quote), "\\\\" + quote);
            builder.append(quote).append(input).append(quote);
        } else
            spaceIfName(String.valueOf(value));
    }

    @Override
    public void visit(Exp.FieldExp exp) {
        exp.lhs.accept(this);
        builder.append(".");
        visit(exp.name);
    }

    @Override
    public void visit(Exp.IndexExp exp) {
        exp.lhs.accept(this);
        builder.append("[");
        exp.exp.accept(this);
        builder.append("]");
    }

    @Override
    public void visit(Exp.MethodCall exp) {
        exp.lhs.accept(this);
        builder.append(":").append(exp.name);
        exp.args.accept(this);
    }

    @Override
    public void visit(Exp.NameExp exp) {
        visit(exp.name);
    }

    @Override
    public void visit(Exp.ParensExp exp) {
        builder.append("(");
        super.visit(exp);
        builder.append(")");
    }

    @Override
    public void visit(Exp.UnopExp exp) {
        switch (exp.op) {
            case Lua.OP_UNM -> builder.append("-");
            case Lua.OP_NOT -> spaceIfName("not");
            case Lua.OP_LEN -> builder.append("#");
            default -> throw new IllegalStateException("unhandled op " + exp.op);
        }
        super.visit(exp);
    }

    @Override
    public void visit(Exp.VarargsExp exp) {
        builder.append("...");
    }

    @Override
    public void visit(ParList pars) {
        builder.append("(");
        super.visit(pars);
        if (pars.isvararg) {
            if (!(pars.names == null || pars.names.isEmpty()))
                builder.append(',');
            builder.append("...");
        }
        builder.append(")");
    }

    @Override
    public void visit(TableConstructor table) {
        builder.append("{");
        if (table.fields != null) {
            for (Iterator<TableField> iterator = table.fields.iterator(); iterator.hasNext(); ) {
                iterator.next().accept(this);
                if (iterator.hasNext()) builder.append(";");
            }
        }
        builder.append("}");
    }

    @Override
    public void visitVars(List<Exp.VarExp> vars) {
        if (vars != null) {
            for (Iterator<Exp.VarExp> iterator = vars.iterator(); iterator.hasNext(); ) {
                iterator.next().accept(this);
                if (iterator.hasNext()) builder.append(",");
            }
        }
    }

    @Override
    public void visitExps(List<Exp> exps) {
        if (exps != null) {
            for (Iterator<Exp> iterator = exps.iterator(); iterator.hasNext(); ) {
                iterator.next().accept(this);
                if (iterator.hasNext()) builder.append(",");
            }
        }
    }

    @Override
    public void visitNames(List<Name> names) {
        if (names != null) {
            spaceIfName();
            for (Iterator<Name> iterator = names.iterator(); iterator.hasNext(); ) {
                visit(iterator.next());
                if (iterator.hasNext()) builder.append(",");
            }
        }
    }

    @Override
    public void visit(Name name) {
        visit(vars.getOrDefault(name.variable != null && name.variable.isLocal() ? name.variable : null, name.name));
    }

    @Override
    public void visit(String name) {
        spaceIfName(name);
    }

    @Override
    public void visit(Stat.Goto gotostat) {
        newlineIfName("goto ").append(gotostat.name);
    }

    @Override
    public void visit(Stat.Label label) {
        builder.append("::").append(label.name).append("::");
    }

    private StringBuilder newlineIfName(String next) {
        return charIfName(next, FiguraMod.debugModeEnabled() ? '\n' : ' ');
    }

    private void spaceIfName() {
        spaceIfName("");
    }

    private StringBuilder spaceIfName(String next) {
        return charIfName(next, ' ');
    }

    private StringBuilder charIfName(String next, char c) {
        int length = builder.length();
        char ch = length > 0 ? builder.charAt(length - 1) : '\0';
        if (length > 0 && (ch == '_' || Character.isLetterOrDigit(ch)))
            builder.append(c);
        return builder.append(next);
    }

    public String getString() {
//            int next = 0,
//            line = 1;
//            do {
//                builder.insert(next, "--[[" + (line < 10 ? "   " : line < 100 ? "  " : line < 1000 ? " " : "") + line + "]] ");
//                line++;
//            } while ((next = builder.indexOf("\n", next) + 1) != 0);
        return builder.toString();
    }

    public int length() {
        return builder.length();
    }

    private class ScopedBody implements AutoCloseable {
        private final boolean push;

        ScopedBody(NameScope scope) {
            push = scope != null;
            if (push)
                pushScope(scope);
        }

        public void close() {
            if (push)
                popScope();
        }
    }
}
