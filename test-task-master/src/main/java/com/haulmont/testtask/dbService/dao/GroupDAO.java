package com.haulmont.testtask.dbService.dao;

import com.haulmont.testtask.dbService.dataSets.Group;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Order;

import java.util.List;

public class GroupDAO {
    private Session session;

    public GroupDAO(Session session) {
        this.session = session;
    }

    public List<Group> getAll() throws HibernateException{
        Criteria criteria = session.createCriteria(Group.class);
        return (List<Group>) criteria
                .addOrder(Order.asc("number"))
                .list();
    }

    public Group insert(Group gr) throws HibernateException {
        session.save(gr);
        return gr;
    }

    public void update(Group gr) throws HibernateException {
        session.update(gr);
    }

    public void delete(Group gr) throws HibernateException {
        session.delete(gr);
    }
}