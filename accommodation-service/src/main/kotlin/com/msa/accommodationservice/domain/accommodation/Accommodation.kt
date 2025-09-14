package com.msa.accommodationservice.domain.accommodation

import com.msa.supportmodule.exception.BusinessErrorCode


data class Accommodation(
    val id: Long,
    val hostId: Long,
    val name: String,
    val description: String,
    val city: String,
    val address: String,
    val contactNumber: String
) {

    init {
        if (name.isBlank()) {
            throw BusinessErrorCode.CONFLICT.exception("숙소 이름은 빈칸일 수 없습니다.")
        }
        if (description.isBlank()) {
            throw BusinessErrorCode.CONFLICT.exception("숙소 설명은 빈칸일 수 없습니다.")
        }
        if (city.isBlank()) {
            throw BusinessErrorCode.CONFLICT.exception("숙소 위치의 도시는 빈칸일 수 없습니다.")
        }
        if (address.isBlank()) {
            throw BusinessErrorCode.CONFLICT.exception("숙소 주소는 빈칸일 수 없습니다.")
        }
        if (contactNumber.isBlank()) {
            throw BusinessErrorCode.CONFLICT.exception("숙소 연락처는 빈칸일 수 없습니다.")
        }
    }

}
