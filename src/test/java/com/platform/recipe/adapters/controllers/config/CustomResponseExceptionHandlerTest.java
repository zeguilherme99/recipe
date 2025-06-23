package com.platform.recipe.adapters.controllers.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.platform.recipe.domain.exceptions.DataNotFoundException;
import com.platform.recipe.domain.exceptions.ErrorCode;
import com.platform.recipe.domain.exceptions.InvalidDataException;
import com.platform.recipe.domain.exceptions.UnexpectedErrorException;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;

@ExtendWith(MockitoExtension.class)
class CustomResponseExceptionHandlerTest {

  private CustomResponseExceptionHandler customResponseExceptionHandler;
  private String additionalInfo;
  private Throwable precedingException;

  @BeforeEach
  void beforeEach() {
    customResponseExceptionHandler = new CustomResponseExceptionHandler();
    additionalInfo = getAdditionalInfo();
    precedingException = getThrowable();
  }

  @Test
  void testDataNotFoundException() {
    DataNotFoundException dataNotFoundException = new DataNotFoundException(ErrorCode.INVALID_DATA);
    ResponseEntity<ResponseError.ResponseErrorMessage> responseError = customResponseExceptionHandler
      .dataNotFoundException(dataNotFoundException);

    HttpStatusCode httpStatusCode = responseError.getStatusCode();
    ResponseError.ResponseErrorMessage responseErrorMessage = responseError.getBody();

    assertEquals(HttpStatus.NOT_FOUND, httpStatusCode);
    assertEquals(ErrorCode.INVALID_DATA.getMessage(), responseErrorMessage.message());
    assertEquals(ErrorCode.INVALID_DATA.getTitle(), responseErrorMessage.title());
    assertEquals(ErrorCode.INVALID_DATA.getCode(), responseErrorMessage.code());
    assertTrue(responseErrorMessage.fieldErrors().isEmpty());
  }

