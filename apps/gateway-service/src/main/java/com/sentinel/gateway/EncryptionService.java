package com.sentinel.gateway;

import org.springframework.stereotype.Service;
import java.util.Base64;

@Service
public class EncryptionService {

    // Simulating Signal Protocol encryption
    public String encrypt(String plainText) {
        // In a real scenario, this would happen on the CLIENT.
        // On the server, we simulate receiving already encrypted text.
        return Base64.getEncoder().encodeToString(plainText.getBytes());
    }

    public String decrypt(String encryptedText) {
        return new String(Base64.getDecoder().decode(encryptedText));
    }
}
