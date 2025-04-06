package com.reservation.common.terms.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.reservation.common.clause.domain.Clause;
import com.reservation.common.clause.domain.Clause.ClauseBuilder;
import com.reservation.common.terms.domain.Terms.TermsBuilder;
import com.reservation.commonmodel.exception.BusinessException;
import com.reservation.commonmodel.terms.TermsCode;
import com.reservation.commonmodel.terms.TermsStatus;
import com.reservation.commonmodel.terms.TermsType;

public class ClausesTest {
	@Test
	public void 조문리스트일급컬렉션생성_성공() {
		LocalDateTime now = LocalDateTime.now();
		Terms terms = new TermsBuilder()
			.code(TermsCode.TERMS_USE)
			.title("이용약관")
			.type(TermsType.REQUIRED)
			.status(TermsStatus.ACTIVE)
			.version(1)
			.isLatest(true)
			.exposedFrom(now)
			.displayOrder(1)
			.build();
		Clause clause1 = new ClauseBuilder()
			.terms(terms)
			.clauseOrder(1)
			.title("제1조 (목적)")
			.content("이 약관은 회사와 사용자 간의 서비스 이용에 관한 기본적인 사항을 규정합니다.")
			.build();
		Clause clause2 = new ClauseBuilder()
			.terms(terms)
			.clauseOrder(2)
			.title("제2조 (정의)")
			.content("이 약관에서 사용하는 용어의 정의는 다음과 같습니다...")
			.build();

		Clauses clauses = new Clauses(List.of(clause1, clause2));

		assertNotNull(clauses);
		assertEquals(2, clauses.size());
		assertEquals(clause1, clauses.get(0));
		assertEquals(clause2, clauses.get(1));
	}

	@Test
	public void 조문리스트일급컬렉션생성_조항최소개수미달() {
		Exception exception = assertThrows(BusinessException.class, () -> new Clauses(List.of()));

		assertEquals("조항은 최소 1개 이상이어야 합니다.", exception.getMessage());
	}
}
