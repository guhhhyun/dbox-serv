package com.dongkuksystems.dbox.services.manager.template;

import java.util.List;

import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.upload.AttachedFile;
import com.dongkuksystems.dbox.models.type.doc.DocTemplate;

public interface TemplateService {
  String createTemplate(UserSession userSession, AttachedFile aFile) throws Exception;
  List<DocTemplate> selectTemplates(String comOrgId, String delStatus) throws Exception;
  String updateTemplate(UserSession userSession, AttachedFile aFile, String rObjectId, String objectName, String templateCode) throws Exception;
  String deleteTemplate(UserSession userSession, String rObjectId) throws Exception;
  String templateNameUpdate(UserSession userSession, String rObjectId, String objectName) throws Exception; 

}
