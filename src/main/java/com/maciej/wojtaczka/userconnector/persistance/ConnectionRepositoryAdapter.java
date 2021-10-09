package com.maciej.wojtaczka.userconnector.persistance;

import com.maciej.wojtaczka.userconnector.domain.ConnectionRepository;
import com.maciej.wojtaczka.userconnector.domain.model.Connection;
import com.maciej.wojtaczka.userconnector.domain.model.ConnectionRequest;
import com.maciej.wojtaczka.userconnector.persistance.entity.ConnectionEntity;
import com.maciej.wojtaczka.userconnector.persistance.entity.ConnectionRequestEntity;
import org.springframework.data.cassandra.core.CassandraOperations;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ConnectionRepositoryAdapter implements ConnectionRepository {

	private final ConnectionRequestCassandraRepository repository;
	private final ConnectionRequestModelToDbEntityMapper connectionRequestMapper;
	private final CassandraOperations cassandraTemplate;

	public ConnectionRepositoryAdapter(ConnectionRequestCassandraRepository repository,
									   ConnectionRequestModelToDbEntityMapper connectionRequestMapper,
									   CassandraOperations cassandraTemplate) {
		this.repository = repository;
		this.connectionRequestMapper = connectionRequestMapper;
		this.cassandraTemplate = cassandraTemplate;
	}

	@Override
	public ConnectionRequest saveConnectionRequest(ConnectionRequest connectionRequest) {

		ConnectionRequestEntity save = repository.save(connectionRequestMapper.toDbEntity(connectionRequest));

		return connectionRequestMapper.toModel(save);
	}

	@Override
	public List<ConnectionRequest> findConnectionRequestByRecipientId(UUID recipientId) {
		return repository.findByRecipientId(recipientId).stream()
						 .map(connectionRequestMapper::toModel)
						 .collect(Collectors.toList());
	}

	@Override
	public Optional<ConnectionRequest> findConnectionRequest(UUID recipientId, UUID requesterId) {
		return repository.findByRecipientIdAndRequesterId(recipientId, requesterId);
	}

	@Override
	public void saveConnectionAndRemoveRequest(Connection connection, ConnectionRequest connectionRequest) {

		ConnectionRequestEntity connectionRequestEntity = connectionRequestMapper.toDbEntity(connectionRequest);

		ConnectionEntity connectionForUser1 = ConnectionEntity.builder()
															  .user1(connection.getUser1())
															  .user2(connection.getUser2())
															  .connectionDate(connection.getConnectionDate())
															  .build();
		ConnectionEntity connectionForUser2 = ConnectionEntity.builder()
															  .user1(connection.getUser2())
															  .user2(connection.getUser1())
															  .connectionDate(connection.getConnectionDate())
															  .build();

		cassandraTemplate.batchOps()
						 .delete(connectionRequestEntity)
						 .insert(connectionForUser1, connectionForUser2)
						 .execute();
	}
}
