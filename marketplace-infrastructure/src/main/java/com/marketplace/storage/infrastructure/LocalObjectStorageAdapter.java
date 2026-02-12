package com.marketplace.storage.infrastructure;

import com.marketplace.storage.domain.port.ObjectStorage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
@ConditionalOnProperty(name = "storage.provider", havingValue = "local", matchIfMissing = true)
public class LocalObjectStorageAdapter implements ObjectStorage {
    private final StorageProperties properties;

    public LocalObjectStorageAdapter(StorageProperties properties) {
        this.properties = properties;
    }

    @Override
    public StoredObject store(String key, byte[] content, String contentType) {
        Path root = Path.of(properties.getLocal().getRootPath());
        Path destination = root.resolve(key).normalize();
        try {
            Files.createDirectories(destination.getParent());
            Files.write(destination, content);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot store object locally: " + key, e);
        }
        return new StoredObject(key, resolve(key));
    }

    @Override
    public URI resolve(String key) {
        return URI.create(properties.getLocal().getPublicBaseUrl() + "/" + key);
    }
}
