package com.dongkuksystems.dbox.services.manager.history;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.dongkuksystems.dbox.daos.type.manager.history.HistoryDao;
import com.dongkuksystems.dbox.models.dto.type.manager.history.HistoryDeleteFilterDto;
import com.dongkuksystems.dbox.models.type.history.HistoryAttach;
import com.dongkuksystems.dbox.models.type.history.HistoryDocDistribution;
import com.dongkuksystems.dbox.models.type.history.HistoryDocLifeCycle;
import com.dongkuksystems.dbox.models.type.history.HistoryUsb;
import com.dongkuksystems.dbox.models.type.request.ReqDelete;
import com.dongkuksystems.dbox.services.AbstractCommonService;

@Service
public class HistoryServiceImpl extends AbstractCommonService implements HistoryService {

	private final HistoryDao historyDao;

	public HistoryServiceImpl(HistoryDao historyDao/* , PrintDao printDao */) {
		this.historyDao = historyDao;
	}

	@Override
	public List<Map<String, Object>> selectHistoryTotal(HistoryDeleteFilterDto historyDeleteFilterDto) {
		
		String sColumnCnt 		= historyDeleteFilterDto.getColumnCnt(); 		// 필요한 컬럼 갯수
		String sCalendarGubun 	= historyDeleteFilterDto.getCalendarGubun(); 	// D 일, W 주, M 월, B 분기, Y 년
		String sStartDate 		= historyDeleteFilterDto.getStartDate();
		
		List<Map<String, Object>> liTotalData = new ArrayList<Map<String,Object>>();	// 최종 리턴 정보.
		
		try {
			
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); 
			Calendar cal = Calendar.getInstance();
			cal.setTime(formatter.parse(sStartDate));
			
			int AddForamt = 0;
			int AddNum	  = 0;
			if(sCalendarGubun.equals("D"))
			{
				AddForamt 	= Calendar.DATE;
				AddNum 	= 1;
			}
			else if(sCalendarGubun.equals("W"))
			{
				AddForamt 	= Calendar.DATE;
				AddNum 	= 7;
			}
			else if(sCalendarGubun.equals("M"))
			{
				AddForamt 	= Calendar.MONTH;
				AddNum 	= 1;
			}
			else if(sCalendarGubun.equals("B"))
			{
				AddForamt 	= Calendar.MONTH;
				AddNum 	= 3;
			}
			else if(sCalendarGubun.equals("Y"))
			{
				AddForamt 	= Calendar.YEAR;
				AddNum 	= 1;
			}
			
			//======================================
			// 12가지 Date 정보 조회.
			//		DB 조회상 <  로 구별 하기 때문에 일단위일경우  다음날 보다 작음으로 검색함 
			//======================================
			String[] asDateList = {"","","","","","","","","","","",""}; 
			for(int i = 0 ; i < 12; i++)
			{
				cal.add(AddForamt, AddNum);
				asDateList[i] = formatter.format(cal.getTime());
			}

			// CAL01 ~ CAL02 로 해서 12가지 로 구분함
			historyDeleteFilterDto.setCal01(asDateList[0]);
			historyDeleteFilterDto.setCal02(asDateList[1]);
			historyDeleteFilterDto.setCal03(asDateList[2]);
			historyDeleteFilterDto.setCal04(asDateList[3]);
			historyDeleteFilterDto.setCal05(asDateList[4]);
			historyDeleteFilterDto.setCal06(asDateList[5]);
			historyDeleteFilterDto.setCal07(asDateList[6]);
			historyDeleteFilterDto.setCal08(asDateList[7]);
			historyDeleteFilterDto.setCal09(asDateList[8]);
			historyDeleteFilterDto.setCal10(asDateList[9]);
			historyDeleteFilterDto.setCal11(asDateList[10]);
			historyDeleteFilterDto.setCal12(asDateList[11]);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//==================================================
		// 기본메뉴 구성 ( 기본 메뉴 가 있어서 배열로 처리함 )
		// 		
		//	- 행 : 가로 3개 + 평균 1개 + 유동적인 12개  : 16개
		// 	- 열 : 세로 12 + 비율 2개 				 : 14개
		//==================================================
		String sAppend = "월";
		if(sCalendarGubun.equals("D"))
		{
			sAppend 	= "일";
		}
		else if(sCalendarGubun.equals("W"))
		{
			sAppend 	= "주";
		}
		else if(sCalendarGubun.equals("M"))
		{
			sAppend 	= "월";
		}
		else if(sCalendarGubun.equals("B"))
		{
			sAppend 	= "분기";
		}
		else if(sCalendarGubun.equals("Y"))
		{
			sAppend 	= "년";
		}
		
		String[][] arrayMenu = {
									  {"사용자수"			, "명/" + sAppend	, "", "", "", "", "", "", "", "", "", "", "", "", ""}
									, {"다운로드 성능"		, "s/MB/" + sAppend	, "", "", "", "", "", "", "", "", "", "", "", "", ""}
								};
		
		
		//==========================
		// 직급 추가. ( 전체 일경우 IN 조건 처리 안함 )
		//==========================
		List<String> liGradeList = new ArrayList<String>();
		historyDeleteFilterDto.setGradeSearch("N");
		historyDeleteFilterDto.setGradeSearchEtc("N");
		if(historyDeleteFilterDto.getGrade().equals("임원,부장,차장,과장,대리,사원,기타"))
		{
			historyDeleteFilterDto.setGradeSearch("N");
			historyDeleteFilterDto.setGradeSearchEtc("N");
		}
		else
		{
			
			if(historyDeleteFilterDto.getGrade().contains("임원"))
			{
				historyDeleteFilterDto.setGradeSearch("Y");
				liGradeList.add("임원");
			}
			if(historyDeleteFilterDto.getGrade().contains("부장"))
			{
				historyDeleteFilterDto.setGradeSearch("Y");
				liGradeList.add("부장");
			}
			if(historyDeleteFilterDto.getGrade().contains("차장"))
			{
				historyDeleteFilterDto.setGradeSearch("Y");
				liGradeList.add("차장");
			}
			if(historyDeleteFilterDto.getGrade().contains("과장"))
			{
				historyDeleteFilterDto.setGradeSearch("Y");
				liGradeList.add("과장");
			}
			if(historyDeleteFilterDto.getGrade().contains("대리"))
			{
				historyDeleteFilterDto.setGradeSearch("Y");
				liGradeList.add("대리");
			}
			if(historyDeleteFilterDto.getGrade().contains("사원"))
			{
				historyDeleteFilterDto.setGradeSearch("Y");
				liGradeList.add("사원");
			}
			
			if(historyDeleteFilterDto.getGrade().contains("기타"))
			{
				historyDeleteFilterDto.setGradeSearchEtc("Y");
				// liGradeList.add("기타");
			}
		}
		
		
		//======================
		// Data 추가
		//======================
		List<Map<String, String>> liTotalItemData = historyDao.selectHistoryTotal(historyDeleteFilterDto, liGradeList);		
		for(Map<String, String> sub :  liTotalItemData)
		{
			String 	sDataGubun 	= sub.get("dategubun");
			int 	iDataGubun	= Integer.parseInt(sDataGubun);
			int 	DataCol		= 1 + iDataGubun;
			
			arrayMenu[0][DataCol] 	= String.valueOf(sub.get("use_dbox"));
			arrayMenu[1][DataCol] 	= String.valueOf(sub.get("down_time"));
			
		}

		//====================================================
		// 증가량 확인 ( 마지막 열 - 첫번재 열 )
		//  사용자수 int
		//  평균 double
		//====================================================
		int iRowSum = 0;
		for(int j = 0 ; j < Integer.parseInt(sColumnCnt); j++)
		{
			iRowSum  += Integer.parseInt( StringUtils.isBlank(arrayMenu[0][j + 2]) ? "0" : arrayMenu[0][j + 2] );
		}
		arrayMenu[0][14] = String.valueOf( iRowSum / Integer.parseInt(sColumnCnt) );  
		
		
		double dRowSum = 0;
		for(int j = 0 ; j < Integer.parseInt(sColumnCnt); j++)
		{
			dRowSum  += Double.parseDouble( StringUtils.isBlank(arrayMenu[1][j + 2]) ? "0" : arrayMenu[1][j + 2] );
		}
		
		arrayMenu[1][14] = String.format("%.2f", dRowSum / Integer.parseInt(sColumnCnt));

		//======================
		//	최종 리턴 형식 값 생성
		//======================
		for(int i = 0 ; i < 2; i++)
		{
			
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("col0"	, arrayMenu[i][0]);
			map.put("col1"	, arrayMenu[i][1]);
			
			map.put("col2"	, arrayMenu[i][2]);
			map.put("col3"	, arrayMenu[i][3]);
			map.put("col4"	, arrayMenu[i][4]);
			map.put("col5"	, arrayMenu[i][5]);
			map.put("col6"	, arrayMenu[i][6]);
			map.put("col7"	, arrayMenu[i][7]);
			map.put("col8"	, arrayMenu[i][8]);
			map.put("col9"	, arrayMenu[i][9]);
			map.put("col10"	, arrayMenu[i][10]);
			map.put("col10"	, arrayMenu[i][11]);
			map.put("col12"	, arrayMenu[i][12]);
			map.put("col13"	, arrayMenu[i][13]);
			
			map.put("col14"	, arrayMenu[i][14]);
			
			liTotalData.add(map);
			
		}
		
		return liTotalData;
	}
	
