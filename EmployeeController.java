package com.oit.skillportal.controller;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import com.oit.skillportal.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.oit.skillportal.entity.Employee;
import com.oit.skillportal.service.CompetencyService;
import com.oit.skillportal.service.EmployeeService;
import com.oit.skillportal.service.UploadService;
import com.oit.skillportal.utility.MessageResult;
import com.oit.skillportal.utility.ResponseData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api/employee")
public class EmployeeController {

	@Autowired
	EmployeeService employeeService;

	@Autowired
	CompetencyService competencyService;

	private String allowedImageFormat = ".jpg,.gif,.png,.jpeg";

	private String allowedResumeFormat = ".doc,.pdf,.docx";

	@Autowired
	private UploadService storageService;

	/**
	 * 
	 */

	@GetMapping("/isServiceUp")
	public String checkHealth() {
		return "Server is running";
	}

	
	public static final String VALUE="value";
	public static final String LABEL="label";
	/**
	 * 
	 * @param employeeDto
	 * @return
	 * @throws EmployeeAlreadyExistsException
	 */

	@PostMapping("/createEmployee")
	public MessageResult saveEmployee(@Valid @RequestBody EmployeeDto employeeDto) {

		log.info("EmployeeController :createEmployee() ");

		if (employeeService.checkEmployeeEmail(employeeDto.getEmail())) {
			return MessageResult.error("email already exists");
		}
		if (employeeService.checkEmployeeId(employeeDto.getEmployeeId())) {
			return MessageResult.error("employeeId already exists");
		}
		Long ga = employeeDto.getGa();  
		Long oh = employeeDto.getOh();
		Long deployable = employeeDto.getDeployable();

		Long total = deployable + oh + ga;
 
		if (total > 100)
			return MessageResult.error("allocation exceeds 100%");
		if (total < 100)
			return MessageResult.error("allocation must be 100%");

		EmployeeDto newEmployee = null;
		
		try {
			newEmployee = employeeService.saveEmpolyee(employeeDto);

		} catch (Throwable e) {

			log.info("EmployeeController :createEmployee() : error: {}", e.fillInStackTrace());
		}

		if (newEmployee == null) {
			log.info("EmployeeController :createEmployee() : failure");
			return MessageResult.error("Error in creating employee");
		}
		log.info("EmployeeController :createEmployee() : success");
		return MessageResult.success("Employee created successfully");

	}

	/**
	 * 
	 * @param id
	 * @param employeeDto
	 * @return
	 * @throws EmployeeDoesNotExistsException
	 */

