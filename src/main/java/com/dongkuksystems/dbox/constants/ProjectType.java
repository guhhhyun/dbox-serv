package com.dongkuksystems.dbox.constants;

public enum ProjectType {
  PROJECT("P", "프로젝트"), 
  RESEARCH("R", "연구과제")
  ;
  private String value;
  private String desc;

  private ProjectType(String value, String desc) {
    this.value = value;
    this.desc = desc;
  }

  public String getValue() {
    return value;
  }
  public String getDesc() {
    return desc;
  }

  public static ProjectType findByValue(String value) {
    for (ProjectType prType : ProjectType.values()) {
      if (prType.value.equals(value)) {
        return prType;
      }
    }
    throw new RuntimeException();
  }
}
