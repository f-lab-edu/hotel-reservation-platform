package com.reservation.common.clause.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.reservation.common.terms.domain.Terms;
import com.reservation.commonmodel.terms.TermsCode;
import com.reservation.commonmodel.terms.TermsStatus;
import com.reservation.commonmodel.terms.TermsType;

public class ClauseTest {
	Terms terms = new Terms.TermsBuilder()
		.code(TermsCode.TERMS_USE)
		.title("이용약관")
		.type(TermsType.REQUIRED)
		.status(TermsStatus.ACTIVE)
		.exposedFrom(LocalDateTime.now())
		.displayOrder(1)
		.build();

	@Test
	public void 조문생성_정상() {
		Clause clause = new Clause.ClauseBuilder()
			.terms(terms)
			.clauseOrder(1)
			.title("제1조 (목적)")
			.content("이 약관은...")
			.build();

		assertNotNull(clause);
		assertEquals(terms, clause.getTerms());
		assertEquals(1, clause.getClauseOrder());
		assertEquals("제1조 (목적)", clause.getTitle());
		assertEquals("이 약관은...", clause.getContent());
	}

	@Test
	public void 조문생성_약관매핑누락() {
		Exception exception = assertThrows(IllegalArgumentException.class, () ->
			new Clause.ClauseBuilder()
				.terms(null)
				.clauseOrder(1)
				.title("제1조 (목적)")
				.content("이 약관은...")
				.build()
		);
		assertEquals("약관 정보는 필수입니다.", exception.getMessage());
	}

	@Test
	public void 조문생성_순서오류() {
		Exception exception = assertThrows(IllegalArgumentException.class, () ->
			new Clause.ClauseBuilder()
				.terms(terms)
				.clauseOrder(0)
				.title("제1조 (목적)")
				.content("이 약관은...")
				.build()
		);
		assertEquals("조문 순서는 1 이상이어야 합니다.", exception.getMessage());
	}

	@Test
	public void 조문생성_제목누락() {
		Exception exception = assertThrows(IllegalArgumentException.class, () ->
			new Clause.ClauseBuilder()
				.terms(terms)
				.clauseOrder(1)
				.title("")
				.content("이 약관은...")
				.build()
		);
		assertEquals("조문 제목은 필수입니다.", exception.getMessage());
	}

	@Test
	public void 조문생성_내용누락() {
		Exception exception = assertThrows(IllegalArgumentException.class, () ->
			new Clause.ClauseBuilder()
				.terms(terms)
				.clauseOrder(1)
				.title("제1조 (목적)")
				.content("")
				.build()
		);
		assertEquals("조문 내용은 필수입니다.", exception.getMessage());
	}
}
