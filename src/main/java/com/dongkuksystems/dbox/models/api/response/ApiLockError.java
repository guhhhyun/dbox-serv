package com.dongkuksystems.dbox.models.api.response;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.http.HttpStatus;

public class ApiLockError extends ApiError {

  private String type;

  ApiLockError(ApiError apiError) {
    this(apiError.getMessage(), HttpStatus.valueOf(apiError.getStatus()), null);
  }
  
  ApiLockError(String message, HttpStatus status, String type) {
    super(message, status);
    this.type = type;
  }
  
  public String getType() {
    return type;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("message", super.getMessage()).append("type", type)
        .append("status", super.getStatus()).toString();
  }

}
