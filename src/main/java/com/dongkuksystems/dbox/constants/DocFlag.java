package com.dongkuksystems.dbox.constants;

public enum DocFlag {
  GENERAL("G", "일반문서"), 
  PERSONAL("P", "개인정보문서");

  private String value;
  private String desc;

  private DocFlag(String value, String desc) {
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
