package com.dongkuksystems.dbox.constants;

public enum DataObjectType {
  FOLDER("F", "folder"), 
  PROJECT_FOLDER("P", "project folder"), 
  RESEARCH_FOLDER("R", "research folder"), 
  TEMPLATE_FILE("T", "template file");

  private String value;
  private String desc;

  private DataObjectType(String value, String desc) {
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
