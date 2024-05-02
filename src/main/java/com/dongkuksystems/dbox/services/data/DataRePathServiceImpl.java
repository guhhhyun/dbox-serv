package com.dongkuksystems.dbox.services.data;
import static com.dongkuksystems.dbox.models.api.response.ApiResult.OK;

import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfGroup;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfTime;
import com.dongkuksystems.dbox.constants.AuthorType;
import com.dongkuksystems.dbox.constants.ComCodeType;
import com.dongkuksystems.dbox.constants.DCTMConstants;
import com.dongkuksystems.dbox.constants.DocLogItem;
import com.dongkuksystems.dbox.constants.DocStatus;
import com.dongkuksystems.dbox.constants.FolderStatus;
import com.dongkuksystems.dbox.constants.GrantedLevels;
import com.dongkuksystems.dbox.constants.HamType;
import com.dongkuksystems.dbox.constants.SysObjectType;
import com.dongkuksystems.dbox.daos.table.kakao.KakaoDao;
import com.dongkuksystems.dbox.daos.type.docbox.project.ProjectDao;
import com.dongkuksystems.dbox.daos.type.docbox.research.ResearchDao;
import com.dongkuksystems.dbox.daos.type.manager.preservationperiod.PreservationPeriodDao;
import com.dongkuksystems.dbox.daos.type.path.PathDao;
import com.dongkuksystems.dbox.errors.BadRequestException;
import com.dongkuksystems.dbox.errors.NotFoundException;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.path.DPath;
import com.dongkuksystems.dbox.models.dto.type.auth.CheckAuthParam;
import com.dongkuksystems.dbox.models.dto.type.auth.FolderAuthDto;
import com.dongkuksystems.dbox.models.dto.type.auth.HamInfoResult;
import com.dongkuksystems.dbox.models.dto.type.auth.RegistAuthShareDto;
import com.dongkuksystems.dbox.models.dto.type.docbox.project.ProjectCreateDto;
import com.dongkuksystems.dbox.models.dto.type.docbox.research.ResearchCreateDto;
import com.dongkuksystems.dbox.models.dto.type.folder.RegistFolderDto;
import com.dongkuksystems.dbox.models.dto.type.kakao.KakaoDetail;
import com.dongkuksystems.dbox.models.dto.type.manager.preservationperiod.PreservationPeriodDto;
import com.dongkuksystems.dbox.models.table.etc.VDept;
import com.dongkuksystems.dbox.models.table.etc.VUser;
import com.dongkuksystems.dbox.models.table.log.LogDoc;
import com.dongkuksystems.dbox.models.type.auth.AuthBase;
import com.dongkuksystems.dbox.models.type.docbox.Project;
import com.dongkuksystems.dbox.models.type.docbox.Research;
import com.dongkuksystems.dbox.models.type.request.ReqUseUsb;
import com.dongkuksystems.dbox.models.type.user.UserPreset;
import com.dongkuksystems.dbox.securities.JWT;
import com.dongkuksystems.dbox.services.AbstractCommonService;
import com.dongkuksystems.dbox.services.auth.AuthService;
import com.dongkuksystems.dbox.services.code.CodeService;
import com.dongkuksystems.dbox.services.dept.GwDeptService;
import com.dongkuksystems.dbox.services.folder.FolderService;
import com.dongkuksystems.dbox.services.notification.NotificationService;
import com.dongkuksystems.dbox.services.project.ProjectService;
import com.dongkuksystems.dbox.services.research.ResearchService;
import com.dongkuksystems.dbox.utils.CommonUtils;
import com.dongkuksystems.dbox.utils.dctm.DCTMUtils;


@Service
public class DataRePathServiceImpl extends AbstractCommonService implements DataRePathService {

  
  private final FolderService folderService ;
  private final AuthService authService;  
  private final CodeService codeService;
  private final GwDeptService gwDeptService;
  private final JWT jwt;
  private final PathDao pathDao;
  private final ProjectDao projectDao;
  private final ResearchDao researchDao;
  
  private final ProjectService projectService;
  private final ResearchService researchService;
  
  private final DataPathService dataPathService;
  
  private Map<String, Integer> secLevelMap;
  private Map<String, String> uSrcMap;
  private Map<String, String> uTgtMap;
  private Map<String, String> psrvPMap; //보존년한맵
  private Map<String, String> psrvPMapDf; //보존년한맵(기본값)
  private final PreservationPeriodDao psrvPeriodDao;

  public DataRePathServiceImpl( AuthService authService, CodeService codeService
		     , JWT jwt, PathDao pathDao
		     , FolderService folderService,GwDeptService gwDeptService
		     , ProjectService projectService, ResearchService researchService
		     , ProjectDao projectDao, ResearchDao researchDao
		     , PreservationPeriodDao psrvPeriodDao
		     , DataPathService dataPathService
		     ) {
	this.authService     = authService;
    this.codeService     = codeService;
    this.jwt = jwt;
    this.pathDao = pathDao   		;
    this.folderService=folderService;
    this.gwDeptService=gwDeptService;
    this.projectService = projectService;
    this.researchService = researchService;
    this.projectDao=projectDao;
    this.researchDao=researchDao;
    this.psrvPeriodDao = psrvPeriodDao;

    this.secLevelMap = new HashMap<String, Integer>();
	this.uSrcMap  = new HashMap<String, String>();
	this.uTgtMap  = new HashMap<String, String>();
	
	this.psrvPMap  = new HashMap<String, String>();
	this.psrvPMapDf= new HashMap<String, String>();
	this.dataPathService = dataPathService;
    
    
  }

  //리스트 정렬용
  class uFolderAscending implements Comparator<String>{
	    public int compare(String a, String b)
		{
			return a.compareTo(b);
		}
  }

  
  void initMapInfo() {
	    //보안등급 비교용 Map
		this.secLevelMap.put("S", 4);
		this.secLevelMap.put("T", 3);
		this.secLevelMap.put("C", 2);
		this.secLevelMap.put("G", 1);
		this.secLevelMap.put(null,  0);
		this.secLevelMap.put("",  0);
		
  	
		//RE:생성이력, DEL:삭제이력
		this.uSrcMap.put("D", "DEL") ;// 부서함
		this.uSrcMap.put("P", "DEL") ;// 프로젝트/투자
		this.uSrcMap.put("R", "DEL") ;// 연구과제
		this.uSrcMap.put("M", "DEL") ;// 연구과제
		
		this.uSrcMap.put("DI", "RE"); //중요문서함

		
		//RE:생성이력, DEL:삭제이력
		this.uTgtMap.put("D", "RE") ;// 부서함
		this.uTgtMap.put("S", "RE") ;// 공유/협업
		this.uTgtMap.put("P", "RE") ;// 프로젝트/투자
		this.uTgtMap.put("R", "RE") ;// 연구과제
		this.uTgtMap.put("C",  "RE") ;// 조직함(회사)
		this.uTgtMap.put("M",  "RE") ;// 조직함(관리팀)
		
		this.uTgtMap.put("DI", "DEL"); //중요문서함
		
		//보존년한 기본
		psrvPMapDf.put("S", "20");
		psrvPMapDf.put("C", "10");
		psrvPMapDf.put("G", "10");
		psrvPMapDf.put("T", "15");
		

  }

    
    
    
    //이동시 대상 폴더의 상위폴더와 cabinet 변경  (edms_folder)
    private void uptFolderPath(IDfSession idfSession, String uUpFolId, String rObjectId, String ps_RcevCab, String crUser, DPath dto) throws Exception{

    	try {
    		
    		IDfPersistentObject idf_PObj = idfSession.getObject(new DfId(rObjectId));
    		
    		//---------------------------------------------------------------
    		// 속성 값 수정
    		//---------------------------------------------------------------
    		if(uUpFolId !=null && null ==dto.getPrCode())
    			 idf_PObj.setString("u_up_fol_id"    ,  uUpFolId);
    		else
    			idf_PObj.setString("u_up_fol_id"    ,  uUpFolId);

    		idf_PObj.setString("u_cabinet_code"	,  ps_RcevCab);
    		idf_PObj.setString("u_fol_status", "O");

    		
    		
    		idf_PObj.setString("u_pr_code", dto.getPrCode());
    		idf_PObj.setString("u_pr_type", dto.getPrType());

    		idf_PObj.setString("u_fol_type", dto.getTargetFolType())  ;
    		idf_PObj.setString("u_delete_status", " ");
    		
    		//if (secLevelMap.get(idf_PObj.getString("u_sec_level")) < secLevelMap.get(dto.getTargetSecLevel())) {
    		//	idf_PObj.setString("u_sec_level", dto.getTargetSecLevel() );
    		//}

			idf_PObj.save();
    		//---------------------------------------------------------------
    	} catch (Exception e) {
			e.printStackTrace();
	    }
    }    
    
    
    //이동시 링크파일의 상퓌 폴더 변경  (edms_doc_link)
    private void uptDocLink(IDfSession idfSession, String uUpFolId, String rObjectId, String ps_RcevCab, String crUser, DPath dto) throws Exception{
    	try {
    		
    		IDfPersistentObject idf_PObj = idfSession.getObject(new DfId(rObjectId));
    		
    		//---------------------------------------------------------------
    		// 속성 값 수정
    		//---------------------------------------------------------------
    		idf_PObj.setString("u_fol_id"	    ,  uUpFolId);
    		idf_PObj.setString("u_cabinet_code"	,  ps_RcevCab);
    		//idf_PObj.setString("u_author_id"	, "g_" + ps_RcevCab);
			idf_PObj.setString("u_create_user"	, crUser);
			idf_PObj.setTime  ("u_create_date"	, new DfTime());

    		//if (secLevelMap.get(idf_PObj.getString("u_sec_level")) < secLevelMap.get(dto.getTargetSecLevel())) {
    		//	idf_PObj.setString("u_sec_level", dto.getTargetSecLevel() );
    		//}
			
			idf_PObj.save();
    		//---------------------------------------------------------------
    	} catch (Exception e) {
			e.printStackTrace();
	    }    	
    }    
    
    //이동처리시에 동일한 이름의 폴더가 있어서 폴더함침시 소스쪽 폴더정보를 삭제처리
    private void delHapFolderAuthBase(UserSession userSession, IDfSession idfSession, String rObjectId) throws Exception{
    	IDfCollection idf_Col=null;
    	try {  
    		
				//edms_auth_base 관련 정보 지움
				String s_Dql = "delete edms_auth_base object " +
						" where u_obj_id = '" + rObjectId + "' and u_add_gubun='G' ";
				
				idf_Col = DCTMUtils.getCollectionByDQL(idfSession, s_Dql, DfQuery.DF_QUERY);
				if(idf_Col != null) idf_Col.close();

				s_Dql = "delete edms_auth_share object " +
						" where u_obj_id = '" + rObjectId + "'";
				
				idf_Col = DCTMUtils.getCollectionByDQL(idfSession, s_Dql, DfQuery.DF_QUERY);
				if(idf_Col != null) idf_Col.close();
				
    	}catch (Exception e) {
			e.printStackTrace();
	    }finally {
	    	if(idf_Col != null) idf_Col.close();
	    }
    }    
    
    //폴더의 기본 auth 리스트 반환
    private List<String> getFolderAuthorList(UserSession userSession, IDfSession idfSession, String rObjectId, DPath dto ) throws Exception{
    	
		    if( (!dto.getSourceGubun().equals(dto.getTargetFolType()) ) ){  //&& dto.getUptPthGbn().equals("C")) || dto.getUptPthGbn().equals("M")) {
			    String s_Dql_d = "delete edms_auth_base object " +
				         " where u_obj_id = '" + rObjectId + "' and ( u_add_gubun='S' )";  //속성에서 추가되거나 다른 경로로 추가된 것들은 그대로 유지하기 우해서 u_add_gubun 조건 사용
		        IDfCollection idf_Col_D = DCTMUtils.getCollectionByDQL(idfSession, s_Dql_d, DfQuery.DF_QUERY);
		        if(idf_Col_D != null) idf_Col_D.close();
			}
		
			
			String s_ComId    = dto.getTgComId();
			String ps_RcevCab = dto.getTgCabinetcode();

			Map<String, Integer> map_Author =new HashMap<String,Integer>();	        	
		
			if(dto.getPrType().equals("") || dto.getPrType().equals(" ")) { //부서함이거나 사별함
				if(dto.getTargetFolType().substring(0,1).equals("C")) {  //사별함
			    	  map_Author = DCTMConstants.COM_FOLDER_HAM_AUTH_MAP;
		
				}else if(dto.getTargetFolType().substring(0,1).equals("M")) { //공용문서함
		    	      switch(ComCodeType.findByValue(s_ComId)) {      
		    	      case DKS:
		    	    	map_Author = DCTMConstants.DEFAULT_FOLDER_COM_DKS_HAM_MAP;
		    	        break;
		    	      case ITG:
		    	    	map_Author = DCTMConstants.DEFAULT_FOLDER_COM_ITG_HAM_MAP;
		    	        break;
		    	      case FEI:
		    	    	map_Author = DCTMConstants.COM_FOLDER_HAM_AUTH_MAP;
		    	        break;
		    	      default:
		    	    	break;
		    	      }
				}else if(dto.getTargetFolType().substring(0,1).equals("D")) {  //부서
					
					if(!dto.getTargetGubun().substring(0,2).equals("DI")) {
					    map_Author = DCTMConstants.TEAM_FOLDER_AUTH_MAP;     //부서일반
				    }else {
					    map_Author = DCTMConstants.TEAM_FOLDER_IMP_AUTH_MAP; //중요문서
				    }
				}
			}else { //프로젝트,연구과제
			    if(dto.getPrType().equals("P")) {
			    	map_Author = DCTMConstants.PJT_FOLDER_AUTH_MAP;
		
			    }else if(dto.getPrType().equals("R")) {
			    	map_Author = DCTMConstants.RSCH_FOLDER_AUTH_MAP;
			    }
			}
			List<String> lst_Author = new ArrayList<String>();
			for (Entry<String, Integer> entry : map_Author.entrySet()) {
				String s_AuthorId= entry.getKey();
				s_AuthorId =s_AuthorId.replace( DCTMConstants.COM_CODE_STR, s_ComId).replace( DCTMConstants.CABINET_CODE_STR, ps_RcevCab).replace( DCTMConstants.DEFAULT_CABINET_STR, ps_RcevCab);
				s_AuthorId =s_AuthorId.replace( DCTMConstants.PJT_CODE_STR, dto.getPrCode()).replace( DCTMConstants.RSCH_CODE_STR, dto.getPrCode());
				s_AuthorId =s_AuthorId.replace( DCTMConstants.PJT_CODE_STR, dto.getPrCode()).replace( DCTMConstants.RSCH_CODE_STR, dto.getPrCode());
				
				lst_Author.add(s_AuthorId);
			}
			return lst_Author;
    }
    
    //폴더의 기본 auth Share 리스트 반환
    private boolean setFolderAuthShareList(UserSession userSession, IDfSession idfSession, String uFolId, String rObjectId , DPath dto, String p_ObjType ) throws Exception{
    	

	    String s_Dql = "select * from edms_auth_share where u_obj_id ='"+ uFolId +"'" ;
		
  		IDfQuery 		idf_Qry 	= null;
  		idf_Qry = new DfQuery();
  		idf_Qry.setDQL(s_Dql);
  			
  		IDfCollection idf_Col=null;
  		try {
  			    idf_Col = idf_Qry.execute(idfSession,DfQuery.QUERY);
  		} catch (Exception e) {
  			e.printStackTrace();
  			return false;
  		}
  		try {
		    
	  		String lDocStatus[] = {"L","C"};
	  		IDfDocument idfDoc_Obj =null;
  		    if(p_ObjType.equals("D")){//문서
  		    	idfDoc_Obj =(IDfDocument)idfSession.getObject(new DfId(rObjectId));
  		    }

  		    //권한자를 READ, DELETE 두 군데 모두 넣은 경우, Closed권한에 넣을때는 한 번만 넣도록 한다
  		    Map<String,String> imiMap= new HashMap<String,String>();

	  		while(idf_Col != null && idf_Col.next())
	  		{
	  		    String s_Author=idf_Col.getString("u_author_id");
	  		    if(null== s_Author || s_Author.equals("g_null")|| s_Author.equals("") || s_Author.equals(" ")) continue;

	  		    String s_AuthorType=idf_Col.getString("u_author_type");
	  		    String s_PermitType=idf_Col.getString("u_permit_type");
	  		    
	  		    //IDfPersistentObject idf_PObj=null;

	  		    if(p_ObjType.equals("F")){  //폴더인 경우
	  		    	IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_share");
		  			idf_PObj.setString("u_obj_id"		, rObjectId);
		  			idf_PObj.setString("u_author_id"		, s_Author);
		  			idf_PObj.setString("u_author_type"		, s_AuthorType);
		  			idf_PObj.setString("u_permit_type"		, s_PermitType);
		  			idf_PObj.setString("u_create_user"		, dto.getReqUser());
		  			idf_PObj.setTime  ("u_create_date"	, new DfTime());
		  			idf_PObj.save();
	  		    }else if(p_ObjType.equals("D")){
	  			
	  		    	//rObjectId 	= DCTMUtils.getCurrentObjectID(idfSession, rObjectId); //문서의 최신버전 r_object_id를 가져온다.
	  		    	String ls_UDocKey = idfDoc_Obj.getString("u_doc_key");
	  		    	
		  		    if(s_AuthorType.equals("D") ) s_Author="g_"+ s_Author;
		  		  
		  		    for(int i=0; i< lDocStatus.length; i++) { 
		  		    	
		  		    	System.out.println(lDocStatus[i]+ " : "+ s_Author+" : "+ s_AuthorType +" : "+ ls_UDocKey);
		  		    	
						//if(s_Author.equals("g_" + dto.getTgCabinetcode())) continue; //복사자부서, 
						//if(s_Author.equals(dto.getReqUser())) continue;              //복사자는 ... auth_base 추가시에 'D'삭제권한으로 추가해주므로 중복되지 않게 제거 
						
						if(lDocStatus[i].equals("C")) {
							if(null !=imiMap.get(s_Author))
							    imiMap.put(s_Author,"1");
							else
								continue;
							s_PermitType="R";
							//if(idfDoc_Obj.getString("u_sec_level").equals("S") && idfDoc_Obj.getString("u_doc_status").equals("C")) continue;  //closed의 제한등급이면 pass
						}
						//System.out.println("========"+lDocStatus[i]+ " : "+ s_Author+" : "+ s_AuthorType +" : "+ ls_UDocKey+" : "+ s_PermitType);
//if(s_Author.equals("jungkyo.jung")) s_Author="changmyoun.ji";
				  	    int i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='"+lDocStatus[i]+"'  and u_author_type ='S' ");
				  	    if(i_AuthorCnt < 1) {

				  	    	IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");

				  			idf_PObj.setString("u_obj_id"		, ls_UDocKey);  //문서는 문서Key를 objId에 넣도록 한다(2022.01.06 확인)
				  			idf_PObj.setString("u_obj_type"		, p_ObjType);    //폴더, 문서 
				  			idf_PObj.setString("u_doc_status"	, lDocStatus[i]);
				  			idf_PObj.setString("u_permit_type"	, s_PermitType);
							idf_PObj.setString("u_own_dept_yn"	, s_Author.equals("g_" + dto.getTgCabinetcode()) ? "Y" : "");
							idf_PObj.setString("u_author_id"	, s_Author);
							idf_PObj.setString("u_author_type"	, s_AuthorType.equals("S")?"U":s_AuthorType);
				  			
				  			idf_PObj.setString("u_create_user"	, dto.getReqUser());
				  			idf_PObj.setTime  ("u_create_date"	, new DfTime());
				  			idf_PObj.setString("u_add_gubun"	, "S");  //공유-협업그룹
				  			
				  			idf_PObj.save();
				  	    }
		  		    }
		  		    
		  		    if(p_ObjType.equals("D") && null !=idfDoc_Obj){//문서
					//if(s_Author.equals("g_" + dto.getTgCabinetcode())) continue; //복사자부서, 
					//if(s_Author.equals(dto.getReqUser())) continue;              //복사자는 ... auth_base 추가시에 'D'삭제권한으로 추가해주므로 중복되지 않게 제거 
	  		    	
	  		    	  if( idfDoc_Obj.getPermitEx( s_Author) != GrantedLevels.findByLabel(idf_Col.getString("u_permit_type"))) {
  		    		    
	  		    		idfDoc_Obj.grant(s_Author, GrantedLevels.findByLabel(idf_Col.getString("u_permit_type")), ""); //삭제가능 grant 권한 부여
 		    		    
	  		    		//if(s_AuthorType.equals("D")) {
	  		    		    //if(!idfDoc_Obj.getString("u_sec_level").equals("S") && !idfDoc_Obj.getString("u_sec_level").equals("T")) {
	  		    		    //idfDoc_Obj.grant(s_Author+"_sub", GrantedLevels.findByLabel(idf_Col.getString("u_permit_type")), ""); //삭제가능 grant 권한 부여
	  		    		    //}
	  		    		//    idfDoc_Obj.save();
	  		    	    //}
	  		    	  }
				    }
	  		    }
	  	    }
	  		if(idf_Col != null) idf_Col.close();
  		}catch (Exception e) {
   			e.printStackTrace();
   			return false;
	    }finally {
	    	if(idf_Col != null) idf_Col.close();
	    }
  		return true ;
    }
	
    
    
