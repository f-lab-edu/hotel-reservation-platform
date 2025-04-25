package com.reservation.domain.terms;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.reservation.domain.base.BaseEntity;
import com.reservation.domain.terms.enums.TermsCode;
import com.reservation.domain.terms.enums.TermsStatus;
import com.reservation.domain.terms.enums.TermsType;
import com.reservation.support.exception.ErrorCode;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Builder;
import lombok.Getter;

@Getter
@Entity
@Table(
	name = "terms",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_terms_code_version", columnNames = {"code", "version"})
	}
)
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

	@Column(nullable = false)
	private Integer version; // 버전 역할

	@Column(nullable = false)
	private Boolean isLatest; // 최신 버전 여부

	@Column(nullable = false)
	private LocalDateTime exposedFrom; // 노출 시작일

	@Column(nullable = true)
	private LocalDateTime exposedToOrNull; // 노출 종료일

	@Column(nullable = false)
	private Integer displayOrder; // 정렬 순서

	@OneToMany(mappedBy = "terms", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private final List<Clause> clauses = new ArrayList<>();

	protected Terms() {
	}

	@Builder
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

	public void setClauses(List<Clause> clauses) {
		this.clauses.addAll(clauses);
	}

	public void validateComplete() {
		if (clauses.isEmpty()) {
			throw ErrorCode.CONFLICT.exception("약관은 하나 이상의 조항을 포함해야 합니다.");
		}
	}

	public void deprecate() {
		this.status = TermsStatus.DEPRECATED;
		this.isLatest = false;
	}

	public void setNewVersionAndIdInitialization(int version) {
		id = null; // 새로운 버전 생성 시 PK ID는 null 설정
		this.version = version;
	}
}
