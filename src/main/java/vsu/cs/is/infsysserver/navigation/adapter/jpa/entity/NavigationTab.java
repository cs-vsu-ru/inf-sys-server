package vsu.cs.is.infsysserver.navigation.adapter.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vsu.cs.is.infsysserver.navigation.adapter.rest.dto.request.NavigationTabUpdateRequest;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "navigation_tabs")
@Entity
public class NavigationTab {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "visible", nullable = false)
    private Boolean visible;

    public void updateFromRequest(NavigationTabUpdateRequest request) {
        this.name = request.name() != null ? request.name() : this.name;
        this.url = request.url() != null ? request.url() : this.url;
        this.sortOrder = request.sortOrder() != null ? request.sortOrder() : this.sortOrder;
        this.visible = request.visible() != null ? request.visible() : this.visible;
    }
}
