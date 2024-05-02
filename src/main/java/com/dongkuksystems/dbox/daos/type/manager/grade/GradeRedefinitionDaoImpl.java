package com.dongkuksystems.dbox.daos.type.manager.grade;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.type.manager.graderedefinition.GradeRedefinition;



@Primary
@Repository
public class GradeRedefinitionDaoImpl implements GradeRedefinitionDao {
	private GradeRedefinitionMapper gradeRedefinitionMapper;

	public GradeRedefinitionDaoImpl(GradeRedefinitionMapper gradeRedefinitionMapper) {		
	    this.gradeRedefinitionMapper = gradeRedefinitionMapper;
	  }
	
	@Override
	  public List<GradeRedefinition> selectGradeRedefinition() {		
	    return gradeRedefinitionMapper.selectGradeRedefinition();
	  }

}


