package net.minecraftforge.common.util;

import java.util.Optional;

public class LazyOptional<T> {
    public Optional<T> resolve() {
        return Optional.empty();
    }
}
