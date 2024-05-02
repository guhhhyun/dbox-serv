package com.dongkuksystems.dbox.daos.table.reqdisposal;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.dto.type.request.ReqDisposalDetailDto;
import com.dongkuksystems.dbox.models.dto.type.request.ReqDisposalFilterDto;
import com.dongkuksystems.dbox.models.type.request.ReqDelete;

@Primary
@Repository
public class ReqDisposalDaoImpl implements ReqDisposalDao {
  private ReqDisposalMapper reqDisposalMapper;
    
  public ReqDisposalDaoImpl(ReqDisposalMapper reqDisposalMapper) {
    this.reqDisposalMapper = reqDisposalMapper;
  }

  @Override
  public List<ReqDelete> reqClosedDelDetailAll() {
  
    return reqDisposalMapper.reqClosedDelDetailAll();
  }

  @Override
  public ReqDelete dataByObjId(String rObjectId) {

    return reqDisposalMapper.dataByObjId(rObjectId);
  }

  @Override
  public List<ReqDisposalDetailDto> selectListByDeptCode(String deptCode, ReqDisposalFilterDto dto) {
    return reqDisposalMapper.selectListByDeptCode(deptCode, dto);
  }
  
}
