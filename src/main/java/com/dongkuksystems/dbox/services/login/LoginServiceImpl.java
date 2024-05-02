package com.dongkuksystems.dbox.services.login;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Objects;
import java.util.Optional;

import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dongkuksystems.dbox.daos.table.etc.gwaddjob.GwAddJobDao;
import com.dongkuksystems.dbox.daos.table.etc.gwuser.GwUserDao;
import com.dongkuksystems.dbox.errors.NotFoundException;
import com.dongkuksystems.dbox.errors.UnauthorizedException;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.managerconfig.ManagerConfigDto;
import com.dongkuksystems.dbox.models.table.etc.GwUser;
import com.dongkuksystems.dbox.models.table.etc.VUser;
import com.dongkuksystems.dbox.services.AbstractCommonService;
import com.dongkuksystems.dbox.services.manager.managerconfig.ManagerConfigService;
import com.dongkuksystems.dbox.services.user.UserService;

@Service
public class LoginServiceImpl extends AbstractCommonService implements LoginService {
  private final UserService userSevice;
  private final GwUserDao gwUserDao;
  private final GwAddJobDao gwAddJobDao;
  private final PasswordEncoder passwordEncoder;
  private final ManagerConfigService managerConfigService;

  public LoginServiceImpl(UserService userSevice, GwUserDao gwUserDao, GwAddJobDao gwAddJobDao, PasswordEncoder passwordEncoder, ManagerConfigService managerConfigService) {
    this.userSevice = userSevice;
    this.gwUserDao = gwUserDao;
    this.gwAddJobDao = gwAddJobDao;
    this.passwordEncoder = passwordEncoder;
    this.managerConfigService = managerConfigService;
  }
  
  /**
   * DCTM
   * 
   * @return 
   * @throws Exception
   */
  @Override
  @Transactional
  public UserSession login(String socialPerId, String password) throws Exception {
    checkNotNull(password, "password must be provided.");
    UserSession userSession = new UserSession();
    
    // 개발에서는 
    Environment env = getEnv();
    String[] profiles = env.getActiveProfiles();
    if (profiles != null && profiles.length > 0 && "pro".equals(profiles[0])) {
      boolean logined = gwUserDao.login(socialPerId, password);
      if (!logined)
        throw new UnauthorizedException("로그인 실패" + (" + socialPerId + "));

    } else {
      Optional<VUser> testUser = gwUserDao.selectOneByUserId(socialPerId);
      // 지식자산팀
      if ("UNC50014030".equals(testUser.get().getOrgId())) {
        // 'dmadmin1!'
        if ("7604311C19AFF27A7622F30496146CA24B0C1BBD95F60073DCC883BB0503318D".equals(password)) {
          logger.info("★★★★★★★★★★ 운영서버가 아니므로 모든 계정에 대해 특정pw로 로그인 성공 처리합니다. (계정: " + socialPerId + ") ★★★★★★★★★★");
        } else {
          throw new UnauthorizedException("로그인 실패" + "(" + socialPerId + ")");
        }
      } else if ("kyunghee.kim".equals(socialPerId) || "odong.kwon".equals(socialPerId)
          || "jonghwa2.kim".equals(socialPerId) || "chanjo.moon".equals(socialPerId)
          || "hyoungwoo.kim".equals(socialPerId) || "minyong.ha".equals(socialPerId)
          || "jincheol.koo".equals(socialPerId) || "sanghun21c.lee".equals(socialPerId)
          || "kyungoh.jung".equals(socialPerId) || "DKS50149950".equals(testUser.get().getOrgId())) {
        // 'dbox12!'
        if ("CA575AF5362048957EAD562C29C6B136AB3D51B3DD6528C29BE69171DA8BB479".equals(password)) {
          logger.info("★★★★★★★★★★ 운영서버가 아니므로 모든 계정에 대해 특정pw로 로그인 성공 처리합니다. (계정: " + socialPerId + ") ★★★★★★★★★★");
        } else {
          throw new UnauthorizedException("로그인 실패" + "(" + socialPerId + ")");
        }
      } else {
        // dmadmin2@
        if ("96DEE5E705086AFC362053D0254FFDF7812E2F3814CB6038A9B2CD79EC5A88E8".equals(password)) {
          logger.info("★★★★★★★★★★ 운영서버가 아니므로 모든 계정에 대해 특정pw로 로그인 성공 처리합니다. (계정: " + socialPerId + ") ★★★★★★★★★★");
        } else {
          throw new UnauthorizedException("로그인 실패" + "(" + socialPerId + ")");
        }
      }
    }
    
    VUser user = userSevice.selectOneByUserId(socialPerId).orElseThrow(() -> new NotFoundException(VUser.class, socialPerId));
    user.setAddDepts(gwAddJobDao.selectListByUserId(user.getUserId()));  
    ManagerConfigDto mgr = managerConfigService.selectManagerConfig(socialPerId);
    
    user.setMgr(mgr);
    userSession.setUser(user);
//    userSession.setDeptCabinetCode(deptService.selectDeptByOrgId(Id.of(VDept.class, user.getOrgId())).getUCabinetCode());
    return userSession;
  }
  
  @Override
  public UserSession loginWithoutPassword(String socialPerId) throws Exception {
    UserSession userSession = new UserSession();
    VUser user = getUserBySocialPerId(socialPerId);
    user.setAddDepts(gwAddJobDao.selectListByUserId(user.getUserId()));
    ManagerConfigDto mgr = managerConfigService.selectManagerConfig(socialPerId);

    user.setMgr(mgr);
    userSession.setUser(user);

    return userSession;
  }

  private VUser getUserBySocialPerId(String socialPerId) throws Exception {
    VUser user = userSevice.selectOneByUserId(socialPerId).orElse(null);
    if(Objects.isNull(user)) {
      user = getModelMapper().map(getGwUserBySocialPerId(socialPerId), VUser.class);
      user.setUserId(socialPerId);
    }
    return user;
  }

  private GwUser getGwUserBySocialPerId(String socialPerId) throws Exception {
    return userSevice.selectOtherGwUserOneByUserId(socialPerId)
            .orElseThrow(() -> new NotFoundException(VUser.class, socialPerId));
  }
}
