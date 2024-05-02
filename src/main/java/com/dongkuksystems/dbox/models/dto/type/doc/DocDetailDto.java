package com.dongkuksystems.dbox.models.dto.type.doc;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import com.dongkuksystems.dbox.constants.Commons;
import com.dongkuksystems.dbox.models.dto.etc.SimpleDeptDto;
import com.dongkuksystems.dbox.models.dto.etc.SimpleUserDto;
import com.dongkuksystems.dbox.models.table.etc.VUser;
import com.dongkuksystems.dbox.models.type.auth.AuthBase;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public class DocDetailDto {
    @ApiModelProperty(value = "문서 id", required = true)
    private String rObjectId;
    @ApiModelProperty(value = "프로젝트/연구과제 코드")
    private String uPrCode;
    @ApiModelProperty(value = "프로젝트/연구과제 타입")
    private String uPrType;
    @ApiModelProperty(value = "문서 이름")
    private String objectName;
    @ApiModelProperty(value = "문서 이름 + 확장자")
    private String title;
    @ApiModelProperty(value = "LockOwner 편집중사용자")
    private String rLockOwner;
    @ApiModelProperty(value = "LockOwner 편집중사용자 상세")
    private VUser lockOwnerDetail;
    @ApiModelProperty(value = "용량")
    private String rContentSize;
    @ApiModelProperty(value = "문서함 코드", required = true)
    private String uCabinetCode;
    @ApiModelProperty(value = "문서 키", required = true)
    private String uDocKey;
    @ApiModelProperty(value = "폴더 ID")
    private String uFolId;
    @ApiModelProperty(value = "보안등급")
    private String uSecLevel;
    @ApiModelProperty(value = "보안등급 이름")
    private String secLevelName;
    @ApiModelProperty(value = "문서상태")
    private String uDocStatus;
    @ApiModelProperty(value = "문서상태 이름")
    private String docStatusName;
    @ApiModelProperty(value = "문서구분자")
    private String uDocFlag;
    @ApiModelProperty(value = "삭제상태")
    private String uDeleteStatus;
    @ApiModelProperty(value = "결재문서여부")
    private String uWfDocYn;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @ApiModelProperty(value = "Closed 처리 일시")
    private LocalDateTime uClosedDate;
    @ApiModelProperty(value = "Closed 처리자", example = "사용자ID or 'SYSTEM'")
    private String uCloser;
    @ApiModelProperty(value = "Closed 처리자 상세")
    private VUser closerDetail;
    @ApiModelProperty(value = "개인정부포함여부")
    private Boolean uPrivacyFlag;
    @ApiModelProperty(value = "보존년한")
    private Integer uPreserverFlag;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @ApiModelProperty(value = "보존년한 만료일")
    private LocalDateTime uExpiredDate;
    @ApiModelProperty(value = "복사원본 ID")
    private String uCopyOrgId;
    @ApiModelProperty(value = "통합검색 버전")
    private String uVerSion;
    @ApiModelProperty(value = "파일확장자")
    private String uFileExt;
    @ApiModelProperty(value = "태그")
    private String uDocTag;
    @ApiModelProperty(value = "분류")
    private String uDocClass;
    @ApiModelProperty(value = "등록소스구분")
    private String uRegSource;
    @ApiModelProperty(value = "등록자")
    private String uRegUser;
    @JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @ApiModelProperty(value = "등록일")
    private LocalDateTime uRegDate;
    @JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @ApiModelProperty(value = "생성일")
    private LocalDateTime uCreateDate;
    @ApiModelProperty(value = "메일자동권한부여")
    private Boolean uAutoAuthMailFlag;
    @ApiModelProperty(value = "작성자들 출력명")
    private String uEditorNames;
    @ApiModelProperty(value = "최종결제자")
    private String approval_user_id;
    @ApiModelProperty(value = "결제링크")
    private String approval_link;
    @ApiModelProperty(value = "폴더경로")
    private String uFolderPath;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @ApiModelProperty(value = "수정일")
    private LocalDateTime uUpdateDate;
    @ApiModelProperty(value = "피드백 개수")
    private int feedbackCount;
    @ApiModelProperty(value = "내용")
    private String docContent;

    @ApiModelProperty(value = "최종 버전")
    private String lastVersion;

    @ApiModelProperty(value = "읽기 가능 여부")
    private Boolean readable;
    @ApiModelProperty(value = "보유 최대 권한")
    private int maxLevel;
 
    @ApiModelProperty(value = "생성일")
    private String createTime;
    @ApiModelProperty(value = "통합검색 문서수정일")
    private String updateTime;
    @ApiModelProperty(value = "통합검색 문서Close일")
    private String uclosedDate;
    @ApiModelProperty(value = "등록자 상세")
    private SimpleUserDto regUserDetail;
    @ApiModelProperty(value = "링크파일 정보")
    private DocLinkDto docLink;
    @ApiModelProperty(value = "복사 원본 상세")
    private DocDetailDto copyOrgDetail;
    @ApiModelProperty(value = "소유부서 상세")
    private SimpleDeptDto ownDeptDetail;

    @ApiModelProperty(value = "버전 리스트")
    private List<String> rVersionLabel;

    @ApiModelProperty(value = "버전 유지 여부")
    private Boolean uVerKeepFlag;

    @ApiModelProperty(value = "작성자 리스트")
    private List<String> uEditor;
    @ApiModelProperty(value = "연관 결재정보 리스트")
    private List<DocApprovalDto> approvals;

    @ApiModelProperty(value = "Live 권한")
    private List<AuthBase> liveAuthBases;
    @ApiModelProperty(value = "Closed 권한")
    private List<AuthBase> closedAuthBases;

    public void setUClosedDate(LocalDateTime uClosedDate) {
      if (uClosedDate == null) this.uClosedDate = null;
      else {
        ZonedDateTime zdt = uClosedDate.atZone(ZoneId.of("Asia/Seoul"));
        long milli = zdt.toInstant().toEpochMilli();
        this.uClosedDate = Commons.NULL_DATE == milli  ? null : uClosedDate;
      }
    }
  
    public void setUExpiredDate(LocalDateTime uExpiredDate) {
      if (uExpiredDate == null) this.uExpiredDate = null;
      else {
        ZonedDateTime zdt = uExpiredDate.atZone(ZoneId.of("Asia/Seoul"));
        long milli = zdt.toInstant().toEpochMilli();
        this.uExpiredDate = Commons.NULL_DATE == milli  ? null : uExpiredDate;
      }
    }
  
    public void setURegDate(LocalDateTime uRegDate) {
      if (uRegDate == null) this.uRegDate = null;
      else {
        ZonedDateTime zdt = uRegDate.atZone(ZoneId.of("Asia/Seoul"));
        long milli = zdt.toInstant().toEpochMilli();
        this.uRegDate = Commons.NULL_DATE == milli  ? null : uRegDate;
      }
    }
    
    public void setUUpdateDate(LocalDateTime uUpdateDate) {
      if (uUpdateDate == null) this.uUpdateDate = null;
      else {
        ZonedDateTime zdt = uUpdateDate.atZone(ZoneId.of("Asia/Seoul"));
        long milli = zdt.toInstant().toEpochMilli();
        this.uUpdateDate = Commons.NULL_DATE == milli  ? null : uUpdateDate;
      }
    }
}
