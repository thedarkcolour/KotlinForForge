package thedarkcolour.kotlinforforge.neoforge.forge

import net.minecraft.util.profiling.ProfilerFiller

public fun ProfilerFiller.use(name: String, toProfile: () -> Unit) {
    push(name)
    toProfile()
    pop()
}

public fun ProfilerFiller.use(supplier: () -> String, toProfile: () -> Unit) {
    push(supplier)
    toProfile()
    pop()
}