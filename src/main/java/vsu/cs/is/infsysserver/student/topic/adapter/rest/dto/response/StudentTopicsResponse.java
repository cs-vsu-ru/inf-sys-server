package vsu.cs.is.infsysserver.student.topic.adapter.rest.dto.response;

import vsu.cs.is.infsysserver.student.topic.adapter.jpa.entity.StudentTopicAssignment;

public record StudentTopicsResponse(
        Long studentId,
        String studentLogin,
        String studentFullName,
        String courseWorkTopic,
        String thesisTopic,
        String supervisorLogin,
        String supervisorFullName,
        Long supervisorEmployeeId
) {

    public static StudentTopicsResponse from(StudentTopicAssignment assignment) {
        return new StudentTopicsResponse(
                assignment.getStudent() != null ? assignment.getStudent().getId() : null,
                assignment.getStudentLogin(),
                assignment.getStudentFullName(),
                assignment.getCourseWorkTopic(),
                assignment.getThesisTopic(),
                assignment.getSupervisorLogin(),
                assignment.getSupervisorFullName(),
                assignment.getSupervisorEmployee() != null ? assignment.getSupervisorEmployee().getId() : null
        );
    }
}
