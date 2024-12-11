package com.luv2code.springmvc.repository;

import com.luv2code.springmvc.models.ScienceGrade;
import org.springframework.data.repository.CrudRepository;

public interface ScienceGradeDao extends CrudRepository<ScienceGrade, Integer> {

    Iterable<ScienceGrade> findGradeByStudentId(Integer id);

    void deleteByStudentId(Integer id);
}
