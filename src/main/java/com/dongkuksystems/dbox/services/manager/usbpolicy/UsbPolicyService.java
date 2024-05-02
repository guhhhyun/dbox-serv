package com.dongkuksystems.dbox.services.manager.usbpolicy;

import java.util.List;

import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.type.manager.usbpolicy.UsbPolicyType;



public interface UsbPolicyService {	
	List<UsbPolicyType> selectUsbPolicyComp(String uComCode);
	
	List<UsbPolicyType> selectUsbPolicyDept(String uComCode);
	
	List<UsbPolicyType> selectUsbPolicyUser(String uComCode);

	String patchCompValue(String rObjectId, String uCodeVal2,UserSession userSession) throws Exception;
	
	String postDeptSave(String uDeptCode,String uComCode,UserSession userSession) throws Exception;
	
	String deleteDept(String rObjectId, UserSession userSession) throws Exception;
	
	String postUserSave(String userId,String uComCode,UserSession userSession) throws Exception;
	
	String deleteUser(String rObjectId, UserSession userSession) throws Exception;
	
	String patchUserValue(String rObjectId, String uPolicy,String uStartDate,String uEndDate,UserSession userSession) throws Exception;
	
	String patchDeptValue(String rObjectId, String uPolicy,String uStartDate,String uEndDate,UserSession userSession) throws Exception;
	
}
