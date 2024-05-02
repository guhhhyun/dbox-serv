package com.dongkuksystems.dbox.services.project;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfGroup;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;
import com.dongkuksystems.dbox.constants.AuthObjType;
import com.dongkuksystems.dbox.constants.AuthorType;
import com.dongkuksystems.dbox.constants.DCTMConstants;
import com.dongkuksystems.dbox.constants.DocStatus;
import com.dongkuksystems.dbox.constants.GrantedLevels;
import com.dongkuksystems.dbox.constants.ProjectOwnJoin;
import com.dongkuksystems.dbox.daos.table.etc.gwdept.GwDeptDao;
import com.dongkuksystems.dbox.daos.type.auth.base.AuthBaseDao;
import com.dongkuksystems.dbox.daos.type.doc.DocDao;
import com.dongkuksystems.dbox.daos.type.docbox.project.ProjectDao;
import com.dongkuksystems.dbox.daos.type.folder.FolderDao;
import com.dongkuksystems.dbox.errors.BadRequestException;
import com.dongkuksystems.dbox.errors.ForbiddenException;
import com.dongkuksystems.dbox.errors.NotFoundException;
import com.dongkuksystems.dbox.errors.UnauthorizedException;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.doc.DocFilterDto;
import com.dongkuksystems.dbox.models.dto.type.docbox.project.ProjectCountDto;
import com.dongkuksystems.dbox.models.dto.type.docbox.project.ProjectCreateDto;
import com.dongkuksystems.dbox.models.dto.type.docbox.project.ProjectDetailDto;
import com.dongkuksystems.dbox.models.dto.type.docbox.project.ProjectFilterDto;
import com.dongkuksystems.dbox.models.dto.type.docbox.project.ProjectRepeatDto;
import com.dongkuksystems.dbox.models.dto.type.docbox.project.ProjectUpdateDto;
import com.dongkuksystems.dbox.models.dto.type.folder.FolderFilterDto;
import com.dongkuksystems.dbox.models.table.etc.GwAddJob;
import com.dongkuksystems.dbox.models.table.etc.VDept;
import com.dongkuksystems.dbox.models.type.auth.AuthBase;
import com.dongkuksystems.dbox.models.type.doc.Doc;
import com.dongkuksystems.dbox.models.type.docbox.Project;
import com.dongkuksystems.dbox.models.type.docbox.ProjectRepeating;
import com.dongkuksystems.dbox.models.type.folder.Folder;
import com.dongkuksystems.dbox.services.AbstractCommonService;
import com.dongkuksystems.dbox.utils.dctm.DCTMUtils;

@Service
public class ProjectServiceImpl extends AbstractCommonService implements ProjectService {
	private final ProjectDao projectDao;
  private final GwDeptDao deptDao;
  private final FolderDao folderDao;
  private final DocDao docDao;
  private final AuthBaseDao authBaseDao;

	public ProjectServiceImpl(ProjectDao projectDao, GwDeptDao deptDao, FolderDao folderDao, DocDao docDao, AuthBaseDao authBaseDao) {
		this.projectDao = projectDao;
    this.deptDao = deptDao;
    this.folderDao = folderDao;
    this.docDao = docDao;
    this.authBaseDao = authBaseDao;
	}

	@Override
	public ProjectDetailDto selectProject(String projectId, String orgId, String userId) throws Exception {
		// 프로젝트 조회
		Project project = projectDao.selectDetailOne(projectId, orgId, userId).orElseThrow(() -> new NotFoundException(Project.class, projectId));
		
		// 프로젝트 상세 DTO 변경
		ProjectDetailDto projectDetailDto = getModelMapper().map(project, ProjectDetailDto.class);
		
		// Repeating 리스트 조회
		List<ProjectRepeating> projectRepeatingList = Optional.ofNullable(project.getProjectRepeatings()).orElse(new ArrayList<>());

		// 참여부서(조회/다운로드) 리스트
		List<VDept> joinDeptReadList = projectRepeatingList.stream()
				.filter((item) -> item.getJoinDeptReadDetail() != null)
				.map((item) -> item.getJoinDeptReadDetail())
				.collect(Collectors.toList());
		projectDetailDto.setJoinDeptReads(joinDeptReadList);

		// 참여부서(편집/삭제) 리스트
		List<VDept> joinDeptDelList = projectRepeatingList.stream()
				.filter((item) -> item.getJoinDeptDelDetail() != null)
				.map((item) -> item.getJoinDeptDelDetail())
				.collect(Collectors.toList());
		projectDetailDto.setJoinDeptDels(joinDeptDelList);
		
		return projectDetailDto;
	}
	
	@Override
	public Optional<Project> selectProjectByUPjtCode(String uPjtCode) throws Exception {
		return projectDao.selectOneByUPjtCode(uPjtCode);
	}

	@Override
	public List<Project> selectProjectList(ProjectFilterDto projectFilterDto, String orgId, String userId) {
		List<Project> pjtList = projectDao.selectDetailList(projectFilterDto, orgId, userId);
		
    return pjtList;
	}
	
