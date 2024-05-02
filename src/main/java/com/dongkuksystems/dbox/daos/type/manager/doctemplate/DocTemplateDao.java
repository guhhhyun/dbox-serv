package com.dongkuksystems.dbox.daos.type.manager.doctemplate;

import java.util.List;
import java.util.Optional;

import com.dongkuksystems.dbox.models.type.doc.DocTemplate; 


public interface DocTemplateDao {
  public List<DocTemplate> selectAll(); 
  public Optional<DocTemplate> selectOneById(String docTemplateId);
  public Optional<DocTemplate> selectOneByTemplateType(String templateType);
  public List<DocTemplate> selectTemplates(String comOrgId, String delStatus); 
  
}
