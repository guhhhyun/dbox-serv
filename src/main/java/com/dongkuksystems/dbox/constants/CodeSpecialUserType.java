package com.dongkuksystems.dbox.constants;

public enum CodeSpecialUserType {
	USER("USER"),
	DEPT("DEPT"),
	JOB_POSITION("JOB_POSITION"),
	JOB_LEVEL("JOB_LEVEL"),
	JOB_TITLE("JOB_TITLE");

  private String value;

  private CodeSpecialUserType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
