package com.dongkuksystems.dbox.daos.type.manager.disposalrequest;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.dto.type.manager.disposalrequest.DisposalRequestDto;
import com.dongkuksystems.dbox.models.type.manager.disposalrequest.DisposalRequest;

public interface DisposalRequestMapper {
  public List<DisposalRequest> selectDisposalRequest(@Param("disposalRequest") DisposalRequestDto dto);
}
