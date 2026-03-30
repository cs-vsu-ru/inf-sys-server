package vsu.cs.is.infsysserver.security.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import vsu.cs.is.infsysserver.configuration.CorsConfig;
import vsu.cs.is.infsysserver.employee.EmployeeService;
import vsu.cs.is.infsysserver.employee.adapter.rest.EmployeeController;
import vsu.cs.is.infsysserver.employee.adapter.rest.dto.response.EmployeeAdminResponse;
import vsu.cs.is.infsysserver.employee.adapter.rest.dto.response.EmployeeResponse;
import vsu.cs.is.infsysserver.news.NewsService;
import vsu.cs.is.infsysserver.news.adapter.rest.NewsController;
import vsu.cs.is.infsysserver.news.adapter.rest.dto.response.NewsResponse;
import vsu.cs.is.infsysserver.security.entity.temp.Role;
import vsu.cs.is.infsysserver.security.entity.token.TokenRepository;
import vsu.cs.is.infsysserver.security.service.AuthenticationService;
import vsu.cs.is.infsysserver.security.service.JwtService;
import vsu.cs.is.infsysserver.student.adapter.StudentService;
import vsu.cs.is.infsysserver.student.adapter.jpa.StudentRepository;
import vsu.cs.is.infsysserver.student.adapter.jpa.entity.Student;
import vsu.cs.is.infsysserver.student.adapter.rest.StudentController;
import vsu.cs.is.infsysserver.student.adapter.rest.response.StudentResponse;
import vsu.cs.is.infsysserver.upload.UploadService;
import vsu.cs.is.infsysserver.upload.adapter.rest.UploadController;
import vsu.cs.is.infsysserver.user.adapter.jpa.entity.User;
import vsu.cs.is.infsysserver.user.adapter.rest.UserController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {
        NewsController.class,
        EmployeeController.class,
        UploadController.class,
        StudentController.class,
        UserController.class
}, properties = {
        "application.security.jwt.secret-key=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970",
        "application.security.jwt.expiration=86400000",
        "application.security.jwt.refresh-token.expiration=604800000"
})
@Import({SecurityConfig.class, JwtFilter.class, CorsConfig.class, JwtService.class})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationProvider authenticationProvider;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private TokenRepository tokenRepository;

    @MockBean
    private NewsService newsService;

    @MockBean
    private EmployeeService employeeService;

    @MockBean
    private UploadService uploadService;

    @MockBean
    private StudentRepository studentRepository;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private StudentService studentService;

    @BeforeEach
    void setUp() {
        given(newsService.getAllNews()).willReturn(List.of(newsResponse()));
        given(newsService.createNews(any())).willReturn(newsResponse());
        given(employeeService.getEmployeeAdminById(anyLong())).willReturn(employeeAdminResponse());
        given(employeeService.getEmployeeByLogin(any())).willReturn(employeeResponse());
        given(uploadService.uploadFile(any())).willReturn("/api/files/test.png");
        given(studentService.getCurrentStudent()).willReturn(studentResponse());
        given(studentRepository.findById(anyLong())).willReturn(Optional.of(studentEntity()));
    }

    @Test
    void getNewsShouldBePublic() throws Exception {
        mockMvc.perform(get("/api/news"))
                .andExpect(status().isOk());
    }

    @Test
    void createNewsShouldRequireModeratorOrAdminPermission() throws Exception {
        mockMvc.perform(post("/api/news")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/news")
                        .with(userAuth("teacher"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/news")
                        .with(moderatorAuth("moderator"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated());
    }

    @Test
    void adminEmployeeEndpointShouldRequireAdminPermission() throws Exception {
        mockMvc.perform(get("/api/employees/admin/1")
                        .with(moderatorAuth("moderator")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/employees/admin/1")
                        .with(adminAuth("admin")))
                .andExpect(status().isOk());
    }

    @Test
    void accountEndpointsShouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/account"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/account")
                        .with(userAuth("teacher")))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/student/account")
                        .with(userAuth("student")))
                .andExpect(status().isOk());
    }

    @Test
    void studentDetailsShouldRequireAdminReadPermission() throws Exception {
        mockMvc.perform(get("/api/student/1")
                        .with(userAuth("student")))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/student/1")
                        .with(adminAuth("admin")))
                .andExpect(status().isOk());
    }

    @Test
    void uploadShouldRequireModeratorOrAdminPermission() throws Exception {
        mockMvc.perform(multipart("/api/upload-file")
                        .file("file", "hello".getBytes()))
                .andExpect(status().isForbidden());

        mockMvc.perform(multipart("/api/upload-file")
                        .file("file", "hello".getBytes())
                        .with(moderatorAuth("moderator")))
                .andExpect(status().isOk());
    }

    private RequestPostProcessor userAuth(String login) {
        return auth(login, Role.USER);
    }

    private RequestPostProcessor moderatorAuth(String login) {
        return auth(login, Role.MODERATOR);
    }

    private RequestPostProcessor adminAuth(String login) {
        return auth(login, Role.ADMIN);
    }

    private RequestPostProcessor auth(String login, Role role) {
        return authentication(new UsernamePasswordAuthenticationToken(
                login,
                "N/A",
                role.getAuthorities().stream()
                        .map(authority -> new SimpleGrantedAuthority(authority.getAuthority()))
                        .toList()
        ));
    }

    private NewsResponse newsResponse() {
        return new NewsResponse(
                1L,
                "Title",
                "Content",
                "https://example.com/image.jpg",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    private EmployeeResponse employeeResponse() {
        return EmployeeResponse.builder()
                .id(1L)
                .firstName("Ivan")
                .lastName("Ivanov")
                .email("ivanov@example.com")
                .mainRole(Role.USER)
                .build();
    }

    private EmployeeAdminResponse employeeAdminResponse() {
        return EmployeeAdminResponse.builder()
                .id(1L)
                .firstName("Ivan")
                .lastName("Ivanov")
                .email("ivanov@example.com")
                .login("ivanov")
                .mainRole(Role.ADMIN)
                .build();
    }

    private StudentResponse studentResponse() {
        return new StudentResponse(studentEntity());
    }

    private Student studentEntity() {
        return Student.builder()
                .id(1L)
                .user(User.builder()
                        .id(10L)
                        .login("student")
                        .email("student@example.com")
                        .firstName("Student")
                        .lastName("User")
                        .role(Role.USER)
                        .build())
                .supervisor(User.builder().id(1L).build())
                .course(3)
                .group("3.1")
                .startYear(2023)
                .endYear(2027)
                .build();
    }
}
