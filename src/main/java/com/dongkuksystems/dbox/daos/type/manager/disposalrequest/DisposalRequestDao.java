package com.dongkuksystems.dbox.daos.type.manager.disposalrequest;

import java.util.List;

import com.dongkuksystems.dbox.models.dto.type.manager.disposalrequest.DisposalRequestDto;
import com.dongkuksystems.dbox.models.type.manager.disposalrequest.DisposalRequest;

public interface DisposalRequestDao {
  public List<DisposalRequest> selectDisposalRequest(DisposalRequestDto dto);
}
