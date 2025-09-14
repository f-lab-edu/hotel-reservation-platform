package com.msa.accommodationservice.accommodation.service

import com.msa.accommodationservice.accommodation.repository.AccommodationRepository
import com.msa.accommodationservice.accommodation.service.dto.AccommodationDto
import com.msa.accommodationservice.domain.accommodation.Accommodation
import com.msa.supportmodule.exception.BusinessErrorCode
import com.msa.supportmodule.infrastructure.IdGenerator
import org.springframework.stereotype.Service


@Service
class AccommodationService(
    private val repository: AccommodationRepository,
    private val idGenerator: IdGenerator,
) {

    fun register(dto: AccommodationDto): Accommodation {
        checkAlreadyRegisteredThrow(dto.hostId)

        checkNameAndAddressDuplicateThrow(
            name = dto.name,
            address = dto.address
        )

        val newAccommodation = dto.toAccommodation(newId = idGenerator.generate())

        repository.insert(newAccommodation)

        return newAccommodation
    }

    private fun checkAlreadyRegisteredThrow(hostId: Long) {
        if (repository.findByHostId(hostId) != null) {
            throw BusinessErrorCode.CONFLICT.exception("이미 숙소 정보를 등록했습니다. 수정을 원할시 수정 기능을 사용하세요.")
        }
    }

    private fun checkNameAndAddressDuplicateThrow(name: String, address: String) {
        if (repository.findByNameAndAddress(name = name, address = address) != null) {
            throw BusinessErrorCode.CONFLICT.exception("이미 해당 이름과 주소의 숙소 정보가 존재합니다.")
        }
    }

    fun update(dto: AccommodationDto): Accommodation {
        val registeredAccommodation = repository.findByHostId(dto.hostId)
            ?: throw BusinessErrorCode.BAD_REQUEST.exception("숙소 정보를 아직 등록하지 않았습니다. 수정할 숙소 정보를 먼저 등록하세요.")

        if (!(registeredAccommodation.name == dto.name && registeredAccommodation.address == dto.address)) {
            checkNameAndAddressDuplicateThrow(
                name = dto.name,
                address = dto.address
            )
        }

        val updateAccommodation = Accommodation(
            id = registeredAccommodation.id,
            hostId = registeredAccommodation.hostId,
            name = dto.name,
            description = dto.description,
            contactNumber = dto.contactNumber,
            city = dto.city,
            address = dto.address
        )

        repository.update(updateAccommodation)

        return updateAccommodation
    }

    fun getAccommodations(): List<Accommodation> {

        return repository.findAll()
    }

    fun getAccommodation(accommodationId: Long): Accommodation {
        val accommodation = repository.findById(accommodationId)
            ?: throw BusinessErrorCode.BAD_REQUEST.exception("존재하지 않는 숙소 정보입니다.")

        return accommodation
    }

    fun getAccommodationByHostId(hostId: Long): Accommodation {
        val registeredAccommodation = repository.findByHostId(hostId)
            ?: throw BusinessErrorCode.BAD_REQUEST.exception("숙소 정보를 아직 등록하지 않았습니다. 수정할 숙소 정보를 먼저 등록하세요.")

        return registeredAccommodation
    }

}
