package com.dongkuksystems.dbox.daos.type.manager.usbpolicy;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.type.manager.usbpolicy.UsbPolicyType;

@Primary
@Repository
public class UsbPolicyDaoImpl implements UsbPolicyDao {
  private UsbPolicyMapper usbPolicyMapper;

  public UsbPolicyDaoImpl(UsbPolicyMapper usbPolicyMapper) {
    this.usbPolicyMapper = usbPolicyMapper;
  }

  @Override
  public List<UsbPolicyType> selectUsbPolicyComp(String uComCode) {
    return usbPolicyMapper.selectUsbPolicyComp(uComCode);
  }

  @Override
  public List<UsbPolicyType> selectUsbPolicyDept(String uComCode) {
    return usbPolicyMapper.selectUsbPolicyDept(uComCode);
  }

  @Override
  public List<UsbPolicyType> selectUsbPolicyUser(String uComCode) {
    return usbPolicyMapper.selectUsbPolicyUser(uComCode);
  }

  @Override
  public List<UsbPolicyType> selectCheckUsbPolicy(String uTargetId) {
    return usbPolicyMapper.selectCheckUsbPolicy(uTargetId);
  }

  @Override
  public List<UsbPolicyType> selectEndDeptList() {
    return usbPolicyMapper.selectEndDeptList();
  }

  @Override
  public List<UsbPolicyType> selectEndUserList() {
    return usbPolicyMapper.selectEndUserList();
  }
}
