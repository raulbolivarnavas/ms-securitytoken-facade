package com.raulbolivar.securitytoken.adapter.in.rest.mapper;

import com.raulbolivar.securitytoken.model.GenerateTokenCommand;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.springframework.util.LinkedMultiValueMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GenerateTokenApiMapperTest {

    private final GenerateTokenApiMapper mapper = Mappers.getMapper(GenerateTokenApiMapper.class);

    @Test
    void toCommandShouldMapFormFieldsToDomainCommand() {
        LinkedMultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", "enc-id");
        form.add("client_secret", "enc-secret");
        form.add("grant_type", "enc-grant");
        form.add("scope", "enc-scope");
        form.add("channel", "8");

        GenerateTokenCommand command = mapper.toCommand(form);

        assertEquals("enc-id", command.clientId());
        assertEquals("enc-secret", command.clientSecret());
        assertEquals("enc-grant", command.grantType());
        assertEquals("enc-scope", command.scope());
        assertEquals("8", command.channel());
    }
}
