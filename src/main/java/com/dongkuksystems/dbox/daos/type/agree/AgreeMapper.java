package com.dongkuksystems.dbox.daos.type.agree;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.dto.type.agree.AgreeFilter;
import com.dongkuksystems.dbox.models.type.agree.Agree;

public interface AgreeMapper {

	public Optional<Agree> agreementByUserId(@Param("uUserId") String uUserId);
	public List<Agree> selectList(@Param("filter") AgreeFilter filter);
	public List<Agree> selectListByUserId(@Param("uUserId") String uUserId);
	public List<Agree> selectListByOrgId(@Param("orgId") String orgId);
    public List<Agree> selectListUserId(@Param("userId")String userId);

}
