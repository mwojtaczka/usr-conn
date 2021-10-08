package com.maciej.wojtaczka.userconnector.persistance.entity;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("connection_request")
@Builder
@Getter
public class ConnectionRequestEntity {

	@PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED, name = "recipient_id")
	UUID recipientId;

	@PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING, name = "creation_time")
	Instant creationTime;

	@Column("requester_id")
	UUID requesterId;

}
