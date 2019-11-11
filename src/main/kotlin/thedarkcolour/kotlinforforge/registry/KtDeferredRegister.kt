package thedarkcolour.kotlinforforge.registry

import net.minecraft.util.ResourceLocation
import net.minecraftforge.event.RegistryEvent
import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.registries.IForgeRegistry
import net.minecraftforge.registries.IForgeRegistryEntry
import java.util.*
import java.util.function.Supplier
import kotlin.collections.LinkedHashMap

/**
 * @see [net.minecraftforge.registries.DeferredRegister] for example use
 */
class KtDeferredRegister<T : IForgeRegistryEntry<T>>(private val registry: IForgeRegistry<T>, private val modid: String) {
    private val entries: MutableMap<KtRegistryObject<T>, Supplier<out T>> = LinkedHashMap()
    val entriesView: Set<KtRegistryObject<T>> = Collections.unmodifiableSet(entries.keys)

    fun register(name: String, supplier: Supplier<out T>): KtRegistryObject<T> {
        val key = ResourceLocation(modid, name)
        val obj = KtRegistryObject(key, registry)
        require(entries.putIfAbsent(obj, Supplier { supplier.get().setRegistryName(key) }) == null) { "Duplicate registration $name" }
        return obj
    }

    fun register(bus: IEventBus) = bus.addListener(::addEntries)

    private fun addEntries(event: RegistryEvent.Register<T>) {
        for (entry in entries.entries) {
            registry.register(entry.value.get())
            entry.key.refresh(registry)
        }
    }
}