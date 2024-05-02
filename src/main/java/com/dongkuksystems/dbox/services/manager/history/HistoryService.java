package com.dongkuksystems.dbox.services.manager.history;

import java.util.List;
import java.util.Map;

import com.dongkuksystems.dbox.models.dto.type.manager.history.HistoryDeleteFilterDto;
import com.dongkuksystems.dbox.models.type.history.HistoryAttach;
import com.dongkuksystems.dbox.models.type.history.HistoryDocDistribution;
import com.dongkuksystems.dbox.models.type.history.HistoryDocLifeCycle;
import com.dongkuksystems.dbox.models.type.history.HistoryUsb;
import com.dongkuksystems.dbox.models.type.request.ReqDelete;

public interface HistoryService {
	
	/**
	 * 현황 - 사용현황
	 * @param historyDeleteFilterDto
	 * @return
	 */
	List<Map<String, Object>> selectHistoryTotal(HistoryDeleteFilterDto historyDeleteFilterDto);
	
	/**
	 * 현황 - 항목별 사용 현황
	 * @param historyDeleteFilterDto
	 * @return
	 */
	List<Map<String, Object>> selectHistoryTotalItem(HistoryDeleteFilterDto historyDeleteFilterDto);
	
	/**
	 * 현황 - 자산화 현황
	 * @param historyDeleteFilterDto
	 * @return
	 */
	List<Map<String, Object>> selectHistoryTotalAsset(HistoryDeleteFilterDto historyDeleteFilterDto);
	
	/**
	 * 조직별 이력 - 문서 LifeCycle
	 * @param historyDeleteFilterDto
	 * @return
	 */
	List<HistoryDocLifeCycle> selectHistoryDeptLifeCycle(HistoryDeleteFilterDto historyDeleteFilterDto);

	/**
	 * 조직별 이력 - 문서 유통
	 * @param historyDeleteFilterDto
	 * @return
	 */
	List<HistoryDocDistribution> selectHistoryDeptDistribution(HistoryDeleteFilterDto historyDeleteFilterDto);

	/**
	 * 문서별 이력 - 문서 LifeCycle
	 * @param historyDeleteFilterDto
	 * @return
	 */
	List<ReqDelete> selectHistoryDocumentLifeCycle(HistoryDeleteFilterDto historyDeleteFilterDto);

	/**
	 * 문서별 이력 - 문서 유통
	 * @param historyDeleteFilterDto
	 * @return
	 */
	List<ReqDelete> selectHistoryDocumentDistribution(HistoryDeleteFilterDto historyDeleteFilterDto);

	List<ReqDelete> selectHistoryDelete(HistoryDeleteFilterDto historyDeleteFilterDto);
	List<ReqDelete> selectHistoryMessengerUser(HistoryDeleteFilterDto historyDeleteFilterDto);
	List<ReqDelete> selectHistoryExternalAttach(HistoryDeleteFilterDto historyDeleteFilterDto);
	List<ReqDelete> selectHistoryExternalAttachDetail(HistoryDeleteFilterDto historyDeleteFilterDto);
	
	/**
	 * 문서 로그 상세 이력
	 * @param historyDeleteFilterDto
	 * @return
	 */
	List<HistoryAttach> selectHistoryLogDetail(HistoryDeleteFilterDto historyDeleteFilterDto);
	
	/**
	 * 기타이력 - 외부저장매체 사용이력
	 * @param historyDeleteFilterDto
	 * @return
	 */
	List<HistoryUsb> selectHistoryUsb(HistoryDeleteFilterDto historyDeleteFilterDto);
	
	/**
	 * 기타이력 - 외부저장매체 사용이력 상세
	 * @param historyDeleteFilterDto
	 * @return
	 */
	List<HistoryUsb> selectHistoryUsbDetail(HistoryDeleteFilterDto historyDeleteFilterDto);

	
	/**
	 * 기타이력 - 특이사용자 이력
	 * @param historyDeleteFilterDto
	 * @return
	 */
	List<HistoryUsb> selectHistoryOdd(HistoryDeleteFilterDto historyDeleteFilterDto);
	
	/**
	 * 기타이력 - 자료이관 이력
	 * @param historyDeleteFilterDto
	 * @return
	 */
	List<HistoryUsb> selectHistoryTrans(HistoryDeleteFilterDto historyDeleteFilterDto);

}
