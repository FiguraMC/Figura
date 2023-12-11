package org.figuramc.figura.parsers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.List;

// dummy class for gson reflection
// allows reading the model json way easier
public class BlockbenchModel {
    Meta meta;
    Resolution resolution;
    Texture[] textures;
    Element[] elements;
    Animation[] animations;

    // do not reflection-parse the outliner
    // as it can be either an object or a string
    JsonArray outliner;

    public static class Meta {
        String format_version, model_format;
    }

    public static class Resolution {
        int width, height;
    }

    public static class Texture {
        String name;
        String relative_path;
        String source;
        Float width, height;
        Float uv_width, uv_height;
    }

    // -- elements -- // 

    public static class Element {
        String name;
        String type;
        String uuid;

        float[] from, to;
        float[] rotation;
        float[] origin;
        float inflate;

        Boolean visibility;
        Boolean export;

        // do not reflection-parse faces nor vertices
        // since those are based on type
        JsonObject faces;
        JsonObject vertices;
    }

    // aka outliner object
    public static class GroupElement {
        String name;
        String uuid;
        Boolean visibility;
        Boolean export;
        float[] origin;
        float[] rotation;

        // cannot parse children
        // same reason as outliner
        JsonArray children;
    }

    public static class CubeFace {
        static final List<String> FACES = List.of("north", "south", "west", "east", "up", "down");

        float[] uv;
        float rotation;
        Integer texture;
    }

    public static class MeshFace {
        // cannot parse mesh uv
        // as keys are custom string
        JsonObject uv;
        String[] vertices;
        Integer texture;
    }

    // -- animations -- // 

    public static class Animation {
        String name;

        String loop;
        Boolean override;
        float length;

        String anim_time_update;
        String blend_weight;
        String start_delay;
        String loop_delay;

        // cannot parse animators
        // as it keys are groups uuids or effects
        JsonObject animators;
    }

    public static class KeyFrame {
        String channel;
        String interpolation;
        float time;

        // keyframe data can contain any type of object
        JsonArray data_points;

        // bezier stuff
        float[] bezier_left_value;
        float[] bezier_right_value;
        float[] bezier_left_time;
        float[] bezier_right_time;
    }

    public static class KeyFrameData {
        String x, y, z;
    }
}
