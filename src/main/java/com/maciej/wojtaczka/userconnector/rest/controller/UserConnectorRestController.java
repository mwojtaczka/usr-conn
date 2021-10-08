package com.maciej.wojtaczka.userconnector.rest.controller;

import com.maciej.wojtaczka.userconnector.domain.ConnectorService;
import com.maciej.wojtaczka.userconnector.domain.model.ConnectionRequest;
import com.maciej.wojtaczka.userconnector.rest.controller.dto.ConnectionRequestBody;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
public class UserConnectorRestController {

	public final static String CONNECTION_REQUEST_URL = "/v1/connection-requests";
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
}
