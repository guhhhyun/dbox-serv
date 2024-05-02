package com.dongkuksystems.dbox.daos.type.manager.agentpolicy;

import java.util.Optional;

import com.dongkuksystems.dbox.models.type.manager.UsbPolicy;
import com.dongkuksystems.dbox.models.type.request.ReqUseUsb;

public interface AgentDao {

	  public Optional<ReqUseUsb> selectUseUsbPolicy(String uReqUserId, String uDeptCode);
	  public Optional<UsbPolicy> selectUserDeptUsbPolicy(String uReqUserId, String uDeptCode);
}
