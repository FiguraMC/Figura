package org.figuramc.figura.parsers.Buwwet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.figuramc.figura.FiguraMod;
import org.luaj.vm2.ast.Str;

import java.util.ArrayList;
import java.util.List;

/// A collection of all the recoverable BlockBenchParts from a FiguraModel
public class BlockBenchPart {
    public String name;
    public String uuid;
    public float[] origin;
    public float[] rotation;
    Boolean visibility;

    /// Fill out all the shared fields that Groups and Elements share, and generate a random uuid.
    public BlockBenchPart() {

    }

    public static BlockBenchPart parseNBTchildren(CompoundTag nbt) {
        // Check if this element has children of their own (is a group).
        if (nbt.get("chld") != null) {
            // We're a group
            // TODO: group constructor

            List<BlockBenchPart> children = new ArrayList<BlockBenchPart>();

            ListTag nbtChildren = (ListTag) nbt.get("chld");
            for (Tag childNbtRaw: nbtChildren) {
                CompoundTag nbtChild = (CompoundTag) childNbtRaw;
                children.add(parseNBTchildren(nbtChild));

                FiguraMod.LOGGER.info(String.valueOf(nbtChild));
            }



        } else {
            // We're an element
            // TODO: element constructor

        };

        return new BlockBenchPart();
    }
    public class Group extends BlockBenchPart {
        public BlockBenchPart[] children;


    }
    public class Element extends BlockBenchPart {
        public void what() {
            System.out.println("Field " + name);
        }
    }
}
