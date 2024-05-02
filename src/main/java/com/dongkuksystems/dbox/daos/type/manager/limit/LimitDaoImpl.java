package com.dongkuksystems.dbox.daos.type.manager.limit;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.type.manager.limit.Limit;

@Primary
@Repository
public class LimitDaoImpl implements LimitDao {
  private LimitMapper limitMapper;

  public LimitDaoImpl(LimitMapper limitMapper) {
    this.limitMapper = limitMapper;
  }

  @Override
  public List<Limit> selectLimitValue(String uComCode) {
    return limitMapper.selectLimitValue(uComCode);
  }
}