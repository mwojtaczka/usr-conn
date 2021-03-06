package com.maciej.wojtaczka.userconnector.domain.model;

import com.maciej.wojtaczka.userconnector.domain.DomainEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

	@Test
	void shouldCreateConnectionRequest() {
		//given
		UUID recipientId = UUID.randomUUID();
		UUID requesterId = UUID.randomUUID();
		User recipient = User.builder()
							 .id(recipientId)
							 .build();
		User requester = User.builder()
							 .id(requesterId)
							 .build();

		//when
		ConnectionRequest connectionRequest = requester.createConnectionRequestTo(recipient);

		//then
		Assertions.assertAll(
				() -> assertThat(connectionRequest.getRecipientId()).isEqualTo(recipientId),
				() -> assertThat(connectionRequest.getRequesterId()).isEqualTo(requesterId),
				() -> assertThat(connectionRequest.getCreationTime()).isNotNull()
		);
	}

	@Test
	void shouldCreateConnectionRequestedEvent() {
		//given
		UUID recipientId = UUID.randomUUID();
		UUID requesterId = UUID.randomUUID();
		User recipient = User.builder()
							 .id(recipientId)
							 .build();
		User requester = User.builder()
							 .id(requesterId)
							 .build();

		//when
		ConnectionRequest connectionRequest = requester.createConnectionRequestTo(recipient);

		//then
		List<DomainEvent<?>> domainEvents = requester.getDomainEvents();
		Assertions.assertAll(
				() -> assertThat(domainEvents).hasSize(1),
				() -> assertThat(domainEvents.get(0).getDestination()).isEqualTo("connection-requested"),
				() -> assertThat(domainEvents.get(0).getPayload()).isEqualTo(connectionRequest)
		);
	}

	@Test
	void shouldAcceptConnectionRequest() {
		//given
		UUID recipientId = UUID.randomUUID();
		UUID requesterId = UUID.randomUUID();
		User recipient = User.builder()
							 .id(recipientId)
							 .build();
		ConnectionRequest connectionRequest = ConnectionRequest.builder()
															   .recipientId(recipientId)
															   .requesterId(requesterId)
															   .build();

		//when
		Connection connection = recipient.acceptRequest(connectionRequest);

		//then
		Assertions.assertAll(
				() -> assertThat(connection.getUser1()).isEqualTo(recipientId),
				() -> assertThat(connection.getUser2()).isEqualTo(requesterId),
				() -> assertThat(connection.getConnectionDate()).isNotNull()
		);
	}

	@Test
	void shouldCreateConnectionCreatedEvent() {
		//given
		UUID recipientId = UUID.randomUUID();
		UUID requesterId = UUID.randomUUID();
		User recipient = User.builder()
							 .id(recipientId)
							 .build();
		ConnectionRequest connectionRequest = ConnectionRequest.builder()
															   .recipientId(recipientId)
															   .requesterId(requesterId)
															   .build();

		//when
		Connection connection = recipient.acceptRequest(connectionRequest);

		//then
		List<DomainEvent<?>> domainEvents = recipient.getDomainEvents();
		Assertions.assertAll(
				() -> assertThat(domainEvents).hasSize(1),
				() -> assertThat(domainEvents.get(0).getDestination()).isEqualTo("connection-created"),
				() -> assertThat(domainEvents.get(0).getPayload()).isEqualTo(connection)
		);
	}

}
