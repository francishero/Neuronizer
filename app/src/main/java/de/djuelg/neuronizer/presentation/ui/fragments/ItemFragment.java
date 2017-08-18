package de.djuelg.neuronizer.presentation.ui.fragments;

import android.content.ClipData;
import android.content.ClipDescription;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.djuelg.neuronizer.R;
import de.djuelg.neuronizer.domain.executor.impl.ThreadExecutor;
import de.djuelg.neuronizer.domain.model.todolist.TodoListHeader;
import de.djuelg.neuronizer.domain.model.todolist.TodoListItem;
import de.djuelg.neuronizer.presentation.presenters.ItemPresenter;
import de.djuelg.neuronizer.presentation.presenters.impl.ItemPresenterImpl;
import de.djuelg.neuronizer.storage.TodoListRepositoryImpl;
import de.djuelg.neuronizer.threading.MainThreadImpl;

import static de.djuelg.neuronizer.presentation.ui.Constants.KEY_ITEM_UUID;
import static de.djuelg.neuronizer.presentation.ui.Constants.KEY_TODO_LIST_UUID;
import static de.djuelg.neuronizer.presentation.ui.custom.AppbarCustomizer.changeAppbarTitle;

/**
 *
 */
public class ItemFragment extends Fragment implements ItemPresenter.View, View.OnClickListener {

    @Bind(R.id.header_spinner) Spinner headerSpinner;
    @Bind(R.id.editText_item_title) EditText titleEditText;
    @Bind(R.id.important_switch) SwitchCompat importantSwitch;
    @Bind(R.id.editText_item_details) EditText detailsEditText;
    @Bind(R.id.button_save_item) FloatingActionButton saveButton;
    @Bind(R.id.button_copy_title) Button copyTitleButton;
    @Bind(R.id.button_copy_details) Button copyDetailsButton;

    private ItemPresenter mPresenter;
    private TodoListItem item;
    private String todoListUuid;
    private String itemUuid;

    public ItemFragment() {
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     */
    public static ItemFragment addItem(String todoListUuid) {
        ItemFragment fragment = new ItemFragment();
        Bundle args = new Bundle();
        args.putString(KEY_TODO_LIST_UUID, todoListUuid);
        fragment.setArguments(args);
        return fragment;
    }

    public static ItemFragment editItem(String todoListUuid, String itemUuid) {
        ItemFragment fragment = new ItemFragment();
        Bundle args = new Bundle();
        args.putString(KEY_TODO_LIST_UUID, todoListUuid);
        args.putString(KEY_ITEM_UUID, itemUuid);
        fragment.setArguments(args);
        return fragment;
    }

    private boolean isEditMode() {
        return itemUuid != null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPresenter = new ItemPresenterImpl(
                ThreadExecutor.getInstance(),
                MainThreadImpl.getInstance(),
                this,
                new TodoListRepositoryImpl()
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_item, container, false);
        InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        ButterKnife.bind(this, view);

        saveButton.setOnClickListener(this);
        copyTitleButton.setOnClickListener(this);
        copyDetailsButton.setOnClickListener(this);
        inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        titleEditText.requestFocus();

        loadItems();
        changeAppbarTitle(getActivity(), isEditMode()
                ? R.string.fragment_edit_item
                : R.string.fragment_add_item );

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        InputMethodManager inputManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(titleEditText.getWindowToken(), 0);
    }

    private void loadItems() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            todoListUuid = bundle.getString(KEY_TODO_LIST_UUID);
            itemUuid = bundle.getString(KEY_ITEM_UUID);
        }

        if (isEditMode()) {
            mPresenter.editMode(itemUuid);
        } else {
            mPresenter.addMode(todoListUuid);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_save_item:
                addOrEditItemWithCurrentViewInput();
                break;
            case R.id.button_copy_title:
                copyTitleToClipboard();
                break;
            case R.id.button_copy_details:
                copyDetailsToClipboard();
                break;
        }
    }

    private void addOrEditItemWithCurrentViewInput() {
        String title = titleEditText.getText().toString();
        if (title.isEmpty()) {
            Toast.makeText(getActivity(), R.string.title_mandatory, Toast.LENGTH_SHORT).show();
            return;
        }

        TodoListHeader header = ((TodoListHeader) headerSpinner.getSelectedItem());
        boolean important = importantSwitch.isChecked();
        String details = detailsEditText.getText().toString();

        if(isEditMode()) {
            mPresenter.editItem(itemUuid, title, item.getPosition(), important, details, item.isDone(),
                    todoListUuid, header.getUuid());
        } else {
            mPresenter.expandHeaderOfItem(header.getUuid(), header.getTitle(), header.getPosition());
            mPresenter.addItem(title, important, details, todoListUuid, header.getUuid());
        }
    }

    private void copyTitleToClipboard() {
        copyToClipboard(titleEditText.getText().toString());
    }

    private void copyDetailsToClipboard() {
        copyToClipboard(detailsEditText.getText().toString());
    }

    private void copyToClipboard(String text) {
        if (text.isEmpty()){
            Toast.makeText(getActivity(), R.string.no_clipboard, Toast.LENGTH_SHORT).show();
            return;
        }
        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(ClipDescription.MIMETYPE_TEXT_PLAIN, text);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(getActivity(), R.string.added_clipboard, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void itemSynced() {
        getActivity().onBackPressed();
    }

    @Override
    public void onHeadersLoaded(List<TodoListHeader> headers) {
        ArrayAdapter<TodoListHeader> spinnerAdapter = new ArrayAdapter<>(getContext(),
                R.layout.spinner_item, headers);
        headerSpinner.setAdapter(spinnerAdapter);

        for (TodoListHeader header : headers) {
            if (isEditMode() && header.getUuid().equals(item.getParentHeaderUuid()))
                headerSpinner.setSelection(headers.indexOf(header));
        }
    }

    @Override
    public void onItemLoaded(TodoListItem item) {
        this.item = item;

        titleEditText.append(item.getTitle());
        importantSwitch.setChecked(item.isImportant());
        detailsEditText.append(item.getDetails());

        // load headers after item retrieved in editMode mode
        mPresenter.addMode(todoListUuid);
    }
}