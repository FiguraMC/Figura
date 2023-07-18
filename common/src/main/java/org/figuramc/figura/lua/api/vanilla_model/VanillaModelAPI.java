package org.figuramc.figura.lua.api.vanilla_model;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import org.figuramc.figura.lua.LuaWhitelist;
import org.figuramc.figura.lua.docs.LuaFieldDoc;
import org.figuramc.figura.lua.docs.LuaTypeDoc;
import org.figuramc.figura.avatar.Avatar;
import org.figuramc.figura.entries.FiguraVanillaPart;
import org.figuramc.figura.model.ParentType;
import org.figuramc.figura.model.VanillaModelProvider;

import java.util.*;
import java.util.function.Function;

@LuaWhitelist
@LuaTypeDoc(
        name = "VanillaModelAPI",
        value = "vanilla_model"
)
public class VanillaModelAPI {

    // -- body -- //

    @LuaWhitelist
    @LuaFieldDoc("vanilla_model.head")
    public final VanillaModelPart HEAD;
    @LuaWhitelist
    @LuaFieldDoc("vanilla_model.body")
    public final VanillaModelPart BODY;
    @LuaWhitelist
    @LuaFieldDoc("vanilla_model.left_arm")
    public final VanillaModelPart LEFT_ARM;
    @LuaWhitelist
    @LuaFieldDoc("vanilla_model.right_arm")
    public final VanillaModelPart RIGHT_ARM;
    @LuaWhitelist
    @LuaFieldDoc("vanilla_model.left_leg")
    public final VanillaModelPart LEFT_LEG;
    @LuaWhitelist
    @LuaFieldDoc("vanilla_model.right_leg")
    public final VanillaModelPart RIGHT_LEG;

    @LuaWhitelist
    @LuaFieldDoc("vanilla_model.hat")
    public final VanillaModelPart HAT;
    @LuaWhitelist
    @LuaFieldDoc("vanilla_model.jacket")
    public final VanillaModelPart JACKET;
    @LuaWhitelist
    @LuaFieldDoc("vanilla_model.left_sleeve")
    public final VanillaModelPart LEFT_SLEEVE;
    @LuaWhitelist
    @LuaFieldDoc("vanilla_model.right_sleeve")
    public final VanillaModelPart RIGHT_SLEEVE;
    @LuaWhitelist
    @LuaFieldDoc("vanilla_model.left_pants")
    public final VanillaModelPart LEFT_PANTS;
    @LuaWhitelist
    @LuaFieldDoc("vanilla_model.right_pants")
    public final VanillaModelPart RIGHT_PANTS;

    // -- cape -- //

    @LuaWhitelist
    @LuaFieldDoc("vanilla_model.cape_model")
    public final VanillaModelPart CAPE_MODEL;
    @LuaWhitelist
    @LuaFieldDoc("vanilla_model.fake_cape")
    public final VanillaModelPart FAKE_CAPE;

    // -- armor -- //

    @LuaWhitelist
    @LuaFieldDoc("vanilla_model.helmet_item")
    public final VanillaModelPart HELMET_ITEM;

    @LuaWhitelist
    @LuaFieldDoc("vanilla_model.helmet_head")
    public final VanillaModelPart HELMET_HEAD;
    @LuaWhitelist
    @LuaFieldDoc("vanilla_model.helmet_hat")
    public final VanillaModelPart HELMET_HAT;

    @LuaWhitelist
    @LuaFieldDoc("vanilla_model.chestplate_body")
    public final VanillaModelPart CHESTPLATE_BODY;
    @LuaWhitelist
    @LuaFieldDoc("vanilla_model.chestplate_left_arm")
    public final VanillaModelPart CHESTPLATE_LEFT_ARM;
    @LuaWhitelist
    @LuaFieldDoc("vanilla_model.chestplate_right_arm")
    public final VanillaModelPart CHESTPLATE_RIGHT_ARM;

