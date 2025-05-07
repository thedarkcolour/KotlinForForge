package net.minecraft.core;

// Up-to-date as of Minecraft 1.21.1
public class Vec3i {
    private int x;
    private int y;
    private int z;

    public Vec3i(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3i offset(Vec3i other) {
        throw new RuntimeException("unimplemented");
    }

    public Vec3i subtract(Vec3i other) {
        throw new RuntimeException("unimplemented");
    }

    public Vec3i multiply(int scalar) {
        throw new RuntimeException("unimplemented");
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getZ() {
        return this.z;
    }
}
