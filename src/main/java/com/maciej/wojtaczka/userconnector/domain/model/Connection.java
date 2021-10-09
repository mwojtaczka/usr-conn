package com.maciej.wojtaczka.userconnector.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Builder
@Value
public class Connection {

	UUID user1;
	UUID user2;
	Instant connectionDate;

}
