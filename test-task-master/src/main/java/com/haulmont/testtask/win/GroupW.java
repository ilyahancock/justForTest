package com.haulmont.testtask.win;

import com.haulmont.testtask.Constants;
import com.haulmont.testtask.MainUI;
import com.haulmont.testtask.dbService.dataSets.Group;
import com.vaadin.ui.TextField;

public  class GroupW extends EditW {
    private Group entity = new Group();
    private TextField groupnumber = new TextField(Constants.GROUP_NO, "");
    private TextField groupname = new TextField(Constants.GROUP_NAME, "");
    private MainUI mainUI;

    public GroupW(MainUI mainUI, boolean isEdit)  {
        super(((isEdit)?Constants.EDIT_GROUP: Constants.NEW_GROUP),400,200);
        this.mainUI = mainUI;
        
        refreshData(isEdit);

        groupnumber.setReadOnly(isEdit);
        groupnumber.setWidth(60, Unit.PIXELS);
        groupname.setWidth(100.0f, Unit.PERCENTAGE);
        layMain.addComponent(groupnumber);
        layMain.addComponent(groupname);
        createCustomButtons();

        saveButton.addClickListener(e -> {
            if ( !isValid() )
                return;
            saveLogic(isEdit);
            mainUI.listGroupEntities();
            close();
        });
    }

    public void refreshData(boolean isEditMode) {
        if (!isEditMode)
            return;
        entity = mainUI.gridGroup.asSingleSelect().getValue();
        groupnumber.setValue(entity.getNumber());
        groupname.setValue(entity.getName());
    }

    public void saveLogic(boolean isEditMode){
        entity.setName(groupname.getValue());
        entity.setNumber(groupnumber.getValue());
        if(isEditMode)
            mainUI.service.updateGroup(entity);
        else
            mainUI.service.newGroup(entity);
    }

    public boolean isValid(  ) {
        if ( groupnumber.getValue().trim().length()<1 || groupname.getValue().trim().length()<1 ) {
            showError(Constants.EMPTY_VALUES);
            return false;
        }
        if( groupnumber.getValue().length()>5 ) {
            showError(Constants.GROUP_NUMBER_TO_LONG);
            return false;
        }
        if( groupname.getValue().length()>20 ) {
            showError(Constants.GROUP_NAME_TO_LONG);
            return false;
        }
        return true;
    }
}