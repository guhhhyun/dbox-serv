package com.dongkuksystems.dbox.daos.type.mobilever;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.dto.mobile.MobileVersionDetail;


@Primary
@Repository
public class MobileVersionDaoImpl implements MobileVersionDao {
  private final MobileVersionMapper mobileVersionMapper;

  public MobileVersionDaoImpl(MobileVersionMapper mobileVersionMapper) {
    this.mobileVersionMapper = mobileVersionMapper;
  }

  @Override
  public MobileVersionDetail mobileVersion() {

    return mobileVersionMapper.mobileVersion();
  }
  
}