    //프로젝트 하위로 들어가게 되면 , 프로젝트의 참여부서(읽기, 쓰기)들을 auth_share 사용자로 등록한다.
    private boolean setProjectJoinDeptToAuthbase(UserSession userSession, IDfSession idfSession, String rObjectId , DPath dto, String p_ObjType ) throws Exception{

	    String s_Dql = "select * from edms_project where u_pjt_code ='"+ dto.getPrCode()+"'" ;
	    if(dto.getPrType().equals("R"))
	    	s_Dql = "select * from edms_research where u_rsch_code ='"+ dto.getPrCode()+"'" ;
		
  		IDfQuery 		idf_Qry 	= null;
  		idf_Qry = new DfQuery();
  		idf_Qry.setDQL(s_Dql);
  			
  		IDfCollection idf_Col=null;
  		try {
  			    idf_Col = idf_Qry.execute(idfSession,DfQuery.QUERY);
  		} catch (Exception e) {
  			e.printStackTrace();
  			return false;
  		}
  		try {
		    
	  		IDfDocument         idf_DocObj =null;
	  		IDfPersistentObject idfFol_Obj =null;
	  		
	  		String s_USecLevel="";
	  		if(p_ObjType.equals("D")) {
		        idf_DocObj =(IDfDocument)idfSession.getObject(new DfId(rObjectId));
		        s_USecLevel=idf_DocObj.getString("u_sec_level");
	  		}else if(p_ObjType.equals("F")) {
  		    	idfFol_Obj =idfSession.getObject(new DfId(rObjectId));
  		    	s_USecLevel=idfFol_Obj.getString("u_sec_level");
  		    }


	  		String s_AddGubun="J";//프로젝트 참여부서들은 u_add_gubun값  J 로 등록한다(*2021.12.16 soik.cha 공지.메신져)
	  		List<String> lDocStatus = new ArrayList<String>();
	  		if(!s_USecLevel.equals("S")) lDocStatus.add("C");
	  		lDocStatus.add("L");
	  		
	  		while(idf_Col != null && idf_Col.next())
	  		{
	  			IDfPersistentObject idf_PjtObj = idfSession.getObject(new DfId(idf_Col.getString("r_object_id")));
	  			
	  			String s_MDepts = idf_PjtObj.getAllRepeatingStrings("u_own_dept", ",");
	  			
	  			String s_RDepts = idf_PjtObj.getAllRepeatingStrings("u_join_dept_read", ",");
	  			String s_DDepts = idf_PjtObj.getAllRepeatingStrings("u_join_dept_del", ",");
	  			
	  			String s_ChiefId = idf_PjtObj.getString("u_chief_id"); //책임자ID
	  			
	  			if(p_ObjType.equals("D") ) {//문서
	  				String ls_MaxPermit="D";
	  				if(idf_DocObj.getString("u_doc_status").equals("C")) ls_MaxPermit="R";
	  				
	  			    if( idf_DocObj.getPermitEx( s_ChiefId) != GrantedLevels.findByLabel(ls_MaxPermit)) {
		    	        idf_DocObj.grant(s_ChiefId, GrantedLevels.findByLabel(ls_MaxPermit), "");
	  		            idf_DocObj.save();
	  			    }
	  			}
	  			
	  			String[] s_MDeptsArr = s_MDepts.split(",");
	  			String[] s_RDeptsArr = s_RDepts.split(",");
	  			String[] s_DDeptsArr = s_DDepts.split(",");
	  			
	  		    for(int ai=0; ai < s_MDeptsArr.length; ai++) {  //주관부서 
  		    		String s_Author=s_MDeptsArr[ai];
  		    		String s_AuthorType="D";
  		  		    VDept myDept = gwDeptService.selectDeptByOrgId(s_Author);
  		  		    if(null != myDept) {
  		  		    	s_Author = myDept.getUCabinetCode();
  		  		        s_Author = "g_"+s_Author;
  		  		    }else {
  		    			System.out.println("#MOLA#유효하지 않은 부서코드" + s_MDeptsArr[ai]);
  		    			continue;
  		    		}
  		  		    
                    String s_PermitType="D";
  		  		    if(p_ObjType.equals("D")) {//문서
  		  		        if(idf_DocObj.getString("u_doc_status").equals("C")) s_PermitType="R"; //closed문서는 읽기권한
	  		  		    if( idf_DocObj.getPermitEx( s_Author) != GrantedLevels.findByLabel(s_PermitType)) {
		  		    	    idf_DocObj.grant(s_Author, GrantedLevels.findByLabel(s_PermitType), "");
		  		    	    if(s_AuthorType.equals("D")) {
		  		    	        idf_DocObj.grant(s_Author+"_sub", GrantedLevels.findByLabel(s_PermitType), "");
		  		    	    }    
			  		        idf_DocObj.save();
		  		    	}
  		  		    }
  		  		    for(int i=0; i< lDocStatus.size(); i++) {
		  		    	
				  	    int i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='"+lDocStatus.get(i)+"'  and u_author_type !='S' ");
				  	    if(i_AuthorCnt < 1) {
		  		    	    s_PermitType="D";
							IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
							if(lDocStatus.get(i).equals("C")) s_PermitType="R"; //Closed권한은 조회/다운로드권한
							
							//if(s_Author.equals("g_" + dto.getTgCabinetcode())) continue; //복사자부서, 
							
				  			idf_PObj.setString("u_obj_id"		, rObjectId);
				  			idf_PObj.setString("u_obj_type"		, p_ObjType);    //폴더, 문서 
				  			idf_PObj.setString("u_doc_status"	, lDocStatus.get(i));
				  			idf_PObj.setString("u_permit_type"	, s_PermitType);
							idf_PObj.setString("u_own_dept_yn"	, s_Author.equals("g_" + dto.getTgCabinetcode()) ? "Y" : "");
							idf_PObj.setString("u_author_id"	, s_Author);
						    idf_PObj.setString("u_author_type"	, "D"); //부서  
				  			idf_PObj.setString("u_create_user"	, dto.getReqUser());
				  			idf_PObj.setTime  ("u_create_date"	, new DfTime());
				  			idf_PObj.setString("u_add_gubun"	, s_AddGubun);  //속성추가
				  			idf_PObj.save();
				  	    }
		  		    }
	  		    }	  			
	  		    for(int ai=0; ai < s_RDeptsArr.length; ai++) {
  		    		String s_Author=s_RDeptsArr[ai];
  		    		if(null==s_Author ||s_Author.equals("")) break;
  		  		    VDept myDept = gwDeptService.selectDeptByOrgId(s_Author);
  		  		    if(null != myDept) {
  		  		    	s_Author = myDept.getUCabinetCode();
  		  		        s_Author = "g_"+s_Author;
  		  		    }else {
  		    			System.out.println("#MOLA#유효하지 않은 부서코드" + s_MDeptsArr[ai]);
  		    			continue;
  		    		}
                    String s_PermitType="R";
  		  		    if(p_ObjType.equals("D")) {//문서
	  		  		    if( idf_DocObj.getPermitEx( s_Author) != GrantedLevels.findByLabel(s_PermitType)) {
		  		    	    idf_DocObj.grant(s_Author, GrantedLevels.findByLabel(s_PermitType), "");
		  		    	    idf_DocObj.grant(s_Author+"_sub", GrantedLevels.findByLabel(s_PermitType), "");
			  		        idf_DocObj.save();
		  		    	}
  		  		    }
  		  		    for(int i=0; i< lDocStatus.size(); i++) {
				  	    int i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='"+lDocStatus.get(i)+"' and u_author_type !='S'");
				  	    if(i_AuthorCnt < 1) {
							IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
							
							//if(s_Author.equals("g_" + dto.getTgCabinetcode())) continue; //복사자부서, 
							
				  			idf_PObj.setString("u_obj_id"		, rObjectId);
				  			idf_PObj.setString("u_obj_type"		, p_ObjType);    //폴더, 문서 
				  			idf_PObj.setString("u_doc_status"	, lDocStatus.get(i));
				  			idf_PObj.setString("u_permit_type"	, s_PermitType);
							idf_PObj.setString("u_own_dept_yn"	, s_Author.equals("g_" + dto.getTgCabinetcode()) ? "Y" : "");
							idf_PObj.setString("u_author_id"	, s_Author);
						    idf_PObj.setString("u_author_type"	, "D");  //참여부서들이라 부서로 등록
				  			idf_PObj.setString("u_create_user"	, dto.getReqUser());
				  			idf_PObj.setTime  ("u_create_date"	, new DfTime());
				  			idf_PObj.setString("u_add_gubun"	, s_AddGubun);  //속성추가
				  			idf_PObj.save();
				  	    }
		  		    }
	  		    }
	  		    for(int ai=0; ai < s_DDeptsArr.length; ai++) {
  		    		String s_Author=s_DDeptsArr[ai];
  		    		if(null==s_Author ||s_Author.equals("")) break;
  		  		    VDept myDept = gwDeptService.selectDeptByOrgId(s_Author);
  		  		    if(null != myDept) {
  		  		    	s_Author = myDept.getUCabinetCode();
  		  		        s_Author = "g_"+s_Author;
  		  		    }else {
  		    			System.out.println("#MOLA#유효하지 않은 부서코드" + s_MDeptsArr[ai]);
  		    			continue;
  		    		}
                    String s_PermitType="D";
  		  		    if(p_ObjType.equals("D")) {//문서
  		  		        if(idf_DocObj.getString("u_doc_status").equals("C")) s_PermitType="R";
	  		  		    if( idf_DocObj.getPermitEx( s_Author) != GrantedLevels.findByLabel(s_PermitType)) {
		  		    	    idf_DocObj.grant(s_Author, GrantedLevels.findByLabel(s_PermitType), "");
		  		    	    idf_DocObj.grant(s_Author+"_sub", GrantedLevels.findByLabel(s_PermitType), "");
			  		        idf_DocObj.save();
		  		    	}
  		  		    }
		  		    for(int i=0; i< lDocStatus.size(); i++) {

				  	    int i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='"+lDocStatus.get(i)+"'  and u_author_type !='S'");
				  	    if(i_AuthorCnt < 1) {
		  		    	    s_PermitType="D";  //삭제권한
							IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
							if(lDocStatus.get(i).equals("C")) s_PermitType="R"; //Closed권한은 조회/다운로드권한
							
							//if(s_Author.equals("g_" + dto.getTgCabinetcode())) continue; //복사자부서,
				  			idf_PObj.setString("u_obj_id"		, rObjectId);
				  			idf_PObj.setString("u_obj_type"		, p_ObjType);    //폴더, 문서 
				  			idf_PObj.setString("u_doc_status"	, lDocStatus.get(i));
				  			idf_PObj.setString("u_permit_type"	, s_PermitType);
							idf_PObj.setString("u_own_dept_yn"	, s_Author.equals("g_" + dto.getTgCabinetcode()) ? "Y" : "");
							idf_PObj.setString("u_author_id"	, s_Author);
							idf_PObj.setString("u_author_type"	, "D");  //참여부서들이라 부서로 등록
				  			idf_PObj.setString("u_create_user"	, dto.getReqUser());
				  			idf_PObj.setTime  ("u_create_date"	, new DfTime());
				  			idf_PObj.setString("u_add_gubun"	, s_AddGubun);  //속성추가
				  			idf_PObj.save();
				  	    }
		  		    }
	  		    }
	  	    }
	  		if(idf_Col != null) idf_Col.close();
  		}catch (Exception e) {
   			e.printStackTrace();
   			return false;
	    }finally {
	    	if(idf_Col != null) idf_Col.close();
	    }
  		return true ;
    }    
    
    //상위 폴더의 기본 auth base 리스트 반환
    private boolean setFolderAuthBaseList(UserSession userSession, IDfSession idfSession, String ps_UObjId, String rObjectId , DPath dto, String p_ObjType ) throws Exception{
    	
    	
	    String s_Dql = "select distinct u_author_id, u_author_type, u_permit_type, u_add_gubun, u_doc_status from edms_auth_base where u_obj_id ='"+ ps_UObjId +"' and u_author_type in('D','U','C','G')  and u_add_gubun in('P','W', 'G',' ', 'J')  and u_author_id !='g_null'" ;
	    if(p_ObjType.equals("D")){//문서
    	    //s_Dql = "select distinct u_author_id, u_author_type, u_permit_type, u_add_gubun, u_doc_status from edms_auth_base where u_obj_id ='"+ ps_UObjId +"' and u_author_type in('D','U','C','G')  and u_add_gubun in('P','W', 'G',' ','S', 'J')  and u_author_id !='g_null'" ;
	    	s_Dql = "select distinct u_author_id, u_author_type, u_permit_type, u_add_gubun, u_doc_status from edms_auth_base where u_obj_id ='"+ ps_UObjId +"' and u_author_type in('D','U','C','G')  and u_add_gubun in('P','W', 'G',' ', 'J')  and u_author_id !='g_null'" ;
	    }
	    
	    //if(!dto.getPreFolderId().equals("") && !dto.getPreFolderId().equals(ps_UObjId)) {
	    //	s_Dql = "select distinct u_author_id, u_author_type, u_permit_type, u_add_gubun, u_doc_status from edms_auth_base where u_obj_id in('"+ ps_UObjId +"', '"+ dto.getPreFolderId()+"') and u_author_type in('D','U','C','G')  and u_add_gubun in('P','W','S',' ')  " ;
	    //}
	    
  		IDfQuery 		idf_Qry 	= null;
  		idf_Qry = new DfQuery();
  		idf_Qry.setDQL(s_Dql);
  			
  		IDfCollection idf_Col=null;
  		IDfCollection idf_Colb=null;
  		try {
  			    idf_Col = idf_Qry.execute(idfSession,DfQuery.QUERY);
  		} catch (Exception e) {
  			e.printStackTrace();
  			return false;
  		}
  		try {
		    
	  		String lDocStatus[] = {"C", "L"};
	  		IDfDocument         idfDoc_Obj =null;
	  		IDfPersistentObject idfFol_Obj =null;
	  		int liDocMaxPermit =3;
  		    if(p_ObjType.equals("D")){//문서
  		    	idfDoc_Obj =(IDfDocument)idfSession.getObject(new DfId(rObjectId));
  		    	if(null !=idfDoc_Obj && idfDoc_Obj.getString("u_doc_status").equals("L")) liDocMaxPermit=7;
  		    }else if(p_ObjType.equals("F")) {//폴더
  		    	idfFol_Obj =idfSession.getObject(new DfId(rObjectId));
  		    }
  		    
  		    Map<String,String> delMap= new HashMap<String,String>();

	  		while(idf_Col != null && idf_Col.next())
	  		{
	  		    String s_Author=idf_Col.getString("u_author_id");
	  		    
	  		    if(null== s_Author || s_Author.equals("g_null")|| s_Author.equals("") || s_Author.equals(" ")) continue;
	  		    
	  		    String s_AuthorType=idf_Col.getString("u_author_type");
	  		    String s_PermitType=idf_Col.getString("u_permit_type");
	  		    String s_AddGubun  =idf_Col.getString("u_add_gubun");
	  		    String s_DocStatus =idf_Col.getString("u_doc_status");
	  		    
	  		    if(p_ObjType.equals("D")){//문서
	  		    //    if(null !=idfDoc_Obj && idfDoc_Obj.getString("u_sec_level").equals("S") && !s_AddGubun.equals("P")) liDocMaxPermit=3;
		  		    if(null !=idfDoc_Obj && idfDoc_Obj.getString("u_sec_level").equals("S") ) {
		  		        if(s_AuthorType.equals("U") && s_Author.equals(dto.getReqUser()))  s_AddGubun="G";//제한등급일 때, 복사,이동하는 사용자와 같은 ID에 대해서는 G로 추가
		  		        if(s_AuthorType.equals("U") && !s_Author.equals(dto.getReqUser())) s_AddGubun="P";//제한등급일 때, 복사,이동하는 사용자와 다른 사용자ID는 P 로 추가하거나 변경
		  		    }	  		    	
	  		    }
	  		    	  		    
	  		    if(p_ObjType.equals("D") && s_Author.contains("_sub") && (s_AddGubun.equals("P") || s_AddGubun.equals("S"))) continue;
	  		    
	  		    if( p_ObjType.equals("D") && idfDoc_Obj.getString("u_doc_status").equals("L") && s_PermitType.equals("D") ) delMap.put(s_Author, s_AuthorType); 
	  		     
	  		    for(int i=0; i< lDocStatus.length; i++) {
		  		    if(!s_DocStatus.equals(lDocStatus[i])) continue;

			  	    int i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='"+lDocStatus[i]+"' and u_author_type !='S' ");
			  	    if(i_AuthorCnt < 1) {
	  		    	
						IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
						if(lDocStatus[i].equals("C")) {
							s_PermitType="R"; //Closed권한은 조회/다운로드권한
							
							if(p_ObjType.equals("D")) {
								if(liDocMaxPermit > 3 
								  && s_Author.equals("g_" + dto.getTgCabinetcode().toLowerCase()) 
								    && dto.getSrcCabinetcode().equals(dto.getTgCabinetcode()) )  s_PermitType="D";
								//if(idfDoc_Obj.getString("u_sec_level").equals("S")) continue;
								//if(idfDoc_Obj.getString("u_sec_level").equals("G") &&  (!s_Author.equals( dto.getUGroupOrgCabinetCd()) )) continue;
								//else if(idfDoc_Obj.getString("u_sec_level").equals("C") &&  (!s_Author.equals( dto.getUComOrgCabinetCd())   )) continue;
								//else if(idfDoc_Obj.getString("u_sec_level").equals("T") &&  !s_Author.equals( "g_"+dto.getTgCabinetcode()   )) continue;
							}
							else if(p_ObjType.equals("F") && idfFol_Obj.getString("u_sec_level").equals("S") && !s_AddGubun.equals("G")) continue;
						}else {
							s_PermitType=idf_Col.getString("u_permit_type");
						}
						if(lDocStatus[i].equals(s_DocStatus)) { 
						
							 if(p_ObjType.equals("D")) {
									if(liDocMaxPermit > 3 
									  && s_Author.equals("g_" + dto.getTgCabinetcode().toLowerCase()) 
									    && dto.getSrcCabinetcode().equals(dto.getTgCabinetcode()) 
									      && s_DocStatus.equals("L"))  s_PermitType="D";
							 }
							 if(lDocStatus[i].equals("C")) s_PermitType="R";

							//if(s_Author.equals("g_" + dto.getTgCabinetcode())) continue; //복사자부서, 
							//if(s_Author.equals(dto.getReqUser()) && !s_AddGubun.equals("G")) continue;              //복사자는 ... auth_base 추가시에 'D'삭제권한으로 추가해주므로 중복되지 않게 제거 
				  			idf_PObj.setString("u_obj_id"		, rObjectId);
				  			idf_PObj.setString("u_obj_type"		, p_ObjType);    //폴더, 문서 
				  			idf_PObj.setString("u_doc_status"	, lDocStatus[i]);
				  			idf_PObj.setString("u_permit_type"	, s_PermitType);
							idf_PObj.setString("u_own_dept_yn"	, s_Author.equals("g_" + dto.getTgCabinetcode()) ? "Y" : "");
							idf_PObj.setString("u_author_id"	, s_Author);
							//idf_PObj.setString("u_author_type"	, s_AuthorType.equals("S")?"U":s_AuthorType);
							idf_PObj.setString("u_author_type"	, s_AuthorType);
				  			
				  			idf_PObj.setString("u_create_user"	, dto.getReqUser());
				  			idf_PObj.setTime  ("u_create_date"	, new DfTime());
				  			idf_PObj.setString("u_add_gubun"	, s_AddGubun);  //속성추가
				  			idf_PObj.save();
				  			if(p_ObjType.equals("D")) {
				  				if(idfDoc_Obj.getString("u_doc_status").equals(s_DocStatus)) { //문서가 Live이면 Live권한대상자권한부여, 아니면 Closed권한 부여
				  		    		idfDoc_Obj.grant(s_Author, GrantedLevels.findByLabel(s_PermitType), ""); //삭제가능 grant 권한 부여
				  		    		if(s_AuthorType.equals("D")) {//
				  		    			if(s_AddGubun.equals("P") ||s_AddGubun.equals("W")) {// && (!idfDoc_Obj.getString("u_sec_level").equals("S") && !idfDoc_Obj.getString("u_sec_level").equals("T"))) {//
				  		            	    idfDoc_Obj.grant(s_Author+"_sub", GrantedLevels.findByLabel(s_PermitType), ""); //삭제가능 grant 권한 부여
				  		    			}
					  		        }
				  		    		idfDoc_Obj.save();
				  				}
				  			}				  			
						}
			  	    }else {
			  	    	  if(p_ObjType.equals("D") && null !=idfDoc_Obj){//문서
			  	    		  
					  		  if( idfDoc_Obj.getPermitEx( s_Author) != GrantedLevels.findByLabel(s_PermitType)) {
								 if(p_ObjType.equals("D")) {
										if(liDocMaxPermit > 3 
										  && s_Author.equals("g_" + dto.getTgCabinetcode().toLowerCase()) 
										    && dto.getSrcCabinetcode().equals(dto.getTgCabinetcode()) 
										     && s_DocStatus.equals("L"))  s_PermitType="D";
								 }
					  			  
					  			  s_Dql = "UPDATE edms_auth_base  OBJECTS SET u_permit_type='"+ s_PermitType+"' " +
									  	"WHERE u_obj_id='" + rObjectId+"" + "' AND u_author_id = '" + s_Author + "'  and u_add_gubun='"+ s_AddGubun+"' and u_doc_status='"+ s_DocStatus+"'";
					  			  idf_Qry.setDQL(s_Dql);
								  idf_Colb = idf_Qry.execute(idfSession, DfQuery.QUERY);
								  if (idf_Colb!=null && idf_Colb.next()) idf_Colb.close();
								
								  if(null !=delMap.get(s_Author) && (!delMap.get(s_Author).equals("D") && s_PermitType.equals("R")) || s_PermitType.equals("D")) {
                                      if(idfDoc_Obj.getString("u_doc_status").equals(lDocStatus[i] )) {
										  idfDoc_Obj.grant(s_Author, GrantedLevels.findByLabel(s_PermitType), "");
										  //문서권한 줄 때 기본권한(G)는 사내나 그룹사 문서가 아니면 _sub권한(하위 모두 볼 수 있는 권한) 을 주지 않는다. 
						  		          if(s_AuthorType.equals("D") ){//
						  		        	  //if(!(s_AddGubun.equals("G") && (idfDoc_Obj.getString("u_sec_level").equals("S") || idfDoc_Obj.getString("u_sec_level").equals("T")))) {//
					  		        		  if(!idfDoc_Obj.getString("u_doc_status").equals(s_DocStatus)) continue;
	
						  		        	  if(s_AddGubun.equals("P") ||s_AddGubun.equals("W")) {// && (!idfDoc_Obj.getString("u_sec_level").equals("S") && !idfDoc_Obj.getString("u_sec_level").equals("T"))) {
					  		                      idfDoc_Obj.grant(s_Author+"_sub", GrantedLevels.findByLabel(s_PermitType), ""); //삭제가능 grant 권한 부여
						  		        	  }
							  		      }
										  idfDoc_Obj.save();				  			
                                      }
								  }
					  		  }
			  	    	  }
			  	    }
			  	    if(p_ObjType.equals("F") && lDocStatus[i].equals("L") && s_AddGubun.equals("P") && s_AuthorType.equals("D")) {  //폴더 라이브이면서, P로 추가된 조직은 _sub를 author_type='S' 로 추가해준다.
				  	    i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='L' and u_author_type ='S' and u_add_gubun='P' ");
				  	    if(i_AuthorCnt < 1) {
				  	    	IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
				  	    	idf_PObj.setString("u_obj_id"		, rObjectId);
				  			idf_PObj.setString("u_obj_type"		, p_ObjType);    //폴더, 문서 
				  			idf_PObj.setString("u_doc_status"	, "L");
				  			idf_PObj.setString("u_permit_type"	, s_PermitType);
							idf_PObj.setString("u_own_dept_yn"	, s_Author.equals("g_" + dto.getTgCabinetcode()) ? "Y" : "");
							idf_PObj.setString("u_author_id"	, s_Author+"_sub");
							idf_PObj.setString("u_author_type"	, "S"); //기본권한그룹
				  			idf_PObj.setString("u_create_user"	, dto.getReqUser());
				  			idf_PObj.setTime  ("u_create_date"	, new DfTime());
				  			idf_PObj.setString("u_add_gubun"	, s_AddGubun);  //속성추가
				  			idf_PObj.save();				  	    	
				  	    }
			  	    }
			  	    	
			  	    //if(i==0 ) {  //최초 1회( C, L 두 번 루프돌 경우)
			  		    if(p_ObjType.equals("D") && null !=idfDoc_Obj){//문서
	
			  		    	if((null !=delMap.get(s_Author) && !delMap.get(s_Author).equals("D") && s_PermitType.equals("R")) || s_PermitType.equals("D")) {
	  		        		    if(!idfDoc_Obj.getString("u_doc_status").equals(s_DocStatus)) continue;
				  		    	//if( idfDoc_Obj.getPermitEx( s_Author) != GrantedLevels.findByLabel(s_PermitType) ) {
				  		    		idfDoc_Obj.grant(s_Author, GrantedLevels.findByLabel(s_PermitType), ""); //삭제가능 grant 권한 부여
				  		    		if(s_AuthorType.equals("D")) {//
				  		    			if(s_AddGubun.equals("P") ||s_AddGubun.equals("W")) {// && (!idfDoc_Obj.getString("u_sec_level").equals("S") && !idfDoc_Obj.getString("u_sec_level").equals("T"))) {//
				  		            	    idfDoc_Obj.grant(s_Author+"_sub", GrantedLevels.findByLabel(s_PermitType), ""); //삭제가능 grant 권한 부여
				  		    			}
					  		        }
						  	    	idfDoc_Obj.save();
							    //}
				  		   }
			  		    }
			  	    //}
	  		    }
	  		    
	  	    }
	  		if(idf_Col != null) idf_Col.close();
	  		if(idf_Colb != null) idf_Colb.close();
  		}catch (Exception e) {
   			e.printStackTrace();
   			return false;
	    }finally {
	    	if(idf_Col != null) idf_Col.close();
	    	if(idf_Colb != null) idf_Colb.close();
	    }
  		return true ;
    }
    
