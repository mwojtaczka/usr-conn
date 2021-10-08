package com.maciej.wojtaczka.userconnector.persistance;

import com.maciej.wojtaczka.userconnector.persistance.entity.ConnectionRequestEntity;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ConnectionRequestCassandraRepository extends CassandraRepository<ConnectionRequestEntity, UUID> {

	List<ConnectionRequestEntity> findByRecipientId(UUID recipientId);
}