  @Test
  void testException() {
    IOException ioException = new IOException(additionalInfo, precedingException);
    ResponseEntity<ResponseError.ResponseErrorMessage> responseError = customResponseExceptionHandler.exception(ioException);

    HttpStatusCode httpStatusCode = responseError.getStatusCode();
    ResponseError.ResponseErrorMessage responseErrorMessage = responseError.getBody();

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, httpStatusCode);
    assertEquals(ErrorCode.UNEXPECTED_ERROR.getMessage(), responseErrorMessage.message());
    assertEquals(ErrorCode.UNEXPECTED_ERROR.getTitle(), responseErrorMessage.title());
    assertEquals(ErrorCode.UNEXPECTED_ERROR.getCode(), responseErrorMessage.code());
    assertTrue(responseErrorMessage.fieldErrors().isEmpty());
  }

  @Test
  void testInvalidDataException() {
    InvalidDataException invalidDataException = new InvalidDataException(ErrorCode.INVALID_DATA);
    ResponseEntity<ResponseError.ResponseErrorMessage> responseError = customResponseExceptionHandler
      .invalidDataException(invalidDataException);

    HttpStatusCode httpStatusCode = responseError.getStatusCode();
    ResponseError.ResponseErrorMessage responseErrorMessage = responseError.getBody();

    assertEquals(HttpStatus.BAD_REQUEST, httpStatusCode);
    assertEquals(ErrorCode.INVALID_DATA.getMessage(), responseErrorMessage.message());
    assertEquals(ErrorCode.INVALID_DATA.getTitle(), responseErrorMessage.title());
    assertEquals(ErrorCode.INVALID_DATA.getCode(), responseErrorMessage.code());
    assertTrue(responseErrorMessage.fieldErrors().isEmpty());
  }

  @Test
  void testUnexpectedErrorException() {
    UnexpectedErrorException unexpectedErrorException = new UnexpectedErrorException(ErrorCode.UNEXPECTED_ERROR, precedingException);
    ResponseEntity<ResponseError.ResponseErrorMessage> responseError = customResponseExceptionHandler
      .unexpectedErrorException(unexpectedErrorException);

    HttpStatusCode httpStatusCode = responseError.getStatusCode();
    ResponseError.ResponseErrorMessage responseErrorMessage = responseError.getBody();

    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, httpStatusCode);
    assertEquals(ErrorCode.UNEXPECTED_ERROR.getMessage(), responseErrorMessage.message());
    assertEquals(ErrorCode.UNEXPECTED_ERROR.getTitle(), responseErrorMessage.title());
    assertEquals(ErrorCode.UNEXPECTED_ERROR.getCode(), responseErrorMessage.code());
    assertTrue(responseErrorMessage.fieldErrors().isEmpty());
  }

  @Test
  void testHandleHttpMessageNotReadable() {
    HttpMessageNotReadableException httpMessageNotReadableException = new HttpMessageNotReadableException(additionalInfo, precedingException, null);
    HttpHeaders headers = new HttpHeaders();
    HttpStatusCode httpStatusCode = HttpStatusCode.valueOf(400);
    ServletWebRequest webRequestMock = mock(ServletWebRequest.class);
    HttpServletRequest httpServletRequestMock = mock(HttpServletRequest.class);

    when(webRequestMock.getRequest()).thenReturn(httpServletRequestMock);
    when(httpServletRequestMock.getRequestURI()).thenReturn("/api/v1/test");

    ResponseEntity<Object> responseError = customResponseExceptionHandler
      .handleHttpMessageNotReadable(httpMessageNotReadableException, headers, httpStatusCode, webRequestMock);

    HttpStatusCode responseHttpStatusCode = responseError.getStatusCode();
    ResponseError.ResponseErrorMessage responseErrorMessage = (ResponseError.ResponseErrorMessage) responseError.getBody();

    assertEquals(httpStatusCode, responseHttpStatusCode);
    assertEquals(ErrorCode.INVALID_DATA.getMessage(), responseErrorMessage.message());
    assertEquals(ErrorCode.INVALID_DATA.getTitle(), responseErrorMessage.title());
    assertEquals(ErrorCode.INVALID_DATA.getCode(), responseErrorMessage.code());
    assertTrue(responseErrorMessage.fieldErrors().isEmpty());
  }

  @Test
  void testHandleMethodArgumentNotValid() {
    MethodArgumentNotValidException methodArgumentNotValidException = mock(MethodArgumentNotValidException.class);
    BindingResult bindingResult = mock(BindingResult.class);
    FieldError fieldError1 = mock(FieldError.class);
    FieldError fieldError12 = mock(FieldError.class);
    FieldError fieldError2 = mock(FieldError.class);
    ServletWebRequest webRequestMock = mock(ServletWebRequest.class);
    HttpServletRequest httpServletRequestMock = mock(HttpServletRequest.class);

    when(webRequestMock.getRequest()).thenReturn(httpServletRequestMock);
    when(httpServletRequestMock.getRequestURI()).thenReturn("/api/v1/test");

    when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
    when(bindingResult.getFieldErrors())
      .thenReturn(new ArrayList<>(Arrays.asList(fieldError1, fieldError12, fieldError2)));

    when(fieldError1.getField()).thenReturn("fieldError1");
    when(fieldError1.getDefaultMessage()).thenReturn("message 1 from fieldError1");

    when(fieldError12.getField()).thenReturn("fieldError1");
    when(fieldError12.getDefaultMessage()).thenReturn("message 2 from fieldError1");

    when(fieldError2.getField()).thenReturn("fieldError2");
    when(fieldError2.getDefaultMessage()).thenReturn("message 1 from fieldError2");

    HttpHeaders headers = new HttpHeaders();
    HttpStatusCode httpStatusCode = HttpStatusCode.valueOf(400);

    ResponseEntity<Object> responseError = customResponseExceptionHandler
      .handleMethodArgumentNotValid(methodArgumentNotValidException, headers, httpStatusCode, webRequestMock);

    HttpStatusCode responseHttpStatusCode = responseError.getStatusCode();
    ResponseError.ResponseErrorMessage responseErrorMessage = (ResponseError.ResponseErrorMessage) responseError.getBody();

    assertEquals(httpStatusCode, responseHttpStatusCode);
    assertEquals(ErrorCode.INVALID_DATA.getMessage(), responseErrorMessage.message());
    assertEquals(ErrorCode.INVALID_DATA.getTitle(), responseErrorMessage.title());
    assertEquals(ErrorCode.INVALID_DATA.getCode(), responseErrorMessage.code());

    List<ResponseError.FieldError> responseFieldErrors = responseErrorMessage.fieldErrors();

    assertEquals(2, responseFieldErrors.size());

    ResponseError.FieldError responseFieldError1 = responseFieldErrors.get(0);
    ResponseError.FieldError responseFieldError2 = responseFieldErrors.get(1);

    assertEquals("fieldError1", responseFieldError1.name());
    assertEquals("fieldError2", responseFieldError2.name());

    List<String> responseFieldError1Details1 = responseFieldError1.details();
    List<String> responseFieldError1Details2 = responseFieldError2.details();

    assertEquals(2, responseFieldError1Details1.size());
    assertEquals(1, responseFieldError1Details2.size());

    assertEquals("message 1 from fieldError1", responseFieldError1Details1.get(0));
    assertEquals("message 2 from fieldError1", responseFieldError1Details1.get(1));
    assertEquals("message 1 from fieldError2", responseFieldError1Details2.get(0));
  }

  private String getAdditionalInfo() {
    return "Any additional internal information about the error";
  }

  private Throwable getThrowable() {
    return new Throwable("any preceding exception");
  }
}