    @LuaWhitelist
    @LuaFieldDoc("vanilla_model.leggings_body")
    public final VanillaModelPart LEGGINGS_BODY;
    @LuaWhitelist
    @LuaFieldDoc("vanilla_model.leggings_left_leg")
    public final VanillaModelPart LEGGINGS_LEFT_LEG;
    @LuaWhitelist
    @LuaFieldDoc("vanilla_model.leggings_right_leg")
    public final VanillaModelPart LEGGINGS_RIGHT_LEG;

    @LuaWhitelist
    @LuaFieldDoc("vanilla_model.boots_left_leg")
    public final VanillaModelPart BOOTS_LEFT_LEG;
    @LuaWhitelist
    @LuaFieldDoc("vanilla_model.boots_right_leg")
    public final VanillaModelPart BOOTS_RIGHT_LEG;

    // -- elytra -- //

    @LuaWhitelist
    @LuaFieldDoc("vanilla_model.left_elytra")
    public final VanillaModelPart LEFT_ELYTRA;
    @LuaWhitelist
    @LuaFieldDoc("vanilla_model.right_elytra")
    public final VanillaModelPart RIGHT_ELYTRA;

    // -- held items -- //

    @LuaWhitelist
    @LuaFieldDoc("vanilla_model.left_item")
    public final VanillaModelPart LEFT_ITEM;
    @LuaWhitelist
    @LuaFieldDoc("vanilla_model.right_item")
    public final VanillaModelPart RIGHT_ITEM;

    // -- parrots -- //

    @LuaWhitelist
    @LuaFieldDoc("vanilla_model.left_parrot")
    public final VanillaModelPart LEFT_PARROT;
    @LuaWhitelist
    @LuaFieldDoc("vanilla_model.right_parrot")
    public final VanillaModelPart RIGHT_PARROT;


    // -- groups -- //


    @LuaWhitelist
    @LuaFieldDoc("vanilla_model.all")
    public final VanillaGroupPart ALL;

    @LuaWhitelist
    @LuaFieldDoc("vanilla_model.player")
    public final VanillaGroupPart PLAYER;
    @LuaWhitelist
    @LuaFieldDoc("vanilla_model.outer_layer")
    public final VanillaGroupPart OUTER_LAYER;
    @LuaWhitelist
    @LuaFieldDoc("vanilla_model.inner_layer")
    public final VanillaGroupPart INNER_LAYER;

    @LuaWhitelist
    @LuaFieldDoc("vanilla_model.cape")
    public final VanillaGroupPart CAPE;

    @LuaWhitelist
    @LuaFieldDoc("vanilla_model.armor")
    public final VanillaGroupPart ARMOR;
    @LuaWhitelist
    @LuaFieldDoc("vanilla_model.helmet")
    public final VanillaGroupPart HELMET;
    @LuaWhitelist
    @LuaFieldDoc("vanilla_model.chestplate")
    public final VanillaGroupPart CHESTPLATE;
    @LuaWhitelist
    @LuaFieldDoc("vanilla_model.leggings")
    public final VanillaGroupPart LEGGINGS;
    @LuaWhitelist
    @LuaFieldDoc("vanilla_model.boots")
    public final VanillaGroupPart BOOTS;

    @LuaWhitelist
    @LuaFieldDoc("vanilla_model.elytra")
    public final VanillaGroupPart ELYTRA;

    @LuaWhitelist
    @LuaFieldDoc("vanilla_model.held_items")
    public final VanillaGroupPart HELD_ITEMS;

    @LuaWhitelist
    @LuaFieldDoc("vanilla_model.parrots")
    public final VanillaGroupPart PARROTS;

