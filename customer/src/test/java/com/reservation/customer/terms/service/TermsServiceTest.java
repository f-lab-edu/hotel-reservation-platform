package com.reservation.customer.terms.service;

import static com.reservation.customer.terms.service.mapper.CustomerTermsQueryConditionMapper.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

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
import com.reservation.customer.terms.controller.dto.request.TermsSearchCondition;

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
}
