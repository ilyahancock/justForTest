package com.haulmont.testtask.dbService.dataSets;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "student_table", schema = "")
public  class Student implements Serializable {
    @Id
    @Column(name = "sid", nullable = false, insertable = true, updatable = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "firstname", nullable = false, insertable = true, updatable = true,  unique = false, length = 20)
    private String firstname;

    @Column(name = "lastname", nullable = false, insertable = true, updatable = true, unique = false, length = 40)
    private String lastname;

    @Column(name = "patronymicname", insertable = true, updatable = true, unique = false, length = 20)
    private String patronymicname;

    @Column(name = "date", insertable = true, updatable = true, unique = false)
    private Date date;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn( name = "group_id")
    private Group groupEntity;

    public  Group getGroup() {
        return this.groupEntity;
    }

    public void setGroup(Group group) {
        this.groupEntity = group;
    }

    //Important to Hibernate!
    @SuppressWarnings("UnusedDeclaration")
    public Student() {
    }

    @SuppressWarnings("UnusedDeclaration")
    public Student(long id, String firstname, String lastname, String patronymicname, Date date) {
        this.setId(id);
        this.setFirstname(firstname);
        this.setLastname(lastname);
        this.setPatronymicname(patronymicname);
        this.setDate(date);
    }

    public Student(String firstname, String lastname, String patronymicname, Date date, Group group) {
        this.setId(-1);
        this.setFirstname(firstname);
        this.setLastname(lastname);
        this.setPatronymicname(patronymicname);
        this.setDate(date);
        this.setGroup(group);
    }

    @SuppressWarnings("UnusedDeclaration")
    public   String getFirstname() { return firstname; }
    public void setFirstname(String firstname) {  this.firstname = firstname; }

    @SuppressWarnings("UnusedDeclaration")
    public String getLastname() {
        return lastname;
    }
    public void setLastname( String lastname ) { this.lastname = lastname; }

    @SuppressWarnings("UnusedDeclaration")
    public String getPatronymicname() { return patronymicname; }
    public void setPatronymicname( String patronymicname ) { this.patronymicname = patronymicname; }

    @SuppressWarnings("UnusedDeclaration")
    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {  this.date = date; }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", patronymicname='" + patronymicname + '\'' +
                ", date='" + date + '\'' +
               // ", groups='" + groups.toString() + '\'' +
                '}';
    }
}