	@Override
	public ProjectCountDto selectProjectCount(ProjectFilterDto projectFilterDto, String orgId, String userId) throws Exception {
		// 주관부서 진행중 조회
		projectFilterDto.setOwnJoin(ProjectOwnJoin.OWN.getValue());
		projectFilterDto.setUFinishYn("N");
		int ownDoingCount = projectDao.selectCount(projectFilterDto, orgId, userId);

		// 주관부서 완료 조회
		projectFilterDto.setOwnJoin(ProjectOwnJoin.OWN.getValue());
		projectFilterDto.setUFinishYn("Y");
		int ownDoneCount = projectDao.selectCount(projectFilterDto, orgId, userId);
		
		// 참여부서 진행중 조회
		projectFilterDto.setOwnJoin(ProjectOwnJoin.JOIN.getValue());
		projectFilterDto.setUFinishYn("N");
		int joinDoingCount = projectDao.selectCount(projectFilterDto, orgId, userId);

		// 참여부서 완료 조회
		projectFilterDto.setOwnJoin(ProjectOwnJoin.JOIN.getValue());
		projectFilterDto.setUFinishYn("Y");
		int joinDoneCount = projectDao.selectCount(projectFilterDto, orgId, userId);
		
		ProjectCountDto result = ProjectCountDto.builder()
				.ownDoing(ownDoingCount)
				.ownDone(ownDoneCount)
				.joinDoing(joinDoingCount)
				.joinDone(joinDoneCount)
				.build();
		
		return result;
	}

	/** 프로젝트 생성.(Insert or Update) **/
	@Override
	public String saveProject(UserSession userSession, IDfSession idfSess, ProjectCreateDto dto) throws Exception {

	    IDfSession idfSession = idfSess != null ? idfSess : this.getIdfSession(userSession);
	    IDfSession adSess =null;

	    String lsUPjtCode = DCTMUtils.checkNullStringByObj(dto.getUPjtCode())==""?projectDao.selectNextPjtNo():dto.getUPjtCode();	    
	    //String newPjtNo = dto.getUPjtCode()==null projectDao.selectNextPjtNo(); //신규 프로젝트 번호를 채번

	    try { 
	 	    if (!idfSession.isTransactionActive()) {
	 	        idfSession.beginTrans();
	 	    }

			dto.setUPjtCode(lsUPjtCode);
	
			/** To-Do 권한체크 Start (생성하고자 하는 프로젝트 위치에 대한 생성 / 변경 권한 보유 여부 확인) **/
			
			String rObjectId = dto.getRObjectId();
			if( !dto.getUCabinetCode().equals(userSession.getUser().getDeptCabinetcode())){
				throw new ForbiddenException("프로젝트 " + rObjectId!="" && rObjectId !=null ?"수정":"생성");			
			}
			/**End */		
			
			String orgId=userSession.getUser().getOrgId();
			VDept dept = deptDao.selectOneByOrgIdDefault(orgId);
			dto.setUCabinetCode(dept.getUCabinetCode());
			//dto.setUCabinetCode(userSession.getUser().getDeptDetail().getUCabinetCode());
			IDfPersistentObject idf_PObj = ProjectCreateDto.createProject(idfSession, dto);
	
			/** 생성된 프로젝트에 책임자 그룹생성 / 변경 Start  */

	        String s_GroupCode = "g_"+ lsUPjtCode+"_pjtmgr";
	  	    adSess = DCTMUtils.getAdminSession();
	 	    if (!adSess.isTransactionActive()) {
	 		    adSess.beginTrans();
	 	    }
	        IDfGroup idf_GroupObj = (IDfGroup)adSess.getObjectByQualification("dm_group where group_name = '"+ s_GroupCode +"'");
	 	    
			if(idf_GroupObj != null)
			{
				idf_GroupObj.removeAllUsers();
				idf_GroupObj.addUser(dto.getUChiefId());

				idf_GroupObj.save();
			}else {
				idf_GroupObj = (IDfGroup)adSess.newObject("dm_group");
				idf_GroupObj.setGroupName(s_GroupCode);
				idf_GroupObj.removeAllUsers();
				idf_GroupObj.addUser(dto.getUChiefId());
				idf_GroupObj.save();
			}
			
			/**End */
			idf_PObj.save();
			
		    idfSession.commitTrans();
		    adSess.commitTrans();

		
	    } catch (Exception e) {
	  	  e.printStackTrace();
	  	} finally {
	  	  if (idfSession != null) {
	  	    if (idfSession.isTransactionActive()) {
	  	      idfSession.abortTrans();
	  	    }
	  	    if (idfSession.isConnected()) {
	  	      idfSession.disconnect();
	  	    }
	  	  }
	  	  if (adSess != null) {
		  	    if (adSess.isTransactionActive()) {
		  	    	adSess.abortTrans();
		  	    }
		  	    if (adSess.isConnected()) {
		  	    	adSess.disconnect();
		  	    }
		  	  }
	  	}		
		return lsUPjtCode;
	}

