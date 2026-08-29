package com.raulbolivar.securitytoken.ports.out;

import com.raulbolivar.securitytoken.model.ChannelCryptoConfig;

public interface CredentialDecryptorPort {

    String decrypt(String fieldName, String cipherPayload, ChannelCryptoConfig cfg);
}
