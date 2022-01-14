package com.maciej.wojtaczka.userconnector.rest.controller;

import com.maciej.wojtaczka.userconnector.domain.ConnectionException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionMapper {

	@ExceptionHandler(value = ConnectionException.class)
	ResponseEntity<Object> handleUserException(ConnectionException e) {

		switch (e.getType()) {
			case NOT_FOUND:
				return ResponseEntity.notFound().build();
			case ILLEGAL_STATE:
				return ResponseEntity.badRequest()
									 .body(e.getMessage());
			default:
				return ResponseEntity.internalServerError()
									 .build();
		}
	}
}
