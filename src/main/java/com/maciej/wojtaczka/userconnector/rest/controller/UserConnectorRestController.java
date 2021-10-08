package com.maciej.wojtaczka.userconnector.rest.controller;

import com.maciej.wojtaczka.userconnector.domain.ConnectorService;
import com.maciej.wojtaczka.userconnector.domain.model.ConnectionRequest;
import com.maciej.wojtaczka.userconnector.rest.controller.dto.ConnectionRequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
public class UserConnectorRestController {

	public final static String CONNECTION_REQUEST_URL = "/v1/connection-requests";
	public final static String USER_PARAM = "user";
	private final ConnectorService connectorService;

	public UserConnectorRestController(ConnectorService connectorService) {
		this.connectorService = connectorService;
	}

	@PostMapping(CONNECTION_REQUEST_URL)
	ResponseEntity<ConnectionRequest> sendConnectionRequest(@RequestBody ConnectionRequestBody connectionRequestBody) {

		ConnectionRequest connectionRequest = connectorService.sendConnectionRequest(connectionRequestBody.getRequesterId(),
																					 connectionRequestBody.getRecipientId());
		String resourceLocation = CONNECTION_REQUEST_URL + "/" + connectionRequest.getRequesterId() + "/" + connectionRequest.getRecipientId();
		return ResponseEntity.created(URI.create(resourceLocation))
							 .body(connectionRequest);
	}

	@GetMapping(CONNECTION_REQUEST_URL)
	ResponseEntity<List<ConnectionRequest>> fetchConnectionRequests(@RequestParam(USER_PARAM) UUID recipientId) {

		List<ConnectionRequest> connectionRequests = connectorService.fetchPendingRequests(recipientId);

		return ResponseEntity.ok(connectionRequests);
	}
}
