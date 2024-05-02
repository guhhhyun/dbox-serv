package com.dongkuksystems.dbox.daos.type.manager.deletemanage;

import java.util.List;

import com.dongkuksystems.dbox.models.dto.type.manager.deletemanage.DeleteManageDto;
import com.dongkuksystems.dbox.models.type.manager.deletemanage.DeleteManage;
import com.dongkuksystems.dbox.models.type.manager.deletemanage.DeleteManageLog;

public interface DeleteManageDao {
  public List<DeleteManage> selectDeleteDocument(DeleteManageDto dto);
  public List<DeleteManageLog> selectDeleteDocumentLog(DeleteManageDto dto);
}
