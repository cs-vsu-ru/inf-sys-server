package vsu.cs.is.infsysserver.custom.page.adapter.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vsu.cs.is.infsysserver.custom.page.adapter.jpa.entity.Page;

import java.util.Optional;

public interface PageRepository extends JpaRepository<Page, Long> {

    Optional<Page> findByName(String name);

    void deleteByName(String name);
}
