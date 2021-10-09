package com.maciej.wojtaczka.userconnector.utils;

import com.maciej.wojtaczka.userconnector.domain.model.User;
import lombok.SneakyThrows;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class KafkaTestListener {

	private Map<String, ConcurrentLinkedQueue<ConsumerRecord<String, String>>> recordsPerTopic;
	private Map<String, CountDownLatch> latchPerTopic;

	public KafkaTestListener() {
		recordsPerTopic = new HashMap<>();
		recordsPerTopic.put(User.DomainEvents.CONNECTION_REQUESTED, new ConcurrentLinkedQueue<>());
		recordsPerTopic.put(User.DomainEvents.CONNECTION_CREATED, new ConcurrentLinkedQueue<>());
		latchPerTopic = new HashMap<>();
		latchPerTopic.put(User.DomainEvents.CONNECTION_REQUESTED, new CountDownLatch(1));
		latchPerTopic.put(User.DomainEvents.CONNECTION_CREATED, new CountDownLatch(1));
	}

	public void reset() {
		recordsPerTopic.forEach((k, v) -> v.clear());
		latchPerTopic = latchPerTopic.entrySet().stream()
									 .collect(Collectors.toMap(Map.Entry::getKey, entry -> new CountDownLatch(1)));
	}

	@KafkaListener(topics = User.DomainEvents.CONNECTION_REQUESTED, groupId = "test")
	void receiveConnRequested(ConsumerRecord<String, String> consumerRecord) {

		ConcurrentLinkedQueue<ConsumerRecord<String, String>> connectionRequestedRecords =
				recordsPerTopic.get(User.DomainEvents.CONNECTION_REQUESTED);

		connectionRequestedRecords.add(consumerRecord);

		latchPerTopic.get(User.DomainEvents.CONNECTION_REQUESTED).countDown();
	}

	@KafkaListener(topics = User.DomainEvents.CONNECTION_CREATED, groupId = "test")
	void receiveConnCreated(ConsumerRecord<String, String> consumerRecord) {

		ConcurrentLinkedQueue<ConsumerRecord<String, String>> connectionRequestedRecords =
				recordsPerTopic.get(User.DomainEvents.CONNECTION_CREATED);

		connectionRequestedRecords.add(consumerRecord);

		latchPerTopic.get(User.DomainEvents.CONNECTION_CREATED).countDown();
	}

	@SneakyThrows
	public Optional<String> receiveFirstContentFromTopic(String topic) {
		latchPerTopic.get(topic).await(200, TimeUnit.MILLISECONDS);
		ConsumerRecord<String, String> firstMessage = recordsPerTopic.get(topic).poll();
		if (firstMessage == null) {
			return Optional.empty();
		}
		return Optional.of(firstMessage.value());
	}

	@SneakyThrows
	public boolean noMoreMessagesOnTopic(String topic, long awaitTimeMillis) {
		Thread.sleep(awaitTimeMillis);
		return recordsPerTopic.get(topic).isEmpty();
	}
}
