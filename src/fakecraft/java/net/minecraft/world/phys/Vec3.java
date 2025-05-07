package net.minecraft.world.phys;

import net.minecraft.core.Vec3i;

// Up-to-date as of Minecraft 1.21.1
public class Vec3 {
    public final double x;
    public final double y;
    public final double z;

    public Vec3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static Vec3 atLowerCornerOf(Vec3i vec) {
        throw new RuntimeException("unimplemented");
    }

    public Vec3 add(Vec3 vec) {
        throw new RuntimeException("unimplemented");
    }

    public Vec3 subtract(Vec3 vec) {
        throw new RuntimeException("unimplemented");
    }

    public Vec3 multiply(Vec3 vec) {
        throw new RuntimeException("unimplemented");
    }

    public Vec3 scale(double scalar) {
        throw new RuntimeException("unimplemented");
    }
}
