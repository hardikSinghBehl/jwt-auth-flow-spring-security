package com.behl.cerberus.dto.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.behl.cerberus.dto.UserUpdationRequestDto;
import com.behl.cerberus.entity.User;

import lombok.NonNull;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserPatchOperationMapper {

	@BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
	void patch(@NonNull UserUpdationRequestDto userUpdationRequestDto, @NonNull @MappingTarget User user);

}