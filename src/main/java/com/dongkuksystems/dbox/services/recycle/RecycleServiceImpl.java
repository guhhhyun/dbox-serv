package com.dongkuksystems.dbox.services.recycle;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;

import com.documentum.fc.common.DfId;
import com.dongkuksystems.dbox.constants.DboxObjectType;
import com.dongkuksystems.dbox.constants.DocLogItem;

import com.dongkuksystems.dbox.constants.FolderStatus;
import com.dongkuksystems.dbox.constants.GrantedLevels;
import com.dongkuksystems.dbox.constants.HamType;
import com.dongkuksystems.dbox.constants.SecLevelCode;
import com.dongkuksystems.dbox.daos.table.etc.gwdept.GwDeptDao;
import com.dongkuksystems.dbox.daos.type.auth.CommonAuthDao;
import com.dongkuksystems.dbox.daos.type.code.CodeDao;
import com.dongkuksystems.dbox.daos.type.doc.DocDao;
import com.dongkuksystems.dbox.daos.type.docbox.project.ProjectDao;
import com.dongkuksystems.dbox.daos.type.docbox.research.ResearchDao;
import com.dongkuksystems.dbox.daos.type.folder.FolderDao;
import com.dongkuksystems.dbox.daos.type.manager.deptMgr.DeptMgrDao;
import com.dongkuksystems.dbox.daos.type.path.PathDao;
import com.dongkuksystems.dbox.daos.type.recycle.RecycleDao;
import com.dongkuksystems.dbox.errors.BadRequestException;
import com.dongkuksystems.dbox.errors.NotFoundException;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.path.DPath;
import com.dongkuksystems.dbox.models.dto.type.auth.HamInfoResult;
import com.dongkuksystems.dbox.models.dto.type.folder.FolderDescendantDto;
import com.dongkuksystems.dbox.models.dto.type.manager.deletemanage.DeleteManageDto;
import com.dongkuksystems.dbox.models.dto.type.recycle.DocRecycleDto;
import com.dongkuksystems.dbox.models.dto.type.recycle.FolRecycleDto;
import com.dongkuksystems.dbox.models.dto.type.recycle.ProjectRecycleDto;
import com.dongkuksystems.dbox.models.dto.type.recycle.RecycleDetailDto;
import com.dongkuksystems.dbox.models.dto.type.recycle.ResearchRecycleDto;
import com.dongkuksystems.dbox.models.table.etc.GwJobTitle;
import com.dongkuksystems.dbox.models.table.etc.VDept;
import com.dongkuksystems.dbox.models.table.etc.VUser;
import com.dongkuksystems.dbox.models.table.log.LogDoc;
import com.dongkuksystems.dbox.models.type.code.Code;
import com.dongkuksystems.dbox.models.type.doc.Doc;
import com.dongkuksystems.dbox.models.type.doc.DocRepeating;
import com.dongkuksystems.dbox.models.type.docbox.Project;
import com.dongkuksystems.dbox.models.type.docbox.Research;
import com.dongkuksystems.dbox.models.type.folder.Folder;
import com.dongkuksystems.dbox.models.type.manager.deptMgr.DeptMgrs;
import com.dongkuksystems.dbox.models.type.recycle.Recycle;
import com.dongkuksystems.dbox.services.AbstractCommonService;
import com.dongkuksystems.dbox.services.code.CodeService;
import com.dongkuksystems.dbox.services.data.DataRePathService;
import com.dongkuksystems.dbox.services.dept.GwDeptService;
import com.dongkuksystems.dbox.services.doc.DocService;
import com.dongkuksystems.dbox.services.folder.FolderService;
import com.dongkuksystems.dbox.services.isdelete.IsDeleteService;
import com.dongkuksystems.dbox.services.project.ProjectService;
import com.dongkuksystems.dbox.services.research.ResearchService;
import com.dongkuksystems.dbox.services.user.UserService;
import com.dongkuksystems.dbox.utils.dctm.DCTMUtils;

@Service
public class RecycleServiceImpl extends AbstractCommonService implements RecycleService {
  private final RecycleDao recycleDao;
  private final UserService userService;
  private final CodeService codeService;
  private final FolderService folderService;
  private final DocService docService;
  private final DeptMgrDao deptMgrDao;
  private final GwDeptDao deptDao;
  private final DocDao docDao;
  private final CommonAuthDao commonAuthDao;
  private final ProjectService projectService;
  private final ResearchService researchService;
  private final ProjectDao projectDao;
  private final ResearchDao researchDao;
  private final FolderDao folderDao;
  private final CodeDao codeDao;
  private final PathDao pathDao;
  private final IsDeleteService isDeleteService;
  private final GwDeptService gwDeptService;
  private final DataRePathService pathReService;
  


  public RecycleServiceImpl(RecycleDao recycleDao, UserService userService,
      CodeService codeService, FolderService folderService, DocService docService,
      DeptMgrDao deptMgrDao, GwDeptDao deptDao, DocDao docDao, CommonAuthDao commonAuthDao,
      ProjectService projectService, ResearchService researchService, ProjectDao projectDao,
      ResearchDao researchDao, FolderDao folderDao, CodeDao codeDao, PathDao pathDao,
      IsDeleteService isDeleteService, GwDeptService gwDeptService
      , DataRePathService pathReService
      ) {
    this.recycleDao = recycleDao;
    this.userService = userService;
    this.codeService = codeService;
    this.folderService = folderService;
    this.docService = docService;
    this.docDao = docDao;
    this.deptMgrDao = deptMgrDao;
    this.deptDao = deptDao;
    this.commonAuthDao = commonAuthDao;
    this.projectService = projectService;
    this.researchService = researchService;
    this.projectDao = projectDao;
    this.researchDao = researchDao;
    this.folderDao = folderDao;
    this.codeDao = codeDao;
    this.pathDao = pathDao;
    this.isDeleteService = isDeleteService;
    this.gwDeptService = gwDeptService;
    this.pathReService= pathReService;
  }

  @Override
  public List<RecycleDetailDto> getDeletedDataByOrgId(UserSession userSession, String dataId) throws Exception {
    Recycle recycleData = recycleDao.oneRecycleById(dataId);
    final ModelMapper modelMapper = getModelMapper();
    List<RecycleDetailDto> realList = new ArrayList<>();
    if ("D".equals(recycleData.getUObjType())) {

      List<Recycle> recycleList = recycleDao.oneDocById(dataId);
      Map<String, String> docSecLevelMap = codeService.getSecLevelMap();
      List<DocRecycleDto> docRecycleList = recycleList.stream().map((item) -> {

        DocRecycleDto docRecycleDetailDto = modelMapper.map(item, DocRecycleDto.class);

        Doc doc = Optional.ofNullable(item.getRecycleDetail()).orElse(new Doc());
        VUser recycleUser = Optional.ofNullable(item.getUserDetail()).orElse(new VUser());
        Folder folder = Optional.ofNullable(item.getFolderDetail()).orElse(new Folder());
        VUser deleteUser = Optional.ofNullable(item.getRecycleUserDetail()).orElse(new VUser());
        List<DocRepeating> docV = docDao.selRepeatingOne(doc.getRObjectId());

        docRecycleDetailDto.setDocName(doc.getObjectName());
        docRecycleDetailDto.setWriteUser(doc.getURegUser());
        docRecycleDetailDto.setUCabinetCode(doc.getUCabinetCode());
        docRecycleDetailDto.setWriteUserName(recycleUser.getDisplayName());
        docRecycleDetailDto.setFolderPath(doc.getUFolderPath());
        docRecycleDetailDto.setUpdateDate(doc.getUUpdateDate());
        docRecycleDetailDto.setSize(doc.getRContentSize());
        docRecycleDetailDto.setSecLevel(doc.getUSecLevel());
        docRecycleDetailDto.setUploadDate(doc.getURegDate());
        docRecycleDetailDto.setTag(doc.getUDocTag());
        docRecycleDetailDto.setFileExt(doc.getUFileExt());
        docRecycleDetailDto.setBoonryu(doc.getUDocClass());
        docRecycleDetailDto
            .setDocDeptPath(Optional.ofNullable(doc.getOwnDeptDetail()).orElse(new VDept()).getUnitFullName());
        docRecycleDetailDto.setExpectedDeleteDate(docRecycleDetailDto.getUDeleteDate().plusMonths(1));
        docRecycleDetailDto.setWriteUserJobTitleName(
            Optional.ofNullable(recycleUser.getJobTitleDetail()).orElse(new GwJobTitle()).getName());
        docRecycleDetailDto.setWriteUserDeptName(recycleUser.getOrgNm());
        docRecycleDetailDto.setSecLevelName(docSecLevelMap.get(doc.getUSecLevel()));
        docRecycleDetailDto.setUFolType(folder.getUFolType());
        docRecycleDetailDto.setFolderName(folder.getUFolName());
        docRecycleDetailDto.setDeleteUserName(deleteUser.getDisplayName());
        if (docRecycleDetailDto.getUObjType().equals("D")) {
          docRecycleDetailDto.setVersion(docV.get(0).getRVersionLabel());
        }
        return docRecycleDetailDto;
      }).collect(Collectors.toList());
      realList.addAll(docRecycleList.stream()
          .map(item -> RecycleDetailDto.builder().dataType(DboxObjectType.DOCUMENT.getValue()).docDto(item).build())
          .collect(Collectors.toList()));
    } else if("F".equals(recycleData.getUObjType())) {
      Map<String, String> folSecLevelMap = codeService.getSecLevelMap();
      List<Recycle> recycleList2 = recycleDao.oneFolById(dataId);
      List<FolRecycleDto> folderRecycleList = recycleList2.stream().map((item) -> {
        FolRecycleDto folderRecycleDetailDto = modelMapper.map(item, FolRecycleDto.class);
        VUser folderUser = Optional.ofNullable(item.getFolderUserDetail()).orElse(new VUser());
        Folder folder = Optional.ofNullable(item.getFolderDetail()).orElse(new Folder());
        VUser recycleUser = Optional.ofNullable(item.getRecycleUserDetail()).orElse(new VUser());

          folderRecycleDetailDto.setDocName(folder.getUFolName());
          folderRecycleDetailDto.setWriteUser(folder.getUCreateUser());
          folderRecycleDetailDto.setWriteUserName(folderUser.getDisplayName());
          folderRecycleDetailDto.setSecLevel(folder.getUSecLevel());
          folderRecycleDetailDto.setSecLevelName(folSecLevelMap.get(folder.getUSecLevel()));
          folderRecycleDetailDto.setUCabinetCode(folder.getUCabinetCode());
          folderRecycleDetailDto.setTag(folder.getUFolTag());
          folderRecycleDetailDto.setBoonryu(folder.getUFolClass());
          folderRecycleDetailDto.setFileExt("폴더");
          folderRecycleDetailDto.setUploadDate(folder.getUCreateDate());
          folderRecycleDetailDto.setWriteUserJobTitleName(
              Optional.ofNullable(folderUser.getJobTitleDetail()).orElse(new GwJobTitle()).getName());
          folderRecycleDetailDto.setWriteUserDeptName(folderUser.getOrgNm());
          folderRecycleDetailDto.setExpectedDeleteDate(folderRecycleDetailDto.getUDeleteDate().plusMonths(1));
          folderRecycleDetailDto.setDeleteUserName(recycleUser.getDisplayName());
          folderRecycleDetailDto.setSize(folder.getFolSize());
          folderRecycleDetailDto.setFolderPath(Optional.ofNullable(folder.getOwnDeptDetail()).orElse(new VDept()).getUnitFullName());
          
        return folderRecycleDetailDto;

      }).collect(Collectors.toList());
      realList.addAll(folderRecycleList.stream()
          .map(item -> RecycleDetailDto.builder().dataType(DboxObjectType.FOLDER.getValue()).folDto(item).build())
          .collect(Collectors.toList()));
    }else if("P".equals(recycleData.getUObjType())) {
      
    }
    return realList;
  }

