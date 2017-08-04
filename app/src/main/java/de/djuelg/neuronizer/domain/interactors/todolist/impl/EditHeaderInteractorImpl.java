package de.djuelg.neuronizer.domain.interactors.todolist.impl;

import com.fernandocejas.arrow.optional.Optional;

import de.djuelg.neuronizer.domain.executor.Executor;
import de.djuelg.neuronizer.domain.executor.MainThread;
import de.djuelg.neuronizer.domain.interactors.base.AbstractInteractor;
import de.djuelg.neuronizer.domain.interactors.todolist.EditHeaderInteractor;
import de.djuelg.neuronizer.domain.model.preview.TodoList;
import de.djuelg.neuronizer.domain.model.todolist.TodoListHeader;
import de.djuelg.neuronizer.domain.repository.TodoListRepository;

/**
 * Created by djuelg on 10.07.17.
 */

public class EditHeaderInteractorImpl extends AbstractInteractor implements EditHeaderInteractor {

    private final Callback callback;
    private final TodoListRepository repository;
    private final String uuid;
    private final String title;
    private final int position;
    private final String parentTodoListUuid;

    public EditHeaderInteractorImpl(Executor threadExecutor, MainThread mainThread, Callback callback, TodoListRepository repository, String uuid, String title, int position, String parentTodoListUuid) {
        super(threadExecutor, mainThread);
        this.callback = callback;
        this.repository = repository;
        this.uuid = uuid;
        this.title = title;
        this.position = position;
        this.parentTodoListUuid = parentTodoListUuid;
    }

    @Override
    public void run() {
        final Optional<TodoList> todoList = repository.getTodoListById(parentTodoListUuid);
        final Optional<TodoListHeader> outDatedItem = repository.getHeaderById(uuid);
        if (!todoList.isPresent() || ! outDatedItem.isPresent()) {
            callback.onHeaderNotFound();
            return;
        }

        final TodoListHeader updatedItem = outDatedItem.get().update(title, position, parentTodoListUuid);
        repository.update(updatedItem);

        mMainThread.post(new Runnable() {
            @Override
            public void run() {
                callback.onHeaderUpdated(updatedItem);
            }
        });
    }
}
