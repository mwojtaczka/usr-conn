package com.maciej.wojtaczka.userconnector.domain.model;

import lombok.Value;

import java.util.Set;
import java.util.UUID;

@Value
public class Envelope <T> {

	Set<UUID> recipients;
	T payload;
}