    @Override
    public void addMoveAuthBaseRe(UserSession userSession, IDfSession idfSession, String rObjectId, String ps_RcevCab, String objType, DPath dto, IDfDocument idfNewDoc) throws Exception{
    	
    	String jobGubun=dto.getUptPthGbn(); //C :복사, M :이동
    	
    	if(null==psrvPMapDf.get("S"))   initMapInfo();
    	
    	List<String> lDocStatus = new ArrayList<String>();
    	IDfCollection idf_Col=null;  		
    	
    	try {  
/*
 * 복사시 
    -> 부서,등급 변경 여부
    -> link파일은 원본을 복사 (추가 )
    
    - acl_name 지정
    - 상위폴더 auth_share 여부: 대상폴더의 auth_share가져다가 S구분 사용자로 등록하면서, grant
    - 복사자id와  g_문서함코드를 auth_base에 등록, 복사자 id에  grant로 삭제권한 부여
*/    

		boolean bWork=false;
        //author_type:D, add_gubun='P' ; 프로젝트 루트로 갈 경우 참여부서 추가시
		
		String ls_DocPermitType = "R";
		int liDocMaxPermit =3;
		if(null !=idfNewDoc && objType.equals("D")) {  //문서의 경우, 파라메터로 넘어오는 rObjectId값에 u_doc_key( 복사는 신규문서라 r_object_id=u_doc_key, 이동은 u_doc_key)
			ls_DocPermitType =idfNewDoc.getString("u_doc_status").equals("C")?"R":"D";
			liDocMaxPermit =GrantedLevels.findByLabel(ls_DocPermitType);			
		}
		
		boolean bInitialize=false;
		boolean bFolAuthChg=true;
		if( (null != dto.getPrCode() && !dto.getPrCode().equals("") && !dto.getPrCode().equals(" ")) 
			|| (dto.getSourceGubun().equals("DFO") && (dto.getTargetGubun().equals("PJC")))
			|| (dto.getSourceGubun().equals("DFO") && (dto.getTargetGubun().equals("RJC")))
			|| (dto.getSourceGubun().equals("DFO") && (dto.getTargetGubun().equals("PFO") || dto.getTargetGubun().equals("RFO")))
			|| (dto.getTargetGubun().equals("DFO") && (dto.getSourceGubun().equals("PFO") || dto.getSourceGubun().equals("RFO")|| dto.getSourceGubun().equals("DEL")))
			|| dto.getTargetDboxId().equals(dto.getTgCabinetcode())
			|| !dto.getSrcCabinetcode().equals(dto.getTgCabinetcode())  //부서가 바뀌는 경우
			) { //프로젝트를 생성해서 가거나, 프로젝트폴더밑으로 가는 경우 
				//함간 이동복사시  공유협업권한 초기화
			/*
			    if( dto.getTargetDboxId().equals(dto.getTgCabinetcode()) && dto.getSourceGubun().equals("DFO") && dto.getTargetGubun().equals("DPC") && dto.getSrcCabinetcode().equals(dto.getTgCabinetcode())){ //부서폴더에서 부서함 Root로 가는 경우
			    	if(dto.getUptPthGbn().equals("C")) { //복사의 경우 기존권한을 유지한다.
			    		bFolAuthChg=false;
			    	}else { //이동의 경우 기존 권한 유지 
			    		bFolAuthChg=false;
			    	}
			    	
			    }else {
            */			    
					String s_Dql_d = "delete edms_auth_share object  where u_obj_id = '" + rObjectId + "' ";
		            idf_Col = DCTMUtils.getCollectionByDQL(idfSession, s_Dql_d, DfQuery.DF_QUERY);
		            if(idf_Col != null) idf_Col.close();
	
		            //함간 이동복사시  기본권한 초기화
					s_Dql_d = "delete edms_auth_base object  where u_obj_id = '" + rObjectId + "' ";
		            idf_Col = DCTMUtils.getCollectionByDQL(idfSession, s_Dql_d, DfQuery.DF_QUERY);
		            if(idf_Col != null) idf_Col.close();
	
		            //if( (dto.getSourceGubun().equals("DFO") && (dto.getTargetGubun().equals("PFO") || dto.getTargetGubun().equals("RFO")))
					//   || (dto.getTargetGubun().equals("DFO") && (dto.getSourceGubun().equals("PFO") || dto.getSourceGubun().equals("RFO"))))
		            //    bInitialize=false;  //폴더간 복사때는 상위폴더의 공유협업을 상속받아 사용한다
		            //else
		            bInitialize=true;  //루트로 이동/복사한 경우 프로젝트나 연구과제는 참여부서권한을 추가한다
			  //  }
		}

		if( jobGubun.equals("M")) {  //이동처리
			/* 이동시
			  -> 문서함,등급 변경여부 
			    - acl_name 지정
			    - auth_base 원본의 W인 auth_base사용자들 가져다가 grant 
			    : 부서,등급 변경이 없으면 acl_name그대로 사용   
			      : 변경이 있으면 원본에서 W 인 auth_base사용자들만 상속받고 grant


			     대상폴더의 auth_share가져다가 S구분 사용자로 등록하면서, grant
			    대상폴더의 보안등급이 더 높을때는 auth_base에 속성으로 추가한 사용자들을 상속한다. 		
			 */
			
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////				
//이동 -폴더-권한처리 Start
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
			if(objType.equals("F")) { //폴더
//이동-폴더
				IDfPersistentObject idfFol_Obj = idfSession.getObject(new DfId(rObjectId));
		    	String s_UFolId = idfFol_Obj.getString("u_up_fol_id");
		    	IDfPersistentObject idfUpFol_Obj =null;
		    	if (DfId.isObjectId(s_UFolId)) {
		    		idfUpFol_Obj = idfSession.getObject(new DfId(s_UFolId));

                    //“잠금” 처리된 폴더 내 폴더 및 문서의 생성/수정/삭제/이동/이관은 불가하다. ※ “잠금” 처리된 폴더 자체의 이관은 가능함.(복사 가능 !, 단 잠금처리되지 않은 폴더로)
                    //잠금폴더내 폴더나 문서를 잠금처리되지 않은 폴더로 복사하면 상위폴더가 잠금상태가 아니므로 잠금 상태를 상속받아 사용
		    		idfFol_Obj.setString("u_fol_status",  idfUpFol_Obj.getString("u_fol_status"));
		    		// 기타 상속 속성 확인필요
			    	idfFol_Obj.save();
		    	}		    	    
		    	if(s_UFolId.equals("") || s_UFolId.equals(" ")) {

	    	    	if(!idfFol_Obj.getString("u_pr_code").equals("") && !idfFol_Obj.getString("u_pr_code").equals(" ") && idfFol_Obj.getString("u_pr_type").equals("P")) {
	    	      	 	String s_Dql = "select u_sec_level from edms_project where u_cabinet_code = '" + idfFol_Obj.getString("u_cabinet_code") + "' and u_pjt_code='"+idfFol_Obj.getString("u_pr_code") +"'";
		    	  		IDfQuery 		idf_Qry 	= null;
		    	  		idf_Qry = new DfQuery();
		    	  		idf_Qry.setDQL(s_Dql);
		    	  		try {
		    	  			idf_Col = idf_Qry.execute(idfSession,DfQuery.QUERY);
		    		  		while(idf_Col != null && idf_Col.next()) dto.setTargetSecLevel(idf_Col.getString("u_sec_level"));
		    	  		} catch (Exception e) {
		    				e.printStackTrace();
		    		    }finally {
		    		    	if(idf_Col !=null) idf_Col.close();
		    		    }

		    	    }else if(!idfFol_Obj.getString("u_pr_code").equals("") && !idfFol_Obj.getString("u_pr_code").equals(" ") && idfFol_Obj.getString("u_pr_type").equals("R")) { 
	    	      	 	String s_Dql = "select u_sec_level from edms_research where u_cabinet_code = '" + idfFol_Obj.getString("u_cabinet_code") + "' and u_rsch_code='"+idfFol_Obj.getString("u_pr_code") +"'";
		    	  		IDfQuery 		idf_Qry 	= null;
		    	  		idf_Qry = new DfQuery();
		    	  		idf_Qry.setDQL(s_Dql);
		    	  		try {
		    	  			idf_Col = idf_Qry.execute(idfSession,DfQuery.QUERY);
		    		  		while(idf_Col != null && idf_Col.next()) dto.setTargetSecLevel(idf_Col.getString("u_sec_level"));
		    	  		} catch (Exception e) {
		    				e.printStackTrace();
		    		    }finally {
		    		    	if(idf_Col !=null) idf_Col.close();
		    		    }
		    	    }
	    	    }		    	
//이동-폴더				
		        String ls_BeforeSecLevel = idfFol_Obj.getString("u_sec_level");
		        
		        if(!bFolAuthChg) dto.setTargetSecLevel(ls_BeforeSecLevel);
		        
		    	if(! bInitialize) {
	                ////////////////////////1. 직전 공유그룹 권한리스트 정리 //////////////////////////////////////////////////////////
				    String s_Dql_d = "delete edms_auth_share object  where u_obj_id = '" + rObjectId + "'"; 
			        idf_Col = DCTMUtils.getCollectionByDQL(idfSession, s_Dql_d, DfQuery.DF_QUERY);
			        if(idf_Col != null) idf_Col.close();
	
	                ////////////////////////2. 직전 auth_base 정리 (보안등급 변경없으면, 속성추가항목 유지, 그렇지 않으면 삭제대상에 추가(P, ' ')///
			    	if (bFolAuthChg && secLevelMap.get(ls_BeforeSecLevel) < secLevelMap.get( null==idfUpFol_Obj?dto.getTargetSecLevel():idfUpFol_Obj.getString("u_sec_level"))) {
	
			    		idfFol_Obj.setString("u_sec_level", idfUpFol_Obj==null?dto.getTargetSecLevel():idfUpFol_Obj.getString("u_sec_level"));
						idfFol_Obj.save();
	                    
						if(idfFol_Obj.getString("u_sec_level").equals("S")) { //제한등급일 때는 G인것도 삭제하고, 상속받는다.
					        s_Dql_d = "delete edms_auth_base object " +
					                " where u_obj_id = '" + rObjectId + "'  and (u_add_gubun in('G', 'S', 'P',' ', 'J' ) or u_author_type='S') ";  // 보안등급기본 제거, 권한이 낮으면 이전에 추가되었던 '속성'추가 항목도 제거
						}else {
					        s_Dql_d = "delete edms_auth_base object " +
					                " where u_obj_id = '" + rObjectId + "'  and (u_add_gubun in('S', 'P',' ', 'J' ) or u_author_type='S') ";  // 보안등급기본 제거, 권한이 낮으면 이전에 추가되었던 '속성'추가 항목도 제거
						}
					    idf_Col = DCTMUtils.getCollectionByDQL(idfSession, s_Dql_d, DfQuery.DF_QUERY);
					    if(idf_Col != null) idf_Col.close();
					    bWork= setFolderAuthBaseList(userSession, idfSession, s_UFolId, rObjectId , dto, objType) ;//폴더
				    }else {
						s_Dql_d = "delete edms_auth_base object " +
						        " where u_obj_id = '" + rObjectId +
						        "'  and (u_add_gubun='S' or u_author_type='S'  or u_add_gubun='J' or u_author_id='"+ "g_"+ ps_RcevCab+"' or u_author_id='"+ "g_"+ dto.getUComOrgCabinetCd()+"'  or u_author_id='"+ "g_"+ dto.getUComOrgCabinetCd()+"')  ";  // 보안등급기본, 공유협업 제거
						
						idf_Col = DCTMUtils.getCollectionByDQL(idfSession, s_Dql_d, DfQuery.DF_QUERY);
						if(idf_Col != null) idf_Col.close();
				    }
		    	}else {
		    		if (secLevelMap.get(ls_BeforeSecLevel) < secLevelMap.get(dto.getTargetSecLevel())) {
		    		    idfFol_Obj.setString("u_sec_level", idfUpFol_Obj==null?dto.getTargetSecLevel():idfUpFol_Obj.getString("u_sec_level"));
					    idfFol_Obj.save();
		    		}
		    	}
	            //idf_Col = DCTMUtils.getCollectionByDQL(idfSession, s_Dql_d, DfQuery.DF_QUERY);
//이동-폴더		        
	            ///////////////////////3.폴더속성에 따른 기본권한 추가 /////////////////////////////////////////////////////////
		    	List<String> lst_Author =getFolderAuthorList(userSession, idfSession, rObjectId, dto );
				for(String s_Author : lst_Author)
				{
						IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");

				  		int i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='L'  ");
				  	    if(i_AuthorCnt < 1) {
							idf_PObj.setString("u_obj_id"		, rObjectId);
							idf_PObj.setString("u_obj_type"		, objType);
							idf_PObj.setString("u_doc_status"	, "L");
							idf_PObj.setString("u_permit_type"	, "D");
							idf_PObj.setString("u_own_dept_yn"	, s_Author.equals("g_" + ps_RcevCab) ? "Y" : "");
							idf_PObj.setString("u_author_id"	, s_Author);
							idf_PObj.setString("u_author_type"	, s_Author.equals("g_" + ps_RcevCab) ? "D" : "S");
							idf_PObj.setString("u_create_user"	, dto.getReqUser());
							idf_PObj.setTime  ("u_create_date"	, new DfTime());
							idf_PObj.setString("u_add_gubun"	, "G");
							
							idf_PObj.save(); 
				  	    }
						
						if( s_Author.equals("g_" + ps_RcevCab) ) {  //Closed이면서 author_type이 S 인 것이 들어가지 않도록 함
							if(idfFol_Obj.getString("u_sec_level").equals("S")) s_Author = dto.getReqUser();
					  		i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='C'  ");
					  	    if(i_AuthorCnt < 1) {
							
								idf_PObj = idfSession.newObject("edms_auth_base");
								idf_PObj.setString("u_obj_id"		, rObjectId);
								idf_PObj.setString("u_obj_type"		, objType);
								idf_PObj.setString("u_doc_status"	, "C");
								idf_PObj.setString("u_permit_type"	, "R");
								idf_PObj.setString("u_own_dept_yn"	, s_Author.equals("g_" + ps_RcevCab) ? "Y" : "");
								idf_PObj.setString("u_author_id"	, s_Author);
								idf_PObj.setString("u_author_type"	, s_Author.equals("g_" + ps_RcevCab) ? "D" : "U");
								idf_PObj.setString("u_create_user"	, dto.getReqUser());
								idf_PObj.setTime  ("u_create_date"	, new DfTime());
								idf_PObj.setString("u_add_gubun"	, "G");
								idf_PObj.save();
					  	    }
						}
				}
//이동-폴더		
				if(! bInitialize) {
                    ///////////////////////3.상위폴더 공유속성 상속여부 체크 / 상속처리/////////////////////////////////////////////////////////
					if( bFolAuthChg)
				        bWork= setFolderAuthShareList(userSession, idfSession, s_UFolId, rObjectId , dto, objType) ;//폴더
				    if( dto.getTargetFolType().equals("PFO") || dto.getTargetFolType().equals("RFO")) { //프로젝트나 연구과제용 폴더생성 작업인 경우
						bWork=setProjectJoinDeptToAuthbase( userSession, idfSession, rObjectId , dto, "F" ) ;
					}				    
				}else {
					if( dto.getTargetFolType().equals("PFO") || dto.getTargetFolType().equals("RFO")) { //프로젝트나 연구과제용 폴더생성 작업인 경우
						bWork=setProjectJoinDeptToAuthbase( userSession, idfSession, rObjectId , dto, "F" ) ;
					}

					if( !s_UFolId.equals("") && !s_UFolId.equals(" ")) {
					    bWork= setFolderAuthShareList(userSession, idfSession, s_UFolId, rObjectId , dto, objType) ;//공유그룹 권한자
					    if (secLevelMap.get(ls_BeforeSecLevel) < secLevelMap.get(dto.getTargetSecLevel())) {
						    bWork= setFolderAuthBaseList(userSession, idfSession, s_UFolId, rObjectId , dto, objType) ;//폴더
					    }
					}
					if(!idfFol_Obj.getString("u_sec_level").equals("S")) {
						
						String s_Author = dto.getReqUser();
						if (secLevelMap.get(ls_BeforeSecLevel) < secLevelMap.get(dto.getTargetSecLevel())) {
						    if(idfFol_Obj.getString("u_sec_level").equals("G"))      s_Author =  "g_"+dto.getUGroupOrgCabinetCd();
						    else if(idfFol_Obj.getString("u_sec_level").equals("C")) s_Author =  "g_"+dto.getUComOrgCabinetCd();
						    else if(idfFol_Obj.getString("u_sec_level").equals("T")) s_Author =  "g_"+dto.getTgCabinetcode();
						}
				  		int i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='C' and u_add_gubun in('G','P','J', ' ') ");
				  	    if(i_AuthorCnt < 1) {
					  	    IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
					        idf_PObj = idfSession.newObject("edms_auth_base");
					  		idf_PObj.setString("u_obj_id"		, rObjectId);
					  		idf_PObj.setString("u_obj_type"		, objType);
					  		idf_PObj.setString("u_doc_status"	, "C");
			  				idf_PObj.setString("u_permit_type"	, "R");
							idf_PObj.setString("u_own_dept_yn"	, " ");
							idf_PObj.setString("u_author_id"	, s_Author);
							idf_PObj.setString("u_author_type"	, "U");  //개인
					  		idf_PObj.setString("u_create_user"	, dto.getReqUser());
					  		idf_PObj.setTime  ("u_create_date"	, new DfTime());

					  		if(idfFol_Obj.getString("u_sec_level").equals("S"))   //제한등급의 closed권한에는 추가하지 않는다
					  		    idf_PObj.setString("u_add_gubun"	, "G");
					  		else
					  			idf_PObj.setString("u_add_gubun"	, "G");
					  		idf_PObj.save();
				  	    }			  	    
					}
				}
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////				
// 이동 -문서-권한처리 Start
//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////				
			}else if(objType.equals("D")) { //문서
//이동-문서
		  		    String s_DocStatus=idfNewDoc.getString("u_doc_status");
					lDocStatus.add("C");

		  		    String ls_BeforeSecLevel = idfNewDoc.getString("u_sec_level");
		  		    
			    	String s_UFolId = idfNewDoc.getString("u_fol_id");
			    	IDfPersistentObject idfUpFol_Obj =null;  //보안등급 비교용 상위 폴더 
			    	if (DfId.isObjectId(s_UFolId)) {
			    		idfUpFol_Obj = idfSession.getObject(new DfId(s_UFolId));
			    	}
			    	if(s_UFolId.equals("") || s_UFolId.equals(" ")) {
		    	    	if(!idfNewDoc.getString("u_pr_code").equals("") && !idfNewDoc.getString("u_pr_code").equals(" ") && idfNewDoc.getString("u_pr_type").equals("P")) {
		    	      	 	String s_Dql = "select u_sec_level from edms_project where u_cabinet_code = '" + idfNewDoc.getString("u_cabinet_code") + "' and u_pjt_code='"+idfNewDoc.getString("u_pr_code") +"'";
			    	  		IDfQuery 		idf_Qry 	= null;
			    	  		idf_Qry = new DfQuery();
			    	  		idf_Qry.setDQL(s_Dql);
			    	  		try {
			    	  			idf_Col = idf_Qry.execute(idfSession,DfQuery.QUERY);
			    		  		while(idf_Col != null && idf_Col.next()) dto.setTargetSecLevel(idf_Col.getString("u_sec_level"));
			    	  		} catch (Exception e) {
			    				e.printStackTrace();
			    		    }finally {
			    		    	if(idf_Col !=null) idf_Col.close();
			    		    }
	
			    	    }else if(!idfNewDoc.getString("u_pr_code").equals("") && !idfNewDoc.getString("u_pr_code").equals(" ") && idfNewDoc.getString("u_pr_type").equals("R")) { 
		    	      	 	String s_Dql = "select u_sec_level from edms_research where u_cabinet_code = '" + idfNewDoc.getString("u_cabinet_code") + "' and u_rsch_code='"+idfNewDoc.getString("u_pr_code") +"'";
			    	  		IDfQuery 		idf_Qry 	= null;
			    	  		idf_Qry = new DfQuery();
			    	  		idf_Qry.setDQL(s_Dql);
			    	  		try {
			    	  			idf_Col = idf_Qry.execute(idfSession,DfQuery.QUERY);
			    		  		while(idf_Col != null && idf_Col.next()) dto.setTargetSecLevel(idf_Col.getString("u_sec_level"));
			    	  		} catch (Exception e) {
			    				e.printStackTrace();
			    		    }finally {
			    		    	if(idf_Col !=null) idf_Col.close();
			    		    }
			    	    }
		    	    }
			    	if(! bInitialize) {
			    		if(!s_DocStatus.equals("C") && !idfNewDoc.getString("u_sec_level").equals("S")) lDocStatus.add("L");
////////////////////////1. 직존 공유그룹 권한리스트 정리 //////////////////////////////////////////////////////////
			            String s_Dql_d = "delete edms_auth_share object  where u_obj_id = '" + rObjectId + "'"; 
		                idf_Col = DCTMUtils.getCollectionByDQL(idfSession, s_Dql_d, DfQuery.DF_QUERY);
		                if(idf_Col != null) idf_Col.close();
		                //////////////////////// 1. 기존 auth_base 정리 //////////////////////////////////////////////////////////		
						s_Dql_d = "delete edms_auth_base object " +
						        " where u_obj_id = '" + rObjectId + "'  and (u_add_gubun='S'  or u_add_gubun='J' or u_author_type='S' or u_author_id='"+ "g_"+ ps_RcevCab+"') ";  //공유협업에서 추가된 권한, 보안등급기본추가권한 제거

						if (secLevelMap.get(ls_BeforeSecLevel) < secLevelMap.get(null==idfUpFol_Obj?"T":idfUpFol_Obj.getString("u_sec_level"))) {
							if(idfUpFol_Obj.getString("u_sec_level").equals("S")) //제한등급이면 기존문서의 u_add_gubun 이 G인 것도 삭제
								s_Dql_d = "delete edms_auth_base object " +
								        " where u_obj_id = '" + rObjectId + "'  and u_add_gubun in('G', 'S', 'P',' ', 'J' ) ";  //공유협업에서 추가된 권한, 보안등급기본추가권한 제거, 보안등급 상향되면 '속성추가' 권한도 제거
							else
								s_Dql_d = "delete edms_auth_base object " +
								        " where u_obj_id = '" + rObjectId + "'  and u_add_gubun in('S', 'P',' ', 'J' ) ";  //공유협업에서 추가된 권한, 보안등급기본추가권한 제거, 보안등급 상향되면 '속성추가' 권한도 제거
						}
		
			            idf_Col = DCTMUtils.getCollectionByDQL(idfSession, s_Dql_d, DfQuery.DF_QUERY);
				  		if(idf_Col != null) idf_Col.close();
			    	}
//이동 - 문서 			    	
			    	if(bInitialize) {
						if (secLevelMap.get(ls_BeforeSecLevel) < secLevelMap.get(dto.getTargetSecLevel())) {
							idfNewDoc.setString("u_sec_level", idfUpFol_Obj==null?dto.getTargetSecLevel():idfUpFol_Obj.getString("u_sec_level"));
							idfNewDoc.save();
						}
			  		    //if(!s_DocStatus.equals("C") && !idfNewDoc.getString("u_sec_level").equals("S")) lDocStatus.add("L");
						if(!s_DocStatus.equals("C") ) lDocStatus.add("L");
						
						if( dto.getTargetFolType().equals("PFO") || dto.getTargetFolType().equals("RFO")) { //프로젝트나 연구과제용 폴더생성 작업인 경우
							bWork=setProjectJoinDeptToAuthbase( userSession, idfSession, idfNewDoc.getString("r_object_id") , dto, "D" ) ;
						}
						if(null !=idfUpFol_Obj) { //프로젝트 root로 바로 가는 경우 상위폴더id가 없음
				  		    //2. 상위폴더 공유그룹 체크/권한 추가
				  		    bWork= setFolderAuthShareList(userSession, idfSession, s_UFolId, idfNewDoc.getString("r_object_id") , dto, objType) ;//문서
				  		    if (secLevelMap.get(ls_BeforeSecLevel) < secLevelMap.get(dto.getTargetSecLevel())) {
				  		        bWork= setFolderAuthBaseList(userSession, idfSession, s_UFolId, idfNewDoc.getString("r_object_id") , dto, objType) ;//폴더
				  		    }
				  		    if(!idfUpFol_Obj.getString("u_sec_level").equals("S")) { //제한등급이 아닐때 팀을 u_add_gubun='G' 로 추가
								for(int i=0; i< lDocStatus.size(); i++) {
							  	    String s_Author = "g_" + ps_RcevCab;  //g_부서 : grant는 하지 않음
									if (secLevelMap.get(ls_BeforeSecLevel) < secLevelMap.get(dto.getTargetSecLevel())) {
									    if(idfUpFol_Obj.getString("u_sec_level").equals("G"))      s_Author =  "g_"+dto.getUGroupOrgCabinetCd();
									    else if(idfUpFol_Obj.getString("u_sec_level").equals("C")) s_Author =  "g_"+dto.getUComOrgCabinetCd();
									}
							  	    
							  	    int i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='"+lDocStatus.get(i)+"'");
							  	    if(i_AuthorCnt < 1) {
			
								  	    IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
								        idf_PObj = idfSession.newObject("edms_auth_base");
								  		idf_PObj.setString("u_obj_id"		, rObjectId);
								  		idf_PObj.setString("u_obj_type"		, objType);
								  		idf_PObj.setString("u_doc_status"	, lDocStatus.get(i));
							  			if(lDocStatus.get(i).equals("C"))
							  				idf_PObj.setString("u_permit_type"	, "R");
							  			else
								  		    idf_PObj.setString("u_permit_type"	, "D");
										idf_PObj.setString("u_own_dept_yn"	, "Y");
										idf_PObj.setString("u_author_id"	, s_Author);
										idf_PObj.setString("u_author_type"	,"D"); //부서
								  		idf_PObj.setString("u_create_user"	, dto.getReqUser());
								  		idf_PObj.setTime  ("u_create_date"	, new DfTime());
								  		idf_PObj.setString("u_add_gubun"	, "G");
								  		idf_PObj.save();  		
							  	    }
							  	    if(i==0 && s_DocStatus.equals("L")) {//&& idfNewDoc.getPermitEx( s_Author) != liDocMaxPermit) {
							  	        idfNewDoc.grant(s_Author, liDocMaxPermit,"");
							  	    	if((!idfNewDoc.getString("u_sec_level").equals("S") && !idfNewDoc.getString("u_sec_level").equals("T"))) {//
							  	            idfNewDoc.grant(s_Author+"_sub", liDocMaxPermit,"");
							  	    	}
							  		    idfNewDoc.save();
							  	    }	
					  		    }
							  								
							}				  		    
						}else {
							for(int i=0; i< lDocStatus.size(); i++) {
						  	    String s_Author = "g_" + ps_RcevCab;  //g_부서 : grant는 하지 않음
								if (secLevelMap.get(ls_BeforeSecLevel) < secLevelMap.get(dto.getTargetSecLevel())) {
								    if(dto.getTargetSecLevel().equals("G"))      s_Author =  "g_"+dto.getUGroupOrgCabinetCd();
								    else if(dto.getTargetSecLevel().equals("C")) s_Author =  "g_"+dto.getUComOrgCabinetCd();
								}
						  	    
						  	    int i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='"+lDocStatus.get(i)+"'");
						  	    
						  	    if(idfNewDoc.getString("u_sec_level").equals("S")) {
						  	    	if(idfNewDoc.getString("u_doc_status").equals("C")) liDocMaxPermit=3;
						  	    	else liDocMaxPermit=7; //제한문서는 부서에 읽기권한 부여
						  	    }
						  	    
						  	    if(i_AuthorCnt < 1) {
						  	    	if(idfNewDoc.getString("u_sec_level").equals("S") && lDocStatus.get(i).equals("C")) {
						  	    		s_Author = dto.getReqUser();  //이동권한자
								  	    i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='C'");
								  	    if(i_AuthorCnt < 1) {
									  	    IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
									  	    
									        idf_PObj = idfSession.newObject("edms_auth_base");
									  		idf_PObj.setString("u_obj_id"		, rObjectId);
									  		idf_PObj.setString("u_obj_type"		, objType);
									  		idf_PObj.setString("u_doc_status"	, "C");
							  		        idf_PObj.setString("u_permit_type"	, "R");
											idf_PObj.setString("u_own_dept_yn"	, " ");
											idf_PObj.setString("u_author_id"	, s_Author);
											idf_PObj.setString("u_author_type"	,"U");
									  		idf_PObj.setString("u_create_user"	, dto.getReqUser());
									  		idf_PObj.setTime  ("u_create_date"	, new DfTime());
									  		idf_PObj.setString("u_add_gubun"	, "G");
									  		idf_PObj.save();  
						  	    	    }
						  	    	}else {
								  	    IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
								        idf_PObj = idfSession.newObject("edms_auth_base");
								  		idf_PObj.setString("u_obj_id"		, rObjectId);
								  		idf_PObj.setString("u_obj_type"		, objType);
								  		idf_PObj.setString("u_doc_status"	, lDocStatus.get(i));
							  			if(liDocMaxPermit==3 || lDocStatus.get(i).equals("C"))
							  				idf_PObj.setString("u_permit_type"	, "R");
							  			else if(liDocMaxPermit==7)
								  		    idf_PObj.setString("u_permit_type"	, "D");
										idf_PObj.setString("u_own_dept_yn"	, "Y");
										idf_PObj.setString("u_author_id"	, s_Author);
										idf_PObj.setString("u_author_type"	,"D"); //부서
								  		idf_PObj.setString("u_create_user"	, dto.getReqUser());
								  		idf_PObj.setTime  ("u_create_date"	, new DfTime());
								  		idf_PObj.setString("u_add_gubun"	, "G");
								  		idf_PObj.save();  		
						  	    	}
						  	    }
						  			  
						  	    if(i==0 && s_DocStatus.equals("L")) {//&& idfNewDoc.getPermitEx( s_Author) != liDocMaxPermit) {
						  	        idfNewDoc.grant(s_Author, liDocMaxPermit,"");
						  	    	if((!idfNewDoc.getString("u_sec_level").equals("S") && !idfNewDoc.getString("u_sec_level").equals("T"))) {//
						  	            idfNewDoc.grant(s_Author+"_sub", liDocMaxPermit,"");
						  	    	}
						  		    idfNewDoc.save();
						  	    }    
					  		    s_Author = dto.getReqUser();  //이동권한자
						  	    if(idfNewDoc.getPermitEx( s_Author) != 7 && idfNewDoc.getString("u_doc_status").equals("L")) {
						  	        idfNewDoc.grant(s_Author, 7,"");
						  		    idfNewDoc.save();
						  	    }
						  	    s_Author = dto.getReqUser();  //g_부서 : grant는 하지 않음
						  	    //if(idfNewDoc.getPermitEx( s_Author) != liDocMaxPermit) {
						  	        idfNewDoc.grant(s_Author, liDocMaxPermit,"");
						  		    idfNewDoc.save();
						  	    //}
				  		    }
						}
//이동--문서						
			    	}else {
						if (secLevelMap.get(ls_BeforeSecLevel) < secLevelMap.get(dto.getTargetSecLevel())) {
							
							idfNewDoc.setString("u_sec_level", idfUpFol_Obj==null?dto.getTargetSecLevel():idfUpFol_Obj.getString("u_sec_level"));
							idfNewDoc.save();

				  		    if(!s_DocStatus.equals("C") && !idfNewDoc.getString("u_sec_level").equals("S")) lDocStatus.add("L");
							
					  		bWork= setFolderAuthShareList(userSession, idfSession, s_UFolId, idfNewDoc.getString("r_object_id") , dto, objType) ;//문서
					  		
					  		if( dto.getTargetFolType().equals("PFO") || dto.getTargetFolType().equals("RFO")) { //프로젝트나 연구과제용 폴더생성 작업인 경우
								bWork=setProjectJoinDeptToAuthbase( userSession, idfSession, idfNewDoc.getString("r_object_id") , dto, "D" ) ;
							}
				  		    //bWork= setFolderAuthBaseList(userSession, idfSession, s_UFolId, idfNewDoc.getString("r_object_id") , dto, objType) ;
					  		//if(dto.getTargetSecLevel().equals("S")) {
					  		String s_Author="";
							if(idfNewDoc.getString("u_doc_status").equals("L") ) {
									if(!idfNewDoc.getString("u_sec_level").equals("S")) {
										s_Author =  "g_"+ps_RcevCab;
										if(idfNewDoc.getString("u_sec_level").equals("G"))       s_Author =  "g_"+dto.getUGroupOrgCabinetCd();
									    else if(idfNewDoc.getString("u_sec_level").equals("C"))  s_Author =  "g_"+dto.getUComOrgCabinetCd();
													
								  	    int i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='L'");
								  	    int i_AuthorCntClose = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='C' ");
								  	    
								  	    if(i_AuthorCntClose > 0) {
								  	    	String s_Dql_d = "delete edms_auth_base object " +
											        " where u_obj_id = '" + rObjectId + "'  and u_author_id= '"+s_Author +"' and u_doc_status='C' and u_add_gubun='G' "; 
								            idf_Col = DCTMUtils.getCollectionByDQL(idfSession, s_Dql_d, DfQuery.DF_QUERY);
									  		if(idf_Col != null) idf_Col.close();							  	    	
								  	    }
								  	    bWork= setFolderAuthShareList(userSession, idfSession, s_UFolId, idfNewDoc.getString("r_object_id") , dto, objType) ;//문서
								  	    bWork= setFolderAuthBaseList(userSession, idfSession, s_UFolId, idfNewDoc.getString("r_object_id") , dto, objType) ;
								  	    
								  	    if(i_AuthorCnt < 1) {
									  	    IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");

									        idf_PObj = idfSession.newObject("edms_auth_base");
									  		idf_PObj.setString("u_obj_id"		, rObjectId);
									  		idf_PObj.setString("u_obj_type"		, objType);
									  		idf_PObj.setString("u_doc_status"	, "L");
								  		    idf_PObj.setString("u_permit_type"	, "D");
											idf_PObj.setString("u_own_dept_yn"	, "Y");
											idf_PObj.setString("u_author_id"	, s_Author);
											idf_PObj.setString("u_author_type"	,"D"); //부서
									  		idf_PObj.setString("u_create_user"	, dto.getReqUser());
									  		idf_PObj.setTime  ("u_create_date"	, new DfTime());
									  		idf_PObj.setString("u_add_gubun"	, "G");
									  		idf_PObj.save();  
								  	    }
								  	    if( s_DocStatus.equals("L")) {
								  	    //if(idfNewDoc.getPermitEx( s_Author) != liDocMaxPermit) {
								  	        idfNewDoc.grant(s_Author, liDocMaxPermit,"");
								  	        if((!idfNewDoc.getString("u_sec_level").equals("S") && !idfNewDoc.getString("u_sec_level").equals("T"))) {//
								  	            idfNewDoc.grant(s_Author+"_sub", liDocMaxPermit,"");
								  	        }
								  		    idfNewDoc.save();
								  	    //}
								  	    }
									}else {
										s_Author =  "g_"+ps_RcevCab;
								  	    int i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='L'");
								  	    if(i_AuthorCnt < 1) {
									  	    IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");

									        idf_PObj = idfSession.newObject("edms_auth_base");
									  		idf_PObj.setString("u_obj_id"		, rObjectId);
									  		idf_PObj.setString("u_obj_type"		, objType);
									  		idf_PObj.setString("u_doc_status"	, "L");
								  		    idf_PObj.setString("u_permit_type"	, "D");
											idf_PObj.setString("u_own_dept_yn"	, "Y");
											idf_PObj.setString("u_author_id"	, s_Author);
											idf_PObj.setString("u_author_type"	,"D"); //부서
									  		idf_PObj.setString("u_create_user"	, dto.getReqUser());
									  		idf_PObj.setTime  ("u_create_date"	, new DfTime());
									  		idf_PObj.setString("u_add_gubun"	, "G");
									  		idf_PObj.save();  
								  	    }
								  	    if( s_DocStatus.equals("L")) {
								  	    //if(idfNewDoc.getPermitEx( s_Author) != liDocMaxPermit) {
								  	        idfNewDoc.grant(s_Author, liDocMaxPermit,"");
								  		    idfNewDoc.save();
								  	    //}
								  	    }
									}
					  	    		
							  	    s_Author = dto.getReqUser();  //g_부서 : grant는 하지 않음
							  	    int i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='L' and u_add_gubun='G' ");
							  	    if(i_AuthorCnt > 0) {
							  	    	String s_Dql_d = "delete edms_auth_base object " +
										        " where u_obj_id = '" + rObjectId + "'  and u_author_id= '"+s_Author +"' and u_doc_status='L' and u_add_gubun='G' "; 
							            idf_Col = DCTMUtils.getCollectionByDQL(idfSession, s_Dql_d, DfQuery.DF_QUERY);
								  		if(idf_Col != null) idf_Col.close();							  	    	
							  	    }

							  	    //if(idfNewDoc.getPermitEx( s_Author) != liDocMaxPermit) {
							  	        idfNewDoc.grant(s_Author, liDocMaxPermit,"");
							  		    idfNewDoc.save();
							  	    //}				  	    
							  	  
							  	     i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='L' ");
							  	    if(i_AuthorCnt < 1) {
								  	    IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
								  		idf_PObj.setString("u_obj_id"		, rObjectId);
								  		idf_PObj.setString("u_obj_type"		, objType);
								  		idf_PObj.setString("u_doc_status"	, "L");
							  		    idf_PObj.setString("u_permit_type"	, "D");
										idf_PObj.setString("u_own_dept_yn"	, " ");
										idf_PObj.setString("u_author_id"	, s_Author);
										idf_PObj.setString("u_author_type"	,"U");
								  		idf_PObj.setString("u_create_user"	, dto.getReqUser());
								  		idf_PObj.setTime  ("u_create_date"	, new DfTime());
								  		idf_PObj.setString("u_add_gubun"	, "P");
								  		idf_PObj.save();  
							  	    }
							  	    i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='C' and u_add_gubun='P' ");
							  	    if(i_AuthorCnt < 1) {
							  	    	IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
								  		idf_PObj.setString("u_obj_id"		, rObjectId);
								  		idf_PObj.setString("u_obj_type"		, objType);
								  		idf_PObj.setString("u_doc_status"	, "C");
							  		    idf_PObj.setString("u_permit_type"	, "R");
										idf_PObj.setString("u_own_dept_yn"	, " ");
										idf_PObj.setString("u_author_id"	, s_Author);
										idf_PObj.setString("u_author_type"	,"U");
								  		idf_PObj.setString("u_create_user"	, dto.getReqUser());
								  		idf_PObj.setTime  ("u_create_date"	, new DfTime());
								  		idf_PObj.setString("u_add_gubun"	, "P");
								  		idf_PObj.save();  
							  	    }
//이동-문서							  		
					  	   	}else {
									bWork= setFolderAuthBaseList(userSession, idfSession, s_UFolId, rObjectId , dto, objType) ;
                                    if(!idfNewDoc.getString("u_sec_level").equals("S")) {
                                    	s_Author =  "g_"+ps_RcevCab;
										if(idfNewDoc.getString("u_sec_level").equals("G"))       s_Author =  "g_"+dto.getUGroupOrgCabinetCd();
									    else if(idfNewDoc.getString("u_sec_level").equals("C"))  s_Author =  "g_"+dto.getUComOrgCabinetCd();
													
								  	    int i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='L'");
								  	    int i_AuthorCntClose = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='C' ");
								  	    
								  	    if(i_AuthorCntClose > 0) {
								  	    	String s_Dql_d = "delete edms_auth_base object " +
											        " where u_obj_id = '" + rObjectId + "'  and u_author_id= '"+s_Author +"' and u_doc_status='C' and u_add_gubun='G' "; 
								            idf_Col = DCTMUtils.getCollectionByDQL(idfSession, s_Dql_d, DfQuery.DF_QUERY);
									  		if(idf_Col != null) idf_Col.close();							  	    	
								  	    }
								  	    bWork= setFolderAuthShareList(userSession, idfSession, s_UFolId, idfNewDoc.getString("r_object_id") , dto, objType) ;//문서
								  	    bWork= setFolderAuthBaseList(userSession, idfSession, s_UFolId, idfNewDoc.getString("r_object_id") , dto, objType) ;//문서
								  	    
								  	    if( s_DocStatus.equals("L")) {
								  	    //if(idfNewDoc.getPermitEx( s_Author) != liDocMaxPermit) {
								  	        idfNewDoc.grant(s_Author, liDocMaxPermit,"");
								  	        if((!idfNewDoc.getString("u_sec_level").equals("S") && !idfNewDoc.getString("u_sec_level").equals("T"))) {//
								  	            idfNewDoc.grant(s_Author+"_sub", liDocMaxPermit,"");
								  	        }
								  		    idfNewDoc.save();
								  	    //}
								  	    }
								  	    if(i_AuthorCnt < 1) {
									  	    IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");

									        idf_PObj = idfSession.newObject("edms_auth_base");
									  		idf_PObj.setString("u_obj_id"		, rObjectId);
									  		idf_PObj.setString("u_obj_type"		, objType);
									  		idf_PObj.setString("u_doc_status"	, "L");
								  		    idf_PObj.setString("u_permit_type"	, "D");
											idf_PObj.setString("u_own_dept_yn"	, "Y");
											idf_PObj.setString("u_author_id"	, s_Author);
											idf_PObj.setString("u_author_type"	,"D"); //부서
									  		idf_PObj.setString("u_create_user"	, dto.getReqUser());
									  		idf_PObj.setTime  ("u_create_date"	, new DfTime());
									  		idf_PObj.setString("u_add_gubun"	, "G");
									  		idf_PObj.save();  
								  	    }
									}
							  	    s_Author = dto.getReqUser();  //g_부서 : grant는 하지 않음
							  	    //if(idfNewDoc.getPermitEx( s_Author) != liDocMaxPermit) {
							  	        idfNewDoc.grant(s_Author, liDocMaxPermit,"");
							  		    idfNewDoc.save();
							  	    //}				  	    
							  	    int i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='L' and u_add_gubun='P' ");
							  	    if(i_AuthorCnt < 1) {
								  	    IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
								  	    
								        idf_PObj = idfSession.newObject("edms_auth_base");
								  		idf_PObj.setString("u_obj_id"		, rObjectId);
								  		idf_PObj.setString("u_obj_type"		, objType);
								  		idf_PObj.setString("u_doc_status"	, "L");
							  		    idf_PObj.setString("u_permit_type"	, "D");
										idf_PObj.setString("u_own_dept_yn"	, " ");
										idf_PObj.setString("u_author_id"	, s_Author);
										idf_PObj.setString("u_author_type"	,"U"); //부서
								  		idf_PObj.setString("u_create_user"	, dto.getReqUser());
								  		idf_PObj.setTime  ("u_create_date"	, new DfTime());
								  		idf_PObj.setString("u_add_gubun"	, "P");
								  		idf_PObj.save();  
					  	    	    }
					      	}
					  		//}
//이동--문서
						}else {//문서의 보안등급이 이동하려는 폴더같은 타겟쪽 보안등급보다 더 높거나 같은 경우 
					  		bWork= setFolderAuthShareList(userSession, idfSession, s_UFolId, idfNewDoc.getString("r_object_id") , dto, objType) ;//문서
					  		//문서의 기존 권한체계를 가져감
					  		bWork= setFolderAuthBaseList(userSession, idfSession, idfNewDoc.getString("r_object_id"), idfNewDoc.getString("r_object_id") , dto, objType) ;

							if(!ls_BeforeSecLevel.equals("S")) {
								for(int i=0; i< lDocStatus.size(); i++) {
							  	    String s_Author = "g_" + ps_RcevCab;
							  	    if(lDocStatus.get(i).equals("C")) {
								        if(ls_BeforeSecLevel.equals("G"))      s_Author =  "g_"+dto.getUGroupOrgCabinetCd();
								        else if(ls_BeforeSecLevel.equals("C")) s_Author =  "g_"+dto.getUComOrgCabinetCd();
								    }
							  	    //if(i==0 && idfNewDoc.getPermitEx( s_Author) != liDocMaxPermit) {
							  	    if( s_DocStatus.equals("L")) {
							  	        idfNewDoc.grant(s_Author, liDocMaxPermit,"");
							  	        if((!idfNewDoc.getString("u_sec_level").equals("S") && !idfNewDoc.getString("u_sec_level").equals("T"))) {//
							  	            idfNewDoc.grant(s_Author+"_sub", liDocMaxPermit,"");
							  	        }
							  		    idfNewDoc.save();
							  	    //}
							  	    }
							  	    int i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='"+lDocStatus.get(i)+"'");
							  	    if(i_AuthorCnt < 1) {
								  	    IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
								  		idf_PObj.setString("u_obj_id"		, rObjectId);
								  		idf_PObj.setString("u_obj_type"		, objType);
								  		idf_PObj.setString("u_doc_status"	, lDocStatus.get(i));
							  			if(lDocStatus.get(i).equals("C"))
							  				idf_PObj.setString("u_permit_type"	, "R");
							  			else
								  		    idf_PObj.setString("u_permit_type"	, "D");
										idf_PObj.setString("u_own_dept_yn"	, "Y");
										idf_PObj.setString("u_author_id"	, s_Author);
										idf_PObj.setString("u_author_type"	,"D"); //부서
								  		idf_PObj.setString("u_create_user"	, dto.getReqUser());
								  		idf_PObj.setTime  ("u_create_date"	, new DfTime());
								  		idf_PObj.setString("u_add_gubun"	, "G");
								  		idf_PObj.save();
							  	    }
					  		    }
							}else {  //제한등급 문서의 경우 
								for(int i=0; i< lDocStatus.size(); i++) {
							  	    String s_Author = dto.getReqUser();  //g_부서 : grant는 하지 않음
							  	    //if(i==0 && idfNewDoc.getPermitEx( s_Author) != liDocMaxPermit) {
							  	    if(i==0) {
							  	        idfNewDoc.grant(s_Author, liDocMaxPermit,"");
							  		    idfNewDoc.save();
								    }
							  	    //}
							  	    
							  	    int i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='"+lDocStatus.get(i)+"'");
							  	    if(i_AuthorCnt < 1) {
								  	    IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
								  	    
								        idf_PObj = idfSession.newObject("edms_auth_base");
								  		idf_PObj.setString("u_obj_id"		, rObjectId);
								  		idf_PObj.setString("u_obj_type"		, objType);
								  		idf_PObj.setString("u_doc_status"	, lDocStatus.get(i));
							  			if(lDocStatus.get(i).equals("C"))
							  				idf_PObj.setString("u_permit_type"	, "R");
							  			else
								  		    idf_PObj.setString("u_permit_type"	, "D");
										idf_PObj.setString("u_own_dept_yn"	, " ");
										idf_PObj.setString("u_author_id"	, s_Author);
										idf_PObj.setString("u_author_type"	,"D"); //부서
								  		idf_PObj.setString("u_create_user"	, dto.getReqUser());
								  		idf_PObj.setTime  ("u_create_date"	, new DfTime());
								  		if((idfNewDoc.getString("u_sec_level").equals("S"))) {
								  		    idf_PObj.setString("u_add_gubun"	, "G");
								  		}else {
								  			idf_PObj.setString("u_add_gubun"	, "P");
								  		}
								  		idf_PObj.save();  
							  	    }
							  	    
					  		    }
								String s_Author="";
								if(idfNewDoc.getString("u_doc_status").equals("L") ) {
							  	    s_Author = "g_" + ps_RcevCab;
							  	    
							  	    liDocMaxPermit=3;//제한문서는 부서에 Read권한만 

							  	    //if(idfNewDoc.getPermitEx( s_Author) != liDocMaxPermit) {
							  	    if(s_DocStatus.equals("L")) {
							  	        idfNewDoc.grant(s_Author, liDocMaxPermit,"");
							  	        if((!idfNewDoc.getString("u_sec_level").equals("S") && !idfNewDoc.getString("u_sec_level").equals("T"))) {//
							  	            idfNewDoc.grant(s_Author+"_sub", liDocMaxPermit,"");
							  	        }
							  		    idfNewDoc.save();
							  	    //}
							  	    }
							  	    int i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='L'");
							  	    if(i_AuthorCnt < 1) {
								  	    IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
								  	    
								        idf_PObj = idfSession.newObject("edms_auth_base");
								  		idf_PObj.setString("u_obj_id"		, rObjectId);
								  		idf_PObj.setString("u_obj_type"		, objType);
								  		idf_PObj.setString("u_doc_status"	, "L");
							  		    idf_PObj.setString("u_permit_type"	, "D");
										idf_PObj.setString("u_own_dept_yn"	, "Y");
										idf_PObj.setString("u_author_id"	, s_Author);
										idf_PObj.setString("u_author_type"	,"D"); //부서
								  		idf_PObj.setString("u_create_user"	, dto.getReqUser());
								  		idf_PObj.setTime  ("u_create_date"	, new DfTime());
								  		idf_PObj.setString("u_add_gubun"	, "G");
								  		idf_PObj.save();  
							  	    }
							  	    liDocMaxPermit=7;//이동을 할 수 있는 작업자는 7 권한   
							  	    s_Author = dto.getReqUser();  //g_부서 : grant는 하지 않음
							  	    //if(idfNewDoc.getPermitEx( s_Author) != liDocMaxPermit) {
							  	        idfNewDoc.grant(s_Author, liDocMaxPermit,"");
							  		    idfNewDoc.save();
							  	    //}
/*							  		    
							  	    i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='L' ");
							  	    if(i_AuthorCnt > 0) {
							  	    	String s_Dql_d = "delete edms_auth_base object " +
										        " where u_obj_id = '" + rObjectId + "'  and u_author_id= '"+s_Author +"' and u_doc_status='L'  and u_add_gubun not in('S','J','W','P') "; 
							            idf_Col = DCTMUtils.getCollectionByDQL(idfSession, s_Dql_d, DfQuery.DF_QUERY);
								  		if(idf_Col != null) idf_Col.close();							  	    	
							  	    }

							  	    IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
							        idf_PObj = idfSession.newObject("edms_auth_base");
							  		idf_PObj.setString("u_obj_id"		, rObjectId);
							  		idf_PObj.setString("u_obj_type"		, objType);
							  		idf_PObj.setString("u_doc_status"	, "L");
						  		    idf_PObj.setString("u_permit_type"	, "D");
									idf_PObj.setString("u_own_dept_yn"	, "Y");
									idf_PObj.setString("u_author_id"	, s_Author);
									idf_PObj.setString("u_author_type"	,"U"); //부서
							  		idf_PObj.setString("u_create_user"	, dto.getReqUser());
							  		idf_PObj.setTime  ("u_create_date"	, new DfTime());
							  		if((idfNewDoc.getString("u_sec_level").equals("S"))) {
							  		    idf_PObj.setString("u_add_gubun"	, "G");
							  		}else {
							  			idf_PObj.setString("u_add_gubun"	, "P");
							  		}
							  		idf_PObj.save();  
*/							  		
					  	    	}else {
					  	    		
							  	    s_Author = "g_" + ps_RcevCab;
							  	    int i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='L'");
							  	    if(i_AuthorCnt < 1) {
								  	    IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
								  	    
								        idf_PObj = idfSession.newObject("edms_auth_base");
								  		idf_PObj.setString("u_obj_id"		, rObjectId);
								  		idf_PObj.setString("u_obj_type"		, objType);
								  		idf_PObj.setString("u_doc_status"	, "L");
							  		    idf_PObj.setString("u_permit_type"	, "D");
										idf_PObj.setString("u_own_dept_yn"	, "Y");
										idf_PObj.setString("u_author_id"	, s_Author);
										idf_PObj.setString("u_author_type"	,"D"); //부서
								  		idf_PObj.setString("u_create_user"	, dto.getReqUser());
								  		idf_PObj.setTime  ("u_create_date"	, new DfTime());
								  		idf_PObj.setString("u_add_gubun"	, "G");
								  		idf_PObj.save();  
							  	    }

							  	    
							  	    s_Author = dto.getReqUser();  //g_부서 : grant는 하지 않음
							  	    //if(idfNewDoc.getPermitEx( s_Author) != liDocMaxPermit) {
							  	        idfNewDoc.grant(s_Author, liDocMaxPermit,"");
							  		    idfNewDoc.save();
							  	    //}				  	    
							  	    
							  	    i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='L' ");
							  	    if(i_AuthorCnt < 1) {
								  	    IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
								  	    
								        idf_PObj = idfSession.newObject("edms_auth_base");
								  		idf_PObj.setString("u_obj_id"		, rObjectId);
								  		idf_PObj.setString("u_obj_type"		, objType);
								  		idf_PObj.setString("u_doc_status"	, "L");
							  		    idf_PObj.setString("u_permit_type"	, "D");
										idf_PObj.setString("u_own_dept_yn"	, "Y");
										idf_PObj.setString("u_author_id"	, s_Author);
										idf_PObj.setString("u_author_type"	,"U"); //사용자
								  		idf_PObj.setString("u_create_user"	, dto.getReqUser());
								  		idf_PObj.setTime  ("u_create_date"	, new DfTime());
								  		idf_PObj.setString("u_add_gubun"	, "P");
								  		idf_PObj.save();  
					  	    	    }
					  	    	}								
							}
							
						}
						if(null !=idfUpFol_Obj) {
							if(!idfUpFol_Obj.getString("u_sec_level").equals("S") && !ls_BeforeSecLevel.equals("S")) { //제한등급이 아닐때 팀을 u_add_gubun='G' 로 추가
								
								if(s_DocStatus.equals("C")) {
								    if(lDocStatus.size() < 2 ) lDocStatus.add("L");
								}
								
								for(int i=0; i< lDocStatus.size(); i++) {
							  	    String s_Author = "g_" + ps_RcevCab;  //g_부서 : grant는 하지 않음
							  	    
									if (secLevelMap.get(idfUpFol_Obj.getString("u_sec_level")) < secLevelMap.get(dto.getTargetSecLevel())) {
									    if(idfUpFol_Obj.getString("u_sec_level").equals("G"))      s_Author =  "g_"+dto.getUGroupOrgCabinetCd();
									    else if(idfUpFol_Obj.getString("u_sec_level").equals("C")) s_Author =  "g_"+dto.getUComOrgCabinetCd();
									    else if(idfUpFol_Obj.getString("u_sec_level").equals("T")) s_Author =  "g_"+dto.getTgCabinetcode();
									}
									if( lDocStatus.get(i).equals(s_DocStatus)) {
								  	    //if(i==0 && idfNewDoc.getPermitEx( s_Author) != liDocMaxPermit) {
										if( s_DocStatus.equals("L")) {
								  	        idfNewDoc.grant(s_Author, liDocMaxPermit,"");
								  	        if((!idfNewDoc.getString("u_sec_level").equals("S") && !idfNewDoc.getString("u_sec_level").equals("T"))) {//
								  	            idfNewDoc.grant(s_Author+"_sub", liDocMaxPermit,"");
								  	        }
								  		    idfNewDoc.save();
								  	    //}
										}
									}
							  	    int i_AuthorCnt = DCTMUtils.getCountByDQL( idfSession, " edms_auth_base where u_obj_id='"+rObjectId +"' and u_author_id='"+s_Author +"' and u_doc_status='"+lDocStatus.get(i)+"'");
							  	    if(i_AuthorCnt < 1) {
			
								  	    IDfPersistentObject idf_PObj = idfSession.newObject("edms_auth_base");
								        idf_PObj = idfSession.newObject("edms_auth_base");
								  		idf_PObj.setString("u_obj_id"		, rObjectId);
								  		idf_PObj.setString("u_obj_type"		, objType);
								  		idf_PObj.setString("u_doc_status"	, lDocStatus.get(i));
							  			if(lDocStatus.get(i).equals("C"))
							  				idf_PObj.setString("u_permit_type"	, "R");
							  			else
								  		    idf_PObj.setString("u_permit_type"	, "D");
										idf_PObj.setString("u_own_dept_yn"	, "Y");
										idf_PObj.setString("u_author_id"	, s_Author);
										idf_PObj.setString("u_author_type"	,"D"); //부서
								  		idf_PObj.setString("u_create_user"	, dto.getReqUser());
								  		idf_PObj.setTime  ("u_create_date"	, new DfTime());
								  		idf_PObj.setString("u_add_gubun"	, "G");
								  		idf_PObj.save();  		
							  	    }
					  		    }
							}else {
								if(s_DocStatus.equals("C")) {
								    if(lDocStatus.size() < 2 ) lDocStatus.add("L");
								}else if(s_DocStatus.equals("L")) {
									if(lDocStatus.size() < 2 ) lDocStatus.add("L");
								}
						  	    String s_Author = "g_" + ps_RcevCab;  //g_부서 : grant는 하지 않음
								if (secLevelMap.get(idfUpFol_Obj.getString("u_sec_level")) < secLevelMap.get(dto.getTargetSecLevel())) {
								    if(idfUpFol_Obj.getString("u_sec_level").equals("G"))      s_Author =  "g_"+dto.getUGroupOrgCabinetCd();
								    else if(idfUpFol_Obj.getString("u_sec_level").equals("C")) s_Author =  "g_"+dto.getUComOrgCabinetCd();
								    else if(idfUpFol_Obj.getString("u_sec_level").equals("T")) s_Author =  "g_"+dto.getTgCabinetcode();
								}
								for(int i=0; i< lDocStatus.size(); i++) {
									if( lDocStatus.get(i).equals(s_DocStatus)) {
										if( s_DocStatus.equals("L")) {
								  	        idfNewDoc.grant(s_Author, liDocMaxPermit,"");
								  	        if((!idfNewDoc.getString("u_sec_level").equals("S") && !idfNewDoc.getString("u_sec_level").equals("T"))) {//
								  	            idfNewDoc.grant(s_Author+"_sub", liDocMaxPermit,"");
								  	        }
								  		    idfNewDoc.save();
										}
									}
								}
								
							}
						}
					}
			    }
			}	
		    

    	} catch (Exception e) {
			e.printStackTrace();
	    }finally {
	    	if(idf_Col !=null) idf_Col.close();
	    }
    }
    
