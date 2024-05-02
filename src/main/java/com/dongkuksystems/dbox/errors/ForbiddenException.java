package com.dongkuksystems.dbox.errors;


import com.dongkuksystems.dbox.utils.MessageUtils;


public class ForbiddenException extends ServiceRuntimeException {
	private static final String MESSAGE_KEY = "error.forbidden";
  private static final String MESSAGE_DETAIL = "error.forbidden.details";

  public ForbiddenException(String message) {
    super(MESSAGE_KEY, MESSAGE_DETAIL, new Object[] { message });
  }

  @Override
  public String getMessage() {
    return MessageUtils.getInstance().getMessage(getDetailKey(), getParams());
  }

  @Override
  public String toString() {
    return MessageUtils.getInstance().getMessage(getMessageKey());
  }

}

