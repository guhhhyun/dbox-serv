package com.dongkuksystems.dbox.daos.type.manager.noticonfig;

import java.util.List;

import com.dongkuksystems.dbox.models.type.manager.noticonfig.NotiConfig;

public interface NotiConfigDao {
	public List<NotiConfig> selectAll(String uComCode);
	public NotiConfig selectOneByCodes(String uComCode, String uEventCode);
}
