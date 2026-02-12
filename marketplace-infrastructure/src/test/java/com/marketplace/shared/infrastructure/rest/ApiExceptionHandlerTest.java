package com.marketplace.shared.infrastructure.rest;

import com.marketplace.shared.domain.exception.BusinessException;
import com.marketplace.shared.domain.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Path;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.HttpInputMessage;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.lang.reflect.Method;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class ApiExceptionHandlerTest {

    private final ApiExceptionHandler handler = new ApiExceptionHandler();

    @Test
    void shouldMapBusinessAndSecurityExceptions() {
        ErrorResponse business = handler.handleBusiness(new BusinessException(ErrorCode.ORDER_NOT_FOUND)).getBody();
        ErrorResponse badCreds = handler.handleBadCredentials(new BadCredentialsException("bad")).getBody();
        ErrorResponse auth = handler.handleAuthentication(new AuthenticationException("missing") {}).getBody();
        ErrorResponse denied = handler.handleAccessDenied(new AccessDeniedException("forbidden")).getBody();

        assertThat(business).isNotNull();
        assertThat(business.code())
            .isEqualTo("ORD-001");
        assertThat(badCreds).isNotNull();
        assertThat(badCreds.code()).isEqualTo("AUTH-002");
        assertThat(auth).isNotNull();
        assertThat(auth.code()).isEqualTo("AUTH-001");
        assertThat(denied).isNotNull();
        assertThat(denied.code()).isEqualTo("AUTH-003");
    }

    @Test
    void shouldMapValidationExceptions() throws Exception {
        Method m = this.getClass().getDeclaredMethod("dummyMethod", String.class);
        HandlerMethod hm = new HandlerMethod(this, m);
        BeanPropertyBindingResult binding = new BeanPropertyBindingResult(new Object(), "obj");
        binding.addError(new FieldError("obj", "field", "must not be blank"));
        MethodArgumentNotValidException manve = new MethodArgumentNotValidException(hm.getMethodParameters()[0], binding);

        ErrorResponse validation = handler.handleValidation(manve).getBody();
        ErrorResponse missingParam = handler.handleMissingRequestParameter(
            new MissingServletRequestParameterException("eventId", "String")
        ).getBody();
        ErrorResponse missingPart = handler.handleMissingRequestPart(
            new MissingServletRequestPartException("file")
        ).getBody();
        ErrorResponse unreadable = handler.handleMessageNotReadable(
            new HttpMessageNotReadableException("bad body", Mockito.mock(HttpInputMessage.class))
        ).getBody();
        ErrorResponse illegalArg = handler.handleIllegalArgument(new IllegalArgumentException("bad arg")).getBody();
        ErrorResponse illegalState = handler.handleIllegalState(new IllegalStateException("bad state")).getBody();

        assertThat(validation).isNotNull();
        assertThat(validation.code()).isEqualTo("GEN-001");
        assertThat(missingParam).isNotNull();
        assertThat(missingParam.message()).contains("eventId");
        assertThat(missingPart).isNotNull();
        assertThat(missingPart.message()).contains("file");
        assertThat(unreadable).isNotNull();
        assertThat(unreadable.message()).isEqualTo("Corps de requete invalide");
        assertThat(illegalArg).isNotNull();
        assertThat(illegalArg.code()).isEqualTo("GEN-001");
        assertThat(illegalState).isNotNull();
        assertThat(illegalState.code()).isEqualTo("GEN-409");
    }

    @Test
    void shouldMapConstraintAndGenericExceptions() {
        @SuppressWarnings("unchecked")
        ConstraintViolation<Object> violation = Mockito.mock(ConstraintViolation.class);
        Path propertyPath = Mockito.mock(Path.class);
        when(propertyPath.toString()).thenReturn("payload.field");
        when(violation.getPropertyPath()).thenReturn(propertyPath);
        when(violation.getMessage()).thenReturn("must not be null");
        ConstraintViolationException cve = new ConstraintViolationException(Set.of(violation));
        ErrorResponse constraint = handler.handleConstraintValidation(cve).getBody();
        assertThat(constraint).isNotNull();
        assertThat(constraint.message())
            .contains("payload.field")
            .contains("must not be null");

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/test");
        ErrorResponse generic = handler.handleGeneric(new RuntimeException("boom"), request).getBody();
        assertThat(generic).isNotNull();
        assertThat(generic.code()).isEqualTo("GEN-999");
    }

    @SuppressWarnings("unused")
    private void dummyMethod(String value) {
    }
}
