package com.reservation.commonapi.host.repository;

import java.util.Optional;

import com.reservation.commonmodel.host.HostDto;
import com.reservation.commonmodel.host.HostStatus;

public interface HostModuleRepository {

	Optional<HostDto> findOneByEmailAndStatusIsNot(String email, HostStatus status);

	Optional<HostDto> findById(Long id);
}
