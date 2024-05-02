package com.dongkuksystems.dbox.controllers;

import static com.dongkuksystems.dbox.models.api.response.ApiResult.OK;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;
import com.dongkuksystems.dbox.constants.AuthType;
import com.dongkuksystems.dbox.constants.SecLevelCode;
import com.dongkuksystems.dbox.constants.SysObjectType;
import com.dongkuksystems.dbox.daos.type.doc.DocDao;
import com.dongkuksystems.dbox.daos.type.docbox.project.ProjectDao;
import com.dongkuksystems.dbox.daos.type.user.preset.UserPresetDao;
import com.dongkuksystems.dbox.errors.NotFoundException;
import com.dongkuksystems.dbox.models.api.response.ApiResult;
import com.dongkuksystems.dbox.models.common.Documentum;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.path.DPath;
import com.dongkuksystems.dbox.models.dto.type.auth.FolderAuthDto;
import com.dongkuksystems.dbox.models.table.etc.VDept;
import com.dongkuksystems.dbox.models.type.auth.AuthBase;
import com.dongkuksystems.dbox.models.type.auth.AuthShare;
import com.dongkuksystems.dbox.models.type.folder.Folder;
import com.dongkuksystems.dbox.repositories.redis.RedisRepository;
import com.dongkuksystems.dbox.securities.JwtAuthentication;
import com.dongkuksystems.dbox.services.auth.AuthService;
import com.dongkuksystems.dbox.services.data.DataPathService;
import com.dongkuksystems.dbox.services.data.DataRePathService;
import com.dongkuksystems.dbox.services.dept.GwDeptService;
import com.dongkuksystems.dbox.services.doc.DocService;
import com.dongkuksystems.dbox.services.folder.FolderService;
import com.dongkuksystems.dbox.utils.dctm.DCTMUtils;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@Api(tags = "test APIs")
public class TestController extends AbstractCommonController {

  private RedisRepository redisRepository;
  private AuthService authService;
  private Documentum documentum;
  private DocDao docDao;
  private UserPresetDao userPresetDao;
  private ProjectDao projectDao;
  private DocService docService;
  
  ////////////////////////////////////////////////////////// 추가
  private final DataPathService    pathService;
  private final DataRePathService  pathReService;
  private final GwDeptService      gwDeptService;
  private final FolderService      folderService;
  ////////////////////////////////////////////////////////// 추가

  public TestController(UserPresetDao userPresetDao, RedisRepository redisRepository, ProjectDao projectDao, AuthService authService, Documentum documentum, DocDao docDao, DocService docService
		   , DataPathService    pathService
		   , DataRePathService  pathReService
		   , GwDeptService      gwDeptService
		   , FolderService      folderService
		   ) {
    this.userPresetDao = userPresetDao;
    this.redisRepository = redisRepository;
    this.authService = authService;
    this.projectDao = projectDao;
    this.documentum = documentum;
    this.docDao = docDao;
    this.docService = docService;
    
    this.pathService   = pathService;
    this.pathReService = pathReService;
    this.gwDeptService = gwDeptService;
    this.folderService = folderService;
    
  }


  @GetMapping("/dy-test")
  @ApiOperation(value = "test확인")
  public ApiResult<Object> getSession(@AuthenticationPrincipal JwtAuthentication authentication,
      @ApiParam(value = "부서, 프로젝트/투자, 연구과제 코드", example = "UNC50014030") @RequestParam String pjtId) throws Exception {
    UserSession userSession = (UserSession) redisRepository.getObject(authentication.loginId, UserSession.class);
    
    IDfSession idfSession = documentum.getSession(userSession);
    IDfPersistentObject obj = (IDfPersistentObject) idfSession.newObject(SysObjectType.FOLDER.getValue());
    FolderAuthDto rst = authService.selectRootFolderAuths(userSession, AuthType.DEPT_IMP, SecLevelCode.LIVE, "d00009", obj.getObjectId().getId());
    if (rst.getAuthBaseList() != null) {
      for (AuthBase ab : rst.getAuthBaseList()) {
        System.out.println(ab.getUDocStatus() + "__" + ab.getUPermitType() + "__" + ab.getUAuthorId());
      }
    }
    if (rst.getAuthShareList() != null) {
      for (AuthShare ab : rst.getAuthShareList()) {
        System.out.println(ab.getUPermitType() + "__" + ab.getUAuthorId());
      }
    }
    return OK(rst);
  }

