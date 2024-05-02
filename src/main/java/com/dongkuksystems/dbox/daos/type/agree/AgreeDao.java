package com.dongkuksystems.dbox.daos.type.agree;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.dto.type.agree.AgreeFilter;
import com.dongkuksystems.dbox.models.type.agree.Agree;

public interface AgreeDao {
	public Optional<Agree> agreementByUserId(String uUserId);
	public List<Agree> selectList(AgreeFilter filter);
	public List<Agree> selectListByUserId(String uUserId);
	public List<Agree> selectListByOrgId(String orgId);
    public List<Agree> selectListUserId(String userId);
}
