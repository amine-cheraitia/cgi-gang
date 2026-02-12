package com.marketplace.storage.infrastructure;

import com.marketplace.storage.domain.port.ObjectStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class LocalObjectStorageAdapterTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldStoreObjectLocallyAndResolveUri() throws Exception {
        StorageProperties properties = new StorageProperties();
        properties.getLocal().setRootPath(tempDir.toString());
        properties.getLocal().setPublicBaseUrl("http://localhost:8080/files");

        LocalObjectStorageAdapter adapter = new LocalObjectStorageAdapter(properties);
        ObjectStorage.StoredObject stored = adapter.store("tickets/file.txt", "ok".getBytes(), "text/plain");

        Path expectedPath = tempDir.resolve("tickets/file.txt");
        assertThat(Files.exists(expectedPath)).isTrue();
        assertThat(Files.readString(expectedPath)).isEqualTo("ok");
        assertThat(stored.uri().toString()).isEqualTo("http://localhost:8080/files/tickets/file.txt");
    }
}
