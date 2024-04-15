package it.gov.pagopa.logextractor.enums;

public enum RedisMode {
    SERVERLESS("serverless"),
    MANAGED("managed");

    private final String value;

    RedisMode(String value) {
        this.value = value;
    }
}
