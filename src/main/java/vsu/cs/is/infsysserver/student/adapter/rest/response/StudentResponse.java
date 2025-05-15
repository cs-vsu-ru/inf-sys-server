package vsu.cs.is.infsysserver.student.adapter.rest.response;


import lombok.Data;
import vsu.cs.is.infsysserver.security.entity.temp.Role;
import vsu.cs.is.infsysserver.student.adapter.jpa.entity.Student;

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
        this.supervisor = student.getSupervisor().getId();
        this.imageUrl = student.getImageUrl();
    }
}
