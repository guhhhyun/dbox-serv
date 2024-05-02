package com.dongkuksystems.dbox.constants;

import com.dongkuksystems.dbox.models.dto.type.auth.HamInfoResult;

public enum AuthType {  
  DEPT("DEPT", "부서 일반 (폴더 겸용)"),                        /**폴더 겸용**/                   
  DEPT_WF("DEPT_WF", "부서 WF (폴더 겸용)"),                  /**폴더 겸용**/
  DEPT_PERSONAL("DEPT_PERSONAL", "부서 개인정보"), 
  DEPT_PERSONAL_WF("DEPT_PERSONAL_WF", "뷰소 개인정보 WF"), 
  DEPT_IMP("DEPT_IMP", "부서 중요문서함 (폴더 겸용)"),            /**폴더 겸용**/
  PJT("PJT", "프로젝트 일반 (폴더 겸용)"),                       /**폴더 겸용**/
  PJT_WF("PJT_WF", "프로젝트 WF"), 
  PJT_PERSONAL("PJT_PERSONAL", "프로젝트 개인정보"), 
  PJT_PERSONAL_WF("PJT_PERSONAL_WF", "프로젝트 개인정보 WF"), 
  RSCH("RSCH", "전자결재 일반 (폴더 겸용)"),                      /**폴더 겸용**/
  RSCH_WF("RSCH_WF", "전자결재 WF"), 
  RSCH_PERSONAL("RSCH_PERSONAL", "전자결재 개인정보"), 
  RSCH_PERSONAL_WF("RSCH_PERSONAL_WF", "전자결재 개인정보 WF"), 
  COMPANY("COMPANY", "사별함 일반 (폴더 겸용)"),                 /**폴더 겸용**/
  C_DKS("C_DKS", "사별함 동국제강 관리직 (폴더 겸용)"),              /**폴더 겸용**/
  C_ITG("C_ITG", "사별함 인터지스 관리 (폴더 겸용)");               /**폴더 겸용**/

  private String value;
  private String desc;

  private AuthType(String value, String desc) {
    this.value = value;
    this.desc = desc;
  }

  public String getValue() {
    return value;
  }
  public String getDesc() {
    return desc;
  }

  /*
   *  일반 파일생성시 사용.
   */ 
  public static AuthType findAuthTypeForDocReg(HamInfoResult hamInfo) {
    switch (HamType.findByValue(hamInfo.getHamType())) {
    case DEPT:
      return AuthType.DEPT;
    case IMPORTANT:
      return AuthType.DEPT_IMP;
    case PROJECT:
      return AuthType.PJT;
    case RESEARCH:
      return AuthType.RSCH;
    case COMPANY:
      return AuthType.COMPANY;
    case COMPANY_M:
      if (ComCodeType.DKS.getValue().toLowerCase().equals(hamInfo.getComOrgId())) {
        return AuthType.C_DKS;
      } else {
        return AuthType.C_ITG;
      }
    default: 
      return AuthType.DEPT;
    }
  }
  
  public static AuthType findByValue(String value) {

    for (AuthType authType : AuthType.values()) {
      if (authType.value.equals(value)) {
        return authType;
      }
    }
    throw new RuntimeException("Auth type을 찾을 수없습니다.");
  }
}
