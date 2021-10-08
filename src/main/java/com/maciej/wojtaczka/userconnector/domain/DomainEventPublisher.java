package com.maciej.wojtaczka.userconnector.domain;

public interface DomainEventPublisher {

	void publish(DomainEvent<?> domainEvent);
}
