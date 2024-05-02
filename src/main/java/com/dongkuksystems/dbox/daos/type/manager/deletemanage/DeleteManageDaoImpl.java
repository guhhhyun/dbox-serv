package com.dongkuksystems.dbox.daos.type.manager.deletemanage;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.dto.type.manager.deletemanage.DeleteManageDto;
import com.dongkuksystems.dbox.models.type.manager.deletemanage.DeleteManage;
import com.dongkuksystems.dbox.models.type.manager.deletemanage.DeleteManageLog;

@Primary
@Repository
public class DeleteManageDaoImpl implements DeleteManageDao {
  private DeleteManageMapper deleteManageMapper;
  
  public DeleteManageDaoImpl(DeleteManageMapper deleteManageMapper) {
    this.deleteManageMapper = deleteManageMapper;
  }
  
  @Override
    public List<DeleteManage> selectDeleteDocument(DeleteManageDto dto) {
      return deleteManageMapper.selectDeleteDocument(dto);
    }  
  @Override
  public List<DeleteManageLog> selectDeleteDocumentLog(DeleteManageDto dto) {
    return deleteManageMapper.selectDeleteDocumentLog(dto);
  }  
}
