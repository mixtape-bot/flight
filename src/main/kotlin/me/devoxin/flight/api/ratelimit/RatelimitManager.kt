package me.devoxin.flight.api.ratelimit

public interface RatelimitManager {
    /**
     * Checks whether the entity associated with the provided ID is on rate-limit.
     * When BucketType is `GUILD` and the command was invoked in a private context, this
     * method won't be called.
     *
     * @param id
     *        The ID of the entity. If the bucket type is USER, this will be a user ID.
     *        If the bucket type is GUILD, this will be the guild id.
     *        If the bucket type is GLOBAL, this will be -1.
     *
     * @param type
     *        The type of bucket the rate-limit belongs to.
     *        For example, one bucket for each entity type; USER, GUILD, GLOBAL.
     *        If this parameter is GUILD, theoretically you would do `bucket[type].get(id) != null`
     *
     * @param commandName
     *        The command that was invoked.
     *
     * @returns True, if the entity associated with the ID is on rate-limit and the command should
     *          not be executed.
     */
    public fun isRatelimited(id: Long, type: RatelimitType, commandName: String): Boolean

    /**
     * Gets the remaining time of the rate-limit in milliseconds.
     * This may either return 0L, or throw an exception if an entry isn't present, however
     * this should not happen as `isRateLimited` should be called prior to this.
     *
     * @param id
     *        The ID of the entity. The ID could belong to a user or guild, or be -1 if the bucket is GLOBAL.
     *
     * @param type
     *        The type of bucket to check the rate-limit of.
     *
     * @param commandName
     *        The command to get the rate-limit time of.
     */
    public fun getExpirationDate(id: Long, type: RatelimitType, commandName: String): Long

    /**
     * Adds a rate-limit for the given entity ID.
     * It is up to you whether this passively, or actively removes expired rate-limits.
     * When BucketType is `GUILD` and the command was invoked in a private context, this
     * method won't be called.
     *
     * @param id
     *        The ID of the entity, that the rate-limit should be associated with.
     *        This ID could belong to a user or guild. If bucket is GLOBAL, this will be -1.
     *
     * @param type
     *        The type of bucket the rate-limit belongs to.
     *
     * @param duration
     *        How long the rate-limit should last for, in milliseconds.
     *
     * @param commandName
     *        The command to set rate-limit for.
     */
    public fun putRatelimit(id: Long, type: RatelimitType, duration: Long, commandName: String)
}
