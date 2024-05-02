package com.dongkuksystems.dbox.daos.table.lvldown;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.type.request.ReqLvlDown;

@Primary
@Repository
public class LvlDownDaoImpl implements LvlDownDao{
  
  private LvlDownMapper lvlDownMapper;
  
  public LvlDownDaoImpl(LvlDownMapper lvlDownMapper) {
    this.lvlDownMapper = lvlDownMapper;
  }


  @Override
  public List<ReqLvlDown> reqLvlDownDetailAll() {
    
    return lvlDownMapper.reqLvlDownDetailAll();
  }

  @Override
  public ReqLvlDown dataByObjId(String rObjectId) {
 
    return lvlDownMapper.dataByObjId(rObjectId);
  }
  
  
  
}
