package com.dongkuksystems.dbox.daos.type.acl;

import java.util.List;

import com.dongkuksystems.dbox.models.dto.type.acl.DmAclFilterDto;
import com.dongkuksystems.dbox.models.type.acl.DmAcl;
import com.dongkuksystems.dbox.models.type.acl.DmAclRepeating;

public interface DmAclDao {
	public List<DmAcl> selectList(DmAclFilterDto dmAclFilterDto);
	public List<DmAclRepeating> selectRepeatingList(DmAclFilterDto dmAclFilterDto);
}
