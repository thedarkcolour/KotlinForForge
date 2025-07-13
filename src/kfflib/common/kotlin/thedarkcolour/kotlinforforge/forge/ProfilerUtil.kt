package thedarkcolour.kotlinforforge.forge

import net.minecraft.util.profiling.ProfilerFiller

public fun ProfilerFiller.use(name: String, toProfile: ProfilerFiller.() -> Unit) {
    push(name)
    this.toProfile()
    pop()
}

public fun ProfilerFiller.use(supplier: () -> String, toProfile: ProfilerFiller.() -> Unit) {
    push(supplier)
    this.toProfile()
    pop()
}