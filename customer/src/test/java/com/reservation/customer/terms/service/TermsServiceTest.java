package com.reservation.customer.terms.service;

import static com.reservation.customer.terms.service.mapper.TermsQueryConditionMapper.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import com.reservation.commonapi.customer.query.CustomerTermsQueryCondition;
import com.reservation.commonapi.customer.query.sort.CustomerTermsSortCondition;
import com.reservation.commonapi.customer.query.sort.CustomerTermsSortField;
import com.reservation.commonapi.customer.repository.CustomerTermsRepository;
import com.reservation.commonapi.customer.repository.dto.CustomerTermsDto;
import com.reservation.commonmodel.exception.BusinessException;
import com.reservation.commonmodel.terms.ClauseDto;
import com.reservation.commonmodel.terms.TermsCode;
import com.reservation.commonmodel.terms.TermsDto;
import com.reservation.commonmodel.terms.TermsStatus;
import com.reservation.commonmodel.terms.TermsType;
import com.reservation.customer.terms.controller.dto.request.TermsSearchCondition;
import com.reservation.customer.terms.controller.dto.response.TermsDetailResponse;

@ExtendWith(MockitoExtension.class)
public class TermsServiceTest {

	@Mock
	private CustomerTermsRepository termsRepository;

	@InjectMocks
	private TermsService termsService;

	private TermsSearchCondition searchCondition;

	@BeforeEach
	void setUp() {
		searchCondition = new TermsSearchCondition(
			0,
			10,
			List.of(new CustomerTermsSortCondition(CustomerTermsSortField.DISPLAY_ORDER, Sort.Direction.DESC))
		);
	}

	@Test
	@DisplayName("약관 리스트 조회 성공")
	void findTerms_ReturnsTermsList() {
		CustomerTermsQueryCondition queryCondition = fromSearchConditionToQueryCondition(searchCondition);
		Page<CustomerTermsDto> expectedPage = mock(Page.class);

		when(termsRepository.findTermsByCondition(queryCondition)).thenReturn(expectedPage);

		Page<CustomerTermsDto> result = termsService.findTerms(searchCondition);

		assertThat(result).isEqualTo(expectedPage);
		verify(termsRepository).findTermsByCondition(queryCondition);
	}

	@Test
	@DisplayName("약관 ID로 조회 성공")
	void findById_ReturnsTermsDetailResponse() {
		Long id = 1L;
		TermsDto termsDto = new TermsDto(
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

		when(termsRepository.findById(id)).thenReturn(Optional.of(termsDto));

		TermsDetailResponse response = termsService.findById(id);

		assertThat(response).isNotNull();
		verify(termsRepository).findById(id);
	}

	@Test
	@DisplayName("약관 ID로 조회 실패 - 약관이 존재하지 않음")
	void findById_ThrowsExceptionWhenTermsNotFound() {
		Long id = 1L;

		when(termsRepository.findById(id)).thenReturn(Optional.empty());

		BusinessException exception = assertThrows(BusinessException.class, () -> {
			termsService.findById(id);
		});

		assertThat(exception.getMessage()).isEqualTo("존재하지 않는 약관입니다.");
		verify(termsRepository).findById(id);
	}

	@Test
	@DisplayName("약관 ID로 조회 실패 - 공개된 약관이 아님")
	void findById_ThrowsExceptionWhenTermsNotLatest() {
		Long id = 1L;
		TermsDto termsDto = new TermsDto(
			id,
			TermsCode.TERMS_USE,
			"서비스 이용약관",
			TermsType.REQUIRED,
			TermsStatus.ACTIVE,
			1,
			false, // isLatest가 false
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

		when(termsRepository.findById(id)).thenReturn(Optional.of(termsDto));

		BusinessException exception = assertThrows(BusinessException.class, () -> {
			termsService.findById(id);
		});

		assertThat(exception.getMessage()).isEqualTo("공개된 약관이 아닙니다.");
		verify(termsRepository).findById(id);
	}

	@Test
	@DisplayName("약관 ID로 조회 실패 - 현재 사용 중인 약관이 아님")
	void findById_ThrowsExceptionWhenTermsNotActive() {
		Long id = 1L;
		TermsDto termsDto = new TermsDto(
			id,
			TermsCode.TERMS_USE,
			"서비스 이용약관",
			TermsType.REQUIRED,
			TermsStatus.INACTIVE, // TermsStatus가 ACTIVE가 아님
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

		when(termsRepository.findById(id)).thenReturn(Optional.of(termsDto));

		BusinessException exception = assertThrows(BusinessException.class, () -> {
			termsService.findById(id);
		});

		assertThat(exception.getMessage()).isEqualTo("현재 사용 중인 약관이 아닙니다.");
		verify(termsRepository).findById(id);
	}
}
