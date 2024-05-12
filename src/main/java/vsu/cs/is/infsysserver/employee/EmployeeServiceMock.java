package vsu.cs.is.infsysserver.employee;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vsu.cs.is.infsysserver.employee.adapter.rest.dto.request.EmployeeCreateRequest;
import vsu.cs.is.infsysserver.employee.adapter.rest.dto.request.EmployeeUpdateRequest;
import vsu.cs.is.infsysserver.employee.adapter.rest.dto.response.EmployeeAdminResponse;
import vsu.cs.is.infsysserver.employee.adapter.rest.dto.response.EmployeeResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeeServiceMock implements EmployeeService {
    @Override
    public List<EmployeeResponse> getAllEmployees() {
        return List.of(
                EmployeeResponse.builder()
                        .id(1L)
                        .email("example@example1.com")
                        .academicDegree("Кандидат наук")
                        .academicTitle("Доцент ")
                        .experience("20")
                        .professionalExperience("18")
                        .firstName("Иванов")
                        .imageUrl("http://www.cs.vsu.ru:80/is/api/files/4ecf18f1-c0a3-47a1-bc71-79ade98c1da6Зуев.jpg")
                        .lastName("Иван")
                        .patronymic("Иванович")
                        .build(),

                EmployeeResponse.builder()
                        .id(2L)
                        .email("example@example2.com")
                        .academicDegree("Кандидат наук")
                        .academicTitle("без ученого звания")
                        .imageUrl("http://www.cs.vsu.ru:80/is/api/files/8d246826-b3df-4b4c-b338-5be4f214972dДесятирикова.jpg")
                        .experience("10")
                        .professionalExperience("10")
                        .firstName("Петров")
                        .lastName("Петр")
                        .patronymic("Петрович")
                        .build());
    }

    @Override
    public EmployeeResponse getEmployeeById(long id) {
        return EmployeeResponse.builder()
                .id(1L)
                .email("example@example.com")
                .academicDegree("Кандидат наук")
                .academicTitle("Доцент ")
                .experience("20")
                .professionalExperience("18")
                .firstName("Иванов")
                .lastName("Иван")
                .patronymic("Иванович")
                .build();
    }

    @Override
    public EmployeeAdminResponse getEmployeeAdminById(long id) {
        return null;
    }

    @Override
    public EmployeeResponse createEmployee(EmployeeCreateRequest employeeCreateRequest) {
        return EmployeeResponse.builder()
                .id(1L)
                .email("example@example.com")
                .academicDegree("Кандидат наук")
                .academicTitle("Доцент ")
                .experience("20")
                .professionalExperience("18")
                .firstName("Иванов")
                .lastName("Иван")
                .patronymic("Иванович")
                .build();
    }

    @Override
    public EmployeeAdminResponse updateEmployeeById(long id, EmployeeUpdateRequest employeeUpdateRequest) {
        return EmployeeAdminResponse.builder()
                .id(1L)
                .email("example@example.com")
                .academicDegree("Кандидат наук")
                .academicTitle("Доцент ")
                .experience("20")
                .professionalExperience("18")
                .firstName("Иванов")
                .lastName("Иван")
                .patronymic("Иванович")
                .build();
    }

    @Override
    public void deleteEmployeeById(long id) {
    }
}
