package com.dongkuksystems.dbox.daos.type.manager.preservationperiod;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.dto.type.manager.preservationperiod.PreservationPeriodDto;

public interface PreservationPeriodMapper {

    Map<String, Object> selectPreservationPeriodByComCode(String comCode);

    List<Map<String, Object>> selectDepts();

    PreservationPeriodDto selectOneByComCode(@Param("uComCode") String comCode);

}