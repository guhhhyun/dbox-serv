package com.dongkuksystems.dbox.daos.type.doc;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.dto.type.data.DataCreatorDto;
import com.dongkuksystems.dbox.models.dto.type.doc.DocFilterDto;
import com.dongkuksystems.dbox.models.dto.type.doc.DocLinkDto;
import com.dongkuksystems.dbox.models.dto.type.doc.DocLinkListDto;
import com.dongkuksystems.dbox.models.dto.type.doc.DocPathDto;
import com.dongkuksystems.dbox.models.dto.type.doc.DocVersionListDto;
import com.dongkuksystems.dbox.models.type.doc.Doc;
import com.dongkuksystems.dbox.models.type.doc.DocRepeating; 

public interface DocMapper {
  public Optional<Doc> selectOne(@Param("rObjectId") String rObjectId, @Param("isUDocKey") boolean isUDocKey);
  public Optional<Doc> selectDetailOne(@Param("rObjectId") String rObjectId, @Param("userId") String userId, @Param("isUDocKey") boolean isUDocKey);
  public List<DocRepeating> selectRepeatingOne(@Param("rObjectId") String rObjectId, @Param("isUDocKey") boolean isUDocKey);
  public List<Doc> selectList(@Param("doc") DocFilterDto docFilterDto);
  public List<Doc> selectAuthorizedDetailList(@Param("doc") DocFilterDto docFilterDto, @Param("userId") String userId, @Param("level") int level);
  public List<DocRepeating> selectRepeatingList(@Param("doc") DocFilterDto docFilterDto);
  public List<DocRepeating> selectAuthorizedRepeatingList(@Param("doc") DocFilterDto docFilterDto, @Param("userId") String userId, @Param("level") int level);
  public List<Doc> selectListForFolderId(@Param("folderId") String folderId);
  public List<DocPathDto> selectDocPathList(@Param("doc") DocFilterDto docFilterDto);
  public boolean selectDocDuple(@Param("doc") DocFilterDto docFilterDto, @Param("docPaths") List<String> docPaths);
  public boolean selectLockStatus(@Param("rObjectId") String objectId);
  public String selectDepthCodeForBelowOrgBox(@Param("uCabinetCode") String uCabinetCode);
  
  public int selectOwnDeptCount(@Param("uPjtType") String uPjtType,@Param("uPjtCode") String uPjtCode, @Param("deptCodes") List<String> deptCodes);
  public int selecJoinDeptCount(@Param("uPjtType") String uPjtType,@Param("uPjtCode") String uPjtCode, @Param("deptCodes") List<String> deptCodes);
  public List<String> selectOrgIdByUserId(@Param("userIds") List<String> userIds);
  public List<Doc> selectAuthorizedListByFolIds(@Param("folIds") List<String> folIds, @Param("userId") String userId, @Param("level") int level);
  public List<Doc> selectDeleteAuthorizedListByFolIds(@Param("folIds") List<String> folIds, @Param("userId") String userId, @Param("level") int level);
  public List<Doc> selectListByFolIds(@Param("folIds") List<String> folIds);
  public List<String> selectDocChk(@Param("dataId")String value);
  public List<DataCreatorDto> selectDocCreators(@Param("dataId")String dataId);
  public List<DocVersionListDto> selectDocVersionList(@Param("dataId")String docKey);
  public List<DocVersionListDto> selectDocImpVersionList(@Param("dataId")String docImpKey);
  public List<DocLinkListDto> selectDocLinkList(@Param("dataId")String dataId);
  public List<Doc> selectAuthorizedSearchList(@Param("doc")DocFilterDto docFilterDto, @Param("userId")String userId, @Param("level")int level, @Param("dataId")String dataId, @Param("searchName")String searchName, 
		  @Param("dataCabinetCode")String dataCabinetCode, @Param("imwoncheck")String imwoncheck);
  public List<DocRepeating> selectAuthorizedRepeatingSearchList(@Param("doc") DocFilterDto docFilterDto, @Param("userId") String userId, @Param("level") int level,
		@Param("searchName")String searchName, @Param("dataId")String dataId, @Param("dataCabinetCode")String dataCabinetCode, @Param("imwoncheck")String imwoncheck);
  public List<Doc> selectImpAuthorizedSearchList(@Param("doc") DocFilterDto docFilterDto,@Param("userId") String userId, @Param("level")int level, @Param("dataId")String dataId,
		@Param("searchName")String searchName, @Param("dataCabinetCode")String dataCabinetCode);
public List<DocRepeating> selectDocImpAuthorizedRepeatingSearchList(@Param("doc") DocFilterDto docFilterDto, @Param("userId") String userId, @Param("level") int level,
    @Param("searchName")String searchName, @Param("dataId")String dataId, @Param("dataCabinetCode")String dataCabinetCode);

  List<Map<String, String>> selectDocsEApproval(String rObjectId);
  public List<Doc> selectListByPrCode(@Param("uPrCode") String prCode);
  public List<DocRepeating> repeatingOneNotCurrent(@Param("rObjectId") String rObjectId);
  public List<DocRepeating> selRepeatingOne(@Param("rObjectId") String rObjectId);
  public List<Doc> recycleListByFolIds(@Param("folIds") List<String> folIds);
  public List<Doc> recycleListForFolderId(@Param("folderId") String folderId);
  public DocLinkDto selectDSearchLinkList(@Param("doc")DocLinkDto linkFileDataDto);
}
