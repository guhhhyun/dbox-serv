package com.dongkuksystems.dbox.constants;

public enum DocType {
  NORMAL_DOC("N", "일반문서"), 
  APPROVAL_DOC("Y", "결재문서");

  private String value;
  private String desc;

  private DocType(String value, String desc) {
    this.value = value;
    this.desc = desc;
  }

  public String getValue() {
    return value;
  }
  public String getDesc() {
    return desc;
  }
}
