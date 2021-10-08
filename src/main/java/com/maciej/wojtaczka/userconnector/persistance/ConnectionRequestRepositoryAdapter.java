package com.maciej.wojtaczka.userconnector.persistance;

import com.maciej.wojtaczka.userconnector.domain.ConnectionRequestRepository;
import com.maciej.wojtaczka.userconnector.domain.model.ConnectionRequest;
import com.maciej.wojtaczka.userconnector.persistance.entity.ConnectionRequestEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

	@Override
	public List<ConnectionRequest> findByRecipientId(UUID recipientId) {
		return repository.findByRecipientId(recipientId).stream()
						 .map(mapper::toModel)
						 .collect(Collectors.toList());
	}
}
