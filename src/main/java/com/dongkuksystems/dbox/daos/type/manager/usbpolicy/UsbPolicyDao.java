package com.dongkuksystems.dbox.daos.type.manager.usbpolicy;

import java.util.List;

import com.dongkuksystems.dbox.models.type.manager.usbpolicy.UsbPolicyType;

public interface UsbPolicyDao {
	
	  public List<UsbPolicyType> selectUsbPolicyComp(String uComCode);

	  public List<UsbPolicyType> selectUsbPolicyDept(String uComCode);
	  
	  public List<UsbPolicyType> selectUsbPolicyUser(String uComCode);
	  
	  public List<UsbPolicyType> selectCheckUsbPolicy(String uTargetId);
	  
	  public List<UsbPolicyType> selectEndDeptList();
	  public List<UsbPolicyType> selectEndUserList();

}
