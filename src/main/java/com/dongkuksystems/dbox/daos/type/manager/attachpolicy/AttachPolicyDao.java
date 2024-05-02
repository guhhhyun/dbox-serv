package com.dongkuksystems.dbox.daos.type.manager.attachpolicy;

import java.util.List;
import java.util.Optional;

import com.dongkuksystems.dbox.models.type.manager.AttachPolicy;

public interface AttachPolicyDao {

	public List<AttachPolicy> selectAll(); 
	public Optional<AttachPolicy> selectOneByObjectId(String rObjectId);
	public Optional<AttachPolicy> selectOneBySystemKey(String systemKey);
	public List<AttachPolicy> selectAllAgentPolicy(String uUserId);
}
