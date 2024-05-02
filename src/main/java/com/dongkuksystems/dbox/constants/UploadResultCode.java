package com.dongkuksystems.dbox.constants;

public enum UploadResultCode {
  AUTH("업로드 권한 없음 : {0}건"), 
  LOCK("해당 폴더 및 상위 폴더가 잠금상태 : {0}건"),  
  EXT("업로드 제한대상(실행파일 및 압축파일) : {0}건"), 
  NAMELENGTH("폴더/파일명 길이 제한(영문240, 한글80) : {0}건"),
  ERROR("기타에러 : {0}건");

  private String value;

  private UploadResultCode(String value ) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  public static String findByName(String name) {
    for (UploadResultCode uploadResultCode : UploadResultCode.values()) {
      if (uploadResultCode.name().equals(name)) {
        return uploadResultCode.getValue();
      } 
    }
    return UploadResultCode.ERROR.value;
  }
}
