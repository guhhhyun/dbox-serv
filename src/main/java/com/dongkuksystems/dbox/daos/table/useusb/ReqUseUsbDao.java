package com.dongkuksystems.dbox.daos.table.useusb;

import java.util.List;
import java.util.Optional;

import com.dongkuksystems.dbox.models.dto.type.request.ReqTakeoutConfigDto;
import com.dongkuksystems.dbox.models.dto.type.request.ReqTakeoutDetailDto;
import com.dongkuksystems.dbox.models.dto.type.request.ReqUseUsbApprovalListDto;
import com.dongkuksystems.dbox.models.type.manager.TakeoutConfig;
import com.dongkuksystems.dbox.models.type.request.ReqTakeout;
import com.dongkuksystems.dbox.models.type.request.ReqTakeoutDoc;
import com.dongkuksystems.dbox.models.type.request.ReqUseUsb;

public interface ReqUseUsbDao {
	
    public Optional<ReqUseUsb> selectOneByObjectId(String rObjectId);
	public List<ReqUseUsbApprovalListDto> selectReqUseUsbApprovList(String userId);
}
