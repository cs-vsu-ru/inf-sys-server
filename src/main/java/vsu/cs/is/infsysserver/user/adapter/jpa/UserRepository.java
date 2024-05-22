package vsu.cs.is.infsysserver.user.adapter.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vsu.cs.is.infsysserver.user.adapter.jpa.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByLogin(String login);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE User u set u.password = :password where u.login =:login")
    void savePasswordByLogin(@Param("login") String login, @Param("password") String password);
}
