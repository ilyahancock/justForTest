package com.haulmont.testtask.dbService;

import com.haulmont.testtask.dbService.dao.GroupDAO;
import com.haulmont.testtask.dbService.dao.StudentDAO;
import com.haulmont.testtask.dbService.dataSets.Group;
import com.haulmont.testtask.dbService.dataSets.Student;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.service.ServiceRegistry;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class DBService {
    private static final String hibernate_show_sql = "true";
    private static final String hibernate_hbm2ddl_auto = "update";

    private final SessionFactory sessionFactory;
    

    public DBService() {
        Configuration configuration = getHSQLDBConfiguration();
        sessionFactory = createSessionFactory(configuration);
    }

    public static Configuration getHSQLDBConfiguration() {
        Configuration configuration = new Configuration();
        configuration.addAnnotatedClass(Student.class);
        configuration.addAnnotatedClass(Group.class);

        configuration.setProperty("hibernate.archive.autodetection", "class,hbm");
        configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.HSQLDialect");
        configuration.setProperty("hibernate.connection.driver_class", "org.hsqldb.jdbcDriver");
        configuration.setProperty("hibernate.connection.url", "jdbc:hsqldb:file:testdb");
        configuration.setProperty("hibernate.connection.username", "sa");
        configuration.setProperty("hibernate.connection.password", "");

        configuration.setProperty("hibernate.show_sql", hibernate_show_sql);
        configuration.setProperty("hibernate.hbm2ddl.auto", hibernate_hbm2ddl_auto);
        return configuration;
    }

    public static Configuration getH2Configuration() {
        Configuration configuration = new Configuration();
        configuration.addAnnotatedClass(Student.class);
        configuration.addAnnotatedClass(Group.class);

        configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        configuration.setProperty("hibernate.connection.driver_class", "org.h2.Driver");
        configuration.setProperty("hibernate.connection.url", "jdbc:h2:./h2db");
        configuration.setProperty("hibernate.connection.username", "tully");
        configuration.setProperty("hibernate.connection.password", "tully");
        configuration.setProperty("hibernate.show_sql", hibernate_show_sql);
        configuration.setProperty("hibernate.hbm2ddl.auto", hibernate_hbm2ddl_auto);
        return configuration;
    }

    private List<Student> getStudentsList(String gr, String name)  {
        try {
            Session session = sessionFactory.openSession();
            StudentDAO stdao = new StudentDAO(session);
            List<Student> list_students = stdao.getByGroupAndLastname( gr, name);
            session.close();
            return list_students;
        } catch (HibernateException e) {
            //throw new Exception(e);
            return null;
        }
    }

    public List<Student> getAllStudents() {
        return getStudentsList("","");
    }

    public List<Student> getStudentsByGroupAndLastname(String gr,String lastname)  {
        return getStudentsList(gr,lastname);
    }

    public List<Student> getStudentsByLastname(String lastname)  {
        return getStudentsList("",lastname);
    }

    public void newStudent(Student s){
        try {
            Session session = sessionFactory.openSession();
            session.beginTransaction();
            StudentDAO stdao = new StudentDAO(session);
            //Student st =
                    stdao.insert(s)  ;
            session.getTransaction().commit();
            session.close();
            //return st;
        } catch (HibernateException e) {
          // throw new Exception(e);
            //return null;
        }
    }

    public void updateStudent(Student st)  {
        try {
            Session session = sessionFactory.openSession();
            session.beginTransaction();
            StudentDAO stdao = new StudentDAO(session);
            stdao.update(st)  ;
            session.getTransaction().commit();
            session.close();
        } catch (HibernateException e) {
          //  throw new Exception(e);
        }
    }

    public void updateGroup(Group gr)  {
        try {
            Session session = sessionFactory.openSession();
            session.beginTransaction();
            GroupDAO grdao = new GroupDAO(session);
            grdao.update(gr)  ;
            session.getTransaction().commit();
            session.close();
        } catch (HibernateException e) {
            //throw new Exception(e);
        }
    }

    public void newGroup(Group g)  {
        try {
            Session session = sessionFactory.openSession();
            session.beginTransaction();
            GroupDAO grdao = new GroupDAO(session);
            //Group gr =
                    grdao.insert(g);
            session.getTransaction().commit();
            session.close();
            //return gr;
        } catch (HibernateException e) {
           // throw new Exception(e);
            //return null;
        }
    }

    public void deleteGroup(Group gr)  {
        try {
            Session session = sessionFactory.openSession();
            session.beginTransaction();
            GroupDAO grdao = new GroupDAO(session);
            grdao.delete(gr)  ;
            session.getTransaction().commit();
            session.close();
        } catch (HibernateException e) {
           // throw new Exception(e);
        }
    }

    public void deleteStudent(Student st)  {
        try {
            Session session = sessionFactory.openSession();
            session.beginTransaction();
            StudentDAO stdao = new StudentDAO(session);
            stdao.delete(st)  ;
            session.getTransaction().commit();
            session.close();
        } catch (HibernateException e) {
            //throw new Exception(e);
        }
    }

    public List<Group> getAllGroups() {
        try {
            Session session = sessionFactory.openSession();

            GroupDAO grdao = new GroupDAO(session);
            List<Group> list_grps = grdao.getAll();
            session.close();
            return list_grps;
        } catch (HibernateException e) {
            //throw new Exception(e);
            return null;
        }
    }

    public void printConnectInfo() {
        try {
            SessionFactoryImpl sessionFactoryImpl = (SessionFactoryImpl) sessionFactory;
            Connection connection = sessionFactory.
                    getSessionFactoryOptions().getServiceRegistry().
                    getService(ConnectionProvider.class).getConnection();
            System.out.println("DB name: " + connection.getMetaData().getDatabaseProductName());
            System.out.println("DB version: " + connection.getMetaData().getDatabaseProductVersion());
            System.out.println("Driver: " + connection.getMetaData().getDriverName());
            System.out.println("Autocommit: " + connection.getAutoCommit());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static SessionFactory createSessionFactory(Configuration configuration) {
        StandardServiceRegistryBuilder builder = new StandardServiceRegistryBuilder();
        builder.applySettings(configuration.getProperties());
        ServiceRegistry serviceRegistry = builder.build();
        return configuration.buildSessionFactory(serviceRegistry);
    }
}