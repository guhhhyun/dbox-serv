package com.dongkuksystems.dbox.daos.type.manager.limit;

import java.util.List;

import com.dongkuksystems.dbox.models.type.manager.limit.Limit;

public interface LimitMapper {

	public List<Limit> selectLimitValue(String uComCode);
}
