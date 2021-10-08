package com.maciej.wojtaczka.userconnector.config;

import com.datastax.oss.driver.api.core.CqlSession;
import com.github.nosan.embedded.cassandra.Cassandra;
import com.github.nosan.embedded.cassandra.CassandraBuilder;
import com.github.nosan.embedded.cassandra.Settings;
import com.github.nosan.embedded.cassandra.cql.CqlScript;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.net.InetSocketAddress;

@TestConfiguration
public class TestConfig {

	@Bean
	Cassandra cassandra() {
//			Cassandra cassandra = cassandraBuilder.build();
		Cassandra cassandra = new CassandraBuilder()
				.build();
		cassandra.start();
		try {
			Settings settings = cassandra.getSettings();
			try (CqlSession session = CqlSession.builder()
												.addContactPoint(new InetSocketAddress(settings.getAddress(), settings.getPort()))
												.withLocalDatacenter("datacenter1")
												.build()) {
					CqlScript.ofClassPath("schema.cql").forEachStatement(session::execute);
			}
		} finally {
//			cassandra.stop();
		}
		return cassandra;
	}

}
