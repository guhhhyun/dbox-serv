package com.dongkuksystems.dbox.constants;

public enum DrmSecLevelType {
	GROUP("G", 16), 
  COMPANY("C", 1), 
  TEAM("T", 10), 
	INDIVIDUAL("S", 10),
  LIVE("L", 10),
  LIVE_CLOSED("LC", 10);
	//'S:제한,T:팀내,C:사내,G:그룹사내'),
  private String code;
  private int value;

  private DrmSecLevelType(String code, int value) {
    this.code = code;
    this.value = value;
  }

  public String getCode() {
    return code;
  }
  public int getValue() {
    return value;
  }
  
  public static DrmSecLevelType findByValue(String value) {
    for (DrmSecLevelType type : DrmSecLevelType.values()) {
      if (type.code.equals(value)) {
        return type;
      }
    }
    throw new RuntimeException();
  }
}
