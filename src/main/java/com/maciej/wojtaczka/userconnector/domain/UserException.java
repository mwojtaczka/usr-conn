package com.maciej.wojtaczka.userconnector.domain;

import java.util.UUID;

public class UserException extends RuntimeException {

	public UserException(String message) {
		super(message);
	}

	static UserException userNotFound(UUID userId) {
		return new UserException(String.format("User with id: %s not found", userId));
	}
}
