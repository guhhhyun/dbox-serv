package com.dongkuksystems.dbox.models.dto.type.upload;

import static com.dongkuksystems.dbox.utils.DboxStringUtils.CheckString;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.util.Map;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;
import com.documentum.fc.common.IDfTime;
import com.dongkuksystems.dbox.constants.AclTemplate;
import com.dongkuksystems.dbox.constants.DocStatus;
import com.dongkuksystems.dbox.constants.HamType;
import com.dongkuksystems.dbox.constants.SecLevelCode;
import com.dongkuksystems.dbox.constants.SysObjectType;
import com.dongkuksystems.dbox.models.dto.type.auth.HamInfoResult;
import com.dongkuksystems.dbox.models.type.doc.Doc;
import com.dongkuksystems.dbox.utils.DateTimeUtils;
import com.dongkuksystems.dbox.utils.DboxStringUtils;
import com.dongkuksystems.dbox.utils.dctm.DCTMUtils;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UploadDocDto {
  @ApiModelProperty(value = "상위 함 타입", example = "P:프로젝트, R:연구과제, D:부서, F:folder, I:중요문서함, C, M", required = true)
  private String hamType;
  @ApiModelProperty(value = "상위 id", notes = "hamType이 F가 아닐경우 project code, research code, orgId", required = true)
  private String upObjectId;
  @Builder.Default
  @ApiModelProperty(value = "건너띄기:S, 버전갱신:V, 복사본 추가:C")
  private String uploadFlag = "S";

//  @ApiModelProperty(value = "상위 함 id", example = "프로젝트, 연구과제 아이디, 부서아이디", hidden = true)
//  private String hamId;

  @Builder.Default
  @ApiModelProperty(value = "권한레벨", hidden = true)
  private String sSecLevel = "T";
  @Builder.Default
  @ApiModelProperty(value = "문서상태", hidden = true)
  private String docStatus = "L";
  @Builder.Default
  @ApiModelProperty(value = "등록소스구분', SET COMMENT_TEXT='D:DBox, P:PC'", hidden = true)
  private String uRegSource = "D";
  @ApiModelProperty(value = "폴더 드래그로 project, research 생성시 사용", hidden = true)
  private String prType;
  @ApiModelProperty(value = "폴더타입", hidden = true)
  private String uFolType;

  @ApiModelProperty(value = "문서 acl", hidden = true)
  private String docAcl;
  @ApiModelProperty(value = "상위 함 id", example = "프로젝트, 연구과제 아이디, 부서아이디", hidden = true)
  HamInfoResult hamInfo;
  @ApiModelProperty(value = "문서 삭제상태(TEMP저장 여부)", hidden = true)
  private String deleteStatus;
  
  @Builder.Default
  @ApiModelProperty(value = "문서", hidden = true)
  private Doc doc = new Doc();

  public boolean checkRoot() {
    if (!"F".equals(this.hamType)) {
      return true;
    } else {
      return false;
    }
  }

  public boolean hamValidation() {
    if ("P".equals(this.hamType) || "R".equals(this.hamType)) {
      return true;
    }
    return false;
  }

  public static boolean hamValidation(String str) {
    if ("P".equals(str) || "R".equals(str)) {
      return true;
    }
    return false;
  }

//  public void addAdditionalInfo() {
//    if (checkRoot()) {
//      this.hamId = this.upObjectId;
//    } 
//  }

  public String makeFolderType() {
    if ("P".equals(this.hamInfo.getHamType())) {
      return "PFO";
    } else if ("R".equals(this.hamInfo.getHamType())) {
      return "RFO";
    }
    return "DFO";
  }

  public static UploadDocDto fromKUploadParam(String val) throws JsonParseException, JsonMappingException, IOException {
    Map<String, Object> map = DboxStringUtils.strToMap(val);
//    checkArgument(!(Objects.equal(map.get("upFolderId"), null) || Objects.equal(map.get("upFolderId"), "")), "upFolderId must be provided.");
//    checkArgument(!(Objects.equal(map.get("sCabinetCode"), null) || Objects.equal(map.get("sCabinetCode"), "")), "sCabinetCode must be provided.");
    return UploadDocDto.builder()
//                          .upFolderId((String)map.get("upFolderId"))
//                          .sCabinetCode((String)map.get("sCabinetCode"))
//                          .sSecLevel((String)map.get("sSecLevel"))
//                          .addGrantedMap((Map<String, String>) map.get("addGrantedMap"))
        .build();
  }


  public static IDfDocument overWirteDocument(IDfSession idfSession, String objectId, AttachedKUploadFile aFile)
      throws Exception {
    IDfDocument idfDoc = (IDfDocument) idfSession.getObject(new DfId(objectId));
    idfDoc.setObjectName(aFile.getOriginalFileName());
    idfDoc.setContentType(aFile.getDContentType());
    idfDoc.setFile(aFile.getNewFileLocation());
    return idfDoc;
  }
  
  public static IDfDocument toIDfDocument(IDfSession idfSession, UploadDocDto dto, AttachedKUploadFile aFile)
      throws Exception {

    String s_DCTMFolderId = DCTMUtils.makeEDMFolder(idfSession);
//  String s_CabinetCode  = "d00004";
//  String s_SecLevel     = "T";

    IDfDocument idfDoc = (IDfDocument) idfSession.newObject(SysObjectType.DOC.getValue());
    // 문서명
    idfDoc.setObjectName(aFile.getFileNameOnly());
    idfDoc.setTitle(aFile.getFileNameOnly().concat(".").concat(aFile.getFileExtention()));

    // 포맷
    idfDoc.setContentType(aFile.getDContentType());

    // 소유자 지정 : Docbase Owner로 지정
    idfDoc.setOwnerName(idfSession.getDocbaseOwnerName());

    // 템플릿 ACL 적용
    idfDoc.setACLDomain(idfSession.getDocbaseOwnerName());
    
    idfDoc.setACLName(dto.getDocAcl());

    // 파일 set
    idfDoc.setFile(aFile.getNewFileLocation());
    // DCTM 폴더(화면에 보이는 부서 폴더 아님)
    idfDoc.link(s_DCTMFolderId);

    // 업무 속성 지정
    idfDoc.setString("u_cabinet_code", dto.getHamInfo().getUCabinetCode()); // 문서함코드
    idfDoc.setString("u_doc_key", "" + idfDoc.getChronicleId()); // 문서 키
    idfDoc.setString("u_fol_id", dto.getUpObjectId()); // 부서폴더
    idfDoc.setString("u_sec_level", dto.getSSecLevel()); // 보안등급
    idfDoc.setString("u_doc_status", dto.getDocStatus());
    idfDoc.setString("u_doc_flag", "S"); // S:일반, M:마이그레이션
    idfDoc.setString("u_file_ext", aFile.getFileExtention()); // 파일확장자
    idfDoc.setString("u_reg_user", dto.getDoc().getURegUser()); // 등록자
    idfDoc.setString("u_reg_date", (new DfTime()).toString()); // 등록자
    idfDoc.setString("u_update_date", (new DfTime()).toString()); // 업데이트 날짜
    
    idfDoc.setString("u_last_editor", dto.getDoc().getURegUser()); // 등록자
    //editors repeating
    idfDoc.appendString("u_editor", dto.getDoc().getURegUser());
    
    idfDoc.setString("u_pr_code", hamValidation(dto.getHamInfo().getHamType()) ? dto.getHamInfo().getMyCode() : null); //
    idfDoc.setString("u_pr_type", hamValidation(dto.getHamInfo().getHamType()) ? dto.getHamInfo().getHamType() : null); // 프로젝트/연구과제
    
    //TODO: 보존연한 참조해야함 현재 default 0 (영구 - 2999-12-31)
    LocalDateTime now = LocalDateTime.now();
    if (hamValidation(dto.getHamInfo().getHamType())) {
      idfDoc.setInt("u_preserve_flag", 0); // 보존년한 기본 영구
      if (DocStatus.CLOSED.getValue().equals(dto.getDocStatus())) {
        LocalDateTime expireDate = LocalDateTime.of(9999, 12, 31, 0, 0);
        IDfTime dfExpireDate = new DfTime (Timestamp.valueOf(expireDate)) ;
        idfDoc.setTime("u_expired_date", dfExpireDate);
      }
    } else {
      idfDoc.setInt("u_preserve_flag", dto.getDoc().getUPreserverFlag()); // 보존년한
      if (DocStatus.CLOSED.getValue().equals(dto.getDocStatus())) {
        int preserverFlag = dto.getDoc().getUPreserverFlag();
        LocalDateTime expireDate = now.plusYears(preserverFlag);
        IDfTime dfExpireDate = new DfTime (Timestamp.valueOf(expireDate)) ;
        idfDoc.setTime("u_expired_date", dfExpireDate);
      }
    }
    //파일 신규 생성시 보존연한 X
//    idfDoc.setString("u_expired_date", DateTimeUtils.addYears(dto.getDoc().getUPreserverFlag()));
    
    if (!CheckString(dto.getDoc().getURegSource())) idfDoc.setString("u_reg_source", dto.getDoc().getURegSource()); // 등록소스구분', SET COMMENT_TEXT='D:DBox, P:PC'
    if (!CheckString(dto.getDoc().getUWfDocYn())) idfDoc.setString("u_wf_doc_yn", dto.getDoc().getUWfDocYn()); // 결재문서여부', DEFAULT 'N'


    if (DocStatus.CLOSED.getValue().equals(dto.getDocStatus())) {
      IDfTime closedDate = new DfTime (Timestamp.valueOf(now));
      idfDoc.setString("u_closer"               , dto.getDoc().getURegUser());
      idfDoc.setTime("u_closed_date"            , closedDate);
    } else {
      if (!CheckString(dto.getDoc().getUCloser())) idfDoc.setString("u_closer", dto.getDoc().getUCloser()); // 결재문서여부', DEFAULT 'N'
      if (!CheckString(dto.getDoc().getUCloser())) idfDoc.setString("u_closed_date", (new DfTime()).toString()); // Closed 처리일시
    }
    
    if (!dto.getDoc().getUPrivacyFlag()) idfDoc.setBoolean("u_privacy_flag", dto.getDoc().getUPrivacyFlag()); // 개인정보포함여부
    idfDoc.setBoolean("u_auto_auth_mail_flag", dto.getDoc().isUAutoAuthMailFlag()); // 메일자동권한부여
    idfDoc.setBoolean("u_takeout_flag", dto.getDoc().isUTakeoutFlag()); // 반출 여부
    idfDoc.setBoolean("u_ver_keep_flag", dto.getDoc().isUVerKeepFlag()); // 버전 유지 여부
    
    
    if (!CheckString(dto.getDoc().getUCopyOrgId())) idfDoc.setString("u_copy_org_id", dto.getDoc().getUCopyOrgId()); // 복사원본 ID
    if (!CheckString(dto.getDoc().getUDocTag())) idfDoc.setString("u_doc_tag", dto.getDoc().getUDocTag()); // 태그
    
    if (!CheckString(dto.getDoc().getUDocClass())) idfDoc.setString("u_doc_class", dto.getDoc().getUDocClass()); // 분류
    
    if (!CheckString(dto.getDeleteStatus())) idfDoc.setString("u_delete_status", dto.getDeleteStatus()); 			// Office 에서 다른 이름 저장시 ( 임시 저장 상태 )
    
//    if (!CheckString(dto.getDoc().getURegSource())) idfDoc.setString("u_recycle_date", dto.getDoc().getUWfDocYn()); // 휴지통으로 삭제일
    // 코드
//  u_wf_system                 CHAR(100) REPEATING (SET LABEL_TEXT='결재시스템명'),
//  u_wf_form                   CHAR(100) REPEATING (SET LABEL_TEXT='결재양식명'),
//  u_wf_title                  CHAR(200) REPEATING (SET LABEL_TEXT='결재제목'),
//  u_wf_approver               CHAR(100) REPEATING (SET LABEL_TEXT='결재자'),
//  u_wf_approval_date          TIME REPEATING (SET LABEL_TEXT='결재일'),
//  u_wf_key                    CHAR(100) REPEATING (SET LABEL_TEXT='결재키'),
//  u_wf_link                   CHAR(250) REPEATING (SET LABEL_TEXT='결재링크'),
    
    return idfDoc;
  }

  public static IDfDocument toIDfDocument(IDfSession idfSession, UploadDocDto dto, AttachedFile aFile)
      throws Exception {

    // tmp
    String s_DCTMFolderId = DCTMUtils.makeEDMFolder(idfSession);
//    String s_CabinetCode  = "d00004";
//    String s_SecLevel     = "T";

    IDfDocument idfDoc = (IDfDocument) idfSession.newObject(SysObjectType.DOC.getValue());
    // 문서명
    idfDoc.setObjectName(aFile.getOriginalFileName());

    // 포맷
    idfDoc.setContentType(aFile.getDContentType());

    // 소유자 지정 : Docbase Owner로 지정
    idfDoc.setOwnerName(idfSession.getDocbaseOwnerName());

    // 템플릿 ACL 적용
    idfDoc.setACLDomain(idfSession.getDocbaseOwnerName());
    idfDoc.setACLName("a_" + dto.getHamInfo().getUCabinetCode());

    // 추가 권한자 지정 : 기본 권한이나 공유/협업 대상자 등
    // 추가 권한자를 추가하면 ACL명이 'dm_45XXXXXXXXXXXXXX' 형식(Custom ACL)으로 변경 됨
//    idfDoc.grant("d00005", 3, ""); // 조회/다운로드 권한(부서) : 부서에 해당하는 DCTM 그룹(케비넷 코드)
//    idfDoc.grant("user1", 3, ""); // 조회/다운로드 권한(개인) : 사용자 ID
//    idfDoc.grant("d00006", 7, ""); // 편집/삭제 권한
//    idfDoc.grant("user2", 7, ""); // 편집/삭제 권한

//    for (Map.Entry<String, String> entry : dto.getAddGrantedMap().entrySet()) {
//      idfDoc.grant(entry.getKey(), GrantedLevels.findByLevel(entry.getValue()), "");
//    }

    // 파일 set
    idfDoc.setFile(aFile.getTmpFileName());
    // DCTM 폴더(화면에 보이는 부서 폴더 아님)
    idfDoc.link(s_DCTMFolderId);

    // 업무 속성 지정
    idfDoc.setString("u_cabinet_code", dto.getHamInfo().getUCabinetCode()); // 문서함코드
    idfDoc.setString("u_doc_key", "" + idfDoc.getChronicleId()); // 문서 키
    idfDoc.setString("u_fol_id", dto.getUpObjectId()); // 부서폴더
    idfDoc.setString("u_sec_level", dto.getSSecLevel()); // 보안등급
    idfDoc.setString("u_doc_status", "L");
    idfDoc.setString("u_doc_flag", "S"); // S:일반, M:마이그레이션
    idfDoc.setString("u_file_ext", aFile.getFileExtention()); // 파일확장자
    idfDoc.setString("u_reg_source", dto.uRegSource); // 등록소스구분', SET COMMENT_TEXT='D:DBox, P:PC'
    idfDoc.setString("u_reg_user", idfSession.getDocbaseOwnerName()); // 등록자
    idfDoc.setString("u_reg_date", (new DfTime()).toString()); // 등록자
    idfDoc.setString("u_update_date", (new DfTime()).toString()); // 업데이트 날짜
    idfDoc.setString("u_pr_code", hamValidation(dto.getHamInfo().getHamType()) ? dto.getHamInfo().getHamType() : null); //
    idfDoc.setString("u_pr_type", hamValidation(dto.getHamInfo().getHamType()) ? dto.getHamInfo().getMyCode() : null); // 프로젝트/연구과제
                                                                                                                       // 코드
    if (dto.getDoc() != null) {
      idfDoc.setString("u_wf_doc_yn", dto.getDoc().getUWfDocYn()); // 결재문서여부', DEFAULT 'N'
      idfDoc.setString("u_closed_date", dto.getDoc().getUWfDocYn()); // Closed 처리일시
      idfDoc.setString("u_closer", dto.getDoc().getUWfDocYn()); // 결재문서여부', DEFAULT 'N'
      idfDoc.setString("u_editor", dto.getDoc().getUWfDocYn()); // 결재문서여부', DEFAULT 'N'
      idfDoc.setString("u_open_flag", dto.getDoc().getUWfDocYn()); // 공개여부', DEFAULT 1
      idfDoc.setString("u_privacy_flag", dto.getDoc().getUWfDocYn()); // 결재문서여부', DEFAULT 'N'
      idfDoc.setString("u_preserve_flag", dto.getDoc().getUWfDocYn()); // 보존년한
      idfDoc.setString("u_expired_date", dto.getDoc().getUWfDocYn()); // 보존년한 만료일
      idfDoc.setString("u_copy_org_id", dto.getDoc().getUWfDocYn()); // 복사원본 ID
      idfDoc.setString("u_doc_tag", dto.getDoc().getUWfDocYn()); // 태그
      idfDoc.setString("u_doc_class", dto.getDoc().getUWfDocYn()); // 분류
      idfDoc.setString("u_auto_auth_mail_flag", dto.getDoc().getUWfDocYn()); // 메일자동권한부여
      idfDoc.setString("u_takeout_flag", dto.getDoc().getUWfDocYn()); // 반출 여부
      idfDoc.setString("u_ver_keep_flag", dto.getDoc().getUWfDocYn()); // 버전 유지 여부
      idfDoc.setString("u_recycle_date", dto.getDoc().getUWfDocYn()); // 휴지통으로 삭제일
//      idfDoc.setString("u_editor_names", dto.getDoc().getUWfDocYn()); //작성자들 출력 명', SET COMMENT_TEXT='edms_doc_r.edms_trg_set_doc_editor_names trigger에서 자동 Update'
//      idfDoc.setString("u_folder_path", dto.getDoc().getUWfDocYn()); //폴더경로', SET COMMENT_TEXT='edms_doc_s와 edms_folder_s의 edms_trg_set_doc_folder_path trigger에서 자동 Update'
//      idfDoc.setString("u_update_date", dto.getDoc().getUWfDocYn()); //파일수정일', SET COMMENT_TEXT='dmr_content_r.edms_trg_set_doc_update_date trigger에서 자동 Update'),
//    u_wf_system                 CHAR(100) REPEATING (SET LABEL_TEXT='결재시스템명'),
//    u_wf_form                   CHAR(100) REPEATING (SET LABEL_TEXT='결재양식명'),
//    u_wf_title                  CHAR(200) REPEATING (SET LABEL_TEXT='결재제목'),
//    u_wf_approver               CHAR(100) REPEATING (SET LABEL_TEXT='결재자'),
//    u_wf_approval_date          TIME REPEATING (SET LABEL_TEXT='결재일'),
//    u_wf_key                    CHAR(100) REPEATING (SET LABEL_TEXT='결재키'),
//    u_wf_link                   CHAR(250) REPEATING (SET LABEL_TEXT='결재링크'),
    }

    return idfDoc;
  }
  
  public static String makeAclName(String cabinetCode, SecLevelCode secCode, HamType hamType, boolean isLive, boolean isPrivate) {
    String aclName = MessageFormat.format(AclTemplate.DEPT_BASIC_ACL_LIVE.getValue(), cabinetCode);
    switch (hamType) {
    //부서  문서
    case DEPT:
      if (isPrivate) {
        if (isLive) {
          return MessageFormat.format(AclTemplate.DEPT_PRIVACY_ACL_LIVE.getValue(), cabinetCode);
        } else {
          return MessageFormat.format(AclTemplate.DEPT_PRIVACY_ACL_SEC.getValue(), cabinetCode);
        }
      } else {
        if (isLive) {
          return MessageFormat.format(AclTemplate.DEPT_BASIC_ACL_LIVE.getValue(), cabinetCode);
        } else {
          switch (secCode) {
          case TEAM:
            return MessageFormat.format(AclTemplate.DEPT_BASIC_ACL_TEAM.getValue(), cabinetCode);
          case COMPANY:
            return MessageFormat.format(AclTemplate.DEPT_BASIC_ACL_COM.getValue(), cabinetCode);
          case GROUP:
            return MessageFormat.format(AclTemplate.DEPT_BASIC_ACL_GROUP.getValue(), cabinetCode);
          case SEC:
            return MessageFormat.format(AclTemplate.DEPT_BASIC_ACL_SEC.getValue(), cabinetCode);
          }
        }
      }
      break;
    //중요 문서
    case IMPORTANT: 
      return MessageFormat.format(AclTemplate.DEPT_IMP_ACL.getValue(), cabinetCode);
    case PROJECT:
      if (isPrivate) {
        if (isLive) {
          return MessageFormat.format(AclTemplate.PJT_PRIVACY_ACL_LIVE.getValue(), cabinetCode);
        } else {
          return MessageFormat.format(AclTemplate.PJT_PRIVACY_ACL_SEC.getValue(), cabinetCode);
        }
      } else {
        if (isLive) {
          return MessageFormat.format(AclTemplate.PJT_BASIC_ACL_LIVE.getValue(), cabinetCode);
        } else {
          switch (secCode) {
          case TEAM:
            return MessageFormat.format(AclTemplate.PJT_WF_ACL_TEAM.getValue(), cabinetCode);
          case COMPANY:
            return MessageFormat.format(AclTemplate.PJT_WF_ACL_COM.getValue(), cabinetCode);
          case GROUP:
            return MessageFormat.format(AclTemplate.PJT_WF_ACL_GROUP.getValue(), cabinetCode);
          case SEC:
            return MessageFormat.format(AclTemplate.PJT_WF_ACL_SEC.getValue(), cabinetCode);
          }
        }
      }
      break;
    case RESEARCH:
      if (isPrivate) {
        if (isLive) {
          return MessageFormat.format(AclTemplate.RSCH_PRIVACY_ACL_LIVE.getValue(), cabinetCode);
        } else {
          return MessageFormat.format(AclTemplate.RSCH_PRIVACY_ACL_SEC.getValue(), cabinetCode);
        }
      } else {
        if (isLive) {
          return MessageFormat.format(AclTemplate.RSCH_BASIC_ACL_LIVE.getValue(), cabinetCode);
        } else {
          switch (secCode) {
          case TEAM:
            return MessageFormat.format(AclTemplate.RSCH_BASIC_ACL_TEAM.getValue(), cabinetCode);
          case COMPANY:
            return MessageFormat.format(AclTemplate.RSCH_BASIC_ACL_COM.getValue(), cabinetCode);
          case GROUP:
            return MessageFormat.format(AclTemplate.RSCH_BASIC_ACL_GROUP.getValue(), cabinetCode);
          case SEC:
            return MessageFormat.format(AclTemplate.RSCH_BASIC_ACL_SEC.getValue(), cabinetCode);
          }
        }
      }
      break;
    }
    return aclName;
  }
  
}









