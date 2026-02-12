package com.marketplace.storage.domain.port;

import java.net.URI;

public interface ObjectStorage {
    StoredObject store(String key, byte[] content, String contentType);

    URI resolve(String key);

    record StoredObject(String key, URI uri) {
    }
}
