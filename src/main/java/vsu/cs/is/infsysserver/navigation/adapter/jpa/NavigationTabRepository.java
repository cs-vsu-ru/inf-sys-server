package vsu.cs.is.infsysserver.navigation.adapter.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import vsu.cs.is.infsysserver.navigation.adapter.jpa.entity.NavigationTab;

import java.util.List;

public interface NavigationTabRepository extends JpaRepository<NavigationTab, Long> {

    List<NavigationTab> findAllByOrderBySortOrder();

    List<NavigationTab> findAllByVisibleTrueOrderBySortOrder();
}
