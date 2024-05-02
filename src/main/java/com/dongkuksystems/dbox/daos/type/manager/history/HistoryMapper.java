package com.dongkuksystems.dbox.daos.type.manager.history;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.dto.type.manager.history.HistoryDeleteFilterDto;
import com.dongkuksystems.dbox.models.type.history.HistoryAttach;
import com.dongkuksystems.dbox.models.type.history.HistoryDocDistribution;
import com.dongkuksystems.dbox.models.type.history.HistoryDocLifeCycle;
import com.dongkuksystems.dbox.models.type.history.HistoryUsb;
import com.dongkuksystems.dbox.models.type.request.ReqDelete; 

public interface HistoryMapper { 
  
	public List<Map<String, String>> selectHistoryTotal(@Param("historyDeleteDto") HistoryDeleteFilterDto historyDeleteFilterDto, @Param("liGradeList") List<String> liGradeList);
	public List<Map<String, String>> selectHistoryTotalItem(@Param("historyDeleteDto") HistoryDeleteFilterDto historyDeleteFilterDto, @Param("liGradeList") List<String> liGradeList);
	public List<Map<String, String>> selectHistoryTotalAsset(@Param("historyDeleteDto") HistoryDeleteFilterDto historyDeleteFilterDto);
	
	public List<ReqDelete> selectHistoryDeletes(@Param("historyDeleteDto") HistoryDeleteFilterDto historyDeleteFilterDto);
	
	public List<ReqDelete> selectHistoryExternalAttach(@Param("historyDeleteDto") HistoryDeleteFilterDto historyDeleteFilterDto);
	public List<ReqDelete> selectHistoryMessengerUser(@Param("historyDeleteDto") HistoryDeleteFilterDto historyDeleteFilterDto);
	public List<ReqDelete> selectHistoryExternalAttachDetail(@Param("historyDeleteDto") HistoryDeleteFilterDto historyDeleteFilterDto);

	public List<HistoryDocLifeCycle> selectHistoryDeptLifeCycle(@Param("historyDeleteDto") HistoryDeleteFilterDto historyDeleteFilterDto);
	public List<HistoryDocDistribution> selectHistoryDeptDistribution(@Param("historyDeleteDto") HistoryDeleteFilterDto historyDeleteFilterDto);
	
	public List<ReqDelete> selectHistoryDocumentLifeCycle(@Param("historyDeleteDto") HistoryDeleteFilterDto historyDeleteFilterDto);
	public List<ReqDelete> selectHistoryDocumentDistribution(@Param("historyDeleteDto") HistoryDeleteFilterDto historyDeleteFilterDto);
	
	public List<HistoryAttach> selectHistoryLogDetail(@Param("historyDeleteDto") HistoryDeleteFilterDto historyDeleteFilterDto);
	public List<HistoryAttach> selectHistoryLogPrintDetail(@Param("historyDeleteDto") HistoryDeleteFilterDto historyDeleteFilterDto);
	
	public List<HistoryUsb> selectHistoryUsb(@Param("historyDeleteDto") HistoryDeleteFilterDto historyDeleteFilterDto);
	public List<HistoryUsb> selectHistoryUsbDetail(@Param("historyDeleteDto") HistoryDeleteFilterDto historyDeleteFilterDto);
	
	public List<HistoryUsb> selectHistoryOdd(@Param("historyDeleteDto") HistoryDeleteFilterDto historyDeleteFilterDto);
	public List<HistoryUsb> selectHistoryTrans(@Param("historyDeleteDto") HistoryDeleteFilterDto historyDeleteFilterDto);

}
