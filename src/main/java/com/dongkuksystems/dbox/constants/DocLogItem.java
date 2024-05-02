package com.dongkuksystems.dbox.constants;

public enum DocLogItem {
	RE("RE", "등록"),
	VE("VE", "조회"),
	DL("DL", "다운로드"),
  EO("EO", "편집"),
	ED("ED", "편집완료"),
	AP("AP", "결재"),
	CJ("CJ", "closed 처리"),
	CL("CL", "closed 자료변경 (C->L)"),
	DR("DR", "폐기 요청"),
	DA("DA", "폐기 승인"),
	DC("DC", "폐기 요청 취소"),
	UE("UE", "외부저장매체 반출"),
	ER("ER", "복호화 반출 요청"),
	EA("EA", "복호화 반출 승인"),
	TR("TR", "자료이관"),
	LV("LV", "Live 버전 갱신"),
	LD("LD", "Live 삭제"),
	LA("LA", "휴지통 자동 삭제"),
	LP("LP", "휴지통 수동 삭제"),
	SH("SH", "공유"),
	PR("PR", "권한신청"),
	PA("PA", "권한부여"),
	DM("DM", "자료 이동"),
	SC("SC", "보안 등급 변경"),
	DP("DP", "복사"),
	RC("RC", "보존연한 변경"),
	DS("DS", "자동 폐기"),
	RR("RR", "휴지통 문서 복원"),
	CC("CC", "잠금 문서 강제 해제"),
	AT("AT", "첨부")
    ;
  private String value;
  private String desc;

  private DocLogItem (String value, String desc) {
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
