package com.maciej.wojtaczka.userconnector.persistence;

import com.maciej.wojtaczka.userconnector.domain.model.ConnectionRequest;
import com.maciej.wojtaczka.userconnector.persistence.entity.ConnectionRequestEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ConnectionRequestModelToDbEntityMapper {

	ConnectionRequest toModel(ConnectionRequestEntity connectionRequestEntity);

	ConnectionRequestEntity toDbEntity(ConnectionRequest connectionRequest);
}