	@PutMapping("/editEmployee")
	public MessageResult editEmployee(@Valid @RequestBody EmployeeDto employeeDto) {
		log.info("EmployeeController :editEmployee()", employeeDto);

		Employee employee = null;

		Long ga = employeeDto.getGa();
		Long oh = employeeDto.getOh();
		Long deployable = employeeDto.getDeployable();

		Long total = deployable + oh + ga;

		if (total > 100)
			return MessageResult.error("allocation exceeds 100%");
		if (total < 100)
			return MessageResult.error("allocation must be 100%");
		


		try {
			employee = employeeService.editEmpolyee(employeeDto);

		} catch (Throwable e) {

			log.info("EmployeeController :editEmployee() : error: {}", e.fillInStackTrace());

		}

	
		
		if (employee == null) {
			log.info("EmployeeController :editEmployee() : failure:");
			return MessageResult.error("Error in modifying employee");
		}
		log.info("EmployeeController :editEmployee() : success:");
		return MessageResult.success("Employee modified successfully");

	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	@DeleteMapping("/deleteEmployee/{id}")
	public MessageResult deleteEmployee(@PathVariable String id) {
		Assert.hasText(id, "input employee id required");
		log.info("EmployeeController : deleteEmployee() {}", id);

		boolean isDeleted = employeeService.deleteEmployee(id);

		if (!isDeleted) {
			return MessageResult.error("Error in deleting employee");
		}

		return MessageResult.success("Employee deleted successfully");

	}

	/**
	 * 
	 * @param filters
	 * @return
	 */
	@GetMapping("/getAllEmployee")
	public MessageResult getAllEmployee() {

		log.info("EmployeeController :getAllEmployee()");
 
		List<EmployeeListProjection> resultList = employeeService.findAllEmployee();

		if (resultList == null) {
			log.info("EmployeeController :getAllEmployee(): data not found");
			return MessageResult.noContent();
		}
		ResponseData data = new ResponseData();
		data.setFilters(prepareFilterList());
		data.setActions(new ArrayList<>(Arrays.asList("view", "edit", "delete")));
		data.setData(resultList);
		log.info("EmployeeController :getAllEmployee():  data found");
		return MessageResult.dataFound(data);

	}

	/**
	 * 
	 * @return
	 */
	private List<Map<String, String>> prepareFilterList() {

		List<Map<String, String>> list = new ArrayList<>();

		Map<String, String> map1 = new HashMap<>();
		map1.put(VALUE, "firstName");
		map1.put(LABEL, "First Name");
		Map<String, String> map2 = new HashMap<>();
		map2.put(VALUE, "lastName");
		map2.put(LABEL, "Last Name");
		Map<String, String> map3 = new HashMap<>();
		map3.put(VALUE, "primarySkill");
		map3.put(LABEL, "Primary Skill");
		Map<String, String> map4 = new HashMap<>();
		map4.put(VALUE, "departmentName");
		map4.put(LABEL, "Department Name");
		Map<String, String> map5 = new HashMap<>();
		map5.put(VALUE, "education");
		map5.put(LABEL, "Education");

		Map<String, String> map6 = new HashMap<>();
		map6.put(VALUE, "employeeId");
		map6.put(LABEL, "Employee Id");

		Map<String, String> map7 = new HashMap<>();
		map7.put(VALUE, "email");
		map7.put(LABEL, "Email");

		Map<String, String> map8 = new HashMap<>();
		map8.put(VALUE, "officeLocation");
		map8.put(LABEL, "Office Location");

		Map<String, String> map9 = new HashMap<>();
		map9.put(VALUE, "email");
		map9.put(LABEL, "Email");

		Map<String, String> map10 = new HashMap<>();
		map10.put(VALUE, "designation");
		map10.put(LABEL, "Designation");

		list.add(map10);
		list.add(map1);
		list.add(map2);
		list.add(map3);
		list.add(map4);
		list.add(map5);
		list.add(map6);
		list.add(map7);
		list.add(map8);
		list.add(map9);

		return list;
	}

	/**
	 * 
	 * @param id
	 * @return
	 * @throws ParseException 
	 */

	@GetMapping("/getEmployee/{id}")
	public MessageResult getEmployeeByID(@PathVariable String id) throws ParseException {
		log.info("EmployeeController :getEmployeeByID() {}", id);

		EmployeeDto employeeDto = employeeService.findEmployeeById(id);

		if (employeeDto == null) {
			log.info("EmployeeController :getEmployeeByID() : data not found");
			return MessageResult.noContent();
		}
		log.info("EmployeeController :getEmployeeByID() : data  found");
		return MessageResult.dataFound(employeeDto);

	}

	/**   
	 * 
	 * @param email
	 * @return
	 */

	@GetMapping("/checkEmployeeEmail/{email}")
	public MessageResult checkEmployeeEmail(@PathVariable String email) {
		log.info("EmployeeController :checkEmployeeEmail() {} ", email);

		boolean check = employeeService.checkEmployeeEmail(email);

		return MessageResult.dataFound(check);

	}

	/**
	 * 
	 * @param id
	 * @return
	 */
	@GetMapping("/checkEmployeeId/{id}")
	public MessageResult checkEmployeeId(@PathVariable String id) {
		log.info("EmployeeController :checkEmployeeId() {}", id);

		boolean check = employeeService.checkEmployeeId(id);

		return MessageResult.dataFound(check);

	}

	/* comptency -api */

	/**
	 * 
	 * @param id
	 * @return
	 */

	@GetMapping("/getCompetency/{id}")
	public MessageResult getSkill(@PathVariable Long id) {
		log.info("EmployeeController :getSkill() {}", id);
		CompetencyDto skillDto = competencyService.getCompetency(id);

		if (skillDto == null) {
			log.info("EmployeeController :getSkill() : data not found");
			return MessageResult.noContent();
		}
		log.info("EmployeeController :getSkill() : data found");
		return MessageResult.dataFound(skillDto);
	}

	/**
	 * 
	 * @return
	 */
	@GetMapping("/getAllCompetency")
	public MessageResult getSkills() {
		log.info("EmployeeController : getSkills()");
		List<CompetencyDto> skillsList = competencyService.getAllCompetency();

		if (skillsList == null) {
			log.info("EmployeeController : getSkills() : no data");
			return MessageResult.noContent();
		}

		ResponseData data = new ResponseData();

		data.setActions(new ArrayList<>(Arrays.asList("edit", "delete")));
		data.setData(skillsList);
		log.info("EmployeeController : getSkills() : data found");
		return MessageResult.dataFound(data);
	}

	/**
	 * get only competency names*
	 * 
	 * @return
	 */
	@GetMapping("/getAllCompetencyList")
	public MessageResult getSkillsList() {
		log.info("EmployeeController : getSkillsList()");
		List<CompetencyListDto> skillsList = competencyService.getAllCompetencyList();

		if (skillsList == null) {
			log.info("EmployeeController : getSkillsList() : no data");
			return MessageResult.noContent();
		}
		log.info("EmployeeController : getSkillsList() : data found");
		return MessageResult.dataFound(skillsList);
	}

	/**
	 * all attributes names from all competencies
	 * 
	 * @return
	 */
	@GetMapping("/getAllAttributeNames")
	public MessageResult getAllAttributeNames() {
		log.info("EmployeeController : getAllAttributeNames()");
		List<String> attributes = competencyService.getAllAttributeNames();

		if (attributes == null) {
			log.info("EmployeeController : getAllAttributeNames() : no data");
			return MessageResult.noContent();
		}
		log.info("EmployeeController : getAllAttributeNames() : data found");
		return MessageResult.dataFound(attributes);
	}

	/**
	 * 
	 * @return
	 */

	@GetMapping("/getAllCompetencyNames")
	public MessageResult getAllSkillNames() {
		log.info("EmployeeController : getAllSkillNames()");
		List<String> skills = competencyService.getAllSkillNames();

		if (skills == null) {
			log.info("EmployeeController : getAllSkillNames() : no data");
			return MessageResult.noContent();
		}
		log.info("EmployeeController : getAllSkillNames() : data found");
		return MessageResult.dataFound(skills);
	}

	/**
	 * get all attributes under competency by id
	 */
	@GetMapping("/getAttributesByCompetency/{id}")
	public MessageResult getAttributes(@PathVariable Long id) {
		log.info("EmployeeController : getAttributes() {}", id);
		List<AttributeListDto> attributeList = competencyService.getAllAttributesList(id);

		if (attributeList == null) {
			log.info("EmployeeController : getAttributes(): no data");
			return MessageResult.noContent();
		}
		log.info("EmployeeController : getAttributes() : data found");
		return MessageResult.dataFound(attributeList);
	}

	/**
	 * 
	 * @param skill
	 * @return
	 */

	@PostMapping("/createCompetency")
	public MessageResult saveCompetency(@Valid @RequestBody CompetencyDto skill) {

		log.info("EmployeeController : saveCompetency() ", skill);
		CompetencyDto obj = null;
		if (competencyService.checkCompetency(skill.getName()))
			return MessageResult.validationError("skill already exists");

		try {
			obj = competencyService.saveCompetency(skill);
		} catch (Exception e) {
			log.error("EmployeeController : saveCompetency(): error", e.fillInStackTrace());
		}

		if (obj == null) {
			log.info("EmployeeController :saveCompetency() : failure");
			return MessageResult.error("Error in creating competency");
		}
		log.info("EmployeeController :saveCompetency() : success");
		return MessageResult.success("competency added successfully");
	}

	/**
	 * 
	 */
	@PutMapping("/editCompetency")
	public MessageResult editCompetency(@RequestBody CompetencyDto skill) {
		log.info("EmployeeController : editCompetency()", skill);
		CompetencyDto obj = null;
		try {
			obj=competencyService.editCompetency(skill);
		} catch (Exception e) {
			log.error("EmployeeController : editCompetency(): error", e.fillInStackTrace());
			
		}

		if (obj == null) {
			log.info("EmployeeController :editCompetency() : failure");
			return MessageResult.error("Error in editing competency");
		}
		log.info("EmployeeController :editCompetency() : success");
		return MessageResult.success("competency edited successfully.");
	}

	/**
	 * 
	 */
	@DeleteMapping("/deleteCompetency/{id}")
	public MessageResult deleteCompetency(@PathVariable Long id) {
		log.info("EmployeeController : deleteCompetency() {}", id);

		if (!competencyService.checkCompetency(id)) {
			return MessageResult.error("competency you trying to delete does not exists.");
		}

		try {
			competencyService.deleteCompetency(id);
		} catch (Exception e) {
			log.error("EmployeeController : deleteCompetency(): error", e.fillInStackTrace());
			
		}
		log.info("EmployeeController : deleteCompetency() :success");
		return MessageResult.success("competency deleted successfully.");
	}

	/**
	 * 
	 */



	

	/**
	 * 
	 * 
	 * @return
	 */

	@GetMapping("/getAllEmployeeStaticData")
	public MessageResult allEmployeeStaticData() {
		log.info("EmployeeController : allEmployeeStaticData ()");
		EmployeeDataDto allemployeedata = employeeService.findAllSaticEmployeeData();
		if (allemployeedata != null) {
			log.info("EmployeeController : allEmployeeStaticData ():data found");
			return MessageResult.dataFound(allemployeedata);
		}
		log.info("EmployeeController : allEmployeeStaticData ():no data found");
		return MessageResult.noContent();
	}
    /**
     * 
     * @return
     */
	@GetMapping("/getReportingTo")
	public MessageResult getAllEmployeeRoles() {
		log.info("EmployeeController : getAllEmployeeRoles() ");
		List<Long> roleIds = new ArrayList<>();
		// add code to fetch role's id from DB
		roleIds.add(Long.valueOf(1));
		roleIds.add(Long.valueOf(2));
		roleIds.add(Long.valueOf(3));
		roleIds.add(Long.valueOf(4));
		roleIds.add(Long.valueOf(5));
		roleIds.add(Long.valueOf(6));
		roleIds.add(Long.valueOf(7));
		

		List<EmployeesRoleDto> allroles = employeeService.findAllRoleNames(roleIds);
		if (allroles != null) {
			log.info("EmployeeController : getAllEmployeeRoles() : data found");
			return MessageResult.dataFound(allroles);
		}
		log.info("EmployeeController : getAllEmployeeRoles() :no data found");
		return MessageResult.noContent();
	}
   /**
    * 
    * @return
    */
	@GetMapping("/getAllProjectManangers")
	public MessageResult getAllProjectManagers() {
		log.info("EmployeeController : getAllProjectManangers() ");
		// add code to fetch role's id from DB
		List<Long> roleNames = new ArrayList<>();
		roleNames.add(Long.valueOf(4));
		roleNames.add(Long.valueOf(2));
		roleNames.add(Long.valueOf(5));

		List<EmployeesRoleDto> allroles = employeeService.findAllRoleNames(roleNames);
		if (allroles != null) {
			log.info("EmployeeController : getAllProjectManangers() : data found");
			return MessageResult.dataFound(allroles);
		}
		log.info("EmployeeController : getAllProjectManangers() :no data found");
		return MessageResult.noContent();
	}

	/**
	 * 
	 * @param file
	 * @return
	 */
	
	@PostMapping("/upload/image")
	public MessageResult upload(@RequestParam("file") MultipartFile file) {
		log.info("EmployeeController : upload() ");
		if (file == null || file.isEmpty()) {

			return MessageResult.error("Image not found");

		}
		if (file.getSize() > 2097152) {

			return MessageResult.error("Image too Large : max size 2MB");

		}
	
		String fileName = org.apache.commons.io.FilenameUtils.getName(file.getOriginalFilename());
		String suffix = fileName.substring(fileName.lastIndexOf("."), fileName.length());

		if (!allowedImageFormat.contains(suffix.trim().toLowerCase())) {
			return MessageResult.error("format not supported");
		}

		String url = storageService.uploadPofileImage(suffix.trim().toLowerCase(), file);

		if (url == null || url.isBlank()) {
			log.info("EmployeeController : upload() : failed");

			return MessageResult.uploadError();
		}
		log.info("EmployeeController : upload() : success");
		return MessageResult.uploadSuccess(url);

	}

	/**
	 * 
	 * @param file
	 * @return
	 */

	@PostMapping("/upload/resume")
	public MessageResult uploadDoc(@RequestParam("file") MultipartFile file) {
		log.info("EmployeeController : uploadDoc() ");
		if (file == null || file.isEmpty()) {

			return MessageResult.error("File not found.");

		}
		if (file.getSize() > 5242880) {

			return MessageResult.error("File too Large : max size 5MB");
		}

		String fileName = org.apache.commons.io.FilenameUtils.getName(file.getOriginalFilename());
		String suffix = fileName.substring(fileName.lastIndexOf("."), fileName.length());

		if (!allowedResumeFormat.contains(suffix.trim().toLowerCase())) {
			return MessageResult.error("format not supported : supported formats .doc/.pdf");
		}

		String url = storageService.uploadResume(suffix.trim().toLowerCase(), file);

		if (url == null || url.isBlank()) {
			return MessageResult.uploadError();
		}

		return MessageResult.uploadSuccess(url);

	}

	


	
	/**
	 * download resume only
	 */

	@GetMapping("/download/{fileName}")
	public ResponseEntity<ByteArrayResource> downloadFile(@PathVariable String fileName) {
		log.info("EmployeeController : downloadFile() ");
		
		String suffix = fileName.substring(fileName.lastIndexOf("."), fileName.length());
		ByteArrayResource resource =null;
		int length=0;
		if (allowedImageFormat.contains(suffix.trim().toLowerCase())) {
			log.info("ProjectController : deleteFile(): downloading profile picture..");
			byte[] data = storageService.downloadImage(fileName);
			resource = new ByteArrayResource(data);
			length=data.length;
		}
		if (allowedResumeFormat.contains(suffix.trim().toLowerCase())) {
			log.info("ProjectController : downloadResume(): downloading resume...");
			byte[] data = storageService.downloadResume(fileName);
			resource = new ByteArrayResource(data);
			length=data.length;
		}
		
	
		
		return ResponseEntity.ok().contentLength(length).header("Content-type", "application/octet-stream")
				.header("Content-disposition", "attachment; filename=\"" + fileName + "\"").body(resource);
	}

	/**
	 * 
	 * @param fileName
	 * @return
	 */

	@DeleteMapping("/delete/{fileName}")
	public MessageResult deleteFile(@PathVariable String fileName) {
		log.info("EmployeeController : deleteFile()");

		String suffix = fileName.substring(fileName.lastIndexOf("."), fileName.length());
		if (allowedImageFormat.contains(suffix.trim().toLowerCase())) {
			log.info("ProjectController : deleteFile(): deleting profile picture");
			return MessageResult.success(storageService.deleteImage(fileName) + "");
		}
		if (allowedResumeFormat.contains(suffix.trim().toLowerCase())) {
			log.info("ProjectController : deleteFile(): deleting resume");
			return MessageResult.success(storageService.deleteResume(fileName) + "");
		}

		return MessageResult.error("Error in deleting file");
	}
	
	
}
