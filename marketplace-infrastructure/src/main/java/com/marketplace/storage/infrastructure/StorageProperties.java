package com.marketplace.storage.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "storage")
public class StorageProperties {
    private String provider = "local";
    private Local local = new Local();
    private S3 s3 = new S3();

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public Local getLocal() {
        return local;
    }

    public void setLocal(Local local) {
        this.local = local;
    }

    public S3 getS3() {
        return s3;
    }

    public void setS3(S3 s3) {
        this.s3 = s3;
    }

    public static class Local {
        private String rootPath = "./data/storage";
        private String publicBaseUrl = "http://localhost:8080/files";

        public String getRootPath() {
            return rootPath;
        }

        public void setRootPath(String rootPath) {
            this.rootPath = rootPath;
        }

        public String getPublicBaseUrl() {
            return publicBaseUrl;
        }

        public void setPublicBaseUrl(String publicBaseUrl) {
            this.publicBaseUrl = publicBaseUrl;
        }
    }

    public static class S3 {
        private String bucket = "";
        private String region = "";
        private long signedUrlDurationSeconds = 900;

        public String getBucket() {
            return bucket;
        }

        public void setBucket(String bucket) {
            this.bucket = bucket;
        }

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public long getSignedUrlDurationSeconds() {
            return signedUrlDurationSeconds;
        }

        public void setSignedUrlDurationSeconds(long signedUrlDurationSeconds) {
            this.signedUrlDurationSeconds = signedUrlDurationSeconds;
        }
    }
}
