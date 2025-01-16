/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package models;

import java.util.Date;
import utilities.UniqueIdGenerator;

/**
 *
 * @author nidhinair
 */
public abstract class WorkRequest {
    private String id;
    private String type;

    public void setId(String id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setResponsibleRole(String responsibleRole) {
        this.responsibleRole = responsibleRole;
    }

    public void setIsRoleRestricted(boolean isRoleRestricted) {
        this.isRoleRestricted = isRoleRestricted;
    }
    private String description;
    private String status;
    private String responsibleRole; // Null if not role-specific
    private boolean isRoleRestricted; // True for maintenance requests
    private String priority;
    private UserAccount createdBy;
    private Date createdDate;
 
    public WorkRequest(String type, String responsibleRole, boolean isRoleRestricted) {
        this.id = UniqueIdGenerator.generateId();
        this.type = type;
        this.description = "";
        this.status = "Pending";
        this.responsibleRole = responsibleRole;
        this.isRoleRestricted = isRoleRestricted;
        this.createdDate = new Date();
    }
 
    public String getId() {
        return id;
    }
 
    public String getType() {
        return type;
    }
 
    public String getDescription() {
        return description;
    }
 
    public void setDescription(String description) {
        this.description = description;
    }
 
    public String getStatus() {
        return status;
    }
 
    public void setStatus(String status) {
        this.status = status;
    }
 
    public String getResponsibleRole() {
        return responsibleRole;
    }
 
    public boolean isRoleRestricted() {
        return isRoleRestricted;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public UserAccount getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UserAccount createdBy) {
        this.createdBy = createdBy;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }
}
