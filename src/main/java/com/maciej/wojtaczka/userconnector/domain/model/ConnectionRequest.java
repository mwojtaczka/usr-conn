package com.maciej.wojtaczka.userconnector.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Builder
@Value
public class ConnectionRequest {

	UUID requesterId;
	UUID recipientId;
	Instant creationTime;

}


