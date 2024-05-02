package com.dongkuksystems.dbox.daos.type.manager.lockeddata;

import com.dongkuksystems.dbox.models.dto.type.manager.managerconfig.LockedDataDto;

import java.util.List;
import java.util.Map;

public interface LockedDataMapper {

    List<Map<String, Object>> selectLockedData(LockedDataDto lockedDataDto);

}