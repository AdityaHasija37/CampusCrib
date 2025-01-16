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
 
public class Network {
    private String name;
    private List<Enterprise> enterprises;
 
    public Network(String name) {
        this.name = name;
        this.enterprises = new ArrayList<>();
    }
 
    public String getName() {
        return name;
    }
 
    public void addEnterprise(Enterprise enterprise) {
        if (enterprise != null) {
            enterprises.add(enterprise);
        }
    }
 
    public List<Enterprise> getEnterprises() {
        return enterprises;
    }
}