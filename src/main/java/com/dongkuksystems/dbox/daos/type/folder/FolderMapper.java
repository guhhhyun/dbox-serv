package com.dongkuksystems.dbox.daos.type.folder;

import java.util.List;
import java.util.Optional;

import com.dongkuksystems.dbox.models.dto.type.data.DataCreatorDto;
import org.apache.ibatis.annotations.Param;

import com.documentum.fc.client.IDfSession;
import com.dongkuksystems.dbox.models.dto.type.folder.FolderDescendantDto;
import com.dongkuksystems.dbox.models.dto.type.folder.FolderDetailDto;
import com.dongkuksystems.dbox.models.dto.type.folder.FolderFilterDto;
import com.dongkuksystems.dbox.models.type.folder.Folder;

public interface FolderMapper {
	public List<Folder> selectList(@Param("folder") FolderFilterDto folderFilterDto);
	public List<Folder> selectAuthorizedList(@Param("folder") FolderFilterDto folderFilterDto, @Param("userId") String userId, @Param("orgId") String orgId);
	public Optional<Folder> selectOne(@Param("objectId") String objectId);
	public Optional<Folder> selectDetailOne(@Param("objectId") String objectId, @Param("userId") String userId);
	public List<Folder> selectFolderPaths(@Param("objectId") String objectId);
	public boolean selectAncestorHasFolType(@Param("objectId") String objectId, @Param("folType") String folType);
	public Integer selectAncestorHasLock(@Param("objectId") String objectId);
	public List<FolderDescendantDto> selectDescendants(@Param("rObjectId") String rObjectId, @Param("userId") String userId, @Param("cutUnder") boolean cutUnder);
	public List<FolderDescendantDto> selectListDescendants(@Param("rObjectId") String rObjectId, @Param("userId") String userId);
	public List<Folder> selectShareFolderList(@Param("folder") FolderFilterDto folderFilterDto, @Param("userId") String userId, @Param("checkChildren") boolean checkChildren);
	public List<FolderDescendantDto> selectDataListSearch(@Param("rObjectId")String rObjectId,@Param("userId")String userId, @Param("searchName") String searchName);
	public List<FolderDetailDto> selectListSearch(@Param("rObjectId")String rObjectId);
	public List<Folder> selectAuthorizedSearchList(@Param("folder") FolderFilterDto folderFilterDto, @Param("userId") String userId, @Param("orgId") String orgId, @Param("searchName")  String searchName, @Param("dataId")String dataId, @Param("dataCabinetCode")String dataCabinetCode);
	public String selectDboxSearchFolderPath(@Param("dataId")String dataId);
  	public List<Folder> selectListByPrCode(@Param("uPrCode") String prCode);
    List<DataCreatorDto> selectFolderCreators(String dataId);
  	public List<FolderDescendantDto> selectDescendantsAll(String rObjectId);
    public List<String> selectSearchList(@Param("dataId")String dataId);
    public String selectDsearchFullPath(@Param("folId")String folId, @Param("folderType")String folderType, @Param("dataCabinetCode")String dataCabinetCode);
}
