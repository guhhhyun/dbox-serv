package com.dongkuksystems.dbox.services.doc;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.dongkuksystems.dbox.daos.type.doc.DocImpDao;
import com.dongkuksystems.dbox.models.type.doc.DocImp;
import com.dongkuksystems.dbox.services.AbstractCommonService;

@Service
public class DocImpServiceImpl extends AbstractCommonService implements DocImpService {
  private final DocImpDao docImpDao;

  public DocImpServiceImpl(DocImpDao docImpDao) {
    this.docImpDao = docImpDao;
  }
  
  @Override
  public Optional<DocImp> selectOne(String rObjectId) throws Exception {
  	return docImpDao.selectOne(rObjectId);
  }
}
