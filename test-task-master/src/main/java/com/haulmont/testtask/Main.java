package com.haulmont.testtask;

import com.haulmont.testtask.dbService.DBService;
import com.haulmont.testtask.dbService.dataSets.Group;
import com.haulmont.testtask.dbService.dataSets.Student;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import java.util.List;

public class Main {

    private static void createDefaultData() throws  Exception{
        theDB.execSQL("sql.sql");
        
        DBService dbService = new DBService();
        dbService.printConnectInfo();
         /*
        if(dbService.getAllStudents().isEmpty()) {

            Group myGroup = dbService.newGroup("gr1", "group 1");
            Date today = new Date();
            dbService.newStudent("st 1", "lastname1", "pname1", today, myGroup);
            dbService.newStudent("st 2", "lastname2", "pname2", today, myGroup);
            dbService.newStudent("st 3", "lastname3", "pname3", today, myGroup);
        }
        */


        List<Student> sts = dbService.getAllStudents();
        for (Student s : sts)
            System.out.println(s);

        List<Group> grs = dbService.getAllGroups();
        for (Group g : grs)
            System.out.println(g);

        //System.out.println(theDB.dumpDB());
    }

    public static void main(String[] args) throws Exception {
        //createDefaultData();

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.addServlet(new ServletHolder(new MainUI.MyUIServlet()), "/*");

        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{ context });

        Server server = new Server(8080);
        server.setHandler(handlers);
        System.out.println("server started");
        server.start() ;
        server.join();
    }
}
