package msa.hotel.modules.idgenerator

import com.github.f4b6a3.tsid.TsidCreator

class IdGenerator {
    fun generate(): ULong = TsidCreator.getTsid().toLong().toULong()
}
