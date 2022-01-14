package com.maciej.wojtaczka.userconnector.domain;

import java.util.UUID;

public class ConnectionException extends RuntimeException {

	private final Type type;

	public ConnectionException(String message, Type type) {
		super(message);
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	static ConnectionException connectionRequestNotFound(UUID recipientId, UUID requesterId) {
		return new ConnectionException(
				String.format("Connection request for recipient: %s from requester: %s, not found", recipientId, requesterId), Type.NOT_FOUND);
	}

	static ConnectionException cannotSendConnectionRequest(UUID recipientId, UUID requesterId, String reason) {
		return new ConnectionException(
				String.format("Cannot send connection request from %s to %s because %s", recipientId, requesterId, reason), Type.ILLEGAL_STATE);
	}

	public enum Type {
		NOT_FOUND, ILLEGAL_STATE
	}

}
