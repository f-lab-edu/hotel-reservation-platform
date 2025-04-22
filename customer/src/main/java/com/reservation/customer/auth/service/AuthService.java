package com.reservation.customer.auth.service;

import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.reservation.common.member.socialaccount.domain.SocialAccount;
import com.reservation.common.member.socialaccount.repository.JpaSocialAccountRepository;
import com.reservation.commonapi.customer.repository.CustomerMemberRepository;
import com.reservation.commonauth.auth.login.LoginService;
import com.reservation.commonauth.auth.login.social.OAuthUserInfo;
import com.reservation.commonauth.auth.login.social.SocialLoginService;
import com.reservation.commonmodel.auth.Role;
import com.reservation.commonmodel.auth.login.SocialLoginProvider;
import com.reservation.commonmodel.exception.ErrorCode;
import com.reservation.commonmodel.member.MemberDto;
import com.reservation.commonmodel.member.MemberStatus;
import com.reservation.customer.auth.controller.dto.request.LoginRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class AuthService {
	private static final String REDIRECT_URL = "https://hotel-reservation-frontend.com/login/success";
	private static final String SOCIAL_SIGNUP_URL = "https://hotel-reservation-frontend.com/signup?email=";

	private final CustomerMemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final LoginService loginService;
	private final SocialLoginService socialLoginService;
	private final JpaSocialAccountRepository socialAccountRepository;

	public ResponseEntity<Void> login(LoginRequest request) {
		MemberDto memberDto = checkMemberInfo(request.email(), request.password());

		return loginService.login(memberDto.id(), Role.CUSTOMER);
	}

	private MemberDto checkMemberInfo(String email, String password) {
		MemberDto memberDto = memberRepository.findOneByEmailAndStatusIsNot(email, MemberStatus.WITHDRAWN)
			.orElseThrow(() -> ErrorCode.NOT_FOUND.exception("일치하는 로그인 정보가 없습니다."));

		checkMemberStatus(memberDto.status());

		if (!passwordEncoder.matches(password, memberDto.password())) {
			throw ErrorCode.NOT_FOUND.exception("일치하는 로그인 정보가 없습니다.");
		}
		return memberDto;
	}

	private void checkMemberStatus(MemberStatus status) {
		if (status == MemberStatus.INACTIVE) {
			throw ErrorCode.NOT_FOUND.exception("휴먼 계정 입니다. 휴먼 해제 바랍니다.");
		}
		if (status == MemberStatus.SUSPENDED) {
			throw ErrorCode.NOT_FOUND.exception("정지 계정 입니다. 고객 센터로 연락 바랍니다.");
		}
	}

	public MemberDto findMe(Long memberId) {
		return memberRepository.findById(memberId)
			.orElseThrow(() -> ErrorCode.NOT_FOUND.exception("회원 정보가 존재하지 않습니다."));
	}

	public ResponseEntity<Void> login(SocialLoginProvider provider, String code) {
		OAuthUserInfo oAuthUserInfo = socialLoginService.authenticate(provider, code);
		Optional<SocialAccount> optionalSocialAccount = socialAccountRepository.findOneByProviderAndEmail(provider,
			oAuthUserInfo.email());

		// 신규 회원 가입
		if (optionalSocialAccount.isEmpty()) {
			return ResponseEntity
				.status(HttpStatus.FOUND)
				.header(HttpHeaders.LOCATION, SOCIAL_SIGNUP_URL + oAuthUserInfo.email())
				.build();
		}

		MemberDto memberDto = findMe(optionalSocialAccount.get().getMemberId());
		checkMemberStatus(memberDto.status());

		return loginService.login(memberDto.id(), Role.CUSTOMER, REDIRECT_URL);
	}
}
