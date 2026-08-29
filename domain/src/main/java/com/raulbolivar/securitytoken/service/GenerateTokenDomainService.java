package com.raulbolivar.securitytoken.service;

import com.raulbolivar.securitytoken.exception.MissingParameterException;
import com.raulbolivar.securitytoken.model.ChannelCryptoConfig;
import com.raulbolivar.securitytoken.model.DecryptedCredentials;
import com.raulbolivar.securitytoken.model.GenerateTokenCommand;

import java.util.ArrayList;
import java.util.List;

public class GenerateTokenDomainService {

    public DecryptedCredentials prepareDecryptedCredentials(GenerateTokenCommand command,
                                                            ChannelCryptoConfig cfg,
                                                            FieldDecryptor decryptor) {
        validateRequired(command);

        return new DecryptedCredentials(
                decryptor.decrypt("client_id", command.clientId(), cfg),
                decryptor.decrypt("client_secret", command.clientSecret(), cfg),
                decryptor.decrypt("grant_type", command.grantType(), cfg),
                decryptor.decrypt("scope", command.scope(), cfg)
        );
    }

    private void validateRequired(GenerateTokenCommand cmd) {
        List<String> missing = new ArrayList<>();
        if (isBlank(cmd.clientId())) missing.add("client_id");
        if (isBlank(cmd.clientSecret())) missing.add("client_secret");
        if (isBlank(cmd.grantType())) missing.add("grant_type");
        if (isBlank(cmd.scope())) missing.add("scope");
        if (isBlank(cmd.channel())) missing.add("channel");

        if (!missing.isEmpty()) {
            throw new MissingParameterException(missing);
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    @FunctionalInterface
    public interface FieldDecryptor {
        String decrypt(String fieldName, String cipherPayload, ChannelCryptoConfig cfg);
    }
}
