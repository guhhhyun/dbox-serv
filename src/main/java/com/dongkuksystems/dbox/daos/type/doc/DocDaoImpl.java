package com.dongkuksystems.dbox.daos.type.doc;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.dto.type.data.DataCreatorDto;
import com.dongkuksystems.dbox.models.dto.type.doc.DocFilterDto;
import com.dongkuksystems.dbox.models.dto.type.doc.DocLinkDto;
import com.dongkuksystems.dbox.models.dto.type.doc.DocLinkListDto;
import com.dongkuksystems.dbox.models.dto.type.doc.DocPathDto;
import com.dongkuksystems.dbox.models.dto.type.doc.DocVersionListDto;
import com.dongkuksystems.dbox.models.type.doc.Doc;
import com.dongkuksystems.dbox.models.type.doc.DocRepeating;

@Primary
@Repository
public class DocDaoImpl implements DocDao {
	private DocMapper docMapper;

	public DocDaoImpl(DocMapper docMapper) {
		this.docMapper = docMapper;
	}
	
	@Override
	public Optional<Doc> selectOne(String rObjectId) {
	  return selectOne(rObjectId, false);
	}

	@Override
	public Optional<Doc> selectOne(String rObjectId, boolean isUDocKey) {
		return docMapper.selectOne(rObjectId, isUDocKey);
	}
	
	@Override
	public Optional<Doc> selectDetailOne(String rObjectId, String userId) {
	  return selectDetailOne(rObjectId, userId, false);
	}
	
	@Override
	public Optional<Doc> selectDetailOne(String rObjectId, String userId, boolean isUDocKey) {
		return docMapper.selectDetailOne(rObjectId, userId, isUDocKey);
	}
	
	@Override
	public List<DocRepeating> selRepeatingOne(String rObjectId) {
	  return docMapper.selRepeatingOne(rObjectId);
	}
	
	@Override
	public List<DocRepeating> selectRepeatingOne(String rObjectId, boolean isUDocKey) {
		return docMapper.selectRepeatingOne(rObjectId, isUDocKey);
	}

  @Override
	public List<Doc> selectList(DocFilterDto docFilterDto) {
		return docMapper.selectList(docFilterDto);
	}
	
	@Override
  public List<Doc> selectListByPrCode(String prCode) {
    return docMapper.selectListByPrCode(prCode);
  }

  @Override
  public List<Doc> selectAuthorizedDetailList(DocFilterDto docFilterDto, String userId, int level) {
		return docMapper.selectAuthorizedDetailList(docFilterDto, userId, level);
	}
	
	@Override
	public List<DocRepeating> selectRepeatingList(DocFilterDto docFilterDto) {
		return docMapper.selectRepeatingList(docFilterDto);
	}
	
	@Override
	public List<DocRepeating> selectAuthorizedRepeatingList(DocFilterDto docFilterDto, String userId, int level) {
		return docMapper.selectAuthorizedRepeatingList(docFilterDto, userId, level);
	}

	@Override
	public List<Doc> selectListForFolderId(String folderId) {
		return docMapper.selectListForFolderId(folderId);
	}
	
  @Override
  public List<Doc> recycleListForFolderId(String folderId) {
    return docMapper.recycleListForFolderId(folderId);
  }

  @Override
  public List<Doc> recycleListByFolIds(List<String> folIds) {
    return docMapper.recycleListByFolIds(folIds);
  }

  @Override
  public List<DocPathDto> selectDocPathList(DocFilterDto docFilterDto) {
    return docMapper.selectDocPathList(docFilterDto);
  }
  
  @Override
  public boolean selectDocDuple(DocFilterDto docFilterDto, List<String> docPaths) {
    return docMapper.selectDocDuple(docFilterDto, docPaths);
  }

  @Override
  public boolean selectLockStatus(String objectId) {
    return docMapper.selectLockStatus(objectId);
  }
  
