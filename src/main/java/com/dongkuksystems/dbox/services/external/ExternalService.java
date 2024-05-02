package com.dongkuksystems.dbox.services.external;

import com.documentum.fc.client.IDfSession;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.etc.CustomInputStreamResource;
import com.dongkuksystems.dbox.models.dto.type.agree.DboxAttaDocDto;
import com.dongkuksystems.dbox.models.dto.type.agree.RegistAgreeDto;
import com.dongkuksystems.dbox.models.dto.type.agree.WfDocDto;
import com.dongkuksystems.dbox.models.dto.type.upload.AttachedFile;

public interface ExternalService {
  CustomInputStreamResource createExternalLinkFile(String docId) throws Exception;
  String createExternalLinkFileByFind( UserSession usersession, String docId) throws Exception;
  
  String registerAgree(UserSession userSession, RegistAgreeDto dto) throws Exception;  //동의서 전송(objectId값이 넘어오면 update, 없으면 insert)
  
  String createWfDoc(UserSession userSession, IDfSession idfSess, String path, String socialPerId,   WfDocDto dto, AttachedFile aFile, String folderName) throws Exception; //전자결재 첨부파일 등록
  String updateWfDoc(UserSession userSession, IDfSession idfSess, String path, String socialPerId,   WfDocDto dto, AttachedFile aFile, String folderName) throws Exception; //전자결재 첨부파일 d'box 파일정보 변경
  
  String removeWfDoc(UserSession userSession, IDfSession idfSess, String keyString, String keyGubun, String attachType, String approvalId, String userId) throws Exception; //전자결재 첨부파일 삭제, keyGubun( "F": 첨부파일만 삭제될 때, "C" 첨부파일들을 가진 컨텐츠를 삭제하는 경우 처리)
  
  String updateBandWfDoc(UserSession userSession, IDfSession idfSess,  DboxAttaDocDto dto) throws Exception ; //dbox 파일을 첨부한데서 api로 권한을 추가할 것을 요청할 때 
	  
  String makeDWVFolder(IDfSession idf_Sess, String cabinetCode, String folderType, String folderName, String yearFolName, String userId) throws Exception;

  CustomInputStreamResource downloadInfsInstaller() throws Exception;
	String makeSynapViewerUrl(String dataId, String token, boolean isUDocKey) throws Exception;
  String makeDboxUrl(String dataId) throws Exception;
}
