package com.dongkuksystems.dbox.constants;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DCTMConstants {

	public static final String DOCBASE 			  = "dkdoc";
	public static final String DCTM_ADMIN_ID 	= "dmadmin";
	public static final String DCTM_GLOBAL_PW = "__EDM_SUPERPW__";
	public static final String DCTM_BLANK 		= " ";
	public static final String ACL_PREFIX 		= "a_";
	public static final String DATE_FORMAT 		= "yyyy-MM-dd HH:mm:ss";
	public static final String VERSIONUP_TEMPLATE 	= "{0},CURRENT";

	public static final String DKG     = "dkg";
  public static final String DKSM     = "dksm";
  public static final String ITG00006350     = "itg00006350";
  public static final String DEFAULT_CABINET_STR     = "[DEFAULT_CABINET]";
  public static final String DEFAULT_COMCODE_STR     = "[DEFAULT_COMCODE]";
  public static final String DEFAULT_GROUP_STR     = "[DEFAULT_GROUP]";
	public static final String COM_CODE_STR 		= "[COM_CODE]";
	public static final String CABINET_CODE_STR 		= "[CABINET_CODE]";
	public static final String PJT_CODE_STR 		= "[PJT_CODE]";
	public static final String RSCH_CODE_STR 		= "[RSCH_CODE]";
	
	
  @SuppressWarnings("serial")
  public static final Map<String, Integer> DEFAULT_TEAM_AUTH_GROUP_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_chairman", 7);
//          put("g_ent_mgr", "2");
        }
      });
  @SuppressWarnings("serial")
  public static final Map<String, Integer> DEFAULT_TEAM_AUTH_COM_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_{0}_mgr_g_a", 7);
          put("g_{0}_mgr_g_b", 7);
          put("g_{0}_audit_b", 3);
          put("g_{0}_audit_b", 3);
        }
      });
  @SuppressWarnings("serial")
  public static final Map<String, Integer> DEFAULT_AUTH_COM_HAM_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_{0}_mgr_g_a", 7);
          put("g_{0}_mgr_g_b", 7);
//          put("g_{0}", 7);
        }
      });
  @SuppressWarnings("serial")
  public static final Map<String, Integer> DEFAULT_AUTH_COM_DKS_HAM_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_{0}_mgr_g_a", 7);
          put("g_{0}_mgr_g_b", 7);
          put("g_dksm", 7);
//          put("g_{0}", 7);
        }
      });
  @SuppressWarnings("serial")
  public static final Map<String, Integer> DEFAULT_AUTH_COM_ITG_HAM_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_{0}_mgr_g_a", 7);
          put("g_{0}_mgr_g_b", 7);
          put("g_itg00006350", 7);
