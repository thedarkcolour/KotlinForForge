package net.minecraft.world.phys;

// Up-to-date as of Minecraft 1.21.1
public class Vec2 {
    public final float x;
    public final float y;

    public Vec2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vec2 add(Vec2 other) {
        throw new RuntimeException("unimplemented");
    }

    public Vec2 negated() {
        throw new RuntimeException("unimplemented");
    }

    public Vec2 scale(float scale) {
        throw new RuntimeException("unimplemented");
    }
}
