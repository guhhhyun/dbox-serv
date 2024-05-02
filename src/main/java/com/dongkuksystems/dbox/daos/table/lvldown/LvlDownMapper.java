package com.dongkuksystems.dbox.daos.table.lvldown;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.type.request.ReqLvlDown;

public interface LvlDownMapper {

  public List<ReqLvlDown> reqLvlDownDetailAll();

  public ReqLvlDown dataByObjId(@Param("rObjectId") String rObjectId);

}
