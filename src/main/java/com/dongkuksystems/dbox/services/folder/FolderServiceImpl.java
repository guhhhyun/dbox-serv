package com.dongkuksystems.dbox.services.folder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.dongkuksystems.dbox.models.dto.type.data.DataCreatorDto;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.dongkuksystems.dbox.constants.DboxObjectType;
import com.dongkuksystems.dbox.constants.FolderType;
import com.dongkuksystems.dbox.constants.GrantedLevels;
import com.dongkuksystems.dbox.daos.type.auth.base.AuthBaseDao;
import com.dongkuksystems.dbox.daos.type.auth.share.AuthShareDao;
import com.dongkuksystems.dbox.daos.type.folder.FolderDao;
import com.dongkuksystems.dbox.errors.ForbiddenException;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.data.DataDetailDto;
import com.dongkuksystems.dbox.models.dto.type.folder.FolderDescendantDto;
import com.dongkuksystems.dbox.models.dto.type.folder.FolderDetailDto;
import com.dongkuksystems.dbox.models.dto.type.folder.FolderFilterDto;
import com.dongkuksystems.dbox.models.dto.type.folder.RegistFolderDto;
import com.dongkuksystems.dbox.models.type.auth.AuthBase;
import com.dongkuksystems.dbox.models.type.auth.AuthShare;
import com.dongkuksystems.dbox.models.type.folder.Folder;
import com.dongkuksystems.dbox.services.AbstractCommonService;
import com.dongkuksystems.dbox.services.cache.CacheService;
import com.dongkuksystems.dbox.services.code.CodeService;

@Service
public class FolderServiceImpl extends AbstractCommonService implements FolderService {
  private final CacheService cacheService;
  private final CodeService codeService;
  private final FolderDao folderDao;
  private final AuthBaseDao authBaseDao;
  private final AuthShareDao authShareDao;
  
  public FolderServiceImpl(CacheService cacheService, CodeService codeService, FolderDao folderDao, AuthBaseDao authBaseDao, AuthShareDao authShareDao) {
    this.cacheService = cacheService;
    this.codeService = codeService;
    this.folderDao = folderDao;
    this.authBaseDao = authBaseDao;
    this.authShareDao = authShareDao;
  }

  @Override
  public String createFolder(UserSession userSession, RegistFolderDto dto) throws Exception {
    IDfSession idfSession = this.getIdfSession(userSession);
    IDfPersistentObject idfFolder = RegistFolderDto.toIDfPersistentObject(idfSession, dto);
    idfFolder.save();
    return idfFolder.getObjectId().getId();
  }
  
  @Override
  public String createFolder(IDfSession idfSession, RegistFolderDto dto) throws Exception {
    IDfPersistentObject idfFolder = RegistFolderDto.toIDfPersistentObject(idfSession, dto);
    idfFolder.save();
    return idfFolder.getObjectId().getId();
  }

  @Override
  public List<Folder> selectFolderChildren(FolderFilterDto dto) throws Exception {
    return folderDao.selectList(dto);
  }
  
  @Override
  public List<FolderDetailDto> selectAuthorizedDetailChildren(FolderFilterDto dto, String userId, String orgId) throws Exception {
		// 보안등급 코드 조회
		Map<String, String> secLevelMap = codeService.getSecLevelMap();
		// 폴더상태 코드 조회
		Map<String, String> folStatusMap = codeService.getFolStatusMap();

		final ModelMapper modelMapper = getModelMapper();
		
		List<Folder> folderList = folderDao.selectAuthorizedList(dto, userId, orgId);
		List<FolderDetailDto> folderDetailDtoList = folderList.stream()
			.map(item -> {
			FolderDetailDto folderDto = modelMapper.map(item, FolderDetailDto.class);
			folderDto.setSecLevelName(secLevelMap.get(folderDto.getUSecLevel()));
			folderDto.setFolStatusName(folStatusMap.get(folderDto.getUFolStatus()));
			
			return folderDto;
		}).collect(Collectors.toList());
		
		return folderDetailDtoList;
  }

  @Override
  public Optional<Folder> selectOne(String objectId) throws Exception {
    return folderDao.selectOne(objectId);
  }

