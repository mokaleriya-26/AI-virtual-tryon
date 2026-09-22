package com.virtualfit.ai.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "products")
public class Product {
    
    @Id
    private String id;
    
    private String name;
    private double price;
    private String category;
    private String imageUrl;
    private java.util.List<String> sizes;
    private java.util.List<String> colors;

    public Product() {}

    public Product(String id, String name, double price, String category, String imageUrl, java.util.List<String> sizes, java.util.List<String> colors) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.imageUrl = imageUrl;
        this.sizes = sizes;
        this.colors = colors;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    
    public java.util.List<String> getSizes() { return sizes; }
    public void setSizes(java.util.List<String> sizes) { this.sizes = sizes; }
    
    public java.util.List<String> getColors() { return colors; }
    public void setColors(java.util.List<String> colors) { this.colors = colors; }

    public static ProductBuilder builder() {
        return new ProductBuilder();
    }

    public static class ProductBuilder {
        private String id;
        private String name;
        private double price;
        private String category;
        private String imageUrl;
        private java.util.List<String> sizes;
        private java.util.List<String> colors;

        public ProductBuilder id(String id) { this.id = id; return this; }
        public ProductBuilder name(String name) { this.name = name; return this; }
        public ProductBuilder price(double price) { this.price = price; return this; }
        public ProductBuilder category(String category) { this.category = category; return this; }
        public ProductBuilder imageUrl(String imageUrl) { this.imageUrl = imageUrl; return this; }
        public ProductBuilder sizes(java.util.List<String> sizes) { this.sizes = sizes; return this; }
        public ProductBuilder colors(java.util.List<String> colors) { this.colors = colors; return this; }
        public Product build() { return new Product(id, name, price, category, imageUrl, sizes, colors); }
    }
}
