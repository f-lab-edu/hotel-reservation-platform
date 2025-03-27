package com.reservation.common.terms.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.reservation.common.clause.domain.Clause;
import com.reservation.common.clause.domain.Clause.ClauseBuilder;
import com.reservation.common.terms.domain.Terms.TermsBuilder;
import com.reservation.commonmodel.terms.TermsCode;
import com.reservation.commonmodel.terms.TermsStatus;
import com.reservation.commonmodel.terms.TermsType;

public class TermsTest {
	@Test
	public void 약관생성_성공() {
		LocalDateTime now = LocalDateTime.now();
		Terms terms = new TermsBuilder()
			.code(TermsCode.TERMS_USE)
			.title("이용약관")
			.type(TermsType.REQUIRED)
			.status(TermsStatus.ACTIVE)
			.exposedFrom(now)
			.displayOrder(1)
			.build();

		assertNotNull(terms);
		assertEquals(TermsCode.TERMS_USE, terms.getCode());
		assertEquals("이용약관", terms.getTitle());
		assertEquals(TermsType.REQUIRED, terms.getType());
		assertEquals(TermsStatus.ACTIVE, terms.getStatus());
		assertEquals(now, terms.getExposedFrom());
		assertEquals(1, terms.getDisplayOrder());
	}

	@Test
	public void 약관에조항일급컬렉션세팅_성공() {
		Terms terms = new TermsBuilder()
			.code(TermsCode.TERMS_USE)
			.title("이용약관")
			.type(TermsType.REQUIRED)
			.status(TermsStatus.ACTIVE)
			.exposedFrom(LocalDateTime.now())
			.displayOrder(1)
			.build();

		Clause clause = new ClauseBuilder()
			.terms(terms)
			.clauseOrder(1)
			.title("제1조 (목적)")
			.content("이 약관은 회사와 사용자 간의 서비스 이용에 관한 기본적인 사항을 규정합니다.")
			.build();
		Clauses clauses = new Clauses(List.of(clause));

		terms.setClauses(clauses);

		assertNotNull(terms.getClauseList());
		assertEquals(1, terms.getClauseList().size());
		assertEquals(clause, terms.getClauseList().getFirst());
		assertDoesNotThrow(terms::validateComplete);
	}

	@Test
	public void 약관상태검증_조항항목누락() {
		Terms terms = new TermsBuilder()
			.code(TermsCode.TERMS_USE)
			.title("이용약관")
			.type(TermsType.REQUIRED)
			.status(TermsStatus.ACTIVE)
			.exposedFrom(LocalDateTime.now())
			.displayOrder(1)
			.build();

		Exception exception = assertThrows(IllegalStateException.class, terms::validateComplete);
		assertEquals("약관은 하나 이상의 조항을 포함해야 합니다.", exception.getMessage());
	}
}
