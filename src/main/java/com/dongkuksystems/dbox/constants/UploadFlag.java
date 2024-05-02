package com.dongkuksystems.dbox.constants;

public enum UploadFlag {
  NEW_D("D", "dbox 신규생성 (서버전용), api param에선 사용하지 않음"), 
  NEW_P("P", "pc 신규생성 (서버전용), api param에선 사용하지 않음"), 
  OVERWIRTE("W", "건너띄기"), 
  SKIP("S", "건너띄기"), 
  VERSION("V", "버전갱신"), 
  COPY("C", "복사본 추가");

  private String value;
  private String desc;

  private UploadFlag(String value, String desc) {
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