  @GetMapping("/testIsRootAuthenticated")
  @ApiOperation(value = "부서, 프로젝트/투자, 연구과제 최상위 권한 보유 여부 확인")
  public ApiResult<Boolean> testIsRootAuthenticated(@AuthenticationPrincipal JwtAuthentication authentication,
  		@ApiParam(value = "부서, 프로젝트/투자, 연구과제 코드", example = "UNC50014030") @RequestParam String hamId) throws Exception {
    boolean result = authService.isRootAuthenticated(hamId, authentication.loginId);
    
    return OK(result);
  }
  
  @GetMapping("/testIsRootAuthenticated_tst")
  @ApiOperation(value = "부서, 프로젝트/투자, 연구과제 최상위 권한 보유 여부 확인")
  public ApiResult<Boolean> testIsRootAuthenticatedTest(@AuthenticationPrincipal JwtAuthentication authentication,
  		@ApiParam(value = "부서, 프로젝트/투자, 연구과제 코드", example = "UNC50014030") @RequestParam String hamId) throws Exception {
    boolean result = authService.isRootAuthenticated(hamId, authentication.loginId);
    
    return OK(result);
  }
  
  
  /**
	 * 복원처리 테스트(타겟지정)
	 * 
	 * @param authentication
	 * @param dataViewCheckoutDto
	 * @param dataId
	 * @return
	 * @throws Exception
	 */

	@GetMapping(value = "/testRestore")
	@ApiOperation(value = "부서, 프로젝트/투자, 연구과제 최상위 권한 보유 여부 확인")
	public ApiResult<Object> getViewCheckRestore(@AuthenticationPrincipal JwtAuthentication authentication,
			@ApiParam(value = "부서, 프로젝트/투자, 연구과제 최상위 권한 보유 여부 확인", example = "0003383f80ce05b2")  @RequestParam String sTargetDboxId ,
		HttpServletRequest request) throws Exception {

		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,	UserSession.class);
		boolean isMobile = request.getHeader("User-Agent").toUpperCase().indexOf("MOBILE") > -1; // TODO flutter에서도 모바일
																									// 여부 확인 가능한지
		final String userId = userSession.getUser().getUserId();
		
		String delUserId = userId ;//잠정적으로 현재 세션사용자id를 지정=> 삭제나 폐기요청한 사용자ID가 와야 함
		
		Map<String, String> body = new HashMap<String, String>();
		
        String  authExclusive =null;
        String  ps_Del =null; 
        //복원시에는 u_delete_status를 ' ' 로 복원한 이후라, 이때는 ps_Del을 " " 로 세팅해야함
        //복원처리 전에 뭔가를 해야한다면 null로 줄 것

        
//////////////////////////////////////////////////////////////////////////
	     String sTargetSecLevel="T";//팀내
	     sTargetDboxId="0003383f8002724b";  // 화면에서 넘어오는 타겟폴더(어디로 이동할건지) '문서디자인'  폴더로
        
        //복원할 폴더를 담는다.
        List<String> sFolArray =  new ArrayList<String>();  //선언 필요
        sFolArray.add("0003383f80ce104c"); //삭제테스트2( 모바일/웹/디자인팀 - 휴지통
        
        
        //복원할 파일을 담는다
        List<String> sFilArray =  new ArrayList<String>(); //선언 필요
        //sFilArray.add("0903383f802b6432"); //복사문서_move
////////////////////////////////////////////////////////////////////////
	    String sSrcgetCabinetCode = userSession.getUser().getDeptCabinetcode();  //웹/모바일/디자인팀 테스트계: d00142, 운영계:d00303
	    String s_PrCode=" " ; //프로젝트,연구투자코드 
	    String s_PrType=" " ; //프로젝트(P), 연구투자(R), 부서함(" ");
	    String sTargetCabinetCode ="";
	    String sSourceGubun       ="";  //문서함 폴더인데
	    String sTargetGubun       ="";  //문서함 폴더로
	    String sTargetFolType     =""; //복원할 폴더 타입
	     
