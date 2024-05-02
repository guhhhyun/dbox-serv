package com.dongkuksystems.dbox.models.type.history;

import java.time.LocalDateTime;

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
public class HistoryDocLifeCycle {
	
	@ApiModelProperty(value = "문서 KEY")
	private String uDocKey;
	@ApiModelProperty(value = "문서명")
	private String uDocName;
	@ApiModelProperty(value = "문서함CODE")
	private String uCabinetCode;
	
	// 조직별 LifeCycle 정보
	@ApiModelProperty(value = "사용자")
	private String uJobUser;
	@ApiModelProperty(value = "사용자명")
	private String jobUserName;
	
	
	// 특이사용자 이력 ( DL 다운, DT 반출, PR 권한신청, PT 출력, LD 삭제)
	@ApiModelProperty(value = "특이사용지정일 r_object_id")
	private String rObjectId;
	@ApiModelProperty(value = "번호")
	private String rownum;
	@ApiModelProperty(value = "사용자 ID")
	private String uUserId;	
	@ApiModelProperty(value = "사용자명")
	private String displayName;
	@ApiModelProperty(value = "부서명")
	private String deptName;
	@ApiModelProperty(value = "상태")
	private String uLockStatus;
	@ApiModelProperty(value = "상태명")
	private String lockStatusName;
	
	@JsonFormat(pattern = "yyyy-MM-dd")
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@ApiModelProperty(value = "지정일")
	private LocalDateTime logDate;
	
	@JsonFormat(pattern = "yyyy-MM-dd")
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@ApiModelProperty(value = "로그조회 시작일")
	private LocalDateTime logStartDate;
	
	@JsonFormat(pattern = "yyyy-MM-dd")
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@ApiModelProperty(value = "로그조회 종료 시간")
	private LocalDateTime logEndDate;
		
	@ApiModelProperty(value = "반출")
	private String cntDt;
	@ApiModelProperty(value = "권한신청")
	private String cntPr;
	@ApiModelProperty(value = "인쇄")
	private String cntPt;
	
	
	
	@ApiModelProperty(value = "등록")
	private String cntRe;
	@ApiModelProperty(value = "복사")
	private String cntDp;
	@ApiModelProperty(value = "조회=[D:Dbox, L:링크]")
	private String cntVed;
	@ApiModelProperty(value = "문서함조회=[D:Dbox, M:링크]")
	private String cntVeM;
	@ApiModelProperty(value = "수정=[W:덮어쓰기, V:버전생성]")
	private String cntEdW;
	@ApiModelProperty(value = "수정=[W:덮어쓰기, V:버전생성]")
	private String cntEdV;
	@ApiModelProperty(value = "결재")
	private String cntAp;
	@ApiModelProperty(value = "다운로드")
	private String cntDl;
	@ApiModelProperty(value = "이동")
	private String cntDm;
	@ApiModelProperty(value = "이관")
	private String cntTr;
	@ApiModelProperty(value = "잠금 문서 강제 해제")
	private String cntCc;
	@ApiModelProperty(value = "Closed 처리")
	private String cntCj;
	@ApiModelProperty(value = "Closed 자료변경 (C→L)")
	private String cntCl;
	@ApiModelProperty(value = "보존연한 변경")
	private String cntRc;
	@ApiModelProperty(value = "Live 삭제")
	private String cntLd;
	@ApiModelProperty(value = "휴지통 자동 삭제")
	private String cntLa;
	@ApiModelProperty(value = "휴지통 수동 삭제")
	private String cntLp;
	@ApiModelProperty(value = "폐기 요청")
	private String cntDr;
	@ApiModelProperty(value = "폐기 승인")
	private String cntDa;
	
}
