package com.foodredistribution.foodredistribution.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.GeoPointField;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

@Document(indexName = "food_posts")
public class FoodPostDocument {

    @Id
    private String id; // Use String for Elasticsearch IDs typically, but we map it from the DB Long ID

    @Field(type = FieldType.Text)
    private String foodName;

    @Field(type = FieldType.Text)
    private String description;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Integer)
    private Integer quantity;

    @GeoPointField
    private GeoPoint location;

    public FoodPostDocument() {
    }

    public FoodPostDocument(String id, String foodName, String description, String status, GeoPoint location) {
        this.id = id;
        this.foodName = foodName;
        this.description = description;
        this.status = status;
        this.location = location;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
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

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public GeoPoint getLocation() {
        return location;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }
}