	@Override
	public List<Map<String, Object>> selectHistoryTotalItem(HistoryDeleteFilterDto historyDeleteFilterDto) {
		
		String sColumnCnt 		= historyDeleteFilterDto.getColumnCnt(); 		// 필요한 컬럼 갯수
		String sCalendarGubun 	= historyDeleteFilterDto.getCalendarGubun(); 	// D 일, W 주, M 월, B 분기, Y 년
		String sStartDate 		= historyDeleteFilterDto.getStartDate();
		
		List<Map<String, Object>> liTotalData = new ArrayList<Map<String,Object>>();	// 최종 리턴 정보.
		
		try {
			
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); 
			Calendar cal = Calendar.getInstance();
			cal.setTime(formatter.parse(sStartDate));
			
			int AddForamt = 0;
			int AddNum	  = 0;
			if(sCalendarGubun.equals("D"))
			{
				AddForamt 	= Calendar.DATE;
				AddNum 	= 1;
			}
			else if(sCalendarGubun.equals("W"))
			{
				AddForamt 	= Calendar.DATE;
				AddNum 	= 7;
			}
			else if(sCalendarGubun.equals("M"))
			{
				AddForamt 	= Calendar.MONTH;
				AddNum 	= 1;
			}
			else if(sCalendarGubun.equals("B"))
			{
				AddForamt 	= Calendar.MONTH;
				AddNum 	= 3;
			}
			else if(sCalendarGubun.equals("Y"))
			{
				AddForamt 	= Calendar.YEAR;
				AddNum 	= 1;
			}
			
			//======================================
			// 12가지 Date 정보 조회.
			//		DB 조회상 <  로 구별 하기 때문에 일단위일경우  다음날 보다 작음으로 검색함 
			//======================================
			String[] asDateList = {"","","","","","","","","","","",""}; 
			for(int i = 0 ; i < 12; i++)
			{
				cal.add(AddForamt, AddNum);
				asDateList[i] = formatter.format(cal.getTime());
			}

			// CAL01 ~ CAL02 로 해서 12가지 로 구분함
			historyDeleteFilterDto.setCal01(asDateList[0]);
			historyDeleteFilterDto.setCal02(asDateList[1]);
			historyDeleteFilterDto.setCal03(asDateList[2]);
			historyDeleteFilterDto.setCal04(asDateList[3]);
			historyDeleteFilterDto.setCal05(asDateList[4]);
			historyDeleteFilterDto.setCal06(asDateList[5]);
			historyDeleteFilterDto.setCal07(asDateList[6]);
			historyDeleteFilterDto.setCal08(asDateList[7]);
			historyDeleteFilterDto.setCal09(asDateList[8]);
			historyDeleteFilterDto.setCal10(asDateList[9]);
			historyDeleteFilterDto.setCal11(asDateList[10]);
			historyDeleteFilterDto.setCal12(asDateList[11]);
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 시작일 , 갯수, 기간 (일, 월 주 등 )
		// 일 
		// 주  7일씩
		// 월  한달씩
		// 분기 3개월씩
		// 년  1년씩.
		
		// 시작일 조회
		// 일 : 1루씩 +
		// 주 : 7일씩 +
		// 월 : 
		
		
		//==================================================
		// 기본메뉴 구성 ( 기본 메뉴 가 있어서 배열로 처리함 )
		// 		
		//	- 행 : 가로 3개 + 평균 1개 + 유동적인 12개  : 16개
		// 	- 열 : 세로 12 + 비율 2개 				 : 14개
		//==================================================
		String sAppend = "월";
		if(sCalendarGubun.equals("D"))
		{
			sAppend 	= "일";
		}
		else if(sCalendarGubun.equals("W"))
		{
			sAppend 	= "주";
		}
		else if(sCalendarGubun.equals("M"))
		{
			sAppend 	= "월";
		}
		else if(sCalendarGubun.equals("B"))
		{
			sAppend 	= "분기";
		}
		else if(sCalendarGubun.equals("Y"))
		{
			sAppend 	= "년";
		}
		
		// ① ② ③ ④ ⑤ ⑥
		String[][] arrayMenu = {
									  {"D'Box 작업률"			, "① D’Box 내작성문서"	, "문서/" + sAppend	, "", "", "", "", "", "", "", "", "", "", "", "", ""}
									, {""					, "② PC 내 작성문서"	, "문서/" + sAppend	, "", "", "", "", "", "", "", "", "", "", "", "", ""}
									, {""					, "① / ( ① + ② )"	, "비율(%)"			, "", "", "", "", "", "", "", "", "", "", "", "", ""}
									, {"D'Box 활용률"			, "① 등록"			, "문서/" + sAppend	, "", "", "", "", "", "", "", "", "", "", "", "", ""}
									, {""					, "② 수정"			, "문서/" + sAppend	, "", "", "", "", "", "", "", "", "", "", "", "", ""}
									, {""					, "③ 조회"			, "횟수/" + sAppend	, "", "", "", "", "", "", "", "", "", "", "", "", ""}
									, {""					, "④ 다운로드"			, "횟수/" + sAppend	, "", "", "", "", "", "", "", "", "", "", "", "", ""}
									, {""					, "(② + ③ + ④) / ①"	, "비율(%)"			, "", "", "", "", "", "", "", "", "", "", "", "", ""}
									, {"보안 모니터링"			, "자가승인"			, "문서/" + sAppend	, "", "", "", "", "", "", "", "", "", "", "", "", ""}
									, {""					, "복호화(반출)"		, "문서/" + sAppend	, "", "", "", "", "", "", "", "", "", "", "", "", ""}
									, {""					, "인쇄"				, "문서/" + sAppend	, "", "", "", "", "", "", "", "", "", "", "", "", ""}
									, {""					, "다운로드"			, "문서/" + sAppend	, "", "", "", "", "", "", "", "", "", "", "", "", ""}
									, {""					, "삭제"				, "문서/" + sAppend	, "", "", "", "", "", "", "", "", "", "", "", "", ""}
									, {"평균 문서작성 소요시간"	, ""				, "일/"  + sAppend	, "", "", "", "", "", "", "", "", "", "", "", "", ""}
								};
		
		
		//==========================
		// 직급 추가. ( 전체 일경우 IN 조건 처리 안함 )
		//==========================
		List<String> liGradeList = new ArrayList<String>();
		historyDeleteFilterDto.setGradeSearch("N");
		historyDeleteFilterDto.setGradeSearchEtc("N");
		if(historyDeleteFilterDto.getGrade().equals("임원,부장,차장,과장,대리,사원,기타"))
		{
			historyDeleteFilterDto.setGradeSearch("N");
			historyDeleteFilterDto.setGradeSearchEtc("N");
		}
		else
		{
			
			if(historyDeleteFilterDto.getGrade().contains("임원"))
			{
				historyDeleteFilterDto.setGradeSearch("Y");
				liGradeList.add("임원");
			}
			if(historyDeleteFilterDto.getGrade().contains("부장"))
			{
				historyDeleteFilterDto.setGradeSearch("Y");
				liGradeList.add("부장");
			}
			if(historyDeleteFilterDto.getGrade().contains("차장"))
			{
				historyDeleteFilterDto.setGradeSearch("Y");
				liGradeList.add("차장");
			}
			if(historyDeleteFilterDto.getGrade().contains("과장"))
			{
				historyDeleteFilterDto.setGradeSearch("Y");
				liGradeList.add("과장");
			}
			if(historyDeleteFilterDto.getGrade().contains("대리"))
			{
				historyDeleteFilterDto.setGradeSearch("Y");
				liGradeList.add("대리");
			}
			if(historyDeleteFilterDto.getGrade().contains("사원"))
			{
				historyDeleteFilterDto.setGradeSearch("Y");
				liGradeList.add("사원");
			}
			
			if(historyDeleteFilterDto.getGrade().contains("기타"))
			{
				historyDeleteFilterDto.setGradeSearchEtc("Y");
				// liGradeList.add("기타");
			}
		}
		
		
		//======================
		// Data 추가
		//======================
		List<Map<String, String>> liTotalItemData = historyDao.selectHistoryTotalItem(historyDeleteFilterDto, liGradeList);		
		for(Map<String, String> sub :  liTotalItemData)
		{
			String 	sDataGubun 	= sub.get("dategubun");
			int 	iDataGubun	= Integer.parseInt(sDataGubun);
			int 	DataCol		= 2 + iDataGubun;
			
			arrayMenu[0][DataCol] 	= String.valueOf(sub.get("cnt_dx_reg"));
			arrayMenu[1][DataCol] 	= String.valueOf(sub.get("cnt_pc_reg"));
			
			// 1. D'Box 작업률 열 통계 :: 1 / (1+2)   ==> Unexpected exception occurred: / by zero
			int idx = Integer.parseInt( StringUtils.isBlank(arrayMenu[0][DataCol]) ? "0" : arrayMenu[0][DataCol] );
			int ipc = Integer.parseInt( StringUtils.isBlank(arrayMenu[1][DataCol]) ? "0" : arrayMenu[1][DataCol] );
			
			if( (idx + ipc) == 0 )
			{
				arrayMenu[2][DataCol] 	= String.valueOf(0)  + "%"; ; 
			}
			else
			{
				// arrayMenu[2][DataCol] 	= String.valueOf( ( idx * 100.0) / (idx + ipc) )  + "%";
				arrayMenu[2][DataCol] 	= String.format("%.1f", ( idx * 100.0) / (idx + ipc) )  + "%";
				
			}

			arrayMenu[3][DataCol] 	= String.valueOf(idx + ipc);   			// CNT_DX_REG + CNT_PC_REG
			arrayMenu[4][DataCol] 	= String.valueOf(sub.get("cnt_edit"));
			arrayMenu[5][DataCol] 	= String.valueOf(sub.get("cnt_view"));
			arrayMenu[6][DataCol] 	= String.valueOf(sub.get("cnt_down"));
			
			// 1. D'Box 활용률 열 통계 :: (2+3+4)/ 1
			int iedit = Integer.parseInt( StringUtils.isBlank(arrayMenu[4][DataCol]) ? "0" : arrayMenu[4][DataCol] );
			int iview = Integer.parseInt( StringUtils.isBlank(arrayMenu[5][DataCol]) ? "0" : arrayMenu[5][DataCol] );
			int idown = Integer.parseInt( StringUtils.isBlank(arrayMenu[6][DataCol]) ? "0" : arrayMenu[6][DataCol] );
			
			if( (idx + ipc) == 0 )
			{
				arrayMenu[7][DataCol] 	= String.valueOf(0)  + "%"; ; 
			}
			else
			{
				// arrayMenu[7][DataCol] 	= String.valueOf( ( ( iedit + iview + idown ) * 100.0)  / (idx + ipc)  )  + "%";
				arrayMenu[7][DataCol] 	= String.format("%.1f", ( ( iedit + iview + idown ) * 100.0)  / (idx + ipc)  )  + "%";
			}
			
			arrayMenu[8][DataCol] 	= String.valueOf(sub.get("cnt_selfout"));
			arrayMenu[9][DataCol] 	= String.valueOf(sub.get("cnt_takeout"));
			arrayMenu[10][DataCol] 	= String.valueOf(sub.get("cnt_print"));
			arrayMenu[11][DataCol] 	= String.valueOf(sub.get("cnt_down"));		// 이부분 2번 하는건가??
			arrayMenu[12][DataCol] 	= String.valueOf(sub.get("cnt_delete"));
			
			arrayMenu[13][DataCol] 	= String.valueOf(sub.get("avg_edit_time"));
			
			// arrayMenu[13][DataCol] 	= String.format("%.2f", sub.get("avg_edit_time"));
			// arrayMenu[13][DataCol] 	= String.format("%.2f", 0.0);
			// arrayMenu[13][DataCol] 	= String.format("%.2f", 1);
			// arrayMenu[13][DataCol] 	= String.format("%.2f", 0.1);
			
			//  String.format("%.2f", dRowSum / Integer.parseInt(sColumnCnt));
		}
		
		//====================================================
		// 증가량 확인 ( 마지막 열 - 첫번재 열 )
		//====================================================
		for(int i = 0 ; i < 14; i++)
		{

			int iRowSum = 0;
			
			if(i == 2)
			{
				// 이전 정보 통계 필요 할경우
				int iRow0 = Integer.parseInt( StringUtils.isBlank(arrayMenu[0][15]) ? "0" : arrayMenu[0][15] );
				int iRow1 = Integer.parseInt( StringUtils.isBlank(arrayMenu[1][15]) ? "0" : arrayMenu[1][15] );
				
				if( (iRow0 + iRow1) == 0 )
				{
					arrayMenu[i][15] = String.valueOf(0)  + "%"; ; 
				}
				else
				{
					// arrayMenu[i][15] = String.valueOf(iRow0 / (iRow0 + iRow1) )  + "%";
					arrayMenu[i][15] = String.format("%.1f", ( iRow0 * 100.0 ) / (iRow0 + iRow1) )  + "%";
					
				}
				
			}
			else if(i == 7)
			{
				// 이전 정보 통계 필요 할경우
				int iRow3 = Integer.parseInt( StringUtils.isBlank(arrayMenu[3][15]) ? "0" : arrayMenu[3][15] );
				int iRow4 = Integer.parseInt( StringUtils.isBlank(arrayMenu[4][15]) ? "0" : arrayMenu[4][15] );
				int iRow5 = Integer.parseInt( StringUtils.isBlank(arrayMenu[5][15]) ? "0" : arrayMenu[5][15] );
				int iRow6 = Integer.parseInt( StringUtils.isBlank(arrayMenu[6][15]) ? "0" : arrayMenu[6][15] );
				
				if( iRow3 == 0 )
				{
					arrayMenu[i][15] = String.valueOf(0) + "%"; ; 
				}
				else
				{
					// arrayMenu[i][15] = String.valueOf( ( iRow4 + iRow5 + iRow6 ) / iRow3 ) + "%"; 
					arrayMenu[i][15] = String.format("%.1f", (( iRow4 + iRow5 + iRow6 ) * 100.0 ) / iRow3 ) + "%";
					
				}
			}
			else if(i == 13)
			{
				// 평균 문서작성 소유시간  ( 작성 시간 있는 정보 에서 평균을 구한 값 )
				double dRowSum = 0;
				for(int j = 0 ; j < Integer.parseInt(sColumnCnt); j++)
				{
					dRowSum  += Double.parseDouble( StringUtils.isBlank(arrayMenu[i][j + 3]) ? "0" : arrayMenu[i][j + 3] );
				}
				
				arrayMenu[i][15] = String.format("%.1f", dRowSum / Integer.parseInt(sColumnCnt));
				
			}
			else
			{
				// 월별 평균 필요
				
				for(int j = 0 ; j < Integer.parseInt(sColumnCnt); j++)
				{
					iRowSum  += Integer.parseInt( StringUtils.isBlank(arrayMenu[i][j + 3]) ? "0" : arrayMenu[i][j + 3] );
				}
				
				arrayMenu[i][15] = String.valueOf( iRowSum / Integer.parseInt(sColumnCnt) );  
				
			}
			
		}
		
		//======================
		//	최종 리턴 형식 값 생성
		//======================
		for(int i = 0 ; i < 14; i++)
		{
			
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("col0"	, arrayMenu[i][0]);
			map.put("col1"	, arrayMenu[i][1]);
			map.put("col2"	, arrayMenu[i][2]);
			
			map.put("col3"	, arrayMenu[i][3]);
			map.put("col4"	, arrayMenu[i][4]);
			map.put("col5"	, arrayMenu[i][5]);
			map.put("col6"	, arrayMenu[i][6]);
			map.put("col7"	, arrayMenu[i][7]);
			map.put("col8"	, arrayMenu[i][8]);
			map.put("col9"	, arrayMenu[i][9]);
			map.put("col10"	, arrayMenu[i][10]);
			map.put("col10"	, arrayMenu[i][11]);
			map.put("col12"	, arrayMenu[i][12]);
			map.put("col13"	, arrayMenu[i][13]);
			map.put("col14"	, arrayMenu[i][14]);
			
			map.put("col15"	, arrayMenu[i][15]);
			liTotalData.add(map);
			
		}
		
		return liTotalData;
		
	}
	
