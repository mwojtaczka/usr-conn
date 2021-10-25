package com.maciej.wojtaczka.userconnector.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.maciej.wojtaczka.userconnector.domain.model.User;
import com.maciej.wojtaczka.userconnector.persistence.ConnectionRequestCassandraRepository;
import com.maciej.wojtaczka.userconnector.persistence.entity.ConnectionRequestEntity;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.maciej.wojtaczka.userconnector.rest.client.UserServiceRestClient.GET_USERS_BY_IDS;

@Component
public class UserFixtures {

	private final WireMockServer wireMockServer;
	private final Set<User> existingUsers = new HashSet<>();
	private final ObjectMapper objectMapper;
	private final ConnectionRequestCassandraRepository repository;

	UserFixtures(WireMockServer wireMockServer, ObjectMapper objectMapper,
				 ConnectionRequestCassandraRepository repository) {
		this.wireMockServer = wireMockServer;
		this.objectMapper = objectMapper;
		this.repository = repository;
	}

	public UserBuilder user() {
		return new UserBuilder();
	}

	public class UserBuilder {

		private final User.UserBuilder userBuilder;
		private final Set<ConnectionRequestBuilder> pendingRequestsForUser = new HashSet<>();

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

			List<ConnectionRequestEntity> connectionRequests = pendingRequestsForUser
					.stream()
					.map(connReq -> connReq.build(user.getId()))
					.collect(Collectors.toList());
			repository.saveAll(connectionRequests);

		}

		private User build() {
			return userBuilder.build();
		}
	}

	public static class ConnectionRequestBuilder {
		private final ConnectionRequestEntity.ConnectionRequestEntityBuilder connectionRequestBuilder;
		private final UserBuilder userBuilder;

		public ConnectionRequestBuilder(UserBuilder userBuilder) {
			this.userBuilder = userBuilder;
			this.connectionRequestBuilder = ConnectionRequestEntity.builder()
																   .requesterId(UUID.randomUUID())
																   .creationTime(Instant.now());
		}

		public ConnectionRequestBuilder fromRequester(UUID requesterId) {
			connectionRequestBuilder.requesterId(requesterId);
			return this;
		}

		public ConnectionRequestBuilder atTime(Instant time) {
			connectionRequestBuilder.creationTime(time);
			return this;
		}

		public UserBuilder andThisUser() {
			userBuilder.pendingRequestsForUser.add(this);
			return userBuilder;
		}

		private ConnectionRequestEntity build(UUID recipientId) {
			return connectionRequestBuilder
					.recipientId(recipientId)
					.build();
		}
	}

}
