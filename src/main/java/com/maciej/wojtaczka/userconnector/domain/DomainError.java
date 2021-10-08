package com.maciej.wojtaczka.userconnector.domain;

import lombok.Value;

import java.util.Collection;
import java.util.List;


@Value
public class DomainError {
	List<String> messages;
	Type type;


	private DomainError(List<String> messages, Type type) {
		this.messages = messages;
		this.type = type;
	}

	public static DomainError of(Collection<String> messages, Type type) {
		return new DomainError(List.copyOf(messages), type);
	}

	public static DomainError of(String message, Type type) {
		return new DomainError(List.of(message), type);
	}

	public enum Type {
		COLLISION,
		VALIDATION_FAILED,
	}


}
