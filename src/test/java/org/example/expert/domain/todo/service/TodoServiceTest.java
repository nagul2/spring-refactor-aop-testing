package org.example.expert.domain.todo.service;

import org.example.expert.client.WeatherClient;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.dto.request.TodoSaveRequest;
import org.example.expert.domain.todo.dto.response.TodoSaveResponse;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private WeatherClient weatherClient;

    @InjectMocks
    private TodoService todoService;

    @Test
    void Todo_전체_조회시_page가_0미만으로_1페이지로_보정되어_요청한다() {
        // given
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);  // pageable 캡처
        given(todoRepository.findAllByOrderByModifiedAtDesc(any(Pageable.class))).willReturn(Page.empty()); // 빈 페이지 설정

        int page1 = 0;
        int page2 = -15;

        // when
        todoService.getTodos(page1, 10);
        todoService.getTodos(page2, 10);

        // then
        verify(todoRepository, times(2)).findAllByOrderByModifiedAtDesc(captor.capture());  // 2번 호출되었는지 확인
        assertEquals(0, captor.getAllValues().get(0).getPageNumber());  // 첫 번째 호출 결과 0을 집어넣어도 첫 페이지 호출
        assertEquals(0, captor.getAllValues().get(1).getPageNumber());  // 두 번째 호출 결과 -15를 집어넣어도 첫 페이지 호출

    }

    @Test
    void Todo_전체_조회시_정상_응답케이스() {
        // given
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);  // pageable 캡처
        given(todoRepository.findAllByOrderByModifiedAtDesc(any(Pageable.class))).willReturn(Page.empty()); // 빈 페이지 설정

        int page1 = 1;
        int page2 = 3;

        // when
        todoService.getTodos(page1, 10);
        todoService.getTodos(page2, 10);

        // then
        verify(todoRepository, times(2)).findAllByOrderByModifiedAtDesc(captor.capture());  // 2번 호출되었는지 확인
        assertEquals(0, captor.getAllValues().get(0).getPageNumber());  // 첫 번째 호출 결과 0을 집어넣어도 첫 페이지 호출
        assertEquals(2, captor.getAllValues().get(1).getPageNumber());  // 두 번째 호출 결과 -15를 집어넣어도 첫 페이지 호출
    }

    @Test
    void Todo_단건_조회시_없는TODO를_조회하면_예외가_발생한다() {
        // given
        long notExistTodoId = 0;
        given(todoRepository.findByIdWithUser(notExistTodoId)).willReturn(Optional.empty());

        // when & then
        assertThrows(InvalidRequestException.class, () -> todoService.getTodo(notExistTodoId));
    }

    @Test
    void Todo_저장시_정상적으로_저장된다() {
        // given
        AuthUser authUser = new AuthUser(1L, "test@email.com", UserRole.USER);
        String title = "테스트 제목";
        String contents = "테스트 본문";
        String weather = "맑음";

        TodoSaveRequest request = new TodoSaveRequest(title, contents);

        given(weatherClient.getTodayWeather()).willReturn(weather);
        given(todoRepository.save(any(Todo.class)))
                .willAnswer(invocation -> invocation.getArgument(0)); // 저장되는 객체 그대로 반환

        // when
        TodoSaveResponse result = todoService.saveTodo(authUser, request);

        // then
        assertEquals(title, result.getTitle());
        assertEquals(contents, result.getContents());
        assertEquals(weather, result.getWeather());
        assertEquals(authUser.getEmail(), result.getUser().getEmail());
    }

}