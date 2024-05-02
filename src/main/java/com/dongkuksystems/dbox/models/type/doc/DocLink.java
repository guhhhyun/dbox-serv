package com.dongkuksystems.dbox.models.type.doc;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfEnumeration;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;
import com.dongkuksystems.dbox.constants.GrantedLevels;
import com.dongkuksystems.dbox.models.dto.type.agree.WfDocDto;
import com.dongkuksystems.dbox.models.dto.type.docbox.project.ProjectCreateDto;
import com.dongkuksystems.dbox.models.dto.type.upload.AttachedFile;
import com.dongkuksystems.dbox.models.dto.type.upload.UploadDocDto;
import com.dongkuksystems.dbox.utils.dctm.DCTMUtils;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**edms_wf_doclist 대신 링크파일용으로 신규 생성(edms_doc_link*/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder 
public class DocLink {
  @ApiModelProperty(value = "Object ID")
  private String rObjectId;
  @ApiModelProperty(value = "문서 ID")
  private String uDocId;
  @ApiModelProperty(value = "문서 키")
  private String uDocKey;
  @ApiModelProperty(value = "문서함 코드")
  private String uCabinetCode;
  @ApiModelProperty(value = "폴더 ID")
  private String uFolId;
  @ApiModelProperty(value = "링크종류")
  private String uLinkType;
  @ApiModelProperty(value = "생성자")
  private String uCreateUser;
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "생성 일시")
  private LocalDateTime uCreateDate;
  @ApiModelProperty(value = "수정자")
  private String uModifyUser;
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "수정 일시")
  private LocalDateTime uModifyDate;
  
  private static String docTypeNm="edms_doc";
  private static String linkTypeNm="edms_doc_link";
		  
  public static IDfPersistentObject createDocLink(IDfSession idfSession, WfDocDto dto, String preUFilId, String uLinkType )
			throws Exception {

		IDfPersistentObject idf_PObj = null;
		
		String s_doc_key = dto.getUDocKey();
		String s_ObjId = idfSession.getIdByQualification( linkTypeNm+" where u_cabinet_code='"+ dto.getUCabinetCode()+"' and u_doc_key='" + s_doc_key +"'").toString();
		
		if(!DfId.isObjectId(s_ObjId)) //신규생성건
		{
			IDfDocument idfDocObj = (IDfDocument)idfSession.getObject(new DfId(s_doc_key));  //문서Key라야 함(s_ObjId는 edms_doc_link의 r_object_id라 사용하면 안됨

			idf_PObj = (IDfPersistentObject) idfSession.newObject( linkTypeNm );
			idf_PObj.setString("u_doc_id",           idfDocObj.getString("r_object_id")      ); //r_object_id, document.getChronicleId()
			idf_PObj.setString("u_doc_key",          dto.getUDocKey()     ); //문서번호
			idf_PObj.setString("u_cabinet_code",     dto.getUCabinetCode() ); //문서함코드
			
		    idf_PObj.setString("u_fol_id",           preUFilId             ); //변경전 폴더 id, saveWfList이후에  전자결재폴더id로 바꿔서 edms_doc에 저장( link테이블에 있는 u_fol_id가 이전폴더id가 됨 )
		    idf_PObj.setString("u_link_type",        uLinkType             ); //링크종류', SET COMMENT_TEXT='W:결재')
			
			idf_PObj.setString("u_create_user",      dto.getApprovalWriter());
		    idf_PObj.setString("u_create_date",   (new DfTime()).toString());
			
		}		
		
		return idf_PObj;
	}

  
	 public static String removeDocLink(IDfSession idfSession, String cabinetCode, String keyString, String keyGubun, String userId)
				throws Exception {
		String wfDocId="";
		String uFolId= "";
		IDfPersistentObject idf_PObj=null;
		
		if(keyGubun.equals("F")) { //첨부문서 하나 삭제되는 경우
		    wfDocId  = idfSession.getIdByQualification( linkTypeNm + " where u_cabinet_code='"+ cabinetCode+"' and u_doc_key='" + keyString +"'").toString();
		    if(DfId.isObjectId(wfDocId)) {
		        idf_PObj = (IDfPersistentObject)idfSession.getObject(new DfId(wfDocId));
		        uFolId = idf_PObj.getString("u_fol_id");//폴더아이디가 edms_doc의 u_fol_id로 업데이트 해주게 된다
				
		        IDfPersistentObject idf_LinkDel = idfSession.newObject("edms_doc_link_del");
				idf_LinkDel.setString("u_doc_id",           idf_PObj.getString("u_doc_id")  ); //r_object_id, document.getChronicleId()
				idf_LinkDel.setString("u_doc_key",          idf_PObj.getString("u_doc_key")  ); //문서번호
				idf_LinkDel.setString("u_cabinet_code",     idf_PObj.getString("u_cabinet_code")); //문서함코드
				idf_LinkDel.setString("u_fol_id",           idf_PObj.getString("u_fol_id")  ); //변경전 폴더 id, saveWfList이후에  전자결재폴더id로 바꿔서 edms_doc에 저장( link테이블에 있는 u_fol_id가 이전폴더id가 됨 )
				idf_LinkDel.setString("u_link_type",        idf_PObj.getString("u_link_type")  ); //링크종류', SET COMMENT_TEXT='W:결재')
				idf_LinkDel.setString("u_create_user",      userId); //삭제 작업자
				idf_LinkDel.setString("u_create_date",   (new DfTime()).toString());
				idf_LinkDel.save();
		        
			    idf_PObj.destroy();
		    }else {
		    	System.out.println("삭제할게 없음 :" + keyString);
		    }
		}else {  // 전자결재 회수시 사용
			IDfEnumeration wfDocIdem = null;
			//1. u_wf_key로 edms_doc에서 u_doc_key를 찾는다(doctypeNm = "edms_doc")
			wfDocIdem=idfSession.getObjectsByQuery("select  r_object_id, i_vstamp,i_is_replica, '"+ docTypeNm +"' as r_object_type from "+ docTypeNm +" where u_cabinet_code='"+ cabinetCode+"' and u_wf_key = '" + keyString +"'", null) ; //해당하는 문서 리스트를 찾음
			
			while(wfDocIdem.hasMoreElements()) {  //같은 결재번호를 갖는 u_doc_key가 여러개이면 , 문서 링크를 찾아서 삭제한다
			     idf_PObj = (IDfPersistentObject) wfDocIdem.nextElement();
			     //System.out.print("key : "+ idf_PObj.getObjectId());
			     
			     String rObjectId = idf_PObj.getObjectId().toString();
			     wfDocId  = idfSession.getIdByQualification( linkTypeNm + " where u_cabinet_code='"+ cabinetCode+"' and u_doc_key='" + rObjectId +"'").toString();
				 idf_PObj = (IDfPersistentObject)idfSession.getObject(new DfId(wfDocId));
				 
				 IDfPersistentObject idf_LinkDel = idfSession.newObject("edms_doc_link_del");
				 idf_LinkDel.setString("u_doc_id",           idf_PObj.getString("u_doc_id")  ); //r_object_id, document.getChronicleId()
				 idf_LinkDel.setString("u_doc_key",          idf_PObj.getString("u_doc_key")  ); //문서번호
				 idf_LinkDel.setString("u_cabinet_code",     idf_PObj.getString("u_cabinet_code")); //문서함코드
				 idf_LinkDel.setString("u_fol_id",           idf_PObj.getString("u_fol_id")  ); //변경전 폴더 id, saveWfList이후에  전자결재폴더id로 바꿔서 edms_doc에 저장( link테이블에 있는 u_fol_id가 이전폴더id가 됨 )
				 idf_LinkDel.setString("u_link_type",        idf_PObj.getString("u_link_type")  ); //링크종류', SET COMMENT_TEXT='W:결재')
				 idf_LinkDel.setString("u_create_user",      userId); //삭제 작업자
				 idf_LinkDel.setString("u_create_date",   (new DfTime()).toString());
				 idf_LinkDel.save();
				 
				 idf_PObj.destroy();
			     
			}
		}
		return uFolId;
	 }
}
