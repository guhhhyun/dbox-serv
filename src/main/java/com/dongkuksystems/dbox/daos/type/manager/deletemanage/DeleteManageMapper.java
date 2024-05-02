package com.dongkuksystems.dbox.daos.type.manager.deletemanage;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.dto.type.manager.deletemanage.DeleteManageDto;
import com.dongkuksystems.dbox.models.type.manager.deletemanage.DeleteManage;
import com.dongkuksystems.dbox.models.type.manager.deletemanage.DeleteManageLog;

public interface DeleteManageMapper {
  public List<DeleteManage> selectDeleteDocument(@Param("deleteManage") DeleteManageDto dto);
  public List<DeleteManageLog> selectDeleteDocumentLog(@Param("deleteManage") DeleteManageDto dto);
}
