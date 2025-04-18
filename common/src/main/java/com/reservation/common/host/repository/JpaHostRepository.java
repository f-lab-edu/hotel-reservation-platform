package com.reservation.common.host.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservation.common.host.domain.Host;
import com.reservation.commonmodel.host.HostStatus;

public interface JpaHostRepository extends JpaRepository<Host, Long> {
	Optional<Host> findOneByEmailAndStatusIsNot(String email, HostStatus status);

}
