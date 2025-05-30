package com.reservation.customer.accommodation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservation.domain.accommodation.Accommodation;

public interface JpaAccommodationRepository extends JpaRepository<Accommodation, Long> {
}
