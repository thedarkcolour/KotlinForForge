package thedarkcolour.kotlinforforge.test

import net.minecraftforge.eventbus.api.BusBuilder
import net.minecraftforge.eventbus.api.Event
import net.minecraftforge.eventbus.api.EventPriority
import net.minecraftforge.eventbus.api.GenericEvent
import org.junit.Assert.assertTrue
import thedarkcolour.kotlinforforge.eventbus.KotlinEventBus
import java.util.function.Consumer

/**
 * Uncomment the `@Mod` annotation and uncomment the entry in mods.toml to run the test mod
 */
//@Mod("test_mod")
public class TestMod {

    init {
        val l = arrayListOf<Throwable>()

        try {
            testAddListenerBridge()
        } catch (t: Throwable) {
            l.add(t)
        }
        try {
            testFunctionReference()
        } catch (t: Throwable) {
            l.add(t)
        }
        try {
            testGenericListeners()
        } catch (t: Throwable) {
            l.add(t)
        }

        l.forEach { throwable ->
            throwable.printStackTrace()
        }

        if (l.isNotEmpty()) {
            throw RuntimeException("Test mod did not pass")
        }
    }

    // Test event class
    public open class TestEvent(public var value: String) : Event()

    /**
     * Tests the new bridge lambdas introduced in Kotlin 1.4.
     */
    private fun testAddListenerBridge() {
        val bus = KotlinEventBus(BusBuilder.builder())

        // Make sure the type checker works properly in Kotlin 1.4
        bus.addListener<TestEvent> { event ->
            event.value = "Foo"
        }

        // Check that the event posts for added listeners
        val event = TestEvent("Bar")
        bus.post(event)
        assertTrue(event.value == "Foo")
    }

    // Test function for function references
    private fun exampleFunction(event: TestEvent) {
        event.value = "Foo"
    }

    /**
     * Test that function references work properly in [KotlinEventBus.addListener].
     */
    private fun testFunctionReference() {
        val bus = KotlinEventBus(BusBuilder.builder())
        val event = TestEvent("Bar")

        bus.addListener(::exampleFunction)
        bus.post(event)
        assertTrue(event.value == "Foo")
    }

    // generic event
    public class TestGenericEvent<T>(type: Class<T>, internal var value: T) : GenericEvent<T>(type)

    // test the function references here as well
    private fun functionReference(event: TestGenericEvent<String>) {
        event.value = "Far"
    }

    // Consumer class
    public class EventConsumer : Consumer<TestGenericEvent<String>> {
        override fun accept(t: TestGenericEvent<String>) {
            t.value = "Jar"
        }
    }

    /**
     * Test that [KotlinEventBus.addGenericListener] respects the generic type
     * of an event fired on the [KotlinEventBus] instance.
     */
    public fun testGenericListeners() {
        // event bus
        val bus = KotlinEventBus(BusBuilder.builder())

        @Suppress("JoinDeclarationAndAssignment")
        var fooEvent: TestGenericEvent<String>

        // test fooEvent
        fooEvent = TestGenericEvent(String::class.java, "Foo")
        bus.addGenericListener(priority = EventPriority.HIGHEST) { e: TestGenericEvent<String> ->
            e.value = "Bar"
        }
        bus.post(fooEvent)
        assertTrue(fooEvent.value == "Bar")

        // test consumer subclass
        fooEvent = TestGenericEvent(String::class.java, "Foo")
        bus.addGenericListener(priority = EventPriority.HIGH, EventConsumer())
        bus.post(fooEvent)
        assertTrue(fooEvent.value == "Jar")

        // test barEvent for function references
        fooEvent = TestGenericEvent(String::class.java, "Foo")
        bus.addGenericListener(priority = EventPriority.NORMAL, ::functionReference)
        bus.post(fooEvent)
        assertTrue(fooEvent.value == "Far")
    }
}