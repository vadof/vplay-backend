package com.vcasino.clicker.mapper;

import com.vcasino.clicker.dto.task.TaskDto;
import com.vcasino.clicker.entity.Task;
import com.vcasino.clicker.mapper.common.EntityMapper;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        builder = @Builder(disableBuilder = true),
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface TaskMapper extends EntityMapper<Task, TaskDto> {
    @Override
    @Mapping(target = "service", source = "integratedService")
    TaskDto toDto(Task entity);
}