  @Override
  public int selectOwnDeptCount(String uPjtType, String uPjtCode, List<String> deptCodes) {
	return docMapper.selectOwnDeptCount(uPjtType, uPjtCode, deptCodes);
  }
  
  @Override
  public int selectJoinDeptCount(String uPjtType, String uPjtCode, List<String> deptCodes) {
	return docMapper.selectOwnDeptCount(uPjtType, uPjtCode, deptCodes);
  }

  @Override
  public String selectDepthCodeForBelowOrgBox(String uCabinetCode) {
	return docMapper.selectDepthCodeForBelowOrgBox(uCabinetCode);
  }

	@Override
	public List<String> selectOrgIdByUserId(List<String> userIds) {
		return docMapper.selectOrgIdByUserId(userIds);
	}
	
	@Override
	public List<Doc> selectAuthorizedListByFolIds(List<String> folIds, String userId, int level) {
		return docMapper.selectAuthorizedListByFolIds(folIds, userId, level);
	}
	
	@Override
	public List<Doc> selectDeleteAuthorizedListByFolIds(List<String> folIds, String userId, int level) {
		return docMapper.selectDeleteAuthorizedListByFolIds(folIds, userId, level);
	}

	@Override
	public List<Doc> selectListByFolIds(List<String> folIds) {
		return docMapper.selectListByFolIds(folIds);
	}
	
	@Override
	public List<String> selectDocChk(String value) {
		// TODO Auto-generated method stub
	    return docMapper.selectDocChk(value);
	}


	@Override
	public List<DataCreatorDto> selectDocCreators(String dataId) {
		// TODO Auto-generated method stub
		return docMapper.selectDocCreators(dataId);
	}

	@Override
	public List<DocVersionListDto> selectDocVersionList(String docKey) {
		// TODO Auto-generated method stub
		return docMapper.selectDocVersionList(docKey);
	}

	@Override
	public List<DocVersionListDto> selectDocImpVersionList(String docImpKey) {
		// TODO Auto-generated method stub
		return docMapper.selectDocImpVersionList(docImpKey);
	}

	@Override
	public List<DocLinkListDto> selectDocLinkList(String dataId) {
		// TODO Auto-generated method stub
		return docMapper.selectDocLinkList(dataId);
	}

	@Override
	public List<Doc> selectAuthorizedSearchList(DocFilterDto docFilterDto, String userId, int level, String dataId, String searchName, String dataCabinetCode, String imwoncheck) {
		return docMapper.selectAuthorizedSearchList(docFilterDto, userId, level, dataId, searchName, dataCabinetCode, imwoncheck);
	}

	@Override
	public List<DocRepeating> selectAuthorizedRepeatingSearchList(DocFilterDto docFilterDto, String userId, int level,
			String searchName, String dataId, String dataCabinetCode, String imwoncheck) {
		return docMapper.selectAuthorizedRepeatingSearchList(docFilterDto, userId, level,searchName, dataId, dataCabinetCode, imwoncheck);
	}

	@Override
	public List<Doc> selectImpAuthorizedSearchList(DocFilterDto docFilterDto, String userId, int level, String dataId,
			String searchName, String dataCabinetCode) {
		return docMapper.selectImpAuthorizedSearchList(docFilterDto, userId, level, dataId, searchName, dataCabinetCode);
	}


	@Override
	public List<DocRepeating> selectDocImpAuthorizedRepeatingSearchList(DocFilterDto docFilterDto, String userId, int level,
        String searchName, String dataId, String dataCabinetCode) {
	  return docMapper.selectDocImpAuthorizedRepeatingSearchList(docFilterDto, userId, level,searchName, dataId, dataCabinetCode);
	}

	@Override
	public List<Map<String, String>> selectDocsEApproval(String rObjectId) {
		return docMapper.selectDocsEApproval(rObjectId);
	}

  public DocLinkDto selectDSearchLinkList(DocLinkDto linkFileDataDto) {
    return docMapper.selectDSearchLinkList(linkFileDataDto);
  }
 
}
