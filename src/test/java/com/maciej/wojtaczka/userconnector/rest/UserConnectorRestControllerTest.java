package com.maciej.wojtaczka.userconnector.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.maciej.wojtaczka.userconnector.config.CassandraConfig;
import com.maciej.wojtaczka.userconnector.domain.model.ConnectionRequest;
import com.maciej.wojtaczka.userconnector.persistance.ConnectionRequestCassandraRepository;
import com.maciej.wojtaczka.userconnector.persistance.entity.ConnectionRequestEntity;
import com.maciej.wojtaczka.userconnector.rest.controller.dto.ConnectionRequestBody;
import com.maciej.wojtaczka.userconnector.utils.UserFixtures;
import lombok.SneakyThrows;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static com.maciej.wojtaczka.userconnector.rest.controller.UserConnectorRestController.CONNECTION_REQUEST_URL;
import static com.maciej.wojtaczka.userconnector.rest.controller.UserConnectorRestController.USER_PARAM;
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
	private ConnectionRequestCassandraRepository repository;

	@Autowired
	private WireMockServer wireMockServer;

	@Autowired
	private UserFixtures $;

	@BeforeEach
	void setupEach() {
		wireMockServer.resetAll();
	}

	@Test
	void shouldCreateConnectionRequestAndEmmitEvent() throws Exception {
		//given
		UUID recipientId = UUID.randomUUID();
		UUID requesterId = UUID.randomUUID();
		$.user().withId(requesterId).exists();
		$.user().withId(recipientId).exists();

		ConnectionRequestBody requestBody = ConnectionRequestBody.builder()
																 .requesterId(requesterId)
																 .recipientId(recipientId)
																 .build();

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

	@Test
	void shouldFetchExistingConnectionRequestsForGivenUser() throws Exception {
		//given
		UUID recipientId = UUID.randomUUID();
		UUID requesterId1 = UUID.randomUUID();
		UUID requesterId2 = UUID.randomUUID();
		UUID requesterId3 = UUID.randomUUID();

		$.user()
		 .withId(recipientId)
		 .withPendingConnectionRequest().fromRequester(requesterId1).atTime(Instant.parse("2007-12-03T10:15:30.00Z"))
		 .andThisUser()
		 .withPendingConnectionRequest().fromRequester(requesterId2).atTime(Instant.parse("2007-12-03T10:16:30.00Z"))
		 .andThisUser()
		 .withPendingConnectionRequest().fromRequester(requesterId3).atTime(Instant.parse("2007-12-03T10:17:30.00Z"))
		 .andThisUser()
		 .exists();

		//when
		mockMvc.perform(get(CONNECTION_REQUEST_URL)
								.param(USER_PARAM, recipientId.toString())
								.accept(APPLICATION_JSON))
			   //then
			   .andExpect(status().isOk())
			   .andExpect(jsonPath("$", Matchers.hasSize(3)))
			   .andExpect(jsonPath("$[0].requesterId", equalTo(requesterId3.toString())))
			   .andExpect(jsonPath("$[1].requesterId", equalTo(requesterId2.toString())))
			   .andExpect(jsonPath("$[2].requesterId", equalTo(requesterId1.toString())));

	}

	@SneakyThrows
	private String asJsonString(final Object obj) {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.writeValueAsString(obj);
	}

}
