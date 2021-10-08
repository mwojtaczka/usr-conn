package com.maciej.wojtaczka.userconnector.rest.controller.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ConnectionRequestBody {

	private final UUID requesterId;
	private final UUID recipientId;
}
