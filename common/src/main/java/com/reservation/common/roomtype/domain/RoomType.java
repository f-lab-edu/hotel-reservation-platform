package com.reservation.common.roomtype.domain;

import java.util.List;

import com.reservation.common.domain.BaseEntity;

import jakarta.persistence.Entity;

@Entity
class RoomType extends BaseEntity {
	private Long accommodationId;
	private String name;
	private int capacity;
	private int price;
	private String description;
	private List<String> imageUrls;
	private Integer roomCount; // 이 방 타입에 몇 개의 방이 존재하는지
}