	@Override
	public List<Map<String, Object>> selectHistoryTotalAsset(HistoryDeleteFilterDto historyDeleteFilterDto) {
		
		String sColumnCnt 		= historyDeleteFilterDto.getColumnCnt(); 		// 필요한 컬럼 갯수
		String sCalendarGubun 	= historyDeleteFilterDto.getCalendarGubun(); 	// D 일, W 주, M 월, B 분기, Y 년
		String sStartDate 		= historyDeleteFilterDto.getStartDate();
		
		List<Map<String, Object>> liTotalData = new ArrayList<Map<String,Object>>();	// 최종 리턴 정보.
		
		try {
			
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd"); 
			Calendar cal = Calendar.getInstance();
			cal.setTime(formatter.parse(sStartDate));
			
			int AddForamt = 0;
			int AddNum	  = 0;
			if(sCalendarGubun.equals("D"))
			{
				AddForamt 	= Calendar.DATE;
				AddNum 	= 1;
			}
			else if(sCalendarGubun.equals("W"))
			{
				AddForamt 	= Calendar.DATE;
				AddNum 	= 7;
			}
			else if(sCalendarGubun.equals("M"))
			{
				AddForamt 	= Calendar.MONTH;
				AddNum 	= 1;
			}
			else if(sCalendarGubun.equals("B"))
			{
				sCalendarGubun	= "Q";
				AddForamt 	= Calendar.MONTH;
				AddNum 	= 3;
			}
			else if(sCalendarGubun.equals("Y"))
			{
				AddForamt 	= Calendar.YEAR;
				AddNum 	= 1;
			}
			
			//======================================
			// 12가지 Date 정보 조회.
			//		DB 조회상 <  로 구별 하기 때문에 일단위일경우  다음날 보다 작음으로 검색함 
			//======================================
			String[] asDateList = {"","","","","","","","","","","",""}; 
			for(int i = 0 ; i < 12; i++)
			{
				cal.add(AddForamt, AddNum);
				asDateList[i] = formatter.format(cal.getTime());
			}

			
			// sCalendarGubun
			historyDeleteFilterDto.setCalendarGubun(sCalendarGubun);
			
			// CAL01 ~ CAL02 로 해서 12가지 로 구분함
			historyDeleteFilterDto.setCal01(asDateList[0]);
			historyDeleteFilterDto.setCal02(asDateList[1]);
			historyDeleteFilterDto.setCal03(asDateList[2]);
			historyDeleteFilterDto.setCal04(asDateList[3]);
			historyDeleteFilterDto.setCal05(asDateList[4]);
			historyDeleteFilterDto.setCal06(asDateList[5]);
			historyDeleteFilterDto.setCal07(asDateList[6]);
			historyDeleteFilterDto.setCal08(asDateList[7]);
			historyDeleteFilterDto.setCal09(asDateList[8]);
			historyDeleteFilterDto.setCal10(asDateList[9]);
			historyDeleteFilterDto.setCal11(asDateList[10]);
			historyDeleteFilterDto.setCal12(asDateList[11]);
			
		} catch (Exception e) {
			e.printStackTrace();
		}

		
		//==================================================
		// 기본메뉴 구성 ( 기본 메뉴 가 있어서 배열로 처리함 )
		// 		
		//	- 행 : 가로 3개 + 평균 1개 + 유동적인 12개  : 16개
		// 	- 열 : 세로 12 + 비율 2개 				 : 14개
		//==================================================
		// ① ② ③ ④ ⑤ ⑥
		String[][] arrayMenu = {
									  {"D'Box 내 생성 문서"		, "① Live 문서"				, "문서(누계)", "", "", "", "", "", "", "", "", "", "", "", "", ""}
									, {""					, "② Closed 문서(시스템)"		, "문서(누계)", "", "", "", "", "", "", "", "", "", "", "", "", ""}
									, {""					, "③ Closed 문서(사용자)"		, "문서(누계)", "", "", "", "", "", "", "", "", "", "", "", "", ""}
									, {""					, "합계 (① + ② + ③)"			, "문서(누계)", "", "", "", "", "", "", "", "", "", "", "", "", ""}
									, {"PC 등록 문서"			, "④ Live 문서"				, "문서(누계)", "", "", "", "", "", "", "", "", "", "", "", "", ""}
									, {""					, "⑤ Closed 문서(시스템)"		, "문서(누계)", "", "", "", "", "", "", "", "", "", "", "", "", ""}
									, {""					, "⑥ Closed 문서(사용자)"		, "문서(누계)", "", "", "", "", "", "", "", "", "", "", "", "", ""}
									, {""					, "합계 (④ + ⑤ + ⑥)"			, "문서(누계)", "", "", "", "", "", "", "", "", "", "", "", "", ""}
									, {"총계"					, "(① + ② + ③ + ④ + ⑤ + ⑥)"	, "문서(누계)", "", "", "", "", "", "", "", "", "", "", "", "", ""}
								};
		
		//======================
		// Data 추가
		//======================
		List<Map<String, String>> liTotalItemData = historyDao.selectHistoryTotalAsset(historyDeleteFilterDto);		
		for(Map<String, String> sub :  liTotalItemData)
		{
			String 	sDataGubun 	= sub.get("dategubun");
			int 	iDataGubun	= Integer.parseInt(sDataGubun);
			int 	DataCol		= 2 + iDataGubun;
			
			
			arrayMenu[0][DataCol] 	= String.valueOf(sub.get("cnt_dx_live"));
			arrayMenu[1][DataCol] 	= String.valueOf(sub.get("cnt_dx_close_s"));
			arrayMenu[2][DataCol] 	= String.valueOf(sub.get("cnt_dx_close_u"));
			
			// 1. D'Box 내 생성 문서 	:: LIVE + CLOSE(시스템) + CLOSE ( 사용자 )
			int idxl 	= Integer.parseInt(arrayMenu[0][DataCol]);
			int idxcs 	= Integer.parseInt(arrayMenu[1][DataCol]);
			int idxcu 	= Integer.parseInt(arrayMenu[1][DataCol]);
			arrayMenu[3][DataCol] 	= String.valueOf( idxl + idxcs + idxcu );
			
			arrayMenu[4][DataCol] 	= String.valueOf(sub.get("cnt_pc_live"));
			arrayMenu[5][DataCol] 	= String.valueOf(sub.get("cnt_pc_close_s"));
			arrayMenu[6][DataCol] 	= String.valueOf(sub.get("cnt_pc_close_u"));
			
			// 2. PC 등록문서 			:: LIVE + CLOSE(시스템) + CLOSE ( 사용자 )
			int ipcl 	= Integer.parseInt(arrayMenu[4][DataCol]);
			int ipccs 	= Integer.parseInt(arrayMenu[5][DataCol]);
			int ipccu 	= Integer.parseInt(arrayMenu[6][DataCol]);
			arrayMenu[7][DataCol] 	= String.valueOf( ipcl + ipccs + ipccu );
			
			arrayMenu[8][DataCol] 	= String.valueOf( idxl + idxcs + idxcu + ipcl + ipccs + ipccu );

			
		}
		
		//====================================================
		// 증가량 확인 ( 마지막 열 - 첫번재 열 )
		//====================================================
		for(int i = 0 ; i < 9; i++)
		{

			int iStartColumnValue	= Integer.parseInt( StringUtils.isBlank(arrayMenu[i][3]) ? "0" : arrayMenu[i][3] );
			int iEndColumnValue		= Integer.parseInt( StringUtils.isBlank(arrayMenu[i][2 + Integer.parseInt(sColumnCnt)]) ? "0" : arrayMenu[i][2 + Integer.parseInt(sColumnCnt)]  );
			
			if(Integer.parseInt(sColumnCnt) == 1)
			{
				arrayMenu[i][15] = String.valueOf(iStartColumnValue);
			}
			else
			{
				arrayMenu[i][15] = String.valueOf(iEndColumnValue - iStartColumnValue);
			}
			
		}
		
		
		//======================
		//	최종 리턴 형식 값 생성
		//======================
		for(int i = 0 ; i < 9; i++)
		{
			
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("col0"	, arrayMenu[i][0]);
			map.put("col1"	, arrayMenu[i][1]);
			map.put("col2"	, arrayMenu[i][2]);
			
			map.put("col3"	, arrayMenu[i][3]);
			map.put("col4"	, arrayMenu[i][4]);
			map.put("col5"	, arrayMenu[i][5]);
			map.put("col6"	, arrayMenu[i][6]);
			map.put("col7"	, arrayMenu[i][7]);
			map.put("col8"	, arrayMenu[i][8]);
			map.put("col9"	, arrayMenu[i][9]);
			map.put("col10"	, arrayMenu[i][10]);
			map.put("col10"	, arrayMenu[i][11]);
			map.put("col12"	, arrayMenu[i][12]);
			map.put("col13"	, arrayMenu[i][13]);
			map.put("col14"	, arrayMenu[i][14]);
			
			map.put("col15"	, arrayMenu[i][15]);
			liTotalData.add(map);
			
		}
		
		return liTotalData;
	}
	