	/** 프로젝트 삭제. (delete)**/
	@Override
	public String deleteProject(String rObjectId, UserSession userSession,IDfSession idfSess) throws Exception {
	    IDfSession idfSession = idfSess != null ? idfSess : this.getIdfSession(userSession);
	    IDfSession adSess =null;

	    try { 
	 	    if (!idfSession.isTransactionActive()) {
	 	        idfSession.beginTrans();
	 	    }
			String s_ObjId = rObjectId;
			IDfPersistentObject idf_PObj = idfSession.getObject(new DfId(s_ObjId));
			idf_PObj.destroy();
			
			idfSession.commitTrans();
			
		} catch (Exception e) {
			System.out.println(e.getMessage());
			throw e;
		} finally {
		  	  if (idfSession != null) {
		  	    if (idfSession.isTransactionActive()) {
		  	      idfSession.abortTrans();
		  	    }
		  	    if (idfSession.isConnected()) {
		  	      idfSession.disconnect();
		  	    }
		  	  }
		}	
		return rObjectId;
	}
	
	/** 프로젝트 생성. **/
	@Override
	public String createProject(UserSession userSession, ProjectCreateDto dto, IDfSession session) throws Exception {
		final String userId = userSession.getUser().getUserId();
		final String orgId = userSession.getUser().getOrgId();
		final boolean isOwnDept = Objects.equals(orgId, dto.getUOwnDept())
		    || userSession.getUser().getAddDepts().stream().anyMatch(item -> Objects.equals(item.getUnitCode(), dto.getUOwnDept()));
		
		// 권한 확인
		if (!isOwnDept) throw new RuntimeException("다른 부서에 프로젝트를 생성할 수 없습니다.");
		
		// 현재 사용자 세션 생성 및 트랜잭션 시작
//		IDfSession idfSession = Objects.isNull(session) ? this.getIdfSession(userSession): session;
//	  if (!idfSession.isTransactionActive()) {
//      idfSession.beginTrans();
//    }

		// 책임자 그룹 생성을 위한 관리자 세션 생성 및 트랜잭션 시작
		IDfSession adSess = DCTMUtils.getAdminSession();
    if (!adSess.isTransactionActive()) {
	    adSess.beginTrans();
    }
		
		try {
			// 신규 프로젝트 번호를 채번, 설정
//			String newPjtNo = projectDao.selectNextPjtNo();
//			dto.setUPjtCode(newPjtNo);

			// 주관부서 코드를 전달받지 않았을 경우 사용자 소속 부서, 캐비닛 코드 설정
			if (dto.getUOwnDept() == null) { 					
				dto.setUOwnDept(orgId);
				VDept dept = deptDao.selectOneByOrgIdDefault(orgId);
				String cabinetCode = dept.getUCabinetCode();
				dto.setUCabinetCode(cabinetCode);
			} else { // 전달받았을 경우 전달받은 부서, 캐비닛 코드 설정
				VDept dept = deptDao.selectOneByOrgIdDefault(dto.getUOwnDept());
				String cabinetCode = dept.getUCabinetCode();
				dto.setUCabinetCode(cabinetCode);	
			}
			
			// 책임자를 전달받지 않았을 경우 사용자로 설정
			if (dto.getUChiefId() == null) dto.setUChiefId(userId);

			// 시행연도를 전달받지 않았을 경우 현재 연도 설정
			if (dto.getUStartYear() == null) {
				int year = Calendar.getInstance().get(Calendar.YEAR);
				String yearStr = Integer.toString(year);
				dto.setUStartYear(yearStr);
			}
			
			// 최초 생성 시 완료 여부 N(진행중)
			dto.setUFinishYn("N");
		  
		  // 프로젝트 생성 데이터 매핑
			IDfPersistentObject idf_PObj = ProjectCreateDto.createProject(adSess, dto);
			
 	    // 책임자 그룹 생성
	    String s_GroupCode = "g_" + idf_PObj.getString("u_pjt_code") + "_pjtmgr";
      IDfGroup idf_GroupObj = (IDfGroup)adSess.getObjectByQualification("dm_group where group_name = '"+ s_GroupCode +"'");
      if(idf_GroupObj == null) {
				idf_GroupObj = (IDfGroup)adSess.newObject("dm_group");
				idf_GroupObj.setGroupName(s_GroupCode);
			}
			idf_GroupObj.removeAllUsers();
			idf_GroupObj.addUser(dto.getUChiefId());
			idf_GroupObj.save();
			
			idf_PObj.save();

			// 트랜잭션 커밋
//      if (Objects.isNull(session)) {
//        idfSession.commitTrans();
//      }
	    adSess.commitTrans();
			
			return idf_PObj.getString("u_pjt_code");
		} finally {
//		  if (Objects.isNull(session)) {
//	  	  if (idfSession != null) {
//	  	    if (idfSession.isTransactionActive()) {
//	  	      idfSession.abortTrans();
//	  	    }
//	  	    if (idfSession.isConnected()) {
//	  	      idfSession.disconnect();
//	  	    }
//	  	  }
//		  }
  	  if (adSess != null) {
  	    if (adSess.isTransactionActive()) {
  	    	adSess.abortTrans();
  	    }
  	    if (adSess.isConnected()) {
  	    	adSess.disconnect();
  	    }
  	  }
    }
	}

