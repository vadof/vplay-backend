package com.vcasino.bet.mapper;

import org.mapstruct.IterableMapping;
import org.mapstruct.NullValueMappingStrategy;

import java.util.List;

public interface EntityMapper<E, D> {

    D toDto(E entity);

    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    List<D> toDtos(List<E> entityList);

}
