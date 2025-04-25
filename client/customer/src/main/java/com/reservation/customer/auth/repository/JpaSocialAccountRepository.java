package com.reservation.customer.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservation.domain.socialaccount.SocialAccount;
import com.reservation.support.enums.SocialLoginProvider;

public interface JpaSocialAccountRepository extends JpaRepository<SocialAccount, Long> {
	Optional<SocialAccount> findOneByProviderAndEmail(SocialLoginProvider provider, String email);
}
