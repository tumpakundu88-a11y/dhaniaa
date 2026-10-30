package com.dhania.waypoint;

public class Waypoint {
    public String name;
    public double x, y, z;
    public String dim;
    public int color;
    public boolean visible = true;

    public Waypoint() {}

    public Waypoint(String name, double x, double y, double z, String dim, int color) {
        this.name = name; this.x = x; this.y = y; this.z = z; this.dim = dim; this.color = color;
    }
}
