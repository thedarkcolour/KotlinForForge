package thedarkcolour.kotlinforforge.neoforge.forge

import net.minecraft.world.item.ItemStack
import net.neoforged.neoforge.transfer.ResourceHandler
import net.neoforged.neoforge.transfer.ResourceHandlerUtil
import net.neoforged.neoforge.transfer.item.ItemResource
import net.neoforged.neoforge.transfer.item.ItemUtil
import net.neoforged.neoforge.transfer.resource.Resource
import net.neoforged.neoforge.transfer.resource.ResourceStack
import net.neoforged.neoforge.transfer.transaction.Transaction

/**
 * Opens a root transaction and runs [block] with that transaction supplied as a context parameter.
 *
 * A transaction can be treated as a checkpoint:
 *
 * * [openRoot] creates a root checkpoint.
 * * Resource operations can modify transaction-aware objects after the checkpoint is created.
 * * [commit] validates the changes made in the current transaction and passes them to its parent, if any.
 * * Leaving the scope without calling [commit] aborts the transaction and rolls back its changes.
 * * [open] creates a child checkpoint. Committing the child does not commit the root transaction.
 *
 * Example:
 *
 * ```kotlin
 * fun transfer(
 *     source: ResourceHandler<ItemResource>,
 *     target: ResourceHandler<ItemResource>,
 *     stack: ItemStack,
 * ): Boolean {
 *     if (stack.isEmpty) return false
 *
 *     val resource = ItemResource.of(stack)
 *     return openRoot {
 *         // (A) Extract the resource in the root transaction.
 *         val extracted = source.extract(resource, stack.count)
 *         val transferred = if (extracted > 0) {
 *             open {
 *                 // (B) Try to insert it in a child transaction.
 *                 (target.insert(resource, extracted) == extracted).also { fullyInserted ->
 *                     if (fullyInserted) {
 *                         commit()
 *                     }
 *                 }
 *             }
 *         } else {
 *             false
 *         }
 *
 *         // (C) Apply the child changes and the extraction only if the transfer succeeded.
 *         if (transferred) {
 *             commit()
 *         }
 *         transferred
 *     }
 * }
 * ```
 *
 * In this example, if the target cannot accept the complete extracted amount, the child transaction is closed
 * without [commit], and the root transaction is also closed without [commit]. Both operations are therefore rolled
 * back. If the transfer succeeds, the child transaction is committed first and the root transaction is committed
 * afterwards, applying all changes. The Boolean returned by [openRoot] is the value returned by [block]; it does not
 * commit the transaction by itself.
 *
 * Transactions are valid only on the thread on which they were opened. A root transaction can only be opened when
 * no transaction is open or closing on the current thread.
 *
 * @param block Operations to run in the root transaction.
 * @return The value returned by [block]. Returning a value does not commit the transaction; [commit] must still be
 * called explicitly.
 * @throws IllegalStateException If a transaction is already open or closing on the current thread.
 */
public fun <R> openRoot(block: context(Transaction) () -> R): R = Transaction.openRoot().use {
    context(it) {
        block()
    }
}

/**
 * Opens a child transaction with the current transaction as its parent and runs [block] with the child supplied as a
 * context parameter.
 *
 * The child transaction's changes are applied to its parent only if [commit] is called. Otherwise, they are aborted
 * when the block completes. Committed child changes may still be aborted if an ancestor transaction is not committed.
 *
 * @param block Operations to run in the child transaction.
 * @return The value returned by [block]. Returning a value does not commit the child transaction; [commit] must still
 * be called explicitly.
 * @throws IllegalStateException If the parent transaction is not the current transaction or has already been closed.
 */
context(transaction: Transaction)
public fun <R> open(block: context(Transaction) () -> R): R = Transaction.open(transaction).use {
    context(it) {
        block()
    }
}

/**
 * Commits and closes the current transaction, including changes from its committed child transactions.
 *
 * Committing a child transaction applies its changes to its parent, but the changes are applied only if the root
 * transaction is also committed. Calling this function on a root transaction applies its changes. The transaction
 * must be current and must be used on the thread on which it was opened.
 *
 * @throws IllegalStateException If this is not the current transaction, it was already closed, or it is used from
 * a different thread.
 */
context(transaction: Transaction)
public fun commit() {
    transaction.commit()
}

