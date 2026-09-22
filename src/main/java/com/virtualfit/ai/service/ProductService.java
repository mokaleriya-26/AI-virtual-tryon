package com.virtualfit.ai.service;

import com.virtualfit.ai.model.Product;
import com.virtualfit.ai.repository.ProductRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }


    @PostConstruct
    public void seedData() {
        if (productRepository.count() == 0) {
            List<Product> seedProducts = Arrays.asList(
                createProduct("black-oversized-tshirt", "Black Oversized T-Shirt", "T-Shirts", 999.0, Arrays.asList("S", "M", "L", "XL"), "/images/products/tshirts/black-oversized-tshirt.jpeg"),
                createProduct("white-basic-tshirt", "White Basic T-Shirt", "T-Shirts", 899.0, Arrays.asList("S", "M", "L"), "/images/products/tshirts/white-basic-tshirt.jpg"),
                createProduct("lavender-tshirt", "Lavender T-Shirt", "T-Shirts", 899.0, Arrays.asList("M", "L", "XL"), "/images/products/tshirts/lavender-tshirt.jpg"),
                createProduct("classic-white-shirt", "Classic White Shirt", "Shirts", 1499.0, Arrays.asList("S", "M", "L"), "/images/products/shirts/classic-white-shirt.jpeg"),
                createProduct("navy-casual-shirt", "Navy Casual Shirt", "Shirts", 1599.0, Arrays.asList("M", "L", "XL"), "/images/products/shirts/navy-casual-shirt.webp"),
                createProduct("cream-linen-shirt", "Cream Linen Shirt", "Shirts", 1699.0, Arrays.asList("S", "M", "L", "XL"), "/images/products/shirts/cream-linen-shirt.webp"),
                createProduct("rose-satin-dress", "Rose Satin Dress", "Dresses", 2499.0, Arrays.asList("S", "M"), "/images/products/dresses/rose-satin-dress.jpg"),
                createProduct("black-bodycon-dress", "Black Bodycon Dress", "Dresses", 2299.0, Arrays.asList("S", "M", "L"), "/images/products/dresses/black-bodycon-dress.jpeg"),
                createProduct("floral-summer-dress", "Floral Summer Dress", "Dresses", 2199.0, Arrays.asList("M", "L", "XL"), "/images/products/dresses/floral-summer-dress.jpg"),
                createProduct("burgundy-midi-dress", "Burgundy Midi Dress", "Dresses", 2399.0, Arrays.asList("S", "M", "L"), "/images/products/dresses/burgundy-midi-dress.jpeg"),
                createProduct("vintage-denim-jacket", "Vintage Denim Jacket", "Jackets", 2999.0, Arrays.asList("M", "L", "XL"), "/images/products/jackets/vintage-denim-jacket.jpeg"),
                createProduct("olive-oversized-jacket", "Olive Oversized Jacket", "Jackets", 2799.0, Arrays.asList("S", "M", "L"), "/images/products/jackets/olive-oversized-jacket.jpeg"),
                createProduct("blue-straight-jeans", "Blue Straight Jeans", "Jeans", 1999.0, Arrays.asList("28", "30", "32", "34"), "/images/products/jeans/blue-straight-jeans.jpeg"),
                createProduct("black-wide-leg-pants", "Black Wide-Leg Pants", "Jeans", 1999.0, Arrays.asList("30", "32", "34"), "/images/products/jeans/images.jpeg"),
                createProduct("lavender-crop-top", "Lavender Crop Top", "Tops", 1199.0, Arrays.asList("S", "M"), "/images/products/tops/lavender-crop-top.jpeg"),
                createProduct("white-ribbed-top", "White Ribbed Top", "Tops", 1099.0, Arrays.asList("S", "M", "L"), "/images/products/tops/white-ribbed-top.jpeg"),
                createProduct("chocolate-brown-top", "Chocolate Brown Top", "Tops", 1299.0, Arrays.asList("S", "M", "L"), "/images/products/tops/chocolate-brown-top.jpeg"),
                createProduct("beige-knit-sweater", "Beige Knit Sweater", "Sweaters", 1899.0, Arrays.asList("M", "L", "XL"), "/images/products/sweaters/beige-knit-sweater.jpeg")
            );
            productRepository.saveAll(seedProducts);
        }
    }

    private Product createProduct(String id, String name, String category, Double price, List<String> sizes, String imageUrl) {
        Product p = new Product();
        p.setId(id);
        p.setName(name);
        p.setCategory(category);
        p.setPrice(price);
        p.setSizes(sizes);
        p.setColors(Arrays.asList("Default"));
        p.setImageUrl(imageUrl);
        return p;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategoryIgnoreCase(category);
    }
}
