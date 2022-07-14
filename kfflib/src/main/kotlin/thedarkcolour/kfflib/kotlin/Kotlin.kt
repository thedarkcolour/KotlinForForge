package thedarkcolour.kfflib.kotlin

import java.util.*
import java.util.function.Supplier

/**
 * Returns a supplier that always returns the same value.
 */
public fun <T> supply(value: T): Supplier<T> {
    return Supplier { value }
}

/**
 * Returns an empty new [EnumMap].
 */
public inline fun <reified K : Enum<K>, V> enumMapOf(): MutableMap<K, V> {
    return EnumMap(K::class.java)
}

/**
 * Returns a new [EnumMap] with the specified contents, given as a list of pairs
 * where the first component is the key and the second is the value.
 */
public inline fun <reified K : Enum<K>, V> enumMapOf(vararg pairs: Pair<K, V>): MutableMap<K, V> {
    return EnumMap<K, V>(K::class.java).apply { putAll(pairs) }
}

/**
 * Returns an empty [EnumSet] with the specified element type.
 */
public inline fun <reified E : Enum<E>> enumSet(): EnumSet<E> {
    return EnumSet.noneOf(E::class.java)
}

/**
 * Creates an enum set initially containing the specified element.
 *
 * Overloads of this method exist to initialize an enum set with
 * one through five elements. A sixth overloading is provided that
 * uses the varargs feature. This overloading may be used to create
 * an enum set initially containing an arbitrary number of elements, but
 * is likely to run slower than the overloads that do not use varargs.
 */
public fun <E : Enum<E>> enumSetOf(e: E): EnumSet<E> {
    return EnumSet.of(e)
}

/**
 * @see enumSetOf
 */
public fun <E : Enum<E>> enumSetOf(e1: E, e2: E): EnumSet<E> {
    return EnumSet.of(e1, e2)
}

/**
 * @see enumSetOf
 */
public fun <E : Enum<E>> enumSetOf(e1: E, e2: E, e3: E): EnumSet<E> {
    return EnumSet.of(e1, e2, e3)
}

/**
 * @see enumSetOf
 */
public fun <E : Enum<E>> enumSetOf(e1: E, e2: E, e3: E, e4: E): EnumSet<E> {
    return EnumSet.of(e1, e2, e3, e4)
}

/**
 * @see enumSetOf
 */
public fun <E : Enum<E>> enumSetOf(e1: E, e2: E, e3: E, e4: E, e5: E): EnumSet<E> {
    return EnumSet.of(e1, e2, e3, e4, e5)
}

/**
 * Creates an enum set initially containing the specified elements.
 * This factory, whose parameter list uses the varargs feature, may
 * be used to create an enum set initially containing an arbitrary
 * number of elements, but it is likely to run slower than the overloads
 * that do not use varargs.
 */
public fun <E : Enum<E>> enumSetOf(first: E, vararg rest: E): EnumSet<E> {
    return EnumSet.of(first, *rest)
}
