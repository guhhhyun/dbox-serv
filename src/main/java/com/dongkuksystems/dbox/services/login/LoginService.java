package com.dongkuksystems.dbox.services.login;

import com.dongkuksystems.dbox.models.common.UserSession;

public interface LoginService {
  UserSession login(String socialPerId, String password) throws Exception;
  UserSession loginWithoutPassword(String socialPerId) throws Exception;
}
