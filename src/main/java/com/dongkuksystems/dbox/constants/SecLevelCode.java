package com.dongkuksystems.dbox.constants;

public enum SecLevelCode {
  LIVE("L", "라이브(서버 권한 로직 용도)", -1), 
  
  SEC("S", "제한", 0), 
  TEAM("T", "팀내", 1),
  COMPANY("C", "사내", 2),
  GROUP("G", "그룹사내", 3);

  private String value;
  private String desc;
  private int order;

  private SecLevelCode(String value, String desc, int order) {
    this.value = value;
    this.desc = desc;
    this.order = order;
  }

  public String getValue() {
    return value;
  }
  
  public String getDesc() {
    return desc;
  }
  
  public int getOrder() {
    return order;
  }
  
  public static SecLevelCode compareSecs(String a, String b) {
    SecLevelCode from = SecLevelCode.findByValue(a);
    SecLevelCode to   = SecLevelCode.findByValue(b);
    
    if (from.order > to.order) return to;
    else return from;
  }
  
  public static SecLevelCode findByValue(String value) {
    for (SecLevelCode secLevelCode : SecLevelCode.values()) {
      if (secLevelCode.value.equals(value)) {
        return secLevelCode;
      }
      if (secLevelCode.name().equals(value)) {
        return secLevelCode;
      }
    }
    throw new RuntimeException();
  }
}
