package com.dongkuksystems.dbox.services.mobile;

import org.springframework.stereotype.Service;

import com.dongkuksystems.dbox.daos.type.mobilever.MobileVersionDao;
import com.dongkuksystems.dbox.models.dto.mobile.MobileVersionDetail;
import com.dongkuksystems.dbox.services.AbstractCommonService;

@Service
public class MobileVersionServiceImpl extends AbstractCommonService implements MobileVersionService{
  private final MobileVersionDao mobileVersionDao;

  public MobileVersionServiceImpl(MobileVersionDao mobileVersionDao) {
    this.mobileVersionDao = mobileVersionDao;
  }

  @Override
  public MobileVersionDetail mobileVersion() throws Exception {

    return mobileVersionDao.mobileVersion();
  }
  
}
