package vsu.cs.is.infsysserver.student.adapter.rest.request;


import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StudentRequest {

    private String email;

    private String firstName;

    private String lastName;

    private String patronymic;

    private Integer course;

    private String group;

    private Integer startYear;

    private Integer endYear;

    private Long supervisor;

    private String login;

    private String password;
}
