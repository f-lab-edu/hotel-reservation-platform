package com.msa.identityservice.config

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.extensions.spring.SpringTestExtension
import io.kotest.extensions.spring.SpringTestLifecycleMode


class TestConfig : AbstractProjectConfig() {

    override fun extensions() = listOf(SpringTestExtension(SpringTestLifecycleMode.Root))

}
