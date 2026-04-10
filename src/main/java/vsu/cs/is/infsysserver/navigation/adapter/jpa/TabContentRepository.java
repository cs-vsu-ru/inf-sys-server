package vsu.cs.is.infsysserver.navigation.adapter.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import vsu.cs.is.infsysserver.navigation.adapter.jpa.entity.TabContent;

import java.util.Optional;

public interface TabContentRepository extends JpaRepository<TabContent, Long> {

    Optional<TabContent> findByTabId(Long tabId);

    boolean existsByTabId(Long tabId);
}