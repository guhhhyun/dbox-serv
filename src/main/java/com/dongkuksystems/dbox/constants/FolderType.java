package com.dongkuksystems.dbox.constants;

public enum FolderType {
  DPC("DPC", "부서 문서함"), 
  DWF("DWF", "부서 전자결재 폴더"),
  DWT("DWT", "부서 전자결재 임시 폴더"),
  DWY("DWY", "부서 전자결재 년도 폴더"),       //폴더타입 -> 문서저장가능
  DFO("DFO", "부서 폴더"),                //폴더타입 -> 문서저장가능     
  
  DIM("DIM", "부서 중요문서함"),
  DIF("DIF", "부서 중요문서함 폴더"),         //폴더타입 -> 문서저장가능
  
  PJT("PJT", "프로젝트/투자"),
  POW("POW", "주관 프로젝트/투자"),
  PFN("PFN", "프로젝트 완료 구분"),
  PCL("PCL", "프로젝트 정리용 폴더"),      
  PJF("PJP", "프로젝트 문서함(진행)"),
  PJP("PJF", "프로젝트 문서함(완료)"),
  PFO("PFO", "프로젝트 폴더"),              //폴더타입 -> 문서저장가능
  PIC("PIC", "참여 프로젝트 문서함"),         //폴더타입 -> 문서저장가능

  RSC("RSC", "연구과제"),
  ROW("ROW", "주관 연구과제"),
  RFN("RFN", "연구과제 완료 구분"),
  RCL("RCL", "연구과제 정리용 폴더"),
  RSF("RSF", "연구과제 문서함(완료)"),
  RSP("RSP", "연구과제 문서함(진행)"),
  RFO("RFO", "연구과제 폴더 "),            //폴더타입 -> 문서저장가능
  RIC("RIC", "참여 프로젝트 문서함"),         //폴더타입 -> 문서저장가능

  SHR("SHR", "공유/협업"),
  SFO("SFO", "공유폴더"),
  EXP("EXP", "반출함"),
  RCY("RCY", "휴지통");
  
  private String value;
  private String desc;

  private FolderType(String value, String desc) {
    this.value = value;
    this.desc = desc;
  }

  public String getValue() {
    return value;
  }
  
  public String getDesc() {
    return desc;
  }
  
  public static FolderType findByValue(String value) {
    for (FolderType folderType : FolderType.values()) {
      if (folderType.value.equals(value)) {
        return folderType;
      }
    }
    throw new RuntimeException();
  }
}
