package com.luv2code.springmvc;

import com.luv2code.springmvc.models.CollegeStudent;
import com.luv2code.springmvc.models.GradebookCollegeStudent;
import com.luv2code.springmvc.repository.MathGradesDao;
import com.luv2code.springmvc.repository.StudentDao;
import com.luv2code.springmvc.service.StudentAndGradeService;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestPropertySource("/application-test.properties")
@AutoConfigureMockMvc
@SpringBootTest
public class TestGradeBookController {

    @Autowired
    private static MockHttpServletRequest request;

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private StudentAndGradeService studentCreateServiceMock;

    @Autowired
    private StudentAndGradeService studentAndGradeService;

    @Autowired
    private MathGradesDao mathGradesDao;

    @Autowired
    private StudentDao studentDao;

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

    @BeforeAll
    public static void beforeAllMethod() {

        request = new MockHttpServletRequest();

        request.setParameter("firstName", "Reddy");
        request.setParameter("lastName", "Hemanth Kumar");
        request.setParameter("emailAddress", "reddy.hemanth@gmail.com");
    }

    @BeforeEach
    public void beforeEachSetupDatabase() {
        jdbc.execute(sqlAddStudent);
        jdbc.execute(sqlAddMathGrade);
        jdbc.execute(sqlAddScienceGrade);
        jdbc.execute(sqlAddHistoryGrade);
    }

    @Test
    @DisplayName("Test Fetch Students Controller Request")
    public void testGetStudentHttpRequest() throws Exception {
        CollegeStudent studentOne = new CollegeStudent("Reddy", "Hemanth Kumar", "reddy.hemanth@gmail.com");

        CollegeStudent studentTwo = new CollegeStudent("Srinivas", "reddy", "srinivas.reddy@gmail.com");

        List<CollegeStudent> collegeStudentList = new ArrayList<>(Arrays.asList(studentOne,studentTwo));

        when(studentCreateServiceMock.getAllStudentsOfGradeBook()).thenReturn(collegeStudentList);

        assertIterableEquals(collegeStudentList,  studentCreateServiceMock.getAllStudentsOfGradeBook());

        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/"))
                .andExpect(status().isOk()).andReturn();

        ModelAndView mav = mvcResult.getModelAndView();

        ModelAndViewAssert.assertViewName(mav, "index");
    }

    @DisplayName("Test Create a Student in the Database")
    @Test
    public void testCreateStudentInDataBaseUsingController() throws Exception {

        CollegeStudent collegeStudent = new CollegeStudent("Priyanks", "Dey", "priyanks@gmail.com");

        List<CollegeStudent> collegeStudentList = new ArrayList<>(Arrays.asList(collegeStudent));

        when(studentCreateServiceMock.getAllStudentsOfGradeBook()).thenReturn(collegeStudentList);

        assertIterableEquals(collegeStudentList, studentCreateServiceMock.getAllStudentsOfGradeBook());

        MvcResult mvcResult = mockMvc.perform(post("/")
                .contentType(MediaType.APPLICATION_JSON)
                .param("firstName", request.getParameterValues("firstName"))
                .param("lastName", request.getParameterValues("lastName"))
                .param("emailAddress", request.getParameterValues("emailAddress")))
                .andExpect(status().isOk()).andReturn();

        ModelAndView mav = mvcResult.getModelAndView();

        ModelAndViewAssert.assertViewName(mav, "index");

        CollegeStudent verifyCollegeStudent = studentDao.findByEmailAddress("reddy.hemanth@gmail.com");

        assertNotNull(verifyCollegeStudent, "This should not be null");

    }

    @DisplayName("Test To Delete the Existing Student")
    @Test
    public void testDeleteExistingStudentUsingId() throws Exception {

        assertTrue(studentDao.findById(1).isPresent());

        MvcResult mvcResult = mockMvc.perform(get("/delete/student/{id}", 1))
                                     .andExpect(status().isOk()).andReturn();

        ModelAndView mav = mvcResult.getModelAndView();

        ModelAndViewAssert.assertViewName(mav, "index");

        assertFalse(studentDao.findById(1).isPresent());
    }

    @DisplayName("Deleting the Student that doesn't exist and directs to the Error Page")
    @Test
    public void testDeletingTheStudentThatDoesntExistDirectsToHttpErrorPage() throws Exception {

        MvcResult mvcResult = mockMvc.perform(
                get("/delete/student/{id}", 0))
                .andExpect(status().isOk()).andReturn();

        ModelAndView mav = mvcResult.getModelAndView();

        ModelAndViewAssert.assertViewName(mav, "error");

    }


