package com.dongkuksystems.dbox.daos.type.manager.agentpolicy;

import java.util.Optional;

import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.type.manager.UsbPolicy;
import com.dongkuksystems.dbox.models.type.request.ReqUseUsb;

public interface AgentMapper {

	  public Optional<ReqUseUsb> selectUseUsbPolicy(@Param("uReqUserId") String uReqUserId, @Param("uDeptCode") String uDeptCode);
	  
	  public Optional<UsbPolicy> selectUserDeptUsbPolicy(@Param("uReqUserId") String uReqUserId, @Param("uDeptCode") String uDeptCode);

	  public int selectLiveUpdateTarget(@Param("uReqUserId") String uReqUserId);
	  
}
