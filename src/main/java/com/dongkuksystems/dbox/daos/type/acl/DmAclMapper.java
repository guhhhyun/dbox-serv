package com.dongkuksystems.dbox.daos.type.acl;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.dto.type.acl.DmAclFilterDto;
import com.dongkuksystems.dbox.models.type.acl.DmAcl;
import com.dongkuksystems.dbox.models.type.acl.DmAclRepeating;

public interface DmAclMapper {
	public List<DmAcl> selectList(@Param("dmAcl") DmAclFilterDto dmAclFilterDto);
	public List<DmAclRepeating> selectRepeatingList(@Param("dmAcl") DmAclFilterDto dmAclFilterDto);
}
