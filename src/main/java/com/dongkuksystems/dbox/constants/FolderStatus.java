package com.dongkuksystems.dbox.constants;

public enum FolderStatus {
  ORDINARY("O", "일반"), 
  LOCK("C", "잠금");

  private String value;
  private String desc;

  private FolderStatus(String value, String desc) {
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
