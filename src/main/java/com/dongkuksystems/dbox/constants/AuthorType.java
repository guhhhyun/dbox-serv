package com.dongkuksystems.dbox.constants;

public enum AuthorType {
  TEAM("D", "부서"), 
  COMPANY("C", "사"), 
  GROUP("G", "그룹"), 
  USER("U", "사용자"),
  DEFAULT("S", "기본권한그룹"),
  
  ALL("A", "모두조회용도(sql)");
  
  private String value;
  private String desc;

  private AuthorType(String value, String desc) {
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
