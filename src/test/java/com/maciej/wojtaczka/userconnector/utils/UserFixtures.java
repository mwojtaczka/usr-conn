package com.maciej.wojtaczka.userconnector.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.maciej.wojtaczka.userconnector.domain.model.Connection;
import com.maciej.wojtaczka.userconnector.domain.model.ConnectionRequest;
import com.maciej.wojtaczka.userconnector.domain.model.User;
import com.maciej.wojtaczka.userconnector.persistence.ConnectionRequestModelToDbEntityMapper;
import com.maciej.wojtaczka.userconnector.persistence.entity.ConnectionEntity;
import lombok.SneakyThrows;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.maciej.wojtaczka.userconnector.rest.client.UserServiceRestClient.GET_USERS_BY_IDS;

@Component
public class UserFixtures {

	private final WireMockServer wireMockServer;
	private final Set<User> existingUsers = new HashSet<>();
	private final ObjectMapper objectMapper;
	private final ConnectionRequestModelToDbEntityMapper connectionRequestMapper;
	private final CassandraOperations cassandraOperations;

	UserFixtures(WireMockServer wireMockServer,
				 ObjectMapper objectMapper,
				 ConnectionRequestModelToDbEntityMapper connectionRequestMapper,
				 CassandraOperations cassandraOperations) {
		this.wireMockServer = wireMockServer;
		this.objectMapper = objectMapper;
		this.connectionRequestMapper = connectionRequestMapper;
		this.cassandraOperations = cassandraOperations;
	}

	public UserBuilder givenUser() {
		return new UserBuilder();
	}

	public class UserBuilder {

		private final User.UserBuilder userBuilder;
		private final Set<ConnectionRequestBuilder> pendingRequestsForUser = new HashSet<>();
		private final Set<UserConnectionBuilder> userConnections = new HashSet<>();

		public UserBuilder() {
			userBuilder = User.builder()
							  .id(UUID.randomUUID())
							  .name("DefaultName")
							  .surname("DefaultSurname")
							  .nickname("DefaultNickname");
		}

		public UserBuilder withId(UUID id) {
			userBuilder.id(id);
			return this;
		}

		public ConnectionRequestBuilder withPendingConnectionRequest() {
			return new ConnectionRequestBuilder(this);
		}

		public UserConnectionBuilder connected() {
			return new UserConnectionBuilder(this);
		}

		@SneakyThrows
		public void exists() {
			User user = build();
			existingUsers.add(user);

			String jsonResponseBody = objectMapper.writeValueAsString(existingUsers);

			ResponseDefinitionBuilder response = WireMock.aResponse()
														 .withHeader("Content-type", "application/json")
														 .withBody(jsonResponseBody);
			wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(GET_USERS_BY_IDS))
										   .willReturn(response));

			pendingRequestsForUser
					.stream()
					.map(connReq -> connReq.build(user.getId()))
					.map(connectionRequestMapper::toDbEntity)
					.forEach(cassandraOperations::insert);

			userConnections
					.stream()
					.map(builder -> builder.build(user.getId()))
					.map(ConnectionEntity::from)
					.forEach(cassandraOperations::insert);

		}

		private User build() {
			return userBuilder.build();
		}
	}

	public static class ConnectionRequestBuilder {
		private final ConnectionRequest.ConnectionRequestBuilder connectionRequestBuilder;
		private final UserBuilder userBuilder;

		public ConnectionRequestBuilder(UserBuilder userBuilder) {
			this.userBuilder = userBuilder;
			this.connectionRequestBuilder = ConnectionRequest.builder()
															 .requesterId(UUID.randomUUID())
															 .creationTime(Instant.now());
			userBuilder.pendingRequestsForUser.add(this);
		}

		public ConnectionRequestBuilder fromRequester(UUID requesterId) {
			connectionRequestBuilder.requesterId(requesterId);
			return this;
		}

		public ConnectionRequestBuilder atTime(Instant time) {
			connectionRequestBuilder.creationTime(time);
			return this;
		}

		public ConnectionRequestBuilder andAlso() {
			return new ConnectionRequestBuilder(userBuilder);
		}

		public UserBuilder andTheGivenUser() {
			return userBuilder;
		}

		private ConnectionRequest build(UUID recipientId) {
			return connectionRequestBuilder
					.recipientId(recipientId)
					.build();
		}
	}

	public static class UserConnectionBuilder {
		private final Connection.ConnectionBuilder builder;
		private final UserBuilder userBuilder;

		public UserConnectionBuilder(UserBuilder userBuilder) {
			this.userBuilder = userBuilder;
			userBuilder.userConnections.add(this);

			builder = Connection.builder()
								.user2(UUID.randomUUID())
								.connectionDate(Instant.now());
		}

		public UserConnectionBuilder withUser(UUID userId) {
			builder.user2(userId);
			return this;
		}

		public UserConnectionBuilder atTime(Instant time) {
			builder.connectionDate(time);
			return this;
		}

		public UserConnectionBuilder andAlso() {
			return new UserConnectionBuilder(userBuilder);
		}

		public UserBuilder andTheGivenUser() {
			return userBuilder;
		}

		private Connection build(UUID ownerId) {
			return builder.user1(ownerId).build();
		}
	}


}
