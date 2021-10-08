package com.maciej.wojtaczka.userconnector.domain;

import com.maciej.wojtaczka.userconnector.domain.model.ConnectionRequest;

public interface ConnectionRequestRepository {

	ConnectionRequest save(ConnectionRequest connectionRequest);
}
