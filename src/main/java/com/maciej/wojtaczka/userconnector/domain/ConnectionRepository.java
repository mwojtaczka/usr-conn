package com.maciej.wojtaczka.userconnector.domain;

import com.maciej.wojtaczka.userconnector.domain.model.Connection;
import com.maciej.wojtaczka.userconnector.domain.model.ConnectionRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ConnectionRepository {

	ConnectionRequest saveConnectionRequest(ConnectionRequest connectionRequest);

	List<ConnectionRequest> findConnectionRequestsByRecipientId(UUID recipientId);

	CompletableFuture<Optional<ConnectionRequest>> findConnectionRequest(UUID recipientId, UUID requesterId);

	void saveConnectionAndRemoveRequest(Connection connection, ConnectionRequest connectionRequest);

    List<Connection> findUserConnections(UUID connectionOwnerId);

	CompletableFuture<Optional<Connection>> findConnection(UUID connectionOwnerId, UUID connectedUserId);
}
