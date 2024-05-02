package com.dongkuksystems.dbox.services.manager.stopword;

import java.util.List;

import org.springframework.stereotype.Service;

import com.documentum.fc.client.IDfGroup;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;
import com.dongkuksystems.dbox.daos.type.manager.stopword.StopWordDao;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.stopword.StopWordDto;
import com.dongkuksystems.dbox.models.type.manager.stopword.StopWord;
import com.dongkuksystems.dbox.services.AbstractCommonService;
import com.dongkuksystems.dbox.services.manager.stopword.StopWordService;
import com.dongkuksystems.dbox.utils.dctm.DCTMUtils;

@Service
public class StopWordServiceImpl extends AbstractCommonService implements StopWordService {

  private final StopWordDao stopWordDao;

  public StopWordServiceImpl(StopWordDao stopWordDao) {
    this.stopWordDao = stopWordDao;
  }

  @Override
  public List<StopWord> selectStopWord(String companyCode) {
    return stopWordDao.selectStopWord(companyCode);
  }

  @Override
  public List<StopWord> selectStopWordGroup(String companyCode ) {
    return stopWordDao.selectStopWordGroup(companyCode );
  }
  
  
  @Override
  public List<StopWord> selectBlindDept(String companyCode ) {
    return stopWordDao.selectBlindDept(companyCode );
  }

   




}