  @Override
  public String updateProject(UserSession userSession, ProjectUpdateDto dto, IDfSession session) throws Exception {
    final ModelMapper modelMapper = getModelMapper();
    final String userId = userSession.getUser().getUserId();
    final String orgId = userSession.getUser().getOrgId();
    // 기존 프로젝트/투자 조회
    Project project = projectDao.selectOneByUPjtCode(dto.getUPjtCode()).orElseThrow(() -> new BadRequestException("Not found Project"));
    
    final boolean isOwnDept = Objects.equals(orgId, project.getUOwnDept())
        || userSession.getUser().getAddDepts().stream().anyMatch(item -> Objects.equals(item.getUnitCode(), project.getUOwnDept()));
    final boolean isChief = Objects.equals(userId, project.getUChief());
    
    // 권한 확인
    if (!isOwnDept && !isChief) throw new RuntimeException("프로젝트/투자에 대한 수정권한이 없습니다.");

		// 현재 사용자 세션 생성 및 트랜잭션 시작
//		IDfSession idfSession = Objects.isNull(session) ? this.getIdfSession(userSession): session;
//	  if (!idfSession.isTransactionActive()) {
//      idfSession.beginTrans();
//    }

		// 책임자 그룹 생성을 위한 관리자 세션 생성 및 트랜잭션 시작
		IDfSession adSess = DCTMUtils.getAdminSession();
    if (!adSess.isTransactionActive()) {
	    adSess.beginTrans();
    }

    try {
      //변경자가 주관부서 소속인지 확인
      if (Objects.isNull(userSession.getUser().getOrgId()))
        throw new UnauthorizedException("Unauthorized");
      if (!userSession.getUser().getOrgId().equals(project.getUOwnDept())) {
        if (Objects.isNull(userSession.getUser().getAddDepts())) {
          boolean isAuthed = false;
          for (GwAddJob gd : userSession.getUser().getAddDepts()) {
            if (gd.getUnitCode().equals(project.getUOwnDept())) {
              isAuthed = true;
              break;
            }
          }
          if (!isAuthed)
            throw new UnauthorizedException("Unauthorized");
        }
      }
      ProjectCreateDto updateDto = modelMapper.map(project, ProjectCreateDto.class);
  
  	  // FIXME uChiefId 변수명이 일치 하지 않아, 일단.. 후에 수정.
  	  updateDto.setUChiefId(project.getUChief());
  
      // 수정자 연도
      updateDto.setUUpdateUser(userId);// update user
      if (!Objects.isNull(dto.getUFinishYn())) updateDto.setUStartYear(dto.getUFinishYn());   // 완료 여부
      if (!Objects.isNull(dto.getUPjtName()))
        if (!dto.getUPjtName().isEmpty()) updateDto.setUPjtName(dto.getUPjtName());         	// 프로젝트 명
      if (!Objects.isNull(dto.getUStartYear()))
        if (!dto.getUStartYear().isEmpty())  updateDto.setUStartYear(dto.getUStartYear());    // 시행년도
      if (!Objects.isNull(dto.getUChiefId()))
        if (!dto.getUChiefId().isEmpty())  updateDto.setUChiefId(dto.getUChiefId());          // 책임자
      if (!Objects.isNull(dto.getUListOpenYn()))
        if (!dto.getUListOpenYn().isEmpty())  updateDto.setUListOpenYn(dto.getUListOpenYn()); // 목록보기 활성화
      if (!Objects.isNull(dto.getUSecLevel()))
        if (!dto.getUSecLevel().isEmpty())  updateDto.setUSecLevel(dto.getUSecLevel());       // 보안등급
      if (!Objects.isNull(dto.getUOwnDept()))
        if (!dto.getUOwnDept().isEmpty())  updateDto.setUOwnDept(dto.getUOwnDept());          // 소유부서
      updateDto.setUJoinDeptReads(dto.getUJoinDeptReads());																		// 참여부서 (조회/다운로드)
      updateDto.setUJoinDeptDels(dto.getUJoinDeptDels());																			// 참여부서 (편집/삭제)
  
      IDfPersistentObject idf_PObj = ProjectCreateDto.createProject(adSess, updateDto);
      
      // 프로젝트/투자 하위 폴더 조회
      FolderFilterDto folderFilterDto = FolderFilterDto.builder()
          .uPrCode(dto.getUPjtCode())
          .uDeleteStatus(DCTMConstants.DCTM_BLANK)
          .build();
      List<Folder> folderList = folderDao.selectList(folderFilterDto);
      
      // 프로젝트/투자 하위 문서 조회
      DocFilterDto docFilterDto = DocFilterDto.builder()
          .uPrCode(dto.getUPjtCode())
          .build();
      List<Doc> docList = docDao.selectList(docFilterDto);

      // 기존 하위폴더 권한 제거
      for (Folder folder : folderList) {
        String s_Dql = "DELETE edms_auth_base OBJECTS WHERE u_add_gubun='J' AND u_obj_id ='" + folder.getRObjectId() + "'";

        IDfCollection idf_Colb = DCTMUtils.getCollectionByDQL(adSess, s_Dql, DfQuery.DF_QUERY);
        if (idf_Colb != null) idf_Colb.close();
      }

      // 기존 하위문서 권한 제거
      for (Doc doc : docList) {
        List<AuthBase> authBaseList = authBaseDao.selectList(doc.getUDocKey(), "A").stream()
            .filter(item -> "J".equals(item.getUAddGubun()))
            .collect(Collectors.toList());
        
        String s_Dql = "DELETE edms_auth_base OBJECTS WHERE u_add_gubun='J' AND u_obj_id ='" + doc.getUDocKey() + "'";

        IDfCollection idf_Colb = DCTMUtils.getCollectionByDQL(adSess, s_Dql, DfQuery.DF_QUERY);
        if (idf_Colb != null) idf_Colb.close();
        
        // 기존 권한 revoke
        IDfDocument iDfDoc = (IDfDocument)adSess.getObject(new DfId(doc.getRObjectId()));
        for (AuthBase authBase : authBaseList) {
          iDfDoc.revoke(authBase.getUAuthorId(), null);
          iDfDoc.revoke(authBase.getUAuthorId().concat("_sub"), null);
        }
        iDfDoc.save();
      }
      
      // Read 권한 부여
      if (dto.getUJoinDeptReads() != null) {
        for (String joinDeptCode : dto.getUJoinDeptReads()) {
          String ownDeptYn = orgId.equals(joinDeptCode) ? "Y" : "N";
          
          // 부서의 캐비닛 코드 조회
          VDept vDept = deptDao.selectOneByOrgId(joinDeptCode).orElseThrow(() -> new RuntimeException("전달받은 부서가 존재하지 않는 부서입니다."));
          String authorId = "g_" + vDept.getUCabinetCode();
          
          for (Folder folder : folderList) {
            // Live 권한 부여
            IDfPersistentObject iDfAuthBaseLive = adSess.newObject("edms_auth_base");
            iDfAuthBaseLive.setString("u_obj_id"      , folder.getRObjectId());
            iDfAuthBaseLive.setString("u_obj_type"    , AuthObjType.FOLDER.getValue());
            iDfAuthBaseLive.setString("u_doc_status"  , DocStatus.LIVE.getValue());
            iDfAuthBaseLive.setString("u_permit_type" , GrantedLevels.READ.getLabel());
            iDfAuthBaseLive.setString("u_own_dept_yn" , ownDeptYn);
            iDfAuthBaseLive.setString("u_author_id"   , authorId);
            iDfAuthBaseLive.setString("u_author_type" , AuthorType.TEAM.getValue());
            iDfAuthBaseLive.setString("u_create_user" , userId);
            iDfAuthBaseLive.setString("u_create_date" , (new DfTime()).toString());
            iDfAuthBaseLive.setString("u_add_gubun"   , "J");
            
            iDfAuthBaseLive.save();
            
            // Live _sub 처리
            IDfPersistentObject iDfAuthBaseLiveSub = adSess.newObject("edms_auth_base");
            iDfAuthBaseLiveSub.setString("u_obj_id"      , folder.getRObjectId());
            iDfAuthBaseLiveSub.setString("u_obj_type"    , AuthObjType.FOLDER.getValue());
            iDfAuthBaseLiveSub.setString("u_doc_status"  , DocStatus.LIVE.getValue());
            iDfAuthBaseLiveSub.setString("u_permit_type" , GrantedLevels.READ.getLabel());
            iDfAuthBaseLiveSub.setString("u_own_dept_yn" , ownDeptYn);
            iDfAuthBaseLiveSub.setString("u_author_id"   , authorId.concat("_sub"));
            iDfAuthBaseLiveSub.setString("u_author_type" , AuthorType.DEFAULT.getValue());
            iDfAuthBaseLiveSub.setString("u_create_user" , userId);
            iDfAuthBaseLiveSub.setString("u_create_date" , (new DfTime()).toString());
            iDfAuthBaseLiveSub.setString("u_add_gubun"   , "J");
            
            iDfAuthBaseLiveSub.save();
            
            // Closed 권한 부여
            IDfPersistentObject iDfAuthBaseClosed = adSess.newObject("edms_auth_base");
            iDfAuthBaseClosed.setString("u_obj_id"      , folder.getRObjectId());
            iDfAuthBaseClosed.setString("u_obj_type"    , AuthObjType.FOLDER.getValue());
            iDfAuthBaseClosed.setString("u_doc_status"  , DocStatus.CLOSED.getValue());
            iDfAuthBaseClosed.setString("u_permit_type" , GrantedLevels.READ.getLabel());
            iDfAuthBaseClosed.setString("u_own_dept_yn" , ownDeptYn);
            iDfAuthBaseClosed.setString("u_author_id"   , authorId);
            iDfAuthBaseClosed.setString("u_author_type" , AuthorType.TEAM.getValue());
            iDfAuthBaseClosed.setString("u_create_user" , userId);
            iDfAuthBaseClosed.setString("u_create_date" , (new DfTime()).toString());
            iDfAuthBaseClosed.setString("u_add_gubun"   , "J");
            
            iDfAuthBaseClosed.save();
            
            // Closed _sub 처리
            IDfPersistentObject iDfAuthBaseClosedSub = adSess.newObject("edms_auth_base");
            iDfAuthBaseClosedSub.setString("u_obj_id"      , folder.getRObjectId());
            iDfAuthBaseClosedSub.setString("u_obj_type"    , AuthObjType.FOLDER.getValue());
            iDfAuthBaseClosedSub.setString("u_doc_status"  , DocStatus.CLOSED.getValue());
            iDfAuthBaseClosedSub.setString("u_permit_type" , GrantedLevels.READ.getLabel());
            iDfAuthBaseClosedSub.setString("u_own_dept_yn" , ownDeptYn);
            iDfAuthBaseClosedSub.setString("u_author_id"   , authorId.concat("_sub"));
            iDfAuthBaseClosedSub.setString("u_author_type" , AuthorType.DEFAULT.getValue());
            iDfAuthBaseClosedSub.setString("u_create_user" , userId);
            iDfAuthBaseClosedSub.setString("u_create_date" , (new DfTime()).toString());
            iDfAuthBaseClosedSub.setString("u_add_gubun"   , "J");
            
            iDfAuthBaseClosedSub.save();
          }
          
          for (Doc doc : docList) {
            // Live 권한 부여
            IDfPersistentObject iDfAuthBaseLive = adSess.newObject("edms_auth_base");
            iDfAuthBaseLive.setString("u_obj_id"      , doc.getUDocKey());
            iDfAuthBaseLive.setString("u_obj_type"    , AuthObjType.DOCUMENT.getValue());
            iDfAuthBaseLive.setString("u_doc_status"  , DocStatus.LIVE.getValue());
            iDfAuthBaseLive.setString("u_permit_type" , GrantedLevels.READ.getLabel());
            iDfAuthBaseLive.setString("u_own_dept_yn" , ownDeptYn);
            iDfAuthBaseLive.setString("u_author_id"   , authorId);
            iDfAuthBaseLive.setString("u_author_type" , AuthorType.TEAM.getValue());
            iDfAuthBaseLive.setString("u_create_user" , userId);
            iDfAuthBaseLive.setString("u_create_date" , (new DfTime()).toString());
            iDfAuthBaseLive.setString("u_add_gubun"   , "J");
            
            iDfAuthBaseLive.save();
            
            // Closed 권한 부여
            IDfPersistentObject iDfAuthBaseClosed = adSess.newObject("edms_auth_base");
            iDfAuthBaseClosed.setString("u_obj_id"      , doc.getUDocKey());
            iDfAuthBaseClosed.setString("u_obj_type"    , AuthObjType.DOCUMENT.getValue());
            iDfAuthBaseClosed.setString("u_doc_status"  , DocStatus.CLOSED.getValue());
            iDfAuthBaseClosed.setString("u_permit_type" , GrantedLevels.READ.getLabel());
            iDfAuthBaseClosed.setString("u_own_dept_yn" , ownDeptYn);
            iDfAuthBaseClosed.setString("u_author_id"   , authorId);
            iDfAuthBaseClosed.setString("u_author_type" , AuthorType.TEAM.getValue());
            iDfAuthBaseClosed.setString("u_create_user" , userId);
            iDfAuthBaseClosed.setString("u_create_date" , (new DfTime()).toString());
            iDfAuthBaseClosed.setString("u_add_gubun"   , "J");
            
            iDfAuthBaseClosed.save();
            
            // 문서에 grant
            IDfDocument iDfDoc = (IDfDocument)adSess.getObject(new DfId(doc.getRObjectId()));
            iDfDoc.grant(authorId, GrantedLevels.READ.getLevel(), null);
            iDfDoc.grant(authorId.concat("_sub"), GrantedLevels.READ.getLevel(), null);
            iDfDoc.save();
          }
        }
      }

      // Delete 권한 부여
      if (dto.getUJoinDeptDels() != null) {
        for (String joinDeptCode : dto.getUJoinDeptDels()) {
          String ownDeptYn = orgId.equals(joinDeptCode) ? "Y" : "N";
          
          // 부서의 캐비닛 코드 조회
          VDept vDept = deptDao.selectOneByOrgId(joinDeptCode).orElseThrow(() -> new RuntimeException("전달받은 부서가 존재하지 않는 부서입니다."));
          String authorId = "g_" + vDept.getUCabinetCode();
          
          for (Folder folder : folderList) {
            // Live 권한 부여
            IDfPersistentObject iDfAuthBaseLive = adSess.newObject("edms_auth_base");
            iDfAuthBaseLive.setString("u_obj_id"      , folder.getRObjectId());
            iDfAuthBaseLive.setString("u_obj_type"    , AuthObjType.FOLDER.getValue());
            iDfAuthBaseLive.setString("u_doc_status"  , DocStatus.LIVE.getValue());
            iDfAuthBaseLive.setString("u_permit_type" , GrantedLevels.DELETE.getLabel());
            iDfAuthBaseLive.setString("u_own_dept_yn" , ownDeptYn);
            iDfAuthBaseLive.setString("u_author_id"   , authorId);
            iDfAuthBaseLive.setString("u_author_type" , AuthorType.TEAM.getValue());
            iDfAuthBaseLive.setString("u_create_user" , userId);
            iDfAuthBaseLive.setString("u_create_date" , (new DfTime()).toString());
            iDfAuthBaseLive.setString("u_add_gubun"   , "J");
            
            iDfAuthBaseLive.save();
            
            // Live _sub 처리
            IDfPersistentObject iDfAuthBaseLiveSub = adSess.newObject("edms_auth_base");
            iDfAuthBaseLiveSub.setString("u_obj_id"      , folder.getRObjectId());
            iDfAuthBaseLiveSub.setString("u_obj_type"    , AuthObjType.FOLDER.getValue());
            iDfAuthBaseLiveSub.setString("u_doc_status"  , DocStatus.LIVE.getValue());
            iDfAuthBaseLiveSub.setString("u_permit_type" , GrantedLevels.DELETE.getLabel());
            iDfAuthBaseLiveSub.setString("u_own_dept_yn" , ownDeptYn);
            iDfAuthBaseLiveSub.setString("u_author_id"   , authorId.concat("_sub"));
            iDfAuthBaseLiveSub.setString("u_author_type" , AuthorType.DEFAULT.getValue());
            iDfAuthBaseLiveSub.setString("u_create_user" , userId);
            iDfAuthBaseLiveSub.setString("u_create_date" , (new DfTime()).toString());
            iDfAuthBaseLiveSub.setString("u_add_gubun"   , "J");
            
            iDfAuthBaseLiveSub.save();

            // Closed 권한 부여
            IDfPersistentObject iDfAuthBaseClosed = adSess.newObject("edms_auth_base");
            iDfAuthBaseClosed.setString("u_obj_id"      , folder.getRObjectId());
            iDfAuthBaseClosed.setString("u_obj_type"    , AuthObjType.FOLDER.getValue());
            iDfAuthBaseClosed.setString("u_doc_status"  , DocStatus.CLOSED.getValue());
            iDfAuthBaseClosed.setString("u_permit_type" , GrantedLevels.READ.getLabel());
            iDfAuthBaseClosed.setString("u_own_dept_yn" , ownDeptYn);
            iDfAuthBaseClosed.setString("u_author_id"   , authorId);
            iDfAuthBaseClosed.setString("u_author_type" , AuthorType.TEAM.getValue());
            iDfAuthBaseClosed.setString("u_create_user" , userId);
            iDfAuthBaseClosed.setString("u_create_date" , (new DfTime()).toString());
            iDfAuthBaseClosed.setString("u_add_gubun"   , "J");
            
            iDfAuthBaseClosed.save();
            
            // Closed _sub 처리
            IDfPersistentObject iDfAuthBaseClosedSub = adSess.newObject("edms_auth_base");
            iDfAuthBaseClosedSub.setString("u_obj_id"      , folder.getRObjectId());
            iDfAuthBaseClosedSub.setString("u_obj_type"    , AuthObjType.FOLDER.getValue());
            iDfAuthBaseClosedSub.setString("u_doc_status"  , DocStatus.CLOSED.getValue());
            iDfAuthBaseClosedSub.setString("u_permit_type" , GrantedLevels.READ.getLabel());
            iDfAuthBaseClosedSub.setString("u_own_dept_yn" , ownDeptYn);
            iDfAuthBaseClosedSub.setString("u_author_id"   , authorId.concat("_sub"));
            iDfAuthBaseClosedSub.setString("u_author_type" , AuthorType.DEFAULT.getValue());
            iDfAuthBaseClosedSub.setString("u_create_user" , userId);
            iDfAuthBaseClosedSub.setString("u_create_date" , (new DfTime()).toString());
            iDfAuthBaseClosedSub.setString("u_add_gubun"   , "J");
            
            iDfAuthBaseClosedSub.save();
          }
          
          for (Doc doc : docList) {
            // Live 권한 부여
            IDfPersistentObject iDfAuthBaseLive = adSess.newObject("edms_auth_base");
            iDfAuthBaseLive.setString("u_obj_id"      , doc.getUDocKey());
            iDfAuthBaseLive.setString("u_obj_type"    , AuthObjType.DOCUMENT.getValue());
            iDfAuthBaseLive.setString("u_doc_status"  , DocStatus.LIVE.getValue());
            iDfAuthBaseLive.setString("u_permit_type" , GrantedLevels.DELETE.getLabel());
            iDfAuthBaseLive.setString("u_own_dept_yn" , ownDeptYn);
            iDfAuthBaseLive.setString("u_author_id"   , authorId);
            iDfAuthBaseLive.setString("u_author_type" , AuthorType.TEAM.getValue());
            iDfAuthBaseLive.setString("u_create_user" , userId);
            iDfAuthBaseLive.setString("u_create_date" , (new DfTime()).toString());
            iDfAuthBaseLive.setString("u_add_gubun"   , "J");
            
            iDfAuthBaseLive.save();

            // Closed 권한 부여
            IDfPersistentObject iDfAuthBaseClosed = adSess.newObject("edms_auth_base");
            iDfAuthBaseClosed.setString("u_obj_id"      , doc.getUDocKey());
            iDfAuthBaseClosed.setString("u_obj_type"    , AuthObjType.DOCUMENT.getValue());
            iDfAuthBaseClosed.setString("u_doc_status"  , DocStatus.CLOSED.getValue());
            iDfAuthBaseClosed.setString("u_permit_type" , GrantedLevels.READ.getLabel());
            iDfAuthBaseClosed.setString("u_own_dept_yn" , ownDeptYn);
            iDfAuthBaseClosed.setString("u_author_id"   , authorId);
            iDfAuthBaseClosed.setString("u_author_type" , AuthorType.TEAM.getValue());
            iDfAuthBaseClosed.setString("u_create_user" , userId);
            iDfAuthBaseClosed.setString("u_create_date" , (new DfTime()).toString());
            iDfAuthBaseClosed.setString("u_add_gubun"   , "J");
            
            iDfAuthBaseClosed.save();
            
            // 문서에 grant
            IDfDocument iDfDoc = (IDfDocument)adSess.getObject(new DfId(doc.getRObjectId()));
            if (DocStatus.LIVE.getValue().equals(doc.getUDocStatus())) {
              iDfDoc.grant(authorId, GrantedLevels.DELETE.getLevel(), null);
              iDfDoc.grant(authorId.concat("_sub"), GrantedLevels.DELETE.getLevel(), null); 
            } else {
              iDfDoc.grant(authorId, GrantedLevels.READ.getLevel(), null);
              iDfDoc.grant(authorId.concat("_sub"), GrantedLevels.READ.getLevel(), null); 
            }
            iDfDoc.save();
          }
        }
      }
  
      // 책임자 그룹 생성
      String s_GroupCode = "g_"+ dto.getUPjtCode()+"_pjtmgr";
      IDfGroup idf_GroupObj = (IDfGroup)adSess.getObjectByQualification("dm_group where group_name = '"+ s_GroupCode +"'");
  
      String uChiefId = dto.getUChiefId() == null ? project.getUChief() : dto.getUChiefId();
  
      if (idf_GroupObj != null) {
  			idf_GroupObj.removeAllUsers();
  			idf_GroupObj.addUser(uChiefId);
  
  			idf_GroupObj.save();
  		} else {
  			idf_GroupObj = (IDfGroup)adSess.newObject("dm_group");
  			idf_GroupObj.setGroupName(s_GroupCode);
  			idf_GroupObj.removeAllUsers();
  			idf_GroupObj.addUser(uChiefId);
  
  			idf_GroupObj.save();
  		}
  
      idf_PObj.save();
  
  		// 트랜잭션 커밋
//      idfSession.commitTrans();
      adSess.commitTrans();
  
      return dto.getUPjtCode();
    } finally {
//  	  if (Objects.isNull(session)) {
//    	  if (idfSession != null) {
//    	    if (idfSession.isTransactionActive()) {
//    	      idfSession.abortTrans();
//    	    }
//    	    if (idfSession.isConnected()) {
//    	      idfSession.disconnect();
//    	    }
//    	  }
//  	  }
  	  if (adSess != null) {
  	    if (adSess.isTransactionActive()) {
  	    	adSess.abortTrans();
  	    }
  	    if (adSess.isConnected()) {
  	    	adSess.disconnect();
  	    }
  	  }
    }
  }

