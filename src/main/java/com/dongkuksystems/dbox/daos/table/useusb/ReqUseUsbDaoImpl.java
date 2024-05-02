package com.dongkuksystems.dbox.daos.table.useusb;
 
import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.dto.type.request.ReqTakeoutConfigDto;
import com.dongkuksystems.dbox.models.dto.type.request.ReqTakeoutDetailDto;
import com.dongkuksystems.dbox.models.dto.type.request.ReqUseUsbApprovalListDto;
import com.dongkuksystems.dbox.models.type.manager.TakeoutConfig;
import com.dongkuksystems.dbox.models.type.request.ReqTakeout;
import com.dongkuksystems.dbox.models.type.request.ReqTakeoutDoc;
import com.dongkuksystems.dbox.models.type.request.ReqUseUsb;
 
@Primary
@Repository
public class ReqUseUsbDaoImpl implements ReqUseUsbDao{
    private ReqUseUsbMapper reqUseUsbMapper;
 
    public ReqUseUsbDaoImpl(ReqUseUsbMapper reqUseUsbMapper) {
        this.reqUseUsbMapper = reqUseUsbMapper;
    }
    
    @Override
    public Optional<ReqUseUsb> selectOneByObjectId(String rObjectId) {
        
        return reqUseUsbMapper.selectOneByObjectId(rObjectId);
    }
 
    @Override
    public List<ReqUseUsbApprovalListDto> selectReqUseUsbApprovList(String uApprover) {
      return reqUseUsbMapper.selectReqUseUsbApprovList(uApprover);
    }
}
