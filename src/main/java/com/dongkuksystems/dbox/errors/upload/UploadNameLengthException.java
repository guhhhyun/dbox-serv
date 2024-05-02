package com.dongkuksystems.dbox.errors.upload;

import com.dongkuksystems.dbox.errors.ServiceRuntimeException;
import com.dongkuksystems.dbox.utils.MessageUtils;


public class UploadNameLengthException extends ServiceRuntimeException {
	private static final long serialVersionUID = -4540234967449872763L;

  private static final String MESSAGE_KEY = "error.uploadNameLength";
  private static final String MESSAGE_DETAIL = "error.uploadNameLength.details";

  public UploadNameLengthException(String message) {
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

