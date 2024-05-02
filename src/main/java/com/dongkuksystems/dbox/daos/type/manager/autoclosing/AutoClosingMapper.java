package com.dongkuksystems.dbox.daos.type.manager.autoclosing;

import com.dongkuksystems.dbox.models.dto.type.manager.managerconfig.AutoClosingDto;

import java.util.List;
import java.util.Map;

public interface AutoClosingMapper {

    Map<String, Object> selectPeriodToCloseByComCode(String comCode);

    List<Map<String, Object>> selectDataToClose(AutoClosingDto autoClosingDto);

    List<Map<String, Object>> selectDataByDocKeyToClose(String docKey);

}