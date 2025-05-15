package vsu.cs.is.infsysserver.custom.page.adapter.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vsu.cs.is.infsysserver.custom.page.adapter.jpa.JsonbMapConverter;

import java.util.Map;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "page_elements")
public class PageElement {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "page_elements_sequence"
    )
    @SequenceGenerator(
            name = "page_elements_sequence",
            allocationSize = 1
    )
    private Long id;

    @Convert(converter = JsonbMapConverter.class)
    @Column(name = "value", nullable = false)
    private Map<String, String> value;

    @Column(name = "position")
    private int position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "block_id")
    private PageBlock block;

}