	     //붙여넣기할 폴더정보
	    if(DfId.isObjectId(sTargetDboxId)) {  //타겟아이디에 폴더가 아닌 부서함 코드가 넘어오면 부서함 루트임
	         Optional<Folder> optFolder = folderService.selectOne(sTargetDboxId);
	         if(optFolder.isPresent()) {

	             //타겟폴더에 대한 쓰기(D) 권한이 있는지 체크
	     		    boolean gwonhan = authService.checkFolderAuth(sTargetDboxId, userId, "D");// D(삭제) 권한 보유 여부 학인
	     		    if (!gwonhan) {
	     		 	    throw new RuntimeException("붙여넣기할 폴더에 쓰기권한이 부족합니다");
	     		    }
	        	    sTargetGubun      = optFolder.get().getUFolType();
	        	    sTargetFolType    = optFolder.get().getUFolType();
					sTargetCabinetCode= optFolder.get().getUCabinetCode();//문서함코드
					sTargetSecLevel   = optFolder.get().getUSecLevel();   //보안등급
					s_PrCode          = optFolder.get().getUPrCode();     //프로젝트나 연구과제코드
					s_PrType          = optFolder.get().getUPrCode();     //프로젝트(P), 연구투자(R), 부서함(" ");
	         }
	    }else {
		     sTargetCabinetCode =sTargetDboxId;
		     sSourceGubun       ="DFO";  //문서함 폴더인데
		     sTargetGubun       ="DFO";  //문서함 폴더로		     
		     sTargetFolType     ="DFO";  //복원할 폴더 타입
	     }
	     
	      
	     DPath	dto	= DPath.builder()
					.reqUser(userId) //삭제한 사람 id를 넣을것
					.uptPthGbn("M")  //이동
					.prCode(s_PrCode)
					.prType(s_PrType)
					.srcCabinetcode(sSrcgetCabinetCode)
					.tgCabinetcode(sTargetCabinetCode)
					.sourceGubun(sSourceGubun)
					.targetGubun(sTargetGubun)
					.targetFolType(sTargetFolType)
					.targetDboxId(sTargetDboxId)
					.targetSecLevel(sTargetSecLevel)
					.sourceFolders(sFolArray)
					.sourceFiles(sFilArray)
				.build();

	     
	     String resultMsg="";
	     boolean b_IsSubNoAuth=false;
	     ///////////////////////////////////// 문서 권한 체크 ///////////////////////////////////////////////////
	     int icheckAuth=7;      //Live문서 대상으로 하는 복원은 7, Closed문서 대상으로 하는 폐기쪽은 3 을 줘야함 (★★★★★★★)
	     String ps_Status ="L" ;//Live문서 대상   (★★★★★★★) : 폐기반려는 폴더가 해당되지 않으므로, 이 파라메터는 신경 안써도됨
	     
         Map<String, String> uRecoverMap  = new HashMap<String, String>(); //복원한 대상들의 복원직전 u_delete_status값을 백업해놓음

         /////////////////////////////////// 문서 권한 체크 ///////////////////////////////////////////////////
	     
