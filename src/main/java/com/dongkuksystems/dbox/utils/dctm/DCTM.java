package com.dongkuksystems.dbox.utils.dctm;
import java.io.File;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.DfACL;
import com.documentum.fc.client.DfAuthenticationException;
import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfACL;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLoginInfo;
import com.documentum.fc.common.DfTime;
import com.documentum.fc.common.IDfLoginInfo;
import com.dongkuksystems.dbox.utils.dctm.DCTMUtils;

public class DCTM
{
	static long l_Time1 = 0L;
	static long l_Time2 = 0L;

//	public static void main(String[] args) throws Exception
//	{
////		IDfTime tt = new DfTime();
////		SysPrint(tt.asString("yyyy-MM-dd HH:mm:ss"));
////		SysPrint(tt.asString("yyyy-MM-dd"));
////		
////		String s_Today = new DfTime().toString();
////		
////		SysPrint("u_login_year  : " +  s_Today);
////		SysPrint("u_login_year  : " +  Integer.parseInt(s_Today.substring(0, 4)));
////		SysPrint("u_login_month : " +  Integer.parseInt(s_Today.substring(5, 7)));
////		SysPrint("u_login_day   : " +  Integer.parseInt(s_Today.substring(8,10)));
////		SysPrint("u_login_hour  : " +  Integer.parseInt(s_Today.substring(11,13)));
//
//		l_Time1 = System.currentTimeMillis (); 
//		
//		IDfSession idf_Sess = null; 
//		
//		try 
//		{
//			IDfClientX idf_ClientX = new DfClientX();
//			
//			IDfClient idf_Client = idf_ClientX.getLocalClient();
//			PrintRunTime("getLocalClient");
//			String s_Docbase = idf_Client.getDocbaseMap().getDocbaseName(0);
//
//			IDfLoginInfo idf_LoginInfo = new DfLoginInfo();
//			idf_LoginInfo.setUser("user1");
//			idf_LoginInfo.setPassword("'");
////			idf_LoginInfo.setUser("dkdoc");
////			idf_LoginInfo.setPassword("DK_idc03");
//			idf_LoginInfo.setDomain(null);
//			
//			idf_Sess = idf_Client.newSession(s_Docbase, idf_LoginInfo);
//			PrintRunTime("newSession");
//
//			SysPrint("연결 " + (idf_Sess.isConnected() ? "성공!" : "실패!")) ;
//				
//			//---------------------------------------------------------
//			//Null Check
////			NullValueCheck(idf_Sess);
//			
//			//※※ SessionManger / Session / Transaction 처리 ※※
////			SessionTransaction();
//			
//			//문서 등록 샘플
////			DocRegist(idf_Sess);
//			
//			//DQL 쿼리
////			DQLQuery(idf_Sess);
//			
//			//Object Instance 생성
////			GetObject(idf_Sess);
//			
//			//문서 외 일반 오브젝트(supertype이 null) 생성 샘플
////			CreateTypeData(idf_Sess);
//			
//			//일반 오브젝트 수정 / 삭제 
////			UpdateObject(idf_Sess);
//			
//			//문서 오브젝트 수정 / 삭제 
////			UpdateDoc(idf_Sess);
//			
//			//Repeating 속성 처리 
////			HandleRepeatingAttr(idf_Sess);
//			
//			//ACL 처리 
////			HandleACL(idf_Sess);
//			
//			//---------------------------------------------------------
//			SysPrint("작업 완료");
//		}
//		catch(Exception e) 
//		{
//			if(e instanceof DfAuthenticationException) SysPrint("Login Failed");
//			e.printStackTrace();
//		}
//		finally
//		{
//			if(idf_Sess != null && idf_Sess.isConnected()) idf_Sess.disconnect();
//			SysPrint("연결 종료!");
//		}
//	}
//	
//	static void NullValueCheck(IDfSession idf_Sess) throws Exception
//	{
//		String objId = "0001e0f380007900";
//		IDfPersistentObject idf_PObj = (IDfPersistentObject) idf_Sess.getObject(new DfId(objId));
//		
//		SysPrint("u1 : " + DCTMUtils.checkNull(idf_PObj.getString("u1")));
//		SysPrint("u2 : " + DCTMUtils.checkNull(idf_PObj.getString("u2")));
//		SysPrint("u3 : " + DCTMUtils.checkNull(idf_PObj.getString("u3")));
//		SysPrint("u4 : " + DCTMUtils.checkNull(idf_PObj.getString("u4")));
//		SysPrint("u5 : " + DCTMUtils.checkNull(idf_PObj.getString("u5")));
//		SysPrint("u6 : " + DCTMUtils.checkNull(idf_PObj.getString("u6")));
//	}
//	
//	static void SessionTransaction(IDfSession idf_Sess) throws Exception
//	{
//		//------------------------------------------------------------
//		// Case1 : 메소드에 DCTM Session 파라미터가 없는 경우
//		//------------------------------------------------------------
//		IDfSessionManager	 idf_SMgr 	= null;
//		IDfSession			 idf_Sess2 	= null;
//
//		try
//		{
//			//웹에서 필수 : 공통 서비스에서 SessionManager 가져오기
//			//idf_SMgr = xxxxService.getDCTMessionManager();
//			if(idf_SMgr == null)
//			{
//				throw new Exception("DCTM SessionManager 가져오기 실패");
//			}
//				
//			//웹에서 필수 : 공통 서비스에서 Session 가져오기
//			//idf_Sess2 = xxxxService.getEDMSession(idf_SMgr);
//			if(idf_Sess2 == null || !idf_Sess2.isConnected())
//			{
//				throw new Exception("DCTM Session 가져오기 실패");
//			}
//			
//			//필요한 경우 DCTM Transaction 시작
//			idf_Sess2.beginTrans();
//			
//			//--------------
//			// 업무 처리...
//			//--------------
//			
//			//DCTM Transaction 활성화 시 commit
//			if(idf_Sess2.isTransactionActive()) idf_Sess2.commitTrans();
//		}
//		catch(Exception e)
//		{
//			//웹에서 필수 : DCTM Transaction 활성화 상태 시 abort
//			if(idf_Sess2!=null && idf_Sess2.isConnected() && idf_Sess2.isTransactionActive())
//			{
//				idf_Sess2.abortTrans();
//			}
//		}
//		finally
//		{
//			//웹에서 필수 : DCTM Session Release
//			if (idf_SMgr != null && idf_Sess2 != null && idf_Sess2.isConnected()) 
//			{
//				idf_SMgr.release(idf_Sess2);
//			}
//		}
//		// Case1 끝
//		
//		//------------------------------------------------------------
//		// Case2 : 메소드에 DCTM Session 파라미터가 전달 된 경우
//		//      => session은 사용 후 별도 처리 필요 없음 호출한데에서 처리 함
//		//      => Transaction은 필요하면 위 case와 동일하게 사용
//		//------------------------------------------------------------
//		try
//		{
//			//웹에서 필수 : 공통 서비스에서 Session 가져오기
//			if(idf_Sess == null || !idf_Sess.isConnected())
//			{
//				throw new Exception("DCTM Session 가져오기 실패");
//			}
//			
//			//필요한 경우 DCTM Transaction 시작
//			idf_Sess.beginTrans();
//			
//			//--------------
//			// 업무 처리...
//			//--------------
//			
//			//DCTM Transaction 활성화 시 commit
//			if(idf_Sess.isTransactionActive()) idf_Sess.commitTrans();
//		}
//		catch(Exception e)
//		{
//			//웹에서 필수 : DCTM Transaction 활성화 상태 시 abort
//			if(idf_Sess != null && idf_Sess.isConnected() && idf_Sess.isTransactionActive())
//			{
//				idf_Sess.abortTrans();
//			}
//		}
//		finally
//		{
//		}
//		// Case2 끝
//		
//	}
//	
//	static void DocRegist(IDfSession idf_Sess) throws Exception
//	{
//		//실제는 로컬에서 WAS로 올라온 파일로 처리
//		File f_File = new File("C:/1.Temp/등록샘플.pptx");
//
//		String s_FileName 		= f_File.getName();
//		String s_DCTMFolderId 	= DCTMUtils.makeEDMFolder(idf_Sess);
//		String s_CabinetCode 	= "d00004";
//		String s_SecLevel 		= "T";
//		
//		//문서 생성 Instance 생성
//		IDfDocument idf_Doc = (IDfDocument)idf_Sess.newObject("edms_doc");
//		
//		//문서명
//		idf_Doc.setObjectName(s_FileName);
//		
//		//포맷
//		idf_Doc.setContentType(DCTMUtils.getFormatByFileExt(idf_Sess, DCTMUtils.getFileExtByFileName(s_FileName)));
//		
//		//소유자 지정 : Docbase Owner로 지정
//		idf_Doc.setOwnerName(idf_Sess.getDocbaseOwnerName());
//		
//		//템플릿 ACL 적용
//		idf_Doc.setACLDomain(idf_Sess.getDocbaseOwnerName());
//		idf_Doc.setACLName("a_" + s_CabinetCode);
//		
//		//추가 권한자 지정 : 기본 권한이나 공유/협업 대상자 등
//		//추가 권한자를 추가하면 ACL명이 'dm_45XXXXXXXXXXXXXX' 형식(Custom ACL)으로 변경 됨 
//		idf_Doc.grant("d00005", 3, "");	//조회/다운로드 권한(부서) : 부서에 해당하는 DCTM 그룹(케비넷 코드)
//		idf_Doc.grant("user1" , 3, "");	//조회/다운로드 권한(개인) : 사용자 ID
//		idf_Doc.grant("d00006", 7, "");	//편집/삭제 권한
//		idf_Doc.grant("user2" , 7, "");	//편집/삭제 권한
//		
//		//파일 set
//		idf_Doc.setFile(f_File.getAbsolutePath());
//
//		//DCTM 폴더(화면에 보이는 부서 폴더 아님)
//		idf_Doc.link(s_DCTMFolderId);
//		
//		//업무 속성 지정
//		idf_Doc.setString("u_cabinet_code"		, s_CabinetCode);				//문서함코드
//		idf_Doc.setString("u_doc_key"			, ""+idf_Doc.getChronicleId());	//문서 키
//		idf_Doc.setString("u_fol_id"			, "0001e0f380000d20");			//부서폴더
//		idf_Doc.setString("u_sec_level"			, s_SecLevel);					//보안등급
//		idf_Doc.setString("u_doc_status"        , "L");
//		
//		//문서 등록 : save 실패시 문서 오브젝트 생성 안됨
//		idf_Doc.save();
//
//	}
//	
//	static void CreateTypeData(IDfSession idf_Sess) throws Exception
//	{
//		IDfPersistentObject idf_PObj = (IDfPersistentObject)idf_Sess.newObject("edms_auth_base");
//		idf_PObj.setString("u_obj_id"		, "0901e0f38001fcfe");
//		idf_PObj.setString("u_obj_type"		, "D");
//		idf_PObj.setString("u_permit_type"	, "R");
//		idf_PObj.setString("u_own_dept_yn"	, "N");
//		idf_PObj.setString("u_author_id"	, "DKS50131234");
//		idf_PObj.setString("u_create_user"	, idf_Sess.getLoginUserName());
//		idf_PObj.setString("u_create_date"	, (new DfTime()).toString());
//		idf_PObj.save();
//		
//	}
//
//	static void DQLQuery(IDfSession idf_Sess) throws Exception
//	{
//		String s_ObjId 			= "";
//		String s_ComCode		= "";
//		String s_DeptCode 		= "";
//		String s_CabinetCode 	= "";
//		String s_Dql 			= "select * from edms_dept where u_up_dept_code = 'DKSM' ";
//		
//		IDfCollection idf_Col = DCTMUtils.getCollectionByDQL(idf_Sess, s_Dql, DfQuery.DF_READ_QUERY);
//		
//		while(idf_Col != null && idf_Col.next())
//		{
//			s_ObjId 		= idf_Col.getString("r_object_id"); 
//			s_ComCode 		= idf_Col.getString("u_com_code"); 
//			s_DeptCode 		= idf_Col.getString("u_dept_code"); 
//			s_CabinetCode 	= idf_Col.getString("u_cabinet_code");
//			
//			//결과 확인
//			SysPrint(s_ObjId + "," + s_ComCode + "," + s_DeptCode + "," + s_CabinetCode);
//		}
//		if(idf_Col != null) idf_Col.close();
//	}		
//
//	static void GetObject(IDfSession idf_Sess) throws Exception
//	{
//		//---------------------------------------------------------------
//		// 오브젝트 Instance 생성 : r_object_id를 알고 있는 경우
//		//---------------------------------------------------------------
//		//일반 오브젝트
//		String s_ObjId = "0001e0f380000d01";
//		IDfPersistentObject idf_PObj = idf_Sess.getObject(new DfId(s_ObjId));
//			
//		//문서 오브젝트
//		String s_DocId = "0901e0f38001fcfe"; //r_object_id
//		IDfDocument idf_Doc = (IDfDocument)idf_Sess.getObject(new DfId(s_DocId));
//		//---------------------------------------------------------------
//		
//		//---------------------------------------------------------------
//		// 오브젝트 Instance 생성 : DQL을 이용하는 경우
//		//---------------------------------------------------------------
//		IDfPersistentObject idf_Dept = (IDfPersistentObject)idf_Sess.getObjectByQualification("edms_dept where u_dept_code = 'DKSM'");
//		if(idf_Dept != null)
//		{
//			idf_Dept.setString("u_dept_chief", "user1");
//			idf_Dept.save();
//		}
//	}
//
//	static void UpdateObject(IDfSession idf_Sess) throws Exception
//	{
//		String s_ObjId = "0001e0f380000d01";
//		
//		IDfPersistentObject idf_PObj = idf_Sess.getObject(new DfId(s_ObjId));
//		
//		//---------------------------------------------------------------
//		// 속성 값 수정
//		//---------------------------------------------------------------
//		idf_PObj.setString("u_up_fol_id"	, "0001e0f380000d8c");
//		idf_PObj.setString("u_modify_user"	, idf_Sess.getLoginUserName());
//		idf_PObj.setString("u_modify_date"	, (new DfTime()).toString());
//		idf_PObj.save();
//		//---------------------------------------------------------------
//		
//		//---------------------------------------------------------------
//		// 오브젝트 삭제
//		//---------------------------------------------------------------
//		idf_PObj.destroy();
//		//---------------------------------------------------------------
//	}
//	
//	static void UpdateDoc(IDfSession idf_Sess) throws Exception
//	{
//		String s_DocId = "0901e0f38001fcfe"; //r_object_id
//		
//		//문서 이이디로 Instance 생성 
//		IDfDocument idf_Doc = (IDfDocument)idf_Sess.getObject(new DfId(s_DocId));
//		
//		//---------------------------------------------------------------
//		// 속성 값 수정
//		//---------------------------------------------------------------
//		idf_Doc.setString("u_fol_id"	, "0001e0f380000d8c");
//		idf_Doc.setString("u_sec_level"	, "C");
//		idf_Doc.save();
//		//---------------------------------------------------------------
//		
//		//---------------------------------------------------------------
//		// 권한 수정 : DocRegist 메소드의 템플릿 ACL, 추가 권한자 지정 부분 참조
//		// => 권한을 수정을 위해 '편집/삭제' 권한 필요
//		//---------------------------------------------------------------
//		
//		//---------------------------------------------------------------
//		// Check-out (편집 시작 할 때)
//		// DCTM Version(5) 권한 이상만 가능
//		//---------------------------------------------------------------
//		if(!idf_Doc.isCheckedOut() && idf_Doc.getPermit() > 4)
//		{
//			idf_Doc.checkout();	//save() 필요없음
//		}
//		//---------------------------------------------------------------
//		
//		//---------------------------------------------------------------
//		// Cancel Check-out (편집 취소 할 때)
//		// 편집을 시작한 사용자 또는 관리자만 가능
//		// 관리자가 작업하는 경우는 superuser로 세션을 만들어 작업 필요
//		//---------------------------------------------------------------
//		if(idf_Doc.isCheckedOut() && idf_Doc.isCheckedOutBy(idf_Sess.getLoginUserName()))
//		{
//			idf_Doc.cancelCheckout();	//save() 필요없음
//		}
//		//---------------------------------------------------------------
//		
//		//---------------------------------------------------------------
//		// Check-in (편집 후 저장 할 때)
//		// getPermit()은 현재 사용자의 해당 문서에 대한 권한을 체크
//		//---------------------------------------------------------------
//		if(idf_Doc.isCheckedOut() && idf_Doc.getPermit() > 4)
//		{
//			//실제는 로컬에서 WAS로 올라온 파일로 처리
//			File f_File = new File("C:/1.Temp/등록샘플.pptx");
//
//			//Check-in 후 잠금(Check-out) 상태를 유지 할 건지 옵션
//			//현장(설정된 부서)에서 '중간 저장'하는 경우 'true' 설정 필요
//			boolean b_KeepLock = false;
//			
//			idf_Doc.setFile(f_File.getAbsolutePath());
//			idf_Doc.checkin(b_KeepLock, "");	//save() 필요없음
//		}
//		//---------------------------------------------------------------
//		
//		//---------------------------------------------------------------
//		// 오브젝트 삭제
//		//---------------------------------------------------------------
//		idf_Doc.destroy();				//해당 버전만 삭제 : 현재(current) 버전이 삭제하면 바로 전 버전이 current 버전이 됨
//		idf_Doc.destroyAllVersions();	//모든 버전 삭제
//		//---------------------------------------------------------------
//	}
//
//	static void HandleRepeatingAttr(IDfSession idf_Sess) throws Exception
//	{
//		String s_ObjId = "0001e0f380007928"; //샘플 : edms_project
//		
//		IDfPersistentObject idf_PObj = idf_Sess.getObject(new DfId(s_ObjId));
//		
//		//---------------------------------------------------------------
//		// Repeating 값 추가 : append
//		// 기존 값이 몇개가 있든 끝에 추가
//		//---------------------------------------------------------------
//		idf_PObj.appendString("u_join_dept_code", "DKSappend");
//		idf_PObj.save();
//		//---------------------------------------------------------------
//		
//		//---------------------------------------------------------------
//		// 현재 Repeating 값 갯수 확인
//		//---------------------------------------------------------------
//		int i_ValCnt = idf_PObj.getValueCount("u_join_dept_code");
//		SysPrint(i_ValCnt);
//		//---------------------------------------------------------------
//		
//		//---------------------------------------------------------------
//		// Repeating 값 중에서 특정 값이 있는지 확인
//		// 확인 값이 없으면 -1 반환
//		//---------------------------------------------------------------
//		int i_ValIdx = idf_PObj.findString("u_join_dept_code", "DKSappend");
//		SysPrint(i_ValIdx);
//		//---------------------------------------------------------------
//
//		//---------------------------------------------------------------
//		// Repeating 값들을 특정 구분자로 묶어서 일괄 조회
//		//---------------------------------------------------------------
//		String s_Vals = idf_PObj.getAllRepeatingStrings("u_join_dept_code", ",");
//		SysPrint(s_Vals);
//		//---------------------------------------------------------------
//		
//		//---------------------------------------------------------------
//		// Repeating 값 추가 : insert
//		// 기존 값의 특정 index에 삽입
//		//---------------------------------------------------------------
//		idf_PObj.insertString("u_join_dept_code", i_ValCnt, "DKSoutbound");	//주어진 index가 범위를 넘어가면 append와 동일
//		idf_PObj.insertString("u_join_dept_code", 1, "DKSinsert");
//		idf_PObj.save();
//		//---------------------------------------------------------------
//		
//	}
//
//	static void HandleACL(IDfSession idf_Sess) throws Exception
//	{
//		//---------------------------------------------------------------
//		// 템플릿 ACL 처리 : 공통 ACL이므로 관리 목적으로 수정하는 경우
//		// => 수정 시 해당 ACL을 사용하는 모든 문서에 적용
//		//---------------------------------------------------------------
//		IDfACL idf_Acl = idf_Sess.getACL(idf_Sess.getDocbaseOwnerName(), "a_d00004");
//		
//		idf_Acl.revoke("user1", null);
//		
//		idf_Acl.grant("user3", DfACL.DF_PERMIT_READ, null);
//		idf_Acl.grant("g_dks_read", DfACL.DF_PERMIT_READ, null);
//		idf_Acl.grant("g_dks_delete", DfACL.DF_PERMIT_DELETE, null);
//		idf_Acl.save();
//		//---------------------------------------------------------------
//		
//		//---------------------------------------------------------------
//		// 특정 문서의 ACL 처리
//		//---------------------------------------------------------------
//		String s_DocId = "0901e0f38001fcfe"; //r_object_id
//		IDfDocument idf_Doc = (IDfDocument)idf_Sess.getObject(new DfId(s_DocId));
//		
//		idf_Acl = idf_Doc.getACL();
//
//		//'dm_'으로 시작하는 ACL은 거의 공통으로 사용되지 않는 경우이므로 이렇게 사용
//		//아래 경우 처럼 문서에 직접해줘도 되지만, 'dm_45xxxxxxxxxxxxxx'가 새로 생기면서 기존 ACL은 garbage가 됨
//		if(idf_Acl.getObjectName().startsWith("dm_"))
//		{
//			idf_Acl.revoke("user1", null);
//			idf_Acl.grant("user3", DfACL.DF_PERMIT_READ, null);
//			idf_Acl.grant("d00005", DfACL.DF_PERMIT_READ, null);
//			idf_Acl.grant("d00006", DfACL.DF_PERMIT_DELETE, null);
//			idf_Acl.save();
//		}
//		//'dm_'으로 시작하지 않는 경우는 거의 템플릿 ACL이므로 이렇게 처리 필요
//		else
//		{
//			idf_Doc.revoke("user1", null);
//			idf_Doc.grant("user3", DfACL.DF_PERMIT_READ, null);
//			idf_Doc.grant("d00005", DfACL.DF_PERMIT_READ, null);
//			idf_Doc.grant("d00006", DfACL.DF_PERMIT_DELETE, null);
//			idf_Doc.save();
//		}
//			
//		//---------------------------------------------------------------
//		
//		
//	}
//   
//	static void PrintRunTime(String s_Job)
//	{
//		l_Time2 = System.currentTimeMillis ();
//		SysPrint("경과(" + s_Job + ") : " + ((l_Time2 - l_Time1) / 1000.0) + "초");
//		l_Time1 = l_Time2;
//	}
//
//	private static void SysPrint(Object obj)
//	{
//		System.out.println(">" + obj + "<");
//	}
}
