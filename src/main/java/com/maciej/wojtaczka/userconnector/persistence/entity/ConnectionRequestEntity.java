package com.maciej.wojtaczka.userconnector.persistence.entity;

import lombok.Builder;
import lombok.Value;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("connection_request")
@Builder
@Value
public class ConnectionRequestEntity {

	@PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED, name = "recipient_id")
	UUID recipientId;

	@PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, name = "requester_id")
	UUID requesterId;

	@PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING, name = "creation_time")
	Instant creationTime;


}
