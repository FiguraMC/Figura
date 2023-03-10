package org.moon.figura.model.rendering;

public class Vertex {

    public float x, y, z;
    public float u, v;
    public float nx, ny, nz;

    public Vertex(float x, float y, float z, float u, float v, float nx, float ny, float nz) {
        this.x = x; this.y = y; this.z = z;
        this.u = u; this.v = v;
        this.nx = nx; this.ny = ny; this.nz = nz;
    }
}