	@Override
	public List<HistoryDocLifeCycle> selectHistoryDeptLifeCycle(HistoryDeleteFilterDto historyDeleteFilterDto) {
		
		List<HistoryDocLifeCycle> result = historyDao.selectHistoryDeptLifeCycle(historyDeleteFilterDto); 
		
		int iSumCntRe 	= result.stream().mapToInt(m-> Integer.parseInt(m.getCntRe())).sum();
		int iSumCntDp 	= result.stream().mapToInt(m-> Integer.parseInt(m.getCntDp())).sum();
		int iSumCntVed	= result.stream().mapToInt(m-> Integer.parseInt(m.getCntVed())).sum();
		int iSumCntVem	= result.stream().mapToInt(m-> Integer.parseInt(m.getCntVeM())).sum();
		int iSumCntEdW	= result.stream().mapToInt(m-> Integer.parseInt(m.getCntEdW())).sum();
		int iSumCntEdV	= result.stream().mapToInt(m-> Integer.parseInt(m.getCntEdV())).sum();
		int iSumCntAp 	= result.stream().mapToInt(m-> Integer.parseInt(m.getCntAp())).sum();
		int iSumCntDl 	= result.stream().mapToInt(m-> Integer.parseInt(m.getCntDl())).sum();
		int iSumCntDm 	= result.stream().mapToInt(m-> Integer.parseInt(m.getCntDm())).sum();
		int iSumCntTr 	= result.stream().mapToInt(m-> Integer.parseInt(m.getCntTr())).sum();
		int iSumCntCc 	= result.stream().mapToInt(m-> Integer.parseInt(m.getCntCc())).sum();
		int iSumCntCj 	= result.stream().mapToInt(m-> Integer.parseInt(m.getCntCj())).sum();
		int iSumCntCl 	= result.stream().mapToInt(m-> Integer.parseInt(m.getCntCl())).sum();
		int iSumCntRc 	= result.stream().mapToInt(m-> Integer.parseInt(m.getCntRc())).sum();
		int iSumCntLd 	= result.stream().mapToInt(m-> Integer.parseInt(m.getCntLd())).sum();
		int iSumCntLa 	= result.stream().mapToInt(m-> Integer.parseInt(m.getCntLa())).sum();
		int iSumCntLp 	= result.stream().mapToInt(m-> Integer.parseInt(m.getCntLp())).sum();
		int iSumCntDr 	= result.stream().mapToInt(m-> Integer.parseInt(m.getCntDr())).sum();
		int iSumCntDa 	= result.stream().mapToInt(m-> Integer.parseInt(m.getCntDa())).sum();
		
		// 부서 sum 추가
		HistoryDocLifeCycle hdlc = HistoryDocLifeCycle.builder()
														.uJobUser("historyAdmin")
														.jobUserName(StringUtils.isBlank(historyDeleteFilterDto.getDeptName()) ? "전체" : historyDeleteFilterDto.getDeptName())
														.cntRe(String.valueOf(iSumCntRe))
														.cntDp(String.valueOf(iSumCntDp))
														.cntVed(String.valueOf(iSumCntVed))
														.cntVeM(String.valueOf(iSumCntVem))
														.cntEdW(String.valueOf(iSumCntEdW))
														.cntEdV(String.valueOf(iSumCntEdV))
														.cntAp(String.valueOf(iSumCntAp))
														.cntDl(String.valueOf(iSumCntDl))
														.cntDm(String.valueOf(iSumCntDm))
														.cntTr(String.valueOf(iSumCntTr))
														.cntCc(String.valueOf(iSumCntCc))
														.cntCl(String.valueOf(iSumCntCl))
														.cntCj(String.valueOf(iSumCntCj))
														.cntRc(String.valueOf(iSumCntRc))
														.cntLd(String.valueOf(iSumCntLd))
														.cntLa(String.valueOf(iSumCntLa))
														.cntLp(String.valueOf(iSumCntLp))
														.cntDr(String.valueOf(iSumCntDr))
														.cntDa(String.valueOf(iSumCntDa))
														.build();
		
		result.add(0, hdlc);
		
		return result;
		
	}

