package com.reservation.common.terms.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.reservation.common.clause.domain.Clause;
import com.reservation.common.domain.BaseEntity;
import com.reservation.commonmodel.terms.TermsCode;
import com.reservation.commonmodel.terms.TermsStatus;
import com.reservation.commonmodel.terms.TermsType;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PostLoad;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.ToString;

@Entity
@Table(
	name = "terms",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_terms_code_version", columnNames = {"code", "version"})
	}
)
@Getter
@ToString
public class Terms extends BaseEntity {

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TermsCode code; // ex: TERMS001

	@Column(nullable = false)
	private String title; // ex: 이용약관

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TermsType type; // 필수 or 선택

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TermsStatus status; // 사용 or 미사용

	private Integer version; // 버전 역할

	@Column(nullable = false)
	private LocalDateTime exposedFrom; // 노출 시작일

	@Column(nullable = true)
	private LocalDateTime exposedTo; // 노출 종료일

	@Column(nullable = false)
	private Integer displayOrder; // 정렬 순서

	@OneToMany(mappedBy = "terms", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@ToString.Exclude
	private List<Clause> clauseList = new ArrayList<>();

	@Transient
	private Clauses clauses;

	protected Terms() {
	}

	private Terms(TermsCode code, String title, TermsType type, TermsStatus status, Integer version,
		LocalDateTime exposedFrom,
		LocalDateTime exposedTo, Integer displayOrder) {
		if (code == null) {
			throw new IllegalArgumentException("약관 코드는 필수입니다.");
		}
		if (title == null || title.isBlank()) {
			throw new IllegalArgumentException("약관 제목은 필수입니다.");
		}
		if (type == null) {
			throw new IllegalArgumentException("약관 타입은 필수입니다.");
		}
		if (status == null) {
			throw new IllegalArgumentException("약관 상태는 필수입니다.");
		}
		if (version == null || version < 1) {
			throw new IllegalArgumentException("버전은 1 이상이어야 합니다.");
		}
		if (exposedFrom == null) {
			throw new IllegalArgumentException("노출 시작일은 필수입니다.");
		}
		if (displayOrder == null || displayOrder < 1) {
			throw new IllegalArgumentException("정렬 순서는 1 이상이어야 합니다.");
		}
		if (exposedTo != null && exposedFrom.isAfter(exposedTo)) {
			throw new IllegalArgumentException("노출 종료일은 노출 시작일보다 늦어야 합니다.");
		}
		this.code = code;
		this.title = title;
		this.type = type;
		this.status = status;
		this.version = version;
		this.exposedFrom = exposedFrom;
		this.exposedTo = exposedTo;
		this.displayOrder = displayOrder;
	}

	public static class TermsBuilder {
		private TermsCode code;
		private String title;
		private TermsType type;
		private TermsStatus status;
		private Integer version;
		private LocalDateTime exposedFrom;
		private LocalDateTime exposedTo;
		private Integer displayOrder;

		public TermsBuilder code(TermsCode code) {
			this.code = code;
			return this;
		}

		public TermsBuilder title(String title) {
			this.title = title;
			return this;
		}

		public TermsBuilder type(TermsType type) {
			this.type = type;
			return this;
		}

		public TermsBuilder status(TermsStatus status) {
			this.status = status;
			return this;
		}

		public TermsBuilder version(Integer version) {
			this.version = version;
			return this;
		}

		public TermsBuilder exposedFrom(LocalDateTime exposedFrom) {
			this.exposedFrom = exposedFrom;
			return this;
		}

		public TermsBuilder exposedTo(LocalDateTime exposedTo) {
			this.exposedTo = exposedTo;
			return this;
		}

		public TermsBuilder displayOrder(Integer displayOrder) {
			this.displayOrder = displayOrder;
			return this;
		}

		public Terms build() {
			return new Terms(code, title, type, status, version, exposedFrom, exposedTo, displayOrder);
		}
	}

	@PostLoad
	private void onLoad() {
		this.clauses = new Clauses(this.clauseList);
	}

	public Clauses getClauses() {
		if (clauses == null) {
			this.clauses = new Clauses(this.clauseList);
		}
		return clauses;
	}

	public void addClause(Clause clause) {
		this.clauseList.add(clause);
	}

	public void setClauses(Clauses clauses) {
		this.clauses = clauses;
		for (Clause clause : clauses.values()) {
			addClause(clause);
		}
	}

	public void validateComplete() {
		if (clauses == null || clauses.isEmpty()) {
			throw new IllegalStateException("약관은 하나 이상의 조항을 포함해야 합니다.");
		}
	}

	public void deprecate() {
		this.status = TermsStatus.DEPRECATED;
	}
}
