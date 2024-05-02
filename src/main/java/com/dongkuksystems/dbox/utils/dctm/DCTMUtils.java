package com.dongkuksystems.dbox.utils.dctm;

import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.StringUtils;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLoginInfo;
import com.documentum.fc.common.DfTime;
import com.documentum.fc.common.IDfLoginInfo;
import com.dongkuksystems.dbox.constants.DCTMConstants;

public class DCTMUtils
{
	/**
	 * 다큐멘텀 Superuser 세션을 생성 및 반환<br>
	 * 사용 후 직접 disconnect 수행 필수
	 * @return IDfSession idf_AdminSess
	 * @throws Exception
	 */
	public static IDfSession getAdminSession() throws Exception
	{
		IDfSession idf_AdminSess = null;
		
		IDfClientX idf_ClientX = new DfClientX();
		IDfClient idf_Client = idf_ClientX.getLocalClient();

		IDfLoginInfo idf_LoginInfo = new DfLoginInfo();
		idf_LoginInfo.setUser(DCTMConstants.DCTM_ADMIN_ID);
		idf_LoginInfo.setPassword(DCTMConstants.DCTM_GLOBAL_PW);
		idf_LoginInfo.setDomain(null);
		
		idf_AdminSess = idf_Client.newSession(DCTMConstants.DOCBASE, idf_LoginInfo);
		
		return idf_AdminSess;
	}
		/**
	 * String 값이 null 인지 체크
	 * @return null이 아니면 원래 값, null이면 "" (String)
	 * @throws Exception
	 */
	public static String checkNull(String s_Str)
	{
		if(s_Str == null || 
		   s_Str.equals("null") || 
		   s_Str.equals("nullstring") || 
		   s_Str.equals("nulldate")
		  ) 
			s_Str = ""; 
		
		return s_Str;
	}
	
	/**
	 * EDM 폴더 생성 (/EDMS/YYYY/MM/DD)
	 * @param idf_Sess : IDfSession (documentum session)
	 * @return New Folder Object ID (String) <br>
	 * @throws Exception
	 */
	public static String makeEDMFolder(IDfSession idf_Sess) throws Exception
	{
		DateFormat df_Format = new SimpleDateFormat("yyyy-MM-dd");
		Date dt_Today = new Date();        
		String[] sa_Today = df_Format.format(dt_Today).split("-");
		
		String s_NewId = "";
		
		s_NewId = makeDCTMFolder(idf_Sess, "/EDMS", sa_Today[0]);
		if(StringUtils.isBlank(s_NewId)) throw new Exception("년도 폴더 생성 실패");
	
		s_NewId = makeDCTMFolder(idf_Sess, "/EDMS/" + sa_Today[0], sa_Today[1]);
		if(StringUtils.isBlank(s_NewId)) throw new Exception("월 폴더 생성 실패");
		
		s_NewId = makeDCTMFolder(idf_Sess, "/EDMS/" + sa_Today[0] + "/" + sa_Today[1], sa_Today[2]);
		if(StringUtils.isBlank(s_NewId)) throw new Exception("일 폴더 생성 실패");
		
		return s_NewId;
	}

