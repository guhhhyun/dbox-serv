package com.dongkuksystems.dbox.constants;

public enum TakeoutType {
  PRE("P", "사전승인"), 
  AUTO("A", "자동승인"), 
  FREE("F", "프리패스");

  private String value;
  private String desc;

  private TakeoutType(String value, String desc) {
    this.value = value;
    this.desc = desc;
  }

  public String getValue() {
    return value;
  }
  
  public String getDesc() {
    return desc;
  }
  
  public static TakeoutType findByValue(String value) {
    for (TakeoutType takeoutType : TakeoutType.values()) {
      if (takeoutType.value.equals(value)) {
        return takeoutType;
      }
    }
    throw new RuntimeException();
  }
}
