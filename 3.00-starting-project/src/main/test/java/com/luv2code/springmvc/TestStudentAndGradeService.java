package com.luv2code.springmvc;

import com.luv2code.springmvc.models.*;
import com.luv2code.springmvc.repository.HistoryGradeDao;
import com.luv2code.springmvc.repository.MathGradesDao;
import com.luv2code.springmvc.repository.ScienceGradeDao;
import com.luv2code.springmvc.repository.StudentDao;
import com.luv2code.springmvc.service.StudentAndGradeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@TestPropertySource("/application-test.properties")
@SpringBootTest
public class TestStudentAndGradeService {

    @Autowired
    private StudentAndGradeService studentService;

    @Autowired
    private MathGradesDao mathGradesDao;

    @Autowired
    private ScienceGradeDao scienceGradeDao;

    @Autowired
    private HistoryGradeDao historyGradeDao;

    @Autowired
    private JdbcTemplate jdbc;

    @Value("${sql.script.create.student}")
    private String sqlAddStudent;

    @Value("${sql.script.create.math.grade}")
    private String sqlAddMathGrade;

    @Value("${sql.script.create.science.grade}")
    private String sqlAddScienceGrade;

    @Value("${sql.script.create.history.grade}")
    private String sqlAddHistoryGrade;

    @Value("${sql.script.delete.student}")
    private String sqlDeleteStudent;

    @Value("${sql.script.delete.math.grade}")
    private String sqlDeleteMathGrade;

    @Value("${sql.script.delete.science.grade}")
    private String sqlDeleteScienceGrade;

    @Value("${sql.script.delete.history.grade}")
    private String sqlDeleteHistoryGrade;

    @Autowired
    StudentDao studentDao;

    @BeforeEach
    public void beforeEachSetupDatabase() {
        jdbc.execute(sqlAddStudent);
        jdbc.execute(sqlAddMathGrade);
        jdbc.execute(sqlAddScienceGrade);
        jdbc.execute(sqlAddHistoryGrade);

    }

    @DisplayName("test create student service")
    @Test
    public void createStudentService() {
        studentService.createStudent("Reddy", "Hemanth Kumar", "hemanth.kumar@gmail.com");

        CollegeStudent student = studentDao.findByEmailAddress("hemanth.kumar@gmail.com");

        assertEquals("hemanth.kumar@gmail.com", student.getEmailAddress(), "The Expected and Actual email should be same");
    }

    @DisplayName("Test Check Student is Null")
    @Test
    public void testCheckIsStudentNull() {

        assertTrue(studentService.checkIfStudentIsNull(1), "This should return True");

        assertFalse(studentService.checkIfStudentIsNull(0), "This should return False as the Student does not exist with ID 0");
    }

    @DisplayName("Test Delete Student From Database")
    @Test
    public void testDeleteStudentFromDatabase() {
        Optional<CollegeStudent> deletedStudent = studentDao.findById(1);

        assertTrue(deletedStudent.isPresent(), "Should return true");

        studentService.deleteStudent(1);

        deletedStudent = studentDao.findById(1);

        assertFalse(deletedStudent.isPresent(), "Should return false");
    }

    @Sql("/insertdata.sql") // This annotation is used to execute the sql file before the test
    @DisplayName("Test Fetch All the student records from the Database")
    @Test
    public void testGradeBookService() {

        Iterable<CollegeStudent> collegeStudentIterable = studentService.getAllStudentsOfGradeBook();

        List<CollegeStudent> collegeStudentList = new ArrayList<>();

        for(CollegeStudent collegeStudent : collegeStudentIterable) {
            collegeStudentList.add(collegeStudent);
        }

        assertEquals(5, collegeStudentList.size());
    }

    @DisplayName("Test to Check Creation of Grades to A Student")
    @Test
    public void testCreateGradeService() {

        // Create the grades
        assertTrue(studentService.createGrade(80.50, 1, "maths"));
        assertTrue(studentService.createGrade(85.15, 1, "science"));
        assertTrue(studentService.createGrade(91.25, 1, "history"));

        // Get all grades with studentId
        Iterable<MathGrade> mathGrades = mathGradesDao.findGradeByStudentId(1);
        Iterable<ScienceGrade> scienceGrades = scienceGradeDao.findGradeByStudentId(1);
        Iterable<HistoryGrade> historyGrades = historyGradeDao.findGradeByStudentId(1);

        // verify there are grades
      /*  assertTrue(mathGrades.iterator().hasNext(), "Student does not have Math Grades");
        assertTrue(scienceGrades.iterator().hasNext(), "Student does not have Science Grades");
        assertTrue(historyGrades.iterator().hasNext(), "Student does not have History Grades");*/

        assertTrue(((Collection<MathGrade>)mathGrades).size() == 2, "Student does not have 2 Math Grades");
        assertTrue(((Collection<ScienceGrade>)scienceGrades).size() == 2, "Student does not have 2 Science Grades");
        assertTrue(((Collection<HistoryGrade>)historyGrades).size() == 2, "Student does not have 2 History Grades");
    }

