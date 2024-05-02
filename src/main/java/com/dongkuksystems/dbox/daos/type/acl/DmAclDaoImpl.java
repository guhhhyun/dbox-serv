package com.dongkuksystems.dbox.daos.type.acl;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.dto.type.acl.DmAclFilterDto;
import com.dongkuksystems.dbox.models.type.acl.DmAcl;
import com.dongkuksystems.dbox.models.type.acl.DmAclRepeating;

@Primary
@Repository
public class DmAclDaoImpl implements DmAclDao {
	private DmAclMapper dmAclMapper;

	public DmAclDaoImpl(DmAclMapper dmAclMapper) {
		this.dmAclMapper = dmAclMapper;
	}

	@Override
	public List<DmAcl> selectList(DmAclFilterDto dmAclFilterDto) {
		return dmAclMapper.selectList(dmAclFilterDto);
	}

	@Override
	public List<DmAclRepeating> selectRepeatingList(DmAclFilterDto dmAclFilterDto) {
		return dmAclMapper.selectRepeatingList(dmAclFilterDto);
	}

}