    @DisplayName("Test StudentInformation with valid id")
    @Test
    public void testStudentInformationWithValidId() throws Exception {

        assertTrue(studentDao.findById(1).isPresent());

        MvcResult mvcResult = mockMvc.perform(
                get("/studentInformation/{id}", 1)).andExpect(status().isOk()).andReturn();

        ModelAndView mav = mvcResult.getModelAndView();

        ModelAndViewAssert.assertViewName(mav, "studentInformation");
    }

    @DisplayName("Test Student Information with Invalid Id")
    @Test
    public void testStudentWithInvalidId() throws Exception {

        assertFalse(studentDao.findById(5).isPresent());

        MvcResult mvcResult = mockMvc.perform(get("/studentInformation/{id}", 5))
                .andExpect(status().isOk()).andReturn();

        ModelAndView mav = mvcResult.getModelAndView();

        ModelAndViewAssert.assertViewName(mav, "error");
    }

    @DisplayName("Test to Create Valid Grade Http Request")
    @Test
    public void testPostGradesToAStudent() throws Exception {

        assertTrue(studentDao.findById(1).isPresent());

        GradebookCollegeStudent gradebookCollegeStudent = studentAndGradeService.getStudentInformation(1);

        assertEquals(1, gradebookCollegeStudent.getStudentGrades().getMathGradeResults().size());

        MvcResult mvcResult = mockMvc.perform(post("/grades")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("value", "89.23")
                        .param("studentId", "1")
                        .param("gradeType", "maths")).andExpect(status().isOk()).andReturn();

        ModelAndView mav = mvcResult.getModelAndView();

        ModelAndViewAssert.assertViewName(mav, "studentInformation");

        gradebookCollegeStudent = studentAndGradeService.getStudentInformation(1);

        assertEquals(2, gradebookCollegeStudent.getStudentGrades().getMathGradeResults().size());
    }

    @DisplayName("Test To Check the Student Grade Creation with Invalid StudentId")
    @Test
    public void testStudentGradeCreationWithInvalidStudentId() throws Exception {

        MvcResult mvcResult = mockMvc.perform(post("/grades")
                .contentType(MediaType.APPLICATION_JSON)
                .param("value", "73.83")
                .param("studentId", "9")
                .param("gradeType", "history")).andExpect(status().isOk()).andReturn();

        ModelAndView mav = mvcResult.getModelAndView();

        ModelAndViewAssert.assertViewName(mav, "error");
    }

    @DisplayName("Test Creation Of Grades With Invalid Parameters")
    @Test
    public void testCreationOfGradesWithInvalidParameters() throws Exception {
        MvcResult mvcResult = mockMvc.perform(post("/grades")
                .contentType(MediaType.APPLICATION_JSON)
                .param("value", "73.83")
                .param("studentId", "1")
                .param("gradeType", "English")).andExpect(status().isOk()).andReturn();

        ModelAndView mav = mvcResult.getModelAndView();

        ModelAndViewAssert.assertViewName(mav, "error");
    }

    @DisplayName("Test Delete Grades Of a Student")
    @Test
    public void testDeleteGradesOfAStudent() throws Exception {

        assertTrue(mathGradesDao.findById(1).isPresent());

        MvcResult mvcResult = mockMvc.perform(get("/grades/{id}/{gradeType}", 1, "maths"))
                .andExpect(status().isOk()).andReturn();

        ModelAndView mav = mvcResult.getModelAndView();

        ModelAndViewAssert.assertViewName(mav, "studentInformation");

        assertFalse(mathGradesDao.findById(1).isPresent());
    }

    @DisplayName("Test Delete Grades With Invalid GradeId")
    @Test
    public void testDeleteGradeOfAStudentWithInvalidGradeId() throws Exception {

        assertFalse(mathGradesDao.findById(4).isPresent());

        MvcResult mvcResult = mockMvc.perform(get("/grades/{id}/{gradeType}", 4, "maths"))
                .andExpect(status().isOk()).andReturn();

        ModelAndView mav = mvcResult.getModelAndView();

        ModelAndViewAssert.assertViewName(mav, "error");

    }

    @DisplayName("Test Delete Grades With Invalid GradeType")
    @Test
    public void testDeleteGradeOfAStudentWithInvalidGradeType() throws Exception {

        assertTrue(mathGradesDao.findById(1).isPresent());

        MvcResult mvcResult = mockMvc.perform(get("/grades/{id}/{gradeType}", 1, "law"))
                .andExpect(status().isOk()).andReturn();

        ModelAndView mav = mvcResult.getModelAndView();

        ModelAndViewAssert.assertViewName(mav, "error");

    }

    @AfterEach
    public void testAfterEach() {
        jdbc.execute(sqlDeleteStudent);
        jdbc.execute(sqlDeleteMathGrade);
        jdbc.execute(sqlDeleteScienceGrade);
        jdbc.execute(sqlDeleteHistoryGrade);
    }

}
