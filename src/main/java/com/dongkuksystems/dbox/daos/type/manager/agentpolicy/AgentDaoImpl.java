package com.dongkuksystems.dbox.daos.type.manager.agentpolicy;

import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.type.manager.UsbPolicy;
import com.dongkuksystems.dbox.models.type.request.ReqUseUsb;


@Primary
@Repository
public class AgentDaoImpl implements AgentDao {
	private AgentMapper agentMapper;
	
	public AgentDaoImpl(AgentMapper agentMapper) {
		this.agentMapper = agentMapper;
	}

	@Override
	public Optional<ReqUseUsb> selectUseUsbPolicy(String uReqUserId, String uDeptCode) {
		return agentMapper.selectUseUsbPolicy(uReqUserId, uDeptCode);
	}
	
	@Override
	public Optional<UsbPolicy> selectUserDeptUsbPolicy(String uReqUserId, String uDeptCode) {
		return agentMapper.selectUserDeptUsbPolicy(uReqUserId, uDeptCode);
	}

	@Override
	public int selectLiveUpdateTarget(String uReqUserId) {
		return agentMapper.selectLiveUpdateTarget(uReqUserId);
	}

}
