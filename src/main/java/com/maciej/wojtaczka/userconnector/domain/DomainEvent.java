package com.maciej.wojtaczka.userconnector.domain;

import com.maciej.wojtaczka.userconnector.domain.model.Envelope;
import lombok.Value;

import java.util.Set;
import java.util.UUID;

@Value
public class DomainEvent<T> {

	String destination;
	Set<UUID> recipients;
	T payload;

	public DomainEvent(String destination, Set<UUID> recipients, T payload) {
		this.destination = destination;
		this.recipients = recipients;
		this.payload = payload;
	}

	public Envelope<T> getEnvelope() {
		return new Envelope<>(recipients, payload);
	}
}
