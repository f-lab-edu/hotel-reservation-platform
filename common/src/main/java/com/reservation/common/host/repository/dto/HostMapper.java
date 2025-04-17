package com.reservation.common.host.repository.dto;

import com.reservation.common.host.domain.Host;
import com.reservation.commonmodel.host.HostDto;

public class HostMapper {
	public static Host fromDtoToEntity(HostDto hostDto) {
		return new Host(hostDto.id(), hostDto.email(), hostDto.password(), hostDto.status());
	}

	public static HostDto fromEntityToDto(Host host) {
		return new HostDto(host.getId(), host.getEmail(), host.getStatus(), host.getPassword());
	}
}
