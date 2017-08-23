package de.djuelg.neuronizer.domain.interactors.todolist.impl;

import com.fernandocejas.arrow.optional.Optional;

import de.djuelg.neuronizer.domain.executor.Executor;
import de.djuelg.neuronizer.domain.executor.MainThread;
import de.djuelg.neuronizer.domain.interactors.base.AbstractInteractor;
import de.djuelg.neuronizer.domain.interactors.todolist.DeleteHeaderInteractor;
import de.djuelg.neuronizer.domain.model.preview.TodoList;
import de.djuelg.neuronizer.domain.model.todolist.TodoListHeader;
import de.djuelg.neuronizer.domain.repository.TodoListRepository;

/**
 * Created by djuelg on 11.07.17.
 */

public class DeleteHeaderInteractorImpl extends AbstractInteractor implements DeleteHeaderInteractor {

    private final Callback callback;
    private final TodoListRepository repository;
    private final String uuid;

    public DeleteHeaderInteractorImpl(Executor threadExecutor, MainThread mainThread, Callback callback, TodoListRepository repository, String uuid) {
        super(threadExecutor, mainThread);
        this.callback = callback;
        this.repository = repository;
        this.uuid = uuid;
    }

    @Override
    public void run() {
        final Optional<TodoListHeader> deletedItem = repository.getHeaderById(uuid);
        if (deletedItem.isPresent()) {
            repository.delete(deletedItem.get());

            final Optional<TodoList> todoList = repository.getTodoListById(deletedItem.get().getParentTodoListUuid());
            if (todoList.isPresent()) repository.update(todoList.get().updateLastChange());

            mMainThread.post(new Runnable() {
                @Override
                public void run() {
                    callback.onHeaderDeleted(deletedItem.get());
                }
            });
        }
    }
}
