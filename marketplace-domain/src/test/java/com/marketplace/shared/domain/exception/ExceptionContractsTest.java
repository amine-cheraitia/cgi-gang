package com.marketplace.shared.domain.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionContractsTest {

    @Test
    void businessExceptionShouldExposeErrorCodeAndMessages() {
        BusinessException defaultMessage = new BusinessException(ErrorCode.ORDER_NOT_FOUND);
        BusinessException customMessage = new BusinessException(ErrorCode.ORDER_NOT_FOUND, "custom");

        assertThat(defaultMessage.getCode()).isEqualTo(ErrorCode.ORDER_NOT_FOUND);
        assertThat(defaultMessage).hasMessage(ErrorCode.ORDER_NOT_FOUND.defaultMessage());
        assertThat(customMessage).hasMessage("custom");
    }

    @Test
    void errorCodeShouldExposeStructuredMetadata() {
        assertThat(ErrorCode.AUTH_BAD_CREDENTIALS.code()).isEqualTo("AUTH-002");
        assertThat(ErrorCode.AUTH_BAD_CREDENTIALS.httpStatus()).isEqualTo(401);
        assertThat(ErrorCode.AUTH_BAD_CREDENTIALS.defaultMessage()).isEqualTo("Identifiants invalides");
        assertThat(ErrorCode.values()).hasSizeGreaterThan(10);
    }
}
