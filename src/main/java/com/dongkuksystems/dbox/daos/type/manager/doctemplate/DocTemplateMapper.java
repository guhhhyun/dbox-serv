package com.dongkuksystems.dbox.daos.type.manager.doctemplate;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.type.doc.DocTemplate; 

public interface DocTemplateMapper { 
  public List<DocTemplate> selectAll();
  public Optional<DocTemplate> selectOneById(@Param("docTemplateId") String docTemplateId);
  public Optional<DocTemplate> selectOneByTemplateType(@Param("templateType") String templateType);
  public List<DocTemplate> selectTemplates(@Param("comOrgId") String comOrgId, @Param("delStatus") String delStatus);
  
}
