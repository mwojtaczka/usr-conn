package com.maciej.wojtaczka.userconnector.domain;

import java.util.UUID;

public class ConnectionException extends RuntimeException {

	public ConnectionException(String message) {
		super(message);
	}

	static ConnectionException connectionRequestNotFound(UUID recipientId, UUID requesterId) {
		return new ConnectionException(
				String.format("Connection request for recipient: %s from requester: %s, not found", recipientId, requesterId));
	}
}
