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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder 
public class WfDoclist {
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
  @ApiModelProperty(value = "결재정보")
  private String uWfInf;
  @ApiModelProperty(value = "최종결재자")
  private String uWfApprover;
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
  
  
  public static IDfPersistentObject saveWfList(IDfSession idfSession, WfDocDto dto)
			throws Exception {

		IDfPersistentObject idf_PObj = null;
		
		String s_doc_key = dto.getDboxId();
		String s_ObjId = idfSession.getIdByQualification("edms_wf_doclist where u_cabinet_code='"+ dto.getUCabinetCode()+"' and u_doc_key='" + s_doc_key +"'").toString();
		
		if(!DfId.isObjectId(s_ObjId))
		{
			idf_PObj = (IDfPersistentObject) idfSession.newObject("edms_wf_doclist");
		}else {
			idf_PObj = (IDfPersistentObject)idfSession.getObject(new DfId(s_ObjId));
		}		
		idf_PObj.setString("u_doc_id",           dto.getDboxId()        );
		idf_PObj.setString("u_cabinet_code",     dto.getUCabinetCode()  );
		idf_PObj.setString("u_fol_id",           dto.getUFolId()        );
		idf_PObj.setString("u_wf_inf",           dto.getApprovalId()    ); //결재 페이지 Link		
		idf_PObj.setString("u_wf_approver",      dto.getApprovalWriter());
		
		//if( s_ObjId.equals("")  ||  null == s_ObjId ) { //신규생성일 때
			idf_PObj.setString("u_create_user",      dto.getApprovalWriter());
		    idf_PObj.setString("u_create_date",   (new DfTime()).toString());
	    //}
		idf_PObj.setString("u_update_user",      dto.getApprovalWriter());
		idf_PObj.setString("u_update_date",   (new DfTime()).toString());
		
        if(dto.getFileDeleteYn().equals("Y")) {
	         idf_PObj.setString("u_delete_status",  "D");//, SET COMMENT_TEXT='R:요청중, D:삭제(휴지통), E:삭제(보존년한)'),
	     }
		
		return idf_PObj;
	}

  
	 public static String removeWfList(IDfSession idfSession, String cabinetCode, String keyString, String keyGubun)
				throws Exception {
		String wfDocId="";
		IDfPersistentObject idf_PObj=null;
		
		if(keyGubun.equals("F")) {
		    wfDocId  = idfSession.getIdByQualification("edms_wf_doclist where u_cabinet_code='"+ cabinetCode+"' and u_doc_key='" + keyString +"'").toString();
		    //삭제한다. 다만, edms_doc에는 그대로 둔다
		    idf_PObj = (IDfPersistentObject)idfSession.getObject(new DfId(wfDocId));
			idf_PObj.destroy();
		}else {
			IDfEnumeration wfDocIdem = null;
			String docId=idfSession.getIdByQualification("r_object_type from edms_doc where u_cabinet_code='"+ cabinetCode+"' and u_wf_key = '" + keyString +"'").toString() ;  //work flow 문서 id를 찾아서
			wfDocIdem=idfSession.getObjectsByQuery("select  r_object_id, i_vstamp,i_is_replica, 'edms_wf_doclist' as r_object_type from edms_doc where u_cabinet_code='"+ cabinetCode+"' and u_doc_id = '" + docId +"'", null) ; //해당하는 문서 리스트를 찾음 
			while(wfDocIdem.hasMoreElements()) {
			     idf_PObj = (IDfPersistentObject) wfDocIdem.nextElement();
			     System.out.print("key : "+ idf_PObj.getObjectId());
			     
			     String rObjectId = idf_PObj.getObjectId().toString();
			     idf_PObj = (IDfPersistentObject)idfSession.getObject(new DfId(rObjectId));
				 idf_PObj.destroy();
			     
			}
		}
		return "";
	 }
}
