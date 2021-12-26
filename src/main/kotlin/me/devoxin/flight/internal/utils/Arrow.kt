package me.devoxin.flight.internal.utils

import arrow.core.*
import arrow.core.computations.ResultEffect.bind

public fun <T> some(value: T): Option<T> = Some(value)

/* result */
public fun <T> Result<T>.toOption(): Option<T> {
    return if (isFailure) none() else some(bind())
}

// rust like extensions cus rust op

/* UNWRAP/EXPECT */
public fun <T> Option<T>.expect(message: String): T =
    getOrElse { throw NullPointerException(message) }

public fun <T> Option<T>.unwrap(): T =
    getOrElse { throw NullPointerException() }

/* UNWRAP OR */
public fun <T> Option<T>.unwrapOr(default: T): T =
    getOrElse { default }

public inline fun <T> Option<T>.unwrapOr(default: () -> T): T =
    getOrElse(default)

/* UNWRAP OR ELSE */
public fun <T> Option<T>.unwrapOrElse(default: Option<T>): T =
    orElse { default }.unwrap()

public inline fun <T> Option<T>.unwrapOrElse(default: () -> Option<T>): T =
    orElse(default).unwrap()
