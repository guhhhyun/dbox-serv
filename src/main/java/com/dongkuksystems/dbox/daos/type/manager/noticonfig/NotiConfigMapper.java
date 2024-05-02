package com.dongkuksystems.dbox.daos.type.manager.noticonfig;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.type.manager.noticonfig.NotiConfig;

public interface NotiConfigMapper {
	public List<NotiConfig> selectAll(@Param("uComCode") String uComCode);

  public NotiConfig selectOneByCodes(@Param("uComCode") String uComCode, @Param("uEventCode") String uEventCode);

}
