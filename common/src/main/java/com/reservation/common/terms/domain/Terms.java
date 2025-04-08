package com.reservation.common.terms.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.reservation.common.clause.domain.Clause;
import com.reservation.common.domain.BaseEntity;
import com.reservation.commonmodel.exception.ErrorCode;
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
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;

@Entity
@Table(
	name = "terms",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_terms_code_version", columnNames = {"code", "version"})
	}
)
public class Terms extends BaseEntity {
	@Getter
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TermsCode code; // ex: TERMS001

	@Getter
	@Column(nullable = false)
	private String title; // ex: 이용약관

	@Getter
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TermsType type; // 필수 or 선택

	@Getter
	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private TermsStatus status; // 사용 or 미사용

	@Getter
	@Column(nullable = false)
	private Integer version; // 버전 역할

	@Getter
	@Column(nullable = false)
	private Boolean isLatest; // 최신 버전 여부

	@Getter
	@Column(nullable = false)
	private LocalDateTime exposedFrom; // 노출 시작일

	@Getter
	@Column(nullable = true)
	private LocalDateTime exposedToOrNull; // 노출 종료일

	@Getter
	@Column(nullable = false)
	private Integer displayOrder; // 정렬 순서

	@OneToMany(mappedBy = "terms", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private final List<Clause> clauseList = new ArrayList<>();

	@Transient
	private Clauses clauses;

	protected Terms() {
	}

	private Terms(Long id, TermsCode code, String title, TermsType type, TermsStatus status, Integer version,
		Boolean isLatest, LocalDateTime exposedFrom, LocalDateTime exposedToOrNull, Integer displayOrder) {
		if (code == null) {
			throw ErrorCode.CONFLICT.exception("약관 코드는 필수입니다.");
		}
		if (title == null || title.isBlank()) {
			throw ErrorCode.CONFLICT.exception("약관 제목은 필수입니다.");
		}
		if (type == null) {
			throw ErrorCode.CONFLICT.exception("약관 타입은 필수입니다.");
		}
		if (status == null) {
			throw ErrorCode.CONFLICT.exception("약관 상태는 필수입니다.");
		}
		if (version == null || version < 1) {
			throw ErrorCode.CONFLICT.exception("버전은 1 이상이어야 합니다.");
		}
		if (isLatest == null) {
			throw ErrorCode.CONFLICT.exception("최신 여부는 필수입니다.");
		}
		if (exposedFrom == null) {
			throw ErrorCode.CONFLICT.exception("노출 시작일은 필수입니다.");
		}
		if (displayOrder == null || displayOrder < 1) {
			throw ErrorCode.CONFLICT.exception("정렬 순서는 1 이상이어야 합니다.");
		}
		if (exposedToOrNull != null && exposedFrom.isAfter(exposedToOrNull)) {
			throw ErrorCode.CONFLICT.exception("노출 종료일은 노출 시작일보다 늦어야 합니다.");
		}
		this.id = id;
		this.code = code;
		this.title = title;
		this.type = type;
		this.status = status;
		this.version = version;
		this.isLatest = isLatest;
		this.exposedFrom = exposedFrom;
		this.exposedToOrNull = exposedToOrNull;
		this.displayOrder = displayOrder;
	}

	public static class TermsBuilder {
		private Long id;
		private TermsCode code;
		private String title;
		private TermsType type;
		private TermsStatus status;
		private Integer version;
		private Boolean isLatest;
		private LocalDateTime exposedFrom;
		private LocalDateTime exposedToOrNull;
		private Integer displayOrder;

		public TermsBuilder id(Long id) {
			this.id = id;
			return this;
		}

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

		public TermsBuilder isLatest(Boolean isLatest) {
			this.isLatest = isLatest;
			return this;
		}

		public TermsBuilder exposedFrom(LocalDateTime exposedFrom) {
			this.exposedFrom = exposedFrom;
			return this;
		}

		public TermsBuilder exposedToOrNull(LocalDateTime exposedToOrNull) {
			this.exposedToOrNull = exposedToOrNull;
			return this;
		}

		public TermsBuilder displayOrder(Integer displayOrder) {
			this.displayOrder = displayOrder;
			return this;
		}

		public Terms build() {
			return new Terms(id, code, title, type, status, version, isLatest, exposedFrom, exposedToOrNull,
				displayOrder);
		}
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
			throw ErrorCode.CONFLICT.exception("약관은 하나 이상의 조항을 포함해야 합니다.");
		}
	}

	public void deprecate() {
		this.status = TermsStatus.DEPRECATED;
		this.isLatest = false;
	}
}
