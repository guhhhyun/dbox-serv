package com.dongkuksystems.dbox.models.type.doc;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import com.dongkuksystems.dbox.constants.Commons;
import com.dongkuksystems.dbox.models.table.etc.VDept;
import com.dongkuksystems.dbox.models.table.etc.VUser;
import com.dongkuksystems.dbox.models.type.auth.AuthBase;
import com.dongkuksystems.dbox.models.type.feedback.Feedback;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Doc {
  @ApiModelProperty(value = "검색문서번호", required = true)
  private Integer searchRowNum;
  @ApiModelProperty(value = "문서 id", required = true)
  private String rObjectId;
  @ApiModelProperty(value = "프로젝트/연구과제 코드", required = true)
  private String uPrCode;
  @ApiModelProperty(value = "프로젝트/연구과제 타입", required = true)
  private String uPrType;
  @ApiModelProperty(value = "소속부서코드(테이블 컬럼X)", hidden = true)
  private String uDeptCode;
  @ApiModelProperty(value = "소속회사코드(테이블 컬럼X)", hidden = true)
  private String uComCode;
  @ApiModelProperty(value = "문서 이름(확장자 미포함)")
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
  @ApiModelProperty(value = "문서상태")
  private String uDocStatus;
  @ApiModelProperty(value = "문서구분자")
  private String uDocFlag;
  @ApiModelProperty(value = "삭제상태")
  private String uDeleteStatus;
  @Builder.Default
  @ApiModelProperty(value = "결재문서여부")
  private String uWfDocYn = "N";
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "Closed 처리 일시")
  private LocalDateTime uClosedDate;
  @ApiModelProperty(value = "Closed 처리자", example = "사용자ID or 'SYSTEM'")
  private String uCloser;
  @ApiModelProperty(value = "Closed 처리자 상세")
  private VUser closerDetail;
  @Builder.Default
  @ApiModelProperty(value = "개인정부포함여부")
  private Boolean uPrivacyFlag = false;
  @Builder.Default
  @ApiModelProperty(value = "보존년한")
  private int uPreserverFlag = 0;
  @Builder.Default
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "보존년한 만료일")
  private LocalDateTime uExpiredDate = LocalDateTime.of(2099, 12, 31, 0, 0, 0);
  @ApiModelProperty(value = "복사원본 ID")
  private String uCopyOrgId;
  @ApiModelProperty(value = "파일확장자")
  private String uFileExt;
  @ApiModelProperty(value = "태그")
  private String uDocTag;
  @ApiModelProperty(value = "분류")
  private String uDocClass;
  @ApiModelProperty(value = "등록소스구분")
  private String uRegSource;
  @ApiModelProperty(value = "등록자 아이디")
  private String uRegUser;
  @JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss")
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "등록일")
  private LocalDateTime uRegDate;
  @ApiModelProperty(value = "메일자동권한부여")
  private boolean uAutoAuthMailFlag;
  @ApiModelProperty(value = "작성자들 출력명")
  private String uEditorNames;
  @ApiModelProperty(value = "폴더경로")
  private String uFolderPath;
  @JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss")
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "수정일")
  private LocalDateTime uUpdateDate;
  @ApiModelProperty(value = "복호화반출여부")
  private boolean uTakeoutFlag;
  @ApiModelProperty(value = "버전유지여부")
  private boolean uVerKeepFlag;
  @ApiModelProperty(value = "마지막 파일 편집자")
  private String uLastEditor;
  
  @ApiModelProperty(value = "보유 최대 권한")
  private int maxLevel;
  @ApiModelProperty(value = "피드백 개수")
  private int feedbackCount;
  
  @JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss")
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "휴지통으로 삭제일")
  private LocalDateTime uRecycleDate;
  @ApiModelProperty(value = "등록자 상세")
  private VUser regUserDetail;
  @ApiModelProperty(value = "링크파일 정보")
  private DocLink docLink;
  @ApiModelProperty(value = "복사 원본 상세")
  private Doc copyOrgDetail;
  @ApiModelProperty(value = "소유부서 상세")
  private VDept ownDeptDetail;
 
  @ApiModelProperty(value = "피드백")
  private List<Feedback> feedbacks;
  @ApiModelProperty(value = "권한")
  private List<AuthBase> authBases;
 
  @ApiModelProperty(value = "반복 타입")
  private List<DocRepeating> docRepeatings;

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

  public void setURecycleDate(LocalDateTime uRecycleDate) {
    if (uRecycleDate == null) this.uRecycleDate = null;
    else {
      ZonedDateTime zdt = uRecycleDate.atZone(ZoneId.of("Asia/Seoul"));
      long milli = zdt.toInstant().toEpochMilli();
      this.uRecycleDate = Commons.NULL_DATE == milli  ? null : uRecycleDate;
    }
  }
}