	@Override
	public List<HistoryDocDistribution> selectHistoryDeptDistribution(HistoryDeleteFilterDto historyDeleteFilterDto) {
		
		List<HistoryDocDistribution> result = historyDao.selectHistoryDeptDistribution(historyDeleteFilterDto); 
		
		int iSumPr 	= result.stream().mapToInt(m-> Integer.parseInt(m.getCntPermitReq())).sum();
		int iSumPa 	= result.stream().mapToInt(m-> Integer.parseInt(m.getCntPermitApprove())).sum();
		int iSumSc 	= result.stream().mapToInt(m-> Integer.parseInt(m.getCntSecChange())).sum();
		int iSumScU = result.stream().mapToInt(m-> Integer.parseInt(m.getCntSecChangeUp())).sum();
		int iSumScD = result.stream().mapToInt(m-> Integer.parseInt(m.getCntSecChangeDown())).sum();
		int iSumTr 	= result.stream().mapToInt(m-> Integer.parseInt(m.getCntTakeReq())).sum();
		int iSumTa 	= result.stream().mapToInt(m-> Integer.parseInt(m.getCntTakeApprove())).sum();
		int iSumAt 	= result.stream().mapToInt(m-> Integer.parseInt(m.getCntAttach())).sum();
		int iSumPt	= result.stream().mapToInt(m-> Integer.parseInt(m.getCntPrint())).sum();
		
		// 부서 sum 추가
		HistoryDocDistribution hdd = HistoryDocDistribution.builder()
														.uJobUser("historyAdmin")
														.jobUserName(StringUtils.isBlank(historyDeleteFilterDto.getDeptName()) ? "전체" : historyDeleteFilterDto.getDeptName())
														.cntPermitReq(String.valueOf(iSumPr))
														.cntPermitApprove(String.valueOf(iSumPa))
														.cntSecChange(String.valueOf(iSumSc))
														.cntSecChangeUp(String.valueOf(iSumScU))
														.cntSecChangeDown(String.valueOf(iSumScD))
														.cntTakeReq(String.valueOf(iSumTr))
														.cntTakeApprove(String.valueOf(iSumTa))
														.cntAttach(String.valueOf(iSumAt))
														.cntPrint(String.valueOf(iSumPt))
														.build();
		
		result.add(0, hdd);
		
		return result;
	}

