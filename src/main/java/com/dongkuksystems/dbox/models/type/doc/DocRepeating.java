package com.dongkuksystems.dbox.models.type.doc;

import java.time.LocalDateTime;

import com.dongkuksystems.dbox.models.table.etc.VUser;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder 
public class DocRepeating {
  private String rObjectId;
  private String uEditor;
  private String rVersionLabel;
  private String uWfSystem;
  private String uWfForm;
  private String uWfTitle;
  private String uWfKey;
  private String uWfApprover;
  private LocalDateTime uWfApprovalDate;

  private VUser approverDetail;
}
