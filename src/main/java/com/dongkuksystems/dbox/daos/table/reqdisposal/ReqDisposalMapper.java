package com.dongkuksystems.dbox.daos.table.reqdisposal;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.dto.type.request.ReqDisposalDetailDto;
import com.dongkuksystems.dbox.models.dto.type.request.ReqDisposalFilterDto;
import com.dongkuksystems.dbox.models.type.request.ReqDelete;

public interface ReqDisposalMapper {

  public List<ReqDelete> reqClosedDelDetailAll();

  public ReqDelete dataByObjId(@Param("rObjectId") String rObjectId);
  
  public List<ReqDisposalDetailDto> selectListByDeptCode(@Param("deptCode") String deptCode, @Param("reqDisposal") ReqDisposalFilterDto dto);


}
