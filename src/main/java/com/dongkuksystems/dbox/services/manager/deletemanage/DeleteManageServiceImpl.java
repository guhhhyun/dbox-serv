package com.dongkuksystems.dbox.services.manager.deletemanage;

import java.util.List;

import org.springframework.stereotype.Service;

import com.dongkuksystems.dbox.daos.type.manager.deletemanage.DeleteManageDao;
import com.dongkuksystems.dbox.daos.type.path.PathDao;
import com.dongkuksystems.dbox.models.dto.type.manager.deletemanage.DeleteManageDto;
import com.dongkuksystems.dbox.models.type.manager.deletemanage.DeleteManage;
import com.dongkuksystems.dbox.models.type.manager.deletemanage.DeleteManageLog;
import com.dongkuksystems.dbox.models.type.manager.duplication.Duplication;
import com.dongkuksystems.dbox.services.AbstractCommonService;

@Service
public class DeleteManageServiceImpl extends AbstractCommonService implements DeleteManageService {

  private final DeleteManageDao deleteManageDao;
  private final PathDao pathDao;
  
  public DeleteManageServiceImpl(DeleteManageDao deleteManageDao, PathDao pathDao) {
    this.deleteManageDao = deleteManageDao;
    this.pathDao = pathDao;
  }
  
  @Override
  public List<DeleteManage> selectDeleteDocument(DeleteManageDto dto) {
    List<DeleteManage> list = deleteManageDao.selectDeleteDocument(dto);
    for(DeleteManage del : list) {
      String result = pathDao.selectFolderPath(del.getUFolId());
      del.setUFolderPath(result);
    }
    return list;
  }
  @Override
  public List<DeleteManageLog> selectDeleteDocumentLog(DeleteManageDto dto) {
    return deleteManageDao.selectDeleteDocumentLog(dto);
  }
}
