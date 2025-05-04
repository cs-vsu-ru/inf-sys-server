package vsu.cs.is.infsysserver.custom.page.adapter.jpa.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.util.List;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "pages")
public class Page {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "pages_sequence"
    )
    @SequenceGenerator(
            name = "pages_sequence",
            allocationSize = 1
    )
    private Long id;

    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @Column(name = "title")
    private String title;

    @OneToMany(mappedBy = "page", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PageBlock> blocks;

}
