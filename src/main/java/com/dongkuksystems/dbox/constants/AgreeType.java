package com.dongkuksystems.dbox.constants;

public enum AgreeType {
  AUTO("T", "부서장동의(자동승인)"), 
  FREE("U", "사용자동의(프리패스)");

  private String value;
  private String desc;

  private AgreeType(String value, String desc) {
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
