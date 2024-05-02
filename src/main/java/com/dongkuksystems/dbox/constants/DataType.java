package com.dongkuksystems.dbox.constants;

public enum DataType {
  FOLDER("F", "folder"), 
  PROJECT_FOLDER("P", "project folder"),
  RESEARCH_FOLDER("R", "research folder"),
  TEMPLATE_FILE("T", "template file"),
  ERROR("E", "에러")
  ;
	
  private String value;
  private String desc;

  private DataType(String value, String desc) {
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