  @Override
  public List<RecycleDetailDto> getDeletedDataList(UserSession userSession, boolean isMobile) throws Exception {
    final ModelMapper modelMapper = getModelMapper();

    if (isMobile) {
      throw new RuntimeException("모바일에선 수행할 수 없습니다.");
    }
    
    List<RecycleDetailDto> realList = new ArrayList<>();
    VUser userr = userService.selectOneByUserId(userSession.getDUserId()).orElse(null);
    VDept deptData = deptDao.selectOneByOrgIdDefault(userr.getOrgId());
    List<Recycle> recycleList = recycleDao.docAuthorizedDetailList(userSession.getDUserId(), GrantedLevels.READ.getLevel(), deptData.getUCabinetCode());
    Set<String> specialUserIdSet = codeService.getSpecialUserIdSet();
    Map<String, String> docSecLevelMap = codeService.getSecLevelMap();
    List<DocRecycleDto> docRecycleList = recycleList.stream().map((item) -> {

      DocRecycleDto docRecycleDetailDto = modelMapper.map(item, DocRecycleDto.class);

      Doc doc = Optional.ofNullable(item.getRecycleDetail()).orElseThrow(() -> new NotFoundException("없는 문서입니다."));
      String comCode = deptDao.selectComCodeByCabinetCode(doc.getUCabinetCode());
      Project project = projectDao.selectOneByUPjtCode(doc.getUPrCode()).orElse(new Project());
      Research research = researchDao.selectOneByURschCode(doc.getUPrCode()).orElse(new Research());
      Code codeData = codeDao.selectOneByOther("CONFIG_DELETE_PERIOD", comCode, "RECYCLE");
      VUser recycleUser = Optional.ofNullable(item.getUserDetail()).orElse(new VUser());
      Folder folder = Optional.ofNullable(item.getFolderDetail()).orElse(new Folder());
      VUser deleteUser = Optional.ofNullable(item.getRecycleUserDetail()).orElse(new VUser());
      List<DocRepeating> docV = docDao.selRepeatingOne(doc.getRObjectId());
      String path = pathDao.selectFolderPath(doc.getUFolId());
      
      docRecycleDetailDto.setDocName(doc.getObjectName());
      docRecycleDetailDto.setWriteUser(doc.getURegUser());
      docRecycleDetailDto.setUCabinetCode(doc.getUCabinetCode());
      docRecycleDetailDto.setWriteUserName(recycleUser.getDisplayName());
      if(" ".equals(doc.getUPrType()) || null == doc.getUPrType()) {
        docRecycleDetailDto.setDocDeptPath("부서함" + path);
      }else if("P".equals(doc.getUPrType())) {
        if(null != project.getRObjectId()) {
          docRecycleDetailDto.setDocDeptPath("프로젝트/" + project.getUPjtName() + path);
        }else {
          docRecycleDetailDto.setDocDeptPath("프로젝트" + path);
        }
      }else if("R".equals(doc.getUPrType())) {
        if(null != research.getRObjectId()) {
          docRecycleDetailDto.setDocDeptPath("연구과제/" + research.getURschName() + path);
        }else {
          docRecycleDetailDto.setDocDeptPath("연구과제" + path); 
        }
      }else {
        docRecycleDetailDto.setDocDeptPath(path);
      }
      docRecycleDetailDto.setUpdateDate(doc.getUUpdateDate());
      docRecycleDetailDto.setSize(doc.getRContentSize());
      docRecycleDetailDto.setSecLevel(doc.getUSecLevel());
      docRecycleDetailDto.setUploadDate(doc.getURegDate());
      docRecycleDetailDto.setTag(doc.getUDocTag());
      docRecycleDetailDto.setFileExt(doc.getUFileExt());
      docRecycleDetailDto.setBoonryu(doc.getUDocClass());
      docRecycleDetailDto.setExpectedDeleteDate(docRecycleDetailDto.getUDeleteDate().plusDays(Integer.parseInt(codeData.getUCodeVal3())));
      docRecycleDetailDto.setWriteUserJobTitleName(
          Optional.ofNullable(recycleUser.getJobTitleDetail()).orElse(new GwJobTitle()).getName());
      docRecycleDetailDto.setWriteUserDeptName(recycleUser.getOrgNm());
      docRecycleDetailDto.setSecLevelName(docSecLevelMap.get(doc.getUSecLevel()));
      docRecycleDetailDto.setUFolId(doc.getUFolId());
      docRecycleDetailDto.setUFolType(folder.getUFolType());
      docRecycleDetailDto.setFolderName(folder.getUFolName());
      docRecycleDetailDto.setDeleteUserName(deleteUser.getDisplayName());
      if (docRecycleDetailDto.getUObjType().equals("D")) {
        docRecycleDetailDto.setVersion(docV.get(0).getRVersionLabel());
      }
      deptDao.selectComCodeByCabinetCode(null);
      return docRecycleDetailDto;
    }).collect(Collectors.toList());
    
    docRecycleList = docRecycleList.stream().filter(item -> {

      String userOrgId = userr.getOrgId();
      DocRecycleDto docRecycleDetailDto = modelMapper.map(item, DocRecycleDto.class);
      String docCabinetCode = docRecycleDetailDto.getUCabinetCode();

      String docDeptCode = deptDao.selectOrgIdByCabinetcode(docCabinetCode);

      if (!(userOrgId.equals(docDeptCode)))
        return false;
      else
        return true;
    }).collect(Collectors.toList());

    docRecycleList = docRecycleList.stream().filter(item -> {

      // 현재 사용자의 특별사용자 여부 확인
      String currentUserId = userSession.getUser().getUserId();
      boolean isSpecial = specialUserIdSet.contains(currentUserId);
      String uSecLevel = item.getSecLevel();

      // 특별사용자가 아니고 제한문서일 경우 제외
      if(isMobile == true) {
        if (!isSpecial && SecLevelCode.SEC.getValue().equals(uSecLevel))
          return false;
        else
          return true;
      }else {
        return true;
      }
    }).collect(Collectors.toList());

    realList.addAll(docRecycleList.stream()
        .map(item -> RecycleDetailDto.builder().dataType(DboxObjectType.DOCUMENT.getValue()).docDto(item).build())
        .collect(Collectors.toList()));
    Map<String, String> folSecLevelMap = codeService.getSecLevelMap();
    List<Recycle> recycleList2 = recycleDao.folderAuthorizedDetailList(userSession.getDUserId(), deptData.getUCabinetCode());
    List<FolRecycleDto> folderRecycleList = recycleList2.stream().map((item) -> {
      FolRecycleDto folderRecycleDetailDto = modelMapper.map(item, FolRecycleDto.class);
      VUser folderUser = Optional.ofNullable(item.getFolderUserDetail()).orElse(new VUser());
      Folder folder = Optional.ofNullable(item.getFolderDetail()).orElse(new Folder());
      String comCode = deptDao.selectComCodeByCabinetCode(folder.getUCabinetCode());
      Code codeData = codeDao.selectOneByOther("CONFIG_DELETE_PERIOD", comCode, "RECYCLE");
      VUser recycleUser = Optional.ofNullable(item.getRecycleUserDetail()).orElse(new VUser());
      String path = pathDao.selectFolderPath(folder.getUUpFolId());
      Project project = projectDao.selectOneByUPjtCode(folder.getUPrCode()).orElse(new Project());
      Research research = researchDao.selectOneByURschCode(folder.getUPrCode()).orElse(new Research());
      boolean authorized = GrantedLevels.findByLabel(folder.getMaxPermitType()) >= GrantedLevels.READ.getLevel();
      if (authorized) {
        folderRecycleDetailDto.setDocName(folder.getUFolName());
        folderRecycleDetailDto.setWriteUser(folder.getUCreateUser());
        folderRecycleDetailDto.setWriteUserName(folderUser.getDisplayName());
        folderRecycleDetailDto.setSecLevel(folder.getUSecLevel());
        folderRecycleDetailDto.setSecLevelName(folSecLevelMap.get(folder.getUSecLevel()));
        folderRecycleDetailDto.setUCabinetCode(folder.getUCabinetCode());
        folderRecycleDetailDto.setTag(folder.getUFolTag());
        folderRecycleDetailDto.setBoonryu(folder.getUFolClass());
        folderRecycleDetailDto.setFileExt("폴더");
        folderRecycleDetailDto.setUploadDate(folder.getUCreateDate());
        folderRecycleDetailDto.setWriteUserJobTitleName(
            Optional.ofNullable(folderUser.getJobTitleDetail()).orElse(new GwJobTitle()).getName());
        folderRecycleDetailDto.setWriteUserDeptName(folderUser.getOrgNm());
        folderRecycleDetailDto.setExpectedDeleteDate(folderRecycleDetailDto.getUDeleteDate().plusDays(Integer.parseInt(codeData.getUCodeVal3())));
        folderRecycleDetailDto.setDeleteUserName(recycleUser.getDisplayName());
        folderRecycleDetailDto.setSize(folder.getFolSize());
        if("DFO".equals(folder.getUFolType())) {
          folderRecycleDetailDto.setFolderPath("부서함" + path);
        }else if("PFO".equals(folder.getUFolType())) {
          if(null != project.getRObjectId()) {
            folderRecycleDetailDto.setFolderPath("프로젝트/" + project.getUPjtName() + path);
          }else {
            folderRecycleDetailDto.setFolderPath("프로젝트" + path);
          }
        }else if("RFO".equals(folder.getUFolType())) {
          if(null != research.getRObjectId()) {
            folderRecycleDetailDto.setFolderPath("연구과제/" + research.getURschName() + path);
          }else {
            folderRecycleDetailDto.setFolderPath("연구과제" + path); 
          }
        }else {
          folderRecycleDetailDto.setFolderPath(path);
        }
        
      }
      return folderRecycleDetailDto;

    }).collect(Collectors.toList());

    folderRecycleList = folderRecycleList.stream().filter(item -> {

      String userOrgId = userr.getOrgId();
      FolRecycleDto folderRecycleDetailDto = modelMapper.map(item, FolRecycleDto.class);
      String folCabinetCode = folderRecycleDetailDto.getUCabinetCode();

      String docDeptCode = deptDao.selectOrgIdByCabinetcode(folCabinetCode);

      if (!(userOrgId.equals(docDeptCode)))
        return false;
      else
        return true;

    }).collect(Collectors.toList());

    realList.addAll(folderRecycleList.stream()
        .map(item -> RecycleDetailDto.builder().dataType(DboxObjectType.FOLDER.getValue()).folDto(item).build())
        .collect(Collectors.toList()));
    
    Map<String, String> projectSecLevelMap = codeService.getSecLevelMap();
    List<Recycle> recycleList3 = recycleDao.projectList();
    List<ProjectRecycleDto> projectRecycleList = recycleList3.stream().map((item) -> {
      ProjectRecycleDto projectRecycleDetailDto = modelMapper.map(item, ProjectRecycleDto.class);
      VUser projectUser = Optional.ofNullable(item.getProjectUserDetail()).orElse(new VUser());
      Project project = Optional.ofNullable(item.getProjectDetail()).orElse(new Project());
      VUser recycleUser = Optional.ofNullable(item.getRecycleUserDetail()).orElse(new VUser());
      String comCode = deptDao.selectComCodeByCabinetCode(project.getUCabinetCode());
      Code codeData = codeDao.selectOneByOther("CONFIG_DELETE_PERIOD", comCode, "RECYCLE");
     
      projectRecycleDetailDto.setDocName(project.getUPjtName());
      projectRecycleDetailDto.setWriteUser(project.getUCreateUser());
      projectRecycleDetailDto.setWriteUserName(projectUser.getDisplayName());
      projectRecycleDetailDto.setSecLevelName(projectSecLevelMap.get(project.getUSecLevel()));
      projectRecycleDetailDto.setFileExt("프로젝트");
      projectRecycleDetailDto.setUploadDate(project.getUCreateDate());
      projectRecycleDetailDto.setWriteUserJobTitleName(
            Optional.ofNullable(projectUser.getJobTitleDetail()).orElse(new GwJobTitle()).getName());
      projectRecycleDetailDto.setWriteUserDeptName(projectUser.getOrgNm());
      projectRecycleDetailDto.setExpectedDeleteDate(projectRecycleDetailDto.getUDeleteDate().plusDays(Integer.parseInt(codeData.getUCodeVal3())));
      projectRecycleDetailDto.setDeleteUserName(recycleUser.getDisplayName());
     
      return projectRecycleDetailDto;

    }).collect(Collectors.toList());
    
    projectRecycleList = projectRecycleList.stream().filter(item -> {

      String userOrgId = userr.getOrgId();
      ProjectRecycleDto projectRecycleDetailDto = modelMapper.map(item, ProjectRecycleDto.class);
      String pjtCabinetCode = projectRecycleDetailDto.getUCabinetCode();

      String pjtDeptCode = deptDao.selectOrgIdByCabinetcode(pjtCabinetCode);

      if (!(userOrgId.equals(pjtDeptCode)))
        return false;
      else
        return true;

    }).collect(Collectors.toList());

    realList.addAll(projectRecycleList.stream()
        .map(item -> RecycleDetailDto.builder().dataType(DboxObjectType.PROJECT.getValue()).pjtDto(item).build())
        .collect(Collectors.toList()));
    
    Map<String, String> researchSecLevelMap = codeService.getSecLevelMap();
    List<Recycle> recycleList4 = recycleDao.researchList();
    List<ResearchRecycleDto> researchRecycleList = recycleList4.stream().map((item) -> {
      ResearchRecycleDto researchRecycleDetailDto = modelMapper.map(item, ResearchRecycleDto.class);
      VUser researchUser = Optional.ofNullable(item.getProjectUserDetail()).orElse(new VUser());
      Research research = Optional.ofNullable(item.getResearchDetail()).orElse(new Research());
      VUser recycleUser = Optional.ofNullable(item.getRecycleUserDetail()).orElse(new VUser());
      String comCode = deptDao.selectComCodeByCabinetCode(research.getUCabinetCode());
      Code codeData = codeDao.selectOneByOther("CONFIG_DELETE_PERIOD", comCode, "RECYCLE");
      
      researchRecycleDetailDto.setDocName(research.getURschName());
      researchRecycleDetailDto.setWriteUser(research.getUCreateUser());
      researchRecycleDetailDto.setWriteUserName(researchUser.getDisplayName());
      researchRecycleDetailDto.setSecLevelName(researchSecLevelMap.get(research.getUSecLevel()));
      researchRecycleDetailDto.setFileExt("연구과제");
      researchRecycleDetailDto.setUploadDate(research.getUCreateDate());
      researchRecycleDetailDto.setWriteUserJobTitleName(
            Optional.ofNullable(researchUser.getJobTitleDetail()).orElse(new GwJobTitle()).getName());
      researchRecycleDetailDto.setWriteUserDeptName(researchUser.getOrgNm());
      researchRecycleDetailDto.setExpectedDeleteDate(researchRecycleDetailDto.getUDeleteDate().plusDays(Integer.parseInt(codeData.getUCodeVal3())));
      researchRecycleDetailDto.setDeleteUserName(recycleUser.getDisplayName());
      return researchRecycleDetailDto;

    }).collect(Collectors.toList());
    
    researchRecycleList = researchRecycleList.stream().filter(item -> {

      String userOrgId = userr.getOrgId();
      ResearchRecycleDto researchRecycleDetailDto = modelMapper.map(item, ResearchRecycleDto.class);
      String rschCabinetCode = researchRecycleDetailDto.getUCabinetCode();

      String rschDeptCode = deptDao.selectOrgIdByCabinetcode(rschCabinetCode);

      if (!(userOrgId.equals(rschDeptCode)))
        return false;
      else
        return true;

    }).collect(Collectors.toList());
    
    realList.addAll(researchRecycleList.stream()
        .map(item -> RecycleDetailDto.builder().dataType(DboxObjectType.RESEARCH.getValue()).rschDto(item).build())
        .collect(Collectors.toList()));
    return realList;
  }

  @Override
  public Recycle getDeletedData(String dataId) throws Exception {

    Recycle recycleData = recycleDao.oneRecycleById(dataId);

    return recycleData;
  }

