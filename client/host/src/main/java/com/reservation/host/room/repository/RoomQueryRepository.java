package com.reservation.host.room.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.reservation.domain.room.QRoom;
import com.reservation.domain.room.Room;
import com.reservation.domain.roomimage.QRoomImage;
import com.reservation.host.room.service.dto.QSearchRoomResult;
import com.reservation.host.room.service.dto.SearchRoomResult;
import com.reservation.querysupport.sort.SortUtils;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RoomQueryRepository {
	private final JPAQueryFactory queryFactory;

	public Page<SearchRoomResult> pagingByAccommodationIdAndNameOrNull(
		long accommodationId,
		String nameOrNull,
		PageRequest pageRequest
	) {
		QRoom room = QRoom.room;
		QRoomImage roomImage = QRoomImage.roomImage;

		BooleanBuilder builder = new BooleanBuilder();

		// 숙소 ID 필터링
		builder.and(room.accommodationId.eq(accommodationId));

		// 룸 이름 필터링
		if (nameOrNull != null) {
			builder.and(room.name.eq(nameOrNull));
		}

		// 정렬 조건 생성
		List<OrderSpecifier<?>> orders = SortUtils.getOrderSpecifiers(pageRequest.getSort(), Room.class, "room");

		// 데이터 조회

		List<SearchRoomResult> content = queryFactory
			.select(new QSearchRoomResult(
				room.id,
				room.accommodationId,
				room.name,
				room.capacity,
				room.price,
				room.descriptionOrNull,
				room.roomCount,
				roomImage.imageUrl
			))
			.from(room)
			.leftJoin(roomImage)
			.on(roomImage.roomTypeId.eq(room.id).and(roomImage.isMainImage.isTrue()))
			.where(builder)
			.offset(pageRequest.getOffset())
			.limit(pageRequest.getPageSize())
			.fetch();

		Long total = queryFactory
			.select(room.count())
			.from(room)
			.where(room.accommodationId.eq(accommodationId))
			.fetchOne();

		return new PageImpl<>(content, pageRequest, total != null ? total : 0);
	}
}
