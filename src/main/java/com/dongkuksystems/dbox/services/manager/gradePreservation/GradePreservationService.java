package com.dongkuksystems.dbox.services.manager.gradePreservation;

import java.util.List;

import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.type.manager.gradePreservation.GradePreservation;


public interface GradePreservationService {	
	List<GradePreservation> selectGradePreservation(String uComCode);

	String patchGradePreservation(String rObjectId, String uLimitValue,String uTeamValue,String uCompValue,String uGroupValue,String uPjtEverFlag,UserSession userSession) throws Exception;
	
	String patchGradeAutoExtend(String rObjectId, String uAutoExtendValue,UserSession userSession) throws Exception;
	
	String patchGradeSaveDept(String rObjectId, String uDeptCodeValue,UserSession userSession) throws Exception;
}
