package me.devoxin.flight.internal.parsers

import me.devoxin.flight.api.Context
import java.util.*

class FloatParser : Parser<Float> {

    override suspend fun parse(ctx: Context, param: String): Optional<Float> {
        return Optional.ofNullable(param.toFloatOrNull())
    }

}
