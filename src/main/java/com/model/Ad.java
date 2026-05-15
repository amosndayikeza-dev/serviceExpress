package com.serviceexpress.model;

import java.util.Date;

public class Ad {
    private Long id;
    private String serviceName;
    private String description;
    private Integer price;
    private String location;
    private String phone;
    private String category;
    private String artisanName;
    private Date createdAt;

    // Constructeurs
    public Ad() {}

    public Ad(String serviceName, String description, Integer price,
              String location, String phone, String category, String artisanName) {
        this.serviceName = serviceName;
        this.description = description;
        this.price = price;
        this.location = location;
        this.phone = phone;
        this.category = category;
        this.artisanName = artisanName;
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getPrice() { return price; }
    public void setPrice(Integer price) { this.price = price; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getArtisanName() { return artisanName; }
    public void setArtisanName(String artisanName) { this.artisanName = artisanName; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}