	/**
	 * DCTM 폴더 생성
	 * @param idf_Sess : IDfSession (documentum session)
	 * @param s_ParentFolderPath : 상위 폴더 경로
	 * @param s_ChildFolderName : 생성 대상 폴더명
	 * @return 성공 : New Folder Object ID (String) <br>
	 *         실패 : "" (String)
	 * @throws Exception
	 */
	public static String makeDCTMFolder(IDfSession idf_Sess, String s_ParentFolderPath, String s_ChildFolderName) throws Exception
	{
		IDfFolder idf_NewFolder	= null;
		String s_ChildID 		= "";
		String s_ParentID 		= "";
		
		s_ChildID = idf_Sess.getIdByQualification("dm_folder where any r_folder_path='" + s_ParentFolderPath + "/" + s_ChildFolderName +"'").toString();
		
		if(!DfId.isObjectId(s_ChildID))
		{
			s_ParentID = idf_Sess.getIdByQualification("dm_folder where any r_folder_path='" + s_ParentFolderPath + "'").toString();
			
			if (DfId.isObjectId(s_ParentID))
			{
				s_ChildFolderName = checkNull(s_ChildFolderName);
				
				s_ChildFolderName = s_ChildFolderName.replaceAll("/", "-");

				idf_NewFolder = (IDfFolder)idf_Sess.newObject("dm_folder");
				idf_NewFolder.setObjectName(s_ChildFolderName);
				idf_NewFolder.link(s_ParentID);
				idf_NewFolder.setOwnerName(idf_Sess.getDocbaseOwnerName()); //2022.01.12 임이사님
				idf_NewFolder.setACLDomain("dm_dbo");
				idf_NewFolder.setACLName("all_write");
				idf_NewFolder.save();
				
				s_ChildID = idf_NewFolder.getObjectId().toString();
			}
		}
		
		if(!DfId.isObjectId(s_ChildID))
		{
			s_ChildID = "";
		}

		return s_ChildID;
	}
	
	/**
	 * 파일명을 받아서 파일 확장자 반환
	 * @param s_FileName : 파일명
	 * @return 성공 : 파일 확장자 (String) <br>
	 *         실패 : "" (String)
	 * @throws Exception
	 */
	public static String getFileExtByFileName(String s_FileName) throws Exception
	{
		String s_Rtn = "";
		int i_Pos = 0;

		i_Pos = s_FileName.lastIndexOf(".");
		s_Rtn = (i_Pos > 0) ? s_FileName.substring(i_Pos + 1) : "";

		return s_Rtn;
	}

	/**
	 * 파일 확장자를 받아서 오브젝트 포멧명 반환
	 * @param idf_Sess : IDfSession (documentum session)
	 * @param s_FileExt : 파일확장자
	 * @return 성공 : DCTM Format Name (String)
	 *    <br> 실패 : "unknown" (String)
	 * @throws Exception
	 */
	@SuppressWarnings("deprecation") 
	public static String getFormatByFileExt(IDfSession idf_Sess, String s_FileExt) throws DfException
	{	
		String s_ObjID = "";
		String s_Format = "";

		switch (s_FileExt)
		{
			case "xls":  s_Format = "excel8book"   ; break;
			case "xlsx": s_Format = "excel12book"  ; break;
			case "doc":  s_Format = "msw8"		   ; break;
			case "docx": s_Format = "msw12"		   ; break;
			case "ppt":  s_Format = "ppt8"		   ; break;
			case "pptx": s_Format = "ppt12"		   ; break;
			case "txt":  s_Format = "crtext"	   ; break;
			case "htm":  s_Format = "html"		   ; break;
			case "html": s_Format = "html"		   ; break;
			case "zip":  s_Format = "zip"		   ; break;
			case "fm":   s_Format = "mdoc55"	   ; break;
			case "indd": s_Format = "indesign7"	   ; break;
			case "vsd":  s_Format = "vsd"		   ; break;
			case "ai":   s_Format = "illustrator11"; break;
			case "psd":  s_Format = "photoshop8"   ; break;
			case "mdb":  s_Format = "ms_access8"   ; break;
			case "mp4":  s_Format = "mpeg-4v"	   ; break;
			case "au":   s_Format = "audio"		   ; break;
			case "jpg":  s_Format = "jpeg"		   ; break;
			case "msg":  s_Format = "msg"		   ; break;
		}

		if (s_Format.equals(""))
		{
			//결과가 여러개일 경우, 오류 없이 그 중에 하나만 리턴 됨
			s_ObjID = idf_Sess.apiGet("id", "dm_format where dos_extension = '" + s_FileExt + "'");
			if (s_ObjID != null)
				s_Format = idf_Sess.apiGet("get", s_ObjID + ",name");
		}

		return (s_Format.equals("") ?  "unknown" : s_Format);
	}

