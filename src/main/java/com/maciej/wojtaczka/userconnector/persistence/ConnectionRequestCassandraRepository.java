package com.maciej.wojtaczka.userconnector.persistence;

import com.maciej.wojtaczka.userconnector.domain.model.ConnectionRequest;
import com.maciej.wojtaczka.userconnector.persistence.entity.ConnectionRequestEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConnectionRequestCassandraRepository extends CassandraRepository<ConnectionRequestEntity, UUID> {

	List<ConnectionRequestEntity> findByRecipientId(UUID recipientId);

	Optional<ConnectionRequest> findByRecipientIdAndRequesterId(UUID recipientId, UUID requesterId);
}
