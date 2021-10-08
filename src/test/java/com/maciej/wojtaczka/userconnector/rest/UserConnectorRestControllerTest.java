package com.maciej.wojtaczka.userconnector.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.maciej.wojtaczka.userconnector.config.TestConfig;
import com.maciej.wojtaczka.userconnector.domain.model.ConnectionRequest;
import com.maciej.wojtaczka.userconnector.domain.model.User;
import com.maciej.wojtaczka.userconnector.persistance.ConnectionRequestCassandraRepository;
import com.maciej.wojtaczka.userconnector.persistance.entity.ConnectionRequestEntity;
import com.maciej.wojtaczka.userconnector.rest.controller.dto.ConnectionRequestBody;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static com.maciej.wojtaczka.userconnector.rest.client.UserServiceRestClient.GET_USERS_BY_IDS;
import static com.maciej.wojtaczka.userconnector.rest.controller.UserConnectorRestController.CONNECTION_REQUEST_URL;
import static java.time.temporal.ChronoUnit.MILLIS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@WebAppConfiguration
@AutoConfigureMockMvc
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
@Import({ TestConfig.class })
class UserConnectorRestControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private ConnectionRequestCassandraRepository repository;

	private static WireMockServer wireMockServer;

	@BeforeAll
	static void setupALl() {
		wireMockServer = new WireMockServer(options().port(8081));
		wireMockServer.start();
	}

	@BeforeEach
	void setupEach() {
		wireMockServer.resetAll();
	}

	@AfterAll
	static void cleanup() {
		wireMockServer.stop();
	}

	@Test
	void shouldCreateConnectionRequestAndEmmitEvent() throws Exception {
		//given
		UUID recipientId = UUID.randomUUID();
		UUID requesterId = UUID.randomUUID();
		ConnectionRequestBody requestBody = ConnectionRequestBody.builder()
																 .requesterId(requesterId)
																 .recipientId(recipientId)
																 .build();

		User requester = User.builder().id(requesterId).build();
		User recipient = User.builder().id(recipientId).build();

		userServiceWillReturn(recipient, requester);

		//when
		String jsonConnectionRequest = mockMvc.perform(post(CONNECTION_REQUEST_URL)
															   .content(asJsonString(requestBody))
															   .contentType(APPLICATION_JSON)
															   .accept(APPLICATION_JSON))
											  //then
											  .andExpect(status().isCreated())
											  .andExpect(jsonPath("$.recipientId", equalTo(recipientId.toString())))
											  .andExpect(jsonPath("$.requesterId", equalTo(requesterId.toString())))
											  .andExpect(jsonPath("$.creationTime", notNullValue()))
											  .andReturn().getResponse().getContentAsString();

		ConnectionRequest connectionRequest = objectMapper.readValue(jsonConnectionRequest, ConnectionRequest.class);
		List<ConnectionRequestEntity> fromDb = repository.findByRecipientId(recipientId);

		assertAll(
				() -> assertThat(fromDb).hasSize(1),
				() -> assertThat(fromDb.get(0).getRecipientId()).isEqualTo(recipientId),
				() -> assertThat(fromDb.get(0).getRequesterId()).isEqualTo(requesterId),
				() -> assertThat(fromDb.get(0).getCreationTime()).isEqualTo(connectionRequest.getCreationTime().truncatedTo(MILLIS))
		);
	}

	@SneakyThrows
	private void userServiceWillReturn(User... users) {

		String jsonResponseBody = objectMapper.writeValueAsString(users);

		ResponseDefinitionBuilder response = WireMock.aResponse()
													 .withHeader("Content-type", "application/json")
													 .withBody(jsonResponseBody);
		wireMockServer.stubFor(WireMock.get(WireMock.urlEqualTo(GET_USERS_BY_IDS))
							   .willReturn(response));

	}

	@SneakyThrows
	private String asJsonString(final Object obj) {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.writeValueAsString(obj);
	}

}
