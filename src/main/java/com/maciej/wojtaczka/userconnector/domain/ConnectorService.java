package com.maciej.wojtaczka.userconnector.domain;

import com.maciej.wojtaczka.userconnector.domain.model.Connection;
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
	private final ConnectionRepository connectionRepository;
	private final DomainEventPublisher domainEventPublisher;

	public ConnectorService(UserService userService,
							ConnectionRepository connectionRepository,
							DomainEventPublisher domainEventPublisher) {
		this.userService = userService;
		this.connectionRepository = connectionRepository;
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

		return connectionRepository.saveConnectionRequest(connectionRequest);
	}

	public List<ConnectionRequest> fetchPendingRequests(UUID recipientId) {
		return connectionRepository.findConnectionRequestByRecipientId(recipientId).stream()
								   .sorted(comparing(ConnectionRequest::getCreationTime).reversed())
								   .collect(Collectors.toList());
	}

	public Connection acceptRequest(ConnectionRequest connectionRequest) {
		UUID recipientId = connectionRequest.getRecipientId();
		UUID requesterId = connectionRequest.getRequesterId();

		ConnectionRequest connectionRequestFromRepository =
				connectionRepository.findConnectionRequest(recipientId, requesterId)
									.orElseThrow(() -> ConnectionException.connectionRequestNotFound(recipientId, requesterId));

		List<User> users = userService.fetchAll(requesterId, recipientId);
		users.stream()
			 .filter(user -> requesterId.equals(user.getId()))
			 .findFirst()
			 .orElseThrow(() -> UserException.userNotFound(requesterId));
		User recipient = users.stream()
							  .filter(user -> recipientId.equals(user.getId()))
							  .findFirst()
							  .orElseThrow(() -> UserException.userNotFound(recipientId));

		Connection newConnection = recipient.acceptRequest(connectionRequestFromRepository);

		connectionRepository.saveConnectionAndRemoveRequest(newConnection, connectionRequestFromRepository);

		recipient.getDomainEvents()
				 .forEach(domainEventPublisher::publish);

		return newConnection;
	}
}