/**
 * Inserts up to [amount] of [resource] into this handler at [index].
 *
 * Changes are made within the current transaction. The resource must not be empty and [amount] must be non-negative.
 *
 * @param index The index to insert into.
 * @param resource The non-empty resource to insert.
 * @param amount The maximum amount to insert; must be non-negative.
 * @return The amount inserted, from `0` (nothing inserted) to [amount] (the full amount inserted).
 * @throws IllegalArgumentException If [resource] is empty or [amount] is negative.
 */
context(transaction: Transaction)
public fun <T : Resource> ResourceHandler<T>.insert(index: Int, resource: T, amount: Int): Int {
    return insert(index, resource, amount, transaction)
}

/**
 * Inserts up to [amount] of [resource] into this handler, letting the handler choose the indices to use.
 * This overload is preferred when an index is not required and is expected to be more efficient than selecting
 * an index in the caller.
 *
 * Changes are made within the current transaction. The resource must not be empty and [amount] must be non-negative.
 *
 * @param resource The non-empty resource to insert.
 * @param amount The maximum amount to insert; must be non-negative.
 * @return The amount inserted, from `0` (nothing inserted) to [amount] (the full amount inserted).
 * @throws IllegalArgumentException If [resource] is empty or [amount] is negative.
 */
context(transaction: Transaction)
public fun <T : Resource> ResourceHandler<T>.insert(resource: T, amount: Int): Int {
    return insert(resource, amount, transaction)
}

/**
 * Extracts up to [amount] of [resource] from this handler at [index].
 *
 * Changes are made within the current transaction. The resource must not be empty and [amount] must be non-negative.
 *
 * @param index The index to extract from.
 * @param resource The non-empty resource to extract.
 * @param amount The maximum amount to extract; must be non-negative.
 * @return The amount extracted, from `0` (nothing extracted) to [amount] (the full amount extracted).
 * @throws IllegalArgumentException If [resource] is empty or [amount] is negative.
 */
context(transaction: Transaction)
public fun <T : Resource> ResourceHandler<T>.extract(index: Int, resource: T, amount: Int): Int {
    return extract(index, resource, amount, transaction)
}

/**
 * Extracts up to [amount] of [resource] from this handler, letting the handler find the indices to use.
 * This overload is preferred when an index is not required and is expected to be more efficient than selecting
 * indices in the caller.
 *
 * Changes are made within the current transaction. The resource must not be empty and [amount] must be non-negative.
 *
 * @param resource The non-empty resource to extract.
 * @param amount The maximum amount to extract; must be non-negative.
 * @return The amount extracted, from `0` (nothing extracted) to [amount] (the full amount extracted).
 * @throws IllegalArgumentException If [resource] is empty or [amount] is negative.
 */
context(transaction: Transaction)
public fun <T : Resource> ResourceHandler<T>.extract(resource: T, amount: Int): Int {
    return extract(resource, amount, transaction)
}

/**
 * Attempts to insert up to [amount] of [resource] using stacking logic: filled indices are tried before empty indices.
 *
 * If [handler] is `null`, this function does nothing and returns `0`. The resource must not be empty and [amount]
 * must be non-negative. Successful changes are committed to the current transaction and can still be aborted by
 * rolling back that transaction.
 *
 * @param handler The handler to insert into, or `null` for a no-op.
 * @param resource The non-empty resource to insert.
 * @param amount The maximum amount to insert; must be non-negative.
 * @return The amount inserted.
 * @throws IllegalArgumentException If [resource] is empty or [amount] is negative.
 */
context(transaction: Transaction)
public fun <T : Resource> insertStacking(
    handler: ResourceHandler<T>?,
    resource: T,
    amount: Int
): Int {
    return ResourceHandlerUtil.insertStacking(handler, resource, amount, transaction)
}

/**
 * Extracts up to [amount] of the first non-empty resource in [handler] for which [predicate] returns `true`.
 * The predicate is never tested with an empty resource. The operation uses a nested transaction and commits it
 * only if a positive amount was extracted; the enclosing transaction can still be aborted.
 *
 * If [handler] is `null` or [amount] is `0`, no extraction is attempted and `null` is returned. [amount] must be
 * non-negative.
 *
 * @param handler The handler to extract from, or `null` for a no-op.
 * @param amount The desired maximum amount to extract; must be non-negative.
 * @param predicate A filter for the non-empty resources in the handler.
 * @return The resource and amount extracted, or `null` if nothing was extracted.
 * @throws IllegalArgumentException If [amount] is negative.
 */
context(transaction: Transaction)
public fun <T : Resource> extractFirst(
    handler: ResourceHandler<T>?,
    amount: Int,
    predicate: (Resource) -> Boolean
): ResourceStack<T>? {
    return ResourceHandlerUtil.extractFirst(handler, predicate, amount, transaction)
}

