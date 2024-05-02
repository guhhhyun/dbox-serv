package com.dongkuksystems.dbox.services.manager.template;

import java.util.List;

import org.springframework.stereotype.Service;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;
import com.dongkuksystems.dbox.daos.type.manager.doctemplate.DocTemplateDao;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.upload.AttachedFile;
import com.dongkuksystems.dbox.models.type.doc.DocTemplate;
import com.dongkuksystems.dbox.services.AbstractCommonService;
import com.dongkuksystems.dbox.services.cache.CacheService;

@Service
public class TemplateServiceImpl extends AbstractCommonService implements TemplateService {

  private final CacheService cacheService;
  private final DocTemplateDao docTemplateDao;

  public TemplateServiceImpl(CacheService cacheService, DocTemplateDao docTemplateDao) {
    this.cacheService = cacheService;
    this.docTemplateDao = docTemplateDao;
  }

  @Override
  public String createTemplate(UserSession userSession, AttachedFile aFile) throws Exception {
    IDfSession idfSession = null;
    IDfDocument idfDoc = null;
//    try {
////      userSession.setDUserId("dmadmin");
//      idfSession = this.getIdfSession(userSession);
//      aFile.setDcmtContentType(cacheService.selectDmFormats());
////      idfDoc = UploadDocDto.toIDfDocument(idfSession, dto, aFile);
//      String s_DCTMFolderId   = "/Templates/DBox";
//      String s_CabinetCode  = "0c0004d28000012f";
//      String s_SecLevel     = "T";
// 
////    u_com_code                  CHAR(100) (SET LABEL_TEXT='회사코드'),
////    u_template_type             CHAR(100) (SET LABEL_TEXT='템플릿구분'),
////    u_template_name             CHAR(100) (SET LABEL_TEXT='템플릿명'),
////    u_sort_order                INTEGER (SET LABEL_TEXT='정렬순서')
//      idfDoc = (IDfDocument) idfSession.newObject("edms_doc_template");
//      // 문서명
//      idfDoc.setObjectName(aFile.getOriginalFileName());
//      // 포맷
//      idfDoc.setContentType(aFile.getDContentType());
//      // 소유자 지정 : Docbase Owner로 지정 
//      idfDoc.setOwnerName(idfSession.getDocbaseOwnerName());
//      // 템플릿 ACL 적용
//      idfDoc.setACLDomain(idfSession.getDocbaseOwnerName());
//      idfDoc.setACLName("all_write");
//      // 파일 set
//      idfDoc.setFile(aFile.getTmpFileName());
//      // DCTM 폴더(화면에 보이는 부서 폴더 아님)
//      idfDoc.link(s_DCTMFolderId);
//      // 업무 속성 지정
////      idfDoc.setString("u_creation_date", (new DfTime()).toString());
//      idfDoc.setString("u_com_code", EntCode.DKG.name()); // 문서함코드
////      idfDoc.setString("u_doc_key", "" + idfDoc.getChronicleId()); // 문서 키
//      idfDoc.setString("u_template_type", "PPT_HORIZONTAL"); // 부서폴더
//      idfDoc.setString("u_sort_order", "1"); 
//      
//      idfDoc.save();
//    } catch (Exception e) {
//      throw e;
//    } finally {
//      aFile.deleteFile();
//      if (idfSession != null && idfSession.isConnected()) {
//        idfSession.disconnect();
//      }
//    }
    return idfDoc.getObjectId().getId();
  }

  @Override
  public List<DocTemplate> selectTemplates(String comOrgId, String delStatus) {
    return docTemplateDao.selectTemplates(comOrgId, delStatus);
  }

  @Override
  public String updateTemplate(UserSession userSession, AttachedFile aFile, String rObjectId, String objectName,
      String templateCode) throws Exception {
    IDfSession idfSession = null;
    IDfDocument idfDoc = null;
    if (templateCode.equals("ppt")) {
      if ((aFile.getFileExtention().equals("pptx") || aFile.getFileExtention().equals("pptm")
          || aFile.getFileExtention().equals("ppt") || aFile.getFileExtention().equals("potx")
          || aFile.getFileExtention().equals("potm") || aFile.getFileExtention().equals("pot"))) {
        try {
          idfSession = this.getIdfSession(userSession);
          aFile.setDcmtContentType(cacheService.selectDmFormats());
          idfDoc = (IDfDocument) idfSession.getObject(new DfId(rObjectId));
          idfDoc.setString("object_name", objectName); // 파일명
          idfDoc.setString("a_content_type", aFile.getDContentType());
          idfDoc.setString("u_delete_status", "F"); // 삭제상태
          // 파일 set
          idfDoc.setFile(aFile.getTmpFileName());
          idfDoc.save();
        } catch (Exception e) {
          throw e;
        } finally {
          aFile.deleteFile();
          sessionRelease(userSession.getUser().getUserId(), idfSession);
        }
        return idfDoc.getObjectId().getId();
      } else {
        Exception e = new Exception("ppt파일 확장자 선택 오류");
        throw e;
      }
    }

    else if (templateCode.equals("word")) {
      if ((aFile.getFileExtention().equals("doc") || aFile.getFileExtention().equals("docm")
          || aFile.getFileExtention().equals("docx") || aFile.getFileExtention().equals("dot")
          || aFile.getFileExtention().equals("dotx"))) {
        try {
          idfSession = this.getIdfSession(userSession);
          aFile.setDcmtContentType(cacheService.selectDmFormats());
          idfDoc = (IDfDocument) idfSession.getObject(new DfId(rObjectId));
          idfDoc.setString("object_name", objectName); // 파일명
          idfDoc.setString("a_content_type", aFile.getDContentType());
          idfDoc.setString("u_delete_status", "F"); // 삭제상태
          // 파일 set
          idfDoc.setFile(aFile.getTmpFileName());
          idfDoc.save();
        } catch (Exception e) {
          throw e;
        } finally {
          aFile.deleteFile();
          sessionRelease(userSession.getUser().getUserId(), idfSession);
        }
        return idfDoc.getObjectId().getId();
      } else {
        Exception e = new Exception("word파일 확장자 선택 오류");
        throw e;
      }
    }
    return null;
  }

  @Override
  public String deleteTemplate(UserSession userSession, String rObjectId) throws Exception {
    IDfSession idfSession = null;
    IDfDocument idfDoc = null;
    try {
      idfSession = this.getIdfSession(userSession);
      idfDoc = (IDfDocument) idfSession.getObject(new DfId(rObjectId));
      idfDoc.setString("u_delete_status", "T"); // 삭제상태
      idfDoc.setString("object_name", "파일 없음");
      idfDoc.save();
    } catch (Exception e) {
      throw e;
    } finally {
      sessionRelease(userSession.getUser().getUserId(), idfSession);
    }
    return rObjectId;
  }

  @Override
  public String templateNameUpdate(UserSession userSession, String rObjectId, String objectName) throws Exception {
    IDfSession idfSession = null;
    IDfDocument idfDoc = null;
    try {
      idfSession = this.getIdfSession(userSession);
      idfDoc = (IDfDocument) idfSession.getObject(new DfId(rObjectId));
      idfDoc.setString("object_name", objectName); // 파일명(사용자가 입력)
      idfDoc.save();
    } catch (Exception e) {
      throw e;
    } finally {
      sessionRelease(userSession.getUser().getUserId(), idfSession);
    }
    return idfDoc.getObjectId().getId();
  }
}