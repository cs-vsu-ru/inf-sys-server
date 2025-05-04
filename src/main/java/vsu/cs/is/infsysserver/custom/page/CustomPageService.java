package vsu.cs.is.infsysserver.custom.page;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vsu.cs.is.infsysserver.custom.page.adapter.PageMapper;
import vsu.cs.is.infsysserver.custom.page.adapter.jpa.entity.Page;
import vsu.cs.is.infsysserver.custom.page.adapter.jpa.repository.PageRepository;
import vsu.cs.is.infsysserver.custom.page.adapter.rest.dto.PageBlockDTO;
import vsu.cs.is.infsysserver.custom.page.adapter.rest.dto.PageDTO;
import vsu.cs.is.infsysserver.custom.page.adapter.rest.dto.PageElementDTO;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.ResponseEntity.ok;

@Service
@RequiredArgsConstructor
public class CustomPageService {

    private final PageRepository pageRepository;
    private final PageMapper pageMapper;

    public ResponseEntity<List<PageDTO>> getAllPages() {
        return ok(pageRepository.findAll().stream().map(pageMapper::map).toList());
    }

    public ResponseEntity<List<String>> getAllPageNames() {
        return ok(pageRepository.findAll().stream().map(Page::getName).toList());
    }

    public ResponseEntity<PageDTO> getPageByName(String name) {
        return ok(pageRepository.findByName(name).map(pageMapper::map)
                .orElseThrow(() -> new EntityNotFoundException("Не найдено ни одной вкладки с названием " + name)));
    }

    public ResponseEntity<PageDTO> createPage(PageDTO page) {
        return savePage(page);
    }

    public ResponseEntity<PageDTO> updatePageByName(PageDTO page) {
        return savePage(page);
    }

    public void deletePageByName(String name) {
        pageRepository.deleteByName(name);
    }

    private ResponseEntity<PageDTO> savePage(PageDTO page) {
        validatePage(page);
        Page newPage = pageMapper.map(page);
        Optional<Page> pageOptional = pageRepository.findByName(page.name());

        if (pageOptional.isPresent()) {
            Page existingPage = pageOptional.get();
            existingPage.setTitle(newPage.getTitle());
            existingPage.setBlocks(newPage.getBlocks());
            pageRepository.save(existingPage);
        } else {
            pageRepository.save(newPage);
        }
        return ok(page);
    }

    private void validatePage(PageDTO page) {
        boolean isPageValid = page.blocks().stream().allMatch(this::isPageBlockValid);

        if (!isPageValid) {
            throw new ValidationException("Переданная вкладка невалидна");
        }
    }

    private boolean isPageBlockValid(PageBlockDTO pageBlock) {
        for (PageElementDTO element : pageBlock.elements()) {
            if (!element.value().keySet().equals(pageBlock.elementsType().keySet())) {
                return false;
            }
        }

        return true;
    }

}
