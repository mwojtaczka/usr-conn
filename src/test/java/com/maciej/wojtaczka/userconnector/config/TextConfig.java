package com.maciej.wojtaczka.userconnector.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

@Configuration
public class TextConfig {

	@Bean
	WireMockServer wireMockServer() {
		WireMockServer wireMockServer = new WireMockServer(options().port(8081));
		wireMockServer.start();
		return wireMockServer;
	}
}
