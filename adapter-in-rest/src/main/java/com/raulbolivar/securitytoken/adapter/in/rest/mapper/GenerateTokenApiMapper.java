package com.raulbolivar.securitytoken.adapter.in.rest.mapper;

import com.raulbolivar.securitytoken.adapter.in.rest.dto.GenerateTokenRequestDto;
import com.raulbolivar.securitytoken.model.GenerateTokenCommand;
import org.mapstruct.Mapper;
import org.springframework.util.MultiValueMap;

@Mapper(componentModel = "spring")
public interface GenerateTokenApiMapper {

    GenerateTokenCommand toCommand(GenerateTokenRequestDto request);

    default GenerateTokenCommand toCommand(MultiValueMap<String, String> form) {

        GenerateTokenRequestDto request = new GenerateTokenRequestDto(
                form.getFirst("client_id"),
                form.getFirst("client_secret"),
                form.getFirst("grant_type"),
                form.getFirst("scope"),
                form.getFirst("channel")
        );

        return toCommand(request);
    }
}
