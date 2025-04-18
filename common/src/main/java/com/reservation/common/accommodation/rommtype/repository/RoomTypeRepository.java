package com.reservation.common.accommodation.rommtype.repository;

import static com.reservation.common.accommodation.rommtype.repository.mapper.RoomTypeMapper.*;
import static com.reservation.common.support.sort.SortUtils.*;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.reservation.common.accommodation.rommtype.domain.QRoomType;
import com.reservation.common.accommodation.rommtype.domain.RoomType;
import com.reservation.common.accommodation.rommtype.repository.mapper.RoomTypeMapper;
import com.reservation.common.accommodation.roomimage.domain.QRoomImage;
import com.reservation.commonapi.host.query.HostRoomTypeQueryCondition;
import com.reservation.commonapi.host.repository.HostRoomTypeRepository;
import com.reservation.commonapi.host.repository.dto.HostRoomTypeDto;
import com.reservation.commonapi.host.repository.dto.QHostRoomTypeDto;
import com.reservation.commonmodel.accommodation.RoomTypeDto;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class RoomTypeRepository implements HostRoomTypeRepository {
	private final JpaRoomTypeRepository jpaRoomTypeRepository;
	private final JPAQueryFactory queryFactory;

	@Override
	public boolean existsByNameAndAccommodationId(String name, Long accommodationId) {
		return jpaRoomTypeRepository.existsByNameAndAccommodationId(name, accommodationId);
	}

	@Override
	public RoomTypeDto save(RoomTypeDto roomTypeDto) {
		RoomType entity = fromDtoToEntity(roomTypeDto);
		return fromEntityToDto(jpaRoomTypeRepository.save(entity));
	}

	@Override
	public boolean existsByIdAndAccommodationId(Long id, Long accommodationId) {
		return jpaRoomTypeRepository.existsByIdAndAccommodationId(id, accommodationId);
	}

	@Override
	public Optional<RoomTypeDto> findOneByNameAndAccommodationId(String name, Long accommodationId) {
		return jpaRoomTypeRepository.findOneByNameAndAccommodationId(name, accommodationId)
			.map(RoomTypeMapper::fromEntityToDto);
	}

	@Override
	public Page<HostRoomTypeDto> findRoomTypes(Long accommodationId, HostRoomTypeQueryCondition condition) {
		QRoomType roomType = QRoomType.roomType;
		QRoomImage roomImage = QRoomImage.roomImage;

		BooleanBuilder builder = new BooleanBuilder();

		// 숙소 ID 필터링
		builder.and(roomType.accommodationId.eq(accommodationId));

		// 룸 타입 명 필터링
		if (condition.name() != null) {
			builder.and(roomType.name.eq(condition.name()));
		}

		Pageable pageable = condition.pageRequest();
		// 정렬 조건 생성
		List<OrderSpecifier<?>> orders = getOrderSpecifiers(condition.pageRequest().getSort(), RoomType.class,
			"roomType");

		// 데이터 조회

		List<HostRoomTypeDto> content = queryFactory
			.select(new QHostRoomTypeDto(
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
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		Long total = queryFactory
			.select(roomType.count())
			.from(roomType)
			.where(roomType.accommodationId.eq(accommodationId))
			.fetchOne();

		return new PageImpl<>(content, pageable, total != null ? total : 0);
	}

	@Override
	public Optional<RoomTypeDto> findOneByIdAndAccommodationId(Long id, Long accommodationId) {
		return jpaRoomTypeRepository.findOneByIdAndAccommodationId(id, accommodationId)
			.map(RoomTypeMapper::fromEntityToDto);
	}
}
