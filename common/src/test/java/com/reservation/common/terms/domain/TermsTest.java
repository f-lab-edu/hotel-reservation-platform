package com.reservation.common.terms.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.reservation.common.clause.domain.Clause;
import com.reservation.common.clause.domain.Clause.ClauseBuilder;
import com.reservation.common.exception.BusinessException;
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
			.version(1)
			.isLatest(true)
			.exposedFrom(now)
			.displayOrder(1)
			.build();

		assertNotNull(terms);
		assertEquals(TermsCode.TERMS_USE, terms.getCode());
		assertEquals("이용약관", terms.getTitle());
		assertEquals(TermsType.REQUIRED, terms.getType());
		assertEquals(TermsStatus.ACTIVE, terms.getStatus());
		assertEquals(1, terms.getVersion());
		assertTrue(terms.getIsLatest());
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
			.version(1)
			.isLatest(true)
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
			.version(1)
			.isLatest(true)
			.exposedFrom(LocalDateTime.now())
			.displayOrder(1)
			.build();

		BusinessException exception = assertThrows(BusinessException.class, terms::validateComplete);
		assertEquals("약관은 하나 이상의 조항을 포함해야 합니다.", exception.getMessage());
	}

	@Test
	public void 생성자_실패_제목_누락() {
		LocalDateTime now = LocalDateTime.now();

		BusinessException exception = assertThrows(BusinessException.class, () -> {
			new Terms.TermsBuilder()
				.code(TermsCode.TERMS_USE)
				.type(TermsType.REQUIRED)
				.status(TermsStatus.ACTIVE)
				.version(1)
				.isLatest(true)
				.exposedFrom(now)
				.displayOrder(1)
				.build();
		});
		assertEquals("약관 제목은 필수입니다.", exception.getMessage());
	}

	@Test
	public void 생성자_실패_타입_누락() {
		LocalDateTime now = LocalDateTime.now();

		BusinessException exception = assertThrows(BusinessException.class, () -> {
			new Terms.TermsBuilder()
				.code(TermsCode.TERMS_USE)
				.title("이용약관")
				.status(TermsStatus.ACTIVE)
				.version(1)
				.isLatest(true)
				.exposedFrom(now)
				.displayOrder(1)
				.build();
		});
		assertEquals("약관 타입은 필수입니다.", exception.getMessage());
	}

	@Test
	public void 생성자_실패_상태_누락() {
		LocalDateTime now = LocalDateTime.now();

		BusinessException exception = assertThrows(BusinessException.class, () -> {
			new Terms.TermsBuilder()
				.code(TermsCode.TERMS_USE)
				.title("이용약관")
				.type(TermsType.REQUIRED)
				.version(1)
				.isLatest(true)
				.exposedFrom(now)
				.displayOrder(1)
				.build();
		});
		assertEquals("약관 상태는 필수입니다.", exception.getMessage());
	}

	@Test
	public void 생성자_실패_버전_누락() {
		LocalDateTime now = LocalDateTime.now();

		BusinessException exception = assertThrows(BusinessException.class, () -> {
			new Terms.TermsBuilder()
				.code(TermsCode.TERMS_USE)
				.title("이용약관")
				.type(TermsType.REQUIRED)
				.status(TermsStatus.ACTIVE)
				.isLatest(true)
				.exposedFrom(now)
				.displayOrder(1)
				.build();
		});
		assertEquals("버전은 1 이상이어야 합니다.", exception.getMessage());
	}

	@Test
	public void 생성자_실패_최신버전여부_누락() {
		LocalDateTime now = LocalDateTime.now();

		BusinessException exception = assertThrows(BusinessException.class, () -> {
			new Terms.TermsBuilder()
				.code(TermsCode.TERMS_USE)
				.title("이용약관")
				.type(TermsType.REQUIRED)
				.status(TermsStatus.ACTIVE)
				.version(1)
				.exposedFrom(now)
				.displayOrder(1)
				.build();
		});
		assertEquals("최신 여부는 필수입니다.", exception.getMessage());
	}

	@Test
	public void 생성자_실패_노출시작일_누락() {
		BusinessException exception = assertThrows(BusinessException.class, () -> {
			new Terms.TermsBuilder()
				.code(TermsCode.TERMS_USE)
				.title("이용약관")
				.type(TermsType.REQUIRED)
				.status(TermsStatus.ACTIVE)
				.version(1)
				.isLatest(true)
				.displayOrder(1)
				.build();
		});
		assertEquals("노출 시작일은 필수입니다.", exception.getMessage());
	}

	@Test
	public void 생성자_실패_정렬순서_누락() {
		LocalDateTime now = LocalDateTime.now();

		BusinessException exception = assertThrows(BusinessException.class, () -> {
			new Terms.TermsBuilder()
				.code(TermsCode.TERMS_USE)
				.title("이용약관")
				.type(TermsType.REQUIRED)
				.status(TermsStatus.ACTIVE)
				.version(1)
				.isLatest(false)
				.exposedFrom(now)
				.build();
		});
		assertEquals("정렬 순서는 1 이상이어야 합니다.", exception.getMessage());
	}
}