  @Override
  public void makeProjectFinished(UserSession userSession, String projectCode) throws Exception {
    IDfSession idfSession = this.getIdfSession(userSession);
    try {
      // 신규 프로젝트 번호를 채번
      Project project = projectDao.selectOneByUPjtCode(projectCode).orElseThrow(() -> new BadRequestException("Not found Project"));
      
      //변경자가 주관부서 소속인지 확인
      if (Objects.isNull(userSession.getUser().getOrgId())) 
        throw new ForbiddenException("프로젝완료 권한이 없습니다.");
      if (!userSession.getUser().getOrgId().equals(project.getUOwnDept())) {
        boolean isAuthed = false;
        if (!Objects.isNull(userSession.getUser().getAddDepts())) {
          for (GwAddJob gd : userSession.getUser().getAddDepts()) {
            if (gd.getUnitCode().equals(project.getUOwnDept())) {
              isAuthed = true;
              break;
            }
          }
        }
        if (!isAuthed) 
          throw new ForbiddenException("프로젝완료 권한이 없습니다.");
      }
//      dto.setUFinishYn("N");                                          // 완료 여부
      
      IDfPersistentObject idf_PObj = idfSession.getObject(new DfId(project.getRObjectId()));
      idf_PObj.setString("u_finish_yn", "Y");
      
      //2022.01.07 추가 JJG( 분류폴더에서  '완료함으로 이동' 을 선택할 수도 있음 Start
      idf_PObj.setString("u_fol_id", "");
      idf_PObj.setString("u_update_user", userSession.getUser().getOrgId());
      idf_PObj.setTime("u_update_date", new DfTime());
      //////// End
      idf_PObj.save();
       
    } finally {
      if (idfSession != null && idfSession.isConnected()) {
        sessionRelease(userSession.getUser().getUserId(), idfSession);
      }
    } 
  }

  @Override
  public List<ProjectRepeatDto> selectRepeatListByCode(String pjtCode) throws Exception {
    return projectDao.selectRepeatListByCode(pjtCode);
  }
}
