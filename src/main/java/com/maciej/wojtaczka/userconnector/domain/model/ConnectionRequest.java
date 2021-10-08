package com.maciej.wojtaczka.userconnector.domain.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Builder
@Value
@EqualsAndHashCode(callSuper = false)
public class ConnectionRequest {

	UUID requesterId;
	UUID recipientId;
	Instant creationTime;

}


