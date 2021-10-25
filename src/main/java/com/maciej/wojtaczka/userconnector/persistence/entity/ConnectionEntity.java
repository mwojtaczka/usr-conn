package com.maciej.wojtaczka.userconnector.persistence.entity;

import com.maciej.wojtaczka.userconnector.domain.model.Connection;
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

	public static ConnectionEntity from(Connection connection) {
		return ConnectionEntity.builder()
							   .user1(connection.getUser1())
							   .user2(connection.getUser2())
							   .connectionDate(connection.getConnectionDate())
							   .build();
	}

	public static ConnectionEntity reversedFrom(Connection connection) {
		return ConnectionEntity.builder()
							   .user1(connection.getUser2())
							   .user2(connection.getUser1())
							   .connectionDate(connection.getConnectionDate())
							   .build();
	}

	public Connection toModel() {
		return Connection.builder()
						 .user1(user1)
						 .user2(user2)
						 .connectionDate(connectionDate)
						 .build();
	}
}