	/**
	 * DQL 쿼리를 받아 쿼리 수행 결과를 IDfCollection 타입으로 반환
	 * @param idf_Sess : IDfSession (documentum session)
	 * @param s_Dql : DQL 쿼리
	 * @param i_QueryType : DQL Type
	 * @return 성공 : 쿼리 결과 (IDfCollection)
	 *    <br> 실패 : NULL
	 * @throws Exception
	 */
	public static IDfCollection getCollectionByDQL(IDfSession idf_Sess, String s_Dql, int i_QueryType) throws Exception
	{
		IDfQuery idf_Query	  = new DfQuery();
		IDfCollection idf_Col = null;

		idf_Query.setDQL(s_Dql);
		idf_Col = idf_Query.execute(idf_Sess, i_QueryType);
		
		return idf_Col;
	}

	/**
	 * Count DQL 쿼리를 받아 int 타입으로 Count 값을 반환<br>
	 * s_Dql 은 'dm_user where ....' 처럼 from 다음 부터 구성
	 * @param idf_Sess : IDfSession (documentum session)
	 * @param s_Dql : DQL 쿼리
	 * @return 성공 : 결과 건수 (int)
	 *    <br> 실패 : -1
	 * @throws Exception
	 */
	public static int getCountByDQL(IDfSession idf_Sess, String s_Dql) throws Exception
	{
		IDfCollection idf_Col = null;
		int i_Rtn = -1;
		
		s_Dql = "select count(*) as cnt from " + s_Dql;
		idf_Col = getCollectionByDQL(idf_Sess, s_Dql, IDfQuery.DF_READ_QUERY);
		
		if (idf_Col != null && idf_Col.next())
		{
			i_Rtn = idf_Col.getInt("cnt");
			idf_Col.close();
		}
		
		return i_Rtn;
	}
	
	/**
	 * 공통코드에서 Code에 대한 Value 리턴.<p>
	 * 코드 구분값과 반환할 속성명을 입력 받아 해당하는값을 리턴한다. 
	 * 
	 * @param  	idf_Sess - Session
	 * @param	ps_codeType - 코드 구분
	 * @param	ps_attrName - 반환할  attribute 명	
	 * 
	 * @return
	 * @throws Exception
	 */
	public static String getCodeValue(IDfSession idf_Sess,String ps_codeType, String ps_attrName) throws Exception
	{
		
		String 			s_rtnVal 	= "";
		String 			s_Dql 		= "";
		IDfQuery 		idf_Qry 	= null;
		IDfCollection 	idf_Col 	= null;
		
		s_Dql = "SELECT " + ps_attrName + " FROM edms_code " +
				"WHERE u_code_type='" + ps_codeType + "'";
		
		idf_Qry = new DfQuery();
		idf_Qry.setDQL(s_Dql);
		
		idf_Col = idf_Qry.execute(idf_Sess,DfQuery.QUERY);
		
		while(idf_Col!=null && idf_Col.next()){
			s_rtnVal = idf_Col.getString(ps_attrName);
		}
		if(idf_Col!=null)
		{
			idf_Col.close();
		}
		
		return s_rtnVal;
	}
	
	/**
	 * 오브젝트 존재 여부 판단/최신문서 확인.<p>
	 * ※pb_isChkCurrVersion값은 필히 용도에 맞게 사용 하십시요. 
	 * <br>1. pb_isChkCurrVersion값은 문서가 아닌 경우 모두 fasle로 입력합니다.
	 * <br>2. pb_isChkCurrVersion값은 문서이며 최신버전 여부 체크시에만 true로 입력 합니다. 
	 * <br>3. 반환 값이 false일 경우 오브젝트가 존재 하지 않는 경우 입니다.
	 * <br>4. pb_isChkCurrVersion값이 true에서 반환 값이 false인 경우 '문서가 삭제 되었거나 , 최신버전이 아닙니다.' 경우 입니다.(메세지처리 유의)
	 * 
	 * @param   idf_Sess - Session
	 * @param   ps_ObjId - 오브젝트 ID
	 * @param   pb_isChkCurrVersion -문서 최신버전 체크 여부
	 * 
	 * @return  b_rtn - true/false 
	 *
	 * @throws DfException
	 * 
	 * @author  
	 * @since 
	 * @version $Id: DctmCommonUtil.java,v 1.0 
	 * @since   2.0
	 */
	public static boolean isValidObjectByID(IDfSession idf_Sess, String ps_ObjId, boolean pb_isChkCurrVersion) 
	{
		
		boolean b_rtn = false;
		
		IDfPersistentObject idf_pObj = null;
		
		try{
			//오브젝트 존재 확인
			idf_pObj = idf_Sess.getObject(new DfId(ps_ObjId));
			if(pb_isChkCurrVersion){
				//최신버전 체크 일 경우
				b_rtn = idf_pObj.getBoolean("i_has_folder");
			}else{
				b_rtn = true;
			}
		}catch(DfException Dfe){
			//오브젝트가 없을 시 오류가 난다.
			b_rtn = false;
		}finally{
			
		}
		return b_rtn;
	}
	