    @DisplayName("Test To Check Create Grade Service With Invalid Parameters")
    @Test
    public void testCreateGradeServiceWithInvalidParameters() {

        assertFalse(studentService.createGrade(120, 1, "meths"));
        assertFalse(studentService.createGrade(-10, 1, "science"));
        assertFalse(studentService.createGrade(76.41, 2, "meths"));
        assertFalse(studentService.createGrade(68.93, 1, "literature"));
    }

    @DisplayName("Test to Delete the Grades")
    @Test
    public void testDeleteStudentGrades() {

        Iterable<MathGrade> mathGrades = mathGradesDao.findGradeByStudentId(1);
        Iterable<ScienceGrade> scienceGrades = scienceGradeDao.findGradeByStudentId(1);
        Iterable<HistoryGrade> historyGrades = historyGradeDao.findGradeByStudentId(1);

        assertTrue(mathGrades.iterator().hasNext());
        assertTrue(scienceGrades.iterator().hasNext());
        assertTrue(historyGrades.iterator().hasNext());

        assertEquals(1, studentService.deleteGrade( 1, "maths"));
        assertEquals(1, studentService.deleteGrade( 1, "science"));
        assertEquals(1, studentService.deleteGrade( 1, "history"));

        mathGrades = mathGradesDao.findGradeByStudentId(1);
        scienceGrades = scienceGradeDao.findGradeByStudentId(1);
        historyGrades = historyGradeDao.findGradeByStudentId(1);

        assertTrue(((Collection<MathGrade>)mathGrades).size() == 0);
        assertTrue(((Collection<ScienceGrade>)scienceGrades).size() == 0);
        assertTrue(((Collection<HistoryGrade>)historyGrades).size() == 0);

    }

    @DisplayName("Test Delete Student Service")
    @Test
    public void testDeleteStudentService() {

        Optional<CollegeStudent> deletedCollegeStudent = studentDao.findById(1);
        Optional<MathGrade> deletedMathGrade = mathGradesDao.findById(1);
        Optional<ScienceGrade> deletedScienceGrade = scienceGradeDao.findById(1);
        Optional<HistoryGrade> deletedHistoryGrade = historyGradeDao.findById(1);

        assertTrue(deletedCollegeStudent.isPresent());
        assertTrue(deletedMathGrade.isPresent());
        assertTrue(deletedScienceGrade.isPresent());
        assertTrue(deletedHistoryGrade.isPresent());

        studentService.deleteStudent(1);

        deletedCollegeStudent = studentDao.findById(1);
        deletedMathGrade = mathGradesDao.findById(1);
        deletedScienceGrade = scienceGradeDao.findById(1);
        deletedHistoryGrade = historyGradeDao.findById(1);

        assertFalse(deletedCollegeStudent.isPresent());
        assertFalse(deletedMathGrade.isPresent());
        assertFalse(deletedScienceGrade.isPresent());
        assertFalse(deletedHistoryGrade.isPresent());

    }



    @DisplayName("Test Check Delete Student Grades With Invalid Parameters")
    @Test
    public void testCheckDeleteGradesWithInvalidGrades() {

        assertEquals(0, studentService.deleteGrade(0, "maths"), "No student should have a grade id with 0");
        assertEquals(0, studentService.deleteGrade(1, "literature"), "No student should have a class of type Literature");
    }

    @DisplayName("Test Student Information")
    @Test
    public void testStudentInformation() {

        GradebookCollegeStudent gradebookCollegeStudent = studentService.getStudentInformation(1);

        assertNotNull(gradebookCollegeStudent);
        assertEquals(1, gradebookCollegeStudent.getId());
        assertEquals("Reddy", gradebookCollegeStudent.getFirstname());
        assertEquals("Hemanth Kumar", gradebookCollegeStudent.getLastname());
        assertEquals("hemanth.reddy@gmail.com", gradebookCollegeStudent.getEmailAddress());
        assertTrue(gradebookCollegeStudent.getStudentGrades().getMathGradeResults().size() == 1);
        assertTrue(gradebookCollegeStudent.getStudentGrades().getScienceGradeResults().size() == 1);
        assertTrue(gradebookCollegeStudent.getStudentGrades().getHistoryGradeResults().size() == 1);

    }

    @DisplayName("Test For Invalid StudentId")
    @Test
    public void testInvalidStudentId() {

        GradebookCollegeStudent gradebookCollegeStudent = studentService.getStudentInformation(2);

        assertNull(gradebookCollegeStudent);

    }

    @AfterEach
    public void testAfterEach() {
        jdbc.execute(sqlDeleteStudent);
        jdbc.execute(sqlDeleteMathGrade);
        jdbc.execute(sqlDeleteScienceGrade);
        jdbc.execute(sqlDeleteHistoryGrade);
    }

}
