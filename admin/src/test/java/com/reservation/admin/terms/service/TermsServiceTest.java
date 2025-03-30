package com.reservation.admin.terms.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import com.reservation.admin.terms.controller.dto.request.CreateClauseRequest;
import com.reservation.admin.terms.controller.dto.request.CreateTermsRequest;
import com.reservation.admin.terms.controller.dto.request.UpdateClauseRequest;
import com.reservation.admin.terms.controller.dto.request.UpdateTermsRequest;
import com.reservation.common.exception.BusinessException;
import com.reservation.common.terms.service.TermsCommandService;
import com.reservation.commonapi.terms.repository.AdminTermsRepository;
import com.reservation.commonmodel.terms.ClauseDto;
import com.reservation.commonmodel.terms.TermsCode;
import com.reservation.commonmodel.terms.TermsDto;
import com.reservation.commonmodel.terms.TermsStatus;
import com.reservation.commonmodel.terms.TermsType;

@ExtendWith(MockitoExtension.class)
public class TermsServiceTest {

	@Mock
	private AdminTermsRepository adminTermsRepository;

	@Mock
	private TermsCommandService termsCommandService;

	@InjectMocks
	private TermsService termsService;

	private CreateTermsRequest createRequest;
	private UpdateTermsRequest updateRequest;

	@BeforeEach
	void setUp() {
		createRequest = new CreateTermsRequest(
			TermsCode.TERMS_USE,
			"서비스 이용약관",
			TermsType.REQUIRED,
			TermsStatus.ACTIVE,
			LocalDateTime.of(2025, 3, 25, 0, 0),
			LocalDateTime.of(2026, 3, 25, 0, 0),
			1,
			List.of(
				new CreateClauseRequest(1, "제1조 (목적)", "이 약관은..."),
				new CreateClauseRequest(2, "제2조 (정의)", "여기서 사용하는 용어는...")
			)
		);

		updateRequest = new UpdateTermsRequest(
			1L,
			TermsCode.TERMS_USE,
			"서비스 이용약관",
			TermsType.REQUIRED,
			TermsStatus.ACTIVE,
			LocalDateTime.of(2025, 3, 25, 0, 0),
			LocalDateTime.of(2026, 3, 25, 0, 0),
			1,
			List.of(
				new UpdateClauseRequest(1, "제1조 (목적)", "이 약관은..."),
				new UpdateClauseRequest(2, "제2조 (정의)", "여기서 사용하는 용어는...")
			)
		);
	}

	@Test
	void 약관저장_성공ID반환() {
		when(adminTermsRepository.findMaxVersionByCode(any(TermsCode.class))).thenReturn(Optional.of(0));
		TermsDto termsDto = new TermsDto(1L,
			TermsCode.TERMS_USE,
			"서비스 이용약관",
			TermsType.REQUIRED,
			TermsStatus.ACTIVE,
			1,
			true,
			LocalDateTime.of(2025, 3, 25, 0, 0),
			LocalDateTime.of(2026, 3, 25, 0, 0),
			1,
			null,
			null,
			List.of(
				new ClauseDto(1L, 1, "제1조 (목적)", "이 약관은..."),
				new ClauseDto(2L, 2, "제2조 (이용)", "이 약관은...")
			));
		when(adminTermsRepository.save(any(TermsDto.class))).thenReturn(termsDto);

		Long id = termsService.createTerms(createRequest);

		assertThat(id).isEqualTo(1L);
	}

	@Test
	void 약관저장_실패_동일한약관존재() {
		when(adminTermsRepository.findMaxVersionByCode(any(TermsCode.class))).thenReturn(Optional.of(0));
		when(adminTermsRepository.save(any(TermsDto.class))).thenThrow(DataIntegrityViolationException.class);

		BusinessException businessException = assertThrows(BusinessException.class, () -> {
			termsService.createTerms(createRequest);
		});

		assertThat(businessException.getMessage()).isEqualTo("데이터 무결성 위반으로 인한 작업 실패, 데이터 확인 요청 필요");
	}

