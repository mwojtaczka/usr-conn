package com.maciej.wojtaczka.userconnector.domain;

import com.maciej.wojtaczka.userconnector.domain.model.Connection;
import com.maciej.wojtaczka.userconnector.domain.model.ConnectionRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConnectionRepository {

	ConnectionRequest saveConnectionRequest(ConnectionRequest connectionRequest);

	List<ConnectionRequest> findConnectionRequestByRecipientId(UUID recipientId);

	Optional<ConnectionRequest> findConnectionRequest(UUID recipientId, UUID requesterId);

	void saveConnectionAndRemoveRequest(Connection connection, ConnectionRequest connectionRequest);

    List<Connection> findConnections(UUID connectionOwnerId);
}
