package com.dongkuksystems.dbox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;
import com.dongkuksystems.dbox.constants.GrantedLevels;
import com.dongkuksystems.dbox.utils.dctm.DCTMUtils;

@ActiveProfiles("pro")
@RunWith(SpringRunner.class)
@SpringBootTest
public class DctmTests {
	public IDfSession iDfSession;

	@Before
	public void before() {
		try {
			iDfSession = DCTMUtils.getAdminSession();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@After
	public void after() {
		if (iDfSession != null && iDfSession.isConnected()) {
			try {
	      if (iDfSession.isTransactionActive()) {
	        iDfSession.abortTrans();
	      }
	      
				iDfSession.disconnect();
			} catch (DfException e) {
				e.printStackTrace();
			}
		}
	}

	@Test
	public void addAuth() {
    try {
      iDfSession.beginTrans();

//      String pjtCode = "p61794";
//      String groupName = "g_d00339";
//      
//      String dql = "SELECT u_doc_key FROM edms_doc WHERE u_pr_code = '" + pjtCode + "'";
//      IDfCollection iDfCol = DCTMUtils.getCollectionByDQL(iDfSession, dql, DfQuery.DF_QUERY);
//      
//      List<String> uDocKeyList = new ArrayList<>();
//      while (iDfCol.next()) {
//        String uDocKey = iDfCol.getString("u_doc_key");
//        System.out.println(uDocKey);
//        
//        uDocKeyList.add(uDocKey);
//      }
      
      List<String> uDocKeyList = Arrays.asList("090315d980009a05",
                                                "090315d980009a29",
                                                "090315d9800099fe",
                                                "090315d980009a0f",
                                                "090315d9800099fc",
                                                "090315d980009a02",
                                                "090315d980009a18",
                                                "090315d980009a20",
                                                "090315d980009a21",
                                                "090315d980009a0c",
                                                "090315d9800099ea",
                                                "090315d980009a0e",
                                                "090315d9800099d5");
      String groupName = "g_d00461";
      
//      modifyDocAcl(uDocKeyList, groupName, GrantedLevels.DELETE.getLevel()); ************************* 사용할 때 주석 해제
      
      iDfSession.commitTrans();
    } catch (Exception e) {
      e.printStackTrace();
    }
	}

	private void saveEdmsCode(String uCodeType, String uTypeName, String uCodeVal1, String uCodeVal2, String uCodeVal3,
			String uCodeName2) {
//		try {
//			IDfPersistentObject idf_PObj = (IDfPersistentObject) iDfSession.newObject("edms_code");
//			idf_PObj.setString("u_code_type", uCodeType);
//			idf_PObj.setString("u_type_name", uTypeName);
//			idf_PObj.setString("u_code_val1", uCodeVal1);
//			idf_PObj.setString("u_code_val2", uCodeVal2);
//			idf_PObj.setString("u_code_val3", uCodeVal3);
//			idf_PObj.setString("u_code_name2", uCodeName2);
//			idf_PObj.save();
//		} catch (DfException e) {
//			e.printStackTrace();
//		}
	}
	
	private void modifyDocAcl(List<String> docKeys, String groupName, int level) {
	  for (String docKey : docKeys) {
	    try {
	      IDfDocument iDfDoc = (IDfDocument) iDfSession.getObject(new DfId(docKey));
        System.out.println("docKey: " + docKey);
        
        // auth_base 추가 (없을 경우에만)
        String dql = "SELECT u_author_id, u_doc_status FROM edms_auth_base WHERE u_obj_id = '" + docKey + "'";
        IDfCollection iDfCol = DCTMUtils.getCollectionByDQL(iDfSession, dql, DfQuery.DF_QUERY);

        boolean hasLiveAuth = false;
        boolean hasClosedAuth = false;
        while (iDfCol.next()) {
          String uAuthorId = iDfCol.getString("u_author_id");
          String uDocStatus = iDfCol.getString("u_doc_status");

          System.out.println("uAuthorId: " + uAuthorId);
          if (Objects.equals(groupName, uAuthorId)) {
            if ("L".equals(uDocStatus)) hasLiveAuth = true;
            else if ("C".equals(uDocStatus)) hasClosedAuth = true;
          }
        }
        
        if (!hasLiveAuth) {
          System.out.println("live: " + groupName);

          iDfDoc.grant(groupName, level, "");
          iDfDoc.grant(groupName + "_sub", level, "");
          
          IDfPersistentObject iDfAuthBase = iDfSession.newObject("edms_auth_base");
          iDfAuthBase.setString("u_obj_id"   , docKey);
          iDfAuthBase.setString("u_obj_type"   , "D");
          iDfAuthBase.setString("u_doc_status" ,  "L");
          iDfAuthBase.setString("u_permit_type"  , "D");
          iDfAuthBase.setString("u_own_dept_yn"  , "Y");
          iDfAuthBase.setString("u_author_id"  ,  groupName);
          iDfAuthBase.setString("u_author_type"  ,  "D");
          iDfAuthBase.setString("u_create_user"  , "jongkil.ahn");
          iDfAuthBase.setTime  ("u_create_date"  , new DfTime());
          iDfAuthBase.setString("u_add_gubun"  , "G");
          iDfAuthBase.save();
        }
        
        if (!hasClosedAuth) {
          System.out.println("closed: " + groupName);

          iDfDoc.grant(groupName, GrantedLevels.READ.getLevel(), "");
          iDfDoc.grant(groupName + "_sub", GrantedLevels.READ.getLevel(), "");
          
          IDfPersistentObject iDfAuthBase = iDfSession.newObject("edms_auth_base");
          iDfAuthBase.setString("u_obj_id"   , docKey);
          iDfAuthBase.setString("u_obj_type"   , "D");
          iDfAuthBase.setString("u_doc_status" ,  "C");
          iDfAuthBase.setString("u_permit_type"  , "R");
          iDfAuthBase.setString("u_own_dept_yn"  , "Y");
          iDfAuthBase.setString("u_author_id"  ,  groupName);
          iDfAuthBase.setString("u_author_type"  ,  "D"); 
          iDfAuthBase.setString("u_create_user"  , "jongkil.ahn");
          iDfAuthBase.setTime  ("u_create_date"  , new DfTime());
          iDfAuthBase.setString("u_add_gubun"  , "G");
          iDfAuthBase.save();
        }
        iDfDoc.save();
	    } catch (Exception e) {
	      e.printStackTrace();
	    }
	  }
	}
}
