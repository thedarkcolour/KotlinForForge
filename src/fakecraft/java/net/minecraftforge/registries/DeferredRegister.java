package net.minecraftforge.registries;

import java.util.function.Supplier;

public class DeferredRegister<T> {
    public <I extends T> RegistryObject<I> register(String name, Supplier<? extends I> sup) {
        throw new IllegalStateException("unimplemented");
    }
}
