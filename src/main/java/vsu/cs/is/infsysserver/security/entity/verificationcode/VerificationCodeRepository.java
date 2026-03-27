package vsu.cs.is.infsysserver.security.entity.verificationcode;

import org.springframework.data.jpa.repository.JpaRepository;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, String> {
}
