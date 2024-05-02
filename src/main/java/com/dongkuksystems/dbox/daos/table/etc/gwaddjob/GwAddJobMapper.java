package com.dongkuksystems.dbox.daos.table.etc.gwaddjob;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.dto.etc.GwAddJobDetail;
import com.dongkuksystems.dbox.models.table.etc.GwAddJob;

public interface GwAddJobMapper {
  public List<GwAddJobDetail> selectDetailedListByAjId(@Param("unitCode") String unitCode);
	public List<GwAddJob> selectListByAjId(@Param("unitCode") String unitCode);
	public List<GwAddJob> selectListByUserId(@Param("userId") String userId);
}
