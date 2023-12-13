package org.figuramc.figura.exporters;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import org.figuramc.figura.math.vector.FiguraVec3;
import org.figuramc.figura.math.vector.FiguraVec4;
import org.figuramc.figura.model.ParentType;

import java.util.ArrayList;
import java.util.UUID;

public class BlockBenchModel {

    private static final String VERSION = "4.9";

    private final JsonObject root = new JsonObject();
    private final ArrayList<Cube> elements = new ArrayList<>();
    private final ArrayList<Element> outliner = new ArrayList<>();
    private final JsonArray textures = new JsonArray();

    public BlockBenchModel(String format) {
        // append metadata
        JsonObject meta = new JsonObject();
        meta.addProperty("format_version", VERSION);
        meta.addProperty("model_format", format);
        root.add("meta", meta);
    }

    public void setResolution(int w, int h) {
        JsonObject resolution = new JsonObject();
        resolution.addProperty("width", w);
        resolution.addProperty("height", h);
        root.add("resolution", resolution);
    }

    // returns the image id
    public int addImage(String name, String source, int width, int height) {
        int id = textures.size();

        JsonObject texture = new JsonObject();
        texture.addProperty("name", name);
        texture.addProperty("id", String.valueOf(id));
        texture.addProperty("source", "data:image/png;base64," + source);
        texture.addProperty("width", width);
        texture.addProperty("height", height);
        texture.addProperty("uv_width", width);
        texture.addProperty("uv_height", height);
        textures.add(texture);

        return id;
    }

    public Group addGroup(String name, FiguraVec3 pivot) {
        return addGroup(name, pivot, null);
    }

    public Group addGroup(String name, FiguraVec3 pivot, Group parent) {
        Group g = new Group(name, pivot);
        addElement(g, parent);
        return g;
    }

    public Group addGroup(ParentType type, FiguraVec3 pivot, Group parent) {
        return addGroup(type.name(), pivot, parent);
    }

    public Cube addCube(FiguraVec3 position, FiguraVec3 size, Group parent) {
        return addCube(parent == null ? "cube" : parent.getName(), position, size, parent);
    }

    public Cube addCube(String name, FiguraVec3 position, FiguraVec3 size, Group parent) {
        return addCube(name, parent == null ? position.plus(size.scaled(0.5)) : parent.getPivot(), position, size, parent);
    }

    public Cube addCube(String name, FiguraVec3 pivot, FiguraVec3 position, FiguraVec3 size, Group parent) {
        Cube c = new Cube(name, pivot, position, size);
        addElement(c, parent);
        return c;
    }

    public void addElement(Element element, Group parent) {
        if (element instanceof Cube cube)
            elements.add(cube);

        if (parent == null) {
            outliner.add(element);
        } else {
            parent.addChild(element);
        }
    }

    public JsonObject build() {
        JsonArray elements = new JsonArray();
        for (Cube cube : this.elements)
            elements.add(cube.build());

        JsonArray outliner = new JsonArray();
        for (Element element : this.outliner)
            outliner.add(element.outliner());

        root.add("elements", elements);
        root.add("outliner", outliner);
        root.add("textures", textures);
        return root;
    }

    private static JsonArray vec3ToJson(FiguraVec3 vec) {
        JsonArray json = new JsonArray();
        json.add(vec.x);
        json.add(vec.y);
        json.add(vec.z);
        return json;
    }

    private static JsonArray vec4ToJson(FiguraVec4 vec) {
        JsonArray json = new JsonArray();
        json.add(vec.x);
        json.add(vec.y);
        json.add(vec.z);
        json.add(vec.w);
        return json;
    }

    private abstract static class Element {

        protected final UUID uuid;
        protected final String name;
        protected final FiguraVec3 pivot;

        public Element(String name, FiguraVec3 pivot) {
            this.name = name;
            this.uuid = UUID.randomUUID();
            this.pivot = pivot;
        }

        public JsonObject build() {
            JsonObject element = new JsonObject();
            element.addProperty("name", name);
            element.addProperty("uuid", uuid.toString());
            element.add("origin", vec3ToJson(pivot));
            return element;
        }

