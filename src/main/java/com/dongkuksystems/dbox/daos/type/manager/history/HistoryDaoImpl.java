package com.dongkuksystems.dbox.daos.type.manager.history;

import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.dto.type.manager.history.HistoryDeleteFilterDto;
import com.dongkuksystems.dbox.models.type.history.HistoryAttach;
import com.dongkuksystems.dbox.models.type.history.HistoryDocDistribution;
import com.dongkuksystems.dbox.models.type.history.HistoryDocLifeCycle;
import com.dongkuksystems.dbox.models.type.history.HistoryUsb;
import com.dongkuksystems.dbox.models.type.request.ReqDelete;

@Primary
@Repository
public class HistoryDaoImpl implements HistoryDao {
	
	private HistoryMapper historyMapper;

	public HistoryDaoImpl(HistoryMapper historyMapper) {
		this.historyMapper = historyMapper;
	}

	@Override
	public List<Map<String, String>> selectHistoryTotal(HistoryDeleteFilterDto historyDeleteFilterDto, List<String> liGradeList) {
		return historyMapper.selectHistoryTotal(historyDeleteFilterDto, liGradeList);
	}
	
	@Override
	public List<Map<String, String>> selectHistoryTotalItem(HistoryDeleteFilterDto historyDeleteFilterDto, List<String> liGradeList) {
		return historyMapper.selectHistoryTotalItem(historyDeleteFilterDto, liGradeList);
	}
	
	@Override
	public List<Map<String, String>> selectHistoryTotalAsset(HistoryDeleteFilterDto historyDeleteFilterDto) {
		return historyMapper.selectHistoryTotalAsset(historyDeleteFilterDto);
	}
	
	@Override
	public List<ReqDelete> selectHistoryDelete(HistoryDeleteFilterDto historyDeleteFilterDto) {
		return historyMapper.selectHistoryDeletes(historyDeleteFilterDto);
	}

	@Override
	public List<ReqDelete> selectHistoryMessengerUser(HistoryDeleteFilterDto historyDeleteFilterDto) {
		return historyMapper.selectHistoryMessengerUser(historyDeleteFilterDto);
	}
	
	@Override
	public List<ReqDelete> selectHistoryExternalAttach(HistoryDeleteFilterDto historyDeleteFilterDto) {
		return historyMapper.selectHistoryExternalAttach(historyDeleteFilterDto);
	}
	
	@Override
	public List<ReqDelete> selectHistoryExternalAttachDetail(HistoryDeleteFilterDto historyDeleteFilterDto) {
		return historyMapper.selectHistoryExternalAttachDetail(historyDeleteFilterDto);
	}
	
	@Override
	public List<HistoryDocLifeCycle> selectHistoryDeptLifeCycle(HistoryDeleteFilterDto historyDeleteFilterDto) {
		return historyMapper.selectHistoryDeptLifeCycle(historyDeleteFilterDto);
	}
	
	@Override
	public List<HistoryDocDistribution> selectHistoryDeptDistribution(HistoryDeleteFilterDto historyDeleteFilterDto) {
		return historyMapper.selectHistoryDeptDistribution(historyDeleteFilterDto);
	}
	
	@Override
	public List<ReqDelete> selectHistoryDocumentLifeCycle(HistoryDeleteFilterDto historyDeleteFilterDto) {
		return historyMapper.selectHistoryDocumentLifeCycle(historyDeleteFilterDto);
	}
	
	@Override
	public List<ReqDelete> selectHistoryDocumentDistribution(HistoryDeleteFilterDto historyDeleteFilterDto) {
		return historyMapper.selectHistoryDocumentDistribution(historyDeleteFilterDto);
	}
	
	
	@Override
	public List<HistoryAttach> selectHistoryLogDetail(HistoryDeleteFilterDto historyDeleteFilterDto) {
		return historyMapper.selectHistoryLogDetail(historyDeleteFilterDto);
	}
	
	@Override
	public List<HistoryAttach> selectHistoryLogPrintDetail(HistoryDeleteFilterDto historyDeleteFilterDto) {
		return historyMapper.selectHistoryLogPrintDetail(historyDeleteFilterDto);
	}
	
	@Override
	public List<HistoryUsb> selectHistoryUsb(HistoryDeleteFilterDto historyDeleteFilterDto) {
		return historyMapper.selectHistoryUsb(historyDeleteFilterDto);
	}
	
	@Override
	public List<HistoryUsb> selectHistoryUsbDetail(HistoryDeleteFilterDto historyDeleteFilterDto) {
		return historyMapper.selectHistoryUsbDetail(historyDeleteFilterDto);
	}
	
	@Override
	public List<HistoryUsb> selectHistoryOdd(HistoryDeleteFilterDto historyDeleteFilterDto) {
		return historyMapper.selectHistoryOdd(historyDeleteFilterDto);
	}
	
	@Override
	public List<HistoryUsb> selectHistoryTrans(HistoryDeleteFilterDto historyDeleteFilterDto) {
		return historyMapper.selectHistoryTrans(historyDeleteFilterDto);
	}
}
