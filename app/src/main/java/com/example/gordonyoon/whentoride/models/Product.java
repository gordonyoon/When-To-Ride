package com.example.gordonyoon.whentoride.models;

import com.google.gson.annotations.SerializedName;

public class Product {

    @SerializedName("capacity")
    private Integer capacity;
    @SerializedName("product_id")
    private String productId;
    @SerializedName("price_details")
    private Object priceDetails;
    @SerializedName("image")
    private String image;
    @SerializedName("display_name")
    private String displayName;
    @SerializedName("description")
    private String description;

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Object getPriceDetails() {
        return priceDetails;
    }

    public void setPriceDetails(Object priceDetails) {
        this.priceDetails = priceDetails;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
