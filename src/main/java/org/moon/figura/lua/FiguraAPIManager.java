package org.moon.figura.lua;

import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.moon.figura.animation.Animation;
import org.moon.figura.avatars.model.FiguraModelPart;
import org.moon.figura.avatars.model.rendertasks.BlockTask;
import org.moon.figura.avatars.model.rendertasks.ItemTask;
import org.moon.figura.avatars.model.rendertasks.RenderTask;
import org.moon.figura.avatars.model.rendertasks.TextTask;
import org.moon.figura.gui.actionwheel.*;
import org.moon.figura.lua.api.*;
import org.moon.figura.lua.api.entity.EntityAPI;
import org.moon.figura.lua.api.entity.LivingEntityAPI;
import org.moon.figura.lua.api.entity.PlayerAPI;
import org.moon.figura.lua.api.event.EventsAPI;
import org.moon.figura.lua.api.event.LuaEvent;
import org.moon.figura.lua.api.keybind.FiguraKeybind;
import org.moon.figura.lua.api.keybind.KeybindAPI;
import org.moon.figura.lua.api.math.MatricesAPI;
import org.moon.figura.lua.api.math.VectorsAPI;
import org.moon.figura.lua.api.nameplate.NameplateAPI;
import org.moon.figura.lua.api.nameplate.NameplateCustomization;
import org.moon.figura.lua.api.nameplate.NameplateEntityCust;
import org.moon.figura.lua.api.nameplate.NameplateGroupCust;
import org.moon.figura.lua.api.particle.ParticleAPI;
import org.moon.figura.lua.api.particle.ParticleBuilder;
import org.moon.figura.lua.api.ping.PingAPI;
import org.moon.figura.lua.api.ping.PingFunction;
import org.moon.figura.lua.api.sound.SoundAPI;
import org.moon.figura.lua.api.sound.SoundBuilder;
import org.moon.figura.lua.api.vanilla_model.VanillaGroupPart;
import org.moon.figura.lua.api.vanilla_model.VanillaModelAPI;
import org.moon.figura.lua.api.vanilla_model.VanillaModelPart;
import org.moon.figura.lua.api.world.BiomeAPI;
import org.moon.figura.lua.api.world.BlockStateAPI;
import org.moon.figura.lua.api.world.ItemStackAPI;
import org.moon.figura.lua.api.world.WorldAPI;
import org.moon.figura.math.matrix.FiguraMat2;
import org.moon.figura.math.matrix.FiguraMat3;
import org.moon.figura.math.matrix.FiguraMat4;
import org.moon.figura.math.vector.*;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * A set of Globals of which there is only one in the MC instance.
 * This set of Globals is used to compile and run other scripts.
 */
public class FiguraAPIManager {

    /**
     * Addon mods simply need to add their classes to the WHITELISTED_CLASSES set,
     * and whichever global vars they want to set into the API_GETTERS map.
     */
    public static final Set<Class<?>> WHITELISTED_CLASSES = new HashSet<>() {{
        add(FiguraVec2.class);
        add(FiguraVec3.class);
        add(FiguraVec4.class);
        add(FiguraVec5.class);
        add(FiguraVec6.class);

        add(FiguraMat2.class);
        add(FiguraMat3.class);
        add(FiguraMat4.class);

        add(EntityAPI.class);
        add(LivingEntityAPI.class);
        add(PlayerAPI.class);

        add(EventsAPI.class);
        add(LuaEvent.class);

        add(FiguraModelPart.class);
        add(RenderTask.class);
        add(ItemTask.class);
        add(BlockTask.class);
        add(TextTask.class);

        add(SoundAPI.class);
        add(SoundBuilder.class);

        add(ParticleAPI.class);
        add(ParticleBuilder.class);

        add(VanillaModelAPI.class);
        add(VanillaGroupPart.class);
        add(VanillaModelPart.class);

        add(KeybindAPI.class);
        add(FiguraKeybind.class);

        add(NameplateAPI.class);
        add(NameplateCustomization.class);
        add(NameplateEntityCust.class);
        add(NameplateGroupCust.class);

        add(ActionWheelAPI.class);
        add(Page.class);
        add(Action.class);
        add(ClickAction.class);
        add(ScrollAction.class);
        add(ToggleAction.class);

        add(VectorsAPI.class);
        add(MatricesAPI.class);

        add(WorldAPI.class);
        add(BiomeAPI.class);
        add(BlockStateAPI.class);
        add(ItemStackAPI.class);

        add(PingAPI.class);
        add(PingFunction.class);

        add(Animation.class);

        add(HostAPI.class);

        add(RendererAPI.class);

        add(ClientAPI.class);

        add(MetaAPI.class);
    }};

    public static final Map<String, Function<FiguraLuaRuntime, Object>> API_GETTERS = new LinkedHashMap<>() {{
        put("events", r -> r.events = new EventsAPI());
        put("sound", r -> new SoundAPI(r.owner));
        put("vanilla_model", r -> r.vanilla_model = new VanillaModelAPI());
        put("keybind", r -> r.keybind = new KeybindAPI(r.owner));
        put("host", r -> r.host = new HostAPI(r.owner));
        put("nameplate", r -> r.nameplate = new NameplateAPI());
        put("renderer", r -> r.renderer = new RendererAPI(r.owner.owner));
        put("action_wheel", r -> r.action_wheel = new ActionWheelAPI(r.owner.owner));
        put("animations", r -> Animation.getTableForAnimations(r.owner));
        put("client", r -> ClientAPI.INSTANCE);
        put("particle", r -> new ParticleAPI(r.owner));
        put("meta", r -> r.meta = new MetaAPI(r.owner));
        put("vectors", r -> VectorsAPI.INSTANCE);
        put("matrices", r -> MatricesAPI.INSTANCE);
        put("world", r -> WorldAPI.INSTANCE);
        put("ping", r -> r.ping = new PingAPI(r.owner));
    }};

    static {
        LuaString.s_metatable = new ReadOnlyLuaTable(LuaString.s_metatable);
    }

    public static void setupTypesAndAPIs(FiguraLuaRuntime runtime) {
        for (Class<?> clazz : WHITELISTED_CLASSES)
            runtime.registerClass(clazz);
        for (Map.Entry<String, Function<FiguraLuaRuntime, Object>> entry : API_GETTERS.entrySet())
            runtime.setGlobal(entry.getKey(), entry.getValue().apply(runtime));
    }

    static class ReadOnlyLuaTable extends LuaTable {
        public ReadOnlyLuaTable(LuaValue table) {
            presize(table.length(), 0);
            for (Varargs n = table.next(LuaValue.NIL); !n.arg1().isnil(); n = table
                    .next(n.arg1())) {
                LuaValue key = n.arg1();
                LuaValue value = n.arg(2);
                super.rawset(key, value.istable() ? new ReadOnlyLuaTable(value) : value);
            }
        }
        public LuaValue setmetatable(LuaValue metatable) { return error("table is read-only"); }
        public void set(int key, LuaValue value) { error("table is read-only"); }
        public void rawset(int key, LuaValue value) { error("table is read-only"); }
        public void rawset(LuaValue key, LuaValue value) { error("table is read-only"); }
        public LuaValue remove(int pos) { return error("table is read-only"); }
    }

}
