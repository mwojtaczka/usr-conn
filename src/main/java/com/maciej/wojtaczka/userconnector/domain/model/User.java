package com.maciej.wojtaczka.userconnector.domain.model;

import com.maciej.wojtaczka.userconnector.domain.DomainEvent;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.time.Instant;
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

	public static class DomainEvents {
		public static final String CONNECTION_REQUESTED = "connection-requested";

		static DomainEvent<ConnectionRequest> connectionRequested(ConnectionRequest connectionRequest) {
			return new DomainEvent<>(CONNECTION_REQUESTED, connectionRequest);
		}

	}
}