/**
 * Moves up to [amount] of resources from [from] to [to] when [predicate] returns `true`.
 * The predicate is never tested with an empty resource. The operation uses nested transactions and commits
 * successful transfers to the current transaction; the enclosing transaction can still be aborted.
 *
 * If either handler is `null` or [amount] is `0`, nothing is moved and `0` is returned. [amount] must be
 * non-negative.
 *
 * @param from The source handler, or `null` for a no-op.
 * @param to The target handler, or `null` for a no-op.
 * @param amount The maximum amount to move; must be non-negative.
 * @param predicate A filter for the non-empty resources that may be moved.
 * @return The amount moved.
 * @throws IllegalArgumentException If [amount] is negative.
 */
context(transaction: Transaction)
public fun <T : Resource> move(
    from: ResourceHandler<T>?,
    to: ResourceHandler<T>?,
    amount: Int,
    predicate: (Resource) -> Boolean
): Int {
    return ResourceHandlerUtil.move(from, to, predicate, amount, transaction)
}

/**
 * Moves up to [amount] of the first matching resource that can be transferred from [from] to [to].
 * The predicate is never tested with an empty resource. Successful transfers are committed to the current
 * transaction and can still be aborted by rolling it back.
 *
 * If either handler is `null` or [amount] is `0`, nothing is moved and `null` is returned. [amount] must be
 * non-negative.
 *
 * @param from The source handler, or `null` for a no-op.
 * @param to The target handler, or `null` for a no-op.
 * @param amount The maximum amount to move; must be non-negative.
 * @param predicate A filter for the non-empty resources that may be moved.
 * @return The resource and amount moved, or `null` if nothing was transferred.
 * @throws IllegalArgumentException If [amount] is negative.
 */
context(transaction: Transaction)
public fun <T : Resource> moveFirst(
    from: ResourceHandler<T>?,
    to: ResourceHandler<T>?,
    amount: Int,
    predicate: (Resource) -> Boolean
): ResourceStack<T>? {
    return ResourceHandlerUtil.moveFirst(from, to, predicate, amount, transaction)
}

/**
 * Finds the first non-empty resource in [handler] for which [predicate] returns `true` and which can be extracted.
 * The predicate is never tested with an empty resource. Extractability is tested in a nested transaction that is
 * always rolled back, so this function does not change the handler.
 *
 * @param handler The handler to search.
 * @param predicate A filter for the non-empty resources in the handler.
 * @return The first matching extractable resource, or `null` if no such resource exists.
 */
context(transaction: Transaction)
public fun <T : Resource> findExtractableResource(handler: ResourceHandler<T>, predicate: (Resource) -> Boolean): T? {
    return ResourceHandlerUtil.findExtractableResource(handler, predicate, transaction)
}

/**
 * Attempts to insert [itemStack] into [handler], leaving distribution to the handler, and returns any leftover.
 * The input stack is not modified. If [simulate] is `true`, the insertion is simulated and the handler is left
 * unchanged; otherwise, changes are committed to the current transaction and can still be aborted by rolling it back.
 *
 * @param handler The handler to insert into.
 * @param itemStack The stack to insert. It is not modified.
 * @param simulate `true` to simulate the result without changing the handler; `false` to insert the items.
 * @return The items that could not be inserted, or an empty stack if all items were inserted.
 */
context(transaction: Transaction)
public fun insertItemReturnRemaining(
    handler: ResourceHandler<ItemResource>,
    itemStack: ItemStack,
    simulate: Boolean
): ItemStack {
    return ItemUtil.insertItemReturnRemaining(handler, itemStack, simulate, transaction)
}

/**
 * Attempts to insert [itemStack] into [handler] at [index] and returns any leftover. The input stack is not modified.
 * If [simulate] is `true`, the insertion is simulated and the handler is left unchanged; otherwise, changes are
 * committed to the current transaction and can still be aborted by rolling it back.
 *
 * @param handler The handler to insert into.
 * @param index The index to insert into.
 * @param itemStack The stack to insert. It is not modified.
 * @param simulate `true` to simulate the result without changing the handler; `false` to insert the items.
 * @return The items that could not be inserted, or an empty stack if all items were inserted.
 */
context(transaction: Transaction)
public fun insertItemReturnRemaining(
    handler: ResourceHandler<ItemResource>,
    index: Int,
    itemStack: ItemStack,
    simulate: Boolean
): ItemStack {
    return ItemUtil.insertItemReturnRemaining(handler, index, itemStack, simulate, transaction)
}
