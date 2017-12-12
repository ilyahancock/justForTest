package com.haulmont.testtask.dbService.dao;

import com.haulmont.testtask.dbService.dataSets.Student;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

import java.util.List;

public class StudentDAO {
    private Session session;

    public StudentDAO(Session session) {
        this.session = session;
    }

    public List<Student> getByGroupAndLastname(String grNumber,String lastname) throws HibernateException {
        Criteria criteria = session.createCriteria(Student.class,"student");
        criteria.createAlias("student.groupEntity","group");
        if (!lastname.trim().isEmpty())
            criteria.add(Restrictions.ilike("student.lastname", lastname, MatchMode.ANYWHERE));
        if (!grNumber.trim().isEmpty())
            criteria.add(Restrictions.eq("group.number", grNumber));
        return (List<Student>) criteria.list();
    }

    public Student insert(Student st) throws HibernateException {
        //Student st = new Student(firstname,lastname,patronymicname,date,group);
        session.save(st);
        return st;
    }

    public void update(Student st) throws HibernateException {
        session.update(st);
    }

    public void delete(Student st) throws HibernateException {
        session.delete(st);
    }
}
