package com.reservation.common.host.repository;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.reservation.common.host.repository.dto.HostMapper;
import com.reservation.commonapi.host.repository.HostModuleRepository;
import com.reservation.commonmodel.host.HostDto;
import com.reservation.commonmodel.host.HostStatus;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class HostRepository implements HostModuleRepository {
	private final JpaHostRepository jpaHostRepository;

	@Override
	public Optional<HostDto> findOneByEmailAndStatusIsNot(String email, HostStatus status) {
		return jpaHostRepository.findOneByEmailAndStatusIsNot(email, status)
			.map(HostMapper::fromEntityToDto);
	}

	@Override
	public Optional<HostDto> findById(Long id) {
		return jpaHostRepository.findById(id).map(HostMapper::fromEntityToDto);
	}
}
