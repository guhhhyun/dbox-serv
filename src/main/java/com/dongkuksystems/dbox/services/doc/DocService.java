package com.dongkuksystems.dbox.services.doc;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfSession;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.etc.CustomInputStreamResource;
import com.dongkuksystems.dbox.models.dto.type.auth.HamInfoResult;
import com.dongkuksystems.dbox.models.dto.type.data.DataCreatorDto;
import com.dongkuksystems.dbox.models.dto.type.data.DataViewCheckoutDto;
import com.dongkuksystems.dbox.models.dto.type.doc.DocDetailDto;
import com.dongkuksystems.dbox.models.dto.type.doc.DocFilterDto;
import com.dongkuksystems.dbox.models.dto.type.doc.DocLinkListDto;
import com.dongkuksystems.dbox.models.dto.type.doc.DocVersionListDto;
import com.dongkuksystems.dbox.models.dto.type.folder.FolderDetailDto;
import com.dongkuksystems.dbox.models.dto.type.upload.AttachedFile;
import com.dongkuksystems.dbox.models.dto.type.upload.AttachedKUploadFile;
import com.dongkuksystems.dbox.models.dto.type.upload.UploadDocDto;
import com.dongkuksystems.dbox.models.type.doc.Doc;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Optional;

public interface DocService {
  String createDoc(UserSession userSession, UploadDocDto dto, AttachedFile aFile, String docType,  String secLevel, HamInfoResult hamInfo, IDfSession idfSession) throws Exception;
  String createDoc(UserSession userSession, UploadDocDto dto, AttachedFile aFile) throws Exception;
  String createDoc(String socialPerId, UploadDocDto dto, AttachedKUploadFile aFile) throws Exception;
  IDfDocument createDoc(UserSession userSession, UploadDocDto dto, AttachedKUploadFile aFile) throws Exception;
  IDfDocument createDoc(IDfSession idfSession, UploadDocDto dto, AttachedKUploadFile aFile) throws Exception;
  String versionUp(String objectId, ByteArrayOutputStream fileStream, UserSession userSession, String objectName) throws Exception;
  String versionUp(String objectId, String filePath, UserSession userSession, String objectName) throws Exception;
  String versionUp(String objectId, ByteArrayOutputStream fileStream, IDfSession idfSession, String objectName) throws Exception;
  String versionUp(String objectId, String filePath, IDfSession idfSession, String objectName) throws Exception;
  String versionUp(IDfDocument idfDoc, ByteArrayOutputStream fileStream, String objectName) throws Exception;
  String versionUp(IDfDocument idfDoc, String filePath, String objectName) throws Exception;
  IDfDocument overWrite(IDfSession idfSession, String objectId, AttachedKUploadFile aFile) throws Exception;
  void checkOut(String objectId, UserSession userSession) throws Exception;
  void checkOut(String objectId, IDfSession idfSession) throws Exception;
  void checkOut(IDfDocument idfDoc) throws Exception;
  String checkIn(String objectId, UserSession userSession) throws Exception;
  String checkIn(String objectId, IDfSession idfSession) throws Exception;
  String checkIn(IDfDocument idfDoc) throws Exception;

  CustomInputStreamResource downloadDoc(UserSession userSession, String docId) throws Exception;
  CustomInputStreamResource downloadDoc(IDfSession idfSession, String docId) throws Exception;
  CustomInputStreamResource downloadDocByFile(IDfSession idfSession, String docId) throws Exception;
  List<Doc> selectList(DocFilterDto dto) throws Exception;
  List<DocDetailDto> selectAuthorizedDetailList(DocFilterDto dto, String userId, int level) throws Exception;
  Optional<Doc> selectOne(String objectId) throws Exception;
  Optional<Doc> selectOne(String objectId, boolean isUDocKey) throws Exception;
  Optional<Doc> selectDetailOne(String objectId, String userId) throws Exception;

  List<Doc> selectListForFolderId(String folderId) throws Exception;
  List<Doc> selectAuthorizedListByFolIds(List<String> folIds, String userId, int level);
  List<Doc> selectDeleteAuthorizedListByFolIds(List<String> folIds, String userId, int level);
  List<Doc> selectListByFolIds(List<String> folIds);
  boolean checkDocsDuple(DocFilterDto docFilterDto);
  boolean isLocked(String objectId);
  DataViewCheckoutDto getViewCheck(UserSession userSession, String objId, String docKey, String sOpenContent, String approveId, String sSysId) throws Exception;
  DataViewCheckoutDto getCheckoutCheck(UserSession userSession, String objId, String docKey, String sOpenContent) throws Exception;
  List<String> selectDocChk(String value) throws Exception;
  List<DocVersionListDto> selectDocVersionList(String docKey) throws Exception;
  List<DocVersionListDto> selectDocImpVersionList(String docImpKey) throws Exception;
  List<DocLinkListDto> selectDocLinkList(String dataId) throws Exception;
  Optional<Doc> selectDetailOne(String dataId, boolean isUDocKey, String userId) throws Exception;
  List<DocDetailDto> selectAuthorizedsearchList(DocFilterDto docFilterDto, String userId, int level, String searchName,
		List<FolderDetailDto> folderDetailDtoList, String dataId, String dataCabinetCode) throws Exception;
  List<DocDetailDto> selectImpAuthorizedsearchList(DocFilterDto docFilterDto, String userId, int level, String searchName,
		List<FolderDetailDto> folderDetailDtoList, String dataId, String dataCabinetCode)throws Exception;

  List<DataCreatorDto> selectDocCreators(String dataId);
}
