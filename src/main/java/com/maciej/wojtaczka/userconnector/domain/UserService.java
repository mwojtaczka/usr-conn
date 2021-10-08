package com.maciej.wojtaczka.userconnector.domain;

import com.maciej.wojtaczka.userconnector.domain.model.User;

import java.util.List;
import java.util.UUID;

public interface UserService {

    List<User> fetchAll(UUID userId, UUID ... otherIds);
}