        try {
		     ps_Del =null ; //u_delete_status값을 " " 로 복원전에 폴더별 권한체크하기 위해서 ps_Del구분자를 null로 줌
		     
		     if(sFilArray.size() > 0) {
		            try {
						for (int i = 0; i < sFilArray.size(); i++) {
							if(sFilArray.get(i).equals("")) continue;
							String s_ObjId 	= sFilArray.get(i);//
							
							boolean b_Auth = authService.checkDocAuth(s_ObjId, delUserId, icheckAuth);//복원
							
				        	 if( !b_Auth) { 
				        		 throw new RuntimeException("권한이 없는 문서가 있음");
							 }
						}
		            }catch(Exception e) {
		            	e.printStackTrace();
		            }
		     }
		     /** 폐기문서 반려처리에서는 sFolArray 부분에 대한 처리 필요없음 ( 위에서 선언만 해놓으면 됨)**/
	         for(int j=0; j < sFolArray.size(); j++) {
					if(sFolArray.get(j).equals("")) continue;
	
					Optional<Folder> optFolder = folderService.selectOne(sFolArray.get(j));
					if(optFolder.isPresent()) {
						authExclusive=null; //전체문서에서 권한있는 문서를 Minus 해서 리스트가 있으면 권한없음 포함
			    	    List<DPath>  NAChkList = pathService.selectNAList(sFolArray.get(j), null, delUserId, authExclusive,  ps_Del, null, dto.getUptPthGbn()); // 하위(전체)에 권한 (없는) 폴더나 문서가 있는지 체크( 폐기-반려처리는 여기와 무관)
			    	    b_IsSubNoAuth	= NAChkList.size() > 0?true:false;
	
						sSourceGubun = optFolder.get().getUFolType();
						sSrcgetCabinetCode= optFolder.get().getUCabinetCode();
						dto.setSourceGubun(sSourceGubun);
						dto.setSrcCabinetcode(sSrcgetCabinetCode);
					}
					break;
	         }
	         if(b_IsSubNoAuth) {  //폴더 하위에 권한없는 문서가 있으면 
	        	 throw new RuntimeException("권한이 없는 문서가 있음");
	         }
	         /*********************************************************복원처리  Start  **********************/
            IDfPersistentObject idfObj = null;
            IDfSession idfSes = DCTMUtils.getAdminSession();
            try {
            	uRecoverMap.put("0003383f80ce104c", "D");
	            idfObj = (IDfPersistentObject)idfSes.getObject(new DfId("0003383f80ce104c"));          idfObj.setString("u_delete_status", "D");          idfObj.save();//최상위폴더(하드코딩중)
	            uRecoverMap.put("0003383f80ce09a2", "P");
	            idfObj = (IDfPersistentObject)idfSes.getObject(new DfId("0003383f80ce09a2"));          idfObj.setString("u_delete_status", null);          idfObj.save();//두번째폴더(하드코딩중)
	            uRecoverMap.put("0903383f802b6432", "P");
	            idfObj = (IDfPersistentObject)idfSes.getObject(new DfId("0903383f802b6432"));          idfObj.setString("u_delete_status", null);          idfObj.save();
	            
	            
		        if (idfSes != null) {
			      if (idfSes.isConnected()) {
			    	 idfSes.disconnect();
			      }
				}
            }catch(Exception e) {
	       		 e.printStackTrace();
	       	 }finally {
			        if (idfSes != null) {
					      if (idfSes.isConnected()) {
					    	 idfSes.disconnect();
					      }
					}
	       	 }
       	 
	         
	         /* Documentum Transaction에서
	          * ...(중략)...
	            IDfPersistentObject idfObj = (IDfPersistentObject)idfSession.getObject(new DfId( s_ObjId));
	            idfObj.setString("u_delete_status", " "); //이런식으로 
	            idfObj.save();
	            
	            //save를 해도 db에 commit된 것은 아니라.
	            //아래쪽에서 selectNAList를 하면 여전히 u_delete_status값이 비어있지 않은 대상들이라 조회가 안될수 있어서 ps_Del은 null을 유지
	             * 
	             ..(중략)...
	        */
	         /*********************************************************복원처리  End  **********************/
	         
	         if(sFolArray.size() < 1) { //파일들만 선택된 경우
	        	 ps_Del =null ; //u_delete_status값이  " " 로 복원된 이후에 이동처리를 하는 경우
	        	 idfSes = DCTMUtils.getAdminSession();
	        	 try {
			         for(int j=0; j < sFilArray.size(); j++) {
							if(sFilArray.get(j).equals("")) continue;
							String s_ObjId 	= sFilArray.get(j);//
							s_ObjId 	= DCTMUtils.getCurrentObjectID(idfSes, s_ObjId); //문서의 최신버전 r_object_id를 가져온다.
							 if(s_ObjId.equals("")) {
							     continue;
							 }
							 IDfDocument idfDocObj = (IDfDocument)idfSes.getObject(new DfId(s_ObjId));
							 Optional<Folder> optFolder = folderService.selectOne(sFolArray.get(j));
							 if(optFolder.isPresent()) {
							     sSourceGubun = optFolder.get().getUFolType();
								 sSrcgetCabinetCode= optFolder.get().getUCabinetCode();
							 }
							 break;
			          }
	        	 }catch(Exception e) {
	        		 e.printStackTrace();
	        	 }finally {
	 		        if (idfSes != null) {
	 				      if (idfSes.isConnected()) {
	 				    	 idfSes.disconnect();
	 				      }
					}
	        	 }
	        	 
	         }
        }catch(RuntimeException e) {
        	throw new RuntimeException(e.getMessage());
        }