    public VanillaModelAPI(Avatar owner) {
        // -- body -- //

        HEAD = new VanillaModelPart(owner, "HEAD", ParentType.Head, VanillaModelProvider.HEAD.func);
        BODY = new VanillaModelPart(owner, "BODY", ParentType.Body, VanillaModelProvider.BODY.func);
        LEFT_ARM = new VanillaModelPart(owner, "LEFT_ARM", ParentType.LeftArm, VanillaModelProvider.LEFT_ARM.func);
        RIGHT_ARM = new VanillaModelPart(owner, "RIGHT_ARM", ParentType.RightArm, VanillaModelProvider.RIGHT_ARM.func);
        LEFT_LEG = new VanillaModelPart(owner, "LEFT_LEG", ParentType.LeftLeg, VanillaModelProvider.LEFT_LEG.func);
        RIGHT_LEG = new VanillaModelPart(owner, "RIGHT_LEG", ParentType.RightLeg, VanillaModelProvider.RIGHT_LEG.func);

        HAT = new VanillaModelPart(owner, "HAT", ParentType.Head, VanillaModelProvider.HAT.func);
        JACKET = new VanillaModelPart(owner, "JACKET", ParentType.Body, VanillaModelProvider.JACKET.func);
        LEFT_SLEEVE = new VanillaModelPart(owner, "LEFT_SLEEVE", ParentType.LeftArm, VanillaModelProvider.LEFT_SLEEVE.func);
        RIGHT_SLEEVE = new VanillaModelPart(owner, "RIGHT_SLEEVE", ParentType.RightArm, VanillaModelProvider.RIGHT_SLEEVE.func);
        LEFT_PANTS = new VanillaModelPart(owner, "LEFT_PANTS", ParentType.LeftLeg, VanillaModelProvider.LEFT_PANTS.func);
        RIGHT_PANTS = new VanillaModelPart(owner, "RIGHT_PANTS", ParentType.RightLeg, VanillaModelProvider.RIGHT_PANTS.func);

        // -- cape -- //

        CAPE_MODEL = new VanillaModelPart(owner, "CAPE_MODEL", ParentType.Cape, VanillaModelProvider.CAPE.func);
        FAKE_CAPE = new VanillaModelPart(owner, "FAKE_CAPE", ParentType.Cape, VanillaModelProvider.FAKE_CAPE.func);

        // -- armor -- //

        HELMET_ITEM = new VanillaModelPart(owner, "HELMET_ITEM", ParentType.Head, null);

        HELMET_HEAD = new VanillaModelPart(owner, "HELMET_HEAD", ParentType.Head, VanillaModelProvider.HEAD.func);
        HELMET_HAT = new VanillaModelPart(owner, "HELMET_HAT", ParentType.Head, VanillaModelProvider.HAT.func);

        CHESTPLATE_BODY = new VanillaModelPart(owner, "CHESTPLATE_BODY", ParentType.Body, VanillaModelProvider.BODY.func);
        CHESTPLATE_LEFT_ARM = new VanillaModelPart(owner, "CHESTPLATE_LEFT_ARM", ParentType.LeftArm, VanillaModelProvider.LEFT_ARM.func);
        CHESTPLATE_RIGHT_ARM = new VanillaModelPart(owner, "CHESTPLATE_RIGHT_ARM", ParentType.RightArm, VanillaModelProvider.RIGHT_ARM.func);

        LEGGINGS_BODY = new VanillaModelPart(owner, "LEGGINGS_BODY", ParentType.Body, VanillaModelProvider.BODY.func);
        LEGGINGS_LEFT_LEG = new VanillaModelPart(owner, "LEGGINGS_LEFT_LEG", ParentType.LeftLeg, VanillaModelProvider.LEFT_LEG.func);
        LEGGINGS_RIGHT_LEG = new VanillaModelPart(owner, "LEGGINGS_RIGHT_LEG", ParentType.RightLeg, VanillaModelProvider.RIGHT_LEG.func);

        BOOTS_LEFT_LEG = new VanillaModelPart(owner, "BOOTS_LEFT_LEG", ParentType.LeftLeg, VanillaModelProvider.LEFT_LEG.func);
        BOOTS_RIGHT_LEG = new VanillaModelPart(owner, "BOOTS_RIGHT_LEG", ParentType.RightLeg, VanillaModelProvider.RIGHT_LEG.func);

        // -- elytra -- //

        LEFT_ELYTRA = new VanillaModelPart(owner, "LEFT_ELYTRA", ParentType.LeftElytra, VanillaModelProvider.LEFT_ELYTRON.func);
        RIGHT_ELYTRA = new VanillaModelPart(owner, "RIGHT_ELYTRA", ParentType.RightElytra, VanillaModelProvider.RIGHT_ELYTRON.func);

        // -- held items -- //

        LEFT_ITEM = new VanillaModelPart(owner, "LEFT_ITEM", ParentType.LeftArm, null);
        RIGHT_ITEM = new VanillaModelPart(owner, "RIGHT_ITEM", ParentType.RightArm, null);

        // -- parrots -- //

        LEFT_PARROT = new VanillaModelPart(owner, "LEFT_PARROT", ParentType.Body, null);
        RIGHT_PARROT = new VanillaModelPart(owner, "RIGHT_PARROT", ParentType.Body, null);


        // -- groups -- //


        INNER_LAYER = new VanillaGroupPart(owner, "INNER_LAYER", HEAD, BODY, LEFT_ARM, RIGHT_ARM, LEFT_LEG, RIGHT_LEG);
        OUTER_LAYER = new VanillaGroupPart(owner, "OUTER_LAYER", HAT, JACKET, LEFT_SLEEVE, RIGHT_SLEEVE, LEFT_PANTS, RIGHT_PANTS);
        PLAYER = new VanillaGroupPart(owner, "PLAYER", INNER_LAYER, OUTER_LAYER);

        CAPE = new VanillaGroupPart(owner, "CAPE", CAPE_MODEL, FAKE_CAPE);

        HELMET = new VanillaGroupPart(owner, "HELMET", HELMET_ITEM, HELMET_HEAD, HELMET_HAT);
        CHESTPLATE = new VanillaGroupPart(owner, "CHESTPLATE", CHESTPLATE_BODY, CHESTPLATE_LEFT_ARM, CHESTPLATE_RIGHT_ARM);
        LEGGINGS = new VanillaGroupPart(owner, "LEGGINGS", LEGGINGS_BODY, LEGGINGS_LEFT_LEG, LEGGINGS_RIGHT_LEG);
        BOOTS = new VanillaGroupPart(owner, "BOOTS", BOOTS_LEFT_LEG, BOOTS_RIGHT_LEG);
        ARMOR = new VanillaGroupPart(owner, "ARMOR", HELMET, CHESTPLATE, LEGGINGS, BOOTS);

        ELYTRA = new VanillaGroupPart(owner, "ELYTRA", LEFT_ELYTRA, RIGHT_ELYTRA);

        HELD_ITEMS = new VanillaGroupPart(owner, "HELD_ITEMS", LEFT_ITEM, RIGHT_ITEM);

        PARROTS = new VanillaGroupPart(owner, "PARROTS", LEFT_PARROT, RIGHT_PARROT);

        List<VanillaGroupPart> groups = new ArrayList<>() {{
            add(PLAYER);
            add(CAPE);
            add(ARMOR);
            add(ELYTRA);
            add(HELD_ITEMS);
            add(PARROTS);
        }};


        // -- modded parts -- //


        for (FiguraVanillaPart entrypoint : ENTRYPOINTS) {
            //prepare group
            List<VanillaModelPart> parts = new ArrayList<>();
            String ID = entrypoint.getID().toUpperCase();

            //get parts
            for (Pair<String, Function<EntityModel<?>, ModelPart>> part : entrypoint.getParts()) {
                String name = ID + "_" + part.getFirst().toUpperCase();
                VanillaModelPart model = new VanillaModelPart(owner, name, ParentType.None, part.getSecond());
                moddedParts.put(name, model);
                parts.add(model);
            }

            //add to group list
            VanillaGroupPart group = new VanillaGroupPart(owner, ID, parts.toArray(new VanillaModelPart[0]));
            moddedParts.put(ID, group);
            groups.add(group);
        }


        // -- all -- //


        ALL = new VanillaGroupPart(owner, "ALL", groups.toArray(new VanillaGroupPart[0]));
    }

