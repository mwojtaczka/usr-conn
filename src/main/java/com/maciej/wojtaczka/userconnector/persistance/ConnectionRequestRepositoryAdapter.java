package com.maciej.wojtaczka.userconnector.persistance;

import com.maciej.wojtaczka.userconnector.domain.ConnectionRequestRepository;
import com.maciej.wojtaczka.userconnector.domain.model.ConnectionRequest;
import com.maciej.wojtaczka.userconnector.persistance.entity.ConnectionRequestEntity;
import org.springframework.stereotype.Component;

@Component
public class ConnectionRequestRepositoryAdapter implements ConnectionRequestRepository {

	private final ConnectionRequestCassandraRepository repository;
	private final ConnectionRequestModelToDbEntityMapper mapper;

	public ConnectionRequestRepositoryAdapter(ConnectionRequestCassandraRepository repository,
											  ConnectionRequestModelToDbEntityMapper mapper) {
		this.repository = repository;
		this.mapper = mapper;
	}

	@Override
	public ConnectionRequest save(ConnectionRequest connectionRequest) {

		ConnectionRequestEntity save = repository.save(mapper.toDbEntity(connectionRequest));

		return mapper.toModel(save);
	}
}