        VDept myDept = gwDeptService.selectDeptByOrgId(userSession.getUser().getOrgId());

        dto.setReqUser(userId);
        
        dto.setUComOrgCabinetCd(gwDeptService.selectDeptByOrgId(myDept.getComOrgId()).getUCabinetCode());
	    dto.setUGroupOrgCabinetCd(gwDeptService.selectDeptByOrgId("DKG").getUCabinetCode());
		dto.setUApprover( myDept.getManagerPerId()); //승인자 세팅
		
	      
	    List<String> folList = new ArrayList<String>();
	    
	    Map<String, List<DPath>> uFolMap     = new HashMap<String, List<DPath>>();
		Map<String, Map<String, List<DPath>>> uFolMapTot  = new HashMap<String, Map<String, List<DPath>>>();
		
		authExclusive="NO";
		for(int i=0; i < sFolArray.size(); i++){

		    List<DPath> NAChkList = pathService.selectNAList(sFolArray.get(i),null,delUserId, authExclusive, ps_Del, ps_Status, dto.getUptPthGbn());  // "L" 은 Live문서만 대상( C 문서는 폐기 요청으로 갔음
	    	IDfSession idfSes = DCTMUtils.getAdminSession();
	    	
	    	System.out.println("판단 전=" + NAChkList.toString());
	    	try {
		        for(int k=0; k< NAChkList.size(); k ++) {
		        	if( null==uRecoverMap.get(NAChkList.get(k).getRObjectId()+""))  continue;
		        	
		        	IDfPersistentObject idfObj = (IDfPersistentObject)idfSes.getObject(new DfId( NAChkList.get(k).getRObjectId()));
		        	
		        	if(sFolArray.get(i).equals(idfObj.getString("r_object_id"))) continue;
 		        	 
		        	if(NAChkList.get(k).getListType().equals("DOC")) {  //naList에는 ListType 폴더(FOL), 문서(DOC) 존재함
		        		boolean b_Auth = authService.checkDocAuth(NAChkList.get(k).getRObjectId(), delUserId, 7); //삭제권한이 있는지 확인(Live문서이므로 삭제권한("D"= 7 체크)
		        		if(!b_Auth) {
			        		NAChkList.remove(k);
			        		k--;
		        		}
		        	}
		        	else if( idfObj.getString("u_delete_status").equals("D") ){
		        		NAChkList.remove(k);
		        		k--;
		        		continue;
		        	}
		        	if(null !=idfObj.getString("u_delete_status") && !idfObj.getString("u_delete_status").trim().equals("")) {  //비어있지 않으면 복구되지 않은 것이므로 pass
		        		
		        		NAChkList.remove(k);
		        		k--;
		        		
		        	}else {
		        		System.out.println(idfObj.getString("r_object_id") +" : '"+ idfObj.getString("u_delete_status") +"' 포함");
		        	}
		        	System.out.println(k +" 번째 =" + NAChkList.get(k).getRObjectId());
		        }
		        System.out.println("판단 후=" + NAChkList.toString());    
		     }catch(Exception e) {
	        	 e.printStackTrace();
	             throw new RuntimeException(e.getMessage());
	         }finally {
	 		    if (idfSes != null) {
	 		      if (idfSes.isConnected()) {
	 		   	    idfSes.disconnect();
	 			  }
				}
	        }
		    uFolMap = NAChkList.stream().collect(Collectors.groupingBy(item -> item.getUFolId())); //업폴더ID, 하위내역으로 정리
		    uFolMapTot.put( sFolArray.get(i),  uFolMap);
		}
		
