package dev.thebjoredcraft.gecko.velocity

import dev.slne.surf.redis.RedisApi
import dev.thebjoredcraft.gecko.velocity.handler.GeckoVelocityRedisHandler

object VelocityRedisService {
    lateinit var redisApi: RedisApi

    fun connect() {
        redisApi = RedisApi.create()
        redisApi.subscribeToEvents(GeckoVelocityRedisHandler)
        redisApi.freezeAndConnect()
    }

    fun disconnect() {
        redisApi.disconnect()
    }
}

val redisApi get() = VelocityRedisService.redisApi