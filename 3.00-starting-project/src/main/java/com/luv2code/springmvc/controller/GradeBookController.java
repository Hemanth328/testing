package com.luv2code.springmvc.controller;

import com.luv2code.springmvc.models.*;
import com.luv2code.springmvc.service.StudentAndGradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class GradeBookController {

	@Autowired
	private Gradebook gradebook;

	@Autowired
	private StudentAndGradeService studentService;

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String getStudents(Model m) {
		Iterable<CollegeStudent> collegeStudentIterable = studentService.getAllStudentsOfGradeBook();
		m.addAttribute("students", collegeStudentIterable);
		return "index";
	}

	@PostMapping("/")
	public String postStudents(@ModelAttribute("student") CollegeStudent student, Model model) {
		System.out.println(student);
		studentService.createStudent(student.getFirstname(), student.getLastname(), student.getEmailAddress());

		Iterable<CollegeStudent> collegeStudentIterable = studentService.getAllStudentsOfGradeBook();

		model.addAttribute("students", collegeStudentIterable);

		return "index";
	}

	@GetMapping("/delete/student/{id}")
	public String deleteStudentWithGivenId(@PathVariable Integer id, Model m) {

		if(!studentService.checkIfStudentIsNull(id))
			return "error";

		studentService.deleteStudent(id);

		Iterable<CollegeStudent> collegeStudentIterable = studentService.getAllStudentsOfGradeBook();

		m.addAttribute("students", collegeStudentIterable);

		return "index";
	}


	@GetMapping("/studentInformation/{id}")
		public String studentInformation(@PathVariable int id, Model m) {

		GradebookCollegeStudent collegeStudent = studentService.getStudentInformation(id);
		if(collegeStudent == null) {
			return "error";
		}

		studentService.configureStudentInformation(id, m);

		return "studentInformation";

		}


	@PostMapping("/grades")
	public String createStudentGrades(@RequestParam("value") Double value, @RequestParam("studentId") Integer studentId,
									  @RequestParam("gradeType") String gradeType, Model m) {

		boolean success = studentService.createGrade(value, studentId, gradeType);

		if(! success)
			return "error";

		studentService.configureStudentInformation(studentId, m);

		return "studentInformation";
	}

	@GetMapping("/grades/{id}/{gradeType}")
	public String deleteGradesUsingIdAndGradeType(@PathVariable Integer id, @PathVariable String gradeType, Model m) {

		Integer studentId = studentService.deleteGrade(id, gradeType);

		if(studentId == 0)
			return "error";

		studentService.configureStudentInformation(studentId, m);

		return "studentInformation";
	}

}