	@Test
	void 약관저장_실패_이미사용중인약관존재() {
		when(adminTermsRepository.existsByCodeAndStatus(any(TermsCode.class), eq(TermsStatus.ACTIVE))).thenReturn(true);

		BusinessException businessException = assertThrows(BusinessException.class, () -> {
			termsService.createTerms(createRequest);
		});

		assertThat(businessException.getMessage()).isEqualTo("이미 사용 중인 약관이 존재합니다. 기존 약관을 수정하세요.");
	}

	@Test
	void 약관수정_성공ID반환() {
		when(adminTermsRepository.findById(anyLong())).thenReturn(Optional.of(new TermsDto(1L,
			TermsCode.TERMS_USE,
			"서비스 이용약관",
			TermsType.REQUIRED,
			TermsStatus.ACTIVE,
			1,
			true,
			LocalDateTime.of(2025, 3, 25, 0, 0),
			LocalDateTime.of(2026, 3, 25, 0, 0),
			1,
			null,
			null,
			List.of(
				new ClauseDto(1L, 1, "제1조 (목적)", "이 약관은..."),
				new ClauseDto(2L, 2, "제2조 (이용)", "이 약관은...")
			))));
		when(adminTermsRepository.findMaxVersionByCode(any(TermsCode.class))).thenReturn(Optional.of(1));

		TermsDto termsDto = new TermsDto(2L,
			TermsCode.TERMS_USE,
			"서비스 이용약관",
			TermsType.REQUIRED,
			TermsStatus.ACTIVE,
			2,
			true,
			LocalDateTime.of(2025, 3, 25, 0, 0),
			LocalDateTime.of(2026, 3, 25, 0, 0),
			1,
			null,
			null,
			List.of(
				new ClauseDto(1L, 1, "제1조 (목적)", "이 약관은..."),
				new ClauseDto(2L, 2, "제2조 (이용)", "이 약관은...")
			));
		when(adminTermsRepository.save(any(TermsDto.class))).thenReturn(termsDto);

		Long id = termsService.updateTerms(updateRequest);

		assertThat(id).isEqualTo(2L);
	}

	@Test
	void 약관수정_실패_동일한약관존재() {
		when(adminTermsRepository.findById(anyLong())).thenReturn(Optional.of(new TermsDto(1L,
			TermsCode.TERMS_USE,
			"서비스 이용약관",
			TermsType.REQUIRED,
			TermsStatus.ACTIVE,
			1,
			true,
			LocalDateTime.of(2025, 3, 25, 0, 0),
			LocalDateTime.of(2026, 3, 25, 0, 0),
			1,
			null,
			null,
			List.of(
				new ClauseDto(1L, 1, "제1조 (목적)", "이 약관은..."),
				new ClauseDto(2L, 2, "제2조 (이용)", "이 약관은...")
			))));
		when(adminTermsRepository.findMaxVersionByCode(any(TermsCode.class))).thenReturn(Optional.of(1));
		when(adminTermsRepository.save(any(TermsDto.class))).thenThrow(DataIntegrityViolationException.class);

		BusinessException businessException = assertThrows(BusinessException.class, () -> {
			termsService.updateTerms(updateRequest);
		});

		assertThat(businessException.getMessage()).isEqualTo("데이터 무결성 위반으로 인한 작업 실패, 데이터 확인 요청 필요");
	}

	@Test
	void 약관수정_실패_과거버전수정() {
		when(adminTermsRepository.findById(anyLong())).thenReturn(Optional.of(new TermsDto(1L,
			TermsCode.TERMS_USE,
			"서비스 이용약관",
			TermsType.REQUIRED,
			TermsStatus.ACTIVE,
			1,
			true,
			LocalDateTime.of(2025, 3, 25, 0, 0),
			LocalDateTime.of(2026, 3, 25, 0, 0),
			1,
			null,
			null,
			List.of(
				new ClauseDto(1L, 1, "제1조 (목적)", "이 약관은..."),
				new ClauseDto(2L, 2, "제2조 (이용)", "이 약관은...")
			))));
		when(adminTermsRepository.findMaxVersionByCode(any(TermsCode.class))).thenReturn(Optional.of(2));

		BusinessException businessException = assertThrows(BusinessException.class, () -> {
			termsService.updateTerms(updateRequest);
		});

		assertThat(businessException.getMessage()).isEqualTo("과거 버전의 약관은 수정할 수 없습니다.");
	}
}
