package com.reservation.host.host.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservation.domain.host.Host;
import com.reservation.domain.host.enums.HostStatus;

public interface JpaHostRepository extends JpaRepository<Host, Long> {
	Optional<Host> findOneByEmailAndStatusIsNot(String email, HostStatus status);
}
