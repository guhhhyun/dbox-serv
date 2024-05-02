package com.dongkuksystems.dbox.constants;

public enum DocStatus {
  LIVE("L", "live"), 
  CLOSED("C", "closed");

  private String value;
  private String desc;

  private DocStatus(String value, String desc) {
    this.value = value;
    this.desc = desc;
  }

  public String getValue() {
    return value;
  }
  
  public String getDesc() {
    return desc;
  }
  
  public static DocStatus findByValue(String value) {
    for (DocStatus docStatus : DocStatus.values()) {
      if (docStatus.value.equals(value)) {
        return docStatus;
      }
      if (docStatus.name().equals(value)) {
        return docStatus;
      }
    }
    throw new RuntimeException();
  }
}
