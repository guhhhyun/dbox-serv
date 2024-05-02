package com.dongkuksystems.dbox.daos.type.manager.gradePreservation;

import java.util.List;

import com.dongkuksystems.dbox.models.type.manager.gradePreservation.GradePreservation;

public interface GradePreservationDao {
	
	  public List<GradePreservation> selectGradePreservation(String uComCode);


}
