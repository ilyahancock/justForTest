package com.haulmont.testtask.win;

import com.haulmont.testtask.Constants;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.ui.*;

public class EditW extends Window {

    FormLayout layMain = new FormLayout();
    public Button closeButton = new Button(Constants.CANCEL, event -> close() );
    public Button saveButton = new Button(Constants.OK);

    public EditW(String s, int width, int height) {
        super(" "+s);
        setIcon(VaadinIcons.BOOK);
        setMainLayout(width,height);
    }

    public void setMainLayout(int width, int height) {
        center();
        setModal(true);
        setWidth(width, Unit.PIXELS);
        setHeight(height, Unit.PIXELS);
        setResizable(false);
        setClosable(false);

        layMain.setMargin(true);
        layMain.addStyleName("outlined");
        layMain.setSizeFull();

        saveButton.setIcon(VaadinIcons.CHECK);
        saveButton.setStyleName("friendly");
        closeButton.setStyleName("danger");
    }

    protected void createCustomButtons(){
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.addComponents(saveButton, closeButton);
        buttonsLayout.setSpacing(true);

        HorizontalLayout hl = new HorizontalLayout(buttonsLayout);
        hl.setComponentAlignment(buttonsLayout,Alignment.TOP_RIGHT);
        hl.setSizeFull();
        layMain.addComponent(hl);
        setContent(layMain);
    }

    protected void showError(String message){
        Notification.show("Error",message ,Notification.Type.WARNING_MESSAGE);
    }
}