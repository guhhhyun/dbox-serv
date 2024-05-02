package com.dongkuksystems.dbox.services.user;


import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dongkuksystems.dbox.daos.table.etc.gwdept.GwDeptDao;
import com.dongkuksystems.dbox.daos.table.etc.gwuser.GwUserDao;
import com.dongkuksystems.dbox.errors.NotFoundException;
import com.dongkuksystems.dbox.models.table.etc.GwUser;
import com.dongkuksystems.dbox.models.table.etc.VDept;
import com.dongkuksystems.dbox.models.table.etc.VUser;
import com.dongkuksystems.dbox.services.AbstractCommonService;
import com.dongkuksystems.dbox.services.code.CodeService;


@Service
public class UserServiceImpl extends AbstractCommonService implements UserService {

  private final CodeService codeService;
  private final GwUserDao userDao;
  private final GwDeptDao deptDao;
  private final PasswordEncoder passwordEncoder;

  public UserServiceImpl(CodeService codeService, GwUserDao userDao, GwDeptDao deptDao, PasswordEncoder passwordEncoder) {
  	this.codeService = codeService;
    this.userDao = userDao;
    this.deptDao = deptDao;
    this.passwordEncoder = passwordEncoder; 
  }

  @Override
  @Transactional
  public VUser login(String userId, String password) throws Exception {
    checkNotNull(password, "password must be provided.");
    VUser user = selectOneByUserId(userId).orElseThrow(() -> new NotFoundException(VUser.class, userId));
    return user;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<VUser> selectOneBySabun(String sabun) throws Exception {
    checkNotNull(sabun, "sabun must be provided.");
    
    Optional<VUser> optUser = userDao.selectOneBySabun(sabun);
    
    // 회사명 설정
    if (optUser.isPresent()) setComOrgNm(optUser.get());
    
    return optUser;
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<VUser> selectOneByUserId(String userId) throws Exception {
    checkNotNull(userId, "UserId must be provided.");
    
    Optional<VUser> optUser = userDao.selectOneByUserId(userId);
    
    if (optUser.isPresent()) {
      // 회사명 설정
      setComOrgNm(optUser.get());
      
      // 회사 캐비닛 코드 설정
      Optional<VDept> optDept = deptDao.selectOneByOrgId(optUser.get().getComOrgId());
      if (optDept.isPresent()) optUser.get().setComCabinetcode(optDept.get().getUCabinetCode());
    }
    
    return optUser;
  }
  
  @Override
  @Transactional(readOnly = true)
  public Optional<GwUser> selectOtherGwUserOneByUserId(String userId) {
    checkNotNull(userId, "UserId must be provided.");
    return userDao.selectOtherGwUserOneByUserId(userId);
  }
  
  @Override
  public List<VUser> selectUserListByUserIds(List<String> userIds) {
  	checkNotNull(userIds, "UserId must be provided.");
    return userDao.selectUserListByUserIds(userIds);
  }
  
  @Override
  public List<VUser> selectGwUserListByUserIds(List<String> userIds) throws Exception {
    checkNotNull(userIds, "UserId must be provided.");
    return userDao.selectGwUserListByUserIds(userIds);
  }
  
  /**
   * 회사명 설정
   */
  private void setComOrgNm(VUser user) throws Exception {
  	Map<String, String> comCodeMap = codeService.getComCodeMap();
  	String comOrgNm = comCodeMap.get(user.getComOrgId());
  	user.setComOrgNm(comOrgNm);
  }

  @Override
  public int updateUserPw(String userId, String oldPw, String newPw) throws Exception {
    int rst = userDao.updateUserPw(userId, oldPw, newPw); 
    return rst;
  }
}