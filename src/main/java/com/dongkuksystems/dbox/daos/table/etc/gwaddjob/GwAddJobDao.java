package com.dongkuksystems.dbox.daos.table.etc.gwaddjob;

import java.util.List;

import com.dongkuksystems.dbox.models.dto.etc.GwAddJobDetail;
import com.dongkuksystems.dbox.models.table.etc.GwAddJob;

public interface GwAddJobDao {

  public List<GwAddJobDetail> selectDetailedListByAjId(String unitCode);
  public List<GwAddJob> selectListByAjId(String unitCode);
  public List<GwAddJob> selectListByUserId(String userId);
}
