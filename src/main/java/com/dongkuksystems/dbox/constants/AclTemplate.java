package com.dongkuksystems.dbox.constants;

public enum AclTemplate {
  DEFAULT("a_{0}_{1}_{2}_{3}_{4}", "기본구조"),
  COMMON_ACL("a_{0}", "사별 공용문서함"),
  DKSM_COMMON_ACL("a_dksm", "동국제강-관리직 공용문서함"),
  ITG_COMMON_ACL("a_itg00006350", "인터지스-관리 공용문서함"),
  
  //부서 일반문서 기본 ACL 정의
  DEPT_BASIC_ACL_LIVE("a_{0}_d_g_l_n", "부서한(라이브)"), 
  DEPT_BASIC_ACL_SEC("a_{0}_d_g_s_n", "제한"),
  DEPT_BASIC_ACL_TEAM("a_{0}_d_g_t_n", "팀내"),
  DEPT_BASIC_ACL_COM("a_{0}_d_g_c_n", "사내"),
  DEPT_BASIC_ACL_GROUP("a_{0}_d_g_g_n", "그룹사내"),
  //부서 전자결재문서 기본 ACL 정의
  DEPT_WF_ACL_SEC("a_{0}_d_g_s_y", "제한"),
  DEPT_WF_ACL_TEAM("a_{0}_d_g_t_y", "팀내"),
  DEPT_WF_ACL_COM("a_{0}_d_g_c_y", "사내"),
  DEPT_WF_ACL_GROUP("a_{0}_d_g_g_y", "그룹사내"),
  //부서 일반문서(개인정보 포함) 기본 ACL 정의
  DEPT_PRIVACY_ACL_LIVE("a_{0}_d_p_l_n", "부서한(라이브)"),
  DEPT_PRIVACY_ACL_SEC("a_{0}_d_p_s_n", "제한"),
  //부서 전자결재문서(개인정보 포함) 기본 ACL 정의
  DEPT_PRIVACY_WF_ACL_LIVE("a_{0}_d_p_l_y", "부서한(라이브)"),
  DEPT_PRIVACY_WF_ACL_SEC("a_{0}_d_p_s_y", "제한"),
  //부서 중요문서 기본 ACL 정의
  DEPT_IMP_ACL("a_{0}_s_g_s_n", "제한"),

  //프로젝트 일반문서 기본 ACL 정의
  PJT_BASIC_ACL_LIVE("a_{0}_p_g_l_n", "부서한(라이브)"), 
  PJT_BASIC_ACL_SEC("a_{0}_p_g_s_n", "제한"),
  PJT_BASIC_ACL_TEAM("a_{0}_p_g_t_n", "팀내"),
  PJT_BASIC_ACL_COM("a_{0}_p_g_c_n", "사내"),
  PJT_BASIC_ACL_GROUP("a_{0}_p_g_g_n", "그룹사내"),
  //프로젝트 전자결재문서 기본 ACL 정의
  PJT_WF_ACL_SEC("a_{0}_p_g_s_y", "제한"),
  PJT_WF_ACL_TEAM("a_{0}_p_g_t_y", "팀내"),
  PJT_WF_ACL_COM("a_{0}_p_g_c_y", "사내"),
  PJT_WF_ACL_GROUP("a_{0}_p_g_g_y", "그룹사내"),
  //프로젝트 일반문서(개인정보 포함) 기본 ACL 정의
  PJT_PRIVACY_ACL_LIVE("a_{0}_p_p_l_n", "부서한(라이브)"),
  PJT_PRIVACY_ACL_SEC("a_{0}_p_p_s_n", "제한"),
  //프로젝트 전자결재문서(개인정보 포함) 기본 ACL 정의
  PJT_PRIVACY_WF_ACL_LIVE("a_{0}_p_p_l_y", "부서한(라이브)"),
  PJT_PRIVACY_WF_ACL_SEC("a_{0}_p_p_s_y", "제한"),

  //연구과제 일반문서 기본 ACL 정의
  RSCH_BASIC_ACL_LIVE("a_{0}_p_g_l_n", "부서한(라이브)"), 
  RSCH_BASIC_ACL_SEC("a_{0}_p_g_s_n", "제한"),
  RSCH_BASIC_ACL_TEAM("a_{0}_p_g_t_n", "팀내"),
  RSCH_BASIC_ACL_COM("a_{0}_p_g_c_n", "사내"),
  RSCH_BASIC_ACL_GROUP("a_{0}_p_g_g_n", "그룹사내"),
  //연구과제 전자결재문서 기본 ACL 정의
  RSCH_WF_ACL_SEC("a_{0}_r_g_s_y", "제한"),
  RSCH_WF_ACL_TEAM("a_{0}_r_g_t_y", "팀내"),
  RSCH_WF_ACL_COM("a_{0}_r_g_c_y", "사내"),
  RSCH_WF_ACL_GROUP("a_{0}_r_g_g_y", "그룹사내"),
  //연구과제 일반문서(개인정보 포함) 기본 ACL 정의
  RSCH_PRIVACY_ACL_LIVE("a_{0}_r_p_l_n", "부서한(라이브)"),
  RSCH_PRIVACY_ACL_SEC("a_{0}_r_p_s_n", "제한"),
  //연구과제 전자결재문서(개인정보 포함) 기본 ACL 정의
  RSCH_PRIVACY_WF_ACL_LIVE("a_{0}_r_p_l_y", "부서한(라이브)"),
  RSCH_PRIVACY_WF_ACL_SEC("a_{0}_r_p_s_y", "제한"),
  
  TEST("a_{0}_d_g_c_n", "그룹사내");

  private String value;
  private String desc;

  private AclTemplate(String value, String desc) {
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
