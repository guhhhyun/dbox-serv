package com.dongkuksystems.dbox.daos.table.lvldown;

import java.util.List;

import com.dongkuksystems.dbox.models.type.request.ReqLvlDown;


public interface LvlDownDao {
  public List<ReqLvlDown> reqLvlDownDetailAll();
  public ReqLvlDown dataByObjId(String rObjectId);
}
