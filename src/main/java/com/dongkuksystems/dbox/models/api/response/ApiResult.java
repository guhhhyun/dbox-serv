package com.dongkuksystems.dbox.models.api.response;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.http.HttpStatus;

public class ApiResult<T> {

  private final boolean success;

  private final T response;

  private final ApiLockError error;

  private ApiResult(boolean success, T response, ApiError error) {
      this.success = success;
      this.response = response;
      this.error = new ApiLockError(error);
  }
  
  private ApiResult(boolean success, T response, ApiLockError error) {
    this.success = success;
    this.response = response;
    this.error = error;
  }

  public static <T> ApiResult<T> OK(T response) {
      return new ApiResult<>(true, response, null);
  }

  public static <T> ApiResult<T> FAIL(T response) {
      return new ApiResult<>(false, response, null);
  }
  
  public static ApiResult<?> ERROR(Throwable throwable, HttpStatus status) {
      return new ApiResult<>(false, null, new ApiError(throwable, status));
  }

  public static ApiResult<?> ERROR(String errorMessage, HttpStatus status) {
      return new ApiResult<>(false, null, new ApiError(errorMessage, status));
  }
  
  public static ApiResult<?> LOCK_ERROR(String errorMessage, String type, HttpStatus status) {
    return new ApiResult<>(false, null, new ApiLockError(errorMessage, status, "L"));
  }

  public boolean isSuccess() {
      return success;
  }

  public ApiLockError getError() {
      return error;
  }

  public T getResponse() {
      return response;
  }

  @Override
  public String toString() {
      return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
              .append("success", success)
              .append("response", response)
              .append("error", error)
              .toString();
  }
}