    /*
  	//중복된 폴더명 확인 후 최종 폴더명 반환(root용)
  	 * 
  	 */
      @Override
  	public String getChekedPjtName(IDfSession  idf_Sess, String ps_CabinetCode, String ps_FinishYn, String s_PjtName, DPath dto) throws Exception{
      	
  	 	Map<String, String> exFolMap = new HashMap<String,String>(); //타겟폴더 root에 존재하는 폴더들
  	 	Map<String, String> exPjtMap = new HashMap<String,String>(); //r_object_id, u_pjt_code맵
  	 	//폴더 중복체크
  	 	if(null !=s_PjtName)
	 		if(s_PjtName.contains("'"))
	 			s_PjtName = s_PjtName.replace("'","''");
  	 		
  	 	String s_Dql = "select r_object_id, u_pjt_code, u_pjt_name from edms_project where u_cabinet_code = '" + ps_CabinetCode + "' " +
  			          "and u_finish_yn ='"+ ps_FinishYn+"' " +
  				      "and u_pjt_name like '" + s_PjtName + "%'";
  		IDfQuery 		idf_Qry 	= null;
  		idf_Qry = new DfQuery();
  		idf_Qry.setDQL(s_Dql);

  		if(null !=s_PjtName) {
 		    if(s_PjtName.contains("''"))
 			    s_PjtName = s_PjtName.replace("''","'");
        }
  		
  		IDfCollection idf_Col=null;
  		try {
  			idf_Col = idf_Qry.execute(idf_Sess,DfQuery.QUERY);

	  		//edms_auth_base에서 찾아서 revoke하고
	  		while(idf_Col != null && idf_Col.next())
	  		{
	  			String exFolName =idf_Col.getString("u_pjt_name");
	  	 		if(exFolName.contains("''"))
	  	 			exFolName = exFolName.replace("''","'");
	  			
	  			String s_RObjectId =idf_Col.getString("r_object_id");
	  			String s_UPjtCode  =idf_Col.getString("u_pjt_code");
	  			
	  			exFolMap.put(exFolName, s_RObjectId);
	  			exPjtMap.put(s_RObjectId, s_UPjtCode);
	  		}
	  		String orgPjtName=s_PjtName;
	  		
	  		if(!exFolMap.isEmpty()) {
				String s_RObjectId =s_PjtName;
				int k=3;
				//for(int i=0; i < k ; i++) {
				for(int i=1; i < k ; i++) {
					if( null !=exFolMap.get(s_PjtName)) {
						s_RObjectId = exFolMap.get(s_PjtName);

						s_PjtName=orgPjtName+" ("+ i +")" ;
						k++;
					}else {
					    break;
					}
				}
			    //if(orgPjtName.equals( s_PjtName))s_PjtName=s_RObjectId;
	  		}
/*				
	  		if(!exFolMap.isEmpty()) {
	  			
	  			if( exFolMap.get(s_PjtName) !=null) {
	
					String s_RObjectId = exFolMap.get(s_PjtName);
	
					boolean gwonhan = authService.isRootAuthenticated(HamType.findByValue( "P"), exPjtMap.get(s_RObjectId), dto.getReqUser()); //해당 프로젝트에 대한 쓰기권한확인
					if(gwonhan) {
						return s_RObjectId;	//권한이 있으면 폴더를 새로 만들지 않고 기존폴더에 복사한다			
					}else {
						s_PjtName = orgPjtName+"(1)";
					}
	  			    
	  			    String tmpS_FolName = s_PjtName;
	  	  			for(int k=1;  k < 10000 ; k++) {
	  	  				if( exFolMap.get(tmpS_FolName)==null ) {
	  	  				    s_PjtName=tmpS_FolName;
	  	  					break;
	  	  				}else {
	  	  					tmpS_FolName = orgPjtName + "("+ k+")";
	  	  				}
	  			    }
	  			}
	  		}
*/	  		
  		} catch (Exception e) {
		    e.printStackTrace();
		}finally {
            if(idf_Col !=null ) idf_Col.close();			
		}
      	
      	return s_PjtName;
      }    
      /*
    	//중복된 연구과제명 확인 후 최종 폴더명 반환(root용)
    	 * 
    	 */
        @Override
    	public String getChekedRscsName(IDfSession  idf_Sess, String ps_CabinetCode, String ps_FinishYn, String s_PjtName, DPath dto) throws Exception{
        	
    	 	Map<String, String> exFolMap = new HashMap<String,String>(); //타겟폴더 root에 존재하는 폴더들
    	 	Map<String, String> exRscMap = new HashMap<String,String>(); //r_object_id, u_rsch_code 맵
    	 	//폴더 중복체크
    	 	if(null !=s_PjtName)
		 		if(s_PjtName.contains("'"))
		 			s_PjtName = s_PjtName.replace("'","''");
    	 	
    	 	String s_Dql = "select r_object_id, u_rsch_code, u_rsch_name from edms_research where u_cabinet_code = '" + ps_CabinetCode + "' " +
    			          "and u_finish_yn ='"+ ps_FinishYn+"' " +
    				      "and u_rsch_name like '" + s_PjtName + "%'";
    		IDfQuery 		idf_Qry 	= null;
    		idf_Qry = new DfQuery();
    		idf_Qry.setDQL(s_Dql);
    		
    		if(null !=s_PjtName)
	 		    if(s_PjtName.contains("''"))
	 			    s_PjtName = s_PjtName.replace("''","'");
    		
    		IDfCollection idf_Col=null;
    		try {
    			idf_Col = idf_Qry.execute(idf_Sess,DfQuery.QUERY);
	    		//edms_auth_base에서 찾아서 revoke하고
	    		while(idf_Col != null && idf_Col.next())
	    		{
	    			String s_RObjectId = idf_Col.getString("r_object_id");//("u_rsch_code");
	      			String s_RschCode  =idf_Col.getString("u_rsch_code");
	      			String s_UPjtCode  =idf_Col.getString("u_pjt_code");
	      			
	    			String exFolName =idf_Col.getString("u_pjt_name");
	    	 		if(exFolName.contains("''"))
	    	 			exFolName = exFolName.replace("''","'");
	    			
	    			exFolMap.put(exFolName, s_RObjectId);
	      			exRscMap.put(s_RObjectId, s_RschCode);
	    		}
	    		String orgPjtName=s_PjtName;
	    		if(!exFolMap.isEmpty()) {
					String s_RObjectId =s_PjtName;
					int k=3;
					//for(int i=0; i < k ; i++) {
					for(int i=1; i < k ; i++) {
						if( null !=exFolMap.get(s_PjtName)) {
							s_RObjectId = exFolMap.get(s_PjtName);

							s_PjtName=orgPjtName+" ("+ i +")" ;
							k++;
						}else {
						    break;
						}
					}
				    //if(orgPjtName.equals( s_PjtName))s_PjtName=s_RObjectId;
		  		}
/*	    		
	    		if(!exFolMap.isEmpty()) {
	
	    			if( exFolMap.get(s_PjtName) !=null) {
					    String s_RObjectId = exFolMap.get(s_PjtName);
					    boolean gwonhan = authService.isRootAuthenticated(HamType.findByValue( "R"), exRscMap.get(s_RObjectId), dto.getReqUser()); //해당 연구과제에 대한 쓰기권한확인
					    if(gwonhan) {
						    return s_RObjectId;	//권한이 있으면 폴더를 새로 만들지 않고 기존폴더에 복사한다			
					    }else {
						    s_PjtName = orgPjtName+"(1)";
					    }
	    			    String tmpS_FolName = s_PjtName;
	    	  			for(int k=1;  k < 10000 ; k++) {
	    	  				if( exFolMap.get(tmpS_FolName)==null ) {
	    	  				    s_PjtName=tmpS_FolName;
	    	  					break;
	    	  				}else {
	    	  					tmpS_FolName = orgPjtName + "("+ k+")";
	    	  				}
	    			    }
	    			}
	    		}
*/	    		
    		} catch (Exception e) {
				e.printStackTrace();
			}finally {
				if(idf_Col !=null ) idf_Col.close();
			}
        	
        	return s_PjtName;
        }   
    /*
	//중복된 폴더명 확인 후 최종 폴더명 반환(root용)
	 * 
	 */
    @Override
	public String getChekedFolderName(IDfSession  idf_Sess, String ps_CabinetCode, String ps_upFolId, String s_FolName, DPath dto, String s_FolId) throws Exception{
    	
    	//String targetGubun=dto.getTargetGubun();
    	//if(targetGubun.substring(0,1).equals("P") || targetGubun.substring(0,1).equals("R")) {
    	//	
    	//}

	 	Map<String, String> exFolMap = new HashMap<String,String>(); //타겟폴더 root에 존재하는 폴더들
	 	//폴더 중복체크
	 	if(null==ps_upFolId || ps_upFolId.equals("")) ps_upFolId=" ";
	 	if(null !=s_FolName)
	 		if(s_FolName.contains("'"))
	 	        s_FolName = s_FolName.replace("'","''");
	 	
        String ls_PrCode = dto.getPrCode();
        if(null !=ls_PrCode && ls_PrCode.equals("")) ls_PrCode=" ";
	 	
		String s_Dql = "select r_object_id, u_fol_name from edms_folder where u_cabinet_code = '" + ps_CabinetCode + "' " +
			          "and u_up_fol_id ='"+ ps_upFolId+"'  and u_delete_status=' ' " +
			          "and u_pr_code ='"+ ls_PrCode+"'" +
				      "and u_fol_name like '" + s_FolName + "%'";

 		if(null !=s_FolName) {
 			if(s_FolName.contains("''"))
 	            s_FolName = s_FolName.replace("''","'");
 		}
	 	
		IDfQuery 		idf_Qry 	= null;
		idf_Qry = new DfQuery();
		idf_Qry.setDQL(s_Dql);
			
		IDfCollection idf_Col=null;
		try {
		    idf_Col = idf_Qry.execute(idf_Sess,DfQuery.QUERY);
			//edms_auth_base에서 찾아서 revoke하고
			while(idf_Col != null && idf_Col.next())
			{
				String exFolId   =idf_Col.getString("r_object_id");
				String exFolName =idf_Col.getString("u_fol_name");
		 		if(exFolName.contains("''"))
		 			exFolName = exFolName.replace("''","'");
				
				exFolMap.put(exFolName, exFolId);
			}
			String orgDocName=s_FolName;
			String ls_FindFolId="";
			if(!exFolMap.isEmpty()) {
				
				if( exFolMap.get(s_FolName) !=null) {
					//권한체크해서
/*					
					if(dto.getUptPthGbn().equals("C")) {
					    String s_RObjectId = exFolMap.get(s_FolName);
					    boolean gwonhan = authService.checkFolderAuth(s_RObjectId, dto.getReqUser(), "D");//GrantedLevels.WRITE.getLabel());
					    if(gwonhan) {
						    return s_RObjectId;	//권한이 있으면 폴더를 새로 만들지 않고 기존폴더에 복사한다			
					    }else {
					        s_FolName = orgDocName+"(1)";
					    }
					}else {
*/					if(!s_FolId.equals("")) {
					    if( dto.getUptPthGbn().equals("M") 
					    		&& exFolMap.get(s_FolName).equals(s_FolId)) {
					    	s_FolName = orgDocName;

					    	return s_FolName;
					    }
						s_FolName = orgDocName+" (1)";
				    }else {
				    	s_FolName = orgDocName+" (1)";
				    }
//					}
				    String tmpS_FolName = s_FolName;
				    int l=3;
		  			for(int k=1;  k < l ; k++) {
		  				if( exFolMap.get(tmpS_FolName)==null ) {
		  					s_FolName=tmpS_FolName;
		  					break;
		  				}else {
		  					tmpS_FolName = orgDocName + " ("+ k+")";
		  				}
		  				l++;
				    }
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			if(idf_Col !=null) idf_Col.close();
		}

    	
    	return s_FolName;
    }
      
    /**
	//중복된 파일명 확인 후 최종 파일명 반환(root용)
	 * ex) 같은 폴더에  테스트.ppt가 있는데 테스트.ppt를 하나 더 복사하면 테스트(1).ppt 라는 이름 부여
	 */
    @Override
	public String getChekedDocNameScnd(IDfSession  idf_Sess, String ps_CabinetCode, String s_UFolId, String ps_DocName, DPath dto, String ps_DocId, String s_Extr) throws Exception{

    	String s_TypeNm="edms_doc";
    	
    	if(s_UFolId.equals("") || s_UFolId.equals(" ")) { //프로젝트 root로 바로 가는 경우 상위폴더id가 없음
    		dto.setUfolStatus("L"); //상위폴더값이 ""나 " " 인 경우
    		s_UFolId=" ";
    		//return ps_DocName;
    		dto.setTargetSecLevel("T"); //부서권한 기본
    	}else {

	    	IDfPersistentObject cpTObj = idf_Sess.getObject(new DfId(s_UFolId));
	    	dto.setUfolStatus("L"); //기본값으로 지정(복사고려)
	    	
	    	if(null !=cpTObj) {
	    		if(cpTObj.getType().getName().equals("edms_folder")) {
	    			if(cpTObj.getString("u_fol_type").equals("DIF")) s_TypeNm="edms_doc_imp";
	    		}
	    		dto.setUfolStatus( cpTObj.getString("u_fol_status")); //상위폴더의 폴더 상태 상속시 이 값을 참조
	    	}
	    	dto.setTargetSecLevel(cpTObj.getString("u_sec_level"));  //상위폴더의 보안등급
    	}
    	
    	Map<String, String> exDocMap = new HashMap<String,String>(); //타겟폴더 root에 존재하는 폴더들
	 	//Document 중복체크
    	//String extStr = DCTMUtils.getFileExtByFileName(ps_DocName);
    	//int i_Pos = ps_DocName.lastIndexOf(".");
		//ps_DocName = (i_Pos > 0) ? ps_DocName.substring(0, i_Pos ) : "";
		String orgDocName=ps_DocName;
		
		ps_DocName = ps_DocName.replace("'","''");
        String ls_PrCode = dto.getPrCode();
        if(null !=ls_PrCode && ls_PrCode.equals("")) ls_PrCode=" ";
        
	 	//if(null !=dto.getPrCode() && !dto.getPrCode().equals(" ") && !dto.getPrCode().equals("")) {
		String s_Dql = "select u_doc_key,title, object_name from "+ s_TypeNm +" where u_cabinet_code = '" + ps_CabinetCode + "' " +
	 			     "and u_fol_id = '" + s_UFolId + "'  " +
	 			     "and u_pr_code = '" + ls_PrCode + "'  " +
				     "and object_name like '" + ps_DocName + "%'  and u_delete_status=' ' and u_file_ext='"+s_Extr +"'";
	 	//}
	 	if(ps_DocName.contains("''"))
	 	    ps_DocName = ps_DocName.replace("''","'");
		IDfQuery 		idf_Qry 	= null;
		idf_Qry = new DfQuery();
		idf_Qry.setDQL(s_Dql);
			
		IDfCollection idf_Col=null;
		try {
			idf_Col = idf_Qry.execute(idf_Sess,DfQuery.QUERY);

			while(idf_Col != null && idf_Col.next())
			{
				String exDocNm =idf_Col.getString("object_name");
			 	if(exDocNm.contains("''"))
			 		exDocNm = exDocNm.replace("''","'");
				
				String exDocKey =idf_Col.getString("u_doc_key");
				exDocMap.put(exDocNm, exDocKey);
			}
			if(!exDocMap.isEmpty()) {
				String s_RObjectId =ps_DocName;
				int k=3;
				//for(int i=0; i < k ; i++) {
				for(int i=1; i < k ; i++) {
					if( null !=exDocMap.get(ps_DocName)) {
						s_RObjectId = exDocMap.get(ps_DocName);
	
						if(s_RObjectId.equals(ps_DocId)) { ps_DocName=s_RObjectId; break;}
	
						ps_DocName=orgDocName+" ("+ i +")" ;
						//ps_DocName=orgDocName+" - 복사본"+ (i<1?"":"("+(i+1)+")") ;
						//i= i==1?i++:i;
	                    //k=i+2;
						k++;
					}else {
					    break;
					}
				}
				if(orgDocName.equals( ps_DocName))ps_DocName=s_RObjectId;
				
			}
		} catch (Exception e) {
			e.printStackTrace();
	    } finally {
		    if(idf_Col != null) idf_Col.close();
	    }
		return ps_DocName;
	}

    
    /** 폴더/파일 이동처리
     * jjg
     * **/
    
    @Override
    public String moveFolderAndFiles(UserSession userSession, IDfSession idfSession, DPath dto, Map<String, Map<String,List<DPath>>> folMapTot, boolean isMobile) throws Exception {
    	//폴더 생성
		String rtnMsg=""; 
		//다른 프로젝트로 이동복사하는 경우에 대한 처리....

		initMapInfo();
		
	    String sOwnSrDeptOrgId = gwDeptService.selectOrgIdByCabinetcode( dto.getSrcCabinetcode());
		String sOwnTgDeptOrgId = gwDeptService.selectOrgIdByCabinetcode( dto.getTgCabinetcode());
		
		String s_CabinetCode = dto.getTgCabinetcode();
		String sTgOrgId = gwDeptService.selectOrgIdByCabinetcode( s_CabinetCode);
		dto.setTgOrgId(sTgOrgId);
		HamInfoResult hamInfo = authService.selectDeptHamInfo(dto.getTgOrgId()).orElse(
	              authService.selectHamInfo(dto.getTgOrgId()).orElseThrow(() -> new NotFoundException(CheckAuthParam.class,  dto.getTgOrgId() )));
		dto.setOwnSrDeptOrgId(sOwnSrDeptOrgId);
		dto.setOwnTgDeptOrgId(sOwnTgDeptOrgId);
		dto.setHamInfo(hamInfo);
		
		String s_ComId   = gwDeptService.selectComCodeByCabinetCode(s_CabinetCode).toLowerCase();
		dto.setTgComId(s_ComId);
		PreservationPeriodDto psrvDto = psrvPeriodDao.selectOneByComCode(s_ComId.toUpperCase());
		psrvPMap.put("S", psrvDto.getUSecSYear());
		psrvPMap.put("C", psrvDto.getUSecCYear());
		psrvPMap.put("G", psrvDto.getUSecGYear());
		psrvPMap.put("T", psrvDto.getUSecTYear());
		
		psrvPMap.put("PJT", psrvDto.getUPjtEverFlag());
		

		try {

			if(folMapTot.size() > 0) {
			
				int iCnt=0;
	        	IDfPersistentObject idfTg_PObj=null;

			    for (Entry<String, Map<String, List<DPath>>> entry : folMapTot.entrySet()) {
			        Map<String, List<DPath>> folMap = folMapTot.get(entry.getKey());

					List<String> folList = new ArrayList<String>();
			        for (Map.Entry<String, List<DPath>> folEntry : folMap.entrySet()) {
				    	folList.add(folEntry.getKey());
				    }
			        
		        	String keyStr=entry.getKey();
		        	if( !keyStr.substring(0,1).equals("P") && !keyStr.substring(0,1).equals("R")) {
			        		rtnMsg=moveFolderAndFile(userSession, idfSession, dto, folList, folMap, isMobile,  keyStr, iCnt);
		        	}else{
		        		if(keyStr.substring(0,1).equals("P")) {  //프로젝트 코드
		        			dto.setPrCode(keyStr);
		        			dto.setPrType("P");
			        		keyStr=keyStr.substring(1);
		        			rtnMsg=moveFolderAndFile(userSession, idfSession, dto, folList, folMap, isMobile, keyStr, iCnt);
		        		}else if(keyStr.substring(0,1).equals("R")) { //연구투자 코드
		        			dto.setPrCode(keyStr);
		        			dto.setPrType("R");
			        		keyStr=keyStr.substring(1);
		        			rtnMsg=moveFolderAndFile(userSession, idfSession, dto, folList, folMap, isMobile,keyStr, iCnt);
		        		}
		        	}
		        	iCnt++;
			    }
			}else {
				List<String> folList = new ArrayList<String>();
				Map<String, List<DPath>> folMap = new HashMap<String, List<DPath>>();
   			    rtnMsg=moveFolderAndFile(userSession, idfSession, dto, folList, folMap, isMobile,"6", 0);
			}

		} catch (Exception e) {
		    e.printStackTrace();
		    rtnMsg=e.getMessage();
        }
		return rtnMsg;
    }
    
    
    @Override
    public String moveFolderAndFile(UserSession userSession, IDfSession idfSession, DPath dto, List<String> folList, Map<String,List<DPath>> folMap, boolean isMobile, String keyStr, int iCnt) throws Exception {

    	try {
			String newFolderName = null;
			/*
			   u_job_code  : RE(등록)
			   u_job_gubun : MOVE
			        중요문서함(DI) -> 부서함  (D)                  : 이동 대상에 대한 생성 이력 생성  
			        중요문서함(DI) -> 공유/협업   (S)               : 이동 대상에 대한 생성 이력 생성
			        중요문서함(DI) -> 프로젝트/투자/연구과제 (P, R)    : 이동 대상에 대한 생성 이력 생성
	
			   u_job_code  : DEL(삭제) => LD 또는 DR (  u_doc_status가 L 이면 LD, C 이면 DR
			   u_job_gubun : MOVE
			        부서함(D)                      -> 중요문서함 (DI)  : 이동 대상에 대한 삭제 이력 생성
			        프로젝트/투자/연구과제 (P, R)      -> 중요문서함 (DI)  : 이동 대상에 대한 삭제 이력 생성
			        조직함 C, M)                   -> 중요문서함 (DI)  : 이동 대상에 대한 삭제 이력 생성
	
			 */		
			//다른 프로젝트로 이동복사하는 경우에 대한 처리....
			//IDfSession idfSession = idfSess != null ? idfSess : this.getIdfSession(userSession);
			
	//		    folList.forEach(System.out::println); //대상 폴더 리스트 (정렬 확인)
		 	if(null !=folList && folList.size() > 0)
		        Collections.sort(folList, new uFolderAscending());
	//		    folList.forEach(System.out::println); //대상 폴더 리스트 (정렬 확인)
	
		    String topFolId = dto.getTargetDboxId(); //옮기려는 최상위 폴더 ID
		    String sUprCodeCheck=dto.getUPrCodeCheck()==null?"9":dto.getUPrCodeCheck();
		    
		    if(null==dto.getPrCode() || dto.getPrCode().equals("") || dto.getPrCode().equals(" ") || dto.getTargetFolType().equals("DFO")) {
		        dto.setPrCode(" ");
		        dto.setPrType(" ");
            }
	
		    IDfPersistentObject idfTg_PObj=null;
	    	String targetGubun = dto.getTargetGubun();
			String s_CabinetCode = dto.getTgCabinetcode();

	
	    	/////////////////////////////////////프로젝트, 연구과제쪽 '폴더' 관련 세팅
	    	String sFinishYn="N";
	    	String sPjtUFolId="";
		    if(targetGubun.equals("PCL") || targetGubun.equals("RCL")) {
		    	idfTg_PObj = idfSession.getObject(new DfId(topFolId));
		    	String objTypeNm= idfTg_PObj.getType().getName();
		    	if(objTypeNm.equals("edms_folder") && targetGubun.equals("PCL"))	    sPjtUFolId=idfTg_PObj.getString("u_fol_id");
		    	else if(objTypeNm.equals("edms_folder") && targetGubun.equals("RCL"))   sPjtUFolId=idfTg_PObj.getString("u_fol_id"); 
	
		    	sFinishYn="Y";
			    topFolId=sPjtUFolId;
	    	}else if(targetGubun.equals("PFN") || targetGubun.equals("RFN") || targetGubun.equals("PIF")) {
	    		sFinishYn="Y" ;
	    		
	    	}else if(targetGubun.equals("PJC") || targetGubun.equals("PIC") || targetGubun.equals("RSC")|| targetGubun.equals("RIC")) { //프로젝트, 연구과제
	    		if (!DfId.isObjectId(topFolId)) topFolId="";
	    		;
	    	}else if(targetGubun.substring(0,1).equals("D")  && ( keyStr.substring(0,1).equals("p")|| keyStr.substring(0,1).equals("r"))) { //올기려는건 프로젝트나 연구과제인데
	    		if( keyStr.substring(0,1).equals("p")|| keyStr.substring(0,1).equals("r")) {
	    		    if(!targetGubun.equals("DWY") && !targetGubun.equals("DIF")) topFolId=""; //타겟이 카테고리여라...아래서 폴더먼저 만들고 하위폴더-문서작업 들어가요
	    		}
	    	}else {
	    		if (!DfId.isObjectId(topFolId)) topFolId="";
	    	}
		    /////////////////////////////////////////////////////////////////
//이동처리
	    	IDfPersistentObject idfSc_PObj = null;
		    
		 	/** 복사대상 기존 폴더 id, 새로만든 폴더 id 맵 : 하위 폴더-파일 생성시에 기존폴더를 이미 만들었는지 확인하고 파일의 u_fol_id 지정을 위해서 사용 */
		 	Map<String, String> folChgMap = new HashMap<String,String>();
		 	Map<String, String> folHapMap = new HashMap<String,String>();
		 	Map<String, String> folChgPrjMap = new HashMap<String,String>(); //폴더로 프로젝트를 만들었을 때,
		 	
		 	Map<String, String> folPathMap = new HashMap<String,String>(); //폴더id, 폴더경로
	
	    	/** 소스쪽 프로젝트 코드가 있는 경우 */
		    if(sUprCodeCheck.substring(0,1).equals("P") || sUprCodeCheck.substring(0,1).equals("R")) {
		        //프로젝트나 폴더를 만들고 그 id가 topFolId가 됨
		    	if(keyStr.substring(0,1).equals("p")) {
                	Optional<Project> pjtOpt = projectDao.selectOne(keyStr);
                	if(pjtOpt.isPresent()) {
        		    	newFolderName = pjtOpt.get().getUPjtName();//소스쪽 프로젝트명
                	}else {
                		newFolderName ="새폴더";
                	}
		    	}
		    	if(keyStr.substring(0,1).equals("r")) {
		    		Optional<Research> researchOpt = researchDao.selectOne(keyStr);
                	if(researchOpt.isPresent()) {
        		    	newFolderName = researchOpt.get().getURschName();//소스쪽 프로젝트명
                	}else {
                		newFolderName ="새폴더";
                	}
		    	}
		    }else if((sUprCodeCheck.equals("9") || sUprCodeCheck ==null) && ( keyStr.substring(0,1).equals("p")|| keyStr.substring(0,1).equals("r"))) { //프로젝트나 연구투자건 통째로, 부서함으로 이동하는 경우 
		        //프로젝트나 폴더를 만들고 그 id가 topFolId가 됨
		    	if(keyStr.substring(0,1).equals("p")) {
                	Optional<Project> pjtOpt = projectDao.selectOne(keyStr);
                	if(pjtOpt.isPresent()) {
        		    	newFolderName = pjtOpt.get().getUPjtName();//소스쪽 프로젝트명
                	}else {
                		newFolderName ="새폴더";
                	}
		    	}
		    	if(keyStr.substring(0,1).equals("r")) {
		    		Optional<Research> researchOpt = researchDao.selectOne(keyStr);
                	if(researchOpt.isPresent()) {
        		    	newFolderName = researchOpt.get().getURschName();//소스쪽 프로젝트명
                	}else {
                		newFolderName ="새폴더";
                	}
		    	}
/*		    	
		    	idfSc_PObj =idfSession.getObject(new DfId(keyStr));
		    	String objTypeNm= idfSc_PObj.getType().getName();
	             
		    	newFolderName = objTypeNm.equals("edms_project")?idfSc_PObj.getString("u_pjt_name"):idfSc_PObj.getString("u_rsch_name");//소스쪽 프로젝트명, 연구과제명
*/		    	
		    	newFolderName = getChekedFolderName( idfSession, dto.getTgCabinetcode(), topFolId, newFolderName, dto, "");
		    	if(DfId.isObjectId(newFolderName)) {
			 	       topFolId = newFolderName;
			 	       folChgMap.put(keyStr, topFolId) ;//(기존폴더ID, 신규폴더ID)
		    		
		    	}else {
					//프로젝트나 연구과제명으로 폴더 생성 
		            RegistFolderDto fto = RegistFolderDto.builder().uUpFolId(topFolId).uFolName(  newFolderName)
			            .uCabinetCode(dto.getTgCabinetcode()).uFolType(dto.getTargetFolType())
			            .uSecLevel(null!=idfSc_PObj?idfSc_PObj.getString("u_sec_level"):dto.getTargetSecLevel())
			            .uFolStatus(FolderStatus.ORDINARY.getValue())
			            .uCreateUser(userSession.getUser().getUserId())
			            .uPrCode(dto.getPrCode())
			            .uPrType(dto.getPrType())
			            .uDeleteStatus("")
			            .build();
				 	       topFolId = folderService.createFolder(idfSession, fto);
				 	       folChgMap.put(keyStr, topFolId) ;//(기존폴더ID, 신규폴더ID)
				 	       
				 	   // 공유/협업 권한 해제
				 	       if(dto.getTargetGubun().substring(0,1).equals("S")) {
	/*						
						  	  	IDfPersistentObject idf_PObj = RegistAuthShareDto.toIDfPersistentObject(
							    		idfSession, 
							    		RegistAuthShareDto.builder()
							    			.uObjId(dataId)
								    		.uAuthorId(item.getTargetId())
								    		.uAuthorType(item.getType())
								    		.uPermitType(item.getPermitType())
								    		.build()
							    	);
	*/						    	
							}
				 	       //dto.setPreFolderId( "");
				 	       dataPathService.addMoveAuthBase( userSession,idfSession, topFolId, dto.getTgCabinetcode(), "F", dto, null); //새로만든 폴더에 권한 부여	(dto는 PreSet 전달용)	    	
		    	}
		    }
	//이동처리
		    if(sUprCodeCheck !=null && !sUprCodeCheck.equals("9")&& !sUprCodeCheck.equals("") && ( keyStr.substring(0,1).equals("p")|| keyStr.substring(0,1).equals("r"))) {//프로젝트나 연구과제로 프로젝트나 연구과제를 생성해야 하는 경우
		    	
		    	String rObjectId=keyStr;
		    	if(keyStr.substring(0,1).equals("p")) {
                	Optional<Project> pjtOpt = projectDao.selectOne(keyStr);
                	if(pjtOpt.isPresent()) {
                		rObjectId = pjtOpt.get().getRObjectId();//소스쪽 프로젝트명
                	}
		    	}
		    	if(keyStr.substring(0,1).equals("r")) {
		    		Optional<Research> researchOpt = researchDao.selectOne(keyStr);
                	if(researchOpt.isPresent()) {
                		rObjectId = researchOpt.get().getRObjectId();//소스쪽 프로젝트명
                	}
		    	}
		    	
		    	idfTg_PObj = idfSession.getObject(new DfId(rObjectId));
		    	String s_TypeName = idfTg_PObj.getType().getName();
		    	
			    if(sUprCodeCheck.substring(0,1).equals("P")) {
			    	if(s_TypeName.equals("edms_project") ){
				    	newFolderName = getChekedPjtName(idfSession, dto.getTgCabinetcode(), sFinishYn, newFolderName, dto);
				    	idfTg_PObj.setString("u_finish_yn", sFinishYn);
				    	idfTg_PObj.setString("u_fol_id", topFolId);
				    	
				    	String sFolId= idfTg_PObj.getString("u_pjt_code");
				    	if(DfId.isObjectId(newFolderName)) {
			    		    idfTg_PObj = idfSession.getObject(new DfId(newFolderName));
			    		    sFolId = idfTg_PObj.getString("u_pjt_code");
				    	}
				    	//String sFolId = idfTg_PObj.getString("u_pjt_code");
				        dto.setPrCode( sFolId );
						dto.setPrType("P");//프로젝트
	
						// 책임자 그룹 생성
					    String s_GroupCode = "g_"+ sFolId+"_pjtmgr";
				        IDfGroup idf_GroupObj = (IDfGroup)idfSession.getObjectByQualification("dm_group where group_name = '"+ s_GroupCode +"'");
			            if(idf_GroupObj == null) {
							idf_GroupObj = (IDfGroup)idfSession.newObject("dm_group");
							idf_GroupObj.setGroupName(s_GroupCode);
						}
						idf_GroupObj.removeAllUsers();
						idf_GroupObj.addUser(userSession.getUser().getUserId());
						idf_GroupObj.save();
							
						//권한작업 필요
						idfTg_PObj.save();
						
	
			    	}else { //프로젝트에서 연구과제로 오는 경우(새로 만들어줌)
				    	newFolderName = getChekedRscsName(idfSession, dto.getTgCabinetcode(), sFinishYn, newFolderName, dto);
				    	String sFolId = newFolderName;
				    	if(!DfId.isObjectId(newFolderName)) {
				            sFolId = researchService.createResearch(userSession, ResearchCreateDto.builder().uRschName(newFolderName).uListOpenYn("Y").uFinishYn(sFinishYn).uOwnDept(userSession.getUser().getOrgId()).uSecLevel(dto.getTargetSecLevel()).uCreateUser(dto.getReqUser()).uFolId(topFolId).build(), idfSession);
				    	}else {
			    		    idfTg_PObj = idfSession.getObject(new DfId(newFolderName));
			    		    sFolId = idfTg_PObj.getString("u_pjt_code");
				    	}
				        dto.setPrCode(sFolId);
						dto.setPrType("R");//연구및과제
			    	}
			    }
			    else if(sUprCodeCheck.substring(0,1).equals("R") ) {
			    	if(s_TypeName.equals("edms_research")){
				    	newFolderName = getChekedRscsName(idfSession, dto.getTgCabinetcode(), sFinishYn, newFolderName, dto);
				    	idfTg_PObj.setString("u_finish_yn", sFinishYn);
				    	idfTg_PObj.setString("u_fol_id", topFolId);
				    	
				    	String sFolId = idfTg_PObj.getString("u_rsch_code");
				    	if(DfId.isObjectId(newFolderName)) {
			    		    idfTg_PObj = idfSession.getObject(new DfId(newFolderName));
			    		    sFolId = idfTg_PObj.getString("u_rsch_code");
				    	}				    	
				        dto.setPrCode(sFolId);
						dto.setPrType("R");//연구및과제
	                    
						// 책임자 그룹 생성
					    String s_GroupCode = "g_"+ sFolId+"_rschmgr";
				        IDfGroup idf_GroupObj = (IDfGroup)idfSession.getObjectByQualification("dm_group where group_name = '"+ s_GroupCode +"'");
			            if(idf_GroupObj == null) {
							idf_GroupObj = (IDfGroup)idfSession.newObject("dm_group");
							idf_GroupObj.setGroupName(s_GroupCode);
						}
						idf_GroupObj.removeAllUsers();
						idf_GroupObj.addUser(userSession.getUser().getUserId());
						idf_GroupObj.save();
						
						//권한작업 필요
						idfTg_PObj.save();
	
			    	}else {//연구과제에서 프로젝트로 오는 경우(새로 만들어줌)
				    	newFolderName = getChekedPjtName(idfSession, dto.getTgCabinetcode(), sFinishYn, newFolderName, dto);
				    	//String sFolId = newFolderName;
				    	//if(!DfId.isObjectId(newFolderName)) {
				    	String    sFolId = projectService.createProject(userSession, ProjectCreateDto.builder().uPjtName(newFolderName).uListOpenYn("Y").uFinishYn(sFinishYn).uOwnDept(userSession.getUser().getOrgId()).uFolId(topFolId).uSecLevel(dto.getTargetSecLevel()).uCreateUser(dto.getReqUser()).build(), idfSession);
				    	//}else {
				    	//	idfTg_PObj = idfSession.getObject(new DfId(sFolId));
				    	//	sFolId = idfTg_PObj.getString("u_pjt_code");
				    	//}
				        dto.setPrCode(sFolId);
						dto.setPrType("P");//프로젝트			         
			    	}				
			    }
	        }else if( sUprCodeCheck.equals("9")){
	        	if(DfId.isObjectId(topFolId)) {
				    idfTg_PObj = idfSession.getObject(new DfId(topFolId));
				    if(idfTg_PObj !=null) {
				    	String objTypeNm= idfTg_PObj.getType().getName();    		    	
		    		    if(objTypeNm.equals("edms_doc") || objTypeNm.equals("edms_folder")) {
				            dto.setPrCode(idfTg_PObj.getString("u_pr_code"));
						    dto.setPrType(idfTg_PObj.getString("u_pr_type"));
						    dto.setTargetSecLevel(idfTg_PObj.getString("u_sec_level"));
		    		    }else if(objTypeNm.equals("edms_project")) {
				            dto.setPrCode(idfTg_PObj.getString("u_pjt_code"));
				            dto.setPrType("P");
						    dto.setTargetSecLevel(idfTg_PObj.getString("u_sec_level"));//프로젝트
		    		    }else if(objTypeNm.equals("edms_research")) {
				            dto.setPrCode(idfTg_PObj.getString("u_rsch_code"));
				            dto.setPrType("R");
						    dto.setTargetSecLevel(idfTg_PObj.getString("u_sec_level"));//연구및과제
		    		    }
				    }
	        	}
	        }
	
		 	String uFolId ="" ;//원래 상위폴더 ID
		 	String nFolId ="";
	        
		 	
			String targetBoxGubun=dto.getTargetGubun().substring(0,2);
			String targetGubunCp = uTgtMap.get(targetBoxGubun)==null? uTgtMap.get(targetBoxGubun.substring(0,1)):uTgtMap.get(targetBoxGubun);
			
			String lsJobCode="DM";
			String lsJobGubun="DM";

			boolean isLog=true;//false; //2022.01.12 모든 경우 로그 남기도록 함(이은주 차장 요청, 중요문서함 도입후 다시 고민해보기로)
			
			boolean isSameType=true;
			
			String s_FolType = dto.getTargetFolType(); //controller에서 변환
	
			List<String> sFolArray=Arrays.asList("");
			List<String> sFilArray=Arrays.asList("");
			
			if( keyStr.equals("6") || (!keyStr.substring(0,1).equals("p") && !keyStr.substring(0,1).equals("r"))) {
			    sFolArray = dto.getSourceFolders();  //edms_folder   에서 관리되는 대상 폴더들 
			    sFilArray = dto.getSourceFiles();    //edms_doc      에 존재하는 대상 파일들
			}
	
			IDfPersistentObject cpTObj = null;
			if(DfId.isObjectId(topFolId))
			    cpTObj =idfSession.getObject(new DfId(topFolId));
			
		 	if(sFolArray.size() > 0 && sFolArray.get(0) !="" ) {
			 	for(int c=0; c < sFolArray.size(); c++) {
	    		 	uFolId =sFolArray.get(c);
	    		 	if(!uFolId.equals(keyStr)) continue;

	    		 	IDfPersistentObject cpFoObj = idfSession.getObject(new DfId(uFolId));
	    		 	String s_FolName = cpFoObj.getString("u_fol_name");
	    		 	
	    			if(dto.getSrcCabinetcode().equals(dto.getTgCabinetcode()) && (!dto.getTargetGubun().equals("POW") && !dto.getTargetGubun().equals("ROW")&& !dto.getTargetGubun().equals("DPC")))
	    				if(null ==dto.getTargetSecLevel() || dto.getTargetSecLevel().equals("")|| dto.getTargetSecLevel().equals(" "))
	    			        dto.setTargetSecLevel(cpFoObj.getString("u_sec_level"));//폴더의 보안등급과 같게 프로젝트를 만들예정
	    		 	
	    		    String sourceBoxGubun=  cpFoObj.getString("u_fol_type").substring(0,2);
	    			String sourceGubun = uSrcMap.get(sourceBoxGubun)==null? uSrcMap.get(sourceBoxGubun.substring(0,1)):uSrcMap.get(sourceBoxGubun);
	    			//dto.setSourceGubun(sourceGubun);
	    			if(targetGubunCp.equals(sourceGubun)) {
	    				isLog=true; //로그기록대상
	    				lsJobGubun =sourceGubun;
	    				lsJobCode=lsJobGubun;
	    			}
	    			isSameType=true;
	    			
		    		cpFoObj.setString("u_pr_code",  dto.getPrCode());
		    		cpFoObj.setString("u_pr_type",  dto.getPrType());
		    		cpFoObj.setString("u_delete_status", " ");

		    		
	    			//소스와 타겟 어느 한쪽이 중요문서함인 경우, edms_doc이나 edms_doc_imp에 새로운 파일을 만들어야 함.
	    			if(!sourceBoxGubun.equals(targetBoxGubun) && (sourceBoxGubun.equals("DI") || sourceBoxGubun.equals("DI")))  {
	    				isSameType=false;
	    			}
		    	
	    			//중복된 폴더명 확인 후 최종 폴더명 반환(root용), 옮기려는 폴더(topFolId)하위에 s_FolName과 같은 이름이 있으면 (1), (2)...로 붙임
			    	if(cpTObj !=null) {
			    	    if( !cpTObj.getType().getName().equals("edms_folder") ) 
	    		 	    {
	    		 	        topFolId = folChgMap.get(uFolId);
	    		 	    }
			    	}
	    			s_FolName = getChekedFolderName( idfSession, s_CabinetCode, topFolId, s_FolName, dto, uFolId);
			    	//if(DfId.isObjectId(s_FolName)) {
				 	       //folHapMap.put(uFolId, s_FolName) ;//(기존폴더ID, 합쳐진폴더ID)
				 	       //delHapFolderAuthBase(userSession , idfSession, uFolId);
				 	       //cpFoObj.destroy();  //이동하려는 레벨에 같은 폴더이름이 있으면 기존 폴더정보는 없애면서 auth_base정보도 없애야 한다.
			    	//}else {
		    			/** topFolId 값이 3종류 
		    			   1.프로젝트나 연구과제가 이동하면서 새로 생성된 폴더 : 위에서 새로 만들어진 폴더의 id
		    			   2.부서함에서 프로젝트-연구과제로나  프로젝트-연구과제함간에 폴더가 아닌 프로젝트나 연구과제단위 이동시 만들어진 프로젝트나 연구과제는 "" 빈값  
		    			   3.폴더에서 폴더로 변경되는 경우, targetBoxId로 오는 값을 사용
		    			   
		    			   folChgMap : 폴더가 소스폴더용으로 새로 만들어진 경우
		    			   folHamMap : 이동하려는 폴더에 같은 이름이 있어서 기존폴더를 destroy하는 경우
		    			 * **/
			    		cpFoObj.setString("u_fol_name", s_FolName);
			    		cpFoObj.save();
	
			    		if(null == folChgPrjMap.get( uFolId)) {
			    		    uptFolderPath(idfSession, topFolId, uFolId, s_CabinetCode, userSession.getUser().getUserId(), dto) ;
			    		    dataPathService.addMoveAuthBase( userSession, idfSession, uFolId, s_CabinetCode, "F", dto, null);
	
	  	    			    folChgMap.put(sFolArray.get(c), uFolId);//최상위로 자신을 지정		  	    			    
			    		}
	  	    			
	  	    			if(topFolId !=null && (null ==dto.getPrCode() || dto.getPrCode().equals("")) && (topFolId !=null && !topFolId.equals(dto.getTargetDboxId())))
	  	    			    folChgMap.put(sFolArray.get(c), topFolId);
	  	    			//if(topFolId !=null ) {
	  	    				//if(topFolId.equals(dto.getTargetDboxId()))
	  	    				   //folChgMap.put(sFolArray.get(c), topFolId);
	  	    			//}
			    	//}
				 	}
		 	}
	//이동처리	 	
			//파일은 바로 처리 (u_fol_di값으로 topFolId)
		    //  파일생성
		 	if(sFilArray.size()  > 0 && sFilArray.get(0) !="" && iCnt < 1) {
			 	for(int c=0; c < sFilArray.size(); c++) {
				     String ls_RobjectId 	= DCTMUtils.getCurrentObjectID(idfSession, sFilArray.get(c)); //문서의 최신버전 r_object_id를 가져온다.
        			 if(ls_RobjectId.equals("")) {
	        			throw new NotFoundException("문서 최신버전이 변경됨[Documentum]");
        			 }
					 IDfDocument idfNewDoc =(IDfDocument)idfSession.getObject(new DfId(ls_RobjectId)); 
					 String sDocKey = idfNewDoc.getString("u_doc_key");
				     String s_TypeName = idfNewDoc.getType().getName(); //edms_doc인지 edms_doc_imp 이거나
				     
				     String s_DocName = idfNewDoc.getString("object_name");

				     String s_Extr    = idfNewDoc.getString("u_file_ext");
				     String s_uFolId  = idfNewDoc.getString("u_fol_id");
				     //NONE(1, "N"), BROWSE(2, "B"), READ(3, "R"), RELATE(4, "RELATE"), VERSION(5, "V"), WRITE(6, "W"), DELETE(7, "D");
				     
				     //if( idfObj.getPermit() < 6) continue; //쓰기권한이상이 있으면 복사할 수 있도록  
				     if(null==folChgMap.get( s_uFolId)) {
				         uFolId = topFolId;  //옮기려는 폴더에 같은 이름이 있는지 체크하는 용도
				     }else {
				    	 uFolId = folChgMap.get( s_uFolId);  //옮기려는 폴더에 같은 이름이 있는지 체크하는 용도
				     }
				     //uFolId 타입이 edms_project나 edms_research이면 s_uFolId로 비교하고 uFolId="";
				     
				     String ls_TypeNm="";
				     if(uFolId.equals("") || uFolId.equals(" ")) {
				    	 
				     }else {
					     IDfPersistentObject typeObj = idfSession.getObject(new DfId(uFolId));
					     if(typeObj.getType().getName().equals("edms_folder")){
		    		         String sourceBoxGubun=  typeObj.getString("u_fol_type").substring(0,2);
		    			     String sourceGubun = uSrcMap.get(sourceBoxGubun)==null? uSrcMap.get(sourceBoxGubun.substring(0,1)):uSrcMap.get(sourceBoxGubun);
			    			 if(targetGubunCp.equals(sourceGubun)) {
			    				isLog=true; //로그기록대상
			    				lsJobGubun =sourceGubun;
			    				lsJobCode=lsJobGubun;
			    			 }
				 	     }
					     ls_TypeNm=typeObj.getType().getName();
				     }
	    			 String s_NDocName= s_DocName;
				     if(ls_TypeNm.equals("edms_project") || ls_TypeNm.equals("edms_research")) {//프로젝트나 연구과제로 파일을 직접 복사할 경우
				    	 uFolId = "";
				     }else {
				    	 s_NDocName=getChekedDocNameScnd(idfSession, s_CabinetCode, uFolId, s_DocName, dto, sDocKey, s_Extr);
				     }
				     if(!isSameType) {
				    	 idfNewDoc = s_TypeName.equals("edms_doc")?saveAsNewFiles(idfSession, idfNewDoc, dto):saveAsNewImpFiles(idfSession, idfNewDoc, dto);
				     }
	    			 if (!DfId.isObjectId(s_DocName) && !s_NDocName.equals( sDocKey)) {  //붙여넣기를 다시 했을때, 같은파일이면 파일명을 바꾸지 않는다.
	    				 s_DocName = s_NDocName ;//s_DocName+"[복사본]";
	    			 }
	    			 if(s_NDocName.equals( sDocKey)) continue;
//이동처리

	    			//중복된 파일명 확인 후 최종 폴더명 반환(root용)
					 idfNewDoc.setTitle(s_DocName + "."+s_Extr);
	    			 idfNewDoc.setObjectName(s_DocName);

				     String registObjId = idfNewDoc.getObjectId().toString();
				     
				     idfNewDoc.setBoolean("u_privacy_flag",      false);   //개인정보포함여부 unChk
				     
				     String uDocStatus   = idfNewDoc.getString("u_doc_status");
				     boolean uPrivacyFlag = idfNewDoc.getBoolean("u_privacy_flag");
				     String uSecLevel    = idfNewDoc.getString("u_sec_level").toLowerCase();

				     if (secLevelMap.get(idfNewDoc.getString("u_sec_level")) < secLevelMap.get(dto.getTargetSecLevel())) { //상위폴더의 보안등급이 더 높은 경우
				    	 uSecLevel=  dto.getTargetSecLevel().toLowerCase();
                     }
				     
				     String uPrivacyStr ="g";
				     if(uPrivacyFlag) {    
				    	 uPrivacyStr="p";
				         uSecLevel = uSecLevel.equals("s")?"s":"l";
				     }
				     
				     if(!targetBoxGubun.equals("DI")) {
				    	 if(!dto.getPrCode().equals("") && !dto.getPrCode().equals(" ")) {
				    	     String aclName = "a_"+s_CabinetCode+"_"+dto.getPrType().toLowerCase() +"_"+uPrivacyStr+"_"+uSecLevel+"_n";
				    	     idfNewDoc.setACLName(aclName);
				    	 }else {
				    	     String aclName = "a_"+s_CabinetCode+"_d_"+uPrivacyStr+"_"+uSecLevel+"_n"; //부서함
				             idfNewDoc.setACLName(aclName);
				    	 }
				     }else {
			    	     String aclName = "a_"+s_CabinetCode+"_s_g_s_n"; //중요문서함
			    	     idfNewDoc.setACLName(aclName);
				     }

	//이동처리
				     //idfNewDoc.setACLDomain(idfSession.getDocbaseOwnerName());
				     idfNewDoc.setString("u_pr_code", dto.getPrCode());
				     idfNewDoc.setString("u_pr_type", dto.getPrType());
				     idfNewDoc.setString("u_delete_status", " ");

				     idfNewDoc.setString("u_fol_id", uFolId); //타겟 폴더 ID
				     idfNewDoc.setString("u_doc_key", "" + idfNewDoc.getChronicleId()); // 문서 키
				     idfNewDoc.setString("u_cabinet_code", s_CabinetCode);

				     String s_PreservFlag= psrvPMap.get( dto.getTargetSecLevel());
				     if(null==s_PreservFlag) s_PreservFlag= psrvPMapDf.get( dto.getTargetSecLevel()); //등록된게 없으면 정책서에서 기술한 기본값
				     if(dto.getPrType().equals("P") || dto.getPrType().equals("R")) {
				    	 s_PreservFlag= psrvPMap.get( "PJT");
				    	 if(null !=s_PreservFlag) {
				    		 if(s_PreservFlag.equals("1")) 
				    	         s_PreservFlag="0";
				    	 }
				     }
				     if(null==s_PreservFlag) s_PreservFlag= idfNewDoc.getString("u_preserve_flag");
				     int li_PreservYear=Integer.parseInt(s_PreservFlag);//==0?9999:Integer.parseInt(s_PreservFlag);
				     idfNewDoc.setInt("u_preserve_flag", li_PreservYear );  //보존연한
				     
				     // 추가 권한자 지정 : 기본 권한이나 공유/협업 대상자 등
				     if (idfNewDoc.getString("u_doc_status").equals("C") ) { //Closed문서인 경우에만 만료일자 계산  
						     IDfTime startDate = new DfTime() ;
  						     // Convert the expiration date to a calendar object.
						     GregorianCalendar cal = new GregorianCalendar(startDate.getYear(), startDate.getMonth(),   startDate.getDay() );  
						     // Add the number of months ­1 (months start counting from 0).
						     cal.add(GregorianCalendar.MONTH, (12 * li_PreservYear) -1) ;
						     IDfTime expireDate = new DfTime (cal.getTime()) ;
						     if(li_PreservYear==0)
						    	 idfNewDoc.setString("u_expired_date",  "9999-12-31 00:00:00");
						     else
						         idfNewDoc.setString("u_expired_date",  expireDate.toString());
				     }

				     idfNewDoc.setString("u_update_date", (new DfTime()).toString());
			         if(!isSameType) {
				    	 IDfDocument idfObj =(IDfDocument)idfSession.getObject(new DfId(ls_RobjectId));
				    	 idfObj.destroy(); //edms_doc, edms_doc_imp간 이동시 기존 문서를 삭제처리한다
				     }else {
					     idfNewDoc.save();
				     }
//이동처리	
			         //dto.setPreFolderId("");
			         dataPathService.addMoveAuthBase(userSession, idfSession, idfNewDoc.getString("u_doc_key"), s_CabinetCode,  "D", dto, idfNewDoc);
	
					//------------------------------------------
					// 과거 버전이 있는 경우 처리
					//------------------------------------------
					if(!idfNewDoc.getString("u_doc_key").equals(idfNewDoc.getObjectId().toString()))
					{
						String s_VerId = "";
						IDfDocument idf_VerDoc = null;
						IDfCollection idf_VerCol = idfNewDoc.getVersions(null);
						
						while(idf_VerCol != null && idf_VerCol.next())
						{
							s_VerId = idf_VerCol.getString("r_object_id");
							//과거 버전만 처리하기 위해 현재 버전은 skip
							if(s_VerId.equals(idfNewDoc.getObjectId().toString()))
								continue;
							
							//권한은 현재 버전의 ACL Name을 사용하면 그대로 적용 됨
							idf_VerDoc = (IDfDocument)idfSession.getObject(new DfId(s_VerId));
							idf_VerDoc.setACLDomain(idfSession.getDocbaseOwnerName());
							idf_VerDoc.setACLName(idfNewDoc.getString("acl_name"));
							
							//위치 변경은 ps_RcevFolId 값이 있는 경우만
							idf_VerDoc.setString("u_fol_id", idfNewDoc.getString("u_fol_id"));
							idf_VerDoc.setString("u_pr_code", idfNewDoc.getString("u_pr_code"));
							idf_VerDoc.setString("u_pr_type", idfNewDoc.getString("u_pr_type"));
							
							idf_VerDoc.setString("u_cabinet_code", idfNewDoc.getString("u_cabinet_code"));
							idf_VerDoc.save();
						}
						if(idf_VerCol != null) idf_VerCol.close(); 
					}
			 	}
		 	}
	//이동처리	 	
	    	for(int i=0; i < folList.size(); i++) {
	    		
	    		uFolId = folList.get(i);//상위폴더 ID
	    		
	    		if(uFolId.equals(" ") && ( !keyStr.substring(0,1).equals("p")&& !keyStr.substring(0,1).equals("r"))) continue;
	    		if(uFolId.equals("") && ( keyStr.substring(0,1).equals("p")|| keyStr.substring(0,1).equals("r"))) uFolId=keyStr ;

	    		//if(null !=folHapMap.get(uFolId)) continue;

	    		if(null==uFolId) continue;
	    	    if(null ==dto.getPrCode() || dto.getPrCode().equals("")|| dto.getPrCode().equals(" ")) { //프로젝트나 연구과제로의 이동이 아닌경우
	    			if(uFolId.equals(" ") || uFolId.equals(""))
	    				nFolId = folChgMap.get(keyStr);
	    			else
		    		    nFolId = folChgMap.get(uFolId);
		    		
		    		if(null != nFolId) { //합폴더가 아닌 경우, 
		    			String newUFolId =nFolId;
		    			boolean isUpt=true;
		    			if(!uFolId.equals(keyStr)) {

		    			    IDfPersistentObject cpFoObj = idfSession.getObject(new DfId(uFolId));
		    			    String upFolId = cpFoObj.getString("u_up_fol_id");
		    			    //newUFolId=folHapMap.get(upFolId);
		    			    //if(null== newUFolId) {
		    			        newUFolId = folChgMap.get(upFolId);
		    			    //}
		    			    
		    			    String s_FolName= cpFoObj.getString("u_fol_name");
			    			s_FolName = getChekedFolderName( idfSession, s_CabinetCode, newUFolId, s_FolName, dto, uFolId);
					    	if(DfId.isObjectId(s_FolName)) {
					    		newUFolId=s_FolName;
						 	    folHapMap.put(upFolId, s_FolName) ;//(기존폴더ID, 합쳐진폴더ID)
						 	    //delHapFolderAuthBase(userSession , idfSession, uFolId);
						 	    //cpFoObj.destroy();
						 	    isUpt=false;
					    	}

		                    if( keyStr.substring(0,1).equals("p")|| keyStr.substring(0,1).equals("r")) {
			    			   if(null==newUFolId ) {
			    				   newUFolId = folChgMap.get(keyStr);
			    			   }
			    		    } 
		    			}
			    		if( null !=folChgMap.get(uFolId) &&  folChgMap.get(uFolId).equals(uFolId)) {
			    			;
			    		}else {
		    				dto.setPreFolderId(uFolId );
			    			if(isUpt) {
		  	    			    uptFolderPath(idfSession, newUFolId, uFolId, s_CabinetCode, userSession.getUser().getUserId(), dto) ;
			    			}
			    			dataPathService.addMoveAuthBase( userSession,idfSession, uFolId, s_CabinetCode, "F", dto, null);
			    		}
	  	    			//folChgMap.put(sFolArray.get(i), newUFolId);
		    		}else {
	    			    IDfPersistentObject cpFoObj = idfSession.getObject(new DfId(uFolId));
	    			    String upFolId = cpFoObj.getString("u_up_fol_id");
	    			    dto.setPreFolderId(uFolId );
		    			uptFolderPath(idfSession, upFolId, uFolId, s_CabinetCode, userSession.getUser().getUserId(), dto) ;
		    			dataPathService.addMoveAuthBase( userSession,idfSession, uFolId, s_CabinetCode, "F", dto, null);
		    		}
	    		}else {
					if(uFolId.equals(keyStr)) {

			    		if( null !=folChgMap.get(uFolId) &&  folChgMap.get(uFolId).equals(uFolId)) {
			    			;
			    		}else {
							
					    	IDfPersistentObject cpFoObj = idfSession.getObject(new DfId(uFolId));
					    	if(!cpFoObj.getType().getName().equals("edms_folder")) {
								if(null==folChgMap.get(uFolId))
							        cpFoObj.setString("u_up_fol_id", uFolId);
					    	    else
					    	    	cpFoObj.setString("u_up_fol_id", folChgMap.get(uFolId));
	
				    		    cpFoObj.setString("u_pr_code", dto.getPrCode());
				    		    cpFoObj.setString("u_pr_type", dto.getPrType());
					    		cpFoObj.setString("u_fol_type", dto.getTargetFolType());

				    		    cpFoObj.save();
					    	}else {
					    		if(null !=folChgPrjMap.get(uFolId)) {
					    			//System.out.println(uFolId+"#MOLA:" + folChgPrjMap.get(uFolId));
					    			;
					    		}
								if(null!=folChgPrjMap.get(uFolId) ) 
							        cpFoObj.setString("u_up_fol_id", folChgPrjMap.get(uFolId));
								if(null!=folChgMap.get(uFolId))  
								    cpFoObj.setString("u_up_fol_id", folChgMap.get(uFolId));
					    		    
							    cpFoObj.setString("u_pr_code", dto.getPrCode());
				    		    cpFoObj.setString("u_pr_type", dto.getPrType());
					    		cpFoObj.setString("u_fol_type", dto.getTargetFolType());
				    		    cpFoObj.save();
	
					    		
					    	}
			    		}
	    			    //folHapMap.put(uFolId, dto.getPrCode()) ;//(기존폴더ID, 프로젝트ID)
				    }else {
				    	IDfPersistentObject cpFoObj = idfSession.getObject(new DfId(uFolId));
    	    			String upFolId = cpFoObj.getString("u_up_fol_id");
				    	if(null !=folChgPrjMap.get(upFolId)) {
				    		cpFoObj.setString("u_up_fol_id", "");
				    		cpFoObj.setString("u_pr_code", dto.getPrCode());
				    		cpFoObj.setString("u_pr_type", dto.getPrType());
				    		cpFoObj.setString("u_fol_type", dto.getTargetFolType());
				    		
				    		cpFoObj.save();
				    	}else {
				    		cpFoObj.setString("u_pr_code", dto.getPrCode());
				    		cpFoObj.setString("u_pr_type", dto.getPrType());
				    		cpFoObj.setString("u_fol_type", dto.getTargetFolType());
				    		
				    		cpFoObj.save();
				    	}
				    	
				    }
    			    IDfPersistentObject cpFoObj = idfSession.getObject(new DfId(uFolId));
    			    String upFolId = cpFoObj.getString("u_up_fol_id");
    			    dto.setPreFolderId(uFolId );
	    			uptFolderPath(idfSession, upFolId, uFolId, s_CabinetCode, userSession.getUser().getUserId(), dto) ;
	    			dataPathService.addMoveAuthBase( userSession,idfSession, uFolId, s_CabinetCode, "F", dto, null);
	    		}
	    		List<DPath> folBList = folMap.get(uFolId);

	    		if(null!=folBList ) {
		    		for(int j=0; j < folBList.size(); j++) {
		    			
		    			DPath dData = folBList.get(j);
	//이동처리	    	
		    			uFolId = dData.getRObjectId();
		    			if(dData.getListType().equals("FOL")) {//폴더("FOL" 인 경우)

		    				nFolId = folChgMap.get(uFolId);

		    				if(null !=dto.getPrCode()  && !dto.getPrCode().equals(" ") && !dto.getPrCode().equals("")) {
		    					if(uFolId.equals(keyStr)) {
		    	    			    //folHapMap.put(uFolId, dto.getPrCode()) ;//(기존폴더ID, 프로젝트ID)
		    				    }else {
		    				    	IDfPersistentObject cpFoObj = idfSession.getObject(new DfId(uFolId));
			    	    			String upFolId = cpFoObj.getString("u_up_fol_id");
			    	    			
		      	    			    uptFolderPath(idfSession, upFolId, uFolId, s_CabinetCode, userSession.getUser().getUserId(), dto) ;
		    	    				dto.setPreFolderId(uFolId );
		    	    				dataPathService.addMoveAuthBase( userSession, idfSession, uFolId, s_CabinetCode, "F", dto, null);
		    				    }
		    	    		}else {
			    				if(nFolId==null || nFolId==""|| nFolId==" ") {
			    					
			    					if(uFolId.equals(keyStr)) continue;
			    					
			    	    			IDfPersistentObject cpFoObj = idfSession.getObject(new DfId(uFolId));
			    	    			String upFolId = cpFoObj.getString("u_up_fol_id");
			    	    			String newUFolId=folChgMap.get(upFolId);
			    	    			boolean isUpt=true;
			    	    			if(null==newUFolId) {
			    	    				
			    	    				//newUFolId=folHapMap.get(upFolId);
			    	    				//if(null== newUFolId) {
			    	    			    //    newUFolId = upFolId; //기존 폴더그대로 유지
			    	    				//}
				    	    			if(dto.getPrCode().equals(newUFolId)) {
				    	    				isUpt=false;
				    	    				dto.setPreFolderId(uFolId );
			      	    			        uptFolderPath(idfSession, "", uFolId, s_CabinetCode, userSession.getUser().getUserId(), dto) ;
			      	    			        dataPathService.addMoveAuthBase( userSession, idfSession, uFolId, s_CabinetCode, "F", dto, null);
				    	    			}else {
				    	    				//isUpt=false;
						    			    String s_FolName= cpFoObj.getString("u_fol_name");
							    			s_FolName = getChekedFolderName( idfSession, s_CabinetCode, newUFolId, s_FolName, dto, uFolId);
									    	//if(DfId.isObjectId(s_FolName)) {
									    	//	isUpt=false;
									    		
									    	//	newUFolId=s_FolName;
										 	//    folHapMap.put(uFolId, s_FolName) ;//(기존폴더ID, 합쳐진폴더ID)
									 	        //delHapFolderAuthBase(userSession , idfSession, uFolId);
									 	        //cpFoObj.destroy();  //이동하려는 레벨에 같은 폴더이름이 있으면 기존 폴더정보는 없애면서 auth_base정보도 없애야 한다.
									    	//}
				    	    			}
			    	    			}
			    	    			if(isUpt) {
			    	    				newUFolId=upFolId;
			      	    			    uptFolderPath(idfSession, newUFolId, uFolId, s_CabinetCode, userSession.getUser().getUserId(), dto) ;
			      	    			    upFolId=newUFolId;
			    				    }
		    	    				dto.setPreFolderId(upFolId );
		    	    				dataPathService.addMoveAuthBase( userSession, idfSession, uFolId, s_CabinetCode, "F", dto, null);
			    				}
		    	    		}
		    			}else if(dData.getListType().equals("LNK")) {//링크파일인 경우
	
		    				uptDocLink(idfSession,  uFolId,  dData.getRObjectId(), s_CabinetCode, userSession.getUser().getUserId(), dto);
		    				
		    			}else { //문서("DOC" 인 경우)
		    			    // nFileId = createCopyDoc(...);
		    			    //U_FOL_ID 값에 nFolId인자값을 넣어줌
						     String ls_RobjectId 	= DCTMUtils.getCurrentObjectID(idfSession, dData.getRObjectId()); //문서의 최신버전 r_object_id를 가져온다.
		        			 if(ls_RobjectId.equals("")) {
			        			    throw new NotFoundException("문서 최신버전이 변경됨[Documentum]");
		        			 }
		        			 ls_RobjectId=ls_RobjectId==""?dData.getRObjectId():ls_RobjectId;
						     IDfDocument idfNewDoc = (IDfDocument)idfSession.getObject(new DfId(ls_RobjectId));
						     String sDocKey = idfNewDoc.getString("u_doc_key");
						     String s_TypeName = idfNewDoc.getType().getName();  //edms_doc 문서인지 edms_doc_imp 문서인지
						     //if( idfObj.getPermit() < 6) continue; //쓰기권한이상이 있으면 복사할 수 있도록
	//이동처리					 
						     /* 이동처리************************ 파일 복사 *****************************/
						     if(!isSameType) {
						    	 idfNewDoc = s_TypeName.equals("edms_doc")?saveAsNewFiles(idfSession, idfNewDoc, dto):saveAsNewImpFiles(idfSession, idfNewDoc, dto);
						     }
						     uFolId = idfNewDoc.getString("u_fol_id");
						     String nFoldId = folChgMap.get(uFolId);
						     if(null==nFoldId)  nFoldId= folChgPrjMap.get(uFolId);
						     
						     if(null !=folChgPrjMap.get(uFolId)) nFoldId= folChgPrjMap.get(uFolId);
						     if(keyStr.equals(uFolId) &&  null != folChgMap.get(uFolId) && folChgMap.get(uFolId).equals("") && folChgMap.get(uFolId).equals(" ")) {
						    	 uFolId = idfNewDoc.getString("u_fol_id");
						    	 nFoldId = uFolId;
						     }
						     
						     //if(null==nFoldId)  nFoldId= folHapMap.get(uFolId);
						     
						     String registObjId = idfNewDoc.getObjectId().toString(); //nFoldId 확인지점
						     String s_DocName = idfNewDoc.getString("object_name");
						     
						     String s_Extr    = idfNewDoc.getString("u_file_ext");
						     if(null !=nFoldId ) {
					             String s_NDocName = s_DocName;
					             if(!dto.getPrCode().equals(nFoldId)) {
			    			         s_NDocName=getChekedDocNameScnd(idfSession, s_CabinetCode, nFoldId, s_DocName, dto, sDocKey, s_Extr);
					             }else {
					            	 nFoldId="";
					             }
				    		     if (!DfId.isObjectId(s_NDocName) && !s_NDocName.equals( sDocKey)) {  //붙여넣기를 다시 했을때, 같은파일이면 파일명을 바꾸지 않는다.
				    				 //idfNewDoc.setString("u_doc_status", "L"); // live //함침처리때 룰이라 제거			    				 
				    				 s_DocName =  s_NDocName ;//s_DocName+"[복사본]";
							     }
				    		     if(null == nFoldId) nFoldId=uFolId;

					             idfNewDoc.setString("u_fol_id", nFoldId); //타겟 폴더 ID
			    			     idfNewDoc.setTitle(s_DocName + "."+s_Extr);
				    			 idfNewDoc.setObjectName (s_DocName);
				    			 
						     }
			    			//중복된 파일명 확인 후 최종 폴더명 반환(root용)
						     String uDocStatus   = idfNewDoc.getString("u_doc_status");
						     
						     idfNewDoc.setBoolean("u_privacy_flag",      false);   //개인정보포함여부 unChk
						     
						     boolean uPrivacyFlag = idfNewDoc.getBoolean("u_privacy_flag");
						     String uSecLevel    = idfNewDoc.getString("u_sec_level").toLowerCase();
						     if (secLevelMap.get(idfNewDoc.getString("u_sec_level")) < secLevelMap.get(dto.getTargetSecLevel())) {
				    		     //상위폴더의 보안등급이 더 높은 경우
						    	 uSecLevel=  dto.getTargetSecLevel().toLowerCase();
				    		 }						     
						     String uPrivacyStr ="g";
						     if(uPrivacyFlag) {    
						    	 uPrivacyStr="p";
						         uSecLevel = uSecLevel.equals("s")?"s":"l";
						     }
						     
						     if(!targetBoxGubun.equals("DI")) {
						    	 if(!dto.getPrCode().equals("") && !dto.getPrCode().equals(" ")) {
						    	     String aclName = "a_"+s_CabinetCode+"_"+dto.getPrType().toLowerCase() +"_"+uPrivacyStr+"_"+uSecLevel+"_n";
						    	     idfNewDoc.setACLName(aclName);
						    	 }else {
						    	     String aclName = "a_"+s_CabinetCode+"_d_"+uPrivacyStr+"_"+uSecLevel+"_n"; //부서함
						             idfNewDoc.setACLName(aclName);
						    	 }
						     }else {
					    	     String aclName = "a_"+s_CabinetCode+"_s_g_s_n"; //중요문서함
					    	     idfNewDoc.setACLName(aclName);
						     }

						     idfNewDoc.setOwnerName(idfSession.getDocbaseOwnerName());
						     
						     idfNewDoc.setString("u_doc_key", "" + idfNewDoc.getChronicleId()); // 문서 키
						     String s_PreservFlag= psrvPMap.get( dto.getTargetSecLevel());
						     if(null==s_PreservFlag) s_PreservFlag= psrvPMapDf.get(dto.getTargetSecLevel()); //등록된게 없으면 정책서에서 기술한 기본값
						     if(dto.getPrType().equals("P") || dto.getPrType().equals("R")) {
					    	     s_PreservFlag= psrvPMap.get( "PJT");
					    	     if(null !=s_PreservFlag) {
					    	    	 if(s_PreservFlag.equals("1")) 
					    	             s_PreservFlag="0";
					    	     }
					    	 }
						     if(null==s_PreservFlag) s_PreservFlag= idfNewDoc.getString("u_preserve_flag");
						     int li_PreservYear=Integer.parseInt(s_PreservFlag) ;//==0?9999:Integer.parseInt(s_PreservFlag);
						     idfNewDoc.setInt("u_preserve_flag", li_PreservYear );  //보존연한
						     
						     // 추가 권한자 지정 : 기본 권한이나 공유/협업 대상자 등
						     if (idfNewDoc.getString("u_doc_status").equals("C") ) { //Closed문서인 경우에만 만료일자 계산  
								     IDfTime startDate = new DfTime() ;
								     // Convert the expiration date to a calendar object.
								     GregorianCalendar cal = new GregorianCalendar(startDate.getYear(), startDate.getMonth(),   startDate.getDay() );  
								     // Add the number of months ­1 (months start counting from 0).
								     cal.add(GregorianCalendar.MONTH, (12 * li_PreservYear) -1) ;
								     IDfTime expireDate = new DfTime (cal.getTime()) ;
								     if(li_PreservYear==0)
								    	 idfNewDoc.setString("u_expired_date",  "9999-12-31 00:00:00");
								     else
								         idfNewDoc.setString("u_expired_date",  expireDate.toString());
						     }
						     
						     idfNewDoc.setString("u_delete_status", " ");
						     idfNewDoc.setString("u_pr_code", dto.getPrCode());
						     idfNewDoc.setString("u_pr_type", dto.getPrType());
						     
						     idfNewDoc.setString("u_cabinet_code", s_CabinetCode);
						     idfNewDoc.setString("u_update_date", (new DfTime()).toString());
						     
						     idfNewDoc.save();
						     dto.setPreFolderId(  idfNewDoc.getString("u_copy_org_id"));
						     dataPathService.addMoveAuthBase( userSession,idfSession, idfNewDoc.getString("u_doc_key"), s_CabinetCode, "D", dto, idfNewDoc);
						     
						     //------------------------------------------
							// 과거 버전이 있는 경우 처리
							//------------------------------------------
						    if(!idfNewDoc.getString("u_doc_key").equals(idfNewDoc.getObjectId().toString()))
							{
								String s_VerId = "";
								IDfDocument idf_VerDoc = null;
								IDfCollection idf_VerCol = idfNewDoc.getVersions(null);
								
								while(idf_VerCol != null && idf_VerCol.next())
								{
									s_VerId = idf_VerCol.getString("r_object_id");
									//과거 버전만 처리하기 위해 현재 버전은 skip
									if(s_VerId.equals(idfNewDoc.getObjectId().toString()))
										continue;
									
									//권한은 현재 버전의 ACL Name을 사용하면 그대로 적용 됨
									idf_VerDoc = (IDfDocument)idfSession.getObject(new DfId(s_VerId));
									idf_VerDoc.setACLDomain(idfSession.getDocbaseOwnerName());
									idf_VerDoc.setACLName(idfNewDoc.getString("acl_name"));
									
									//위치 변경은 ps_RcevFolId 값이 있는 경우만
									idf_VerDoc.setString("u_fol_id", idfNewDoc.getString("u_fol_id"));
									idf_VerDoc.setString("u_pr_code", idfNewDoc.getString("u_pr_code"));
									idf_VerDoc.setString("u_pr_type", idfNewDoc.getString("u_pr_type"));
									
									idf_VerDoc.setString("u_cabinet_code", idfNewDoc.getString("u_cabinet_code"));
									idf_VerDoc.save();
								}
								if(idf_VerCol != null) idf_VerCol.close(); 
						   }						     
		  				   if(!isSameType) {
							   IDfDocument idfObj = (IDfDocument)idfSession.getObject(new DfId(ls_RobjectId));
		  				       idfObj.destroy(); //중요함-일반문서함간 이동이면 원본을 삭제처리한다
		  				   }
		    			}		    			
		    		}
	    		}
	    	}
/*	    	
	    	for (Entry<String,String> entry : folHapMap.entrySet()) {
	    		
	    		if(entry.getKey().equals(entry.getValue())) continue;
	    		if(DfId.isObjectId(entry.getKey())) {
	    			IDfPersistentObject idfObj = (IDfPersistentObject)idfSession.getObject(new DfId(entry.getKey()));
		 	        delHapFolderAuthBase(userSession , idfSession, entry.getKey());
		 	        idfObj.destroy();  //이동하려는 레벨에 같은 폴더이름이 있으면 기존 폴더정보는 없애면서 auth_base정보도 없애야 한다.
	    		}
	    	}
*/
	    	if(null !=dto.getPrCode() && !dto.getPrCode().equals(" ") && !dto.getPrCode().equals("")) {
	    		if(sFolArray.size() > 0 && sFolArray.get(0) !="" ) {
	    			if (DfId.isObjectId(keyStr)) {
	    			    IDfPersistentObject idfObj = (IDfPersistentObject)idfSession.getObject(new DfId(keyStr));
	    			    
	    			    if(null !=idfObj.getString("u_pr_code") && !idfObj.getString("u_pr_code").equals(" ") && !idfObj.getString("u_pr_code").equals("")) {
	    			    	
	    			        if(idfObj.getString("u_pr_code").equals(dto.getPrCode()) && ( dto.getTargetGubun().equals("POW") || dto.getTargetGubun().equals("ROW"))) {
		 	                        delHapFolderAuthBase(userSession , idfSession, keyStr);
		 	                        idfObj.destroy();  //이동하려는 레벨에 같은 폴더이름이 있으면 기존 폴더정보는 없애면서 auth_base정보도 없애야 한다.
	    			        }
	    			    }else if( idfObj.getString("u_pr_code").equals("") && (idfObj.getString("u_fol_type").equals("RFO") || idfObj.getString("u_fol_type").equals("PFO") || dto.getTargetGubun().equals("DPC"))) {
	 	                    delHapFolderAuthBase(userSession , idfSession, keyStr);
	 	                    idfObj.destroy();  //이동하려는 레벨에 같은 폴더이름이 있으면 기존 폴더정보는 없애면서 auth_base정보도 없애야 한다.
	    			    }
	    			}
	    		}

	    	}

    	}catch (Exception e) {
		    e.printStackTrace();
		    return e.getMessage();
        }
		return "";
      }
    

     
        /**
         * 파일 복사
         * */
        @Override
        public IDfDocument saveAsNewFiles(IDfSession idfSession, IDfDocument idfDoc, DPath dto) throws Exception{

        	IDfId idfId = idfDoc.saveAsNew(true);
        	String ls_RobjectId = idfId.getId();
        	
        	IDfDocument oNewSO = (IDfDocument)idfSession.getObject(new DfId(ls_RobjectId));
        	

        	oNewSO.setString("u_last_editor", ""); //마지막 편집자 삭제
    		
    		//String s_MDepts = idf_PjtObj.getAllRepeatingStrings("u_own_dept", ",");
    		
    	    oNewSO.unlink( oNewSO.getFolderId(oNewSO.getFolderIdCount()-1).toString());
     		//---------------------------------------------
    	    String s_DCTMFolderId = DCTMUtils.makeEDMFolder(idfSession);
    		oNewSO.link(s_DCTMFolderId);
    		
            if(dto.getUptPthGbn().equals("C")) {
        	    oNewSO.setString("u_copy_org_id", idfDoc.getChronicleId()+""); //복사의 경우 원본파일 id를 기록해준다
            }
            
            oNewSO.truncate("u_wf_key",           1);    //최초 작성자를 유지하고 나머지 사용자들은 truncate한다
            
            int i_ValIdx = oNewSO.findString("u_editor", dto.getReqUser());
    		if(i_ValIdx < 0){ //기존 편집자에 없으면 append해준다
    	        oNewSO.appendString("u_editor"  , dto.getReqUser());
    		}
    		oNewSO.setString("r_creator_name", dto.getReqUser());
            oNewSO.setString("u_reg_user", dto.getReqUser());
            oNewSO.setString("u_reg_date", (new DfTime()).toString());
            oNewSO.setString("u_update_date", (new DfTime()).toString());
           
            oNewSO.setString("u_wf_doc_yn", "N");
            oNewSO.truncate("u_wf_key",           0);    //결재 ID
    	    oNewSO.truncate("u_wf_system",        0);    //결재 시스템
    	    oNewSO.truncate("u_wf_form",          0);    //결재 양식명
            oNewSO.truncate("u_wf_title",         0);    //결재 제목
            oNewSO.truncate("u_wf_approver",      0);    //결재자
            oNewSO.truncate("u_wf_approval_date", 0); 
            oNewSO.truncate("u_wf_link",          0);    //결재화면 URL
            
            oNewSO.setBoolean("u_privacy_flag",      false);   //개인정보포함여부 unChk
            oNewSO.setBoolean("u_auto_auth_mail_flag",true);    //메일자동권한부여 Chk


            if(!idfDoc.getString("u_cabinet_code").equals(dto.getTgCabinetcode()))
                oNewSO.setString("u_cabinet_code", dto.getTgCabinetcode()); // 문서함코드(다른 부서 복사시)
    	    
    	    //edms_auth_base의 소유부서(기본부서)인 것을 지움
    		//기존 권한 유지를 위해 권한자 목록 가져와서 ACL 부여
        	
        	return oNewSO;

        }
        
        /**
         * 파일 복사(중요문서함으로) : edms_doc이나 edms_doc_imp 서로 문서내용을 복사해서 새 문서를 만듬 
         * */
        @Override
        public IDfDocument saveAsNewImpFiles(IDfSession idfSession, IDfDocument idfDoc, DPath dto) throws Exception{

        	String targetBoxGubun=dto.getTargetGubun().substring(0,2);
        	String typeStr = targetBoxGubun.equals("DI")?SysObjectType.DOC_IMP.getValue():SysObjectType.DOC.getValue();

        	IDfDocument oNewSO = (IDfDocument) idfSession.newObject( typeStr);
            if(dto.getUptPthGbn().equals("C")) {
            	oNewSO.setString("u_copy_org_id", idfDoc.getChronicleId()+""); //복사의 경우 원본파일 id를 기록해준다
            }
            oNewSO.setString("u_last_editor", ""); //마지막 편집자 삭제
            // 파일 set
//            idfNewDoc.setFile(idfDoc.get);

            oNewSO.setContentType(idfDoc.getContentType());
            oNewSO.setContent(CommonUtils.convertByteInputStreamToOut(idfDoc.getContent()));

            // 포맷
            oNewSO.setContentType(idfDoc.getContentType());
            // 소유자 지정 : Docbase Owner로 지정
            oNewSO.setOwnerName(idfSession.getDocbaseOwnerName());
            // 업무 속성 지정
            oNewSO.setString("u_pr_code", idfDoc.getString("u_pr_code")); 
            oNewSO.setString("u_pr_type", idfDoc.getString("u_pr_type")); 
            
            if(!idfDoc.getString("u_cabinet_code").equals(dto.getTgCabinetcode()))
                oNewSO.setString("u_cabinet_code", dto.getTgCabinetcode()); // 문서함코드(다른 부서 복사시)
            
            
            oNewSO.setString("u_doc_key", "" + oNewSO.getChronicleId()); // 문서 키
            oNewSO.setString("u_doc_flag", "S"); // 일반문서, M:마이그레이션문서
            
            oNewSO.setString("u_wf_doc_yn", "N");
            oNewSO.truncate("u_wf_key",           0);    //결재 ID
    	    oNewSO.truncate("u_wf_system",        0);    //결재 시스템
    	    oNewSO.truncate("u_wf_form",          0);    //결재 양식명
            oNewSO.truncate("u_wf_title",         0);    //결재 제목
            oNewSO.truncate("u_wf_approver",      0);    //결재자
            oNewSO.truncate("u_wf_approval_date", 0); 
            oNewSO.truncate("u_wf_link",          0);    //결재화면 URL
            
            oNewSO.truncate("u_wf_key",           1);    //최초 작성자를 유지하고 나머지 사용자들은 truncate한다
            int i_ValIdx = oNewSO.findString("u_editor", dto.getReqUser());
    		if(i_ValIdx < 0){ //기존 편집자에 없으면 append해준다
    	        oNewSO.appendString("u_editor"  , dto.getReqUser());
    		}
            
            
            oNewSO.setBoolean("u_privacy_flag",      false);   //개인정보포함여부 unChk
            oNewSO.setBoolean("u_auto_auth_mail_flag",true);   //메일자동권한부여 Chk
            
            oNewSO.setString("u_sec_level", targetBoxGubun.equals("DI")?"S":idfDoc.getString("u_sec_level")); // 보안등급:제한문서
            oNewSO.setString("u_doc_status", DocStatus.LIVE.getValue()); // live
            oNewSO.setString("r_creator_name", dto.getReqUser());
            oNewSO.setString("u_reg_user", dto.getReqUser());
            oNewSO.setString("u_reg_date", (new DfTime()).toString());
            oNewSO.setString("u_update_date", (new DfTime()).toString());
            
    	    String s_DCTMFolderId = DCTMUtils.makeEDMFolder(idfSession);
    	    //oNewSO.unlink(oNewSO.getString("i_folder_id"));
    	    oNewSO.link(s_DCTMFolderId);
            oNewSO.setString("u_fol_id",   dto.getTargetDboxId()); // 부서폴더
            
        	return oNewSO;

        }


}
