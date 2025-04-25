package com.reservation.customer.auth.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.reservation.auth.login.LoginService;
import com.reservation.auth.login.dto.LoginSettingToken;
import com.reservation.auth.login.dto.OauthSettingToken;
import com.reservation.auth.oauth.OAuthUserInfo;
import com.reservation.auth.oauth.SocialLoginService;
import com.reservation.customer.auth.repository.JpaSocialAccountRepository;
import com.reservation.customer.member.repository.JpaMemberRepository;
import com.reservation.domain.member.Member;
import com.reservation.domain.member.enums.MemberStatus;
import com.reservation.domain.socialaccount.SocialAccount;
import com.reservation.support.enums.Role;
import com.reservation.support.enums.SocialLoginProvider;
import com.reservation.support.exception.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@RequiredArgsConstructor
@Log4j2
public class AuthService {
	private final JpaMemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final LoginService loginService;
	private final SocialLoginService socialLoginService;
	private final JpaSocialAccountRepository socialAccountRepository;

	public LoginSettingToken login(String email, String password) {
		Member checkedMember = checkMemberInfo(email, password);
		return loginService.login(checkedMember.getId(), Role.CUSTOMER);
	}

	private Member checkMemberInfo(String email, String password) {
		Member findMember = memberRepository.findOneByEmailAndStatusIsNot(email, MemberStatus.WITHDRAWN)
			.orElseThrow(() -> ErrorCode.NOT_FOUND.exception("일치하는 로그인 정보가 없습니다."));

		checkMemberStatus(findMember.getStatus());

		if (!passwordEncoder.matches(password, findMember.getPassword())) {
			throw ErrorCode.NOT_FOUND.exception("일치하는 로그인 정보가 없습니다.");
		}
		return findMember;
	}

	private void checkMemberStatus(MemberStatus status) {
		if (status == MemberStatus.INACTIVE) {
			throw ErrorCode.NOT_FOUND.exception("휴먼 계정 입니다. 휴먼 해제 바랍니다.");
		}
		if (status == MemberStatus.SUSPENDED) {
			throw ErrorCode.NOT_FOUND.exception("정지 계정 입니다. 고객 센터로 연락 바랍니다.");
		}
	}

	public OauthSettingToken login(SocialLoginProvider provider, String code) {
		OAuthUserInfo oAuthUserInfo = socialLoginService.authenticate(provider, code);

		Optional<SocialAccount> optionalSocialAccount =
			socialAccountRepository.findOneByProviderAndEmail(provider, oAuthUserInfo.email());

		// 회원 가입이 되어 있지 않은 경우
		if (optionalSocialAccount.isEmpty()) {
			return new OauthSettingToken(null, false, oAuthUserInfo.email());
		}

		Member member = getMe(optionalSocialAccount.get().getMemberId());
		checkMemberStatus(member.getStatus());

		LoginSettingToken loginSettingToken = loginService.login(member.getId(), Role.CUSTOMER);

		return new OauthSettingToken(loginSettingToken, true, oAuthUserInfo.email());
	}

	public Member getMe(Long memberId) {
		return memberRepository.findById(memberId)
			.orElseThrow(() -> ErrorCode.NOT_FOUND.exception("회원 정보가 존재하지 않습니다."));
	}
}
