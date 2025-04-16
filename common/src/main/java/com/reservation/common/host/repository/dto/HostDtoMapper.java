package com.reservation.common.host.repository.dto;

import com.reservation.common.host.domain.Host;
import com.reservation.commonmodel.host.HostDto;

public class HostDtoMapper {
	public static Host toHost(HostDto hostDto) {
		return new Host(hostDto.id(), hostDto.email(), hostDto.password(), hostDto.status());
	}

	public static HostDto fromHost(Host host) {
		return new HostDto(host.getId(), host.getEmail(), host.getStatus(), host.getPassword());
	}
}
