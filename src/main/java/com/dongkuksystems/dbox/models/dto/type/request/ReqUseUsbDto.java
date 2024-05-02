package com.dongkuksystems.dbox.models.dto.type.request;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;
import com.documentum.fc.common.IDfTime;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.type.request.ReqUseUsb;

import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

@Data
@Builder
public class ReqUseUsbDto {
	 public static IDfPersistentObject CreateReqUseUsb(IDfSession idfSession, ReqUseUsb dto) throws Exception {
		IDfPersistentObject idf_PObj = (IDfPersistentObject) idfSession.newObject("edms_req_use_usb");

		idf_PObj.setString ("u_req_user_id"  , idfSession.getLoginUserName());
		idf_PObj.setString ("u_regist_user"  , idfSession.getLoginUserName());
		idf_PObj.setString ("u_req_dept_code", dto.getUReqDeptCode ());
		idf_PObj.setString ("u_req_date"     , (new DfTime()).toString());
		idf_PObj.setString ("u_req_status"   , "P"); // 'P':승인요청,'A':승인,'R':반려 << 승인시 입력됨
		idf_PObj.setBoolean("u_allow_usb"    , dto.isUAllowUsb     ());
		idf_PObj.setBoolean("u_allow_cd"     , dto.isUAllowCd      ());
		idf_PObj.setInt    ("u_use_time"     , dto.getUUseTime     ());
		idf_PObj.setString ("u_req_reason"   , dto.getUReqReason   ());
//		idf_PObj.setString ("u_expired_date" , dto.getUExpiredDate ());
		idf_PObj.setString ("u_approver"     , dto.getUApprover    ());
//		idf_PObj.setString ("u_approve_date" , dto.getUApproveDate ());
//		idf_PObj.setString ("u_reject_reason", dto.getURejectReason());

		idf_PObj.save();

		return idf_PObj;

	}
	 
	public static IDfPersistentObject ApproveReqUseUsb(String rObjectId, IDfSession idfSession, ReqUseUsb dto, UserSession userSession)
			throws Exception {		
		String s_ObjId = rObjectId;
		IDfPersistentObject idf_PObj = idfSession.getObject(new DfId(s_ObjId));
		
		// 정책 시간 + 10분 더하기
		String expiredDate = "";
		SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Calendar calExpiredDate = Calendar.getInstance();
		calExpiredDate.add(Calendar.HOUR, dto.getUUseTime());
		calExpiredDate.add(Calendar.MINUTE, 10);
		expiredDate = date.format(calExpiredDate.getTime());
		
		//승인일 (현재시간 -10 분 빼기
        String approveDate = "";
        Calendar calApproveDate = Calendar.getInstance();
        calApproveDate.add(Calendar.MINUTE, -10);
        approveDate = date.format(calApproveDate.getTime()); 
	 
		idf_PObj.setString ("u_req_status"   , "A"); // 'P':승인요청,'A':승인,'R':반려 << 승인시 입력됨
		idf_PObj.setString ("u_allow_usb"   , "1");
		idf_PObj.setString ("u_expired_date" , expiredDate); // 승인일 + 사용시간 << 승인시 입력됨
//		idf_PObj.setString ("u_approver"     , dto.getUApprover    ());
		idf_PObj.setString ("u_approve_date" , approveDate);
//		idf_PObj.setString ("u_reject_reason", dto.getURejectReason());
		idf_PObj.save(); 
		return idf_PObj;
	}
	
	public static IDfPersistentObject RejectReqUseUsb(String rObjectId, IDfSession idfSession, ReqUseUsb dto, UserSession userSession)
			throws Exception {
	
		String s_ObjId = rObjectId;
		IDfPersistentObject idf_PObj = idfSession.getObject(new DfId(s_ObjId));
		idf_PObj.setString ("u_req_status"   , "R"); // 'P':승인요청,'A':승인,'R':반려 << 승인시 입력됨
//		idf_PObj.setString ("u_expired_date" , dto.getUExpiredDate ()); // 승인일 + 사용시간 << 승인시 입력됨
//		idf_PObj.setString ("u_approver"     , dto.getUApprover    ());
		idf_PObj.setString ("u_approve_date" , (new DfTime()).toString());
		idf_PObj.setString ("u_reject_reason", dto.getURejectReason());
		idf_PObj.save(); 
		return idf_PObj;
	}
	
}