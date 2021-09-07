package me.devoxin.flight.internal.parsers

import me.devoxin.flight.api.command.message.MessageContext
import me.devoxin.flight.api.command.slash.SlashContext
import me.devoxin.flight.internal.arguments.types.Snowflake
import net.dv8tion.jda.api.interactions.commands.OptionMapping
import net.dv8tion.jda.api.interactions.commands.OptionType
import java.util.*
import java.util.regex.Pattern

object SnowflakeResolver : Resolver<Snowflake> {
    private val snowflakeMatch = Pattern.compile("^(?:<(?:@!?|@&|#)(?<sid>[0-9]{17,21})>|(?<id>[0-9]{17,21}))$")

    override val optionType: OptionType = OptionType.STRING

    override suspend fun resolveOption(ctx: SlashContext, option: OptionMapping): Optional<Snowflake> =
        parse(option.asString)

    override suspend fun resolve(ctx: MessageContext, param: String): Optional<Snowflake> =
        parse(param)

    private fun parse(param: String): Optional<Snowflake> {
        val match = snowflakeMatch.matcher(param)

        if (match.matches()) {
            val id = match.group("sid") ?: match.group("id")
            return Optional.of(Snowflake(id.toLong()))
        }

        return Optional.empty()
    }
}
