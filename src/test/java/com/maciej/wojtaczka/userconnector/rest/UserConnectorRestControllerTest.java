package com.maciej.wojtaczka.userconnector.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.maciej.wojtaczka.userconnector.config.CassandraConfig;
import com.maciej.wojtaczka.userconnector.domain.model.Connection;
import com.maciej.wojtaczka.userconnector.domain.model.ConnectionRequest;
import com.maciej.wojtaczka.userconnector.domain.model.User;
import com.maciej.wojtaczka.userconnector.persistence.entity.ConnectionEntity;
import com.maciej.wojtaczka.userconnector.persistence.entity.ConnectionRequestEntity;
import com.maciej.wojtaczka.userconnector.rest.controller.dto.ConnectionRequestBody;
import com.maciej.wojtaczka.userconnector.utils.KafkaTestListener;
import com.maciej.wojtaczka.userconnector.utils.UserFixtures;
import lombok.SneakyThrows;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.maciej.wojtaczka.userconnector.rest.controller.UserConnectorRestController.CONNECTIONS_URL;
import static com.maciej.wojtaczka.userconnector.rest.controller.UserConnectorRestController.CONNECTION_REQUESTS_URL;
import static com.maciej.wojtaczka.userconnector.rest.controller.UserConnectorRestController.USER_PARAM;
import static java.time.Instant.parse;
import static java.time.temporal.ChronoUnit.MILLIS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@WebAppConfiguration
@AutoConfigureMockMvc
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
@Import({ CassandraConfig.class })
class UserConnectorRestControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private CassandraOperations cassandraOperations;

	@Autowired
	private WireMockServer wireMockServer;

	@Autowired
	private UserFixtures $;

	@Autowired
	private KafkaTestListener kafkaTestListener;

	@BeforeEach
	void setupEach() {
		wireMockServer.resetAll();
		kafkaTestListener.reset();
	}

	@Test
	void shouldCreateConnectionRequestAndEmmitEvent() throws Exception {
		//given
		UUID recipientId = UUID.randomUUID();
		UUID requesterId = UUID.randomUUID();
		$.givenUser().withId(requesterId).exists();
		$.givenUser().withId(recipientId).exists();

		ConnectionRequestBody requestBody = ConnectionRequestBody.builder()
																 .requesterId(requesterId)
																 .recipientId(recipientId)
																 .build();

		//when
		ResultActions result = mockMvc.perform(post(CONNECTION_REQUESTS_URL)
													   .content(asJsonString(requestBody))
													   .contentType(APPLICATION_JSON)
													   .accept(APPLICATION_JSON));
		//then
		//verify response
		String jsonConnectionRequest = result.andExpect(status().isCreated())
											 .andExpect(jsonPath("$.recipientId", equalTo(recipientId.toString())))
											 .andExpect(jsonPath("$.requesterId", equalTo(requesterId.toString())))
											 .andExpect(jsonPath("$.creationTime", notNullValue()))
											 .andReturn().getResponse().getContentAsString();

		//verify persistence
		ConnectionRequest connectionRequest = objectMapper.readValue(jsonConnectionRequest, ConnectionRequest.class);
		List<ConnectionRequestEntity> fromDb = cassandraOperations.select(
				String.format("select * from user_connector.connection_request where recipient_id = %s;", recipientId),
				ConnectionRequestEntity.class);
		assertAll(
				() -> assertThat(fromDb).hasSize(1),
				() -> assertThat(fromDb.get(0).getRecipientId()).isEqualTo(recipientId),
				() -> assertThat(fromDb.get(0).getRequesterId()).isEqualTo(requesterId),
				() -> assertThat(fromDb.get(0).getCreationTime()).isEqualTo(connectionRequest.getCreationTime().truncatedTo(MILLIS))
		);

		//verify publishing events
		String capturedEvent = kafkaTestListener.receiveFirstContentFromTopic(User.DomainEvents.CONNECTION_REQUESTED)
												.orElseThrow(() -> new RuntimeException("No event"));
		JSONAssert.assertEquals(jsonConnectionRequest, capturedEvent, false);
		assertThat(kafkaTestListener.noMoreMessagesOnTopic(User.DomainEvents.CONNECTION_REQUESTED, 50)).isTrue();
	}

	@Test
	void shouldFetchExistingConnectionRequestsForGivenUser() throws Exception {
		//given
		UUID recipientId = UUID.randomUUID();
		UUID requesterId1 = UUID.randomUUID();
		UUID requesterId2 = UUID.randomUUID();
		UUID requesterId3 = UUID.randomUUID();

		$.givenUser()
		 .withId(recipientId)
		 .withPendingConnectionRequest().fromRequester(requesterId1).atTime(parse("2007-12-03T10:15:30.00Z"))
		 .andAlso().fromRequester(requesterId2).atTime(parse("2007-12-03T10:16:30.00Z"))
		 .andAlso().fromRequester(requesterId3).atTime(parse("2007-12-03T10:17:30.00Z"))
		 .andTheGivenUser()
		 .exists();

		//when
		mockMvc.perform(get(CONNECTION_REQUESTS_URL)
								.param(USER_PARAM, recipientId.toString())
								.accept(APPLICATION_JSON))
			   //then
			   .andExpect(status().isOk())
			   .andExpect(jsonPath("$", Matchers.hasSize(3)))
			   .andExpect(jsonPath("$[0].requesterId", equalTo(requesterId3.toString())))
			   .andExpect(jsonPath("$[1].requesterId", equalTo(requesterId2.toString())))
			   .andExpect(jsonPath("$[2].requesterId", equalTo(requesterId1.toString())));

	}

	@Test
	void shouldCreateConnectionAndEmmitEvent_whenConnectionAccepted() throws Exception {
		//given
		UUID recipientId = UUID.randomUUID();
		UUID requesterId = UUID.randomUUID();
		$.givenUser().withId(requesterId).exists();
		$.givenUser().withId(recipientId)
		 .withPendingConnectionRequest().fromRequester(requesterId)
		 .andTheGivenUser()
		 .exists();

		ConnectionRequest connectionRequest = ConnectionRequest.builder()
															   .recipientId(recipientId)
															   .requesterId(requesterId)
															   .build();
		//when
		ResultActions result = mockMvc.perform(post(CONNECTIONS_URL)
													   .content(asJsonString(connectionRequest))
													   .contentType(APPLICATION_JSON)
													   .accept(APPLICATION_JSON));
		//then
		//verify response
		String jsonConnection = result.andExpect(status().isCreated())
									  .andExpect(jsonPath("$.user1", equalTo(recipientId.toString())))
									  .andExpect(jsonPath("$.user2", equalTo(requesterId.toString())))
									  .andExpect(jsonPath("$.connectionDate", notNullValue()))
									  .andReturn().getResponse().getContentAsString();

		//verify persistence
		Connection connection = objectMapper.readValue(jsonConnection, Connection.class);

		ConnectionEntity connectionEntity1 = cassandraOperations.selectOne(
				String.format("select * from user_connector.connection where user1 = %s and user2 = %s;", recipientId, requesterId),
				ConnectionEntity.class);
		ConnectionEntity connectionEntity2 = cassandraOperations.selectOne(
				String.format("select * from user_connector.connection where user1 = %s and user2 = %s;", requesterId, recipientId),
				ConnectionEntity.class);
		ConnectionRequestEntity connectionRequestEntity = cassandraOperations.selectOne(
				String.format("select * from user_connector.connection_request where recipient_id = %s and requester_id = %s;",
							  recipientId, requesterId),
				ConnectionRequestEntity.class);

		assertThat(connectionEntity1).isNotNull();
		assertThat(connectionEntity2).isNotNull();
		assertThat(connectionRequestEntity).isNull();

		assertAll(
				() -> assertThat(connectionEntity1.getUser1()).isEqualTo(recipientId),
				() -> assertThat(connectionEntity1.getUser2()).isEqualTo(requesterId),
				() -> assertThat(connectionEntity1.getConnectionDate()).isEqualTo(connection.getConnectionDate().truncatedTo(MILLIS)),
				() -> assertThat(connectionEntity2.getUser1()).isEqualTo(requesterId),
				() -> assertThat(connectionEntity2.getUser2()).isEqualTo(recipientId),
				() -> assertThat(connectionEntity2.getConnectionDate()).isEqualTo(connection.getConnectionDate().truncatedTo(MILLIS))
		);


		//verify publishing event
		String capturedEvent = kafkaTestListener.receiveFirstContentFromTopic(User.DomainEvents.CONNECTION_CREATED)
												.orElseThrow(() -> new RuntimeException("No event"));
		JSONAssert.assertEquals(jsonConnection, capturedEvent, false);
		assertThat(kafkaTestListener.noMoreMessagesOnTopic(User.DomainEvents.CONNECTION_CREATED, 50)).isTrue();
	}

	@Test
	void shouldFetchExistingConnectionsForGivenUser() throws Exception {
		//given
		UUID userId = UUID.randomUUID();
		UUID connectedUserId1 = UUID.randomUUID();
		UUID connectedUserId2 = UUID.randomUUID();
		UUID connectedUserId3 = UUID.randomUUID();

		$.givenUser()
		 .withId(userId)
		 .connected().withUser(connectedUserId1).atTime(parse("2007-12-03T10:15:30.00Z"))
		 .andAlso().withUser(connectedUserId2).atTime(parse("2007-12-03T10:16:30.00Z"))
		 .andAlso().withUser(connectedUserId3).atTime(parse("2007-12-03T10:17:30.00Z"))
		 .andTheGivenUser()
		 .exists();

		var c1 = Connection.builder().user1(userId).user2(connectedUserId1).connectionDate(parse("2007-12-03T10:15:30.00Z")).build();
		var c2 = Connection.builder().user1(userId).user2(connectedUserId2).connectionDate(parse("2007-12-03T10:16:30.00Z")).build();
		var c3 = Connection.builder().user1(userId).user2(connectedUserId3).connectionDate(parse("2007-12-03T10:17:30.00Z")).build();
		Set<Connection> expectedConnections = Set.of(c1, c2, c3);

		//when
		String connectionsJson = mockMvc.perform(get(CONNECTIONS_URL)
													 .param(USER_PARAM, userId.toString())
													 .accept(APPLICATION_JSON))
									//then
									.andExpect(status().isOk())
									.andExpect(jsonPath("$", Matchers.hasSize(3)))
									.andReturn().getResponse().getContentAsString();

		Connection[] connections = objectMapper.readValue(connectionsJson, Connection[].class);

		assertThat(new HashSet<>(Arrays.asList(connections))).isEqualTo(expectedConnections);
	}

	@SneakyThrows
	private String asJsonString(final Object obj) {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.writeValueAsString(obj);
	}

}
