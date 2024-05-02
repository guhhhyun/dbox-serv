package com.dongkuksystems.dbox.daos.type.manager.disposalrequest;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.dto.type.manager.disposalrequest.DisposalRequestDto;
import com.dongkuksystems.dbox.models.type.manager.disposalrequest.DisposalRequest;

@Primary
@Repository
public class DisposalRequestDaoImpl implements DisposalRequestDao {
private DisposalRequestMapper disposalRequestMapper;
  
  public DisposalRequestDaoImpl(DisposalRequestMapper disposalRequestMapper) {
    this.disposalRequestMapper = disposalRequestMapper;
  }
  
  @Override
    public List<DisposalRequest> selectDisposalRequest(DisposalRequestDto dto) {
      return disposalRequestMapper.selectDisposalRequest(dto);
    } 
}
