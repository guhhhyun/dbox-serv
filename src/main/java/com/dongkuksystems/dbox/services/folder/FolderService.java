package com.dongkuksystems.dbox.services.folder;

import java.util.List;
import java.util.Optional;

import com.documentum.fc.client.IDfSession;
import com.dongkuksystems.dbox.constants.FolderType;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.data.DataCreatorDto;
import com.dongkuksystems.dbox.models.dto.type.data.DataDetailDto;
import com.dongkuksystems.dbox.models.dto.type.folder.FolderDescendantDto;
import com.dongkuksystems.dbox.models.dto.type.folder.FolderDetailDto;
import com.dongkuksystems.dbox.models.dto.type.folder.FolderFilterDto;
import com.dongkuksystems.dbox.models.dto.type.folder.FolderLockDto;
import com.dongkuksystems.dbox.models.dto.type.folder.RegistFolderDto;
import com.dongkuksystems.dbox.models.type.folder.Folder;

public interface FolderService {
  String createFolder(UserSession userSession, RegistFolderDto dto) throws Exception;
  String createFolder(IDfSession idfSession, RegistFolderDto dto) throws Exception;
  List<Folder> selectFolderChildren(FolderFilterDto dto) throws Exception;

  /**
   * 권한있는 폴더 하위 조회
   *
   * @param dto 검색 조건
   * @param userId 사용자 아이디
   * @param orgId 사용자 부서코드 (하위 프로젝트/투자 또는 연구과제 여부 파악용, null일 경우 조회 안함)
   * @return 폴더 상세 리스트
   */
  List<FolderDetailDto> selectAuthorizedDetailChildren(FolderFilterDto dto, String userId, String orgId) throws Exception;
  Optional<Folder> selectOne(String objectId) throws Exception;
  Optional<Folder> selectDetailOne(String objectId, String userId) throws Exception;
  List<Folder> getFolderPaths(String objectId) throws Exception;
  boolean selectAncestorHasFolType(String objectId, FolderType folderType) throws Exception;
  Integer selectAncestorHasLock(String objectId) throws Exception;
  public List<FolderDescendantDto> selectDescendants(String rObjectId, String userId, boolean cutUnder);
  List<DataDetailDto> selectShareFolderList(FolderFilterDto dto, String userId) throws Exception;
  List<FolderDescendantDto> selectListDescendants(String rObjectId, String userId);
  List<FolderDetailDto> selectAuthorizedDetailSearchList(FolderFilterDto dto, String userId, String orgId, String searchName, String dataId, String dataCabinetCode) throws Exception;
  List<DataCreatorDto> selectFolderCreators(String dataId);
}
