package com.dongkuksystems.dbox.constants;

public enum DboxObjectType {
  DOCUMENT("D", "document"), 
  FOLDER("F", "folder"),
  PROJECT("P", "project"),
  RESEARCH("R","research");

  private String value;
  private String desc;

  private DboxObjectType(String value, String desc) {
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
