package de.djuelg.neuronizer.domain.repository;

import com.fernandocejas.arrow.optional.Optional;

import java.util.List;

import de.djuelg.neuronizer.domain.model.preview.TodoList;
import de.djuelg.neuronizer.domain.model.todolist.TodoListHeader;
import de.djuelg.neuronizer.domain.model.todolist.TodoListItem;
import de.djuelg.neuronizer.domain.model.todolist.TodoListSection;

/**
 * A repository that is responsible for the page of a single todolist
 */
public interface TodoListRepository {

    Optional<TodoList> getTodoListById(String uuid);

    Optional<TodoListHeader> getHeaderById(String uuid);

    Optional<TodoListItem> getItemById(String uuid);

    List<TodoListSection> getSectionsOfTodoListId(String todoListUuid);

    List<TodoListHeader> getHeadersOfTodoListId(String todoListUuid);

    boolean insert(TodoListHeader header);

    boolean insert(TodoListItem item);

    void delete(TodoListHeader deletedItem);

    void delete(TodoListItem deletedItem);

    void update(TodoListHeader updatedItem);

    void update(TodoListItem updatedItem);
}
