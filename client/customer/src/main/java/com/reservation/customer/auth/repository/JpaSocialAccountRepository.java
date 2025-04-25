package com.reservation.customer.auth.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservation.auth.oauth.enums.SocialLoginProvider;
import com.reservation.domain.socialaccount.SocialAccount;

public interface JpaSocialAccountRepository extends JpaRepository<SocialAccount, Long> {
	Optional<SocialAccount> findOneByProviderAndEmail(SocialLoginProvider provider, String email);
}
