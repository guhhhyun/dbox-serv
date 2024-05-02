package com.dongkuksystems.dbox.models.dto.type.manager.history;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HistoryDeleteFilterDto {
	
	// HistoryDeleteFilterDto(company=UNC, dept=UNC50014030, user=dooyeon.yoo, searchSelect=전체, searchText=테스트권한특성2222, startDate=2021-10-01, endDate=2021-10-31, uUserId=null, uJobDate=null, uDocKey=null, uCabinetCode=null, colField=null)
	
	// 상세이력 호출 menuid
	private String menuid;
	
	// 공통
	private String company;
	private String dept;
	private String deptName;
	
	private String user;
	private String searchSelect;
	private String searchText;
	private String startDate;
	private String endDate;
	
	// 외부사이트 파일 반출 이력 ( uCabinetCode 도 사용함 )
	private String uUserId;
	private String uJobDate;
	private String uAttachSystem;
	
	
	// 조직별이력 - LifeCycle
	private String uDeptCode;	// 선택한 부서 ID
	
	// 문서별이력- 문서유통 ( 공통부분 추가 사용함 )
	private String state;	// 문서별이력 - 문서유통   분류특성.
	
	
	// 문서별이력- 문서유통 상세이력 ( user, startDate, endDate 같이 사용함 )
	private String uDocKey;
	private String uDocName;
	private String uCabinetCode;
	private String colField;
	
	// 외부저장매체 
	private String rObjectId;
	
	// 특이사용자이력 ( 선택된 지정일 : user_lock r_object_id ) 
	private String oddRObjectId;
	
	// 운영현황
	private String grade;			// 직급 (임원,부장,차장,과장,대리,사원,기타)
	private String gradeSearch;
	private String gradeSearchEtc;
	
	private String columnCnt;		// 필요한 컬럼 갯수
	private String calendarGubun;	// D 일, W 주, M 월, B 분기, Y 년
	private String cal01;			// 첫번째 열 정보
	private String cal02;			// 열정보
	private String cal03;			// 열정보
	private String cal04;			// 열정보
	private String cal05;			// 열정보
	private String cal06;			// 열정보
	private String cal07;			// 열정보
	private String cal08;			// 열정보
	private String cal09;			// 열정보
	private String cal10;			// 열정보
	private String cal11;			// 열정보
	private String cal12;			// 12번째 열 정보
	
}
