package com.dongkuksystems.dbox.daos.type.manager.approval;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.type.code.Code;


@Primary
@Repository
public class ApprovalDaoImpl implements ApprovalDao {
	private ApprovalMapper approvalMapper;
	
	public ApprovalDaoImpl(ApprovalMapper approvalMapper) {
		this.approvalMapper = approvalMapper;
	}
	
	@Override
	  public List<Code> selectApproval(String uCodeVal1) {
	    return approvalMapper.selectApproval(uCodeVal1);
	  }

}
