package msa.hotel.modules.idgenerator

import com.github.f4b6a3.tsid.TsidCreator


class IdGenerator {

    fun generate(): Long {
        return TsidCreator.getTsid().toLong()
    }

}
