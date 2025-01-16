/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

/**
 *
 * @author nidhinair
 */
public abstract class Organization {
    private String name;
 
    public Organization(String name) {
        this.name = name;
    }
 
    public String getName() {
        return name;
    }
}