//          put("g_{0}", 7);
        }
      });
  @SuppressWarnings("serial")
  public static final Map<String, Integer> DEFAULT_TEAM_AUTH_COM_PROJECT_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_{0}_pjtmgr", 3);
        }
      });
  @SuppressWarnings("serial")
  public static final Map<String, Integer> DEFAULT_TEAM_AUTH_COM_RESEARCH_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_{0}_rschmgr", 3);
        }
      });
  @SuppressWarnings("serial")
  public static final Map<String, Integer> DEFAULT_AUDIT_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_audit_wf", 3);
        }
      });
  @SuppressWarnings("serial")
  public static final Map<String, Integer> DEFAULT_TEAM_AUTH_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_{0}_imwon", 7);
          put("g_{0}_chief", 7);
          put("g_{0}_mgr_g_a", 7);
          put("g_{0}_mgr_g_b", 7);
          put("g_{0}_old", 7);
        }
      });
  @SuppressWarnings("serial")
  public static final Map<String, Integer> DEFAULT_TEAM_AUTH_TEAM_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_{0}", 7);
        }
      });
  
  
  
  //폴더 시작
  //부서 일반
  @SuppressWarnings("serial")
  public static final Map<String, Integer> TEAM_FOLDER_AUTH_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_chairman", 7);
          put("g_[COM_CODE]_mgr_g_a", 7);
          put("g_[COM_CODE]_mgr_g_b", 7);
          put("g_[CABINET_CODE]_imwon", 7);
          put("g_[CABINET_CODE]_chief", 7);
          put("g_[CABINET_CODE]_mgr_g_a", 7);
          put("g_[CABINET_CODE]_mgr_g_b", 7);
          put("g_[CABINET_CODE]_old", 7);
          put("g_[DEFAULT_CABINET]", 7);
        }
      });

  //부서 중요문서
  @SuppressWarnings("serial")
  public static final Map<String, Integer> TEAM_FOLDER_IMP_AUTH_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_[CABINET_CODE]_chief", 7);
          put("g_[CABINET_CODE]_old", 7);
          put("g_[DEFAULT_CABINET]", 7);
        }
      });
  
  //부서 일반_WF
  @SuppressWarnings("serial")
  public static final Map<String, Integer> TEAM_FOLDER_WF_AUTH_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_chairman", 7);
          put("g_audit_wf", 3);
          put("g_[COM_CODE]_mgr_g_a", 7);
          put("g_[COM_CODE]_mgr_g_b", 7);
          put("g_[CABINET_CODE]_imwon", 7);
          put("g_[CABINET_CODE]_chief", 7);
          put("g_[CABINET_CODE]_mgr_g_a", 7);
          put("g_[CABINET_CODE]_mgr_g_b", 7);
          put("g_[CABINET_CODE]_old", 7);
          put("g_[DEFAULT_CABINET]", 7);
        }
      });

  //프로젝트 폴더
  @SuppressWarnings("serial")
  public static final Map<String, Integer> PJT_FOLDER_AUTH_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_chairman", 7);
          put("g_[COM_CODE]_mgr_g_a", 7);
          put("g_[COM_CODE]_mgr_g_b", 7);
          put("g_[COM_CODE]_pjtmgr", 7);
          put("g_[CABINET_CODE]_imwon", 7);
          put("g_[CABINET_CODE]_chief", 7);
          put("g_[CABINET_CODE]_mgr_g_a", 7);
          put("g_[CABINET_CODE]_mgr_g_b", 7);
          put("g_[CABINET_CODE]_old", 7);
          put("g_[DEFAULT_CABINET]", 7);
          put("g_[PJT_CODE]_pjtmgr", 7);
        }
      });

  //연구과제 일반
  @SuppressWarnings("serial")
  public static final Map<String, Integer> RSCH_FOLDER_AUTH_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_chairman", 7);
          put("g_[COM_CODE]_mgr_g_a", 7);
          put("g_[COM_CODE]_mgr_g_b", 7);
          put("g_[COM_CODE]_rschmgr", 7);
          put("g_[CABINET_CODE]_imwon", 7);
          put("g_[CABINET_CODE]_chief", 7);
          put("g_[CABINET_CODE]_mgr_g_a", 7);
          put("g_[CABINET_CODE]_mgr_g_b", 7);
          put("g_[CABINET_CODE]_old", 7);
          put("g_[DEFAULT_CABINET]", 7);
          put("g_[RSCH_CODE]_rschmgr", 7);
        }
      });
  //사별
  @SuppressWarnings("serial")
  public static final Map<String, Integer> COM_FOLDER_HAM_AUTH_MAP = Collections
  .unmodifiableMap(new HashMap<String, Integer>() {
    {
      put("g_chairman", 7); 
      put("g_[COM_CODE]_mgr_g_a", 7);
      put("g_[COM_CODE]_mgr_g_b", 7);  
      put("g_[COM_CODE]", 7);  
      put("g_[DEFAULT_CABINET]", 7);
    }
  });
  //제강 관리직
  @SuppressWarnings("serial")
  public static final Map<String, Integer> DEFAULT_FOLDER_COM_DKS_HAM_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_chairman", 7); 
          put("g_[COM_CODE]_mgr_g_a", 7);
          put("g_[COM_CODE]_mgr_g_b", 7);
          put("g_dksm", 7);
          put("g_[DEFAULT_CABINET]", 7);
        }
      });
  //인터지스 관리
  @SuppressWarnings("serial")
  public static final Map<String, Integer> DEFAULT_FOLDER_COM_ITG_HAM_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_chairman", 7); 
          put("g_[COM_CODE]_mgr_g_a", 7);
          put("g_[COM_CODE]_mgr_g_b", 7);
          put("g_itg00006350", 7);
          put("g_[DEFAULT_CABINET]", 7);
        }
      });
  //폴더 끝
      
   
  
  
  
  
  
  //문서
  //부서 일반
  @SuppressWarnings("serial")
  public static final Map<String, Integer> TEAM_DEFAULT_LIVE_AUTH_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_chairman", 7);
          put("g_[COM_CODE]_mgr_g_a", 7);
          put("g_[COM_CODE]_mgr_g_b", 7);
          put("g_[CABINET_CODE]_imwon", 7);
          put("g_[CABINET_CODE]_chief", 7);
          put("g_[CABINET_CODE]_mgr_g_a", 7);
          put("g_[CABINET_CODE]_mgr_g_b", 7);
          put("g_[CABINET_CODE]_old", 7);
          put("g_[DEFAULT_CABINET]", 7);
        }
      });

  @SuppressWarnings("serial")
  public static final Map<String, Integer> TEAM_DEFAULT_CLOSED_S_AUTH_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_chairman", 7);
          put("g_[COM_CODE]_mgr_g_a", 7);
          put("g_[COM_CODE]_mgr_g_b", 7);
          put("g_[CABINET_CODE]_imwon", 7);
          put("g_[CABINET_CODE]_chief", 7);
          put("g_[CABINET_CODE]_mgr_g_a", 7);
          put("g_[CABINET_CODE]_mgr_g_b", 7);
        }
      });
  @SuppressWarnings("serial")
  public static final Map<String, Integer> TEAM_DEFAULT_CLOSED_T_AUTH_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_chairman", 7);
          put("g_[COM_CODE]_mgr_g_a", 7);
          put("g_[COM_CODE]_mgr_g_b", 7);
          put("g_[CABINET_CODE]_imwon", 7);
          put("g_[CABINET_CODE]_chief", 7);
          put("g_[CABINET_CODE]_mgr_g_a", 7);
          put("g_[CABINET_CODE]_mgr_g_b", 7);
          put("g_[CABINET_CODE]_old", 7);
          put("g_[DEFAULT_CABINET]", 7);
        }
      });
  @SuppressWarnings("serial")
  public static final Map<String, Integer> TEAM_DEFAULT_CLOSED_C_AUTH_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_chairman", 7);
          put("g_[COM_CODE]_mgr_g_a", 7);
          put("g_[COM_CODE]_mgr_g_b", 7);
          put("g_[CABINET_CODE]_imwon", 7);
          put("g_[CABINET_CODE]_chief", 7);
          put("g_[CABINET_CODE]_mgr_g_a", 7);
          put("g_[CABINET_CODE]_mgr_g_b", 7);
          put("g_[CABINET_CODE]_old", 7);
          put("g_[DEFAULT_COMCODE]", 7);
        }
      });
  @SuppressWarnings("serial")
  public static final Map<String, Integer> TEAM_DEFAULT_CLOSED_G_AUTH_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_chairman", 7);
          put("g_[COM_CODE]_mgr_g_a", 7);
          put("g_[COM_CODE]_mgr_g_b", 7);
          put("g_[CABINET_CODE]_imwon", 7);
          put("g_[CABINET_CODE]_chief", 7);
          put("g_[CABINET_CODE]_mgr_g_a", 7);
          put("g_[CABINET_CODE]_mgr_g_b", 7);
          put("g_[CABINET_CODE]_old", 7);
          put("g_[DEFAULT_GROUP]", 7);  
        }
      });

  //부서 일반_WF
  @SuppressWarnings("serial")
  public static final Map<String, Integer> TEAM_WF_CLOSED_S_AUTH_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_chairman", 7);
          put("g_audit_wf", 3);
          put("g_[COM_CODE]_mgr_g_a", 7);
          put("g_[COM_CODE]_mgr_g_b", 7);
          put("g_[CABINET_CODE]_imwon", 7);
          put("g_[CABINET_CODE]_chief", 7);
          put("g_[CABINET_CODE]_mgr_g_a", 7);
          put("g_[CABINET_CODE]_mgr_g_b", 7);
        }
      });
  @SuppressWarnings("serial")
  public static final Map<String, Integer> TEAM_WF_CLOSED_T_AUTH_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_chairman", 7);
          put("g_audit_wf", 3);
          put("g_[COM_CODE]_mgr_g_a", 7);
          put("g_[COM_CODE]_mgr_g_b", 7);
          put("g_[CABINET_CODE]_imwon", 7);
          put("g_[CABINET_CODE]_chief", 7);
          put("g_[CABINET_CODE]_mgr_g_a", 7);
          put("g_[CABINET_CODE]_mgr_g_b", 7);
          put("g_[CABINET_CODE]_old", 7);
          put("g_[DEFAULT_CABINET]", 7);
        }
      });
  @SuppressWarnings("serial")
  public static final Map<String, Integer> TEAM_WF_CLOSED_C_AUTH_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_chairman", 7);
          put("g_audit_wf", 3);
          put("g_[COM_CODE]_mgr_g_a", 7);
          put("g_[COM_CODE]_mgr_g_b", 7);
          put("g_[CABINET_CODE]_imwon", 7);
          put("g_[CABINET_CODE]_chief", 7);
          put("g_[CABINET_CODE]_mgr_g_a", 7);
          put("g_[CABINET_CODE]_mgr_g_b", 7);
          put("g_[CABINET_CODE]_old", 7);
          put("g_[DEFAULT_COMCODE]", 7);  
        }
      });
  @SuppressWarnings("serial")
  public static final Map<String, Integer> TEAM_WF_CLOSED_G_AUTH_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_chairman", 7);
          put("g_audit_wf", 3);
          put("g_[COM_CODE]_mgr_g_a", 7);
          put("g_[COM_CODE]_mgr_g_b", 7);
          put("g_[CABINET_CODE]_imwon", 7);
          put("g_[CABINET_CODE]_chief", 7);
          put("g_[CABINET_CODE]_mgr_g_a", 7);
          put("g_[CABINET_CODE]_mgr_g_b", 7);
          put("g_[CABINET_CODE]_old", 7);
          put("g_[DEFAULT_GROUP]", 7);  
        }
      });

  //부서 개인정보
  @SuppressWarnings("serial")
  public static final Map<String, Integer> TEAM_PERSONAL_LIVE_T_AUTH_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_chairman", 7);
          put("g_[COM_CODE]_mgr_p_a", 2);
          put("g_[CABINET_CODE]_imwon", 7);
          put("g_[CABINET_CODE]_chief", 7);
          put("g_[CABINET_CODE]_mgr_p_a", 2);
        }
      });
  @SuppressWarnings("serial")
  public static final Map<String, Integer> TEAM_PERSONAL_CLOSED_S_AUTH_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_chairman", 7);
          put("g_[COM_CODE]_mgr_p_a", 2);
          put("g_[CABINET_CODE]_imwon", 7);
          put("g_[CABINET_CODE]_chief", 7);
          put("g_[CABINET_CODE]_mgr_p_a", 2);
        }
      });
  
  //부서 개인정보 WF
  @SuppressWarnings("serial")
  public static final Map<String, Integer> TEAM_PERSONAL_WF_LIVE_T_AUTH_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_chairman", 7);
          put("g_audit_wf_p_b", 3);
          put("g_[COM_CODE]_mgr_p_a", 2);
          put("g_[CABINET_CODE]_imwon", 7);
          put("g_[CABINET_CODE]_chief", 7);
          put("g_[CABINET_CODE]_mgr_p_a", 2);
        }
      });
  @SuppressWarnings("serial")
  public static final Map<String, Integer> TEAM_PERSONAL_WF_CLOSED_S_AUTH_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_chairman", 7);
          put("g_audit_wf_p_b", 3);
          put("g_[COM_CODE]_mgr_p_a", 2);
          put("g_[CABINET_CODE]_imwon", 7);
          put("g_[CABINET_CODE]_chief", 7);
          put("g_[CABINET_CODE]_mgr_p_a", 2);
        }
      });

  //부서 중요문서
  @SuppressWarnings("serial")
  public static final Map<String, Integer> TEAM_IMP_AUTH_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_[CABINET_CODE]_chief", 7);
          put("g_[CABINET_CODE]_mgr_p_a", 7);
        }
      });
  
  
  //프로젝트 일반
  @SuppressWarnings("serial")
  public static final Map<String, Integer> PJT_DEFAULT_LIVE_AUTH_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_chairman", 7);
          put("g_[COM_CODE]_mgr_g_a", 7);
          put("g_[COM_CODE]_mgr_g_b", 7);
          put("g_[COM_CODE]_pjtmgr", 7);
          put("g_[CABINET_CODE]_imwon", 7);
          put("g_[CABINET_CODE]_chief", 7);
          put("g_[CABINET_CODE]_mgr_g_a", 7);
          put("g_[CABINET_CODE]_mgr_g_b", 7);
          put("g_[CABINET_CODE]_old", 7);
          put("g_[DEFAULT_CABINET]", 7);
        }
      });
  @SuppressWarnings("serial")
  public static final Map<String, Integer> PJT_DEFAULT_CLOSED_S_AUTH_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_chairman", 7);
          put("g_[COM_CODE]_mgr_g_a", 7);
          put("g_[COM_CODE]_mgr_g_b", 7);
          put("g_[CABINET_CODE]_imwon", 7);
          put("g_[CABINET_CODE]_chief", 7);
          put("g_[CABINET_CODE]_mgr_g_a", 7);
          put("g_[CABINET_CODE]_mgr_g_b", 7);
        }
      });
  @SuppressWarnings("serial")
  public static final Map<String, Integer> PJT_DEFAULT_CLOSED_T_AUTH_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_chairman", 7);
          put("g_[COM_CODE]_mgr_g_a", 7);
          put("g_[COM_CODE]_mgr_g_b", 7);
          put("g_[COM_CODE]_pjtmgr", 7);
          put("g_[CABINET_CODE]_imwon", 7);
          put("g_[CABINET_CODE]_chief", 7);
          put("g_[CABINET_CODE]_mgr_g_a", 7);
          put("g_[CABINET_CODE]_mgr_g_b", 7);
          put("g_[CABINET_CODE]_old", 7);
          put("g_[DEFAULT_CABINET]", 7);
        }
      });
  @SuppressWarnings("serial")
  public static final Map<String, Integer> PJT_DEFAULT_CLOSED_C_AUTH_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_chairman", 7);
          put("g_[COM_CODE]_mgr_g_a", 7);
          put("g_[COM_CODE]_mgr_g_b", 7);
          put("g_[COM_CODE]_pjtmgr", 7);
          put("g_[CABINET_CODE]_imwon", 7);
          put("g_[CABINET_CODE]_chief", 7);
          put("g_[CABINET_CODE]_mgr_g_a", 7);
          put("g_[CABINET_CODE]_mgr_g_b", 7);
          put("g_[CABINET_CODE]_old", 7);
          put("g_[DEFAULT_COMCODE]", 7);  
        }
      });
  @SuppressWarnings("serial")
  public static final Map<String, Integer> PJT_DEFAULT_CLOSED_G_AUTH_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_chairman", 7);
          put("g_[COM_CODE]_mgr_g_a", 7);
          put("g_[COM_CODE]_mgr_g_b", 7);
          put("g_[COM_CODE]_pjtmgr", 7);
          put("g_[CABINET_CODE]_imwon", 7);
          put("g_[CABINET_CODE]_chief", 7);
          put("g_[CABINET_CODE]_mgr_g_a", 7);
          put("g_[CABINET_CODE]_mgr_g_b", 7);
          put("g_[CABINET_CODE]_old", 7);
          put("g_[DEFAULT_GROUP]", 7);  
        }
      });

  //프로젝트 일반 WF
  @SuppressWarnings("serial")
  public static final Map<String, Integer> PJT_WF_CLOSED_S_AUTH_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_chairman", 7);
          put("g_audit_wf", 3);
          put("g_[COM_CODE]_mgr_g_a", 7);
          put("g_[COM_CODE]_mgr_g_b", 7);
          put("g_[CABINET_CODE]_imwon", 7);
          put("g_[CABINET_CODE]_chief", 7);
          put("g_[CABINET_CODE]_mgr_g_a", 7);
          put("g_[CABINET_CODE]_mgr_g_b", 7);
        }
      });
  @SuppressWarnings("serial")
  public static final Map<String, Integer> PJT_WF_CLOSED_T_AUTH_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_chairman", 7);
          put("g_audit_wf", 3);
          put("g_[COM_CODE]_mgr_g_a", 7);
          put("g_[COM_CODE]_mgr_g_b", 7);
          put("g_[COM_CODE]_pjtmgr", 7);
          put("g_[CABINET_CODE]_imwon", 7);
          put("g_[CABINET_CODE]_chief", 7);
          put("g_[CABINET_CODE]_mgr_g_a", 7);
          put("g_[CABINET_CODE]_mgr_g_b", 7);
          put("g_[CABINET_CODE]_old", 7);
          put("g_[DEFAULT_CABINET]", 7);
        }
      });
  @SuppressWarnings("serial")
  public static final Map<String, Integer> PJT_WF_CLOSED_C_AUTH_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_chairman", 7);
          put("g_audit_wf", 3);
          put("g_[COM_CODE]_mgr_g_a", 7);
          put("g_[COM_CODE]_mgr_g_b", 7);
          put("g_[COM_CODE]_pjtmgr", 7);
          put("g_[CABINET_CODE]_imwon", 7);
          put("g_[CABINET_CODE]_chief", 7);
          put("g_[CABINET_CODE]_mgr_g_a", 7);
          put("g_[CABINET_CODE]_mgr_g_b", 7);
          put("g_[CABINET_CODE]_old", 7);
          put("g_[DEFAULT_COMCODE]", 7);
        }
      });
  @SuppressWarnings("serial")
  public static final Map<String, Integer> PJT_WF_CLOSED_G_AUTH_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_chairman", 7);
          put("g_audit_wf", 3);
          put("g_[COM_CODE]_mgr_g_a", 7);
          put("g_[COM_CODE]_mgr_g_b", 7);
          put("g_[COM_CODE]_pjtmgr", 7);
          put("g_[CABINET_CODE]_imwon", 7);
          put("g_[CABINET_CODE]_chief", 7);
          put("g_[CABINET_CODE]_mgr_g_a", 7);
          put("g_[CABINET_CODE]_mgr_g_b", 7);
          put("g_[CABINET_CODE]_old", 7);
          put("g_[DEFAULT_GROUP]", 7);  
        }
      });

  //프로젝트 개인정보
  @SuppressWarnings("serial")
  public static final Map<String, Integer> PJT_PERSONAL_LIVE_T_AUTH_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_chairman", 7);
          put("g_[COM_CODE]_mgr_p_a", 2);
          put("g_[COM_CODE]_pjtmgr", 7);
          put("g_[CABINET_CODE]_imwon", 7);
          put("g_[CABINET_CODE]_chief", 7);
          put("g_[CABINET_CODE]_mgr_p_a", 2);
        }
      });
  @SuppressWarnings("serial")
  public static final Map<String, Integer> PJT_PERSONAL_CLOSED_S_AUTH_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_chairman", 7);
          put("g_[COM_CODE]_mgr_p_a", 2);
          put("g_[CABINET_CODE]_imwon", 7);
          put("g_[CABINET_CODE]_chief", 7);
          put("g_[CABINET_CODE]_mgr_p_a", 2);
        }
      });
  
  //프로젝트 개인정보 WF
  @SuppressWarnings("serial")
  public static final Map<String, Integer> PJT_PERSONAL_WF_LIVE_T_AUTH_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_chairman", 7);
          put("g_audit_wf_p_b", 3);
          put("g_[COM_CODE]_mgr_p_a", 2);
          put("g_[COM_CODE]_pjtmgr", 7);
          put("g_[CABINET_CODE]_imwon", 7);
          put("g_[CABINET_CODE]_chief", 7);
          put("g_[CABINET_CODE]_mgr_p_a", 2);
        }
      });
  @SuppressWarnings("serial")
  public static final Map<String, Integer> PJT_PERSONAL_WF_CLOSED_S_AUTH_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_chairman", 7);
          put("g_audit_wf_p_b", 3);
          put("g_[COM_CODE]_mgr_p_a", 2);
          put("g_[CABINET_CODE]_imwon", 7);
          put("g_[CABINET_CODE]_chief", 7);
          put("g_[CABINET_CODE]_mgr_p_a", 2);
        }
      });
  

  //연구과제 일반
  @SuppressWarnings("serial")
  public static final Map<String, Integer> RSCH_DEFAULT_LIVE_AUTH_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_chairman", 7);
          put("g_[COM_CODE]_mgr_g_a", 7);
          put("g_[COM_CODE]_mgr_g_b", 7);
          put("g_[COM_CODE]_rschmgr", 7);
          put("g_[CABINET_CODE]_imwon", 7);
          put("g_[CABINET_CODE]_chief", 7);
          put("g_[CABINET_CODE]_mgr_g_a", 7);
          put("g_[CABINET_CODE]_mgr_g_b", 7);
          put("g_[CABINET_CODE]_old", 7);
          put("g_[DEFAULT_CABINET]", 7);
        }
      });
  @SuppressWarnings("serial")
  public static final Map<String, Integer> RSCH_DEFAULT_CLOSED_S_AUTH_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_chairman", 7);
          put("g_[COM_CODE]_mgr_g_a", 7);
          put("g_[COM_CODE]_mgr_g_b", 7);
          put("g_[CABINET_CODE]_imwon", 7);
          put("g_[CABINET_CODE]_chief", 7);
          put("g_[CABINET_CODE]_mgr_g_a", 7);
          put("g_[CABINET_CODE]_mgr_g_b", 7);
        }
      });
  @SuppressWarnings("serial")
  public static final Map<String, Integer> RSCH_DEFAULT_CLOSED_T_AUTH_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_chairman", 7);
          put("g_[COM_CODE]_mgr_g_a", 7);
          put("g_[COM_CODE]_mgr_g_b", 7);
          put("g_[COM_CODE]_rschmgr", 7);
          put("g_[CABINET_CODE]_imwon", 7);
          put("g_[CABINET_CODE]_chief", 7);
          put("g_[CABINET_CODE]_mgr_g_a", 7);
          put("g_[CABINET_CODE]_mgr_g_b", 7);
          put("g_[CABINET_CODE]_old", 7);
          put("g_[DEFAULT_CABINET]", 7);
        }
      });
  @SuppressWarnings("serial")
  public static final Map<String, Integer> RSCH_DEFAULT_CLOSED_C_AUTH_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_chairman", 7);
          put("g_[COM_CODE]_mgr_g_a", 7);
          put("g_[COM_CODE]_mgr_g_b", 7);
          put("g_[COM_CODE]_rschmgr", 7);
          put("g_[CABINET_CODE]_imwon", 7);
          put("g_[CABINET_CODE]_chief", 7);
          put("g_[CABINET_CODE]_mgr_g_a", 7);
          put("g_[CABINET_CODE]_mgr_g_b", 7);
          put("g_[CABINET_CODE]_old", 7);
          put("g_[DEFAULT_COMCODE]", 7);
        }
      });
  @SuppressWarnings("serial")
  public static final Map<String, Integer> RSCH_DEFAULT_CLOSED_G_AUTH_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_chairman", 7);
          put("g_[COM_CODE]_mgr_g_a", 7);
          put("g_[COM_CODE]_mgr_g_b", 7);
          put("g_[COM_CODE]_rschmgr", 7);
          put("g_[CABINET_CODE]_imwon", 7);
          put("g_[CABINET_CODE]_chief", 7);
          put("g_[CABINET_CODE]_mgr_g_a", 7);
          put("g_[CABINET_CODE]_mgr_g_b", 7);
          put("g_[CABINET_CODE]_old", 7);
          put("g_[DEFAULT_GROUP]", 7);  
        }
      });

  //연구과제 일반 WF
  @SuppressWarnings("serial")
  public static final Map<String, Integer> RSCH_WF_CLOSED_S_AUTH_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_chairman", 7);
          put("g_audit_wf", 3);
          put("g_[COM_CODE]_mgr_g_a", 7);
          put("g_[COM_CODE]_mgr_g_b", 7);
          put("g_[CABINET_CODE]_imwon", 7);
          put("g_[CABINET_CODE]_chief", 7);
          put("g_[CABINET_CODE]_mgr_g_a", 7);
          put("g_[CABINET_CODE]_mgr_g_b", 7);
        }
      });
  @SuppressWarnings("serial")
  public static final Map<String, Integer> RSCH_WF_CLOSED_T_AUTH_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_chairman", 7);
          put("g_audit_wf", 3);
          put("g_[COM_CODE]_mgr_g_a", 7);
          put("g_[COM_CODE]_mgr_g_b", 7);
          put("g_[COM_CODE]_rschmgr", 7);
          put("g_[CABINET_CODE]_imwon", 7);
          put("g_[CABINET_CODE]_chief", 7);
          put("g_[CABINET_CODE]_mgr_g_a", 7);
          put("g_[CABINET_CODE]_mgr_g_b", 7);
          put("g_[CABINET_CODE]_old", 7);
          put("g_[DEFAULT_CABINET]", 7);
        }
      });
  @SuppressWarnings("serial")
  public static final Map<String, Integer> RSCH_WF_CLOSED_C_AUTH_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_chairman", 7);
          put("g_audit_wf", 3);
          put("g_[COM_CODE]_mgr_g_a", 7);
          put("g_[COM_CODE]_mgr_g_b", 7);
          put("g_[COM_CODE]_rschmgr", 7);
          put("g_[CABINET_CODE]_imwon", 7);
          put("g_[CABINET_CODE]_chief", 7);
          put("g_[CABINET_CODE]_mgr_g_a", 7);
          put("g_[CABINET_CODE]_mgr_g_b", 7);
          put("g_[CABINET_CODE]_old", 7);
          put("g_[DEFAULT_COMCODE]", 7);  
        }
      });
  @SuppressWarnings("serial")
  public static final Map<String, Integer> RSCH_WF_CLOSED_G_AUTH_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_chairman", 7);
          put("g_audit_wf", 3);
          put("g_[COM_CODE]_mgr_g_a", 7);
          put("g_[COM_CODE]_mgr_g_b", 7);
          put("g_[COM_CODE]_rschmgr", 7);
          put("g_[CABINET_CODE]_imwon", 7);
          put("g_[CABINET_CODE]_chief", 7);
          put("g_[CABINET_CODE]_mgr_g_a", 7);
          put("g_[CABINET_CODE]_mgr_g_b", 7);
          put("g_[CABINET_CODE]_old", 7);
          put("g_[DEFAULT_GROUP]", 7);  
        }
      });

  //연구과제 개인정보
  @SuppressWarnings("serial")
  public static final Map<String, Integer> RSCH_PERSONAL_LIVE_AUTH_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_chairman", 7);
          put("g_[COM_CODE]_mgr_p_a", 2);
          put("g_[COM_CODE]_rschmgr", 7);
          put("g_[CABINET_CODE]_imwon", 7);
          put("g_[CABINET_CODE]_chief", 7);
          put("g_[CABINET_CODE]_mgr_p_a", 2);
        }
      });
  @SuppressWarnings("serial")
  public static final Map<String, Integer> RSCH_PERSONAL_CLOSED_S_AUTH_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_chairman", 7);
          put("g_[COM_CODE]_mgr_p_a", 2);
          put("g_[CABINET_CODE]_imwon", 7);
          put("g_[CABINET_CODE]_chief", 7);
          put("g_[CABINET_CODE]_mgr_p_a", 2);
        }
      });
  
  //연구과제 개인정보 WF
  @SuppressWarnings("serial")
  public static final Map<String, Integer> RSCH_PERSONAL_WF_LIVE_AUTH_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_chairman", 7);
          put("g_audit_wf_p_b", 3);
          put("g_[COM_CODE]_mgr_p_a", 2);
          put("g_[COM_CODE]_rschmgr", 7);
          put("g_[CABINET_CODE]_imwon", 7);
          put("g_[CABINET_CODE]_chief", 7);
          put("g_[CABINET_CODE]_mgr_p_a", 2);
        }
      });
  @SuppressWarnings("serial")
  public static final Map<String, Integer> RSCH_PERSONAL_WF_CLOSED_S_AUTH_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_chairman", 7);
          put("g_audit_wf_p_b", 3);
          put("g_[COM_CODE]_mgr_p_a", 2);
          put("g_[CABINET_CODE]_imwon", 7);
          put("g_[CABINET_CODE]_chief", 7);
          put("g_[CABINET_CODE]_mgr_p_a", 2);
        }
      });
  
  //사별
  @SuppressWarnings("serial")
  public static final Map<String, Integer> COM_HAM_AUTH_MAP = Collections
  .unmodifiableMap(new HashMap<String, Integer>() {
    {
      put("g_chairman", 7); 
      put("g_[COM_CODE]_mgr_g_a", 7);
      put("g_[COM_CODE]_mgr_g_b", 7);  
      put("g_[DEFAULT_COMCODE]", 7);  
    }
  });
  //제강 관리직
  @SuppressWarnings("serial")
  public static final Map<String, Integer> COM_DKS_HAM_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_chairman", 7); 
          put("g_[COM_CODE]_mgr_g_a", 7);
          put("g_[COM_CODE]_mgr_g_b", 7);
          put("g_dksm", 7);
        }
      });
  //인터지스 관리
  @SuppressWarnings("serial")
  public static final Map<String, Integer> COM_ITG_HAM_MAP = Collections
      .unmodifiableMap(new HashMap<String, Integer>() {
        {
          put("g_chairman", 7); 
          put("g_[COM_CODE]_mgr_g_a", 7);
          put("g_[COM_CODE]_mgr_g_b", 7);
          put("g_itg00006350", 7);
        }
      });
  //동국제강_관리직
  
  //인터지스_관리
  
  
  
  
}
