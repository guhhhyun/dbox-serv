package com.dongkuksystems.dbox.constants;

public enum NotiItem {
  FB_W("FB_W", "Feedback 등록", "등록 작성자 List"),
  FB_C("FB_C", "Feedback 댓글 등록", "작성자"),
  TR("TR", "자료 이관, 과제 주관부서 변경 ", "송수신 팀장"),
  SC("SC", "문서 보안 등급 변경", "소유 부서원"),
  OU("OU", "특이사용자 알림", "부서장, 담당임원, 전사문서관리자"),
  DR("DR", "Closed 폐기 요청 / 승인", "요청자 및 승인자"),
  ER("ER", "복호화 반출 요청 / 승인", "요청자 및 승인자"),
  UR("UR", "외부 저장매체 요청 / 승인", "요청자 및 승인자"),
  OR("OR", "특이사용자 해제 요청 / 승인", "요청자 및 승인자"),
  SR("SR", "제안문서 보안 등급 하향 요청 / 승인", "요청자 및 승인자"),
  PR("PR", "권한 신청 / 부여", "요청자 및 승인자"),
  DM("DM", "부서문서관리자 등록/해제", "해당 부서원"),  
  SH("SH", "공유 / 협업 연결 / 해제", "공유 / 협업 받은 부서 / 직원");

  private String value;
  private String desc;
  private String targetDesc;

  private NotiItem(String value, String desc, String targetDesc) {
    this.value = value;
    this.desc = desc;
    this.targetDesc = targetDesc;
  }

  public String getValue() {
    return value;
  }
  public String getDesc() {
    return desc;
  }
  public String getTargetDesc() {
    return targetDesc;
  }
  
  public static NotiItem findByValue(String value) {
    for (NotiItem notiItem : NotiItem.values()) {
      if (notiItem.value.equals(value)) {
        return notiItem;
      }
    }
    throw new RuntimeException();
  }
}
