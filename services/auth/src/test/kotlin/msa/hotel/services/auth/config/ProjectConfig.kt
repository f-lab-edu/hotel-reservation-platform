package msa.hotel.services.auth.config

import io.kotest.core.config.AbstractProjectConfig

object ProjectConfig : AbstractProjectConfig() {
    @Deprecated("use beforeProject supports suspension", replaceWith = ReplaceWith("beforeProject"))
    override fun beforeAll() {
        DotenvLoader.load()
    }
}
