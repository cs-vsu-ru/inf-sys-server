package vsu.cs.is.infsysserver.student.adapter.rest.response;


import lombok.Data;
import vsu.cs.is.infsysserver.security.entity.temp.Role;
import vsu.cs.is.infsysserver.student.adapter.jpa.entity.Department;
import vsu.cs.is.infsysserver.student.adapter.jpa.entity.Student;
import vsu.cs.is.infsysserver.student.topic.adapter.jpa.entity.StudentTopicAssignment;
import vsu.cs.is.infsysserver.user.adapter.jpa.entity.User;

import java.util.Optional;

@Data
public class StudentResponse {

    private Long id;

    private Role role;

    private String email;

    private String firstName;

    private String lastName;

    private String patronymic;

    private Integer course;

    private String group;

    private Integer startYear;

    private Integer endYear;

    private Long supervisor;

    private String imageUrl;

    private String login;

    private String courseJob;

    private Long scientificSupervisor;

    private String departmentInfo;

    private Boolean isActive;

    private String supervisorFullName;

    private Long supervisorEmployeeId;

    private String courseWorkTopic;

    private String thesisTopic;

    public static StudentResponse fromStudentAndTopics(
            Student student,
            Optional<StudentTopicAssignment> assignment
    ) {
        StudentResponse response = new StudentResponse(student);
        assignment.ifPresent(a -> {
            response.setSupervisorFullName(a.getSupervisorFullName());
            response.setSupervisorEmployeeId(
                    a.getSupervisorEmployee() != null
                            ? a.getSupervisorEmployee().getId()
                            : null
            );
            response.setCourseWorkTopic(a.getCourseWorkTopic());
            response.setThesisTopic(a.getThesisTopic());
        });
        return response;
    }

    public StudentResponse(Student student) {
        this.id = student.getId();
        this.role = student.getUser().getRole();
        this.email = student.getUser().getEmail();
        this.firstName = student.getUser().getFirstName();
        this.lastName = student.getUser().getLastName();
        this.login = student.getUser().getLogin();
        this.patronymic = student.getPatronymic();
        this.course = student.getCourse();
        this.group = student.getGroup();
        this.startYear = student.getStartYear();
        this.endYear = student.getEndYear();
        this.supervisor = Optional.ofNullable(student.getSupervisor())
                .map(User::getId)
                .orElse(null);
        this.imageUrl = student.getImageUrl();
        this.courseJob = student.getCourseJob();
        this.scientificSupervisor = Optional.ofNullable(student.getScientificSupervisor())
                .map(User::getId)
                .orElse(null);
        this.departmentInfo = Optional.ofNullable(student.getDepartment())
                .map(Department::getDescription)
                .orElse(null);
        this.isActive = !student.isDisabled();
    }
}
