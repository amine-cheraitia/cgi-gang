package com.marketplace.storage.domain.port;

import java.net.URI;

public interface ObjectStorage {
    StoredObject store(String key, byte[] content, String contentType);

    URI resolve(String key);

    default PresignedUpload presignUpload(String key, String contentType, long expiresInSeconds) {
        throw new UnsupportedOperationException("Presigned upload not supported");
    }

    record StoredObject(String key, URI uri) {
    }

    record PresignedUpload(String key, URI uploadUrl, String method, long expiresInSeconds) {
    }
}
