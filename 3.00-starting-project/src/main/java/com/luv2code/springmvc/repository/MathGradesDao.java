package com.luv2code.springmvc.repository;

import com.luv2code.springmvc.models.MathGrade;
import org.springframework.data.repository.CrudRepository;

public interface MathGradesDao extends CrudRepository<MathGrade, Integer> {

    Iterable<MathGrade> findGradeByStudentId(Integer id);

    void deleteByStudentId(Integer id);
}
