package me.devoxin.flight.internal.utils

internal fun <E> MutableList<E>.splice(amount: Int): List<E> =
    take(amount).onEach { removeFirst() }

internal fun <T> List<T>.optimizeReadOnlyList(): List<T> = when (size) {
    0 -> emptyList()
    1 -> listOf(this[0])
    else -> this
}
