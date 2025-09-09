package com.msa.identityservice.infrastructure

import com.github.f4b6a3.tsid.TsidCreator
import org.springframework.stereotype.Component

@Component
class IdGenerator {

    fun generate(): Long {
        return TsidCreator.getTsid().toLong()
    }
    
}
