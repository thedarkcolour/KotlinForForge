package thedarkcolour.kotlinforforge.eventbus

import net.jodah.typetools.TypeResolver
import net.minecraftforge.eventbus.ASMEventHandler
import net.minecraftforge.eventbus.EventBus
import net.minecraftforge.eventbus.EventBusErrorMessage
import net.minecraftforge.eventbus.ListenerList
import net.minecraftforge.eventbus.api.*
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.MarkerManager
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Modifier
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer

/** @since 2.0.0
 * Fixes [addListener] and [addGenericListener] for Kotlin function references.
 *
 * @param builder The BusBuilder used to configure this event bus
 * @param synthetic Whether this event bus is just a wrapper for another bus
 * @property baseType A supertype of all
 */
public open class KotlinEventBus(
    builder: BusBuilder,
    synthetic: Boolean = false
) : IKotlinEventBus, IEventExceptionHandler {
    private val trackPhases = builder.trackPhases

    protected open val listeners: ConcurrentHashMap<Any, MutableList<IEventListener>> = ConcurrentHashMap()
    protected open val busID: Int = MAX_ID.getAndIncrement()
    private val exceptionHandler = builder.exceptionHandler ?: this
    @Volatile
    private var shutdown = builder.isStartingShutdown

    private val baseType: Class<*> = builder.markerType

    init {
        // see companion object
        if (!synthetic) {
            RESIZE_LISTENER_LIST(busID + 1)
        }
    }

    private fun registerClass(clazz: Class<*>) {
        for (method in clazz.methods) {
            if (Modifier.isStatic(method.modifiers) && method.isAnnotationPresent(SubscribeEvent::class.java)) {
                registerListener(clazz, method, method)
            }
        }
    }

    private fun getDeclMethod(clz: Class<*>, m: Method): Method? {
        return try {
            clz.getDeclaredMethod(m.name, *m.parameterTypes)
        } catch (nse: NoSuchMethodException) {
            null
        }
    }

    private fun registerObject(target: Any) {
        val classes = HashSet<Class<*>>()
        typesFor(target.javaClass, classes)
        Arrays.stream(target.javaClass.methods).filter { m ->
            !Modifier.isStatic(m.modifiers)
        }.forEach { m ->
            classes.map { c ->
                getDeclMethod(c, m)
            }.firstOrNull { rm ->
                rm?.isAnnotationPresent(SubscribeEvent::class.java) == true
            }?.let { rm ->
                registerListener(target, m, rm)
            }
        }
    }

    private fun typesFor(clz: Class<*>, visited: MutableSet<Class<*>>) {
        if (clz.superclass == null) {
            return
        }

        typesFor(clz.superclass, visited)

        for (it in clz.interfaces) {
            typesFor(it, visited)
        }

        visited.add(clz)
    }

    override fun register(target: Any) {
        if (!listeners.containsKey(target)) {
            if (target.javaClass == Class::class.java) {
                registerClass(target as Class<*>)
            } else {
                registerObject(target)
            }
        }
    }

    private fun registerListener(target: Any, f: Method, real: Method) {
        val params: Array<Class<*>> = f.parameterTypes

        if (params.size != 1) {
            throw IllegalArgumentException("""
                Function $f has @SubscribeEvent annotation.
                It has ${params.size} value parameters,
                but event handler functions require a single argument only.
            """.trimIndent()
            )
        }

        val type = params[0]

        if (!Event::class.java.isAssignableFrom(type)) {
            throw IllegalArgumentException("""
                Function $f has @SubscribeEvent annotation,
                but takes an argument that is not an Event subtype : $type
            """.trimIndent())
        }

        if (baseType != Event::class.java && !baseType.isAssignableFrom(type)) {
            throw IllegalStateException("""
                Function $f has @SubscribeEvent annotation,
                but takes an argument that is not a subtype of the base type $baseType: $type
            """.trimIndent())
        }

        register(type, target, real)
    }

    private fun passCancelled(receiveCancelled: Boolean): (Event) -> Boolean = { event ->
        receiveCancelled || !event.isCancelable || !event.isCanceled
    }

    private fun <T : GenericEvent<out F>, F> passGenericCancelled(genericClassFilter: Class<F>, receiveCancelled: Boolean): (T) -> Boolean = { event ->
        event.genericType == genericClassFilter && (receiveCancelled || !event.isCancelable || !event.isCanceled)
    }

    private fun checkNotGeneric(consumer: Consumer<out Event>) {
        checkNotGeneric(getEventClass(consumer))
    }

    private fun checkNotGeneric(clazz: Class<out Event>) {
        if (GenericEvent::class.java.isAssignableFrom(clazz)) {
            throw IllegalArgumentException("Cannot register a generic event listener with addListener, use addGenericListener")
        }
    }

    override fun <T : Event> addListener(consumer: Consumer<T>) {
        checkNotGeneric(consumer)
        addListener(EventPriority.NORMAL, consumer)
    }

    override fun <T : Event> addListener(priority: EventPriority, consumer: Consumer<T>) {
        checkNotGeneric(consumer)
        addListener(priority, false, consumer)
    }

    override fun <T : Event> addListener(priority: EventPriority, receiveCancelled: Boolean, consumer: Consumer<T>) {
        checkNotGeneric(consumer)
        addListener(priority, passCancelled(receiveCancelled), consumer)
    }

    override fun <T : Event> addListener(priority: EventPriority, receiveCancelled: Boolean, eventType: Class<T>, consumer: Consumer<T>) {
        checkNotGeneric(consumer)
        addListener(priority, passCancelled(receiveCancelled), eventType, consumer)
    }

    /**
     * [addGenericListener] with a reified type parameter
     */
    public inline fun <T : GenericEvent<out F>, reified F> addGenericListener(consumer: Consumer<T>) {
        addGenericListener(F::class.java, consumer)
    }

    /**
     * [addGenericListener] with a reified type parameter
     */
    public inline fun <T : GenericEvent<out F>, reified F> addGenericListener(priority: EventPriority, consumer: Consumer<T>) {
        addGenericListener(F::class.java, priority, false, consumer)
    }

    /**
     * [addGenericListener] with a reified type parameter
     */
    public inline fun <T : GenericEvent<out F>, reified F> addGenericListener(priority: EventPriority, receiveCancelled: Boolean, consumer: Consumer<T>) {
        addGenericListener(F::class.java, priority, receiveCancelled, consumer)
    }

    override fun <T : GenericEvent<out F>, F> addGenericListener(genericClassFilter: Class<F>, consumer: Consumer<T>) {
        addGenericListener(genericClassFilter, EventPriority.NORMAL, consumer)
    }

    override fun <T : GenericEvent<out F>, F> addGenericListener(genericClassFilter: Class<F>, priority: EventPriority, consumer: Consumer<T>) {
        addGenericListener(genericClassFilter, priority, false, consumer)
    }

    override fun <T : GenericEvent<out F>, F> addGenericListener(genericClassFilter: Class<F>, priority: EventPriority, receiveCancelled: Boolean, consumer: Consumer<T>) {
        addListener(priority, passGenericCancelled(genericClassFilter, receiveCancelled), consumer)
    }

    /**
     * Fixes issue that crashes when trying to register Kotlin SAM interface
     * for a [Consumer] using the Java [IEventBus.addListener] method
     */
    private fun <T : Event> getEventClass(consumer: Consumer<T>): Class<T> {
        val clazz = consumer.javaClass
        var resolved = TypeResolver.resolveRawArgument(Consumer::class.java, consumer.javaClass)

        if (clazz.simpleName.contains("\$sam$")) {
            try {
                val functionField = clazz.getDeclaredField("function")
                functionField.isAccessible = true
                val function = functionField[consumer]

                // Function should have two type parameters (parameter type and return type)
                resolved = TypeResolver.resolveRawArguments(kotlin.jvm.functions.Function1::class.java, function.javaClass)[0]
            } catch (e: NoSuchFieldException) {
                // Kotlin SAM interfaces compile to classes with a "function" field
                LOGGER.log(Level.FATAL, "Tried to register invalid Kotlin SAM interface: Missing 'function' field")
                throw e
            }
        }

        if (resolved == TypeResolver.Unknown::class.java) {
            LOGGER.error(EVENT_BUS, "Failed to resolve handler for \"$consumer\"")
            throw IllegalStateException("Failed to resolve KFunction event type: $consumer")
        }

        return resolved as Class<T>
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Event> addListener(priority: EventPriority, filter: (T) -> Boolean, consumer: Consumer<T>) {
        val eventType = getEventClass(consumer)

        if (eventType == Event::class.java) {
            LOGGER.warn(EVENT_BUS, """
                Attempting to add a Lambda listener with computed generic type of Event. 
                Are you sure this is what you meant? NOTE : there are complex lambda forms where 
                the generic type information is erased and cannot be recovered at runtime.
            """.trimIndent())
        }

        addListener(priority, filter, eventType, consumer)
    }

    private fun <T : Event> addListener(priority: EventPriority, filter: (T) -> Boolean, eventType: Class<T>, consumer: Consumer<T>) {
        addToListeners(consumer, eventType, priority) { e ->
            if (filter(e as T)) {
                consumer.accept(e)
            }
        }
    }

    private fun register(type: Class<*>, target: Any, f: Method) {
        try {
            val asm = ASMEventHandler(target, f, IGenericEvent::class.java.isAssignableFrom(type))

            addToListeners(target, type, asm.priority, asm)
        } catch (e: IllegalAccessException) {
            LOGGER.error(EVENT_BUS, "Error registering event handler: $type $f", e)
        } catch (e: InstantiationException) {
            LOGGER.error(EVENT_BUS, "Error registering event handler: $type $f", e)
        } catch (e: NoSuchMethodException) {
            LOGGER.error(EVENT_BUS, "Error registering event handler: $type $f", e)
        } catch (e: InvocationTargetException) {
            LOGGER.error(EVENT_BUS, "Error registering event handler: $type $f", e)
        }
    }

    protected open fun addToListeners(target: Any, eventType: Class<*>, priority: EventPriority, listener: IEventListener) {
        val listenerList = EventListenerHelper.getListenerList(eventType)
        listenerList.register(busID, priority, listener)
        val others = listeners.computeIfAbsent(target) { Collections.synchronizedList(ArrayList()) }
        others.add(listener)
    }

    override fun unregister(any: Any?) {
        val list = listeners.remove(any) ?: return

        for (listener in list) {
            ListenerList.unregisterAll(busID, listener)
        }
    }

    override fun <T : GenericEvent<out F>, F> addGenericListener(genericClassFilter: Class<F>, priority: EventPriority, receiveCancelled: Boolean, eventType: Class<T>, consumer: Consumer<T>) {
        addListener(priority, passGenericCancelled(genericClassFilter, receiveCancelled), eventType, consumer)
    }

    override fun post(event: Event): Boolean {
        return post(IEventListener::invoke, event)
    }

    override fun post(wrapper: (IEventListener, Event) -> Unit, event: Event): Boolean {
        if (shutdown) return false
        if (checkTypesOnDispatch && !baseType.isInstance(event)) {
            throw IllegalArgumentException("Cannot post event of type ${event.javaClass.simpleName} to this event. Must match type: " + baseType.simpleName)
        }

        val listeners = event.listenerList.getListeners(busID)

        var index = 0
        try {
            while (index++ < listeners.size) {
                if (!trackPhases && listeners[index]::class.java == EventPriority::class.java) {
                    continue
                } else {
                    wrapper.invoke(listeners[index], event)
                }

            }
        } catch (throwable: Throwable) {
            exceptionHandler.handleException(this, event, listeners, index, throwable)
            throw throwable
        }

        return event.isCancelable && event.isCanceled
    }

    override fun handleException(bus: IEventBus, event: Event, listeners: Array<out IEventListener>, index: Int, throwable: Throwable) {
        LOGGER.error(EVENT_BUS) {
            EventBusErrorMessage(event, index, listeners, throwable)
        }
    }

    override fun shutdown() {
        LOGGER.fatal(EVENT_BUS, "KotlinEventBus $busID shutting down - future events will not be posted.", Exception("stacktrace"))
        shutdown = true
    }

    override fun start() {
        shutdown = false
    }

    private companion object {
        private val LOGGER = LogManager.getLogger()
        private val checkTypesOnDispatch = java.lang.Boolean.parseBoolean(System.getProperty("eventbus.checkTypesOnDispatch", "false"))
        private val EVENT_BUS = MarkerManager.getMarker("EVENTBUS")
        private val MAX_ID: AtomicInteger
        private val RESIZE_LISTENER_LIST: (Int) -> Unit

        init {
            val maxIDField = EventBus::class.java.getDeclaredField("maxID")
            maxIDField.isAccessible = true
            MAX_ID = maxIDField.get(null) as AtomicInteger
            val resizeMethod = ListenerList::class.java.getDeclaredMethod("resize", Int::class.java)
            resizeMethod.isAccessible = true
            RESIZE_LISTENER_LIST = { max -> resizeMethod.invoke(null, max) }
        }
    }
}