	/**
	 * 최신버전 문서의 ObjectID를 읽어 온다.
	 * 
	 * @param   idf_Sess - Session
	 * @param   ps_ObjId - 오브젝트 ID
	 * 
	 * @return  s_ObjId
	 *
	 * @throws DfException
	 * 
	 * @author  
	 * @since 
	 * @version $Id: DctmCommonUtil.java,v 1.0 
	 * @since   2.0
	 */
	public static String getCurrentObjectID(IDfSession idf_Sess, String ps_ObjId) 
	{
		
		String s_ObjId = "";
		String s_ChronId = "";
		
		IDfPersistentObject idf_pObj = null;
		IDfPersistentObject idf_pCurObj = null;
		
		try{
			//오브젝트 존재 확인
			idf_pObj = idf_Sess.getObject(new DfId(ps_ObjId));
			if(idf_pObj != null){
				
				s_ChronId = idf_pObj.getString("i_chronicle_id");
				idf_pCurObj = idf_Sess.getObjectByQualification("dm_document where i_chronicle_id='" + s_ChronId + "'" );
				if(idf_pCurObj != null) s_ObjId = idf_pCurObj.getString("r_object_id");
			}
		}catch(DfException Dfe){
			//오브젝트가 없을 시 오류가 난다.
			
		}finally{
			
		}
		
		return s_ObjId;
	}
	
	// COMMON 추가
	/**
     * 현재 시간을  hh 구분자 mi구분자 ss 전달받은 구분자로  String  반환.<p>  
     * 
     * @param ps_Delimeter - 구분자
     * 
     * @Return s_rtnVal - 현재 시간(hh ps_Delimeter mi ps_Delimeter ss)
     * 
     * @throws Exception
     * 
     * @author  
     * @since 
     * @version $Id: CommonUtil.java,v 1.0 
     * @since   1.0 
     */ 
    public static String getCurrentTime(String ps_Delimeter) throws Exception {
        
        String s_rtnVal = "";
        
        Calendar c_today  = Calendar.getInstance();
        DecimalFormat df = new DecimalFormat("00");
        
        s_rtnVal = df.format(c_today.get(Calendar.HOUR_OF_DAY)) + ps_Delimeter +
                    df.format(c_today.get(Calendar.MINUTE)) + ps_Delimeter +
                    df.format(c_today.get(Calendar.SECOND));
                    
        return s_rtnVal;
    }
    
    /**
     * 현재 날짜와 시간을 yyyy-mm-dd hh:mi:ss형식으로 String  반환.<p>  
     * 
     * @param isTimeRtn - 시간 반환 여부(true/false - 날짜만 또는 날짜 +시간)
     * 
     * @Return s_rtnVal - 현재 날짜와 시간(yyyy-mm-dd hh:mi:ss)
     * 
     * @throws Exception
     * 
     * @author  
     * @since 
     * @version $Id: CommonUtil.java,v 1.0 
     * @since   1.0 
     */ 
    public static String getCurrentDateTime(boolean isTimeRtn) throws Exception {
        
        String s_rtnVal = "";
        
        Calendar c_today  = Calendar.getInstance();
        DecimalFormat df = new DecimalFormat("00");
        
        s_rtnVal = Integer.toString(c_today.get(Calendar.YEAR)) + "-" + 
                    df.format(c_today.get(Calendar.MONTH)+1) + "-" + 
                    df.format(c_today.get(Calendar.DAY_OF_MONTH));
        
        //시간 정보 추가
        if(isTimeRtn)
            s_rtnVal += " " + 
                    df.format(c_today.get(Calendar.HOUR_OF_DAY)) + ":" +
                    df.format(c_today.get(Calendar.MINUTE)) + ":" +
                    df.format(c_today.get(Calendar.SECOND));
                    
        return s_rtnVal;
    }
    
