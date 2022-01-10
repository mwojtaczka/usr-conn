package com.maciej.wojtaczka.userconnector.rest.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestClientConfig {

	@Value("${user.registrar.host:localhost}")
	private String usrReg;

	@Bean
	RestTemplate restTemplate(RestTemplateBuilder builder) {

		return builder.rootUri(String.format("http://%s", usrReg)) //TODO
			   .build();
	}
}
