package com.haulmont.testtask.win;

import com.haulmont.testtask.Constants;
import com.haulmont.testtask.MainUI;
import com.haulmont.testtask.dbService.dataSets.Group;
import com.haulmont.testtask.dbService.dataSets.Student;
import com.haulmont.testtask.utils.DateUtils;
import com.vaadin.ui.DateField;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.TextField;

public  class StudentW extends EditW {
    private MainUI mainUI;
    private Student entity = new Student();
    private NativeSelect groupNo = new NativeSelect<Group>(Constants.GROUP_NO);
    private TextField firstname = new TextField(Constants.NAME, "");
    private TextField lastname = new TextField(Constants.LAST_NAME, "");
    private TextField patronymicname = new TextField(Constants.PA_NAME, "");
    private DateField datestudent = new DateField(Constants.DATE);

    public StudentW( MainUI mainUI, boolean isEdit )  {
        super(((isEdit)? Constants.EDIT_STUDENT:Constants.NEW_STUDENT),510,250);
        this.mainUI = mainUI;

        refreshData(isEdit);

        firstname.setWidth(100, Unit.PIXELS);
        lastname.setWidth(200, Unit.PIXELS);
        patronymicname.setWidth(120, Unit.PIXELS);
        layMain.addComponent(new HorizontalLayout(firstname,lastname,patronymicname));
        datestudent.setWidth(200, Unit.PIXELS);
        datestudent.setDateFormat("yyyy-MM-dd");
        groupNo.setItemCaptionGenerator( apply -> {   return ((Group)apply).getNumber(); });
        groupNo.setEmptySelectionAllowed(false);
        groupNo.setWidth(100, Unit.PIXELS);
        layMain.addComponent(new HorizontalLayout(groupNo,datestudent));
        createCustomButtons();

        saveButton.addClickListener(e -> {
            if ( !isValid() )
                return;
            saveLogic(isEdit);
            mainUI.listStudentEntities();
            close();
        });
    }

    public void refreshData(boolean isEditMode){
        groupNo.setDataProvider(mainUI.gridGroup.getDataProvider());
        if(!isEditMode)
            return;
        entity = mainUI.gridStudent.asSingleSelect().getValue();
        firstname.setValue(entity.getFirstname());
        lastname.setValue(entity.getLastname());
        patronymicname.setValue(entity.getPatronymicname());
        datestudent.setValue(DateUtils.asLocalDate(entity.getDate()));
        groupNo.setSelectedItem(entity.getGroup());
    }

    public void saveLogic(boolean isEditMode){
        entity.setFirstname(firstname.getValue());
        entity.setLastname(lastname.getValue());
        entity.setPatronymicname(patronymicname.getValue());
        entity.setDate(DateUtils.asDate(datestudent.getValue()));
        entity.setGroup((Group)groupNo.getValue());
        if(isEditMode)
            mainUI.service.updateStudent(entity);
        else
            mainUI.service.newStudent(entity);
    }

    public boolean isValid(){
        String sfirstname = firstname.getValue();
        String slastname = lastname.getValue();
        String spatronymicname = patronymicname.getValue();
        if ( sfirstname.trim().length()<1 || slastname.trim().length()<1 || spatronymicname.trim().length()<1 ) {
            showError(Constants.EMPTY_VALUES);
            return false;
        }
        if( sfirstname.length()>20 ) {
            showError(Constants.FIRST_NAME_TO_LONG);
            return false;
        }
        if( slastname.length()>40 ) {
            showError(Constants.LAST_NAME_TO_LONG);
            return false;
        }
        if( spatronymicname.length()>20 ) {
            showError(Constants.PATRONYMIC_NAME_TO_LONG);
            return false;
        }
        if( groupNo == null ) {
            showError(Constants.GROUP_NOT_SELECTED);
            return false;
        }
        if( datestudent.getValue()==null || datestudent.getErrorMessage()!=null ) {
            showError(Constants.DATE_IS_NOT_CORRECT);
            return false;
        }
        return true;
    }
}