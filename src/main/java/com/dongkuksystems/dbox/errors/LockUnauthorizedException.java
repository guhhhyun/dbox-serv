package com.dongkuksystems.dbox.errors;

import com.dongkuksystems.dbox.utils.MessageUtils;

public class LockUnauthorizedException extends ServiceRuntimeException {

  public static final String MESSAGE_KEY = "error.auth.lock";
  public static final String MESSAGE_DETAIL = "error.auth.lock-details";
  public boolean isMobile;
  
  public LockUnauthorizedException(String message, boolean isMobile) {
    super(MESSAGE_KEY, MESSAGE_DETAIL, new Object[] { message });
    this.isMobile = isMobile;
  }

  @Override
  public String getMessage() {
    return MessageUtils.getInstance().getMessage(getDetailKey(), getParams());
  }

  @Override
  public String toString() {
    return MessageUtils.getInstance().getMessage(getMessageKey());
  }

  public boolean isMobile() {
    return isMobile;
  }

  public void setMobile(boolean isMobile) {
    this.isMobile = isMobile;
  }

}