	@Override
	public List<ReqDelete> selectHistoryDocumentLifeCycle(HistoryDeleteFilterDto historyDeleteFilterDto) {
		return historyDao.selectHistoryDocumentLifeCycle(historyDeleteFilterDto);
	}

	@Override
	public List<ReqDelete> selectHistoryDocumentDistribution(HistoryDeleteFilterDto historyDeleteFilterDto) {
		return historyDao.selectHistoryDocumentDistribution(historyDeleteFilterDto);
	}

	@Override
	public List<ReqDelete> selectHistoryDelete(HistoryDeleteFilterDto historyDeleteFilterDto) {
		return historyDao.selectHistoryDelete(historyDeleteFilterDto);
	}
	
	@Override
	public List<ReqDelete> selectHistoryMessengerUser(HistoryDeleteFilterDto historyDeleteFilterDto) {
		return historyDao.selectHistoryMessengerUser(historyDeleteFilterDto);
	}
	
	@Override
	public List<ReqDelete> selectHistoryExternalAttach(HistoryDeleteFilterDto historyDeleteFilterDto) {
		return historyDao.selectHistoryExternalAttach(historyDeleteFilterDto);
	}
	
	@Override
	public List<ReqDelete> selectHistoryExternalAttachDetail(HistoryDeleteFilterDto historyDeleteFilterDto) {
		return historyDao.selectHistoryExternalAttachDetail(historyDeleteFilterDto);
	}
	
