package thedarkcolour.kotlinforforge.forge

import net.minecraft.util.profiling.ProfilerFiller

public fun ProfilerFiller.use(name: String, toProfile: (ProfilerFiller) -> Unit) {
    push(name)
    toProfile(this)
    pop()
}

public fun ProfilerFiller.use(supplier: () -> String, toProfile: (ProfilerFiller) -> Unit) {
    push(supplier)
    toProfile(this)
    pop()
}