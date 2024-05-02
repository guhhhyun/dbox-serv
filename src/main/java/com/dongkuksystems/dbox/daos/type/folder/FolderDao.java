package com.dongkuksystems.dbox.daos.type.folder;

import java.util.List;
import java.util.Optional;

import com.dongkuksystems.dbox.models.dto.type.data.DataCreatorDto;
import com.dongkuksystems.dbox.models.dto.type.folder.FolderDescendantDto;
import com.dongkuksystems.dbox.models.dto.type.folder.FolderDetailDto;
import com.dongkuksystems.dbox.models.dto.type.folder.FolderFilterDto;
import com.dongkuksystems.dbox.models.type.folder.Folder;

public interface FolderDao {
	public List<Folder> selectList(FolderFilterDto folderFilterDto);
	public List<Folder> selectListByPrCode(String prCode);
	public List<Folder> selectAuthorizedList(FolderFilterDto folderFilterDto, String userId, String orgId);
	public Optional<Folder> selectOne(String objectId);
	public Optional<Folder> selectDetailOne(String objectId, String userId);
	public List<Folder> selectFolderPaths(String objectId);
	public boolean selectAncestorHasFolType(String objectId, String folType);
	public Integer selectAncestorHasLock(String objectId);
	public List<FolderDescendantDto> selectDescendants(String rObjectId, String userId, boolean cutUnder);
	public List<FolderDescendantDto> selectDescendantsAll(String rObjectId);
	public List<FolderDescendantDto> selectListDescendants(String rObjectId, String userId);
	public List<Folder> selectShareFolderList(FolderFilterDto folderFilterDto, String userId, boolean checkChildren);
	public List<Folder> selectAuthorizedSearchList(FolderFilterDto folderFilterDto, String userId, String orgId, String searchName, String dataId, String dataCabinetCode);
	List<DataCreatorDto> selectFolderCreators(String dataId);
    List<String> selectSearchList(String dataId);
    public String selectDsearchFullPath(String folId, String folderType, String dataCabinetCode);
}