	@Override
	public List<HistoryAttach> selectHistoryLogDetail(HistoryDeleteFilterDto historyDeleteFilterDto) {
		
		// 출력은 다른 테이블 조회함 =>  cntPt : 특이사용자, cntPrint : 조직 or 문서 문서유통
		String colField = historyDeleteFilterDto.getColField();
		if(colField.equals("cntPt") || colField.equals("cntPrint"))
		{
			// 조직별 이력 			--> cntPrint(사용자별) : ""	 , "", 			"", 			colField, event.data.uJobUser, 	startDate, endDate, ""
			// 문서별 이력 			--> cntPrint(문서별) 	: uDocKey, uDocName,	uCabinetCode, 	colField, user, 				startDate, endDate
			// 기타 특이사용자 이력 	--> cntPt 			: oddRObjectId, colField, user:userid,
			return historyDao.selectHistoryLogPrintDetail(historyDeleteFilterDto);
		}
		else
		{
			// 부서, 문서 LifeCycle
			//	1. 부서 LifeCycle 사용자 선택
			//	2. 부서 LifeCycle 사용자 기능 선택
			//	3. 문서 LifeCycle 사용자 선택
			//	4. 문서 LifeCycle 사용자 기능 선택
			// 부서, 문서 유통
			//	5. 부서 유통 문서명 선택
			//	6. 부서 유통 문서 기능 선택
			//	7. 문서 유통 문서 선택
			//	8. 문서 유통 문서 기능 선택
			//	9. 분류특성 추가로 선택
			// 특이사용자 검색
			//	
			return historyDao.selectHistoryLogDetail(historyDeleteFilterDto);
		}

	}
	
