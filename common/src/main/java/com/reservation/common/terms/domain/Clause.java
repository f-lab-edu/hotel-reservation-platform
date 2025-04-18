package com.reservation.common.terms.domain;

import com.reservation.common.domain.BaseEntity;
import com.reservation.commonmodel.exception.ErrorCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;

/**
 * 약관 조항
 */
@Entity
@Table(
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_clause_terms_order", columnNames = {"terms_id", "clauseOrder"})
	}
)
public class Clause extends BaseEntity {
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "terms_id", nullable = false)
	private Terms terms;

	@Getter
	@Column(nullable = false)
	private int clauseOrder; // 조문 순서

	@Getter
	@Column(nullable = false)
	private String title; // 제1조 (목적)

	@Getter
	@Lob
	@Column(nullable = false)
	private String content; // 조문 내용

	protected Clause() {
	}

	private Clause(Terms terms, int clauseOrder, String title, String content) {
		if (terms == null) {
			throw ErrorCode.CONFLICT.exception("약관 정보는 필수입니다.");
		}
		if (clauseOrder < 1) {
			throw ErrorCode.CONFLICT.exception("조문 순서는 1 이상이어야 합니다.");
		}
		if (title == null || title.isBlank()) {
			throw ErrorCode.CONFLICT.exception("조문 제목은 필수입니다.");
		}
		if (content == null || content.isBlank()) {
			throw ErrorCode.CONFLICT.exception("조문 내용은 필수입니다.");
		}
		this.terms = terms;
		this.clauseOrder = clauseOrder;
		this.title = title;
		this.content = content;
	}

	public static class ClauseBuilder {
		private Terms terms;
		private int clauseOrder;
		private String title;
		private String content;

		public ClauseBuilder terms(Terms terms) {
			this.terms = terms;
			return this;
		}

		public ClauseBuilder clauseOrder(int clauseOrder) {
			this.clauseOrder = clauseOrder;
			return this;
		}

		public ClauseBuilder title(String title) {
			this.title = title;
			return this;
		}

		public ClauseBuilder content(String content) {
			this.content = content;
			return this;
		}

		public Clause build() {
			return new Clause(terms, clauseOrder, title, content);
		}
	}
}
