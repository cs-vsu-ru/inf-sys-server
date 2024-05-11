package vsu.cs.is.infsysserver.news;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vsu.cs.is.infsysserver.exception.GeneralException;
import vsu.cs.is.infsysserver.news.adapter.NewsMapper;
import vsu.cs.is.infsysserver.news.adapter.jpa.NewsRepository;
import vsu.cs.is.infsysserver.news.adapter.jpa.entity.News;
import vsu.cs.is.infsysserver.news.adapter.rest.dto.request.NewsCreateRequest;
import vsu.cs.is.infsysserver.news.adapter.rest.dto.request.NewsUpdateRequest;
import vsu.cs.is.infsysserver.news.adapter.rest.dto.response.NewsResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
@DisplayName("Модульные тесты NewsService")
class NewsServiceTest {

    @Mock
    private NewsMapper newsMapper;

    @Mock
    private NewsRepository newsRepository;

    @InjectMocks
    private NewsService newsService;

    @Test
    @DisplayName("создание новости и отдача сохраненного DTO")
    void createNews_SuccessfulSave_ReturnsNewsResponse() {
        // given
        var newsCreateRequest = new NewsCreateRequest(
                "Новая новость новая",
                "новость новейшая новая",
                "http://cs.vsu.ru/is/image/123"
        );
        var newsEntity = new News();
        newsEntity.setTitle("Новая новость новая");
        newsEntity.setContent("новость новейшая новая");
        newsEntity.setImageLink("http://cs.vsu.ru/is/image/123");

        var now = LocalDateTime.now();

        var savedNews = new News(
                1L,
                "Новая новость новая",
                "новость новейшая новая",
                "http://cs.vsu.ru/is/image/123",
                now,
                null
        );

        doReturn(newsEntity)
                .when(this.newsMapper)
                .map(newsCreateRequest);

        doReturn(new NewsResponse(
                        1L,
                        "Новая новость новая",
                        "новость новейшая новая",
                        "http://cs.vsu.ru/is/image/123",
                        now,
                        null
                )
        )
                .when(this.newsMapper)
                .map(savedNews);
        doReturn(savedNews).when(this.newsRepository).save(newsEntity);

        // when
        var result = this.newsService.createNews(newsCreateRequest);

        // then
        var expectedResult = new NewsResponse(
                1L,
                "Новая новость новая",
                "новость новейшая новая",
                "http://cs.vsu.ru/is/image/123",
                now,
                null
        );
        assertEquals(result, expectedResult);

        verify(this.newsMapper).map(newsCreateRequest);
        verify(this.newsMapper).map(savedNews);
        verify(this.newsRepository).save(newsEntity);
        verifyNoMoreInteractions(this.newsMapper);
        verifyNoMoreInteractions(this.newsRepository);
    }

    @Test
    @DisplayName("Получение news по id")
    void getNewsById_SuccessfulFind_ReturnsNewsResponse() {
        // given
        Long id = 1L;
        var now = LocalDateTime.now();

        var optionalNews = Optional.of(new News(
                id,
                "Новая новость новая",
                "новость новейшая новая",
                "http://cs.vsu.ru/is/image/123",
                now,
                null
        ));
        var foundNews = optionalNews.get();
        var mappedNews = new NewsResponse(
                id,
                "Новая новость новая",
                "новость новейшая новая",
                "http://cs.vsu.ru/is/image/123",
                now,
                null
        );


        doReturn(optionalNews)
                .when(this.newsRepository)
                .findById(id);
        doReturn(mappedNews)
                .when(this.newsMapper)
                .map(foundNews);

        // when
        var result = this.newsService.getNewsById(id);

        // then
        var expectedResult = new NewsResponse(
                id,
                "Новая новость новая",
                "новость новейшая новая",
                "http://cs.vsu.ru/is/image/123",
                now,
                null
        );
        assertEquals(result, expectedResult);

        verify(this.newsMapper).map(foundNews);
        verify(this.newsRepository).findById(id);
        verifyNoMoreInteractions(this.newsMapper);
        verifyNoMoreInteractions(this.newsRepository);
    }

    @Test
    @DisplayName("Отсутствие news по id")
    void getNewsById_NotFoundById_ThrowsException() {
        // given
        Long id = 1L;
        var optionalNews = Optional.empty();

        doReturn(optionalNews)
                .when(this.newsRepository)
                .findById(id);

        // then
        assertThrows(GeneralException.class, () -> this.newsService.getNewsById(id));

        verify(this.newsRepository).findById(id);
        verifyNoMoreInteractions(this.newsRepository);
    }

