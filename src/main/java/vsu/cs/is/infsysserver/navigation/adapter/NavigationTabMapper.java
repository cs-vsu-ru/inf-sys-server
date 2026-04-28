package vsu.cs.is.infsysserver.navigation.adapter;

import org.mapstruct.Mapper;
import vsu.cs.is.infsysserver.navigation.adapter.jpa.entity.NavigationTab;
import vsu.cs.is.infsysserver.navigation.adapter.rest.dto.request.NavigationTabCreateRequest;
import vsu.cs.is.infsysserver.navigation.adapter.rest.dto.response.NavigationTabResponse;

@Mapper
public interface NavigationTabMapper {

    NavigationTabResponse map(NavigationTab navigationTab);

    NavigationTab map(NavigationTabCreateRequest createRequest);
}
