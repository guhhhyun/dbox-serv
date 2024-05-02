package com.dongkuksystems.dbox.constants;

public enum DeleteStatus {
  ING("R", "요청중"), 
  TRASH("D", "삭제(휴지통)"), 
  END("E", "삭제(보존연한)"),
  TEMP("T", "임시저장(Office 다른이름저장)");

  private String value;
  private String desc;

  private DeleteStatus(String value, String desc) {
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
