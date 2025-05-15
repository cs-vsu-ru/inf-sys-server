package vsu.cs.is.infsysserver.custom.page.adapter.jpa.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import vsu.cs.is.infsysserver.custom.page.adapter.jpa.JsonbMapConverter;

import java.util.List;
import java.util.Map;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "page_blocks")
public class PageBlock {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "page_blocks_sequence"
    )
    @SequenceGenerator(
            name = "page_blocks_sequence",
            allocationSize = 1
    )
    private Long id;

    @Column(name = "type")
    private String type;

    @Convert(converter = JsonbMapConverter.class)
    @Column(name = "elements_type", nullable = false, columnDefinition = "jsonb")
    private Map<String, String> elementsType;

    @Column(name = "needs_image")
    private boolean needsImage;

    @Column(name = "position")
    private int position;

    @OneToMany(mappedBy = "block", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PageElement> elements;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id")
    private Page page;

}
