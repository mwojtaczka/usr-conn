package com.maciej.wojtaczka.userconnector.domain;

import com.maciej.wojtaczka.userconnector.domain.model.ConnectionRequest;
import com.maciej.wojtaczka.userconnector.domain.model.User;
import org.springframework.stereotype.Service;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

@Service
public class ConnectorService {

	private final UserService userService;
	private final ConnectionRequestRepository connectionRequestRepository;
	private final DomainEventPublisher domainEventPublisher;

	public ConnectorService(UserService userService,
							ConnectionRequestRepository connectionRequestRepository,
							DomainEventPublisher domainEventPublisher) {
		this.userService = userService;
		this.connectionRequestRepository = connectionRequestRepository;
		this.domainEventPublisher = domainEventPublisher;
	}

	public ConnectionRequest sendConnectionRequest(@NotNull UUID requesterId, @NotNull UUID recipientId) {

		if (requesterId == null || recipientId == null) {
			throw new IllegalArgumentException("One of indices is null");
		}

		List<User> users = userService.fetchAll(requesterId, recipientId);
		User requester = users.stream()
							  .filter(user -> requesterId.equals(user.getId()))
							  .findFirst()
							  .orElseThrow(() -> UserException.userNotFound(requesterId));
		User recipient = users.stream()
							  .filter(user -> recipientId.equals(user.getId()))
							  .findFirst()
							  .orElseThrow(() -> UserException.userNotFound(recipientId));

		ConnectionRequest connectionRequest = requester.createConnectionRequestTo(recipient);

		requester.getDomainEvents()
				 .forEach(domainEventPublisher::publish);

		return connectionRequestRepository.save(connectionRequest);
	}

	public List<ConnectionRequest> fetchPendingRequests(UUID recipientId) {
		return connectionRequestRepository.findByRecipientId(recipientId).stream()
										  .sorted(comparing(ConnectionRequest::getCreationTime).reversed())
										  .collect(Collectors.toList());
	}
}
