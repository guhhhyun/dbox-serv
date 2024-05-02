package com.dongkuksystems.dbox.models.table.etc;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;

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
public class GwUser {
  @ApiModelProperty(value = "그룹웨어 ID(외부사용자는 메일주소형태)", required = true, example = "1234")
  private String socialPerId;
  @ApiModelProperty(value = "이름", required = true)
  private String displayName;
  @ApiModelProperty(value = "계정의 활성/비활성 (A:정상, T:등록, I:승인불가)", required = true)
  private String usageState;
  @ApiModelProperty(value = "사번", required = true)
  private String sabun;
  @ApiModelProperty(value = "회사코드")
  private String comOrgId;
  @ApiModelProperty(value = "부서코드")
  private String orgId;
  @ApiModelProperty(value = "부서명")
  private String orgNm;
  @ApiModelProperty(value = "직위코드")
  private String pstnCode; 
  @ApiModelProperty(value = "직급코드")
  private String levelCode; 
  @ApiModelProperty(value = "직책코드")
  private String titleCode;
  @ApiModelProperty(value = "이메일", required = true)
  private String email;
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "입사일")
  private LocalDateTime enterDate;
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "퇴사일")
  private LocalDateTime exitDate;
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "입사예정일")
  private LocalDateTime insertReserveDate;
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
  
  @ApiModelProperty(value = "소속부서 cabinetCode", notes = "임의추가,컬럼에없음")
  private String deptCabinetcode;

  
  public void login(PasswordEncoder passwordEncoder, String credentials) {
    
//    String tmp = passwordEncoder.encode(credentials);
//    if (!passwordEncoder.matches(credentials, password))
//        throw new IllegalArgumentException("Bad credential");
  }
}


