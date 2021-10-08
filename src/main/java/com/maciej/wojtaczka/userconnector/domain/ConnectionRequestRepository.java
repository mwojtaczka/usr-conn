package com.maciej.wojtaczka.userconnector.domain;

import com.maciej.wojtaczka.userconnector.domain.model.ConnectionRequest;

import java.util.List;
import java.util.UUID;

public interface ConnectionRequestRepository {

	ConnectionRequest save(ConnectionRequest connectionRequest);

	List<ConnectionRequest> findByRecipientId(UUID recipientId);

}
