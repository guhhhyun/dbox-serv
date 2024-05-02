package com.dongkuksystems.dbox.models.dto.type.auth;

import com.documentum.fc.client.IDfSession;
import com.dongkuksystems.dbox.constants.GrantedLevels;
import com.dongkuksystems.dbox.models.table.etc.VUser;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CheckAuthResult {
  private boolean isAuthenticated;
  private String bannedReason;
}
