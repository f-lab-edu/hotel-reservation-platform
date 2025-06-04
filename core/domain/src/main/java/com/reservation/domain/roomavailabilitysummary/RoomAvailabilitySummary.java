package com.reservation.domain.roomavailabilitysummary;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.reservation.domain.base.BaseEntity;
import com.reservation.support.exception.ErrorCode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Transient;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class RoomAvailabilitySummary extends BaseEntity {
	@Column(nullable = false)
	private long roomTypeId; // 룸 타입 ID

	@Column(nullable = false)
	private LocalDate checkInDate; // 예약 가능 날짜

	@Column(nullable = true)
	private Integer availableCount1;

	@Column(nullable = true)
	private Integer availableCount2;

	@Column(nullable = true)
	private Integer availableCount3;

	@Column(nullable = true)
	private Integer availableCount4;

	@Column(nullable = true)
	private Integer availableCount5;

	@Column(nullable = true)
	private Integer availableCount6;

	@Column(nullable = true)
	private Integer availableCount7;

	@Column(nullable = true)
	private Integer availableCount8;

	@Column(nullable = true)
	private Integer availableCount9;

	@Column(nullable = true)
	private Integer availableCount10;

	@Column(nullable = true)
	private Integer availableCount11;

	@Column(nullable = true)
	private Integer availableCount12;

	@Column(nullable = true)
	private Integer availableCount13;

	@Column(nullable = true)
	private Integer availableCount14;

	@Column(nullable = true)
	private Integer availableCount15;

	@Column(nullable = true)
	private Integer availableCount16;

	@Column(nullable = true)
	private Integer availableCount17;

	@Column(nullable = true)
	private Integer availableCount18;

	@Column(nullable = true)
	private Integer availableCount19;

	@Column(nullable = true)
	private Integer availableCount20;

	@Column(nullable = true)
	private Integer availableCount21;

	@Column(nullable = true)
	private Integer availableCount22;

	@Column(nullable = true)
	private Integer availableCount23;

	@Column(nullable = true)
	private Integer availableCount24;

	@Column(nullable = true)
	private Integer availableCount25;

	@Column(nullable = true)
	private Integer availableCount26;

	@Column(nullable = true)
	private Integer availableCount27;

	@Column(nullable = true)
	private Integer availableCount28;

	@Column(nullable = true)
	private Integer availableCount29;

	@Column(nullable = true)
	private Integer availableCount30;

	@Column(nullable = true)
	private Integer totalPrice1;

	@Column(nullable = true)
	private Integer totalPrice2;

	@Column(nullable = true)
	private Integer totalPrice3;

	@Column(nullable = true)
	private Integer totalPrice4;

	@Column(nullable = true)
	private Integer totalPrice5;

	@Column(nullable = true)
	private Integer totalPrice6;

	@Column(nullable = true)
	private Integer totalPrice7;

	@Column(nullable = true)
	private Integer totalPrice8;

	@Column(nullable = true)
	private Integer totalPrice9;

	@Column(nullable = true)
	private Integer totalPrice10;

	@Column(nullable = true)
	private Integer totalPrice11;

	@Column(nullable = true)
	private Integer totalPrice12;

	@Column(nullable = true)
	private Integer totalPrice13;

	@Column(nullable = true)
	private Integer totalPrice14;

	@Column(nullable = true)
	private Integer totalPrice15;

	@Column(nullable = true)
	private Integer totalPrice16;

	@Column(nullable = true)
	private Integer totalPrice17;

	@Column(nullable = true)
	private Integer totalPrice18;

	@Column(nullable = true)
	private Integer totalPrice19;

	@Column(nullable = true)
	private Integer totalPrice20;

	@Column(nullable = true)
	private Integer totalPrice21;

	@Column(nullable = true)
	private Integer totalPrice22;

	@Column(nullable = true)
	private Integer totalPrice23;

	@Column(nullable = true)
	private Integer totalPrice24;

	@Column(nullable = true)
	private Integer totalPrice25;

	@Column(nullable = true)
	private Integer totalPrice26;

	@Column(nullable = true)
	private Integer totalPrice27;

	@Column(nullable = true)
	private Integer totalPrice28;

	@Column(nullable = true)
	private Integer totalPrice29;

	@Column(nullable = true)
	private Integer totalPrice30;

	@Transient
	private List<StayStat> stayStats = new ArrayList<>(30);

	@Builder
	public RoomAvailabilitySummary(
		long roomTypeId,
		LocalDate checkInDate,
		List<Integer> availableCounts,
		List<Integer> totalPrices
	) {
		if (roomTypeId <= 0) {
			throw ErrorCode.BAD_REQUEST.exception("룸 ID는 0보다 커야 합니다.");
		}
		if (checkInDate == null) {
			throw ErrorCode.BAD_REQUEST.exception("예약 가능 날짜는 필수입니다.");
		}
		if (availableCounts == null || availableCounts.size() != 30) {
			throw ErrorCode.BAD_REQUEST.exception("예약 가능 수량은 30개 모두 필요합니다.");
		}
		if (totalPrices == null || totalPrices.size() != 30) {
			throw ErrorCode.BAD_REQUEST.exception("평균 가격은 30개 모두 필요합니다.");
		}

		this.roomTypeId = roomTypeId;
		this.checkInDate = checkInDate;

		this.availableCount1 = availableCounts.get(0);
		this.availableCount2 = availableCounts.get(1);
		this.availableCount3 = availableCounts.get(2);
		this.availableCount4 = availableCounts.get(3);
		this.availableCount5 = availableCounts.get(4);
		this.availableCount6 = availableCounts.get(5);
		this.availableCount7 = availableCounts.get(6);
		this.availableCount8 = availableCounts.get(7);
		this.availableCount9 = availableCounts.get(8);
		this.availableCount10 = availableCounts.get(9);
		this.availableCount11 = availableCounts.get(10);
		this.availableCount12 = availableCounts.get(11);
		this.availableCount13 = availableCounts.get(12);
		this.availableCount14 = availableCounts.get(13);
		this.availableCount15 = availableCounts.get(14);
		this.availableCount16 = availableCounts.get(15);
		this.availableCount17 = availableCounts.get(16);
		this.availableCount18 = availableCounts.get(17);
		this.availableCount19 = availableCounts.get(18);
		this.availableCount20 = availableCounts.get(19);
		this.availableCount21 = availableCounts.get(20);
		this.availableCount22 = availableCounts.get(21);
		this.availableCount23 = availableCounts.get(22);
		this.availableCount24 = availableCounts.get(23);
		this.availableCount25 = availableCounts.get(24);
		this.availableCount26 = availableCounts.get(25);
		this.availableCount27 = availableCounts.get(26);
		this.availableCount28 = availableCounts.get(27);
		this.availableCount29 = availableCounts.get(28);
		this.availableCount30 = availableCounts.get(29);

		this.totalPrice1 = totalPrices.get(0);
		this.totalPrice2 = totalPrices.get(1);
		this.totalPrice3 = totalPrices.get(2);
		this.totalPrice4 = totalPrices.get(3);
		this.totalPrice5 = totalPrices.get(4);
		this.totalPrice6 = totalPrices.get(5);
		this.totalPrice7 = totalPrices.get(6);
		this.totalPrice8 = totalPrices.get(7);
		this.totalPrice9 = totalPrices.get(8);
		this.totalPrice10 = totalPrices.get(9);
		this.totalPrice11 = totalPrices.get(10);
		this.totalPrice12 = totalPrices.get(11);
		this.totalPrice13 = totalPrices.get(12);
		this.totalPrice14 = totalPrices.get(13);
		this.totalPrice15 = totalPrices.get(14);
		this.totalPrice16 = totalPrices.get(15);
		this.totalPrice17 = totalPrices.get(16);
		this.totalPrice18 = totalPrices.get(17);
		this.totalPrice19 = totalPrices.get(18);
		this.totalPrice20 = totalPrices.get(19);
		this.totalPrice21 = totalPrices.get(20);
		this.totalPrice22 = totalPrices.get(21);
		this.totalPrice23 = totalPrices.get(22);
		this.totalPrice24 = totalPrices.get(23);
		this.totalPrice25 = totalPrices.get(24);
		this.totalPrice26 = totalPrices.get(25);
		this.totalPrice27 = totalPrices.get(26);
		this.totalPrice28 = totalPrices.get(27);
		this.totalPrice29 = totalPrices.get(28);
		this.totalPrice30 = totalPrices.get(29);
	}

	@PostLoad
	private void postLoad() {
		for (int i = 1; i <= 30; i++) {
			Integer count = (Integer)getFieldValue("availableCount" + i);
			Integer price = (Integer)getFieldValue("totalPrice" + i);
			stayStats.add(new StayStat(
				Optional.ofNullable(count).orElse(0),
				Optional.ofNullable(price).orElse(0)
			));
		}
	}

	@PrePersist
	@PreUpdate
	private void prePersist() {
		for (int i = 1; i <= 30; i++) {
			StayStat stat = stayStats.get(i - 1);
			setFieldValue("availableCount" + i, stat.availableCount());
			setFieldValue("totalPrice" + i, stat.totalPrice());
		}
	}

	// 리플렉션 유틸
	private Object getFieldValue(String name) {
		try {
			Field field = this.getClass().getDeclaredField(name);
			field.setAccessible(true);
			return field.get(this);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private void setFieldValue(String name, Object value) {
		try {
			Field field = this.getClass().getDeclaredField(name);
			field.setAccessible(true);
			field.set(this, value);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
}
