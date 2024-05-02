package com.dongkuksystems.dbox.models.dto.path;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.bind.annotation.RequestParam;

import com.dongkuksystems.dbox.models.dto.type.auth.HamInfoResult;
import com.dongkuksystems.dbox.models.type.user.UserPreset;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiParam;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder 
public class DPath {

  @ApiModelProperty(value = "DOC TYPE부분(IMP:중요문서, '':일반문서)")
  private String docTypeGbn;
	
  @ApiModelProperty(value = "Object ID")
  private String rObjectId;

  @ApiModelProperty(value = "타입(FOL:폴더,DOC:문서)")
  private String listType;
	  
  @ApiModelProperty(value = "상위폴더 ID")//상위폴더-타입별로 필터링 해서 처리하는 용도로 추가
  private String uFolId;

  @ApiModelProperty(value = "대상의 보안등급")//T,G,C,D
  private String uSecLevel;

  
  @ApiModelProperty(value = "송신문서합구분") //'D**:부서함, W**:결재폴더, I**:중요문서함, P**:프로젝트, R**:연구과제, S**:공유/협업, O**:조직함'
  private String sourceGubun;

  @ApiModelProperty(value = "송신문서합프로젝트/연구과제 코드")
  private String sourcePjtCode;
  
  @ApiModelProperty(value = "요청자")
  private String reqUser;

  @ApiModelProperty(value = "요청자IP")
  private String reqUserIp;
  
  @ApiModelProperty(value = "요청자 프리셋")
  private UserPreset userPreset; 
  
  @ApiModelProperty(value = "대상부서코드(캐비닛코드아님)")
  private String TgOrgId; 
  
  @ApiModelProperty(value = "승인자")  //팀장
  private String uApprover;

  @ApiModelProperty(value = "승인자 이름") 
  private String uApproverNm;
  
  @ApiModelProperty(value = "승인자 이메일")
  private String uApproverEmail;

  @ApiModelProperty(value = "승인자 전화번호")
  private String uApproverPhoneNo;

  @ApiModelProperty(value = "Link삭제건수")
  private int linkDelCnt;
  
  @ApiModelProperty(value = "삭제건수") //휴지통으로 간 건수
  private int reCycleCnt;
  
  @ApiModelProperty(value = "삭제문자열")//** 휴지통 처리 관련 문자열 표시용
  private String reCycleStr;
  
  @ApiModelProperty(value = "폐기요청건수") //폐기요청건수
  private int reqDelCnt;

  @ApiModelProperty(value = "폐기요청등록ID") //req_delete의 r_object_id
  private String reqDelObjId;

  @ApiModelProperty(value = "폐기요청문서ID") //폐기요청문서ID
  private String reqDelDocKey;
  
  @ApiModelProperty(value = "폐기요청내역문자열")//메일이나 카카오톡 보낼때, **외 *건 이렇게 처리
  private String reqDelStr;
  
  @ApiModelProperty(value = "현재파일 상위폴더의 u_fol_status값")
  private String ufolStatus;  //파일명 중복체크할 때, 상위폴더의 u_fol_status를 변경한다.
  
  
  @ApiModelProperty(value = "요청상태_이관시 요청없이이관, 요청구분")
  private String reqStatus;
  
  @ApiModelProperty(value = "송신문서함명_팀명")
  private String orgNm;

  @ApiModelProperty(value = "수신문서함의 보안등급")
  private String targetSecLevel;

  
  @ApiModelProperty(value = "복사로 신규 생성전폴더ID") 
  private String preFolderId;
  
  @ApiModelProperty(value = "프로젝트코드")
  private String prCode;

  @ApiModelProperty(value = "프로젝트 타입")
  private String prType;

  @ApiModelProperty(value = "최종_복사대상폴더타입(edms_folder의 u_fol_type):서버인자" , example = "DFO")
  private String targetFolType;
  
  @ApiModelProperty(value = "프로젝트변경포함여부 :서버인자"  )
  private String uPrCodeCheck;


  @ApiModelProperty(value = "송신문서합코드")
  private String srcCabinetcode;

  @ApiModelProperty(value = "송신부서코드")
  private String ownSrDeptOrgId;
  
  
  @ApiModelProperty(value = "회사문서함코드")
  private String uComOrgCabinetCd;
  
  @ApiModelProperty(value = "그룹문서함코드")
  private String uGroupOrgCabinetCd;
  
  
  @ApiModelProperty(value = "수신문서합코드")
  private String tgCabinetcode;

  @ApiModelProperty(value = "수신부서코드")
  private String ownTgDeptOrgId;

  @ApiModelProperty(value = "수신부서_회사코드")
  private String tgComId;
  
  
  @ApiModelProperty(value = "송신_회사코드")
  private String srcComOrgId;
  
  @ApiModelProperty(value = "함 정보")
  private HamInfoResult hamInfo;
  
  @ApiModelProperty(value = "현재 작업자의 작업자구분(P:개인, D:부서관리자, C:사별관리자, G:그룹관리자, S:시스템)")
  private String uJobUserType;
  
  @ApiModelProperty(value = "이벤트의 알람 대상여부")
  private String uAlarmYn;

  @ApiModelProperty(value = "이벤트의 이메일발송 대상여부")
  private String uEmailYn;

  @ApiModelProperty(value = "이벤트의 MMS발송 대상여부")
  private String uMmsYn;

  @ApiModelProperty(value = "이벤트의 대상 회사코드")
  private String uComCode;

  @ApiModelProperty(value = "파일의 상태(L:Live, C:Close, K:링크")
  private String uObjStatus;
  

 /******************** 작업 파라메터 (API 정의서 항목) Start *******************/
  @ApiModelProperty(value = "복사,이동,삭제를 선택한 폴더id, root폴더는 ' '")  //링크파일 개별 복사,이동및 삭제처리를 위해서 추가함
  private String srcFolId;
  
  @ApiModelProperty(value = "처리구분(C:복사,M:이동,T:이관,D:삭제  :필수Param"  , example = "C")
  private String uptPthGbn;
  
  @ApiModelProperty(value = "선택한 디렉토리(들)")
  private List<String> sourceFolders;

  @ApiModelProperty(value = "선택한 반출함 파일(들)")
  private List<String> sourceTFiles;

  @ApiModelProperty(value = "선택한 반출함 파일(들)의 요청ID 들")
  private List<String> sourceTIds;
  
  @ApiModelProperty(value = "선택한 파일(들)")
  private List<String> sourceFiles;

  @ApiModelProperty(value = "선택한 프로젝트/투자(들)")
  private List<String> sourcePjts;

  @ApiModelProperty(value = "선택한 연구과제(들)")
  private List<String> sourceRscs;

  @ApiModelProperty(value = "복사대상폴더구분(edms_folder의 u_fol_type):필수Param" , example = "DFO")
  private String targetGubun;

  @ApiModelProperty(value = "복사대상폴더"    , example = "000004d280003a10")
  private String targetDboxId;
  
  @ApiModelProperty(value = "이관사유"       , example = "이관사유입력란")
  private String transCause;

  
  @ApiModelProperty(value = "결과 메시지" )
  private List<String> returnStr;
  
  /******************** 작업 파라메터 (API 정의서 항목) End *******************/
  
}
