package com.maciej.wojtaczka.userconnector.rest.client;

import com.maciej.wojtaczka.userconnector.domain.UserService;
import com.maciej.wojtaczka.userconnector.domain.model.User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class UserServiceRestClient implements UserService {

	public final static String GET_USERS_BY_IDS = "/v1/users/";

	private final RestTemplate restTemplate;

	public UserServiceRestClient(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	@Override
	public List<User> fetchAll(UUID userId, UUID... otherIds) {

		StringBuilder uriParamsBuilder = new StringBuilder("id=" + userId.toString());

		for (UUID id : otherIds) {
			uriParamsBuilder.append(",").append(id);
		}

		String uriParams = uriParamsBuilder.toString();

		User[] users = restTemplate.getForObject(GET_USERS_BY_IDS, User[].class, uriParams);

		if (users == null) {
			return List.of();
		}
		return Arrays.asList(users);
	}

}