        public JsonElement outliner() {
            return new JsonPrimitive(uuid.toString());
        }

        public String getName() {
            return name;
        }

        public FiguraVec3 getPivot() {
            return pivot;
        }
    }

    public static class Cube extends Element {

        private final JsonObject faces = new JsonObject();

        private final FiguraVec3 position, size;
        public double inflate;

        public Cube(String name, FiguraVec3 pivot, FiguraVec3 position, FiguraVec3 size) {
            super(name, pivot);
            this.position = position;
            this.size = size;
        }

        @Override
        public JsonObject build() {
            JsonObject cube = super.build();
            cube.add("from", vec3ToJson(position));
            cube.add("to", vec3ToJson(position.plus(size)));
            cube.addProperty("inflate", inflate);
            cube.add("faces", faces);
            return cube;
        }

        public void addFace(String direction, FiguraVec4 uv, int texture, double scaleW, double scaleH) {
            addFace(direction, uv, texture, false, false, scaleW, scaleH);
        }

        public void addFace(String direction, FiguraVec4 uv, int texture, boolean mirrorW, boolean mirrorH, double scaleW, double scaleH) {
            if (Math.signum(scaleW) < 0)
                mirrorW = !mirrorW;
            if (Math.signum(scaleH) < 0)
                mirrorH = !mirrorH;

            if (mirrorW)
                uv.set(uv.z, uv.y, uv.x, uv.w);
            if (mirrorH)
                uv.set(uv.x, uv.w, uv.z, uv.y);

            scaleW = Math.abs(scaleW);
            scaleH = Math.abs(scaleH);
            uv.multiply(scaleW, scaleH, scaleW, scaleH);

            JsonObject face = new JsonObject();
            face.add("uv", vec4ToJson(uv));
            face.addProperty("texture", texture);
            faces.add(direction, face);
        }

        public void generateBoxFaces(double x, double y, int texture) {
            generateBoxFaces(x, y, texture, 1, 1);
        }

        public void generateBoxFaces(double x, double y, int texture, double scaleW, double scaleH) {
            String[] faces = {"up", "down", "east", "north", "west", "south"};
            if (Math.signum(scaleW) < 0) {
                String t = faces[2];
                faces[2] = faces[4];
                faces[4] = t;
            }
            if (Math.signum(scaleH) < 0) {
                String t = faces[0];
                faces[0] = faces[1];
                faces[1] = t;
            }
            addFace(faces[0], FiguraVec4.of(x + size.z, y, x + size.z + size.x, y + size.z), texture, true, true, scaleW, scaleH);
            addFace(faces[1], FiguraVec4.of(x + size.z + size.x, y, x + size.z + size.x + size.x, y + size.z), texture, true, false, scaleW, scaleH);
            addFace(faces[2], FiguraVec4.of(x, y + size.z, x + size.z, y + size.z + size.y), texture, scaleW, scaleH);
            addFace(faces[3], FiguraVec4.of(x + size.z, y + size.z, x + size.z + size.x, y + size.z + size.y), texture, scaleW, scaleH);
            addFace(faces[4], FiguraVec4.of(x + size.z + size.x, y + size.z, x + size.z + size.x + size.z, y + size.z + size.y), texture, scaleW, scaleH);
            addFace(faces[5], FiguraVec4.of(x + size.z + size.x + size.z, y + size.z, x + size.z + size.x + size.z + size.x, y + size.z + size.y), texture, scaleW, scaleH);
        }
    }

    public static class Group extends Element {

        private final ArrayList<Element> children = new ArrayList<>();

        public Group(String name, FiguraVec3 pivot) {
            super(name, pivot);
        }

        @Override
        public JsonObject build() {
            JsonArray children = new JsonArray();
            for (Element element : this.children)
                children.add(element.outliner());

            JsonObject group = super.build();
            group.addProperty("isOpen", true);
            group.add("children", children);
            return group;
        }

        @Override
        public JsonElement outliner() {
            return build();
        }

        public void addChild(Element element) {
            children.add(element);
        }
    }
}
