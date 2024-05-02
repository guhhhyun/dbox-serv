package com.dongkuksystems.dbox.models.dto.type.agree;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfTime;
import com.documentum.fc.common.IDfTime;
import com.dongkuksystems.dbox.constants.AclTemplate;
import com.dongkuksystems.dbox.constants.GrantedLevels;
import com.dongkuksystems.dbox.constants.SecLevelCode;
import com.dongkuksystems.dbox.models.dto.type.upload.AttachedFile;
import com.dongkuksystems.dbox.models.dto.type.upload.AttachedKUploadFile;
import com.dongkuksystems.dbox.models.dto.type.upload.UploadDocDto;
import com.dongkuksystems.dbox.utils.dctm.DCTMUtils;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WfDocDto {
	  @ApiModelProperty(value = "object ID")
	  private String rObjectId;
	  @ApiModelProperty(value = "파일명")
	  private String objectName;

	  @ApiModelProperty(value = "문서 ID")
	  private String uDocId;
	  @ApiModelProperty(value = "문서 키")
	  private String uDocKey;
	  @ApiModelProperty(value = "문서함 코드")
	  private String uCabinetCode;
	  
	  private String uRequestIp;
	  
	  @ApiModelProperty(value = "회사-문서함코드")
	  private String uComOrgCabinetCd;
	  
	  @ApiModelProperty(value = "그룹-문서함코드")
	  private String uGroupOrgCabinetCd;
	  
	  @ApiModelProperty(value = "현재 작업자의 작업자구분(P:개인, D:부서관리자, C:사별관리자, G:그룹관리자, S:시스템)")
	  private String uJobUserType;
	  
	  @ApiModelProperty(value = "폴더 ID") 
	  private String uFolId;
	  @ApiModelProperty(value = "결재정보")
	  private String uWfInf;
	  @ApiModelProperty(value = "최종결재자")
	  private String uWfApprover;
	  @ApiModelProperty(value = "생성자")
	  private String uCreateUser;
	  @JsonSerialize(using = LocalDateTimeSerializer.class)
	  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
	  @ApiModelProperty(value = "생성 일시")
	  private LocalDateTime uCreateDate;
	  @ApiModelProperty(value = "수정자")
	  private String uModifyUser;
	  @JsonSerialize(using = LocalDateTimeSerializer.class)
	  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
	  @ApiModelProperty(value = "수정 일시")
	  private LocalDateTime uModifyDate;
	  
	  @ApiModelProperty(value = "추가권한자 리스트")
	  private Map<String, String> addGrantedMap = new HashMap<String, String>();
	  
	  @ApiModelProperty(value = "결재 ID")
	  private String approvalId           ;
	  @ApiModelProperty(value = "결재 기안자")
	  private String approvalWriter       ;
	  @ApiModelProperty(value = "결재 보안등급")
	  private String approvalLevel        ;
	  @ApiModelProperty(value = "결재 시스템  ")
	  private String approvalSystem       ;
	  @ApiModelProperty(value = "결재제목 ")
	  private String approvalSubject      ;
	  @ApiModelProperty(value = "결재 상태 ( W:기안, A:중간결재, F:최종결재, R:회수, X:반려)")
	  private String approvalState        ;

	  
	  @ApiModelProperty(value = "결재자 리스트")
	  private String approvalDraftLine        ;

	  @ApiModelProperty(value = "결재 참조자 리스트")
	  private String approvalCcLine        ;

	  @ApiModelProperty(value = "결재 수신자 리스트")
	  private String approvalReceiveLine        ;

	  
	  @ApiModelProperty(value = "결재자 리스트 (사람:P, 조직:O, 회사: C) ")
	  @Builder.Default 
	  private Map<String, String[]> approvalDraftLineM = new HashMap<String, String[]>();

	  @ApiModelProperty(value = "결재 참조자 리스트 (사람:P, 조직:O, 회사: C) 리스트")
	  @Builder.Default 
	  private Map<String, String[]> approvalCcLineM = new HashMap<String, String[]>();
	  
	  @ApiModelProperty(value = "결재 수신자 리스트 (사람:P, 조직:O, 회사: C) 리스트")
	  @Builder.Default 
	  private Map<String, String[]> approvalReceiveLineM = new HashMap<String, String[]>();
	  
	   
	  
	  @Builder.Default
	  @ApiModelProperty(value = "보존년한")
	  private int preserverYear = 3; //전자결재 기본 세팅값이 3

	  @Builder.Default
	  @ApiModelProperty(value = "영구보존값")
	  private int youngGu = 0; //전자결재에서 넘어온 보존연한값이 이 값이면 영구보존
	  
	  
	  @ApiModelProperty(value = "결재 화면 링크")
	  private String approvalLink         ;
	  @ApiModelProperty(value = "전자결재 양식명")
	  private String approvalFormName     ;
	  @ApiModelProperty(value = "PC or D'box 첨부 여부")
	  private String attachType           ;
	  
	  //외부 I/F 결과 반환( 결재 ) 
	  @ApiModelProperty(value = "응답 코드")
	  private String status      ;   //응답 코드
	  @ApiModelProperty(value = "응답 메시지")
	  private String message     ;   //응답 메시지
	  @ApiModelProperty(value = "D'box 문서 ID")
	  private String dboxId      ;   //D'box 문서 ID
	  @ApiModelProperty(value = "D'box 문서 보안등급")
	  private String dboxLevel   ;   //D'box 문서 보안등급
	  @ApiModelProperty(value = "D'box 문서 링크파일")
	  private String dboxLink    ;   //D'box 문서 링크파일

	  @ApiModelProperty(value = "첨부파일삭제여부")      
	  private String fileDeleteYn;

	  @ApiModelProperty(value = "파일Closed여부")      
	  private String uDocStatus;
	  

	  public static IDfDocument createWfDoc(IDfSession idfSession, WfDocDto dto, AttachedFile aFile) throws Exception {
	    
	      
		  String apiKey         = null;
		  IDfDocument idfDoc    = null;
		  String loginId        = null;
		  
	
		  String s_CabinetCode  = dto.getUCabinetCode();
		  
		  idfDoc = (IDfDocument) idfSession.newObject("edms_doc");
	      // 문서명
		  String s_ObjectName = aFile.getOriginalFileName();
		  String s_Extr     = DCTMUtils.getFileExtByFileName(s_ObjectName);
		  
		  s_ObjectName=s_ObjectName.replace("."+s_Extr, "");
		  
		  
	      idfDoc.setObjectName(s_ObjectName);
	      idfDoc.setTitle(aFile.getOriginalFileName());
	      // 포맷

	      //idfDoc.setContentType(aFile.getDContentType());
	      //idfDoc.setContentType(DCTMUtils.getFormatByFileExt(idfSession, DCTMUtils.getFileExtByFileName(idfDoc.getString("title"))));

	      // 소유자 지정 : Docbase Owner로 지정 
	      idfDoc.setOwnerName(idfSession.getDocbaseOwnerName());
	      // 템플릿 ACL 적용
	      idfDoc.setACLDomain(idfSession.getDocbaseOwnerName());
	
	      if (dto.getApprovalState().equals("F") || dto.getApprovalState().equals("X")) { //최종승인이 아니면 기본적인 ACL부여
	          idfDoc.setACLName(MessageFormat.format(AclTemplate.DEPT_WF_ACL_SEC.getValue(), s_CabinetCode ));
	          idfDoc.setString("u_doc_status"    , "C"); //파일 상태를 closed로 바꿔준다
	      } else {
	  	      idfDoc.setString("u_doc_status"    ,  dto.getUDocStatus());
	      }

		  String lDocStatus[] = {"C", "L"};
		  String s_ObjId=idfDoc.getChronicleId()+"";
		  String s_Dql="";
		  
		  String lsMaxPermit = dto.getUDocStatus().equals("C")?"R":"D";
		  
		  IDfQuery 		idf_Qry  = null;
		  IDfCollection idf_Colb = null;
		  
		  for (Map.Entry<String, String[]> entry : dto.getApprovalDraftLineM().entrySet()) {
			  for(int i=0; i < entry.getValue().length; i++) {
				 //if ( authService.checkDocAuth(dto.getDboxId(), userSession.getUser().getUserId(), GrantedLevels.DELETE.getLevel()) ) {
					 if(!entry.getValue()[i].equals("")) {
						
					    String sAuthorId = entry.getValue()[i];
					    String sAuthorType=sAuthorId.contains("g_")?"D":"U";
					    
				       // if(sAuthorId.equals("g_"+dto.getUCabinetCode())) continue;
				       // if(sAuthorId.equals(dto.getApprovalWriter())) continue;

						idfDoc.grant(sAuthorId, GrantedLevels.findByLabel(lsMaxPermit), "");
						if(sAuthorType.equals("D"))
							idfDoc.grant(sAuthorId+"_sub", GrantedLevels.findByLabel(lsMaxPermit), "");
						idfDoc.save();
				    
					    for(int j=0; j< lDocStatus.length; j++) {
				  	    	String s_PermitType=lDocStatus[j].equals("C")?"R":"D";
						    IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
			  				idf_PObj.setString("u_obj_id"		, s_ObjId);
			  				idf_PObj.setString("u_obj_type"		, "D");
			  				idf_PObj.setString("u_doc_status"	, lDocStatus[j]);
			  				
			  				idf_PObj.setString("u_permit_type"	, s_PermitType); //권한
			  				
			  				idf_PObj.setString("u_own_dept_yn"	,  sAuthorId.equals("g_"+s_CabinetCode)?"Y":""); //
			  				idf_PObj.setString("u_author_id"	,  sAuthorId);
			  				idf_PObj.setString("u_author_type"	,  sAuthorType); //사용자 
			  				idf_PObj.setString("u_create_user"	, dto.getApprovalWriter());
			  				idf_PObj.setTime  ("u_create_date"	, new DfTime());
			  				idf_PObj.setString("u_ext_key"	, dto.getApprovalId()); //
			  				idf_PObj.setString("u_add_gubun"	, "W"); //
			  				
			  				idf_PObj.save();	
					    }
					 }
				 //}
			  }
	      }
	      for (Map.Entry<String, String[]> entry : dto.getApprovalCcLineM().entrySet()) {
			  for(int i=0; i < entry.getValue().length; i++) {
				 //if ( authService.checkDocAuth(dto.getDboxId(), userSession.getUser().getUserId(), GrantedLevels.DELETE.getLevel()) ) {
					 if(!entry.getValue()[i].equals("")) {
						    String sAuthorId = entry.getValue()[i];
						    String sAuthorType=sAuthorId.contains("g_")?"D":"U";

					        //if(sAuthorId.equals("g_"+dto.getUCabinetCode())) continue;
					        //if(sAuthorId.equals(dto.getApprovalWriter())) continue;
						    
						    idfDoc.grant(sAuthorId, GrantedLevels.findByLabel("R"), "");
							if(sAuthorType.equals("D"))
								idfDoc.grant(sAuthorId+"_sub", GrantedLevels.findByLabel("R"), "");
                            idfDoc.save();
						    
						    for(int j=0; j< lDocStatus.length; j++) {
							    IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
				  				idf_PObj.setString("u_obj_id"		, s_ObjId);
				  				idf_PObj.setString("u_obj_type"		, "D");
				  				idf_PObj.setString("u_doc_status"	,  lDocStatus[j]);
				  				idf_PObj.setString("u_permit_type"	, "R"); //읽기
				  				idf_PObj.setString("u_own_dept_yn"	, sAuthorId.equals("g_"+s_CabinetCode)?"Y":""); //
				  				idf_PObj.setString("u_author_id"	,  sAuthorId);
				  				idf_PObj.setString("u_author_type"	,  sAuthorType); //사용자 
				  				idf_PObj.setString("u_create_user"	, dto.getApprovalWriter());
				  				idf_PObj.setTime  ("u_create_date"	, new DfTime());
				  				idf_PObj.setString("u_ext_key"	, dto.getApprovalId()); //
				  				idf_PObj.setString("u_add_gubun"	, "W"); //
				  				idf_PObj.save();
						   }
				  	    }
			      }
					 
		  }
	      
	      for (Map.Entry<String, String[]> entry : dto.getApprovalReceiveLineM().entrySet()) {
			  for(int i=0; i < entry.getValue().length; i++) {
				 // if ( authService.checkDocAuth(dto.getDboxId(), userSession.getUser().getUserId(), GrantedLevels.DELETE.getLevel()) ) {
					  if(!entry.getValue()[i].equals("")) {
						  String sAuthorId = entry.getValue()[i];
						    String sAuthorType=sAuthorId.contains("g_")?"D":"U";

						        //if(sAuthorId.equals("g_"+dto.getUCabinetCode())) continue;
						        //if(sAuthorId.equals(dto.getApprovalWriter())) continue;
						        
							    idfDoc.grant(sAuthorId, GrantedLevels.findByLabel("R"), "");
								if(sAuthorType.equals("D"))
									idfDoc.grant(sAuthorId+"_sub", GrantedLevels.findByLabel("R"), "");
							    
	                            idfDoc.save();
	                            
	                            for(int j=0; j< lDocStatus.length; j++) {
								    IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
					  				idf_PObj.setString("u_obj_id"		, s_ObjId);
					  				idf_PObj.setString("u_obj_type"		, "D");
					  				idf_PObj.setString("u_doc_status"	, lDocStatus[j]);
					  				idf_PObj.setString("u_permit_type"	, "R"); //읽기
					  				idf_PObj.setString("u_own_dept_yn"	, sAuthorId.equals("g_"+s_CabinetCode)?"Y":""); //
					  				idf_PObj.setString("u_author_id"	,  sAuthorId);
					  				idf_PObj.setString("u_author_type"	,  sAuthorType); //사용자 
					  				idf_PObj.setString("u_create_user"	, dto.getApprovalWriter());
					  				idf_PObj.setTime  ("u_create_date"	, new DfTime());
					  				idf_PObj.setString("u_ext_key"	, dto.getApprovalId()); //
					  				idf_PObj.setString("u_add_gubun"	, "W"); //
					  				idf_PObj.save();
						  		}
				  	    }
			      }
		   }
	      
	   
	  	  int i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+idfDoc.getChronicleId()+"" +"' and u_author_id='"+dto.getApprovalWriter() +"' and u_doc_status='L'  ");
	  	  if(i_AuthorCnt < 1) {
		      IDfPersistentObject idf_PObjU = idfSession.newObject("edms_auth_base");
		      idf_PObjU.setString("u_obj_id"		, idfDoc.getChronicleId()+"");
		      idf_PObjU.setString("u_obj_type"		, "D");
		      idf_PObjU.setString("u_doc_status"	, "L");
		      idf_PObjU.setString("u_permit_type"	, "D"); //읽기-쓰기(편집)
		      idf_PObjU.setString("u_author_id"	,  dto.getApprovalWriter());
		      idf_PObjU.setString("u_author_type"	,  "U"); //사용자 
		      idf_PObjU.setString("u_create_user"	, dto.getUCreateUser());
		      idf_PObjU.setTime  ("u_create_date"	, new DfTime());
		      idf_PObjU.setString("u_add_gubun"	, "G"); //
		      idf_PObjU.save();
	  	  }
	  	  i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+idfDoc.getChronicleId()+"" +"' and u_author_id='"+dto.getApprovalWriter() +"' and u_doc_status='C'  ");
	  	  if(i_AuthorCnt < 1) {
		      if(!dto.getApprovalLevel().equals("G") && !dto.getApprovalLevel().equals("C")) { 
			      IDfPersistentObject idf_PObjU = idfSession.newObject("edms_auth_base");
			      idf_PObjU = idfSession.newObject("edms_auth_base");
			      idf_PObjU.setString("u_obj_id"		, idfDoc.getChronicleId()+"");
			      idf_PObjU.setString("u_obj_type"		, "D");
			      idf_PObjU.setString("u_doc_status"	, "C");
			      idf_PObjU.setString("u_permit_type"	, "R"); //읽기(Closed)
			      idf_PObjU.setString("u_author_id"	,  dto.getApprovalWriter());
			      idf_PObjU.setString("u_author_type"	,  "U"); //사용자 
			      idf_PObjU.setString("u_create_user"	, dto.getUCreateUser());
			      idf_PObjU.setTime  ("u_create_date"	, new DfTime());
			      idf_PObjU.setString("u_add_gubun"	, "G"); //
			      idf_PObjU.save();
		      }
	  	  }
	      if( !dto.getApprovalLevel().equals("S")) {
	          idfDoc.grant(dto.getApprovalWriter(), GrantedLevels.findByLabel(lsMaxPermit), "");
	          idfDoc.save();
	      }else {
	          idfDoc.grant(dto.getApprovalWriter(), GrantedLevels.findByLabel("R"), "");
	          idfDoc.save();
	      }
	      
	      // 추가 권한자 지정 : 기본 권한이나 공유/협업 대상자 등
	      // 추가 권한자를 추가하면 ACL명이 'dm_45XXXXXXXXXXXXXX' 형식(Custom ACL)으로 변경 됨

          String s_AuthStr=  "g_"+dto.getUCabinetCode();
          //System.out.println("#==="+ s_AuthStr);
          if(null!= s_AuthStr && !s_AuthStr.equals("null") &&  !s_AuthStr.equals("") && !s_AuthStr.equals(" ")) {
		  	  i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+idfDoc.getChronicleId()+"" +"' and u_author_id='"+s_AuthStr +"' and u_doc_status='L'  ");
		  	  if(i_AuthorCnt < 1) {
				  IDfPersistentObject idf_PObjD = idfSession.newObject("edms_auth_base");
				  idf_PObjD.setString("u_obj_id"		, idfDoc.getChronicleId()+"");
				  idf_PObjD.setString("u_obj_type"		, "D");
				  idf_PObjD.setString("u_doc_status"	, "L");
				  idf_PObjD.setString("u_permit_type"	, "D"); //읽기/쓰기/편집
				  idf_PObjD.setString("u_own_dept_yn"	, "Y"); //
				  idf_PObjD.setString("u_author_id"	,  s_AuthStr);
				  idf_PObjD.setString("u_author_type"	,  "D"); //사용자 
				  idf_PObjD.setString("u_create_user"	, dto.getUCreateUser());
				  idf_PObjD.setTime  ("u_create_date"	, new DfTime());
				  idf_PObjD.setString("u_add_gubun"	, "G"); //
				  idf_PObjD.save();
		  	  }

		  	  i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+idfDoc.getChronicleId()+"" +"' and u_author_id='"+s_AuthStr +"' and u_doc_status='C'  ");
		  	  if(i_AuthorCnt < 1) {
				  if(!dto.getApprovalLevel().equals("S")) {
		              if(dto.getApprovalLevel().equals("G")) s_AuthStr=dto.getUGroupOrgCabinetCd();
		              else if(dto.getApprovalLevel().equals("C")) s_AuthStr = dto.getUComOrgCabinetCd();
		              
		              IDfPersistentObject idf_PObjD = idfSession.newObject("edms_auth_base");
					  idf_PObjD = idfSession.newObject("edms_auth_base");
				      idf_PObjD.setString("u_obj_id"		, idfDoc.getChronicleId()+"");
				      idf_PObjD.setString("u_obj_type"		, "D");
				      idf_PObjD.setString("u_doc_status"	, "C");
				      idf_PObjD.setString("u_permit_type"	, "R"); //읽기/쓰기/편집 
				      idf_PObjD.setString("u_own_dept_yn"	, "Y"); //
				      idf_PObjD.setString("u_author_id"	,  s_AuthStr);
				      idf_PObjD.setString("u_author_type"	,  "D"); //사용자 
				      idf_PObjD.setString("u_create_user"	, dto.getUCreateUser());
				      idf_PObjD.setTime  ("u_create_date"	, new DfTime());
				      idf_PObjD.setString("u_add_gubun"	, "G"); //
				      idf_PObjD.save();
				  }
			      if( !dto.getApprovalLevel().equals("S")) {
					  idfDoc.grant(s_AuthStr, GrantedLevels.findByLabel(lsMaxPermit), ""); //읽기/쓰기/편집권한
					  idfDoc.grant(s_AuthStr+"_sub", GrantedLevels.findByLabel(lsMaxPermit), "");
			      }
		  	  }
          }
	
	      // 업무 속성 지정
	      idfDoc.setString("u_cabinet_code", s_CabinetCode);           // 문서함코드
	      idfDoc.setString("u_doc_key", "" + idfDoc.getChronicleId()); // 문서 키
	      idfDoc.setString("u_sec_level"     , dto.getApprovalLevel()); // 보안등급
	      
	      //u_file_path,  u_reg_user, u_reg_date, u_folder_path , u_file_ext , a_content_type ( ppt12로 되어있음)
	      
	      idfDoc.setString("u_file_ext",    aFile.getFileExtention());
	      idfDoc.setString("u_reg_date",   (new DfTime()).toString());
	      
	      idfDoc.setString("u_wf_doc_yn", "Y");
	      idfDoc.setString("u_reg_source", "P");//로컬에 있는 문서를 등록할 때,
	      
	      idfDoc.setString("r_creator_name", dto.getUCreateUser());
		  idfDoc.appendString("u_editor",    dto.getUCreateUser());        //결재요청자 
		  
	      idfDoc.appendString("u_wf_system",    dto.getApprovalSystem());        //결재 시스템
	      idfDoc.appendString("u_wf_form",      dto.getApprovalFormName());      //결재 양식명
	      idfDoc.appendString("u_wf_title",     dto.getApprovalSubject());       //결재 제목
	      if(!dto.getApprovalState().equals("W"))
	          idfDoc.appendString("u_wf_approver",  dto.getApprovalWriter());        //결재자
	      idfDoc.appendString("u_wf_approval_date",  (new DfTime()).toString());
	      idfDoc.appendString("u_wf_key",       dto.getApprovalId());            //결재 ID
	      idfDoc.appendString("u_wf_link",      dto.getApprovalLink());          //결재화면 URL
	
	      //개인정보포함여부 언체크, 메일자동권한부여 체크, 반출여부 초기화 등
	      idfDoc.setBoolean("u_privacy_flag",      false);   //개인정보포함여부 unChk
	      idfDoc.setBoolean("u_auto_auth_mail_flag",true);   //메일자동권한부여 Chk
	      idfDoc.setString("u_takeout_flag" , "0");          //반출여부 초기화
	      idfDoc.setString("u_recycle_date" , "");
	      idfDoc.setString("u_last_editor"  , dto.getUCreateUser());	      

	      idfDoc.setString("u_update_date", (new DfTime()).toString());
	      
	      idfDoc.setBoolean("u_ver_keep_flag",   true); //첨부시점에 '버전 유지 여부' 되도록 함(2021.12.1 추가) 
	      
	      
		  return idfDoc;
	  }
	  
	  //결재를 제외한 나머지에서 dbox파일을 첨부파일로 추가할 때 사용 
	  public static IDfDocument updateWfDoc(IDfSession idfSession, DboxAttaDocDto dto) throws Exception {
	      
		  String apiKey         = null;
		  IDfDocument idfDoc    = null;
		  String loginId        = null;
		  
		  
		  String s_CabinetCode  = dto.getCabinetCode();
		  
		  idfDoc = (IDfDocument) idfSession.newObject("edms_doc");
	      
	      // 템플릿 ACL 적용
	      idfDoc.setACLDomain(idfSession.getDocbaseOwnerName());

	      for (Map.Entry<String, String[]> entry : dto.getBandRefLineM().entrySet()) {
    		  for(int i=0; i < entry.getValue().length; i++)
    			  idfDoc.grant(entry.getValue()[i], GrantedLevels.findByLabel("R"), "");
	      }
	      
		  return idfDoc;
	  }
	  

}
