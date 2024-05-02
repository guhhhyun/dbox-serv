package com.dongkuksystems.dbox.daos.type.manager.gradePreservation;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.type.manager.gradePreservation.GradePreservation;

@Primary
@Repository
public class GradePreservationDaoImpl implements GradePreservationDao {
  private GradePreservationMapper gradePreservationMapper;

  public GradePreservationDaoImpl(GradePreservationMapper gradePreservationMapper) {
    this.gradePreservationMapper = gradePreservationMapper;
  }

  @Override
  public List<GradePreservation> selectGradePreservation(String uComCode) {
    return gradePreservationMapper.selectGradePreservation(uComCode);
  }
}