    @Test
    @DisplayName("Получение всех news")
    void getAllNews_SuccessfulFind_ReturnsNewsResponses() {
        // given
        var now = LocalDateTime.now();
        var foundNews = List.of(
                new News(
                        1L,
                        "Новая новость новая",
                        "новость новейшая новая",
                        "http://cs.vsu.ru/is/image/123",
                        now,
                        null
                ),
                new News(
                        2L,
                        "Новее новость новая",
                        "новость новейшая новая",
                        "http://cs.vsu.ru/is/image/12",
                        now,
                        now
                )
        );
        var mappedNews = List.of(
                new NewsResponse(
                        1L,
                        "Новая новость новая",
                        "новость новейшая новая",
                        "http://cs.vsu.ru/is/image/123",
                        now,
                        null
                ),
                new NewsResponse(
                        2L,
                        "Новее новость новая",
                        "новость новейшая новая",
                        "http://cs.vsu.ru/is/image/12",
                        now,
                        now
                )
        );

        doReturn(foundNews)
                .when(this.newsRepository)
                .findAll();

        doReturn(mappedNews.get(0))
                .when(this.newsMapper)
                .map(foundNews.get(0));
        doReturn(mappedNews.get(1))
                .when(this.newsMapper)
                .map(foundNews.get(1));

        // when
        var result = this.newsService.getAllNews();

        // then
        var expectedResult = List.of(
                new NewsResponse(
                        1L,
                        "Новая новость новая",
                        "новость новейшая новая",
                        "http://cs.vsu.ru/is/image/123",
                        now,
                        null
                ),
                new NewsResponse(
                        2L,
                        "Новее новость новая",
                        "новость новейшая новая",
                        "http://cs.vsu.ru/is/image/12",
                        now,
                        now
                )
        );
        assertEquals(result, expectedResult);

        verify(this.newsRepository).findAll();
        verify(this.newsMapper).map(foundNews.get(0));
        verifyNoMoreInteractions(this.newsRepository);
        verifyNoMoreInteractions(this.newsMapper);
    }

    @Test
    @DisplayName("Обновление news по id")
    void updateNewsById_SuccessfulUpdate_ReturnsNewsResponses() {
        // given
        Long id = 1L;
        var now = LocalDateTime.now();
        var updateRequest = new NewsUpdateRequest(
                id,
                "Крутая новость",
                "новость новость новость",
                "http://cs.vsu.ru/is/image/1233"
        );
        var optionalInitialNews = Optional.of(new News(
                2L,
                "Новее новость новая",
                "новость новейшая новая",
                "http://cs.vsu.ru/is/image/12",
                now,
                now
        ));
        var initalNews = optionalInitialNews.get();

        var updateLocalDateTime = LocalDateTime.now();
        var mappedUpdatedNews = new NewsResponse(
                2L,
                "Крутая новость",
                "новость новость новость",
                "http://cs.vsu.ru/is/image/1233",
                now,
                updateLocalDateTime
        );

        doReturn(optionalInitialNews)
                .when(this.newsRepository)
                .findById(id);
        doReturn(mappedUpdatedNews)
                .when(this.newsMapper)
                .map(initalNews);

        // when
        var result = this.newsService.updateNewsById(id, updateRequest);

        // then
        var expectedResult = new NewsResponse(
                2L,
                "Крутая новость",
                "новость новость новость",
                "http://cs.vsu.ru/is/image/1233",
                now,
                updateLocalDateTime
        );

        assertEquals(result, expectedResult);

        verify(this.newsRepository).findById(id);
        verify(this.newsMapper).map(initalNews);
        verifyNoMoreInteractions(this.newsRepository);
        verifyNoMoreInteractions(this.newsMapper);
    }

    @Test
    @DisplayName("Обновление несуществующего news по id")
    void updateNewsById_NotFoundById_ThrowsException() {
        // given
        Long id = 1L;
        var updateRequest = new NewsUpdateRequest(
                id,
                "Крутая новость",
                "новость новость новость",
                "http://cs.vsu.ru/is/image/1233"
        );

        var optionalNews = Optional.empty();

        doReturn(optionalNews)
                .when(newsRepository)
                .findById(id);

        // then
        assertThrows(GeneralException.class, () -> this.newsService.updateNewsById(id, updateRequest));

        verify(this.newsRepository).findById(id);
        verifyNoMoreInteractions(this.newsRepository);
    }

    @Test
    @DisplayName("Удаление news по id")
    void deleteNewsById_SuccessfulDelete_ReturnsVoid() {
        // given
        Long id = 1L;
        var now = LocalDateTime.now();

        var optionalNews = Optional.of(new News(
                2L,
                "Новее новость новая",
                "новость новейшая новая",
                "http://cs.vsu.ru/is/image/12",
                now,
                now
        ));
        var news = optionalNews.get();

        doReturn(optionalNews)
                .when(this.newsRepository).findById(id);

        // when
        newsService.deleteNewsById(id);

        // then
        verify(this.newsRepository).findById(id);
        verify(this.newsRepository).delete(news);
        verifyNoMoreInteractions(this.newsRepository);
    }

    @Test
    @DisplayName("Удаление несуществующего news по id")
    void deleteNewsById_NotFoundById_ThrowsException() {
        // given
        Long id = 1L;

        var emptyEntity = Optional.empty();

        doReturn(emptyEntity)
                .when(this.newsRepository)
                .findById(id);

        // then
        assertThrows(GeneralException.class, () -> this.newsService.deleteNewsById(id));

        verify(this.newsRepository).findById(id);
        verifyNoMoreInteractions(this.newsRepository);
    }

}
