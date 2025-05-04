package vsu.cs.is.infsysserver.custom.page.adapter;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vsu.cs.is.infsysserver.custom.page.adapter.jpa.entity.Page;
import vsu.cs.is.infsysserver.custom.page.adapter.jpa.entity.PageBlock;
import vsu.cs.is.infsysserver.custom.page.adapter.jpa.entity.PageElement;
import vsu.cs.is.infsysserver.custom.page.adapter.rest.dto.PageBlockDTO;
import vsu.cs.is.infsysserver.custom.page.adapter.rest.dto.PageDTO;
import vsu.cs.is.infsysserver.custom.page.adapter.rest.dto.PageElementDTO;

@Mapper
public interface PageMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "blocks", source = "blocks")
    Page map(PageDTO pageDTO);

    PageDTO map(Page page);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "page", ignore = true)
    @Mapping(target = "elements", source = "elements")
    PageBlock map(PageBlockDTO dto);

    PageBlockDTO map(PageBlock entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "block", ignore = true)
    PageElement map(PageElementDTO dto);

    PageElementDTO map(PageElement entity);
}