	@Override
	public List<HistoryUsb> selectHistoryUsb(HistoryDeleteFilterDto historyDeleteFilterDto) {
		return historyDao.selectHistoryUsb(historyDeleteFilterDto);
	}
	
	@Override
	public List<HistoryUsb> selectHistoryUsbDetail(HistoryDeleteFilterDto historyDeleteFilterDto) {
		return historyDao.selectHistoryUsbDetail(historyDeleteFilterDto);
	}
	
	
	@Override
	public List<HistoryUsb> selectHistoryOdd(HistoryDeleteFilterDto historyDeleteFilterDto) {
		
		//=========================================
		// 특이사용자 이력
		//=========================================
		// 	1. EDMS_USER_LOCK 에서 특이사용자 잠금 리스트조회
		// 	2. 잠금리스트에서  ( EDMS_USER_LOCK 테이블에서 이전 잠금해제 일조회
		// 	3. 이전 잠금일 ~~  현재 잠금일 을구한후 EDMS_LOG_ODD 에서 기준 초과한 리스트조회
		// 	4. 기준초과한 부분 다운로드, 반출 권한신청, 출력, 삭제 가 있으면 group by 해서 있으면 O 없으면 빈값 처리함
		//-----------------------------------------
		// 상세 이력
		// 	1. 선택한 잠금일 기준으로(r_object_id, user ) 
		//	2. 다시 이전잠금일 ~ 현재 잠금일 이내에 있는 기준초과일을 구한후 ( 이때 기준 초과하지 않은 날짜는 조회 안함 )
		//	3. EDMS_LOG_DOC 에서 기준초과한 날짜에 맞는 이벤트에 맞게 조회함
		//=========================================
		
		// 상세이력 기간 조회 한번더 확인하자...
		
		return historyDao.selectHistoryOdd(historyDeleteFilterDto);
	}
	
	@Override
	public List<HistoryUsb> selectHistoryTrans(HistoryDeleteFilterDto historyDeleteFilterDto) {
		return historyDao.selectHistoryTrans(historyDeleteFilterDto);
	}
}