    private static final List<FiguraVanillaPart> ENTRYPOINTS = new ArrayList<>();
    private final Map<String, VanillaPart> moddedParts = new HashMap<>();

    public static void initEntryPoints(Set<FiguraVanillaPart> set) {
        ENTRYPOINTS.addAll(set);
    }

    @LuaWhitelist
    public Object __index(String key) {
        if (key == null) return null;
        String name = key.toUpperCase();
        VanillaPart part = switch (name) {
            case "HEAD" -> HEAD;
            case "BODY" -> BODY;
            case "LEFT_ARM" -> LEFT_ARM;
            case "RIGHT_ARM" -> RIGHT_ARM;
            case "LEFT_LEG" -> LEFT_LEG;
            case "RIGHT_LEG" -> RIGHT_LEG;
            case "HAT" -> HAT;
            case "JACKET" -> JACKET;
            case "LEFT_SLEEVE" -> LEFT_SLEEVE;
            case "RIGHT_SLEEVE" -> RIGHT_SLEEVE;
            case "LEFT_PANTS" -> LEFT_PANTS;
            case "RIGHT_PANTS" -> RIGHT_PANTS;
            case "CAPE_MODEL" -> CAPE_MODEL;
            case "FAKE_CAPE" -> FAKE_CAPE;
            case "HELMET_ITEM" -> HELMET_ITEM;
            case "HELMET_HEAD" -> HELMET_HEAD;
            case "HELMET_HAT" -> HELMET_HAT;
            case "CHESTPLATE_BODY" -> CHESTPLATE_BODY;
            case "CHESTPLATE_LEFT_ARM" -> CHESTPLATE_LEFT_ARM;
            case "CHESTPLATE_RIGHT_ARM" -> CHESTPLATE_RIGHT_ARM;
            case "LEGGINGS_BODY" -> LEGGINGS_BODY;
            case "LEGGINGS_LEFT_LEG" -> LEGGINGS_LEFT_LEG;
            case "LEGGINGS_RIGHT_LEG" -> LEGGINGS_RIGHT_LEG;
            case "BOOTS_LEFT_LEG" -> BOOTS_LEFT_LEG;
            case "BOOTS_RIGHT_LEG" -> BOOTS_RIGHT_LEG;
            case "LEFT_ELYTRA" -> LEFT_ELYTRA;
            case "RIGHT_ELYTRA" -> RIGHT_ELYTRA;
            case "LEFT_ITEM" -> LEFT_ITEM;
            case "RIGHT_ITEM" -> RIGHT_ITEM;
            case "LEFT_PARROT" -> LEFT_PARROT;
            case "RIGHT_PARROT" -> RIGHT_PARROT;
            case "INNER_LAYER" -> INNER_LAYER;
            case "OUTER_LAYER" -> OUTER_LAYER;
            case "PLAYER" -> PLAYER;
            case "CAPE" -> CAPE;
            case "HELMET" -> HELMET;
            case "CHESTPLATE" -> CHESTPLATE;
            case "LEGGINGS" -> LEGGINGS;
            case "BOOTS" -> BOOTS;
            case "ARMOR" -> ARMOR;
            case "ELYTRA" -> ELYTRA;
            case "HELD_ITEMS" -> HELD_ITEMS;
            case "PARROTS" -> PARROTS;
            case "ALL" -> ALL;
            default -> null;
        };
        return part != null ? part : moddedParts.get(name);
    }

    @Override
    public String toString() {
        return "VanillaModelAPI";
    }
}
