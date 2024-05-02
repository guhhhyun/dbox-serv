package com.dongkuksystems.dbox.daos.type.folder;

import java.util.List;
import java.util.Optional;

import com.dongkuksystems.dbox.models.dto.type.data.DataCreatorDto;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.dto.type.folder.FolderDescendantDto;
import com.dongkuksystems.dbox.models.dto.type.folder.FolderDetailDto;
import com.dongkuksystems.dbox.models.dto.type.folder.FolderFilterDto;
import com.dongkuksystems.dbox.models.type.folder.Folder;

@Primary
@Repository
public class FolderDaoImpl implements FolderDao {
    private FolderMapper folderMapper;

    public FolderDaoImpl(FolderMapper folderMapper) {
        this.folderMapper = folderMapper;
    }

    @Override
    public List<Folder> selectList(FolderFilterDto folderFilterDto) {
        return folderMapper.selectList(folderFilterDto);
    }

    @Override
    public List<Folder> selectListByPrCode(String prCode) {
        return folderMapper.selectListByPrCode(prCode);
    }

    @Override
    public List<Folder> selectAuthorizedList(FolderFilterDto folderFilterDto, String userId, String orgId) {
        return folderMapper.selectAuthorizedList(folderFilterDto, userId, orgId);
    }

    @Override
    public Optional<Folder> selectOne(String objectId) {
        return folderMapper.selectOne(objectId);
    }

    @Override
    public Optional<Folder> selectDetailOne(String objectId, String userId) {
        return folderMapper.selectDetailOne(objectId, userId);
    }

    @Override
    public List<Folder> selectFolderPaths(String objectId) {
        return folderMapper.selectFolderPaths(objectId);
    }

    @Override
    public boolean selectAncestorHasFolType(String objectId, String folType) {
        return folderMapper.selectAncestorHasFolType(objectId, folType);
    }

    @Override
    public Integer selectAncestorHasLock(String objectId) {
        return folderMapper.selectAncestorHasLock(objectId);
    }

    @Override
    public List<FolderDescendantDto> selectDescendants(String rObjectId, String userId, boolean cutUnder) {
        return folderMapper.selectDescendants(rObjectId, userId, cutUnder);
    }
    
    @Override
    public List<FolderDescendantDto> selectDescendantsAll(String rObjectId) {
      return folderMapper.selectDescendantsAll(rObjectId);
    }

    @Override
    public List<FolderDescendantDto> selectListDescendants(String rObjectId, String userId) {
        return folderMapper.selectListDescendants(rObjectId, userId);
    }

    @Override
    public List<Folder> selectShareFolderList(FolderFilterDto folderFilterDto, String userId, boolean checkChildren) {
        return folderMapper.selectShareFolderList(folderFilterDto, userId, checkChildren);
    }

    @Override
    public List<Folder> selectAuthorizedSearchList(FolderFilterDto folderFilterDto, String userId, String orgId, String searchName, String dataId, String dataCabinetCode) {
        return folderMapper.selectAuthorizedSearchList(folderFilterDto, userId, orgId, searchName, dataId, dataCabinetCode);
    }

    @Override
    public List<DataCreatorDto> selectFolderCreators(String dataId) {
        return folderMapper.selectFolderCreators(dataId);
    }

    @Override
    public List<String> selectSearchList(String dataId) {
      return folderMapper.selectSearchList(dataId);
    }

    @Override
    public String selectDsearchFullPath(String folId, String folderType, String dataCabinetCode) {
      return folderMapper.selectDsearchFullPath(folId,folderType,dataCabinetCode);
    }

}
