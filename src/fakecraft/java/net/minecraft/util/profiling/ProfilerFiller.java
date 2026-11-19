package net.minecraft.util.profiling;

import java.util.function.Supplier;

public interface ProfilerFiller {
    void push(String name);

    void push(Supplier<String> name);

    void pop();
}
