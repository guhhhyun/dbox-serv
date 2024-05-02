package com.dongkuksystems.dbox.constants;

public enum HamSearchType {
  DEPT("D", "dept"), 
  PROJECT("P", "project"),
  RESEARCH("R", "연구과제"), 
  IMPORTANT("I", "중요문서함"),
  COMPANY("C", "사별함"),
  COMPANY_M("M", "사별함(관리직 함)"),
  RECYCLE("E", "휴지통");

  private String value;
  private String desc;

  private HamSearchType(String value, String desc) {
    this.value = value;
    this.desc = desc;
  }

  public String getValue() {
    return value;
  }
  
  public String getDesc() {
    return desc;
  }
  
  public static HamSearchType findByValue(String value) {
    for (HamSearchType hamType : HamSearchType.values()) {
      if (hamType.value.equals(value)) {
        return hamType;
      }
    }
    throw new RuntimeException("함을 찾을 수없습니다.");
  }
}
