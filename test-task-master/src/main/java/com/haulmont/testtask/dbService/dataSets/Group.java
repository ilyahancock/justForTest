package com.haulmont.testtask.dbService.dataSets;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "group_table", schema = "")
public class Group implements Serializable {
    @Id
    @Column(name = "gid", nullable = false, insertable = true, updatable = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "number", nullable = false,  unique = true, insertable = true, updatable = false, length = 5)
    private String number;

    @Column(name = "name", nullable = false,  unique = false, insertable = true, updatable = true, length = 20)
    private String name;

    @Column(name = "setstudents")
    @ElementCollection(targetClass=Student.class)
    @OneToMany(mappedBy = "groupEntity", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Student> setStudents = new HashSet<Student>();

    public Set<Student> getStudents() {
        return this.setStudents;
    }

    public void setStudents(Set<Student> students) {
        this.setStudents = students;
    }

    public void addStudent(Student student) {
        student.setGroup(this);
        getStudents().add(student);
    }

    //Important to Hibernate!
    @SuppressWarnings("UnusedDeclaration")
    public Group() {
    }

    @SuppressWarnings("UnusedDeclaration")
    public Group(long id, String number, String name) {
        this.setId(id);
        this.setNumber(number);
        this.setName(name);
    }

    public Group(String number, String name) {
        this.setId(-1);
        this.setNumber(number);
        this.setName(name);
    }

    @SuppressWarnings("UnusedDeclaration")
    public String getNumber() {
        return number;
    }

    public void setNumber( String number ) { this.number = number; }

    @SuppressWarnings("UnusedDeclaration")
    public String getName() {
        return name;
    }

    public void setName(String name) {  this.name = name; }

    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Group group = (Group) o;

        return number.equals(group.number);
    }

    @Override
    public int hashCode() {
        return number.hashCode();
    }

    @Override
    public String toString() {
        return "Group{" +
                "id=" + id +
                ", number='" + number + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}