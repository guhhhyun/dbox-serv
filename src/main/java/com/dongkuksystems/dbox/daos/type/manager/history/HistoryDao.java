package com.dongkuksystems.dbox.daos.type.manager.history;

import java.util.List;
import java.util.Map;

import com.dongkuksystems.dbox.models.dto.type.manager.history.HistoryDeleteFilterDto;
import com.dongkuksystems.dbox.models.type.history.HistoryAttach;
import com.dongkuksystems.dbox.models.type.history.HistoryDocDistribution;
import com.dongkuksystems.dbox.models.type.history.HistoryDocLifeCycle;
import com.dongkuksystems.dbox.models.type.history.HistoryUsb;
import com.dongkuksystems.dbox.models.type.request.ReqDelete; 


public interface HistoryDao {
  
	List<Map<String, String>> selectHistoryTotal(HistoryDeleteFilterDto historyDeleteFilterDto, List<String> liGradeList);
	List<Map<String, String>> selectHistoryTotalItem(HistoryDeleteFilterDto historyDeleteFilterDto, List<String> liGradeList);
	List<Map<String, String>> selectHistoryTotalAsset(HistoryDeleteFilterDto historyDeleteFilterDto);
	
	List<ReqDelete> selectHistoryDelete(HistoryDeleteFilterDto historyDeleteFilterDto);
	List<ReqDelete> selectHistoryExternalAttach(HistoryDeleteFilterDto historyDeleteFilterDto);
	List<ReqDelete> selectHistoryMessengerUser(HistoryDeleteFilterDto historyDeleteFilterDto);
	List<ReqDelete> selectHistoryExternalAttachDetail(HistoryDeleteFilterDto historyDeleteFilterDto);
	
	List<HistoryDocLifeCycle> selectHistoryDeptLifeCycle(HistoryDeleteFilterDto historyDeleteFilterDto);
	List<HistoryDocDistribution> selectHistoryDeptDistribution(HistoryDeleteFilterDto historyDeleteFilterDto);
	
	List<ReqDelete> selectHistoryDocumentLifeCycle(HistoryDeleteFilterDto historyDeleteFilterDto);
	List<ReqDelete> selectHistoryDocumentDistribution(HistoryDeleteFilterDto historyDeleteFilterDto);
	
	List<HistoryAttach> selectHistoryLogDetail(HistoryDeleteFilterDto historyDeleteFilterDto);
	List<HistoryAttach> selectHistoryLogPrintDetail(HistoryDeleteFilterDto historyDeleteFilterDto);
	
	List<HistoryUsb> selectHistoryUsb(HistoryDeleteFilterDto historyDeleteFilterDto);
	List<HistoryUsb> selectHistoryUsbDetail(HistoryDeleteFilterDto historyDeleteFilterDto);
	
	List<HistoryUsb> selectHistoryOdd(HistoryDeleteFilterDto historyDeleteFilterDto);
	List<HistoryUsb> selectHistoryTrans(HistoryDeleteFilterDto historyDeleteFilterDto);

}
