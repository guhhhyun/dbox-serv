package com.dongkuksystems.dbox.controllers;


import static com.dongkuksystems.dbox.models.api.response.ApiResult.ERROR;
import static com.dongkuksystems.dbox.models.api.response.ApiResult.FAIL;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.dongkuksystems.dbox.errors.BadRequestException;
import com.dongkuksystems.dbox.errors.DupleRequestException;
import com.dongkuksystems.dbox.errors.ForbiddenException;
import com.dongkuksystems.dbox.errors.LockUnauthorizedException;
import com.dongkuksystems.dbox.errors.NotFoundException;
import com.dongkuksystems.dbox.errors.ServiceRuntimeException;
import com.dongkuksystems.dbox.errors.UnauthorizedException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(NoHandlerFoundException.class)
  public ResponseEntity<?> handleNotFoundException(Exception e) {
    log.error("Handler not found exception occurred: {}", e.getMessage(), e);
    
    return newResponse(e, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler({
  	IllegalStateException.class, IllegalArgumentException.class,
    TypeMismatchException.class, HttpMessageNotReadableException.class,
    MissingServletRequestParameterException.class, MultipartException.class
  })
  public ResponseEntity<?> handleBadRequestException(Exception e) {
    log.error("Bad request exception occurred: {}", e.getMessage(), e);
    
    return newResponse(e, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(HttpMediaTypeException.class)
  public ResponseEntity<?> handleHttpMediaTypeException(Exception e) {
    log.error("Unsupported media type exception occurred: {}", e.getMessage(), e);
    
    return newResponse(e, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<?> handleMethodNotAllowedException(Exception e) {
    log.error("Method not allowed exception occurred: {}", e.getMessage(), e);
    
    return newResponse(e, HttpStatus.METHOD_NOT_ALLOWED);
  }
  
  @ExceptionHandler(ServiceRuntimeException.class)
  public ResponseEntity<?> handleServiceRuntimeException(ServiceRuntimeException e) {
    log.error("Unexpected service exception occurred: {}", e.getMessage(), e);
    
    if (e instanceof NotFoundException)
        return newResponse(e, HttpStatus.NOT_FOUND);
    if (e instanceof UnauthorizedException)
      return newResponse(e, HttpStatus.UNAUTHORIZED);
    if (e instanceof LockUnauthorizedException) {
      boolean isMobile = ((LockUnauthorizedException) e).isMobile;
      return newLockResponse(e, HttpStatus.FORBIDDEN, isMobile);
    }
    if (e instanceof BadRequestException)
      return newResponse(e, HttpStatus.BAD_REQUEST);
    if (e instanceof ForbiddenException)
  	  return newResponse(e, HttpStatus.FORBIDDEN);
    if (e instanceof DupleRequestException)
      return newResponse(e, HttpStatus.CONFLICT);

    return newResponse(e, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler({Exception.class})
  public ResponseEntity<?> handleException(Exception e) {
  	log.error("Unexpected exception occurred: {}", e.getMessage(), e);
    return newResponse(e, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  private ResponseEntity<?> newLockResponse(Throwable throwable, HttpStatus status, Boolean isMobile) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Type", "application/json"); 
    if (isMobile) {
      Map<String, Object> error = new HashMap<>();
      error.put("message", throwable.getMessage()); //"User is Locked or Banned" 
      error.put("status", HttpStatus.FORBIDDEN.value());
      return new ResponseEntity<>(FAIL(error), headers, status);
    } else {
      return new ResponseEntity<>(ERROR(throwable, status), headers, status);
    }
  }
  
  private ResponseEntity<?> newResponse(Throwable throwable, HttpStatus status) {
    HttpHeaders headers = new HttpHeaders();
    headers.add("Content-Type", "application/json");
    return new ResponseEntity<>(ERROR(throwable, status), headers, status);
  }
}