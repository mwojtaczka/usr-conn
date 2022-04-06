package com.maciej.wojtaczka.userconnector.domain.model;

import com.maciej.wojtaczka.userconnector.domain.DomainEvent;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@EqualsAndHashCode(callSuper = false)
@Builder
@Value
public class User extends DomainModel {

    UUID id;
    String nickname;
    String name;
    String surname;

    public ConnectionRequest createConnectionRequestTo(User recipient) {

		ConnectionRequest connectionRequest = ConnectionRequest.builder()
												   .requesterId(id)
												   .recipientId(recipient.id)
												   .creationTime(Instant.now())
												   .build();

		addEventToPublish(DomainEvents.connectionRequested(connectionRequest));

		return connectionRequest;
    }

	public Connection acceptRequest(ConnectionRequest connectionRequest) {

		if (!connectionRequest.getRecipientId().equals(id)) {
			throw new IllegalStateException("Recipient id does not match");
		}

		Connection newConnection = Connection.builder()
											 .user1(connectionRequest.getRecipientId())
											 .user2(connectionRequest.getRequesterId())
											 .connectionDate(Instant.now())
											 .build();

		addEventToPublish(DomainEvents.connectionCreated(newConnection, connectionRequest.getRequesterId()));

		return newConnection;
	}

	public static class DomainEvents {
		public static final String CONNECTION_REQUESTED = "connection-requested";
		public static final String CONNECTION_CREATED = "connection-created";

		static DomainEvent<ConnectionRequest> connectionRequested(ConnectionRequest connectionRequest) {
			return new DomainEvent<>(CONNECTION_REQUESTED, Set.of(connectionRequest.getRecipientId()), connectionRequest);
		}

		static DomainEvent<Connection> connectionCreated(Connection connection, UUID requesterId) {
			return new DomainEvent<>(CONNECTION_CREATED, Set.of(requesterId), connection);
		}

	}
}
