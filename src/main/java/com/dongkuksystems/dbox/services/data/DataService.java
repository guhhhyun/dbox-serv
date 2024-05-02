package com.dongkuksystems.dbox.services.data;

import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfSession;
import com.dongkuksystems.dbox.errors.NotFoundException;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.auth.HamInfoResult;
import com.dongkuksystems.dbox.models.dto.type.data.*;
import com.dongkuksystems.dbox.models.dto.type.doc.DocDescendantDto;
import com.dongkuksystems.dbox.models.dto.type.doc.DocFilterDto;
import com.dongkuksystems.dbox.models.dto.type.doc.DocLinkListDto;
import com.dongkuksystems.dbox.models.dto.type.doc.DocVersionListDto;
import com.dongkuksystems.dbox.models.dto.type.folder.FolderLockDto;
import com.dongkuksystems.dbox.models.dto.type.manager.managerconfig.EnhancedUser;
import com.dongkuksystems.dbox.models.dto.type.upload.AttachedKUploadFile;
import com.dongkuksystems.dbox.models.dto.type.upload.UploadDocDto;
import com.dongkuksystems.dbox.models.type.user.UserPreset;
import com.dongkuksystems.dbox.models.type.user.UserPresetDetail;

public interface DataService {
	DataDetailDto getDataOne(UserSession userSession, String dataId, boolean isUDocKey,boolean isMobile) throws Exception;
	List<DataDetailDto> getDataChildren(
	    UserSession userSession, String dataId, String hamType, String folderType, String level, boolean withDoc, boolean checkHasChildren, boolean isMobile) throws Exception;
	List<DocDescendantDto> getDataDescendants(UserSession userSession, String dataId, boolean isMobile, boolean download) throws Exception;
	String registData(UserSession userSession, RegistDataDto registDataDto, IDfSession idfSess, HamInfoResult hamInfo, String userIp, String userType, List<UserPresetDetail> userPresetList) throws Exception;
	List<DataDetailDto> getDataPaths(UserSession userSession, String dataId, boolean isMobile) throws Exception;
	String getDataFullPaths(String folderId, String folderType, String deptCode);
	Object downloadData(UserSession userSession, String dataId, String versionId, String browser, boolean isMobile, boolean noEncrypt, boolean isViewer, String approveId, String syspath, String ip) throws Exception;
	Object updateData(UserSession userSession, String dataId, String versionId, DataUpdateReqDto dataUpdateReqDto, String browser, boolean isMobile, boolean isLock, boolean useUDocKey, boolean isMultipleAuth, String ip) throws Exception;
	Object updateDataForBatch(UserSession userSession, String versionId, DataUpdateBatchReqDto dataUpdateBatchReqDto, String browser, boolean isMobile, boolean isLock, String ip) throws Exception;
	
	Map<String, Object> isDataLock(UserSession userSession, String dataId, String dataType, boolean hasWAuth, String sOpenContent) throws NotFoundException, Exception;
	boolean checkDocsDuple(DocFilterDto docFilterDto);
    
	String getLockFolderData(UserSession userSession, String dataId) throws Exception;
	String patchUnLockFolder(String dataId, UserSession userSession) throws Exception;
	String patchDocClosed(String dataId, UserSession userSession,String ip)throws Exception;
	String patchDocClosedByAttach(String dataId, UserSession userSession,String ip)throws Exception;	// Agent 에서 첨부시 권한조건 없이 Close 처리
	String patchDocUnClosed(String dataId, String docReason, UserSession userSession,String ip)throws Exception;
	List<DataCreatorDto> getDataCreators(UserSession userSession, String dataType, String dataId);
	List<DocVersionListDto> getDataVersionListPaths(UserSession userSession, String dataId, boolean isMobile)throws Exception;
	String postDocVersion(UserSession userSession, String dataId, boolean docVersionChck ,String ip) throws Exception;
	String uploadDoc(UserSession userSession, UploadDocDto dto, AttachedKUploadFile aFile, String userIp, String userType) throws Exception;
	List<DocLinkListDto> getDataLinkListPaths(UserSession userSession, String dataId, boolean isMobile) throws Exception;
	List<DataDetailDto> getDataList(String searchName, String dataId, UserSession userSession, String ip,
            String hamSearchType, String folderType, boolean isMobile, String dataCabinetCode) throws Exception;
	List<DataDetailDto>getDataDsearchList(String searchName, String dataId, UserSession userSession, String deptCode, String folderCode, String folderType) throws Exception;
	List<Map<String, String>> getDocsEApproval(String dataId);
  String unlockDataOne(String dataId, UserSession userSession, String ip) throws Exception;
}


