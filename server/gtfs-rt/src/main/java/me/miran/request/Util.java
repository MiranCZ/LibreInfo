package me.miran.request;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

class Util {

    static String readSecret(String name) {
        Path path = Path.of("/run/secrets/").resolve(name);

        try {
            String value = Files.readString(path).strip();
            if (value.isEmpty()) {
                throw new IllegalStateException("Secret at " + path + " is empty");
            }
            return value;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read secret from " + path + " (is the secret mounted?)", e);
        }
    }

}