		IDfSession idfSession = DCTMUtils.getAdminSession();
		IDfPersistentObject idfObj = (IDfPersistentObject)idfSession.getObject(new DfId("0003383f80ce09a2"));   idfObj.setString("u_delete_status", "P");          idfObj.save();//두번째폴더(하드코딩중)
		                   idfObj = (IDfPersistentObject)idfSession.getObject(new DfId("0903383f802b6432"));    idfObj.setString("u_delete_status", "P");          idfObj.save();//두번째폴더밑 문서(하드코딩중)
		try{
			if (idfSession.isTransactionActive()) {
		  	       idfSession.beginTrans();
			}

	        resultMsg = pathReService.moveFolderAndFiles(userSession, idfSession, dto, uFolMapTot, isMobile);
		        
			if (idfSession.isTransactionActive()) {
			       idfSession.commitTrans();
			}
		        
		} catch (Exception e) {
			 e.printStackTrace();
		} finally {
		        if (idfSession.isTransactionActive()) {
			    idfSession.abortTrans();
		        }
		        
		        if (idfSession != null) {
			      if (idfSession.isConnected()) {
			        idfSession.disconnect();
			      }
			    }
		}			
        if(resultMsg.equals("")) {
            body.put("flag", "S");
		    body.put("status", "200");
		    body.put("message", "처리처리되었습니다");
        }else {
            body.put("flag", "T");
		    body.put("status", "500");
		    body.put("message", resultMsg);
        	
        }
		return OK(body);
	}
	
	@GetMapping(value = "/aContentTypeUpt")
	@ApiOperation(value = "a_content_type일괄변경: 사용하지 않는 거임")
	public ApiResult<Object> updateAContentType(@AuthenticationPrincipal JwtAuthentication authentication,
		HttpServletRequest request) throws Exception {

		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,	UserSession.class);
		boolean isMobile = request.getHeader("User-Agent").toUpperCase().indexOf("MOBILE") > -1; // TODO flutter에서도 모바일
																									// 여부 확인 가능한지
		final String userId = userSession.getUser().getUserId();
		
		String delUserId = userId ;//잠정적으로 현재 세션사용자id를 지정=> 삭제나 폐기요청한 사용자ID가 와야 함
		IDfSession idfSession = DCTMUtils.getAdminSession();
		
	     IDfCollection	idf_Col    = null;
	     IDfCollection	idf_Col_U  = null;
	     
		 LocalDate now = LocalDate.now();
	     DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
		 String formatedNow = now.format(formatter);
		 LocalTime time = LocalTime.now();
		 DateTimeFormatter timeformat = DateTimeFormatter.ofPattern("HH:mm:ss");
		 String timeformatNow = time.format(timeformat);

		 String ls_UpdateDate=formatedNow+" "+timeformatNow;
	     
		 try {
			 
			 String s_Dql = "  select u_doc_key, a_content_type, u_file_ext, b_name as u_content_type, r_lock_owner111111111 "
                       		+"	               from edms_doc_sp a                   "
                       		+"	     , (select dos_extension, max(name) as b_name   "
                       		+"	           from dm_format_s                         "
                       		+"	           group by dos_extension                   "
                       		+"	        ) b                                         "
                       		+"	where a_content_type like 'unknown'                 "
                       		+"	 and b.dos_extension = lower(a.u_file_ext)   and a.i_has_folder=1              "
                       		+"	 and (a.u_file_ext !=' ' and a.u_file_ext !='')    and u_doc_key not in('090315d980009142','090315d980009143')  " ;
                         
			 
   					//+"  from(    select u_doc_key, a_content_type, u_file_ext, (select max(name) from dm_format b where b.dos_extension = lower(a.u_file_ext)) as u_content_type  from edms_doc a where a_content_type like 'unknown'  \n" + 
   			 		//"    ) n" + 
   			 		//"    where u_content_type !=''  " ;
   			 idf_Col = DCTMUtils.getCollectionByDQL(idfSession, s_Dql, DfQuery.DF_QUERY);
 			 while(idf_Col != null && idf_Col.next()) {
 					 if(!idf_Col.getString("r_lock_owner").equals("")) {
 						 System.out.println("편집중:" + idf_Col.getString("u_doc_key"));
 						 continue;
 					 }
 					 
		  			 String s_UptDql = "UPDATE edms_doc  OBJECTS SET a_content_type2222222222='unknown' "+  
		  					              "     , SET u_update_date=DATE('"+ ls_UpdateDate + "','mm/dd/yyyy hh:mm:ss')"+
							         "WHERE u_doc_key='"+ idf_Col.getString("u_doc_key") +"' ";

		  			 System.out.println(idf_Col.getString("u_doc_key"));
		  			 String ps_DocKey =idf_Col.getString("u_doc_key");
		  			
		  			 String ls_RobjectId	= DCTMUtils.getCurrentObjectID(idfSession, ps_DocKey); //문서의 최신버전 r_object_id를 가져온다.
		  			
		  			IDfDocument idf_Doc = (IDfDocument)idfSession.getObject(new DfId(ls_RobjectId));
		  			
		   			if(!ps_DocKey.equals(idf_Doc.getObjectId().toString()))
		  			{
		  				String s_VerId = "";
		  				IDfDocument idf_VerDoc = null;
		  				IDfCollection idf_VerCol = idf_Doc.getVersions(null);
		  				
		  				while(idf_VerCol != null && idf_VerCol.next())
		  				{
		  					s_VerId = idf_VerCol.getString("r_object_id");
		  					//과거 버전만 처리하기 위해 현재 버전은 skip
		  					if(s_VerId.equals(idf_Doc.getObjectId().toString()))
		  						continue;
		  					
		  					//권한은 현재 버전의 ACL Name을 사용하면 그대로 적용 됨
		  					idf_VerDoc = (IDfDocument)idfSession.getObject(new DfId(s_VerId));
		  					idf_VerDoc.setString("a_content_type", idf_Col.getString("u_content_type") );
		  					idf_VerDoc.save();
		  				}
		  				if(idf_VerCol != null) idf_VerCol.close(); 
		  			}		  			 
		  			 
					 IDfQuery 		idf_Qry 	= null;
					 idf_Qry = new DfQuery();
					 idf_Qry.setDQL(s_UptDql);
					 idf_Col_U = idf_Qry.execute(idfSession, DfQuery.QUERY);
					 if (idf_Col_U!=null && idf_Col_U.next()) idf_Col_U.close();
 				 }					 
			 }catch( Exception e) {
				 e.printStackTrace();
			 }
			 finally {
			   	idfSession.disconnect();
			    if(idf_Col != null) idf_Col.close();
			    if(idf_Col_U != null) idf_Col_U.close();
			}
									 		
		
		Map<String, String> body = new HashMap<String, String>();
	    body.put("flag", "T");
	    body.put("status", "500");
	    body.put("message", "처리종료");		
	    return OK(body);
	}
	
	@GetMapping(value = "/fileExtUpdate")
	@ApiOperation(value = "실제파일 확장자 일괄 Update")
	public ApiResult<Object> updateExtrUpdate(@AuthenticationPrincipal JwtAuthentication authentication,
		HttpServletRequest request) throws Exception {

		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,	UserSession.class);
		boolean isMobile = request.getHeader("User-Agent").toUpperCase().indexOf("MOBILE") > -1; // TODO flutter에서도 모바일
																									// 여부 확인 가능한지
		final String userId = userSession.getUser().getUserId();
		
		String delUserId = userId ;//잠정적으로 현재 세션사용자id를 지정=> 삭제나 폐기요청한 사용자ID가 와야 함
		IDfSession idfSession = DCTMUtils.getAdminSession();
		
	     IDfCollection	idf_Col    = null;
	     IDfCollection	idf_Col_U  = null;
	     
		 LocalDate now = LocalDate.now();
	     DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
		 String formatedNow = now.format(formatter);
		 LocalTime time = LocalTime.now();
		 DateTimeFormatter timeformat = DateTimeFormatter.ofPattern("HH:mm:ss");
		 String timeformatNow = time.format(timeformat);

		 String ls_UpdateDate=formatedNow+" "+timeformatNow;
	     
		 try {
			 
			 String s_Dql = "  select u_doc_key, a_content_type, u_file_ext, b_name as u_content_type, r_lock_owner "
                       		+"	               from edms_doc_sp a                   "
                       		+"	     , (select dos_extension, max(name) as b_name   "
                       		+"	           from dm_format_s                         "
                       		+"	           group by dos_extension                   "
                       		+"	        ) b                                         "
                       		+"	where a_content_type like 'unknown'                 "
                       		+"	 and b.dos_extension = lower(a.u_file_ext)   and a.i_has_folder=1              "
                       		+"	 and (a.u_file_ext !=' ' and a.u_file_ext !='')    and u_doc_key not in('090315d980009142','090315d980009143')  " ;
                         
			 
   					//+"  from(    select u_doc_key, a_content_type, u_file_ext, (select max(name) from dm_format b where b.dos_extension = lower(a.u_file_ext)) as u_content_type  from edms_doc a where a_content_type like 'unknown'  \n" + 
   			 		//"    ) n" + 
   			 		//"    where u_content_type !=''  " ;
   			 idf_Col = DCTMUtils.getCollectionByDQL(idfSession, s_Dql, DfQuery.DF_QUERY);
 			 while(idf_Col != null && idf_Col.next()) {
 					 if(!idf_Col.getString("r_lock_owner").equals("")) {
 						 System.out.println("편집중:" + idf_Col.getString("u_doc_key"));
 						 continue;
 					 }
 					 
		  			 String s_UptDql = "UPDATE edms_doc  OBJECTS SET a_content_type='"+idf_Col.getString("u_content_type") +"' "+  
		  					              "     , SET u_update_date=DATE('"+ ls_UpdateDate + "','mm/dd/yyyy hh:mm:ss')"+
							         "WHERE u_doc_key='"+ idf_Col.getString("u_doc_key") +"' ";

		  			 System.out.println(idf_Col.getString("u_doc_key"));
		  			 String ps_DocKey =idf_Col.getString("u_doc_key");
		  			
		  			 String ls_RobjectId	= DCTMUtils.getCurrentObjectID(idfSession, ps_DocKey); //문서의 최신버전 r_object_id를 가져온다.
		  			
		  			IDfDocument idf_Doc = (IDfDocument)idfSession.getObject(new DfId(ls_RobjectId));
		  			
		   			if(!ps_DocKey.equals(idf_Doc.getObjectId().toString()))
		  			{
		  				String s_VerId = "";
		  				IDfDocument idf_VerDoc = null;
		  				IDfCollection idf_VerCol = idf_Doc.getVersions(null);
		  				
		  				while(idf_VerCol != null && idf_VerCol.next())
		  				{
		  					s_VerId = idf_VerCol.getString("r_object_id");
		  					//과거 버전만 처리하기 위해 현재 버전은 skip
		  					if(s_VerId.equals(idf_Doc.getObjectId().toString()))
		  						continue;
		  					
		  					//권한은 현재 버전의 ACL Name을 사용하면 그대로 적용 됨
		  					idf_VerDoc = (IDfDocument)idfSession.getObject(new DfId(s_VerId));
		  					idf_VerDoc.setString("a_content_type", idf_Col.getString("u_content_type") );
		  					idf_VerDoc.save();
		  				}
		  				if(idf_VerCol != null) idf_VerCol.close(); 
		  			}		  			 
		  			 
					 IDfQuery 		idf_Qry 	= null;
					 idf_Qry = new DfQuery();
					 idf_Qry.setDQL(s_UptDql);
					 idf_Col_U = idf_Qry.execute(idfSession, DfQuery.QUERY);
					 if (idf_Col_U!=null && idf_Col_U.next()) idf_Col_U.close();
 				 }					 
			 }catch( Exception e) {
				 e.printStackTrace();
			 }
			 finally {
			   	idfSession.disconnect();
			    if(idf_Col != null) idf_Col.close();
			    if(idf_Col_U != null) idf_Col_U.close();
			}
									 		
		
		Map<String, String> body = new HashMap<String, String>();
	    body.put("flag", "T");
	    body.put("status", "500");
	    body.put("message", "처리종료");		
	    return OK(body);
	}
	
}
