package com.maciej.wojtaczka.userconnector.persistance.entity;

import lombok.Builder;
import lombok.Value;
import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("connection")
@Builder
@Value
public class ConnectionEntity {

	@PrimaryKeyColumn(type = PrimaryKeyType.PARTITIONED, name = "user1")
	UUID user1;

	@PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, name = "user2")
	UUID user2;

	@PrimaryKeyColumn(type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING, name = "connection_date")
	Instant connectionDate;


}
