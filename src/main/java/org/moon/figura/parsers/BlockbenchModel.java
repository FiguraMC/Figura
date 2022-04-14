package org.moon.figura.parsers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

//dummy class for gson reflection
//allows reading the model json way easier
public class BlockbenchModel {
    String name;
    Resolution resolution;
    Texture[] textures;
    Element[] elements;

    //do not reflection-parse the outliner
    //as it can be either an object or a string
    JsonArray outliner;

    public static class Resolution {
        int width, height;
    }

    public static class Texture {
        String name;
        String source;
    }

    public static class Element {
        String name;
        String type;
        String uuid;

        float[] from, to;
        float[] rotation;
        float[] origin;
        float inflate;

        Boolean visibility;

        //do not reflection-parse faces nor vertices
        //since those are based on type
        JsonObject faces;
        JsonObject vertices;
    }

    //aka outliner object
    public static class GroupElement {
        String name;
        Boolean visibility;
        float[] origin;

        //cannot parse children
        //same reason as outliner
        JsonArray children;
    }

    public static class CubeFace {
        static final List<String> FACES = List.of("north", "south", "west", "east", "up", "down");

        float[] uv;
        float rotation;
        Integer texture;
    }

    public static class MeshFace {
        //cannot parse mesh uv
        //as keys are custom string
        JsonObject uv;
        String[] vertices;
        Integer texture;
    }
}
