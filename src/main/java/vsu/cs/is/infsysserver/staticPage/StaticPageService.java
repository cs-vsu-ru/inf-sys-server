package vsu.cs.is.infsysserver.staticPage;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vsu.cs.is.infsysserver.staticPage.adapter.StaticPageMapper;
import vsu.cs.is.infsysserver.staticPage.adapter.jpa.StaticPageRepository;
import vsu.cs.is.infsysserver.staticPage.adapter.jpa.entity.StaticPage;
import vsu.cs.is.infsysserver.staticPage.adapter.rest.dto.request.StaticPageCreateRequest;
import vsu.cs.is.infsysserver.staticPage.adapter.rest.dto.request.StaticPageUpdateRequest;
import vsu.cs.is.infsysserver.staticPage.adapter.rest.dto.response.StaticPageResponse;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class StaticPageService {

    private final StaticPageRepository staticPageRepository;

    private final StaticPageMapper staticPageMapper;

    public Collection<StaticPageResponse> getAllStaticPages() {
        return staticPageRepository.findAll().stream().map(staticPageMapper::map).toList();
    }

    public StaticPageResponse getStaticPageById(Long id) {
        return staticPageMapper.map(findByIdOrThrow(id));
    }

    public StaticPageResponse createStaticPage(StaticPageCreateRequest staticPageCreateRequest) {
        var savedStaticPage = staticPageRepository.save(staticPageMapper.map(staticPageCreateRequest));

        return staticPageMapper.map(savedStaticPage);
    }

    @Transactional
    public StaticPageResponse updateStaticPageById(Long id, StaticPageUpdateRequest staticPageUpdateRequest) {
        var staticPage = findByIdOrThrow(id);
        staticPage.updateFromRequest(staticPageUpdateRequest);

        var upadatedStaticPage = staticPageRepository.save(staticPage);

        return staticPageMapper.map(upadatedStaticPage);
    }

    public void deleteStaticPageById(Long id) {
        staticPageRepository.delete(findByIdOrThrow(id));
    }

    private StaticPage findByIdOrThrow(Long id) {
        return staticPageRepository.findById(id)
                .orElseThrow(
                        () -> {
                            var msg = "По идентификатору: %s не найдено ни одной статической страницы".formatted(id);
                            return new EntityNotFoundException(msg);
                        }
                );
    }

}