    /**
	 * 맵에서 받은 데이터가 null일 경우 "" 반환.<p>   
	 * 
	 * @param po_obj - 맵에서 꺼낸 오브젝트
	 * 
	 * @Return s_rtnVal - 
	 * 
	 * @throws Exception
	 * 
	 * @author  
	 * @since 
	 * @version $Id: CommonUtil.java,v 1.0 
	 * @since   1.0 
	 */ 
	public static String checkNullStringByObj(Object po_obj) throws Exception {
		
		String s_rtnVal = "";
		
		if(po_obj==null || po_obj.equals("null"))
			s_rtnVal = "";
		else{
			s_rtnVal = po_obj.toString();
			if(s_rtnVal.equals("undefined")){
				s_rtnVal = "";
			}
		}
		
		return s_rtnVal;
	}
	
	/**
     * WAS File을 삭제 한다.<p>
     * 
     * @param   ps_FullPath     - 설정 패스(RealPath)
     * 
     * @return void  
     * 
     * @author  
     * @since 
     * @version $Id: CommonUtil.java,v 1.0 
     * @since   1.0
     *
     */
    public static void deleteWASContent(String ps_FullPath) throws Exception{
        
        try{
            //파일 격체 생성
            File  f_DownloadPath = new File(ps_FullPath);
            if(f_DownloadPath.exists()){
                f_DownloadPath.delete();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    /**
	 * 모든 버전의 문서의 권한을 최신문서의 권한으로 세팅한다.<p>
	 * <br>모든 버전의 문서의 권한을 최신문서의 권한으로 세팅한다. 
	 * 
	 * @param  ps_ObjectID	- 문서 r_object_id
	 *         
	 * @throws Exception 
	 * @author  
	 * @since 
	 * @version  
	 * @since   1.0 DctmCommonUtil.java
	 *
	 */
	public static void setAllDocACL(IDfSession idf_Sess , String ps_ObjectID) throws Exception{
		
		IDfSysObject 	idf_sObj	= null;
		IDfSysObject	idf_CurrObj	= null;
		
		IDfCollection   idf_Col 	= null;
		IDfQuery        idf_Qry 	= new DfQuery();
		
		String s_Dql = "";
		
		String s_AclDomain		= "";
		String s_AClName		= "";
		String s_DocID			= "";
		
		try
		{
			
			//전달받은 객체의 최신문서의 정보를 읽어 온다.
			idf_sObj 	= (IDfSysObject)idf_Sess.getObject(new DfId(ps_ObjectID));
			s_DocID		= idf_sObj.getString("u_doc_key");
			
			if(idf_sObj.getBoolean("i_has_folder"))
			{
				s_AclDomain	= idf_sObj.getACLDomain();
				s_AClName	= idf_sObj.getACLName();
			}
			else
			{
				
				//최신 버전의 문서를 읽어 온다.
				idf_CurrObj = (IDfSysObject)idf_Sess.getObjectByQualification("edms_doc WHERE u_doc_key='" + s_DocID + "'");
				s_AclDomain	= idf_CurrObj.getACLDomain();
				s_AClName	= idf_CurrObj.getACLName();
				ps_ObjectID	= idf_CurrObj.getString("r_object_id");
				
			}
			
			//모든 문서의 정보를 업데이트 한다.
			s_Dql = "UPDATE edms_doc (ALL) OBJECTS SET acl_domain='" + s_AclDomain + "', SET acl_name='" + s_AClName + "' " +
					"WHERE u_doc_key='" + s_DocID + "' AND r_object_id <> '" + ps_ObjectID + "'";
			
			idf_Qry.setDQL(s_Dql);
			idf_Col = idf_Qry.execute(idf_Sess, DfQuery.QUERY);
			
			if (idf_Col!=null && idf_Col.next()) 
				idf_Col.close();
			
		} 
		catch (DfException e)
		{
			// throw new EdmsException(e.getMessage(), e.getMessageId());
			throw e;
		}
		finally
		{
			try {
				if ( idf_Col != null ) idf_Col.close(); 
			} catch(Exception e){}
		}
	}
	
	/**
	 * 
	 * 반출함첨부시 완료처리 ( 메일발송후 삭제일경우 완료처리 )
	 * 
	 * @param idfSession
	 * @param r_object_id		- 첨부문서
	 * @param sViewApproveId	- 반출함 결재 [edms_req_takeout] R_OBJECT_ID
	 * @throws Exception
	 */
	public static void updateTakeOutAttachLimitDate(IDfSession idfSession, String r_object_id, String sViewApproveId) throws Exception{

		// 모두 완료 되었는지 체크 필요
		String			s_Dql 		= "";
		String 			s_Rtn 		= "";
		boolean 		b_Rtn		= false;
		IDfCollection	idf_Col		= null;
		IDfQuery 		idf_Query	= new DfQuery();
		
		//=======================================================================
		// [1]. 반출함이 메일발송후 삭제일경우 처리
		// [2]. 반출함 결재에 포함된 각각 모든 문서별 flag 값 변경
		// [3]. 반출함 결재에 포함된 모든 문서가 첨부될경우 반출함 테이블 정보 완료처리 ( limit_date 수정)
		//
		//	** 모든 파일이 반출시에만 최종 limit_date update 함
		// 		반출시 반출문서 [edms_req_takeout_doc] 에 반출완료 처리
		// 		반출시 반출문서 모두 첨부 완료 시 [edms_req_takeout] 에 u_limit_date 변경 처리
		//=======================================================================
		try
		{	
			 
			// 메일발송후 삭제 여부 체크
			IDfPersistentObject idf_TakeOutObj = (IDfPersistentObject) idfSession.getObject(new DfId(sViewApproveId));
			String sLimitFlag = idf_TakeOutObj.getString("u_limit_flag");
			if(StringUtils.isNotBlank(sLimitFlag) && sLimitFlag.equals("M"))
			{
				
				IDfPersistentObject idf_TakeOutDocObj = idfSession.getObjectByQualification("edms_req_takeout_doc where u_req_id='"+ sViewApproveId+"' and u_req_doc_id='" + r_object_id +"'");
				if(idf_TakeOutDocObj != null) 
				{
					idf_TakeOutDocObj.setString("u_status", "E");
					idf_TakeOutDocObj.save();							 
				}
				
	    		s_Dql = "SELECT count(*) as cnt " +
	    				" FROM edms_req_takeout_doc " +
	    				" WHERE u_req_id='" + sViewApproveId + "' " +
	    				" AND u_status is nullstring  ";
			
	    		idf_Query.setDQL(s_Dql);
	    		idf_Col = idf_Query.execute(idfSession, DfQuery.DF_READ_QUERY);
	    		if(idf_Col != null && idf_Col.next())
	    		{
	    			s_Rtn = idf_Col.getString("cnt");
	    		}
	    		
	    		if(idf_Col != null)
	    		{
	    			idf_Col.close();
	    		}
			    
	    		if (s_Rtn.equals("0")) b_Rtn = true;

			}
    		
    		// 모든 파일이 첨부및 취소가 되었으면 
    		if(b_Rtn)
    		{
				idf_TakeOutObj.setTime("u_limit_date"	, new DfTime());
				idf_TakeOutObj.save();					 
    		}
		 
		} 
		catch (Exception e) 
		{
			if (idf_Col != null) try{ idf_Col.close();} catch(Exception e1){}
		}
		finally {
			if (idf_Col != null) try{ idf_Col.close();} catch(Exception e1){}
		}
		
	}
	
}
