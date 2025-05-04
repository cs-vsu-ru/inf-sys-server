package vsu.cs.is.infsysserver.custom.page.adapter;

import org.mapstruct.Mapper;
import vsu.cs.is.infsysserver.custom.page.adapter.jpa.entity.Page;
import vsu.cs.is.infsysserver.custom.page.adapter.rest.dto.PageDTO;

@Mapper
public interface PageMapper {

    PageDTO map(Page page);
    Page map(PageDTO pageDTO);
}
