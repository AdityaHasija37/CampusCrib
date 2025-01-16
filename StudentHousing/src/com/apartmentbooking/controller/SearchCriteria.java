package com.apartmentbooking.controller;

import java.util.List;
import java.util.ArrayList;

public class SearchCriteria {
    private int minBedrooms;
    private double maxPrice;
    private String location;
    private List<String> requiredAmenities;
    
    public SearchCriteria(int minBedrooms, double maxPrice, String location, List<String> requiredAmenities) {
        this.minBedrooms = minBedrooms;
        this.maxPrice = maxPrice;
        this.location = location;
        this.requiredAmenities = requiredAmenities != null ? requiredAmenities : new ArrayList<>();
    }
    
    public int getMinBedrooms() {
        return minBedrooms;
    }
    
    public double getMaxPrice() {
        return maxPrice;
    }
    
    public String getLocation() {
        return location;
    }
    
    public List<String> getRequiredAmenities() {
        return requiredAmenities;
    }
} 