  @Override
  public String restoreDataByOrgId(UserSession userSession, String dataId, String ip, DeleteManageDto dto) throws Exception {
    IDfSession idfSession = null;
    IDfPersistentObject idf_PObj = null;
    IDfPersistentObject idf_PObj2 = null;
    IDfPersistentObject idf_PObj3 = null;
    IDfPersistentObject idf_PObj4 = null;
    IDfSession adSess = null;
    IDfDocument idf_Doc = null;
    Recycle recycleData = null;
    Doc docData = null;
    Folder folData = null;
    Folder moveFolData = null;
    String moveCabinetCode = null;
    
    try {
      idfSession = this.getIdfSession(userSession);
      idf_PObj = (IDfPersistentObject) idfSession.getObject(new DfId(dataId));
      recycleData = recycleDao.oneRecycleById(dataId);
      docData = docService.selectOne(recycleData.getUObjId()).orElse(new Doc());
      List<DocRepeating> docV = docDao.selRepeatingOne(docData.getRObjectId());
      String orgId = deptDao.selectOrgIdByCabinetcode(recycleData.getUCabinetCode());
      LocalDateTime today = LocalDateTime.now();
      idfSession.beginTrans();
      adSess = DCTMUtils.getAdminSession();
      
      String userTypeData = null;      
      if(dto == null || null == dto.getUserType()) {
        userTypeData = "P";      
      } else if("DKG".equals(dto.getUserType())) {
        userTypeData = "G";
      } else if("D".equals(dto.getUserType()))  {
        userTypeData = "D";
      } else {
        userTypeData = "C";
      }
      
      if (!adSess.isTransactionActive()) {
       adSess.beginTrans();
      }     
      folData = folderService.selectOne(recycleData.getUObjId()).orElse(new Folder());

      if(dto != null) {
      moveFolData = folderService.selectOne(dto.getTargetId()).orElse(new Folder());
      }
      
      if (FolderStatus.LOCK.getValue().equals(folData.getUFolStatus())) {
        throw new BadRequestException("폴더가 잠금상태입니다.");
      } 
      
      if (idf_PObj.getString("u_obj_type").equals("D")) {
     
        String type2 = recycleData.getUCabinetType();
        HamType type = HamType.findByValue(type2);
        
        boolean isDelete = isDeleteService.isDelete(docData.getRObjectId());
        switch (type) {
        case DEPT:
        case COMPANY:
        case COMPANY_M:         
          idf_Doc = (IDfDocument) adSess.getObject(new DfId(recycleData.getUObjId()));
          idf_Doc.setString("u_delete_status", " ");
          idf_Doc.setString("u_update_date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
          idf_Doc.save();
          idf_PObj.destroy();
          if(isDelete == true) {
            idf_Doc.setString("u_pr_code", " ");
            idf_Doc.setString("u_pr_type", " ");
            if(null != moveFolData.getRObjectId()) {
              idf_Doc.setString("u_fol_id", moveFolData.getRObjectId());
            }else {
              idf_Doc.setString("u_fol_id", " ");
            }
            idf_Doc.save();          
          }
          break;
        case PROJECT:
          idf_Doc = (IDfDocument) adSess.getObject(new DfId(recycleData.getUObjId()));
          idf_Doc.setString("u_delete_status", " ");
          idf_Doc.setString("u_update_date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
          idf_Doc.save();
           idf_PObj.destroy();
           if(isDelete == true) {
             idf_Doc.setString("u_pr_code", " ");
             idf_Doc.setString("u_pr_type", " ");
             if(null != moveFolData.getRObjectId()) {
               idf_Doc.setString("u_fol_id", moveFolData.getRObjectId());
             }else {
               idf_Doc.setString("u_fol_id", " ");
             }
             idf_Doc.save();      
           }
          
          break;
        case RESEARCH:
            
            idf_Doc = (IDfDocument) adSess.getObject(new DfId(recycleData.getUObjId()));
            idf_Doc.setString("u_delete_status", " ");
            idf_Doc.setString("u_update_date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            idf_Doc.save();
            idf_PObj.destroy();
            if(isDelete == true) {
              idf_Doc.setString("u_pr_code", " ");
              idf_Doc.setString("u_pr_type", " ");
              if(null != moveFolData.getRObjectId()) {
                idf_Doc.setString("u_fol_id", moveFolData.getRObjectId());
              }else {
                idf_Doc.setString("u_fol_id", " ");
              }
              idf_Doc.save();      
            }
 
          break;
        default:
        }        
        LogDoc logDoc = LogDoc.builder().uJobCode(DocLogItem.RR.getValue()).uDocId(docData.getRObjectId())
              .uDocKey(docData.getUDocKey()).uDocName(docData.getTitle())
              .uDocVersion(docV.get(0).getRVersionLabel()).uOwnDeptcode(orgId).uActDeptCode(userSession.getUser().getOrgId())
              .uJobUser(userSession.getDUserId()).uJobUserType(userTypeData).uDocStatus(docData.getUDocStatus())
              .uSecLevel(docData.getUSecLevel()).uCabinetCode(docData.getUCabinetCode()).uJobGubun("").uUserIp(ip)
              .uFileSize(Integer.valueOf(docData.getRContentSize()))
              .build();
        insertLog(logDoc);        
        // 폴더의 경우 하위에 DELETE 권한이 없는 자료가 있을 경우 해당 자료는 남기고 나머지만 복원
      } else if (idf_PObj.getString("u_obj_type").equals("F")) {
        List<FolderDescendantDto> folderDescendantDtoList = folderDao.selectDescendantsAll(recycleData.getUObjId());
        folData = folderService.selectOne(recycleData.getUObjId()).orElse(new Folder());       
        List<Doc> docAllList2 = docDao.recycleListForFolderId(folData.getRObjectId());
        boolean isDelete = isDeleteService.isDeleteFol(folData.getRObjectId());
        HamInfoResult hamInfo = commonAuthDao.selectHamInfo(orgId)
            .orElseThrow(() -> new BadRequestException("존재하지 않는 dataId 입니다. (dataId: " + dataId + ")"));
        HamType type = HamType.findByValue(hamInfo.getHamType());
        switch (type) {
        case DEPT:
        case COMPANY:
        case COMPANY_M:
          
          if (folderDescendantDtoList.size() == 0) {
            idf_PObj2 = (IDfPersistentObject) idfSession.getObject(new DfId(recycleData.getUObjId()));
            idf_PObj2.setString("u_delete_status", " ");
            idf_PObj2.setString("u_update_date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            idf_PObj2.save();
            if(isDelete == true) {
              idf_PObj2.setString("u_pr_code", " ");
              idf_PObj2.setString("u_pr_type", " ");
              idf_PObj2.setString("u_fol_type", "DFO");
              if(null != moveFolData.getRObjectId()) {
                idf_PObj2.setString("u_up_fol_id", moveFolData.getRObjectId());
              }else {
                idf_PObj2.setString("u_up_fol_id", " ");
              }
              idf_PObj2.save();      
            }
            for (int i = 0; i < docAllList2.size(); i++) {
              Doc docDataOne = docService.selectOne(docAllList2.get(i).getRObjectId()).orElse(new Doc());
              String orgId2 = deptDao.selectOrgIdByCabinetcode(docDataOne.getUCabinetCode());
              List<DocRepeating> docV2 = docDao.selRepeatingOne(docDataOne.getRObjectId());
              idf_Doc = (IDfDocument) adSess.getObject(new DfId(docDataOne.getRObjectId()));
              idf_Doc.setString("u_delete_status", " ");
              idf_Doc.setString("u_update_date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
              idf_Doc.save();
              if(isDelete == true) {
                idf_Doc.setString("u_pr_code", " ");
                idf_Doc.setString("u_pr_type", " ");              
                idf_Doc.save();      
              }
              try {
                insertLog(LogDoc.builder().uJobCode(DocLogItem.RR.getValue()).uDocId(docDataOne.getRObjectId())
                    .uDocKey(docDataOne.getUDocKey()).uDocName(docDataOne.getTitle())
                    .uDocVersion(docV2.get(0).getRVersionLabel()).uOwnDeptcode(orgId2).uActDeptCode(userSession.getUser().getOrgId())
                    .uJobUser(userSession.getDUserId()).uJobUserType(userTypeData).uDocStatus(docDataOne.getUDocStatus())
                    .uSecLevel(docDataOne.getUSecLevel()).uCabinetCode(docDataOne.getUCabinetCode()).uJobGubun("")
                    .uUserIp(ip)
                    .uFileSize(Integer.valueOf(docDataOne.getRContentSize()))
                    .build());
              } catch (Exception ex) {
              }
            }
            idf_PObj.destroy();
          } else {
            // 각 폴더의 하위에 있는 문서 조회 (삭제 이상의 권한이 있는 문서만 조회)
            List<String> folIdList = folderDescendantDtoList.stream().map((item) -> item.getRObjectId())
                .collect(Collectors.toList());
            List<Doc> docAllList = docDao.recycleListByFolIds(folIdList);
            idf_PObj2 = (IDfPersistentObject) idfSession.getObject(new DfId(recycleData.getUObjId()));
            idf_PObj2.setString("u_delete_status", " ");
            idf_PObj2.setString("u_update_date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            idf_PObj2.save();
            if(isDelete == true) {
              idf_PObj2.setString("u_pr_code", " ");
              idf_PObj2.setString("u_pr_type", " ");
              idf_PObj2.setString("u_fol_type", "DFO");
              if(null != moveFolData.getRObjectId()) {
                idf_PObj2.setString("u_up_fol_id", moveFolData.getRObjectId());
              }else {
                idf_PObj2.setString("u_up_fol_id", " ");
              }
              idf_PObj2.save();      
            }
            for (int i = 0; i < folIdList.size(); i++) {
              Folder folDescendData = folderService.selectOne(folIdList.get(i)).orElse(new Folder());            
              idf_PObj3 = (IDfPersistentObject) idfSession.getObject(new DfId(folDescendData.getRObjectId()));
              idf_PObj3.setString("u_delete_status", " ");
              idf_PObj3.setString("u_update_date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
              idf_PObj3.save();
              if(isDelete == true) {
                idf_PObj3.setString("u_pr_code", " ");
                idf_PObj3.setString("u_pr_type", " ");
                idf_PObj3.setString("u_fol_type", "DFO");             
                idf_PObj3.save();      
              }
            }
            for (int i = 0; i < docAllList.size(); i++) {
              Doc docDataOne = docService.selectOne(docAllList.get(i).getRObjectId()).orElse(new Doc());
              String orgId2 = deptDao.selectOrgIdByCabinetcode(docDataOne.getUCabinetCode());
              List<DocRepeating> docV2 = docDao.selRepeatingOne(docDataOne.getRObjectId());
              idf_Doc = (IDfDocument) adSess.getObject(new DfId(docDataOne.getRObjectId()));
              idf_Doc.setString("u_delete_status", " ");
              idf_Doc.setString("u_update_date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
              idf_Doc.save();
              if(isDelete == true) {
                idf_Doc.setString("u_pr_code", " ");
                idf_Doc.setString("u_pr_type", " ");              
                idf_Doc.save();      
              }
              LogDoc logDoc = LogDoc.builder().uJobCode(DocLogItem.RR.getValue()).uDocId(docDataOne.getRObjectId())
                    .uDocKey(docDataOne.getUDocKey()).uDocName(docDataOne.getTitle())
                    .uDocVersion(docV2.get(0).getRVersionLabel()).uOwnDeptcode(orgId2).uActDeptCode(userSession.getUser().getOrgId())
                    .uJobUser(userSession.getDUserId()).uJobUserType(userTypeData).uDocStatus(docDataOne.getUDocStatus())
                    .uSecLevel(docDataOne.getUSecLevel()).uCabinetCode(docDataOne.getUCabinetCode()).uJobGubun("")
                    .uUserIp(ip)
                    .uFileSize(Integer.valueOf(docDataOne.getRContentSize()))
                    .build();
                insertLog(logDoc);
            }
            idf_PObj.destroy();
          }
          break;
        case PROJECT:
          Project project = projectService.selectProjectByUPjtCode(folData.getUPrCode())
              .orElseThrow(() -> new BadRequestException("존재하지 않는 프로젝트 코드입니다."));
          
          if (folderDescendantDtoList.size() == 0) {
            idf_PObj2 = (IDfPersistentObject) idfSession.getObject(new DfId(recycleData.getUObjId()));
            idf_PObj2.setString("u_delete_status", " ");
            idf_PObj2.setString("u_update_date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            idf_PObj2.save();
            if(isDelete == true) {
              idf_PObj2.setString("u_pr_code", " ");
              idf_PObj2.setString("u_pr_type", " ");
              idf_PObj2.setString("u_fol_type", "DFO");
              if(null != moveFolData.getRObjectId()) {
                idf_PObj2.setString("u_up_fol_id", moveFolData.getRObjectId());
              }else {
                idf_PObj2.setString("u_up_fol_id", " ");
              }
              idf_PObj2.save();      
            }
            for (int i = 0; i < docAllList2.size(); i++) {
              Doc docDataOne = docService.selectOne(docAllList2.get(i).getRObjectId()).orElse(new Doc());
              String orgId2 = deptDao.selectOrgIdByCabinetcode(docDataOne.getUCabinetCode());
              List<DocRepeating> docV2 = docDao.selRepeatingOne(docDataOne.getRObjectId());
              idf_Doc = (IDfDocument) adSess.getObject(new DfId(docDataOne.getRObjectId()));
              idf_Doc.setString("u_delete_status", " ");
              idf_Doc.setString("u_update_date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
              idf_Doc.save();
              if(isDelete == true) {
                idf_Doc.setString("u_pr_code", " ");
                idf_Doc.setString("u_pr_type", " ");              
                idf_Doc.save();      
              }
              try {
                insertLog(LogDoc.builder().uJobCode(DocLogItem.RR.getValue()).uDocId(docDataOne.getRObjectId())
                    .uDocKey(docDataOne.getUDocKey()).uDocName(docDataOne.getTitle())
                    .uDocVersion(docV2.get(0).getRVersionLabel()).uOwnDeptcode(orgId2).uActDeptCode(userSession.getUser().getOrgId())
                    .uJobUser(userSession.getDUserId()).uJobUserType(userTypeData).uDocStatus(docDataOne.getUDocStatus())
                    .uSecLevel(docDataOne.getUSecLevel()).uCabinetCode(docDataOne.getUCabinetCode()).uJobGubun("")
                    .uUserIp(ip)
                    .uFileSize(Integer.valueOf(docDataOne.getRContentSize()))
                    .build());
              } catch (Exception ex) {
              }
            }
            idf_PObj.destroy();
          } else {
            // 각 폴더의 하위에 있는 문서 조회 (삭제 이상의 권한이 있는 문서만 조회)
            List<String> folIdList = folderDescendantDtoList.stream().map((item) -> item.getRObjectId())
                .collect(Collectors.toList());
            List<Doc> docAllList = docDao.recycleListByFolIds(folIdList);
            idf_PObj2 = (IDfPersistentObject) idfSession.getObject(new DfId(recycleData.getUObjId()));
            idf_PObj2.setString("u_delete_status", " ");
            idf_PObj2.setString("u_update_date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            idf_PObj2.save();
            if(isDelete == true) {
              idf_PObj2.setString("u_pr_code", " ");
              idf_PObj2.setString("u_pr_type", " ");
              idf_PObj2.setString("u_fol_type", "DFO");
              if(null != moveFolData.getRObjectId()) {
                idf_PObj2.setString("u_up_fol_id", moveFolData.getRObjectId());
              }else {
                idf_PObj2.setString("u_up_fol_id", " ");
              }
              idf_PObj2.save();      
            }
            for (int i = 0; i < folIdList.size(); i++) {
              Folder folDescendData = folderService.selectOne(folIdList.get(i)).orElse(new Folder());
              idf_PObj3 = (IDfPersistentObject) idfSession.getObject(new DfId(folDescendData.getRObjectId()));
              idf_PObj3.setString("u_delete_status", " ");
              idf_PObj3.setString("u_update_date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
              idf_PObj3.save();
              if(isDelete == true) {
                idf_PObj3.setString("u_pr_code", " ");
                idf_PObj3.setString("u_pr_type", " ");
                idf_PObj3.setString("u_fol_type", "DFO");             
                idf_PObj3.save();      
              }
            }
            for (int i = 0; i < docAllList.size(); i++) {
              Doc docDataOne = docService.selectOne(docAllList.get(i).getRObjectId()).orElse(new Doc());
              String orgId2 = deptDao.selectOrgIdByCabinetcode(docDataOne.getUCabinetCode());
              List<DocRepeating> docV2 = docDao.selRepeatingOne(docDataOne.getRObjectId());
              idf_Doc = (IDfDocument) adSess.getObject(new DfId(docDataOne.getRObjectId()));
              idf_Doc.setString("u_delete_status", " ");
              idf_Doc.setString("u_update_date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
              idf_Doc.save();
              if(isDelete == true) {
                idf_Doc.setString("u_pr_code", " ");
                idf_Doc.setString("u_pr_type", " ");              
                idf_Doc.save();      
              }
              LogDoc logDoc = LogDoc.builder().uJobCode(DocLogItem.RR.getValue()).uDocId(docDataOne.getRObjectId())
                    .uDocKey(docDataOne.getUDocKey()).uDocName(docDataOne.getTitle())
                    .uDocVersion(docV2.get(0).getRVersionLabel()).uOwnDeptcode(orgId2).uActDeptCode(userSession.getUser().getOrgId())
                    .uJobUser(userSession.getDUserId()).uJobUserType(userTypeData).uDocStatus(docDataOne.getUDocStatus())
                    .uSecLevel(docDataOne.getUSecLevel()).uCabinetCode(docDataOne.getUCabinetCode()).uJobGubun("")
                    .uUserIp(ip)
                    .uFileSize(Integer.valueOf(docDataOne.getRContentSize()))
                    .build();
              insertLog(logDoc);
            }
            idf_PObj.destroy();
          }

          break;
        case RESEARCH:
          Research research = researchService.selectResearchByURschCode(folData.getUPrCode())
              .orElseThrow(() -> new BadRequestException("존재하지 않는 연구과제 코드입니다."));
          if ("D".equals(research.getUDeleteStatus())) {
//            throw new RuntimeException("연구과제가 삭제되었습니다. 경로를 지정해 주십시오.");     
            System.out.print("상위 폴더가 삭제되었습니다. 경로를 지정해 주십시오.");
          }else {
            Folder upfolData = folderService.selectOne(folData.getUUpFolId()).orElse(new Folder());
            if("D".equals(upfolData.getUDeleteStatus()) || "P".equals(upfolData.getUDeleteStatus()) || upfolData.getRObjectId() == null) {
//              throw new RuntimeException("상위 폴더가 삭제되었습니다. 경로를 지정해 주십시오.");
              System.out.print("상위 폴더가 삭제되었습니다. 경로를 지정해 주십시오.");
            }
          }       
          if (folderDescendantDtoList.size() == 0) {
            idf_PObj2 = (IDfPersistentObject) idfSession.getObject(new DfId(recycleData.getUObjId()));
            idf_PObj2.setString("u_delete_status", " ");
            idf_PObj2.setString("u_update_date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            idf_PObj2.save();
            if(isDelete == true) {
              idf_PObj2.setString("u_pr_code", " ");
              idf_PObj2.setString("u_pr_type", " ");
              idf_PObj2.setString("u_fol_type", "DFO");
              if(null != moveFolData.getRObjectId()) {
                idf_PObj2.setString("u_up_fol_id", moveFolData.getRObjectId());
              }else {
                idf_PObj2.setString("u_up_fol_id", " ");
              }
              idf_PObj2.save();      
            }
            for (int i = 0; i < docAllList2.size(); i++) {
              Doc docDataOne = docService.selectOne(docAllList2.get(i).getRObjectId()).orElse(new Doc());
              String orgId2 = deptDao.selectOrgIdByCabinetcode(docDataOne.getUCabinetCode());
              List<DocRepeating> docV2 = docDao.selRepeatingOne(docDataOne.getRObjectId());
              idf_Doc = (IDfDocument) adSess.getObject(new DfId(docDataOne.getRObjectId()));
              idf_Doc.setString("u_delete_status", " ");
              idf_Doc.setString("u_update_date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
              idf_Doc.save();
              if(isDelete == true) {
                idf_Doc.setString("u_pr_code", " ");
                idf_Doc.setString("u_pr_type", " ");              
                idf_Doc.save();      
              }
              LogDoc logDoc = LogDoc.builder().uJobCode(DocLogItem.RR.getValue()).uDocId(docDataOne.getRObjectId())
                    .uDocKey(docDataOne.getUDocKey()).uDocName(docDataOne.getTitle())
                    .uDocVersion(docV2.get(0).getRVersionLabel()).uOwnDeptcode(orgId2).uActDeptCode(userSession.getUser().getOrgId())
                    .uJobUser(userSession.getDUserId()).uJobUserType(userTypeData).uDocStatus(docDataOne.getUDocStatus())
                    .uSecLevel(docDataOne.getUSecLevel()).uCabinetCode(docDataOne.getUCabinetCode()).uJobGubun("")
                    .uUserIp(ip)
                    .uFileSize(Integer.valueOf(docDataOne.getRContentSize()))
                    .build();
              insertLog(logDoc);
            }
            idf_PObj.destroy();
          } else {
            // 각 폴더의 하위에 있는 문서 조회 (삭제 이상의 권한이 있는 문서만 조회)
            List<String> folIdList = folderDescendantDtoList.stream().map((item) -> item.getRObjectId())
                .collect(Collectors.toList());
            List<Doc> docAllList = docDao.recycleListByFolIds(folIdList);
            idf_PObj2 = (IDfPersistentObject) idfSession.getObject(new DfId(recycleData.getUObjId()));
            idf_PObj2.setString("u_delete_status", " ");
            idf_PObj2.setString("u_update_date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            idf_PObj2.save();
            if(isDelete == true) {
              idf_PObj2.setString("u_pr_code", " ");
              idf_PObj2.setString("u_pr_type", " ");
              idf_PObj2.setString("u_fol_type", "DFO");
              if(null != moveFolData.getRObjectId()) {
                idf_PObj2.setString("u_up_fol_id", moveFolData.getRObjectId());
              }else {
                idf_PObj2.setString("u_up_fol_id", " ");
              }
              idf_PObj2.save();      
            }
            for (int i = 0; i < folIdList.size(); i++) {
              Folder folDescendData = folderService.selectOne(folIdList.get(i)).orElse(new Folder());
              idf_PObj3 = (IDfPersistentObject) idfSession.getObject(new DfId(folDescendData.getRObjectId()));
              idf_PObj3.setString("u_delete_status", " ");
              idf_PObj3.setString("u_update_date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
              idf_PObj3.save();
              if(isDelete == true) {
                idf_PObj3.setString("u_pr_code", " ");
                idf_PObj3.setString("u_pr_type", " ");
                idf_PObj3.setString("u_fol_type", "DFO");             
                idf_PObj3.save();      
              }
            }
            for (int i = 0; i < docAllList.size(); i++) {
              Doc docDataOne = docService.selectOne(docAllList.get(i).getRObjectId()).orElse(new Doc());
              String orgId2 = deptDao.selectOrgIdByCabinetcode(docDataOne.getUCabinetCode());
              List<DocRepeating> docV2 = docDao.selRepeatingOne(docDataOne.getRObjectId());
              idf_Doc = (IDfDocument) adSess.getObject(new DfId(docDataOne.getRObjectId()));
              idf_Doc.setString("u_delete_status", " ");
              idf_Doc.setString("u_update_date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
              idf_Doc.save();
              if(isDelete == true) {
                idf_Doc.setString("u_pr_code", " ");
                idf_Doc.setString("u_pr_type", " ");              
                idf_Doc.save();      
              }
              LogDoc logDoc = LogDoc.builder().uJobCode(DocLogItem.RR.getValue()).uDocId(docDataOne.getRObjectId())
                    .uDocKey(docDataOne.getUDocKey()).uDocName(docDataOne.getTitle())
                    .uDocVersion(docV2.get(0).getRVersionLabel()).uOwnDeptcode(orgId2).uActDeptCode(userSession.getUser().getOrgId())
                    .uJobUser(userSession.getDUserId()).uJobUserType(userTypeData).uDocStatus(docDataOne.getUDocStatus())
                    .uSecLevel(docDataOne.getUSecLevel()).uCabinetCode(docDataOne.getUCabinetCode()).uJobGubun("")
                    .uUserIp(ip)
                    .uFileSize(Integer.valueOf(docDataOne.getRContentSize()))
                    .build();
              insertLog(logDoc);
            }
            idf_PObj.destroy();
          }

          break;
        default:
        }
      }else if(idf_PObj.getString("u_obj_type").equals("P")) {
        Project projectData = projectDao.selectOne(recycleData.getUObjId()).orElse(new Project());
        idf_PObj2 = (IDfPersistentObject) idfSession.getObject(new DfId(recycleData.getUObjId()));
        idf_PObj2.setString("u_delete_status", " ");
        idf_PObj2.setString("u_update_date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        idf_PObj2.save();
 //       projectData.setUDeleteStatus(" ");
        List<Doc> docDataList = docDao.selectListByPrCode(projectData.getUPjtCode());
        for(int i=0;i<docDataList.size();i++) {
          docData = docService.selectOne(docDataList.get(i).getRObjectId()).orElse(new Doc());
          List<DocRepeating> docV2 = docDao.selRepeatingOne(docDataList.get(i).getRObjectId());
          String orgId2 = deptDao.selectOrgIdByCabinetcode(docData.getUCabinetCode());
          idf_Doc = (IDfDocument) adSess.getObject(new DfId(docDataList.get(i).getRObjectId()));
          idf_Doc.setString("u_delete_status", " ");
          idf_Doc.setString("u_update_date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
          idf_Doc.save();
          LogDoc logDoc = LogDoc.builder()
                .uJobCode(DocLogItem.RR.getValue())
                .uDocId(docData.getRObjectId())
                .uDocKey(docData.getUDocKey())
                .uDocName(docData.getTitle())
                .uDocVersion(docV2.get(0).getRVersionLabel())
                .uOwnDeptcode(orgId2)
                .uActDeptCode(userSession.getUser().getOrgId())
                .uJobUser(userSession.getDUserId())
                .uJobUserType(userTypeData)
                .uDocStatus(docData.getUDocStatus())
                .uSecLevel(docData.getUSecLevel())
                .uCabinetCode(docData.getUCabinetCode())
                .uJobGubun("")
                .uUserIp(ip)
                .uFileSize(Integer.valueOf(docData.getRContentSize()))
                .build();
          insertLog(logDoc);
        }
        List<Folder> folDataList = folderDao.selectListByPrCode(projectData.getUPjtCode());
        for(int i=0;i<folDataList.size();i++) {
          idf_PObj4 = (IDfPersistentObject) idfSession.getObject(new DfId(folDataList.get(i).getRObjectId()));
          idf_PObj4.setString("u_delete_status", " ");
          idf_PObj4.setString("u_update_date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
          idf_PObj4.save();
        }
        idf_PObj.destroy();
      }else if(idf_PObj.getString("u_obj_type").equals("R")) {
        Research researchData = researchDao.selectOne(recycleData.getUObjId()).orElse(new Research());
        idf_PObj2 = (IDfPersistentObject) idfSession.getObject(new DfId(recycleData.getUObjId()));
        idf_PObj2.setString("u_delete_status", " ");
        idf_PObj2.setString("u_update_date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        idf_PObj2.save();
        List<Doc> docDataList = docDao.selectListByPrCode(researchData.getURschCode());
        for(int i=0;i<docDataList.size();i++) {
          docData = docService.selectOne(docDataList.get(i).getRObjectId()).orElse(new Doc());
          List<DocRepeating> docV2 = docDao.selRepeatingOne(docDataList.get(i).getRObjectId());
          String orgId2 = deptDao.selectOrgIdByCabinetcode(docData.getUCabinetCode());
          idf_Doc = (IDfDocument) adSess.getObject(new DfId(docDataList.get(i).getRObjectId()));
          idf_Doc.setString("u_delete_status", " ");
          idf_Doc.setString("u_update_date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
          idf_Doc.save();
          LogDoc logDoc = LogDoc.builder()
                .uJobCode(DocLogItem.RR.getValue())
                .uDocId(docData.getRObjectId())
                .uDocKey(docData.getUDocKey())
                .uDocName(docData.getTitle())
                .uDocVersion(docV2.get(0).getRVersionLabel())
                .uOwnDeptcode(orgId2)
                .uActDeptCode(userSession.getUser().getOrgId())
                .uJobUser(userSession.getDUserId())
                .uJobUserType(userTypeData)
                .uDocStatus(docData.getUDocStatus())
                .uSecLevel(docData.getUSecLevel())
                .uCabinetCode(docData.getUCabinetCode())
                .uJobGubun("")
                .uUserIp(ip)
                .uFileSize(Integer.valueOf(docData.getRContentSize()))
                .build();
          insertLog(logDoc);
        }
        List<Folder> folDataList = folderDao.selectListByPrCode(researchData.getURschCode());
        for(int i=0;i<folDataList.size();i++) {
          idf_PObj4 = (IDfPersistentObject) idfSession.getObject(new DfId(folDataList.get(i).getRObjectId()));
          idf_PObj4.setString("u_delete_status", " ");
          idf_PObj4.setString("u_update_date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
          idf_PObj4.save();
        }
        idf_PObj.destroy();
      }
      idfSession.commitTrans();
      adSess.commitTrans();
    } catch (Exception e) {
      throw e;
    } finally {
      if (idfSession != null) {
        if (idfSession.isTransactionActive()) {
          idfSession.abortTrans();
        }
        if (adSess.isTransactionActive()) {
          adSess.abortTrans();         
        }

        if (idfSession.isConnected()) {
          sessionRelease(userSession.getUser().getUserId(), idfSession);
          adSess.disconnect();
        }
      }
    }
    return "";
  }

  @Override
  public Map<String, Integer> restoreAllDataByIds(UserSession userSession, List<String> retoreAllList, String ip)
      throws Exception {
    IDfSession idfSession = this.getIdfSession(userSession);
    IDfPersistentObject idf_PObj = null;
    IDfPersistentObject idf_PObj2 = null;
    IDfPersistentObject idf_PObj3 = null;
    IDfPersistentObject idf_PObj4 = null;
    IDfSession adSess = null;
    IDfDocument idf_Doc = null;
    Recycle recycleData = null;
    Doc docData = null;
    Folder folData = null;
    List<DocRepeating> docV = null;
    int successCnt = 0;
    int failCnt = 0;
    idfSession.beginTrans();
    adSess = DCTMUtils.getAdminSession();
    if (!adSess.isTransactionActive()) {
     adSess.beginTrans();
    }
   
    try {
      if (idfSession == null || !idfSession.isConnected()) {
        throw new Exception("DCTM Session 가져오기 실패");
      }

      for (String objId : retoreAllList) {
       
        try {
          idf_PObj = (IDfPersistentObject) idfSession.getObject(new DfId(objId));
          recycleData = recycleDao.oneRecycleById(objId);
          docData = docService.selectOne(recycleData.getUObjId()).orElse(new Doc());
          folData = folderService.selectOne(recycleData.getUObjId()).orElse(new Folder());
          LocalDateTime today = LocalDateTime.now();
          Project projectData = projectDao.selectOne(recycleData.getUObjId()).orElse(new Project());  
          Research researchData = researchDao.selectOne(recycleData.getUObjId()).orElse(new Research());
          List<Doc> docAllList2 = docDao.recycleListForFolderId(folData.getRObjectId());
          if (FolderStatus.LOCK.getValue().equals(folData.getUFolStatus())) {
            throw new BadRequestException("복원할 위치에 권한이 없습니다.");
          }

          if (idf_PObj.getString("u_obj_type").equals("D")) {
            docV = docDao.selRepeatingOne(docData.getRObjectId());
            String orgId = deptDao.selectOrgIdByCabinetcode(recycleData.getUCabinetCode());
            HamInfoResult hamInfo = commonAuthDao.selectHamInfo(orgId)
                .orElseThrow(() -> new BadRequestException("존재하지 않는 dataId 입니다. (dataId: " + objId + ")"));
            HamType type = HamType.findByValue(hamInfo.getHamType());
            switch (type) {
            case DEPT:
            case COMPANY:
            case COMPANY_M:
              idf_Doc = (IDfDocument) adSess.getObject(new DfId(recycleData.getUObjId()));
              idf_Doc.setString("u_delete_status", " ");
              idf_Doc.setString("u_update_date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
              idf_Doc.save();
              idf_PObj.destroy();
              break;
            case PROJECT:
              Project project = projectService.selectProjectByUPjtCode(docData.getUPrCode())
                  .orElseThrow(() -> new BadRequestException("존재하지 않는 프로젝트 코드입니다."));
              if ("D".equals(project.getUDeleteStatus())) {
                idf_PObj3 = (IDfPersistentObject) idfSession.getObject(new DfId(project.getRObjectId()));
                idf_PObj3.setString("u_delete_status", " ");
                idf_PObj3.setString("u_update_date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                idf_PObj3.save();
                idf_Doc = (IDfDocument) adSess.getObject(new DfId(recycleData.getUObjId()));
                idf_Doc.setString("u_delete_status", " ");
                idf_Doc.setString("u_update_date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                idf_Doc.save();
                idf_PObj.destroy();
              } else {
                idf_PObj2 = (IDfPersistentObject) idfSession.getObject(new DfId(recycleData.getUObjId()));
                idf_PObj2.setString("u_delete_status", " ");
                idf_PObj2.setString("u_update_date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                idf_PObj2.save();
                idf_PObj.destroy();
              }
              break;
            case RESEARCH:
              Research research = researchService.selectResearchByURschCode(docData.getUPrCode())
                  .orElseThrow(() -> new BadRequestException("존재하지 않는 연구과제 코드입니다."));
              if ("D".equals(research.getUDeleteStatus())) {
                idf_PObj3 = (IDfPersistentObject) idfSession.getObject(new DfId(research.getRObjectId()));
                idf_PObj3.setString("u_delete_status", " ");
                idf_PObj3.setString("u_update_date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                idf_PObj3.save();
                idf_Doc = (IDfDocument) adSess.getObject(new DfId(recycleData.getUObjId()));
                idf_Doc.setString("u_delete_status", " ");
                idf_Doc.setString("u_update_date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                idf_Doc.save();
                idf_PObj.destroy();
              } else {
                idf_Doc = (IDfDocument) adSess.getObject(new DfId(recycleData.getUObjId()));
                idf_Doc.setString("u_delete_status", " ");
                idf_Doc.setString("u_update_date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                idf_Doc.save();
                idf_PObj.destroy();
              }
              break;
            default:
            }
            LogDoc logDoc = LogDoc.builder().uJobCode(DocLogItem.RR.getValue()).uDocId(docData.getRObjectId())
                  .uDocKey(docData.getUDocKey()).uDocName(docData.getTitle())
                  .uDocVersion(docV.get(0).getRVersionLabel()).uOwnDeptcode(orgId).uActDeptCode(orgId)
                  .uJobUser(userSession.getDUserId()).uJobUserType("P").uDocStatus(docData.getUDocStatus())
                  .uSecLevel(docData.getUSecLevel()).uCabinetCode(docData.getUCabinetCode()).uJobGubun("").uUserIp(ip)
                  .uFileSize(Integer.valueOf(docData.getRContentSize()))
                  .build();
            insertLog(logDoc);
            // 폴더의 경우 하위에 DELETE 권한이 없는 자료가 있을 경우 해당 자료는 남기고 나머지만 복원
          } else if (idf_PObj.getString("u_obj_type").equals("F")) {
            
            List<FolderDescendantDto> folderDescendantDtoList = folderDao.selectDescendantsAll(recycleData.getUObjId());
            folData = folderService.selectOne(recycleData.getUObjId()).orElse(new Folder());
            String orgId = deptDao.selectOrgIdByCabinetcode(recycleData.getUCabinetCode());
            HamInfoResult hamInfo = commonAuthDao.selectHamInfo(orgId)
                .orElseThrow(() -> new BadRequestException("존재하지 않는 dataId 입니다. (dataId: " + objId + ")"));
            HamType type = HamType.findByValue(hamInfo.getHamType());
            switch (type) {
            case DEPT:
            case COMPANY:
            case COMPANY_M:
              if (folderDescendantDtoList.size() == 0) {
                idf_PObj2 = (IDfPersistentObject) idfSession.getObject(new DfId(recycleData.getUObjId()));
                idf_PObj2.setString("u_delete_status", " ");
                idf_PObj2.save();
                for (int i = 0; i < docAllList2.size(); i++) {
                  Doc docDataOne = docService.selectOne(docAllList2.get(i).getRObjectId()).orElse(new Doc());
                  String orgId2 = deptDao.selectOrgIdByCabinetcode(docDataOne.getUCabinetCode());
                  List<DocRepeating> docV2 = docDao.selRepeatingOne(docDataOne.getRObjectId());
                  idf_Doc = (IDfDocument) adSess.getObject(new DfId(docDataOne.getRObjectId()));
                  idf_Doc.setString("u_delete_status", " ");
                  idf_Doc.setString("u_update_date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                  idf_Doc.save();
                  LogDoc logDoc = LogDoc.builder().uJobCode(DocLogItem.RR.getValue()).uDocId(docDataOne.getRObjectId())
                        .uDocKey(docDataOne.getUDocKey()).uDocName(docDataOne.getTitle())
                        .uDocVersion(docV2.get(0).getRVersionLabel()).uOwnDeptcode(orgId2).uActDeptCode(orgId2)
                        .uJobUser(userSession.getDUserId()).uJobUserType("P").uDocStatus(docDataOne.getUDocStatus())
                        .uSecLevel(docDataOne.getUSecLevel()).uCabinetCode(docDataOne.getUCabinetCode()).uJobGubun("")
                        .uUserIp("")
                        .uFileSize(Integer.valueOf(docDataOne.getRContentSize()))
                        .build();
                    insertLog(logDoc);
                }
                idf_PObj.destroy();
              } else {
                // 각 폴더의 하위에 있는 문서 조회 (삭제 이상의 권한이 있는 문서만 조회)
                List<String> folIdList = folderDescendantDtoList.stream().map((item) -> item.getRObjectId())
                    .collect(Collectors.toList());
                List<Doc> docAllList = docDao.recycleListByFolIds(folIdList);
                idf_PObj2 = (IDfPersistentObject) idfSession.getObject(new DfId(recycleData.getUObjId()));
                idf_PObj2.setString("u_delete_status", " ");
                idf_PObj2.setString("u_update_date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                idf_PObj2.save();
                for (int i = 0; i < folIdList.size(); i++) {
                  Folder folDescendData = folderService.selectOne(folIdList.get(i)).orElse(new Folder());
                  idf_PObj3 = (IDfPersistentObject) idfSession.getObject(new DfId(folDescendData.getRObjectId()));
                  idf_PObj3.setString("u_delete_status", " ");
                  idf_PObj3.setString("u_update_date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                  idf_PObj3.save();
                }
                for (int i = 0; i < docAllList.size(); i++) {
                  Doc docDataOne = docService.selectOne(docAllList.get(i).getRObjectId()).orElse(new Doc());
                  String orgId2 = deptDao.selectOrgIdByCabinetcode(docDataOne.getUCabinetCode());
                  List<DocRepeating> docV2 = docDao.selRepeatingOne(docDataOne.getRObjectId());
                  idf_Doc = (IDfDocument) adSess.getObject(new DfId(docDataOne.getRObjectId()));
                  idf_Doc.setString("u_delete_status", " ");
                  idf_Doc.setString("u_update_date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                  idf_Doc.save();
                  LogDoc logDoc = LogDoc.builder().uJobCode(DocLogItem.RR.getValue()).uDocId(docDataOne.getRObjectId())
                        .uDocKey(docDataOne.getUDocKey()).uDocName(docDataOne.getTitle())
                        .uDocVersion(docV2.get(0).getRVersionLabel()).uOwnDeptcode(orgId2).uActDeptCode(orgId2)
                        .uJobUser(userSession.getDUserId()).uJobUserType("P").uDocStatus(docDataOne.getUDocStatus())
                        .uSecLevel(docDataOne.getUSecLevel()).uCabinetCode(docDataOne.getUCabinetCode()).uJobGubun("")
                        .uUserIp(ip)
                        .uFileSize(Integer.valueOf(docDataOne.getRContentSize()))
                        .build();
                  insertLog(logDoc);
                }
                idf_PObj.destroy();
              }
              break;
            case PROJECT:
              Project project = projectService.selectProjectByUPjtCode(folData.getUPrCode())
                  .orElseThrow(() -> new BadRequestException("존재하지 않는 프로젝트 코드입니다."));
              if ("D".equals(project.getUDeleteStatus())) {
                idf_PObj4 = (IDfPersistentObject) idfSession.getObject(new DfId(project.getRObjectId()));
                idf_PObj4.setString("u_delete_status", " ");
                idf_PObj4.setString("u_update_date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                idf_PObj4.save();
              }
              if (folderDescendantDtoList.size() == 0) {
                idf_PObj2 = (IDfPersistentObject) idfSession.getObject(new DfId(recycleData.getUObjId()));
                idf_PObj2.setString("u_delete_status", " ");
                idf_PObj2.setString("u_update_date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                idf_PObj2.save();
                for (int i = 0; i < docAllList2.size(); i++) {
                  Doc docDataOne = docService.selectOne(docAllList2.get(i).getRObjectId()).orElse(new Doc());
                  String orgId2 = deptDao.selectOrgIdByCabinetcode(docDataOne.getUCabinetCode());
                  List<DocRepeating> docV2 = docDao.selRepeatingOne(docDataOne.getRObjectId());
                  idf_Doc = (IDfDocument) adSess.getObject(new DfId(docDataOne.getRObjectId()));
                  idf_Doc.setString("u_delete_status", " ");
                  idf_Doc.setString("u_update_date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                  idf_Doc.save();
                  LogDoc logDoc = LogDoc.builder().uJobCode(DocLogItem.RR.getValue()).uDocId(docDataOne.getRObjectId())
                        .uDocKey(docDataOne.getUDocKey()).uDocName(docDataOne.getTitle())
                        .uDocVersion(docV2.get(0).getRVersionLabel()).uOwnDeptcode(orgId2).uActDeptCode(orgId2)
                        .uJobUser(userSession.getDUserId()).uJobUserType("P").uDocStatus(docDataOne.getUDocStatus())
                        .uSecLevel(docDataOne.getUSecLevel()).uCabinetCode(docDataOne.getUCabinetCode()).uJobGubun("")
                        .uUserIp(ip)
                        .uFileSize(Integer.valueOf(docDataOne.getRContentSize()))
                        .build();
                  insertLog(logDoc);
                }
                idf_PObj.destroy();
              } else {
                // 각 폴더의 하위에 있는 문서 조회 (삭제 이상의 권한이 있는 문서만 조회)
                List<String> folIdList = folderDescendantDtoList.stream().map((item) -> item.getRObjectId())
                    .collect(Collectors.toList());
                List<Doc> docAllList = docDao.recycleListByFolIds(folIdList);
                idf_PObj2 = (IDfPersistentObject) idfSession.getObject(new DfId(recycleData.getUObjId()));
                idf_PObj2.setString("u_delete_status", " ");
                idf_PObj2.setString("u_update_date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                idf_PObj2.save();
                for (int i = 0; i < folIdList.size(); i++) {
                  Folder folDescendData = folderService.selectOne(folIdList.get(i)).orElse(new Folder());
                  idf_PObj3 = (IDfPersistentObject) idfSession.getObject(new DfId(folDescendData.getRObjectId()));
                  idf_PObj3.setString("u_delete_status", " ");
                  idf_PObj3.setString("u_update_date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                  idf_PObj3.save();
                }
                for (int i = 0; i < docAllList.size(); i++) {
                  Doc docDataOne = docService.selectOne(docAllList.get(i).getRObjectId()).orElse(new Doc());
                  String orgId2 = deptDao.selectOrgIdByCabinetcode(docDataOne.getUCabinetCode());
                  List<DocRepeating> docV2 = docDao.selRepeatingOne(docDataOne.getRObjectId());
                  idf_Doc = (IDfDocument) adSess.getObject(new DfId(docDataOne.getRObjectId()));
                  idf_Doc.setString("u_delete_status", " ");
                  idf_Doc.setString("u_update_date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                  idf_Doc.save();
                  LogDoc logDoc = LogDoc.builder().uJobCode(DocLogItem.RR.getValue()).uDocId(docDataOne.getRObjectId())
                        .uDocKey(docDataOne.getUDocKey()).uDocName(docDataOne.getTitle())
                        .uDocVersion(docV2.get(0).getRVersionLabel()).uOwnDeptcode(orgId2).uActDeptCode(orgId2)
                        .uJobUser(userSession.getDUserId()).uJobUserType("P").uDocStatus(docDataOne.getUDocStatus())
                        .uSecLevel(docDataOne.getUSecLevel()).uCabinetCode(docDataOne.getUCabinetCode()).uJobGubun("")
                        .uUserIp(ip)
                        .uFileSize(Integer.valueOf(docDataOne.getRContentSize()))
                        .build();
                  insertLog(logDoc);
                }
                idf_PObj.destroy();
              }

              break;
            case RESEARCH:
              Research research = researchService.selectResearchByURschCode(folData.getUPrCode())
                  .orElseThrow(() -> new BadRequestException("존재하지 않는 연구과제 코드입니다."));
              if ("D".equals(research.getUDeleteStatus())) {
                idf_PObj3 = (IDfPersistentObject) idfSession.getObject(new DfId(research.getRObjectId()));
                idf_PObj3.setString("u_delete_status", " ");
                idf_PObj3.setString("u_update_date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                idf_PObj3.save();
              }
              if (folderDescendantDtoList.size() == 0) {
                idf_PObj2 = (IDfPersistentObject) idfSession.getObject(new DfId(recycleData.getUObjId()));
                idf_PObj2.setString("u_delete_status", " ");
                idf_PObj2.setString("u_update_date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                idf_PObj2.save();
                for (int i = 0; i < docAllList2.size(); i++) {
                  Doc docDataOne = docService.selectOne(docAllList2.get(i).getRObjectId()).orElse(new Doc());
                  String orgId2 = deptDao.selectOrgIdByCabinetcode(docDataOne.getUCabinetCode());
                  List<DocRepeating> docV2 = docDao.selRepeatingOne(docDataOne.getRObjectId());
                  idf_Doc = (IDfDocument) adSess.getObject(new DfId(docDataOne.getRObjectId()));
                  idf_Doc.setString("u_delete_status", " ");
                  idf_Doc.setString("u_update_date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                  idf_Doc.save();
                  LogDoc logDoc = LogDoc.builder().uJobCode(DocLogItem.RR.getValue()).uDocId(docDataOne.getRObjectId())
                        .uDocKey(docDataOne.getUDocKey()).uDocName(docDataOne.getTitle())
                        .uDocVersion(docV2.get(0).getRVersionLabel()).uOwnDeptcode(orgId2).uActDeptCode(orgId2)
                        .uJobUser(userSession.getDUserId()).uJobUserType("P").uDocStatus(docDataOne.getUDocStatus())
                        .uSecLevel(docDataOne.getUSecLevel()).uCabinetCode(docDataOne.getUCabinetCode()).uJobGubun("")
                        .uUserIp(ip)
                        .uFileSize(Integer.valueOf(docDataOne.getRContentSize()))
                        .build();
                  insertLog(logDoc);
                }
                idf_PObj.destroy();
              } else {
                // 각 폴더의 하위에 있는 문서 조회 (삭제 이상의 권한이 있는 문서만 조회)
                List<String> folIdList = folderDescendantDtoList.stream().map((item) -> item.getRObjectId())
                    .collect(Collectors.toList());
                List<Doc> docAllList = docDao.recycleListByFolIds(folIdList);
                idf_PObj2 = (IDfPersistentObject) idfSession.getObject(new DfId(recycleData.getUObjId()));
                idf_PObj2.setString("u_delete_status", " ");
                idf_PObj2.setString("u_update_date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                idf_PObj2.save();
                for (int i = 0; i < folIdList.size(); i++) {
                  Folder folDescendData = folderService.selectOne(folIdList.get(i)).orElse(new Folder());
                  idf_PObj3 = (IDfPersistentObject) idfSession.getObject(new DfId(folDescendData.getRObjectId()));
                  idf_PObj3.setString("u_delete_status", " ");
                  idf_PObj3.setString("u_update_date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                  idf_PObj3.save();
                }
                for (int i = 0; i < docAllList.size(); i++) {
                  Doc docDataOne = docService.selectOne(docAllList.get(i).getRObjectId()).orElse(new Doc());
                  String orgId2 = deptDao.selectOrgIdByCabinetcode(docDataOne.getUCabinetCode());
                  List<DocRepeating> docV2 = docDao.selRepeatingOne(docDataOne.getRObjectId());
                  idf_Doc = (IDfDocument) adSess.getObject(new DfId(docDataOne.getRObjectId()));
                  idf_Doc.setString("u_delete_status", " ");
                  idf_Doc.setString("u_update_date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                  idf_Doc.save();
                  LogDoc logDoc = LogDoc.builder().uJobCode(DocLogItem.RR.getValue()).uDocId(docDataOne.getRObjectId())
                        .uDocKey(docDataOne.getUDocKey()).uDocName(docDataOne.getTitle())
                        .uDocVersion(docV2.get(0).getRVersionLabel()).uOwnDeptcode(orgId2).uActDeptCode(orgId2)
                        .uJobUser(userSession.getDUserId()).uJobUserType("P").uDocStatus(docDataOne.getUDocStatus())
                        .uSecLevel(docDataOne.getUSecLevel()).uCabinetCode(docDataOne.getUCabinetCode()).uJobGubun("")
                        .uUserIp(ip)
                        .uFileSize(Integer.valueOf(docDataOne.getRContentSize()))
                        .build();
                  insertLog(logDoc);
                }
                idf_PObj.destroy();
              }

              break;
            default:
            }

          }else if(idf_PObj.getString("u_obj_type").equals("P")) {
            idf_PObj2 = (IDfPersistentObject) idfSession.getObject(new DfId(recycleData.getUObjId()));
            idf_PObj2.setString("u_delete_status", " ");
            idf_PObj2.save();
     //       projectData.setUDeleteStatus(" ");
            List<Doc> docDataList = docDao.selectListByPrCode(projectData.getUPjtCode());
            for(int i=0;i<docDataList.size();i++) {
              docData = docService.selectOne(docDataList.get(i).getRObjectId()).orElse(new Doc());
              List<DocRepeating> docV2 = docDao.selRepeatingOne(docDataList.get(i).getRObjectId());
              String orgId2 = deptDao.selectOrgIdByCabinetcode(docData.getUCabinetCode());
              idf_Doc = (IDfDocument) adSess.getObject(new DfId(docDataList.get(i).getRObjectId()));
              idf_Doc.setString("u_delete_status", " ");
              idf_Doc.setString("u_update_date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
              idf_Doc.save();
              LogDoc logDoc = LogDoc.builder()
                    .uJobCode(DocLogItem.RR.getValue())
                    .uDocId(docData.getRObjectId())
                    .uDocKey(docData.getUDocKey())
                    .uDocName(docData.getTitle())
                    .uDocVersion(docV2.get(0).getRVersionLabel())
                    .uOwnDeptcode(orgId2)
                    .uActDeptCode(orgId2)
                    .uJobUser(userSession.getDUserId())
                    .uJobUserType("P")
                    .uDocStatus(docData.getUDocStatus())
                    .uSecLevel(docData.getUSecLevel())
                    .uCabinetCode(docData.getUCabinetCode())
                    .uJobGubun("")
                    .uUserIp(ip)
                    .uFileSize(Integer.valueOf(docData.getRContentSize()))
                    .build();
              insertLog(logDoc);
            }
            List<Folder> folDataList = folderDao.selectListByPrCode(projectData.getUPjtCode());
            for(int i=0;i<folDataList.size();i++) {
              idf_PObj4 = (IDfPersistentObject) idfSession.getObject(new DfId(folDataList.get(i).getRObjectId()));
              idf_PObj4.setString("u_delete_status", " ");
              idf_PObj4.setString("u_update_date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
              idf_PObj4.save();
            }
            idf_PObj.destroy();
          }else if(idf_PObj.getString("u_obj_type").equals("R")) {
            idf_PObj2 = (IDfPersistentObject) idfSession.getObject(new DfId(recycleData.getUObjId()));
            idf_PObj2.setString("u_delete_status", " ");
            idf_PObj2.setString("u_update_date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            idf_PObj2.save();
            List<Doc> docDataList = docDao.selectListByPrCode(researchData.getURschCode());
            for(int i=0;i<docDataList.size();i++) {
              docData = docService.selectOne(docDataList.get(i).getRObjectId()).orElse(new Doc());
              List<DocRepeating> docV2 = docDao.selRepeatingOne(docDataList.get(i).getRObjectId());
              String orgId2 = deptDao.selectOrgIdByCabinetcode(docData.getUCabinetCode());
              idf_Doc = (IDfDocument) adSess.getObject(new DfId(docDataList.get(i).getRObjectId()));
              idf_Doc.setString("u_delete_status", " ");
              idf_Doc.setString("u_update_date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
              idf_Doc.save();
              LogDoc logDoc = LogDoc.builder()
                    .uJobCode(DocLogItem.RR.getValue())
                    .uDocId(docData.getRObjectId())
                    .uDocKey(docData.getUDocKey())
                    .uDocName(docData.getTitle())
                    .uDocVersion(docV2.get(0).getRVersionLabel())
                    .uOwnDeptcode(orgId2)
                    .uActDeptCode(orgId2)
                    .uJobUser(userSession.getDUserId())
                    .uJobUserType("P")
                    .uDocStatus(docData.getUDocStatus())
                    .uSecLevel(docData.getUSecLevel())
                    .uCabinetCode(docData.getUCabinetCode())
                    .uJobGubun("")
                    .uUserIp(ip)
                    .uFileSize(Integer.valueOf(docData.getRContentSize()))
                    .build();
              insertLog(logDoc);
            }
            List<Folder> folDataList = folderDao.selectListByPrCode(researchData.getURschCode());
            for(int i=0;i<folDataList.size();i++) {
              idf_PObj4 = (IDfPersistentObject) idfSession.getObject(new DfId(folDataList.get(i).getRObjectId()));
              idf_PObj4.setString("u_delete_status", " ");
              idf_PObj4.setString("u_update_date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
              idf_PObj4.save();
            }
            idf_PObj.destroy();
          }
      
          if ((" ".equals(folData.getUDeleteStatus()) || folData.getUDeleteStatus() == null) || (" ".equals(docData.getUDeleteStatus()) || docData.getUDeleteStatus() == null)
              || (" ".equals(projectData.getUDeleteStatus()) || projectData.getUDeleteStatus() == null) || (" ".equals(researchData.getUDeleteStatus())|| researchData.getUDeleteStatus() == null)) {
            successCnt++;
  
          } else {
            failCnt++;
            continue;
          }

        } catch (Exception e) {
          failCnt++;
          continue;
        } finally {
        }
      }
      idfSession.commitTrans();
      adSess.commitTrans();
    } catch (Exception e) {
      throw e;
    } finally {
      if (idfSession != null) {
        if (idfSession.isTransactionActive()) {
          idfSession.abortTrans();
        }
        if (adSess!=null && adSess.isTransactionActive()) {
          adSess.abortTrans();         
          adSess.disconnect();
        }
        if (idfSession.isConnected()) {
          sessionRelease(userSession.getUser().getUserId(), idfSession);          
        }
      }
    }

    Map<String, Integer> result = new HashMap<String, Integer>();
    result.put("success", successCnt);
    result.put("fail", failCnt);

    return result;
  }

  @Override
  public String deleteData(UserSession userSession, String dataId, String ip, DeleteManageDto dto) throws Exception {
    IDfSession idfSession = null;
    IDfPersistentObject idf_PObj = null;
    IDfDocument idf_Doc2 = null;
    IDfPersistentObject idf_PObj3 = null;
    IDfPersistentObject idf_PObj4 = null;
    IDfPersistentObject idf_PObj5 = null;
    IDfPersistentObject idf_PObj6 = null;
    IDfSession adSess = null;
    Recycle recycleData = recycleDao.oneRecycleById(dataId);
    String reqDeptCode = deptDao.selectOrgIdByCabinetcode(recycleData.getUCabinetCode());
    List<DeptMgrs> reqDeptMgr = deptMgrDao.selectByDeptCode(reqDeptCode);
    VDept deptData = deptDao.selectOneByOrgId(reqDeptCode).orElse(new VDept());
    try {
      idfSession = this.getIdfSession(userSession);
      idfSession.beginTrans();
      adSess = DCTMUtils.getAdminSession();            
      if (!adSess.isTransactionActive()) {
        adSess.beginTrans();
      }
      
      String userTypeData = null;
      if (dto == null || dto.getUserType().equals("")) {
        userTypeData = "D";
      } else if (dto.getUserType().equals("DKG")) {
        userTypeData = "G";      
      } else {
        userTypeData = "C";
      }
      
      idf_PObj = (IDfPersistentObject) idfSession.getObject(new DfId(dataId));
      // 삭제자가 현재 로그인한 사람과 동일한지와 팀장인지 여부 판단 해야함. // chiefFlag ==> 팀장여부??
     
        if (idf_PObj.getString("u_obj_type").equals("D")) {
          Doc docData = docService.selectOne(recycleData.getUObjId()).orElse(new Doc());
          String objId = deptDao.selectOrgIdByCabinetcode(docData.getUCabinetCode());
          List<DocRepeating> docV = docDao.selRepeatingOne(docData.getRObjectId());
          List<String> mgrId = new ArrayList<>();
          for (int i = 0; i < reqDeptMgr.size(); i++) {
           if(reqDeptMgr.get(i).getUUserId().contains(userSession.getDUserId())) {
             mgrId.add(reqDeptMgr.get(i).getUUserId());
             break; 
           } 
          }
          if ((mgrId.contains(userSession.getDUserId()) || userSession.getDUserId().equals(deptData.getManagerPerId()))
              || userSession.getUser().getMgr().getGroupComCode() != null 
              || (userSession.getUser().getMgr().getCompanyComCode() != null && userSession.getUser().getMgr().getCompanyComCode().equals(deptData.getComOrgId()))) {
            LogDoc logDoc = LogDoc.builder()
                    .uJobCode(DocLogItem.LP.getValue())
                    .uDocId(docData.getRObjectId())
                    .uDocKey(docData.getUDocKey())
                    .uDocName(docData.getTitle())
                    .uDocVersion(docV.get(0).getRVersionLabel())
                    .uOwnDeptcode(objId)
                    .uActDeptCode(userSession.getUser().getOrgId())
                    .uJobUser(userSession.getDUserId())
                    .uJobUserType(userTypeData)
                    .uDocStatus(docData.getUDocStatus())
                    .uSecLevel(docData.getUSecLevel())
                    .uCabinetCode(docData.getUCabinetCode())
                    .uJobGubun("")
                    .uUserIp(ip)
                    .uFileSize(Integer.valueOf(docData.getRContentSize()))
                    .build();          
            insertLog(logDoc);
            idf_Doc2 = (IDfDocument) adSess.getObject(new DfId(docData.getRObjectId()));
            idf_Doc2.destroy();
            idf_PObj.destroy();
            String s_Dql = "delete edms_auth_base object " +
                " where u_obj_id = '" + docData.getUDocKey() + "' ";
            
            IDfCollection idf_Col = DCTMUtils.getCollectionByDQL(adSess, s_Dql, DfQuery.DF_QUERY);
            if(idf_Col != null) idf_Col.close();
          } 
           else {
            throw new Exception("권한이 없습니다.");
          }
          
        } else if (idf_PObj.getString("u_obj_type").equals("F")) {
          List<String> mgrId = new ArrayList<>();
          for (int i = 0; i < reqDeptMgr.size(); i++) {
           if(reqDeptMgr.get(i).getUUserId().contains(userSession.getDUserId())) {
             mgrId.add(reqDeptMgr.get(i).getUUserId());
             break; 
           } 
          }
          if ((mgrId.contains(userSession.getDUserId()) || userSession.getDUserId().equals(deptData.getManagerPerId()))
              || userSession.getUser().getMgr().getGroupComCode() != null
              || (userSession.getUser().getMgr().getCompanyComCode() != null && userSession.getUser().getMgr().getCompanyComCode().equals(deptData.getComOrgId()))) {
            List<FolderDescendantDto> folderDescendantDtoList = folderDao.selectDescendantsAll(recycleData.getUObjId());
           
            List<Doc> docAllList2 = docDao.recycleListForFolderId(recycleData.getUObjId());
            if (folderDescendantDtoList.size() == 0) {
              for (int q = 0; q < docAllList2.size(); q++) {
                Doc docDataOne = docService.selectOne(docAllList2.get(q).getRObjectId()).orElse(new Doc());
                String orgId2 = deptDao.selectOrgIdByCabinetcode(docDataOne.getUCabinetCode());
                List<DocRepeating> docV = docDao.selRepeatingOne(docDataOne.getRObjectId());
                LogDoc logDoc = LogDoc.builder()
                        .uJobCode(DocLogItem.LP.getValue())
                        .uDocId(docDataOne.getRObjectId())
                        .uDocKey(docDataOne.getUDocKey())
                        .uDocName(docDataOne.getTitle())
                        .uDocVersion(docV.get(0).getRVersionLabel())
                        .uOwnDeptcode(orgId2)
                        .uActDeptCode(userSession.getUser().getOrgId())
                        .uJobUser(userSession.getDUserId())
                        .uJobUserType(userTypeData)
                        .uDocStatus(docDataOne.getUDocStatus())
                        .uSecLevel(docDataOne.getUSecLevel())
                        .uCabinetCode(docDataOne.getUCabinetCode())
                        .uJobGubun("")
                        .uUserIp(ip)
                        .uFileSize(Integer.valueOf(docDataOne.getRContentSize()))
                        .build();                 
                insertLog(logDoc);
                idf_Doc2 = (IDfDocument) adSess.getObject(new DfId(docAllList2.get(q).getRObjectId()));
                idf_Doc2.destroy();
                String s_Dql = "delete edms_auth_base object " +
                    " where u_obj_id = '" + docAllList2.get(q).getUDocKey() + "' ";
                
                IDfCollection idf_Col = DCTMUtils.getCollectionByDQL(adSess, s_Dql, DfQuery.DF_QUERY);
                if(idf_Col != null) idf_Col.close();
                
              }
              idf_PObj5 = (IDfPersistentObject) idfSession.getObject(new DfId(recycleData.getUObjId()));
              idf_PObj5.destroy();
              
              String s_Dql2 = "delete edms_auth_base object " +
                  " where u_obj_id = '" + recycleData.getUObjId() + "' ";
              
              IDfCollection idf_Col2 = DCTMUtils.getCollectionByDQL(adSess, s_Dql2, DfQuery.DF_QUERY);
              if(idf_Col2 != null) idf_Col2.close();
             
              String s_Dql3 = "delete edms_auth_share object " +
                  " where u_obj_id = '" + recycleData.getUObjId() + "' ";
              
              IDfCollection idf_Col3 = DCTMUtils.getCollectionByDQL(adSess, s_Dql3, DfQuery.DF_QUERY);
              if(idf_Col3 != null) idf_Col3.close();
              
              idf_PObj.destroy();
              
              
            } else {
              List<String> folIdList = folderDescendantDtoList.stream().map((item) -> item.getRObjectId())
                  .collect(Collectors.toList());
              List<Doc> docAllList = docDao.recycleListByFolIds(folIdList);
              for (int q = 0; q < docAllList.size(); q++) {
                Doc docDataOne = docService.selectOne(docAllList.get(q).getRObjectId()).orElse(new Doc());
                String orgId2 = deptDao.selectOrgIdByCabinetcode(docDataOne.getUCabinetCode());
                List<DocRepeating> docV = docDao.selRepeatingOne(docDataOne.getRObjectId());
                LogDoc logDoc = LogDoc.builder()
                        .uJobCode(DocLogItem.LP.getValue())
                        .uDocId(docDataOne.getRObjectId())
                        .uDocKey(docDataOne.getUDocKey())
                        .uDocName(docDataOne.getTitle())
                        .uDocVersion(docV.get(0).getRVersionLabel())
                        .uOwnDeptcode(orgId2).uActDeptCode(orgId2)
                        .uJobUser(userSession.getDUserId())
                        .uJobUserType(userTypeData)
                        .uDocStatus(docDataOne.getUDocStatus())
                        .uSecLevel(docDataOne.getUSecLevel())
                        .uCabinetCode(docDataOne.getUCabinetCode())
                        .uJobGubun("")
                        .uUserIp(ip)
                        .uFileSize(Integer.valueOf(docDataOne.getRContentSize()))
                        .build();                                   
                insertLog(logDoc);
                idf_Doc2 = (IDfDocument) adSess.getObject(new DfId(docAllList.get(q).getRObjectId()));
                idf_Doc2.destroy();
                String s_Dql = "delete edms_auth_base object " +
                    " where u_obj_id = '" + docAllList.get(q).getUDocKey() + "' ";
                
                IDfCollection idf_Col = DCTMUtils.getCollectionByDQL(adSess, s_Dql, DfQuery.DF_QUERY);
                if(idf_Col != null) idf_Col.close();
                
              }
              for (int a=0;a<folIdList.size();a++) {
                idf_PObj5 = (IDfPersistentObject) idfSession.getObject(new DfId(folIdList.get(a)));
                idf_PObj5.destroy();
                String s_Dql2 = "delete edms_auth_base object " +
                    " where u_obj_id = '" + folIdList.get(a) + "' ";
                
                IDfCollection idf_Col2 = DCTMUtils.getCollectionByDQL(adSess, s_Dql2, DfQuery.DF_QUERY);
                if(idf_Col2 != null) idf_Col2.close();
               
                String s_Dql3 = "delete edms_auth_share object " +
                    " where u_obj_id = '" + folIdList.get(a) + "' ";
                
                IDfCollection idf_Col3 = DCTMUtils.getCollectionByDQL(adSess, s_Dql3, DfQuery.DF_QUERY);
                if(idf_Col3 != null) idf_Col3.close();
              }
              idf_PObj.destroy();
            }

          } else {
            throw new Exception("권한이 없습니다.");
          }
          
        }else if(idf_PObj.getString("u_obj_type").equals("P")) {
          Project projectDataOne = projectDao.selectOne(recycleData.getUObjId()).orElse(new Project());
          
          List<Doc> docDataList = docDao.selectListByPrCode(projectDataOne.getUPjtCode());
          for(int i=0;i<docDataList.size();i++) {  
            Doc docDataOne = docService.selectOne(docDataList.get(i).getRObjectId()).orElse(new Doc());
            String orgId2 = deptDao.selectOrgIdByCabinetcode(docDataOne.getUCabinetCode());
            List<DocRepeating> docV = docDao.selRepeatingOne(docDataOne.getRObjectId());
            LogDoc logDoc = LogDoc.builder()
                  .uJobCode(DocLogItem.LP.getValue())
                  .uDocId(docDataOne.getRObjectId())
                  .uDocKey(docDataOne.getUDocKey())
                  .uDocName(docDataOne.getTitle())
                  .uDocVersion(docV.get(0).getRVersionLabel())
                  .uOwnDeptcode(orgId2).uActDeptCode(orgId2)
                  .uJobUser(userSession.getDUserId())
                  .uJobUserType(userTypeData)
                  .uDocStatus(docDataOne.getUDocStatus())
                  .uSecLevel(docDataOne.getUSecLevel())
                  .uCabinetCode(docDataOne.getUCabinetCode())
                  .uJobGubun("")
                  .uUserIp(ip)
                  .uFileSize(Integer.valueOf(docDataOne.getRContentSize()))
                  .build();                                   
            insertLog(logDoc);
            idf_Doc2 = (IDfDocument) adSess.getObject(new DfId(docDataList.get(i).getRObjectId()));
            idf_Doc2.destroy();
            String s_Dql = "delete edms_auth_base object " +
                " where u_obj_id = '" + docDataList.get(i).getUDocKey() + "' ";
            
            IDfCollection idf_Col = DCTMUtils.getCollectionByDQL(adSess, s_Dql, DfQuery.DF_QUERY);
            if(idf_Col != null) idf_Col.close();
            
          }
          List<Folder> folDataList = folderDao.selectListByPrCode(projectDataOne.getUPjtCode());
          for(int i=0;i<folDataList.size();i++) {    
            idf_PObj3 = (IDfPersistentObject) adSess.getObject(new DfId(folDataList.get(i).getRObjectId()));
            idf_PObj3.destroy();
            String s_Dql2 = "delete edms_auth_base object " +
                " where u_obj_id = '" + folDataList.get(i).getRObjectId() + "' ";
            
            IDfCollection idf_Col2 = DCTMUtils.getCollectionByDQL(adSess, s_Dql2, DfQuery.DF_QUERY);
            if(idf_Col2 != null) idf_Col2.close();
            String s_Dql3 = "delete edms_auth_share object " +
                " where u_obj_id = '" + folDataList.get(i).getRObjectId() + "' ";
            
            IDfCollection idf_Col3 = DCTMUtils.getCollectionByDQL(adSess, s_Dql3, DfQuery.DF_QUERY);
            if(idf_Col3 != null) idf_Col3.close();
          }
          idf_PObj.destroy();
        }else if(idf_PObj.getString("u_obj_type").equals("R")) {
          Research researchDataOne = researchDao.selectOne(recycleData.getUObjId()).orElse(new Research());

          List<Doc> docDataList = docDao.selectListByPrCode(researchDataOne.getURschCode());
          for(int i=0;i<docDataList.size();i++) {
            Doc docDataOne = docService.selectOne(docDataList.get(i).getRObjectId()).orElse(new Doc());
            String orgId2 = deptDao.selectOrgIdByCabinetcode(docDataOne.getUCabinetCode());
            List<DocRepeating> docV = docDao.selRepeatingOne(docDataOne.getRObjectId());
            LogDoc logDoc = LogDoc.builder()
                  .uJobCode(DocLogItem.LP.getValue())
                  .uDocId(docDataOne.getRObjectId())
                  .uDocKey(docDataOne.getUDocKey())
                  .uDocName(docDataOne.getTitle())
                  .uDocVersion(docV.get(0).getRVersionLabel())
                  .uOwnDeptcode(orgId2).uActDeptCode(orgId2)
                  .uJobUser(userSession.getDUserId())
                  .uJobUserType(userTypeData)
                  .uDocStatus(docDataOne.getUDocStatus())
                  .uSecLevel(docDataOne.getUSecLevel())
                  .uCabinetCode(docDataOne.getUCabinetCode())
                  .uJobGubun("")
                  .uUserIp(ip)
                  .uFileSize(Integer.valueOf(docDataOne.getRContentSize()))
                  .build();                                   
            insertLog(logDoc);
            idf_Doc2 = (IDfDocument) adSess.getObject(new DfId(docDataList.get(i).getRObjectId()));
            idf_Doc2.destroy();
            String s_Dql = "delete edms_auth_base object " +
                " where u_obj_id = '" + docDataList.get(i).getUDocKey() + "' ";
            
            IDfCollection idf_Col = DCTMUtils.getCollectionByDQL(adSess, s_Dql, DfQuery.DF_QUERY);
            if(idf_Col != null) idf_Col.close();
          }
          List<Folder> folDataList = folderDao.selectListByPrCode(researchDataOne.getURschCode());
          for(int i=0;i<folDataList.size();i++) {
            idf_PObj3 = (IDfPersistentObject) adSess.getObject(new DfId(folDataList.get(i).getRObjectId()));
            idf_PObj3.destroy();
            String s_Dql2 = "delete edms_auth_base object " +
                " where u_obj_id = '" + folDataList.get(i).getRObjectId() + "' ";
            
            IDfCollection idf_Col2 = DCTMUtils.getCollectionByDQL(adSess, s_Dql2, DfQuery.DF_QUERY);
            if(idf_Col2 != null) idf_Col2.close();
            String s_Dql3 = "delete edms_auth_share object " +
                " where u_obj_id = '" + folDataList.get(i).getRObjectId() + "' ";
            
            IDfCollection idf_Col3 = DCTMUtils.getCollectionByDQL(adSess, s_Dql3, DfQuery.DF_QUERY);
            if(idf_Col3 != null) idf_Col3.close();
            
          }
          idf_PObj4 =  (IDfPersistentObject) adSess.getObject(new DfId(researchDataOne.getRObjectId()));
          idf_PObj4.destroy();
          idf_PObj.destroy();
        }
      
      idfSession.commitTrans();
      adSess.commitTrans();
    } catch (Exception e) {
      throw e;
    } finally {
      if (idfSession != null) {
        if (idfSession.isTransactionActive()) {
          idfSession.abortTrans();
        }
        if (adSess.isTransactionActive()) {
          adSess.abortTrans();         
        }
        if (idfSession.isConnected() && adSess.isConnected()) {
          sessionRelease(userSession.getUser().getUserId(), idfSession);
          adSess.disconnect();
        }
      }
    }

    return idf_PObj.getObjectId().getId();
  }

  @Override
  public Map<String, Integer> deleteAllData(UserSession userSession, List<String> deleteAllList, String ip) throws Exception {
    IDfSession idfSession = null;
    IDfSession adSess = null;
    IDfPersistentObject idf_PObj = null;
    IDfDocument idf_Doc2 = null;
    IDfPersistentObject idf_PObj3 = null;
    IDfPersistentObject idf_PObj4 = null;
    IDfPersistentObject idf_PObj5 = null;
    IDfPersistentObject idf_PObj6 = null;
    Recycle recycleData = null;
    String reqDeptCode = null;
    List<DeptMgrs> reqDeptMgr = null;
    VDept deptData = null;
    
    int successCnt = 0;
    int failCnt = 0;
    try {
      idfSession = this.getIdfSession(userSession);
      idfSession.beginTrans();
      adSess = DCTMUtils.getAdminSession();
      if (!adSess.isTransactionActive()) {
        adSess.beginTrans();
      }
      for (String objectId : deleteAllList) {
        try {
          recycleData = recycleDao.oneRecycleById(objectId);       
          reqDeptCode = deptDao.selectOrgIdByCabinetcode(recycleData.getUCabinetCode());
          reqDeptMgr = deptMgrDao.selectByDeptCode(reqDeptCode);
          deptData = deptDao.selectOneByOrgId(reqDeptCode).orElse(new VDept());
          idf_PObj = (IDfPersistentObject) idfSession.getObject(new DfId(objectId));
          // 삭제자가 현재 로그인한 사람과 동일한지와 팀장인지 여부 판단 해야함. // chiefFlag ==> 팀장여부??
          if (idf_PObj.getString("u_obj_type").equals("D")) {
            Doc docData = docService.selectOne(recycleData.getUObjId()).orElse(null);
            String objId = deptDao.selectOrgIdByCabinetcode(docData.getUCabinetCode());
            List<DocRepeating> docV = docDao.selRepeatingOne(docData.getRObjectId());
            List<String> mgrId = new ArrayList<>();
            for (int i = 0; i < reqDeptMgr.size(); i++) {
             if(reqDeptMgr.get(i).getUUserId().contains(userSession.getDUserId())) {
               mgrId.add(reqDeptMgr.get(i).getUUserId());
               break; 
             } 
            }
            if (mgrId.contains(userSession.getDUserId()) || userSession.getDUserId().equals(deptData.getManagerPerId())) {
              LogDoc logDoc = LogDoc.builder()
                      .uJobCode(DocLogItem.LP.getValue())
                      .uDocId(docData.getRObjectId())
                      .uDocKey(docData.getUDocKey())
                      .uDocName(docData.getTitle())
                      .uDocVersion(docV.get(0).getRVersionLabel())
                      .uOwnDeptcode(objId).uActDeptCode(objId)
                      .uJobUser(userSession.getDUserId())
                      .uJobUserType("D")
                      .uDocStatus(docData.getUDocStatus())
                      .uSecLevel(docData.getUSecLevel())
                      .uCabinetCode(docData.getUCabinetCode())
                      .uJobGubun("")
                      .uUserIp(ip)
                      .uFileSize(Integer.valueOf(docData.getRContentSize()))
                      .build();          
              insertLog(logDoc);
              idf_Doc2 = (IDfDocument) adSess.getObject(new DfId(docData.getRObjectId()));
              idf_Doc2.destroy();
              String s_Dql = "delete edms_auth_base object " +
                  " where u_obj_id = '" + docData.getUDocKey() + "' ";
              
              IDfCollection idf_Col = DCTMUtils.getCollectionByDQL(adSess, s_Dql, DfQuery.DF_QUERY);
              if(idf_Col != null) idf_Col.close();
              idf_PObj.destroy();
            } 
             else {
              throw new Exception("권한이 없습니다.");
            }
            
          } else if (idf_PObj.getString("u_obj_type").equals("F")) {
            List<String> mgrId = new ArrayList<>();
            for (int i = 0; i < reqDeptMgr.size(); i++) {
             if(reqDeptMgr.get(i).getUUserId().contains(userSession.getDUserId())) {
               mgrId.add(reqDeptMgr.get(i).getUUserId());
               break; 
             } 
            }
            if (mgrId.contains(userSession.getDUserId()) || userSession.getDUserId().equals(deptData.getManagerPerId())) {
              List<FolderDescendantDto> folderDescendantDtoList = folderDao.selectDescendantsAll(recycleData.getUObjId());
             
              List<Doc> docAllList2 = docDao.recycleListForFolderId(recycleData.getUObjId());
              if (folderDescendantDtoList.size() == 0) {
                for (int q = 0; q < docAllList2.size(); q++) {
                  Doc docDataOne = docService.selectOne(docAllList2.get(q).getRObjectId()).orElse(new Doc());
                  String orgId2 = deptDao.selectOrgIdByCabinetcode(docDataOne.getUCabinetCode());
                  List<DocRepeating> docV = docDao.selRepeatingOne(docDataOne.getRObjectId());
                  LogDoc logDoc = LogDoc.builder()
                          .uJobCode(DocLogItem.LP.getValue())
                          .uDocId(docDataOne.getRObjectId())
                          .uDocKey(docDataOne.getUDocKey())
                          .uDocName(docDataOne.getTitle())
                          .uDocVersion(docV.get(0).getRVersionLabel())
                          .uOwnDeptcode(orgId2).uActDeptCode(orgId2)
                          .uJobUser(userSession.getDUserId())
                          .uJobUserType("D")
                          .uDocStatus(docDataOne.getUDocStatus())
                          .uSecLevel(docDataOne.getUSecLevel())
                          .uCabinetCode(docDataOne.getUCabinetCode())
                          .uJobGubun("")
                          .uUserIp(ip)
                          .uFileSize(Integer.valueOf(docDataOne.getRContentSize()))
                          .build();                 
                  insertLog(logDoc);
                  idf_Doc2 = (IDfDocument) adSess.getObject(new DfId(docAllList2.get(q).getRObjectId()));
                  idf_Doc2.destroy();
                  String s_Dql = "delete edms_auth_base object " +
                      " where u_obj_id = '" + docAllList2.get(q).getUDocKey() + "' ";                  
                  IDfCollection idf_Col = DCTMUtils.getCollectionByDQL(adSess, s_Dql, DfQuery.DF_QUERY);
                  if(idf_Col != null) idf_Col.close();
                  
                  idf_PObj5 = (IDfPersistentObject) idfSession.getObject(new DfId(recycleData.getUObjId()));
                  idf_PObj5.destroy();
                  String s_Dql2 = "delete edms_auth_base object " +
                      " where u_obj_id = '" + recycleData.getUObjId() + "' ";
                  
                  IDfCollection idf_Col2 = DCTMUtils.getCollectionByDQL(adSess, s_Dql2, DfQuery.DF_QUERY);
                  if(idf_Col2 != null) idf_Col2.close();
                  String s_Dql3 = "delete edms_auth_share object " +
                      " where u_obj_id = '" + recycleData.getUObjId() + "' ";
                  
                  IDfCollection idf_Col3 = DCTMUtils.getCollectionByDQL(adSess, s_Dql3, DfQuery.DF_QUERY);
                  if(idf_Col3 != null) idf_Col3.close();
                }
               
                idf_PObj.destroy();
              } else {
                List<String> folIdList = folderDescendantDtoList.stream().map((item) -> item.getRObjectId())
                    .collect(Collectors.toList());
                List<Doc> docAllList = docDao.recycleListByFolIds(folIdList);
                for (int q = 0; q < docAllList.size(); q++) {
                  Doc docDataOne = docService.selectOne(docAllList.get(q).getRObjectId()).orElse(new Doc());
                  String orgId2 = deptDao.selectOrgIdByCabinetcode(docDataOne.getUCabinetCode());
                  List<DocRepeating> docV = docDao.selRepeatingOne(docDataOne.getRObjectId());
                  LogDoc logDoc = LogDoc.builder()
                          .uJobCode(DocLogItem.LP.getValue())
                          .uDocId(docDataOne.getRObjectId())
                          .uDocKey(docDataOne.getUDocKey())
                          .uDocName(docDataOne.getTitle())
                          .uDocVersion(docV.get(0).getRVersionLabel())
                          .uOwnDeptcode(orgId2).uActDeptCode(orgId2)
                          .uJobUser(userSession.getDUserId())
                          .uJobUserType("D")
                          .uDocStatus(docDataOne.getUDocStatus())
                          .uSecLevel(docDataOne.getUSecLevel())
                          .uCabinetCode(docDataOne.getUCabinetCode())
                          .uJobGubun("")
                          .uUserIp(ip)
                          .uFileSize(Integer.valueOf(docDataOne.getRContentSize()))
                          .build();                                   
                  insertLog(logDoc);
                  idf_Doc2 = (IDfDocument) adSess.getObject(new DfId(docAllList.get(q).getRObjectId()));
                  idf_Doc2.destroy();
                  String s_Dql = "delete edms_auth_base object " +
                      " where u_obj_id = '" + docAllList2.get(q).getUDocKey() + "' ";                  
                  IDfCollection idf_Col = DCTMUtils.getCollectionByDQL(adSess, s_Dql, DfQuery.DF_QUERY);
                  if(idf_Col != null) idf_Col.close();
                }
                for (int a=0;a<folIdList.size();a++) {
                  idf_PObj5 = (IDfPersistentObject) idfSession.getObject(new DfId(folIdList.get(a)));                  
                  idf_PObj5.destroy();
                  String s_Dql2 = "delete edms_auth_base object " +
                      " where u_obj_id = '" + folIdList.get(a) + "' ";
                  
                  IDfCollection idf_Col2 = DCTMUtils.getCollectionByDQL(adSess, s_Dql2, DfQuery.DF_QUERY);
                  if(idf_Col2 != null) idf_Col2.close();
                  String s_Dql3 = "delete edms_auth_share object " +
                      " where u_obj_id = '" + folIdList.get(a) + "' ";
                  
                  IDfCollection idf_Col3 = DCTMUtils.getCollectionByDQL(adSess, s_Dql3, DfQuery.DF_QUERY);
                  if(idf_Col3 != null) idf_Col3.close();
                  
                }
                idf_PObj.destroy();
              }

            } else {
              throw new Exception("권한이 없습니다.");
            }
            
          }else if(idf_PObj.getString("u_obj_type").equals("P")) {
            Project projectDataOne = projectDao.selectOne(recycleData.getUObjId()).orElse(new Project());
            
            List<Doc> docDataList = docDao.selectListByPrCode(projectDataOne.getUPjtCode());
            for(int i=0;i<docDataList.size();i++) {  
              idf_Doc2 = (IDfDocument) adSess.getObject(new DfId(docDataList.get(i).getRObjectId()));
              idf_Doc2.destroy();
              String s_Dql = "delete edms_auth_base object " +
                  " where u_obj_id = '" + docDataList.get(i).getUDocKey() + "' ";                  
              IDfCollection idf_Col = DCTMUtils.getCollectionByDQL(adSess, s_Dql, DfQuery.DF_QUERY);
              if(idf_Col != null) idf_Col.close();
            }
            List<Folder> folDataList = folderDao.selectListByPrCode(projectDataOne.getUPjtCode());
            for(int i=0;i<folDataList.size();i++) {    
              idf_PObj3 = (IDfPersistentObject) adSess.getObject(new DfId(folDataList.get(i).getRObjectId()));
              idf_PObj3.destroy();
              String s_Dql2 = "delete edms_auth_base object " +
                  " where u_obj_id = '" + folDataList.get(i).getRObjectId() + "' ";              
              IDfCollection idf_Col2 = DCTMUtils.getCollectionByDQL(adSess, s_Dql2, DfQuery.DF_QUERY);
              if(idf_Col2 != null) idf_Col2.close();
              String s_Dql3 = "delete edms_auth_share object " +
                  " where u_obj_id = '" + folDataList.get(i).getRObjectId() + "' ";              
              IDfCollection idf_Col3 = DCTMUtils.getCollectionByDQL(adSess, s_Dql3, DfQuery.DF_QUERY);
              if(idf_Col3 != null) idf_Col3.close();
            }
            idf_PObj.destroy();
          }else if(idf_PObj.getString("u_obj_type").equals("R")) {
            Research researchDataOne = researchDao.selectOne(recycleData.getUObjId()).orElse(new Research());

            List<Doc> docDataList = docDao.selectListByPrCode(researchDataOne.getURschCode());
            for(int i=0;i<docDataList.size();i++) {
              idf_Doc2 = (IDfDocument) adSess.getObject(new DfId(docDataList.get(i).getRObjectId()));
              idf_Doc2.destroy();
              String s_Dql = "delete edms_auth_base object " +
                  " where u_obj_id = '" + docDataList.get(i).getUDocKey() + "' ";                  
              IDfCollection idf_Col = DCTMUtils.getCollectionByDQL(adSess, s_Dql, DfQuery.DF_QUERY);
              if(idf_Col != null) idf_Col.close();
            }
            List<Folder> folDataList = folderDao.selectListByPrCode(researchDataOne.getURschCode());
            for(int i=0;i<folDataList.size();i++) {
              idf_PObj3 = (IDfPersistentObject) adSess.getObject(new DfId(folDataList.get(i).getRObjectId()));
              idf_PObj3.destroy();
              String s_Dql2 = "delete edms_auth_base object " +
                  " where u_obj_id = '" + folDataList.get(i).getRObjectId() + "' ";              
              IDfCollection idf_Col2 = DCTMUtils.getCollectionByDQL(adSess, s_Dql2, DfQuery.DF_QUERY);
              if(idf_Col2 != null) idf_Col2.close();
              String s_Dql3 = "delete edms_auth_share object " +
                  " where u_obj_id = '" + folDataList.get(i).getRObjectId() + "' ";              
              IDfCollection idf_Col3 = DCTMUtils.getCollectionByDQL(adSess, s_Dql3, DfQuery.DF_QUERY);
              if(idf_Col3 != null) idf_Col3.close();
            }
            idf_PObj4 =  (IDfPersistentObject) adSess.getObject(new DfId(researchDataOne.getRObjectId()));
            idf_PObj4.destroy();
            idf_PObj.destroy();
          }
          
          if (idf_PObj.isDeleted() == true) {
            successCnt++;
            continue;
          } else {
            failCnt++;
          }

        } catch (Exception e) {
          failCnt++;
          continue;
        } finally {
        }
      }
      idfSession.commitTrans();
      adSess.commitTrans();
    } catch (Exception e) {
      throw e;
    } finally {
      if (idfSession != null) {
        if (idfSession.isTransactionActive()) {
          idfSession.abortTrans();
        }
        if (adSess.isTransactionActive()) {
          adSess.abortTrans();         
        }
        if (idfSession.isConnected()) {
          sessionRelease(userSession.getUser().getUserId(), idfSession);
          adSess.disconnect();
        }
      }
    }

    Map<String, Integer> result = new HashMap<String, Integer>();

    result.put("success", successCnt);
    result.put("fail", failCnt);
    return result;
  }

@Override
public Optional<Recycle> selectRecycleCaCode(String orgId) {
  return recycleDao.selectRecycleCaCode(orgId);
}

}
