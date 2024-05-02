package com.dongkuksystems.dbox.daos.table.reqdisposal;

import java.util.List;

import com.dongkuksystems.dbox.models.dto.type.request.ReqDisposalDetailDto;
import com.dongkuksystems.dbox.models.dto.type.request.ReqDisposalFilterDto;
import com.dongkuksystems.dbox.models.type.request.ReqDelete;

public interface ReqDisposalDao {
  public List<ReqDelete> reqClosedDelDetailAll();
  public ReqDelete dataByObjId(String rObjectId);
  public List<ReqDisposalDetailDto> selectListByDeptCode(String deptCode, ReqDisposalFilterDto dto);
}
