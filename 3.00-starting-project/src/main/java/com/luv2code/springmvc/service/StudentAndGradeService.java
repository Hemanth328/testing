package com.luv2code.springmvc.service;

import com.luv2code.springmvc.models.*;
import com.luv2code.springmvc.repository.HistoryGradeDao;
import com.luv2code.springmvc.repository.MathGradesDao;
import com.luv2code.springmvc.repository.ScienceGradeDao;
import com.luv2code.springmvc.repository.StudentDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;


@Service
@Transactional
public class StudentAndGradeService {

    @Autowired
    private StudentDao studentDao;

    @Autowired
    @Qualifier("mathGrades")
    private MathGrade mathGrade;

    @Autowired
    @Qualifier("scienceGrades")
    private ScienceGrade scienceGrade;

    @Autowired
    @Qualifier("historyGrades")
    private HistoryGrade historyGrade;

    @Autowired
    private MathGradesDao mathGradesDao;

    @Autowired
    private ScienceGradeDao scienceGradeDao;

    @Autowired
    private HistoryGradeDao historyGradeDao;

    @Autowired
    private StudentGrades studentGrades;

    public void createStudent(String firstName, String lastName, String email) {
        CollegeStudent student = new CollegeStudent(firstName, lastName, email);
        student.setId(0);
        studentDao.save(student);
    }

    public boolean checkIfStudentIsNull(int id) {

        Optional<CollegeStudent> student = studentDao.findById(id);

        return student.isPresent();
    }

    public void deleteStudent (int id) {

        if(checkIfStudentIsNull(id)) {
            studentDao.deleteById(id);
            mathGradesDao.deleteByStudentId(id);
            scienceGradeDao.deleteByStudentId(id);
            historyGradeDao.deleteByStudentId(id);
        }
    }

    public Iterable<CollegeStudent> getAllStudentsOfGradeBook() {
        return studentDao.findAll();
    }

    public boolean createGrade(double value, int studentId, String gradeType) {

        if(! checkIfStudentIsNull(studentId))
            return false;

        if(value >= 0 && value <= 100) {
            if("maths".equalsIgnoreCase(gradeType)) {
                mathGrade.setId(0);
                mathGrade.setGrade(value);
                mathGrade.setStudentId(studentId);
                mathGradesDao.save(mathGrade);

                return true;
            }

            if ("science".equalsIgnoreCase(gradeType)) {
                scienceGrade.setId(0);
                scienceGrade.setGrade(value);
                scienceGrade.setStudentId(studentId);

                scienceGradeDao.save(scienceGrade);
                return true;
            }

            if ("history".equalsIgnoreCase(gradeType)) {
                historyGrade.setId(0);
                historyGrade.setGrade(value);
                historyGrade.setStudentId(studentId);

                historyGradeDao.save(historyGrade);
                return true;
            }
        }

        return false;
    }

    public int deleteGrade(Integer gradeId, String gradeType) {
        int studentId = 0;

        if(("maths").equalsIgnoreCase(gradeType)) {
            Optional<MathGrade> mathGrades = mathGradesDao.findById(gradeId);
            if(mathGrades.isEmpty())
                return studentId;

            studentId = mathGrades.get().getStudentId();
            mathGradesDao.deleteById(gradeId);
        }

        if(("science").equalsIgnoreCase(gradeType)) {
            Optional<ScienceGrade> scienceGrades = scienceGradeDao.findById(gradeId);
            if(scienceGrades.isEmpty())
                return studentId;

            studentId = scienceGrades.get().getStudentId();
            scienceGradeDao.deleteById(gradeId);
        }

        if(("history").equalsIgnoreCase(gradeType)) {
            Optional<HistoryGrade> historyGrade = historyGradeDao.findById(gradeId);
            if(historyGrade.isEmpty())
                return studentId;

            studentId = historyGrade.get().getStudentId();
            historyGradeDao.deleteById(gradeId);
        }

        return studentId;
    }


    public GradebookCollegeStudent getStudentInformation(int id) {

        Optional<CollegeStudent> student = studentDao.findById(id);

        if(student.isEmpty()) {
            return null;
        }

        Iterable<MathGrade> mathGradeIterable = mathGradesDao.findGradeByStudentId(id);

        System.out.println("Math Grades = "+mathGradeIterable);
        Iterable<ScienceGrade> scienceGradeIterable = scienceGradeDao.findGradeByStudentId(id);

        System.out.println("Science Grades = "+scienceGradeIterable);
        Iterable<HistoryGrade> historyGradeIterable = historyGradeDao.findGradeByStudentId(id);

        System.out.println("History Grades = "+historyGradeIterable);

        List<Grade> mathGradeList = new ArrayList<>();
        mathGradeIterable.forEach(mathGradeList::add);

        List<Grade> scienceGradeList = new ArrayList<>();
        scienceGradeIterable.forEach(scienceGradeList::add);

        List<Grade> historyGradeList = new ArrayList<>();
        historyGradeIterable.forEach(historyGradeList::add);

        studentGrades.setMathGradeResults(mathGradeList);
        studentGrades.setScienceGradeResults(scienceGradeList);
        studentGrades.setHistoryGradeResults(historyGradeList);

        GradebookCollegeStudent gradebookCollegeStudent = new GradebookCollegeStudent(student.get().getId(), student.get().getFirstname(),
                student.get().getLastname(), student.get().getEmailAddress(), studentGrades);

        return gradebookCollegeStudent;
    }

    public void configureStudentInformation(Integer id, Model m) {

        GradebookCollegeStudent collegeStudent = getStudentInformation(id);

        m.addAttribute("student", collegeStudent);
        if(collegeStudent.getStudentGrades().getMathGradeResults().size() > 0) {
            m.addAttribute("mathAverage", collegeStudent.getStudentGrades().findGradePointAverage(
                    collegeStudent.getStudentGrades().getMathGradeResults()
            ));
        }else {
            m.addAttribute("mathAverage", "N/A");
        }

        if(collegeStudent.getStudentGrades().getScienceGradeResults().size() > 0) {
            m.addAttribute("scienceAverage", collegeStudent.getStudentGrades().findGradePointAverage(
                    collegeStudent.getStudentGrades().getScienceGradeResults()
            ));
        }
        else {
            m.addAttribute("scienceAverage", "N/A");
        }

        if(collegeStudent.getStudentGrades().getHistoryGradeResults().size() > 0) {
            m.addAttribute("historyAverage", collegeStudent.getStudentGrades().findGradePointAverage(
                    collegeStudent.getStudentGrades().getHistoryGradeResults()
            ));
        }
        else {
            m.addAttribute("historyAverage", "N/A");
        }

    }
}
