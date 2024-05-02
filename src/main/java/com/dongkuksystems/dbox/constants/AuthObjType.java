package com.dongkuksystems.dbox.constants;

public enum AuthObjType {
  DOCUMENT("D", "문서"), 
  FOLDER("F", "폴더"), 
  BACKUP("B", "문서권한백업");

  private String value;
  private String desc;

  private AuthObjType(String value, String desc) {
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
