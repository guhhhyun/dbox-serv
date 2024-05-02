package com.dongkuksystems.dbox.services.log;

import java.util.List;

import org.springframework.stereotype.Service;

import com.dongkuksystems.dbox.errors.NotFoundException;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.etc.LogDebugDto;
import com.dongkuksystems.dbox.models.table.log.LogDoc;
import com.dongkuksystems.dbox.models.type.doc.Doc;
import com.dongkuksystems.dbox.services.AbstractCommonService;
import com.dongkuksystems.dbox.services.doc.DocService;
import com.dongkuksystems.dbox.services.user.UserService;

@Service
public class LogServiceImpl extends AbstractCommonService implements LogService {
  private final UserService userService;
  private final DocService docService;
  

  public LogServiceImpl(UserService userService, DocService docService) {
    this.userService = userService;
    this.docService = docService;
  }

  @Override
  public void insertDocLog(UserSession userSession, String objectId, String jobCode, String jobGubun, String ip)
      throws Exception {
    Doc doc = docService.selectOne(objectId).orElseThrow(() -> new NotFoundException(Doc.class, objectId));
    String docDept = doc.getUDeptCode();
    String docComId = doc.getUComCode();
    String groupCode = userSession.getUser().getMgr().getGroupComCode();
    String comCode = userSession.getUser().getMgr().getCompanyComCode();
    List<String> depts = userSession.getUser().getMgr().getCompanyDeptCode();
    String jobUserType = null;
//    if (depts != null) {
//      for (String dept : depts) {
//        if (docDept.equals(dept)) {
//          jobUserType = "D";
//          break;
//        }
//      }
//    }
//    jobUserType = comCode != null ? (comCode.equals(docComId)? "C" : jobUserType) : jobUserType;
//    jobUserType = groupCode != null ? "G" : jobUserType;
    LogDoc logDoc = LogDoc.builder()
              .uJobCode(jobCode)
              .uDocId(doc.getRObjectId())
              .uDocKey(doc.getUDocKey())
              .uDocName(doc.getTitle().replaceAll("'", "''"))
//              .uDocVersion()
              .uFileSize(Long.valueOf(doc.getRContentSize()))
              .uOwnDeptcode(doc.getUDeptCode())
              .uActDeptCode(userSession.getUser().getOrgId())
              .uJobUser(userSession.getUser().getUserId())
              .uJobUserType(jobUserType==null ? "P" : jobUserType) //작업자구분(P:개인, D:부서관리자, C:사별관리자, G:그룹관리자, S:시스템)
              .uDocStatus(doc.getUDocStatus())
              .uSecLevel(doc.getUSecLevel())
              .uCabinetCode(doc.getUCabinetCode())
              .uJobGubun(jobGubun)
              .uUserIp(ip)
//              .uAttachSystem("")
              .build();
          insertLog(logDoc);
  }
  
  @Override
  public void insertDebugLog(LogDebugDto logDebugDto) {
    super.insertDebugLog(logDebugDto);
  }
}
