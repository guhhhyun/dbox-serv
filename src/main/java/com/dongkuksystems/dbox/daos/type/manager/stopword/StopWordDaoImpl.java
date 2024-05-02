package com.dongkuksystems.dbox.daos.type.manager.stopword;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.type.manager.stopword.StopWord;
//import com.dongkuksystems.dbox.models.dto.type.manager.rolemanagement.RoleManagementDto;
import com.dongkuksystems.dbox.models.type.manager.stopword.StopWord;


@Primary
@Repository
public class StopWordDaoImpl implements StopWordDao {
	private StopWordMapper stopWordMapper;

	public StopWordDaoImpl(StopWordMapper stopWordMapper) {
		this.stopWordMapper = stopWordMapper;
	}

	@Override
	public List<StopWord> selectStopWord(String companyCode) {
		return stopWordMapper.selectStopWord(companyCode);
	}
	
	 

	@Override
	public List<StopWord> selectStopWordGroup(String companyCode ) {
		return stopWordMapper.selectStopWordGroup(companyCode );
	}
	
	@Override
	public List<StopWord> selectBlindDept(String companyCode ) {
		return stopWordMapper.selectBlindDept(companyCode );
	}
	
	 
}
