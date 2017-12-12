package com.haulmont.testtask;

import com.haulmont.testtask.dbService.DBService;
import com.haulmont.testtask.dbService.dataSets.Group;
import com.haulmont.testtask.dbService.dataSets.Student;
import com.haulmont.testtask.win.GroupW;
import com.haulmont.testtask.win.StudentW;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.*;
import com.vaadin.ui.components.grid.HeaderRow;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.viritin.button.ConfirmButton;
import org.vaadin.viritin.button.MButton;
import org.vaadin.viritin.fields.MTextField;
import org.vaadin.viritin.grid.MGrid;

import javax.servlet.annotation.WebServlet;
import java.text.SimpleDateFormat;
import java.util.Locale;

@Theme(ValoTheme.THEME_NAME)
public class MainUI extends UI {
    public MGrid<Student> gridStudent = new MGrid<>(Student.class)
           .withProperties( "firstname", "lastname","patronymicname")
           .withColumnHeaders( Constants.NAME, Constants.LAST_NAME,Constants.PA_NAME);
    public MGrid<Group> gridGroup = new MGrid<>(Group.class)
           .withProperties("number", "name")
           .withColumnHeaders(Constants.GROUP_NO, Constants.GROUP_NAME);
    public NativeSelect groupNo = new NativeSelect<Group>(Constants.GROUP_NO);
    public final DBService service = new DBService();
    private MTextField filterByLastname = new MTextField()
            .withPlaceholder(Constants.FILTER_BY_LAST_NAME);

    private Button addGroup = new MButton(VaadinIcons.BOOK, this::addGroup);
    private Button editGroup = new MButton(VaadinIcons.PENCIL, this::editGroup);
    private Button deleteGroup = new ConfirmButton(VaadinIcons.TRASH,
            Constants.ARE_YOU_SURE+" "+Constants.DEL_GROUP, this::removeGroup);

    private Button addStudent = new MButton(VaadinIcons.ACADEMY_CAP, this::addStudent);
    private Button editStudent = new MButton(VaadinIcons.PENCIL, this::editStudent);
    private Button deleteStudent = new ConfirmButton(VaadinIcons.TRASH,
            Constants.ARE_YOU_SURE+" "+Constants.DEL_STUDENT, this::deleteStudent);

    public void addGroup(Button.ClickEvent clickEvent) {
        UI.getCurrent().addWindow(new GroupW(this,false));
    }

    public void editGroup(Button.ClickEvent e) {
        editGroup( gridGroup.asSingleSelect().getValue() );
    }

    protected void editGroup(final Group gr) {
        UI.getCurrent().addWindow(new GroupW(this,true));
    }

    public void removeGroup() {
        service.deleteGroup(gridGroup.asSingleSelect().getValue());
        gridGroup.deselectAll();
        listGroupEntities();
    }

    public void addStudent(Button.ClickEvent clickEvent) {
        UI.getCurrent().addWindow(new StudentW(this,false));
    }

    public void editStudent(Button.ClickEvent e) {
        editStudent( gridStudent.asSingleSelect().getValue() );
    }

    protected void editStudent(final Student st) {
        UI.getCurrent().addWindow(new StudentW(this,true));
    }

    public void deleteStudent() {
        service.deleteStudent(gridStudent.asSingleSelect().getValue());
        gridStudent.deselectAll();
        listStudentEntities();
    }

    public void listStudentEntities() {
        listStudentEntities(filterByLastname.getValue());
    }

    private void listStudentEntities(String nameFilter) {
        String likeFilter = "%" + nameFilter + "%";
        if(groupNo.isEmpty())
            gridStudent.setRows(service.getStudentsByLastname(likeFilter));
        else
            gridStudent.setRows(service.getStudentsByGroupAndLastname(((Group)groupNo.getValue()).getNumber(),likeFilter));
        adjustActionStudentButtonState();
    }

    public void listGroupEntities(){
        gridGroup.setRows(service.getAllGroups());
        groupNo.setDataProvider(gridGroup.getDataProvider());
        adjustActionGroupButtonState();
        listStudentEntities();
    }

    protected void adjustActionStudentButtonState() {
        boolean hasSelection = !gridStudent.getSelectedItems().isEmpty();
        editStudent.setEnabled(hasSelection);
        deleteStudent.setEnabled(hasSelection);
    }

    protected void adjustActionGroupButtonState() {
        boolean hasSelection = !gridGroup.getSelectedItems().isEmpty();
        editGroup.setEnabled(hasSelection);
        deleteGroup.setEnabled(hasSelection);
        if (hasSelection)
            groupNo.setSelectedItem(gridGroup.getSelectedItems().iterator().next());
    }

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        listGroupEntities();
        listStudentEntities();

        Grid.Column groupEntity = gridStudent.addColumn(st -> { return st.getGroup().getNumber(); } ).setCaption(Constants.GROUP_NO);
        gridStudent.addColumn(st -> { return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(st.getDate()); } ).setCaption(Constants.DATE);

        HeaderRow row=gridStudent.appendHeaderRow();
        row.getCell("lastname").setComponent(filterByLastname);
        groupNo.setItemCaptionGenerator( apply -> {   return ((Group)apply).getNumber(); });
        groupNo.addValueChangeListener( e -> listStudentEntities() );
        groupNo.setEmptySelectionAllowed(true);
        groupNo.setWidth(70, Unit.PIXELS);
        row.getCell(groupEntity).setComponent(groupNo);
        gridGroup.asSingleSelect().addValueChangeListener( e -> adjustActionGroupButtonState() );
        gridStudent.asSingleSelect().addValueChangeListener( e -> adjustActionStudentButtonState() );
        filterByLastname.addValueChangeListener( e -> listStudentEntities(e.getValue()) );
        addGroup.setStyleName("friendly");
        deleteGroup.setStyleName("danger");
        addStudent.setStyleName("friendly");
        deleteStudent.setStyleName("danger");
        gridStudent.setWidth(100.0f, Unit.PERCENTAGE);
        gridGroup.setWidth(100.0f, Unit.PERCENTAGE);
        VerticalLayout groupLayout = new VerticalLayout( new HorizontalLayout(addGroup,editGroup,deleteGroup) ,gridGroup);
        groupLayout.setMargin(new MarginInfo(true,false,true,true));
        groupLayout.setWidth(500, Unit.PIXELS);
        VerticalLayout studentLayout = new VerticalLayout( new HorizontalLayout(addStudent,editStudent,deleteStudent), gridStudent );
        studentLayout.setWidth(800, Unit.PIXELS);;
        HorizontalLayout mainLayout = new HorizontalLayout(groupLayout,studentLayout);
        mainLayout.setMargin(false);
        mainLayout.setSpacing(false);
        setContent(mainLayout);
    }

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = MainUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {


        public MyUIServlet() throws Exception {
            theDB.execSQL("sql.sql");
        }

    }
}