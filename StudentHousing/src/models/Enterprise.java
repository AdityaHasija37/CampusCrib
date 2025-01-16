/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

/**
 *
 * @author nidhinair
 */
import java.util.ArrayList;
import java.util.List;
 
public abstract class Enterprise {
    private String name;
    private List<Organization> organizations;
 
    public Enterprise(String name) {
        this.name = name;
        this.organizations = new ArrayList<>();
    }
 
    public String getName() {
        return name;
    }
 
    public void addOrganization(Organization organization) {
        if (organization != null) {
            organizations.add(organization);
        }
    }
 
    public List<Organization> getOrganizations() {
        return organizations;
    }
}