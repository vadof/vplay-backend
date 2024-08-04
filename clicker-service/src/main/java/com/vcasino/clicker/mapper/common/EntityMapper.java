package com.vcasino.clicker.mapper.common;

import org.mapstruct.IterableMapping;
import org.mapstruct.NullValueMappingStrategy;

import java.util.List;

public interface EntityMapper<E, D> {

    E toEntity(D dto);

    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    List<E> toEntities(List<D> dtoList);

    D toDto(E entity);

    @IterableMapping(nullValueMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT)
    List<D> toDtos(List<E> entityList);

}
