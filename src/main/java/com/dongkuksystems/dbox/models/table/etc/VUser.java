package com.dongkuksystems.dbox.models.table.etc;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.dongkuksystems.dbox.constants.Commons;
import com.dongkuksystems.dbox.models.dto.type.manager.managerconfig.ManagerConfigDto;
import com.fasterxml.jackson.annotation.JsonFormat;
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
public class VUser {
  @ApiModelProperty(value = "특이사용자 잠금처리 필드")
  private String userState;
  @ApiModelProperty(value = "특이사용자 잠금처리 필드/ 잠금구분 / M:관리자잠금, O:특이사용잠금, S:동기화잠금, L:기준초과'")
  private String uLockType;
  @ApiModelProperty(value = "특이사용자 잠금처리 필드 / 잠금상태 / L:잠금, U:해제, N:기본")
  private String uLockStatus;
  @ApiModelProperty(value = "그룹웨어 ID(외부사용자는 메일주소형태)", required = true, example = "1234")
  private String userId;
  @ApiModelProperty(value = "이름", required = true)
  private String displayName;
  @ApiModelProperty(value = "계정의 활성/비활성 (A:정상, T:등록, I:승인불가)", required = true)
  private String usageState;
  @ApiModelProperty(value = "사번", required = true)
  private String sabun;
  @ApiModelProperty(value = "Object Id")
  private String rObjectId;
  @ApiModelProperty(value = "회사코드")
  private String comOrgId;
  @ApiModelProperty(value = "회사명")
  private String comOrgNm;
  @ApiModelProperty(value = "부서코드")
  private String orgId;
  @ApiModelProperty(value = "GW 부서코드")
  private String gwOrgId;
  @ApiModelProperty(value = "부서명")
  private String orgNm;
  @ApiModelProperty(value = "직위코드")
  private String pstnCode; 
  @ApiModelProperty(value = "직위명")
  private String pstnName; 
  @ApiModelProperty(value = "직급코드")
  private String levelCode;
  @ApiModelProperty(value = "직급명")
  private String levelName; 
  @ApiModelProperty(value = "직책코드")
  private String titleCode; 
  @ApiModelProperty(value = "직책명")
  private String titleName;
  @ApiModelProperty(value = "이메일", required = true)
  private String email;
  @JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss")
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "입사일")
  private LocalDateTime enterDt;
  @JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss")
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  private LocalDateTime enterDate;
  @JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss")
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "퇴사일")
  private LocalDateTime exitDate;
  @JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss")
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "입사예정일")
  private LocalDateTime insertReserveDate;
  @JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss")
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "퇴사예정일")
  private LocalDateTime exitReserverDate;
  @ApiModelProperty(value = "결재수신메일사용")
  private String receivemailUsage;
  @ApiModelProperty(value = "자동 sign on 처리 여부")
  private String autosignonUsage;
  @ApiModelProperty(value = "팀장여부")
  private String chiefFlag;
  @ApiModelProperty(value = "입/퇴사여부")
  private String inoutType;
  @ApiModelProperty(value = "다국어코드")
  private String langCd;
  @ApiModelProperty(value = "정렬키")
  private String sortKey;
  @ApiModelProperty(value = "핸드폰 번호")
  private String mobileTel;
  @ApiModelProperty(value = "영문명")
  private String displayNameEng;
  @ApiModelProperty(value = "임직원/협력사/외부사용자 여부(EMP/PTN/EXT)")
  private String memTypeCd;
  @ApiModelProperty(value = "근무지코드(본사:01,부산:02,인천:03,포항:04,당진:05,신평:06,대전:07,대구:08,호남:09)")
  private String workArea;
  @ApiModelProperty(value = "근무지명(본사:01,부산:02,인천:03,포항:04,당진:05,신평:06,대전:07,대구:08,호남:09)")
  private String workAreaNm;
  @ApiModelProperty(value = "임직원 유형(0:임원,1:관리직,2:기능직,3:협력업체) TQM용")
  private String empType;
  @ApiModelProperty(value = "LOCK")
  private String idStatus;
  @ApiModelProperty(value = "공용계정여부")
  private String shareYn;
  @ApiModelProperty(value = "현지채용자여부")
  private String localEmpYn;

  @ApiModelProperty(value = "직책 상세")
  private GwJobTitle jobTitleDetail;
  @ApiModelProperty(value = "부서 상세")
  private VDept deptDetail;
  @ApiModelProperty(value = "소속부서 cabinetCode", notes = "임의추가,컬럼에없음")
  private String deptCabinetcode;
  @ApiModelProperty(value = "소속회사 cabinetCode", notes = "임의추가,컬럼에없음")
  private String comCabinetcode;
  @ApiModelProperty(value = "겸직", notes = "임의추가,컬럼에없음")
  private List<GwAddJob> addDepts;
  @ApiModelProperty(value = "관리자")
  private ManagerConfigDto mgr;

  public void login(PasswordEncoder passwordEncoder, String credentials) {
  }
  

  public void setEnterDt(LocalDateTime enterDt) {
    if (enterDt == null) this.enterDt = null;
    else {
      ZonedDateTime zdt = enterDt.atZone(ZoneId.of("Asia/Seoul"));
      long milli = zdt.toInstant().toEpochMilli();
      this.enterDt = Commons.NULL_DATE == milli  ? null : enterDt;
    }
  }

  public void setEnterDate(LocalDateTime enterDate) {
    if (enterDate == null) this.enterDate = null;
    else {
      ZonedDateTime zdt = enterDate.atZone(ZoneId.of("Asia/Seoul"));
      long milli = zdt.toInstant().toEpochMilli();
      this.enterDate = Commons.NULL_DATE == milli  ? null : enterDate;
    }
  }

  public void setExitDate(LocalDateTime exitDate) {
    if (exitDate == null) this.exitDate = null;
    else {
      ZonedDateTime zdt = exitDate.atZone(ZoneId.of("Asia/Seoul"));
      long milli = zdt.toInstant().toEpochMilli();
      this.exitDate = Commons.NULL_DATE == milli  ? null : exitDate;
    }
  }
  
  public void setInsertReserveDate(LocalDateTime insertReserveDate) {
    if (insertReserveDate == null) this.insertReserveDate = null;
    else {
      ZonedDateTime zdt = insertReserveDate.atZone(ZoneId.of("Asia/Seoul"));
      long milli = zdt.toInstant().toEpochMilli();
      this.insertReserveDate = Commons.NULL_DATE == milli  ? null : insertReserveDate;
    }
  }
  
  public void setExitReserverDate(LocalDateTime exitReserverDate) {
    if (exitReserverDate == null) this.exitReserverDate = null;
    else {
      ZonedDateTime zdt = exitReserverDate.atZone(ZoneId.of("Asia/Seoul"));
      long milli = zdt.toInstant().toEpochMilli();
      this.exitReserverDate = Commons.NULL_DATE == milli  ? null : exitReserverDate;
    }
  }
}
