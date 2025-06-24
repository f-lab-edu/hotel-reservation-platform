package com.reservation.host.roomtype.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.reservation.domain.roomimage.QRoomImage;
import com.reservation.domain.roomtype.QRoomType;
import com.reservation.domain.roomtype.RoomType;
import com.reservation.host.roomtype.service.dto.QSearchRoomTypeResult;
import com.reservation.host.roomtype.service.dto.SearchRoomTypeResult;
import com.reservation.querysupport.sort.SortUtils;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RoomTypeQueryRepository {
	private final JPAQueryFactory queryFactory;

	public Page<SearchRoomTypeResult> pagingByAccommodationIdAndNameOrNull(
		long accommodationId,
		String nameOrNull,
		PageRequest pageRequest
	) {
		QRoomType roomType = QRoomType.roomType;
		QRoomImage roomImage = QRoomImage.roomImage;

		BooleanBuilder builder = new BooleanBuilder();

		// 숙소 ID 필터링
		builder.and(roomType.accommodationId.eq(accommodationId));

		// 룸 이름 필터링
		if (nameOrNull != null) {
			builder.and(roomType.name.eq(nameOrNull));
		}

		// 정렬 조건 생성
		List<OrderSpecifier<?>> orders = SortUtils.getOrderSpecifiers(pageRequest.getSort(), RoomType.class, "room");

		// 데이터 조회

		List<SearchRoomTypeResult> content = queryFactory
			.select(new QSearchRoomTypeResult(
				roomType.id,
				roomType.accommodationId,
				roomType.name,
				roomType.capacity,
				roomType.price,
				roomType.descriptionOrNull,
				roomType.roomCount,
				roomImage.imageUrl
			))
			.from(roomType)
			.leftJoin(roomImage)
			.on(roomImage.roomTypeId.eq(roomType.id).and(roomImage.isMainImage.isTrue()))
			.where(builder)
			.offset(pageRequest.getOffset())
			.limit(pageRequest.getPageSize())
			.fetch();

		Long total = queryFactory
			.select(roomType.count())
			.from(roomType)
			.where(roomType.accommodationId.eq(accommodationId))
			.fetchOne();

		return new PageImpl<>(content, pageRequest, total != null ? total : 0);
	}
}