  @Override
  public Optional<Folder> selectDetailOne(String objectId, String userId) throws Exception {
  	Optional<Folder> optFolder = folderDao.selectDetailOne(objectId, userId);
  	if (optFolder.isPresent()) {
  		Folder folder = optFolder.get();
  		
  		switch (FolderType.findByValue(folder.getUFolType())) {
  			case DFO:
  			case PFO:
  			case RFO:
        	// 검색(B) 이상의 권한을 가졌는지 여부
        	boolean authorized = GrantedLevels.findByLabel(folder.getMaxPermitType()) >=  GrantedLevels.BROWSE.getLevel();
        	
        	if (authorized) {
            // 폴더에 해당하는 전체 권한 리스트 조회
            List<AuthBase> authBaseList = authBaseDao.selectDetailList(objectId);
            optFolder.get().setAuthBases(authBaseList);
            
            // 폴더에 해당하는 전체 공유/협업 리스트 조회
            List<AuthShare> authShareList = authShareDao.selectDetailList(objectId);
            optFolder.get().setAuthShares(authShareList);
        	} else {
        		throw new ForbiddenException("폴더에 대한 조회권한이 없습니다.");
        	}
  				break;
				default:
	  			optFolder.get().setAuthBases(new ArrayList<AuthBase>());
  		}
  	}

  	return optFolder;
  }
  
  @Override
  public List<Folder> getFolderPaths(String objectId) throws Exception {
    return folderDao.selectFolderPaths(objectId);
  }
  
  @Override
  public boolean selectAncestorHasFolType(String objectId, FolderType folderType) throws Exception {
  	return folderDao.selectAncestorHasFolType(objectId, folderType.getValue());
  }
  
  @Override
  public Integer selectAncestorHasLock(String objectId) throws Exception {
    return folderDao.selectAncestorHasLock(objectId);
  }
  
  @Override
  public List<FolderDescendantDto> selectDescendants(String rObjectId, String userId, boolean cutUnder) {
  	return folderDao.selectDescendants(rObjectId, userId, cutUnder);
  }

  @Override
  public List<DataDetailDto> selectShareFolderList(FolderFilterDto dto, String userId) throws Exception {
    // 보안등급 코드 조회
    Map<String, String> secLevelMap = codeService.getSecLevelMap();
    // 폴더상태 코드 조회
    Map<String, String> folStatusMap = codeService.getFolStatusMap();

    final ModelMapper modelMapper = getModelMapper();
    
    List<Folder> folderList = folderDao.selectShareFolderList(dto, userId, false);  // 속도 때문에 하위 조회 안하도록 함
    List<DataDetailDto> dataDetailDtoList = folderList.stream()
        .map(item -> {
          FolderDetailDto folderDto = modelMapper.map(item, FolderDetailDto.class);
          folderDto.setSecLevelName(secLevelMap.get(folderDto.getUSecLevel()));
          folderDto.setFolStatusName(folStatusMap.get(folderDto.getUFolStatus()));

          // DataDetailDto로 변환하여 반환
        	DataDetailDto dataDetailDto = DataDetailDto.builder()
        			.dataType(DboxObjectType.FOLDER.getValue())
        			.folder(folderDto)
        			.build();
        	
          return dataDetailDto;
        }).collect(Collectors.toList());
    
    return dataDetailDtoList;
  }

	@Override
	public List<FolderDescendantDto> selectListDescendants(String rObjectId, String userId) {
		return folderDao.selectListDescendants(rObjectId, userId);
	}

	
	  @Override
	  public List<FolderDetailDto> selectAuthorizedDetailSearchList(FolderFilterDto dto, String userId, String orgId, String searchName, String dataId, String dataCabinetCode) throws Exception {
			// 보안등급 코드 조회
			Map<String, String> secLevelMap = codeService.getSecLevelMap();
			// 폴더상태 코드 조회
			Map<String, String> folStatusMap = codeService.getFolStatusMap();

			final ModelMapper modelMapper = getModelMapper();
			
			List<Folder> folderList = folderDao.selectAuthorizedSearchList(dto, userId, orgId, searchName, dataId, dataCabinetCode);
			
			List<FolderDetailDto> folderDetailDtoList = folderList.stream()
				.map(item -> {
				FolderDetailDto folderDto = modelMapper.map(item, FolderDetailDto.class);
				folderDto.setSecLevelName(secLevelMap.get(folderDto.getUSecLevel()));
				folderDto.setFolStatusName(folStatusMap.get(folderDto.getUFolStatus()));
				return folderDto;
			}).collect(Collectors.toList());
			
			return folderDetailDtoList;
	  }
    

    @Override
    public List<DataCreatorDto> selectFolderCreators(String dataId) {
        return folderDao.selectFolderCreators(dataId);

    }

}
