package com.reservation.common.member.socialaccount.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.reservation.common.member.socialaccount.domain.SocialAccount;
import com.reservation.commonmodel.auth.login.SocialLoginProvider;

public interface JpaSocialAccountRepository extends JpaRepository<SocialAccount, Long> {
	Optional<SocialAccount> findOneByProviderAndEmail(SocialLoginProvider provider, String email);
}
