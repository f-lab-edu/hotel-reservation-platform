package msa.hotel.services.auth.config

import io.kotest.core.config.AbstractProjectConfig

object ProjectConfig : AbstractProjectConfig() {
    override fun beforeAll() {
        DotenvLoader.load()
    }
}
