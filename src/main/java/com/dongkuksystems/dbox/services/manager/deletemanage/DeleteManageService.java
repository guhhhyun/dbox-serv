package com.dongkuksystems.dbox.services.manager.deletemanage;

import java.util.List;

import com.dongkuksystems.dbox.models.dto.type.manager.deletemanage.DeleteManageDto;
import com.dongkuksystems.dbox.models.type.manager.deletemanage.DeleteManage;
import com.dongkuksystems.dbox.models.type.manager.deletemanage.DeleteManageLog;

public interface DeleteManageService {
  List<DeleteManage> selectDeleteDocument(DeleteManageDto dto);
  
  List<DeleteManageLog> selectDeleteDocumentLog(DeleteManageDto dto);

}
