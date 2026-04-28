package vsu.cs.is.infsysserver.navigation;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vsu.cs.is.infsysserver.exception.GeneralException;
import vsu.cs.is.infsysserver.navigation.adapter.NavigationTabMapper;
import vsu.cs.is.infsysserver.navigation.adapter.jpa.NavigationTabRepository;
import vsu.cs.is.infsysserver.navigation.adapter.jpa.TabContentRepository;
import vsu.cs.is.infsysserver.navigation.adapter.jpa.entity.NavigationTab;
import vsu.cs.is.infsysserver.navigation.adapter.jpa.entity.TabContent;
import vsu.cs.is.infsysserver.navigation.adapter.rest.dto.request.NavigationTabCreateRequest;
import vsu.cs.is.infsysserver.navigation.adapter.rest.dto.request.NavigationTabReorderRequest;
import vsu.cs.is.infsysserver.navigation.adapter.rest.dto.request.NavigationTabUpdateRequest;
import vsu.cs.is.infsysserver.navigation.adapter.rest.dto.request.TabContentUpdateRequest;
import vsu.cs.is.infsysserver.navigation.adapter.rest.dto.response.NavigationTabResponse;
import vsu.cs.is.infsysserver.navigation.adapter.rest.dto.response.TabContentResponse;

import java.util.Collection;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class NavigationTabService {

    private final NavigationTabRepository navigationTabRepository;

    private final TabContentRepository tabContentRepository;

    private final NavigationTabMapper navigationTabMapper;

    public Collection<NavigationTabResponse> getAllTabs() {
        return navigationTabRepository.findAllByOrderBySortOrder()
                .stream()
                .map(navigationTabMapper::map)
                .toList();
    }

    public Collection<NavigationTabResponse> getVisibleTabs() {
        return navigationTabRepository.findAllByVisibleTrueOrderBySortOrder()
                .stream()
                .map(navigationTabMapper::map)
                .toList();
    }

    public NavigationTabResponse getTabById(Long id) {
        var tab = processFindTabById(id);
        return navigationTabMapper.map(tab);
    }

    public NavigationTabResponse createTab(NavigationTabCreateRequest createRequest) {
        var tab = navigationTabMapper.map(createRequest);
        var savedTab = navigationTabRepository.save(tab);
        createEmptyContent(savedTab);
        return navigationTabMapper.map(savedTab);
    }

    @Transactional
    public NavigationTabResponse updateTabById(Long id,
                                               NavigationTabUpdateRequest updateRequest) {
        var tab = processFindTabById(id);
        tab.updateFromRequest(updateRequest);
        return navigationTabMapper.map(tab);
    }

    public void deleteTabById(Long id) {
        var tab = processFindTabById(id);
        navigationTabRepository.delete(tab);
    }

    @Transactional
    public Collection<NavigationTabResponse> reorderTabs(
            NavigationTabReorderRequest reorderRequest) {
        var itemsMap = reorderRequest.items().stream()
                .collect(Collectors.toMap(
                        item -> item.id(),
                        item -> item.sortOrder()
                ));

        var ids = itemsMap.keySet();
        var tabs = navigationTabRepository.findAllById(ids);

        for (var tab : tabs) {
            Integer newOrder = itemsMap.get(tab.getId());
            if (newOrder != null) {
                tab.setSortOrder(newOrder);
            }
        }

        return navigationTabRepository.findAllByOrderBySortOrder()
                .stream()
                .map(navigationTabMapper::map)
                .toList();
    }

    public TabContentResponse getTabContent(Long tabId) {
        var tab = processFindTabById(tabId);
        var tabContent = tabContentRepository.findByTabId(tabId)
                .orElseGet(() -> createEmptyContent(tab));
        return new TabContentResponse(tabId, tabContent.getContent());
    }

    @Transactional
    public TabContentResponse updateTabContent(Long tabId,
                                               TabContentUpdateRequest request) {
        var tab = processFindTabById(tabId);
        var tabContent = tabContentRepository.findByTabId(tabId)
                .orElseGet(() -> {
                    var newContent = new TabContent();
                    newContent.setTab(tab);
                    return newContent;
                });
        tabContent.setContent(request.content());
        tabContentRepository.save(tabContent);
        return new TabContentResponse(tabId, tabContent.getContent());
    }

    private TabContent createEmptyContent(NavigationTab tab) {
        var tabContent = new TabContent();
        tabContent.setTab(tab);
        tabContent.setContent("");
        return tabContentRepository.save(tabContent);
    }

    private NavigationTab processFindTabById(Long id) {
        return navigationTabRepository.findById(id).orElseThrow(
                () -> new GeneralException(
                        "Вкладки навигации по такому идентификатору не существует",
                        HttpStatus.NOT_FOUND
                )
        );
    }
}
