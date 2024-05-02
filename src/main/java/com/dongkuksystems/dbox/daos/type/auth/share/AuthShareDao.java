package com.dongkuksystems.dbox.daos.type.auth.share;

import java.util.List;



import com.dongkuksystems.dbox.models.type.auth.AuthShare;

public interface AuthShareDao {
  public List<AuthShare> selectList(String objectId);
  public List<AuthShare> selectDetailList(String objectId);
}
