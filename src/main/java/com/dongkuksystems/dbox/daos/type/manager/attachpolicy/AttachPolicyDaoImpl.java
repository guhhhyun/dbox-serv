package com.dongkuksystems.dbox.daos.type.manager.attachpolicy;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.type.manager.AttachPolicy;

@Primary
@Repository
public class AttachPolicyDaoImpl implements AttachPolicyDao {

	private AttachPolicyMapper attachPolicyMapper;

	public AttachPolicyDaoImpl(AttachPolicyMapper attachPolicyMapper) {
		this.attachPolicyMapper = attachPolicyMapper;
	}
	
	@Override
	public List<AttachPolicy> selectAll() {
		// return attachPolicyMapper.selectAll();
		
		return attachPolicyMapper.selectAll();
		
	}
	
	@Override
	public Optional<AttachPolicy> selectOneByObjectId(String rObjectId) {
		return attachPolicyMapper.selectOneByObjectId(rObjectId);
	}
	
	@Override
	public Optional<AttachPolicy> selectOneBySystemKey(String systemKey) {

		return attachPolicyMapper.selectOneBySystemKey(systemKey);
		
	}

	@Override
	public List<AttachPolicy> selectAllAgentPolicy(String uUserId) {
		return attachPolicyMapper.selectAllAgentPolicy(uUserId);
	}

	
	
}
