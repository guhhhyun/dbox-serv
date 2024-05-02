package com.dongkuksystems.dbox.daos.type.doc;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.dongkuksystems.dbox.models.dto.type.data.DataCreatorDto;
import com.dongkuksystems.dbox.models.dto.type.doc.DocFilterDto;
import com.dongkuksystems.dbox.models.dto.type.doc.DocLinkDto;
import com.dongkuksystems.dbox.models.dto.type.doc.DocLinkListDto;
import com.dongkuksystems.dbox.models.dto.type.doc.DocPathDto;
import com.dongkuksystems.dbox.models.dto.type.doc.DocVersionListDto;
import com.dongkuksystems.dbox.models.type.doc.Doc;
import com.dongkuksystems.dbox.models.type.doc.DocRepeating;

public interface DocDao {
  public Optional<Doc> selectOne(String rObjectId);
  public Optional<Doc> selectOne(String rObjectId, boolean isUDocKey);
  public Optional<Doc> selectDetailOne(String rObjectId, String userId);
  public Optional<Doc> selectDetailOne(String rObjectId, String userId, boolean isUDocKey);
  public List<DocRepeating> selRepeatingOne(String rObjectId);
  public List<DocRepeating> selectRepeatingOne(String rObjectId, boolean isUDocKey);
  public List<Doc> selectList(DocFilterDto docFilterDto);
  public List<Doc> selectAuthorizedDetailList(DocFilterDto docFilterDto, String userId, int level);
  public List<DocRepeating> selectRepeatingList(DocFilterDto docFilterDto);
  public List<DocRepeating> selectAuthorizedRepeatingList(DocFilterDto docFilterDto, String userId, int level);
  public List<Doc> selectListForFolderId(String folderId);
  public List<Doc> recycleListForFolderId(String folderId);
  public List<Doc> selectListByPrCode(String prCode);
  public List<DocPathDto> selectDocPathList(DocFilterDto docFilterDto);
  public boolean selectDocDuple(DocFilterDto docFilterDto, List<String> docPaths);
  public boolean selectLockStatus(String objectId);

  public String selectDepthCodeForBelowOrgBox(String uCabinetCode);
  
  public int selectOwnDeptCount(String uPjtType, String uPjtCode, List<String> deptCodes);
  public int selectJoinDeptCount(String uPjtType, String uPjtCode, List<String> deptCodes);
  public List<String> selectOrgIdByUserId(List<String> userIds);
  public List<Doc> selectAuthorizedListByFolIds(List<String> folIds, String userId, int level);
  public List<Doc> selectListByFolIds(List<String> folIds);
  public List<Doc> recycleListByFolIds(List<String> folIds);
  public List<Doc> selectDeleteAuthorizedListByFolIds(List<String> folIds, String userId, int level);
  public List<String> selectDocChk(String value);
  public List<DataCreatorDto> selectDocCreators(String dataId);
  public List<DocVersionListDto> selectDocVersionList(String docKey);
  public List<DocVersionListDto> selectDocImpVersionList(String docImpKey);
  public List<DocLinkListDto> selectDocLinkList(String dataId);
  public List<Doc> selectAuthorizedSearchList(DocFilterDto dto, String userId, int level, String dataId, String searchName, String dataCabinetCode, String imwoncheck);
  public List<DocRepeating> selectAuthorizedRepeatingSearchList(DocFilterDto dto, String userId, int level,
		String searchName, String dataId, String dataCabinetCode, String imwoncheck);
public List<Doc> selectImpAuthorizedSearchList(DocFilterDto dto, String userId, int level, String dataId,
		String searchName, String dataCabinetCode);
public List<DocRepeating> selectDocImpAuthorizedRepeatingSearchList(DocFilterDto dto, String userId, int level,
		String searchName,String dataId, String dataCabinetCode);

  List<Map<String, String>> selectDocsEApproval(String rObjectId);
  public DocLinkDto selectDSearchLinkList(DocLinkDto linkFileDataDto);

}
