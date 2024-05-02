package com.dongkuksystems.dbox.daos.type.manager.doctemplate;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.type.doc.DocTemplate; 

@Primary
@Repository
public class DocTemplateDaoImpl implements DocTemplateDao {
  private DocTemplateMapper docTemplateMapper;

  public DocTemplateDaoImpl(DocTemplateMapper docTemplateMapper) {
    this.docTemplateMapper = docTemplateMapper;
  }

  @Override
  public List<DocTemplate> selectAll() {
    return docTemplateMapper.selectAll();
  }

  @Override
  public Optional<DocTemplate> selectOneById(String docTemplateId) {
    return docTemplateMapper.selectOneById(docTemplateId);
  }

  @Override
  public Optional<DocTemplate> selectOneByTemplateType(String templateType) {
    return docTemplateMapper.selectOneByTemplateType(templateType);
  }
  
  @Override
  public List<DocTemplate> selectTemplates(String comOrgId, String delStatus) {
    return docTemplateMapper.selectTemplates(comOrgId, delStatus);
  }
  

}
