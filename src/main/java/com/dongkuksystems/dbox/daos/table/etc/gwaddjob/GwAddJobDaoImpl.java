package com.dongkuksystems.dbox.daos.table.etc.gwaddjob;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.dto.etc.GwAddJobDetail;
import com.dongkuksystems.dbox.models.table.etc.GwAddJob;

@Primary
@Repository
public class GwAddJobDaoImpl implements GwAddJobDao{
	private GwAddJobMapper gwAddJobMapper;

	public GwAddJobDaoImpl(GwAddJobMapper gwAddJobMapper) {
		
		this.gwAddJobMapper = gwAddJobMapper;
	}

  @Override
	public List<GwAddJobDetail> selectDetailedListByAjId(String unitCode) {
    return gwAddJobMapper.selectDetailedListByAjId(unitCode);
	}
	
	@Override
	public List<GwAddJob> selectListByAjId(String unitCode) {
		return gwAddJobMapper.selectListByAjId(unitCode);
	}

  @Override
  public List<GwAddJob> selectListByUserId(String userId) {
    return gwAddJobMapper.selectListByUserId(userId);
  }

}
