package com.maciej.wojtaczka.userconnector.persistence;

import com.datastax.oss.driver.api.core.CqlSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.cassandra.core.AsyncCassandraTemplate;

@Configuration
public class Config {

	@Bean
	AsyncCassandraTemplate asyncCassandraTemplate(CqlSession session) {
		return new AsyncCassandraTemplate(session);
	}

}
