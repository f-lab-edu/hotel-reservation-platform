package com.reservation.admin.terms.service;

import static com.reservation.admin.terms.service.mapper.AdminTermsQueryConditionMapper.*;
import static com.reservation.admin.terms.service.mapper.AdminTermsQueryKeysetConditionMapper.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import com.reservation.admin.terms.controller.dto.request.CreateClauseRequest;
import com.reservation.admin.terms.controller.dto.request.CreateTermsRequest;
import com.reservation.admin.terms.controller.dto.request.TermsKeysetSearchCondition;
import com.reservation.admin.terms.controller.dto.request.TermsSearchCondition;
import com.reservation.admin.terms.controller.dto.request.UpdateClauseRequest;
import com.reservation.admin.terms.controller.dto.request.UpdateTermsRequest;
import com.reservation.common.terms.service.TermsCommandService;
import com.reservation.commonapi.admin.query.AdminTermsKeysetQueryCondition;
import com.reservation.commonapi.admin.query.AdminTermsQueryCondition;
import com.reservation.commonapi.admin.query.sort.AdminTermsSortCondition;
import com.reservation.commonapi.admin.query.sort.AdminTermsSortCursor;
import com.reservation.commonapi.admin.query.sort.AdminTermsSortField;
import com.reservation.commonapi.admin.repository.AdminTermsRepository;
import com.reservation.commonapi.admin.repository.dto.AdminTermsDto;
import com.reservation.commonmodel.exception.BusinessException;
import com.reservation.commonmodel.keyset.KeysetPage;
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

	@Test
	void 약관_리스트_조회_성공() {
		TermsSearchCondition condition = new TermsSearchCondition(
			TermsCode.TERMS_USE,
			false,
			0,
			10,
			List.of(new AdminTermsSortCondition(AdminTermsSortField.CREATED_AT, Sort.Direction.DESC))
		);

		AdminTermsQueryCondition queryCondition = fromSearchConditionToQueryCondition(condition);
		Page<AdminTermsDto> expectedPage = mock(Page.class);

		when(adminTermsRepository.findTermsByCondition(queryCondition)).thenReturn(expectedPage);

		Page<AdminTermsDto> result = termsService.findTerms(condition);

		assertThat(result).isEqualTo(expectedPage);
		verify(adminTermsRepository).findTermsByCondition(queryCondition);
	}

	@Test
	@DisplayName("약관 키셋 리스트 조회 성공")
	void findTermsByKeyset_ReturnsTermsList() {
		TermsKeysetSearchCondition condition = new TermsKeysetSearchCondition(
			TermsCode.TERMS_USE,
			false,
			10,
			List.of(new AdminTermsSortCursor(AdminTermsSortField.CREATED_AT, Sort.Direction.DESC, null))
		);

		AdminTermsKeysetQueryCondition queryCondition = fromSearchConditionToQueryKeysetCondition(condition);
		KeysetPage<AdminTermsDto, AdminTermsSortCursor> expectedPage = mock(KeysetPage.class);

		when(adminTermsRepository.findTermsByKeysetCondition(queryCondition)).thenReturn(expectedPage);

		KeysetPage<AdminTermsDto, AdminTermsSortCursor> result = termsService.findTermsByKeyset(condition);

		assertThat(result).isEqualTo(expectedPage);
		verify(adminTermsRepository).findTermsByKeysetCondition(queryCondition);
	}

	@Test
	@DisplayName("약관 ID로 조회 성공")
	void findById_ReturnsTerms() {
		Long id = 1L;
		TermsDto expectedTerms = new TermsDto(
			id,
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
			)
		);

		when(adminTermsRepository.findWithClausesById(id)).thenReturn(Optional.of(expectedTerms));

		TermsDto result = termsService.findById(id);

		assertThat(result).isEqualTo(expectedTerms);
		verify(adminTermsRepository).findWithClausesById(id);
	}

	@Test
	@DisplayName("약관 ID로 조회 실패 - 약관이 존재하지 않음")
	void findById_ThrowsExceptionWhenTermsNotFound() {
		Long id = 1L;

		when(adminTermsRepository.findWithClausesById(id)).thenReturn(Optional.empty());

		BusinessException exception = assertThrows(BusinessException.class, () -> {
			termsService.findById(id);
		});

		assertThat(exception.getMessage()).isEqualTo("존재하지 않는 약관입니다.");
		verify(adminTermsRepository).findWithClausesById(id);
	}
}
