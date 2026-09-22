package com.virtualfit.ai.service;

import com.virtualfit.ai.model.Product;
import com.virtualfit.ai.repository.ProductRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private static final Set<String> UNSUPPORTED_CATEGORIES =
            Set.of("dresses", "jeans");

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @PostConstruct
    public void seedData() {

        List<Product> seedProducts = Arrays.asList(

                // =========================
                // T-SHIRTS
                // =========================

                createProduct(
                        "turquoise-sports-tshirt",
                        "Turquoise Sports T-Shirt",
                        "T-Shirts",
                        999.0,
                        Arrays.asList("S", "M", "L", "XL"),
                        "/images/products/tshirts/e3d028fac804586eefc0b72d2dd6e845fbbd26d1.jpg.avif"
                ),

                createProduct(
                        "white-graphic-tshirt",
                        "White Graphic T-Shirt",
                        "T-Shirts",
                        1099.0,
                        Arrays.asList("S", "M", "L", "XL"),
                        "/images/products/tshirts/images.jpeg"
                ),

                createProduct(
                        "chicago-23-jersey",
                        "Chicago 23 Jersey T-Shirt",
                        "T-Shirts",
                        1299.0,
                        Arrays.asList("M", "L", "XL"),
                        "/images/products/tshirts/images1.jpeg"
                ),

                createProduct(
                        "orange-v-neck-tshirt",
                        "Orange V-Neck T-Shirt",
                        "T-Shirts",
                        899.0,
                        Arrays.asList("S", "M", "L", "XL"),
                        "/images/products/tshirts/images2.jpeg"
                ),

                createProduct(
                        "black-graphic-tshirt",
                        "Black Graphic T-Shirt",
                        "T-Shirts",
                        999.0,
                        Arrays.asList("S", "M", "L", "XL"),
                        "/images/products/tshirts/images3.jpeg"
                ),

                createProduct(
                        "coral-polo-tshirt",
                        "Coral Polo T-Shirt",
                        "T-Shirts",
                        1199.0,
                        Arrays.asList("S", "M", "L"),
                        "/images/products/tshirts/images4.jpeg"
                ),

                createProduct(
                        "classic-black-tshirt",
                        "Classic Black T-Shirt",
                        "T-Shirts",
                        799.0,
                        Arrays.asList("S", "M", "L", "XL"),
                        "/images/products/tshirts/tshirt.jpeg"
                ),

                createProduct(
                        "black-oversized-tshirt",
                        "Black Oversized T-Shirt",
                        "T-Shirts",
                        999.0,
                        Arrays.asList("S", "M", "L", "XL"),
                        "/images/products/tshirts/black-oversized-tshirt.jpeg"
                ),

                createProduct(
                        "lavender-tshirt",
                        "Lavender T-Shirt",
                        "T-Shirts",
                        899.0,
                        Arrays.asList("S", "M", "L", "XL"),
                        "/images/products/tshirts/lavender-tshirt.jpg"
                ),

                createProduct(
                        "white-basic-tshirt",
                        "White Basic T-Shirt",
                        "T-Shirts",
                        899.0,
                        Arrays.asList("S", "M", "L"),
                        "/images/products/tshirts/white-basic-tshirt.jpg"
                ),


                // =========================
                // SHIRTS
                // =========================

                createProduct(
                        "blue-formal-shirt",
                        "Blue Formal Shirt",
                        "Shirts",
                        1499.0,
                        Arrays.asList("S", "M", "L", "XL"),
                        "/images/products/shirts/1189990-25024701.jpg"
                ),

                createProduct(
                        "floral-womens-shirt",
                        "Floral Printed Shirt",
                        "Shirts",
                        1399.0,
                        Arrays.asList("S", "M", "L"),
                        "/images/products/shirts/901351005_g0.jpg.webp"
                ),

                createProduct(
                        "denim-shirt",
                        "Classic Denim Shirt",
                        "Shirts",
                        1599.0,
                        Arrays.asList("S", "M", "L", "XL"),
                        "/images/products/shirts/images.jpeg"
                ),

                createProduct(
                        "pink-striped-shirt",
                        "Pink Striped Shirt",
                        "Shirts",
                        1299.0,
                        Arrays.asList("S", "M", "L", "XL"),
                        "/images/products/shirts/images2.jpeg"
                ),

                createProduct(
                        "white-company-shirt",
                        "White Formal Shirt",
                        "Shirts",
                        1399.0,
                        Arrays.asList("S", "M", "L", "XL"),
                        "/images/products/shirts/images3.jpeg"
                ),

                createProduct(
                        "classic-white-shirt",
                        "Classic White Shirt",
                        "Shirts",
                        1499.0,
                        Arrays.asList("S", "M", "L"),
                        "/images/products/shirts/classic-white-shirt.jpeg"
                ),

                createProduct(
                        "cream-linen-shirt",
                        "Cream Linen Shirt",
                        "Shirts",
                        1699.0,
                        Arrays.asList("S", "M", "L", "XL"),
                        "/images/products/shirts/cream-linen-shirt.webp"
                ),

                createProduct(
                        "navy-casual-shirt",
                        "Navy Casual Shirt",
                        "Shirts",
                        1599.0,
                        Arrays.asList("M", "L", "XL"),
                        "/images/products/shirts/navy-casual-shirt.webp"
                ),


                // =========================
                // TOPS
                // =========================

                createProduct(
                        "pink-smocked-top",
                        "Pink Smocked Top",
                        "Tops",
                        999.0,
                        Arrays.asList("S", "M", "L"),
                        "/images/products/tops/2KONWrYz_351538d6475e40eca887deae2759be55.jpg.webp"
                ),

                createProduct(
                        "beige-knit-collar-top",
                        "Beige Knit Collar Top",
                        "Tops",
                        1299.0,
                        Arrays.asList("S", "M", "L", "XL"),
                        "/images/products/tops/71ZzwVZFCbL.jpg"
                ),

                createProduct(
                        "burgundy-flared-top",
                        "Burgundy Flared Top",
                        "Tops",
                        1199.0,
                        Arrays.asList("S", "M", "L"),
                        "/images/products/tops/images.jpeg"
                ),

                createProduct(
                        "burgundy-ruched-crop-top",
                        "Burgundy Ruched Crop Top",
                        "Tops",
                        1099.0,
                        Arrays.asList("S", "M", "L"),
                        "/images/products/tops/images2.jpeg"
                ),

                createProduct(
                        "burgundy-ruched-peplum-top",
                        "Burgundy Ruched Peplum Top",
                        "Tops",
                        1199.0,
                        Arrays.asList("S", "M", "L", "XL"),
                        "/images/products/tops/images3.jpeg"
                ),

                createProduct(
                        "purple-cropped-sweatshirt",
                        "Purple Cropped Sweatshirt",
                        "Tops",
                        1399.0,
                        Arrays.asList("S", "M", "L"),
                        "/images/products/tops/images5.jpeg"
                ),

                createProduct(
                        "maroon-button-crop-top",
                        "Maroon Button Crop Top",
                        "Tops",
                        999.0,
                        Arrays.asList("S", "M", "L"),
                        "/images/products/tops/images7.jpeg"
                ),

                createProduct(
                        "olive-knit-sleeve-top",
                        "Olive Knit Sleeve Top",
                        "Tops",
                        1499.0,
                        Arrays.asList("S", "M", "L", "XL"),
                        "/images/products/tops/korean-style-trending-winter-tops-for-women.jpg.webp"
                ),

                createProduct(
                        "chocolate-brown-top",
                        "Chocolate Brown Top",
                        "Tops",
                        1299.0,
                        Arrays.asList("S", "M", "L"),
                        "/images/products/tops/chocolate-brown-top.jpeg"
                ),

                createProduct(
                        "lavender-crop-top",
                        "Lavender Crop Top",
                        "Tops",
                        1199.0,
                        Arrays.asList("S", "M"),
                        "/images/products/tops/lavender-crop-top.jpeg"
                ),

                createProduct(
                        "white-ribbed-top",
                        "White Ribbed Top",
                        "Tops",
                        1099.0,
                        Arrays.asList("S", "M", "L"),
                        "/images/products/tops/white-ribbed-top.jpeg"
                ),


                // =========================
                // SWEATERS
                // =========================

                createProduct(
                        "brown-button-cardigan",
                        "Brown Button Cardigan",
                        "Sweaters",
                        1899.0,
                        Arrays.asList("S", "M", "L"),
                        "/images/products/sweaters/shopping.jpeg"
                ),

                createProduct(
                        "cream-button-cardigan",
                        "Cream Button Cardigan",
                        "Sweaters",
                        1799.0,
                        Arrays.asList("S", "M", "L", "XL"),
                        "/images/products/sweaters/shopping2.jpeg"
                ),

                createProduct(
                        "cream-ribbed-sweater",
                        "Cream Ribbed Sweater",
                        "Sweaters",
                        1999.0,
                        Arrays.asList("S", "M", "L", "XL"),
                        "/images/products/sweaters/shopping3.jpeg"
                ),

                createProduct(
                        "burgundy-cable-knit-sweater",
                        "Burgundy Cable Knit Sweater",
                        "Sweaters",
                        2199.0,
                        Arrays.asList("S", "M", "L"),
                        "/images/products/sweaters/shopping4.jpeg"
                ),

                createProduct(
                        "grey-knit-sweater",
                        "Grey Knit Sweater",
                        "Sweaters",
                        1899.0,
                        Arrays.asList("S", "M", "L", "XL"),
                        "/images/products/sweaters/shopping5.jpeg"
                ),

                createProduct(
                        "grey-textured-sweater",
                        "Grey Textured Sweater",
                        "Sweaters",
                        1999.0,
                        Arrays.asList("S", "M", "L", "XL"),
                        "/images/products/sweaters/shopping7.jpeg"
                ),

                createProduct(
                        "cream-cable-knit-sweater",
                        "Cream Cable Knit Sweater",
                        "Sweaters",
                        2299.0,
                        Arrays.asList("S", "M", "L"),
                        "/images/products/sweaters/beige-knit-sweater.jpeg"
                ),


                // =========================
                // JACKETS
                // =========================

                createProduct(
                        "brown-fleece-jacket",
                        "Brown Fleece Jacket",
                        "Jackets",
                        2499.0,
                        Arrays.asList("S", "M", "L", "XL"),
                        "/images/products/jackets/images.jpeg"
                ),

                createProduct(
                        "black-leather-jacket",
                        "Black Leather Jacket",
                        "Jackets",
                        3999.0,
                        Arrays.asList("S", "M", "L", "XL"),
                        "/images/products/jackets/images2.jpeg"
                ),

                createProduct(
                        "navy-military-denim-jacket",
                        "Navy Military Denim Jacket",
                        "Jackets",
                        3299.0,
                        Arrays.asList("S", "M", "L"),
                        "/images/products/jackets/images3.jpeg"
                ),

                createProduct(
                        "black-denim-jacket",
                        "Black Denim Jacket",
                        "Jackets",
                        2799.0,
                        Arrays.asList("S", "M", "L", "XL"),
                        "/images/products/jackets/images4.jpeg"
                ),

                createProduct(
                        "black-puffer-jacket",
                        "Black Puffer Jacket",
                        "Jackets",
                        3499.0,
                        Arrays.asList("S", "M", "L", "XL"),
                        "/images/products/jackets/images5.jpeg"
                ),

                createProduct(
                        "black-button-blazer",
                        "Black Button Blazer",
                        "Jackets",
                        2999.0,
                        Arrays.asList("S", "M", "L"),
                        "/images/products/jackets/images6.jpeg"
                ),

                createProduct(
                        "olive-oversized-jacket",
                        "Olive Oversized Jacket",
                        "Jackets",
                        2799.0,
                        Arrays.asList("S", "M", "L"),
                        "/images/products/jackets/olive-oversized-jacket.jpeg"
                ),

                createProduct(
                        "vintage-denim-jacket",
                        "Vintage Denim Jacket",
                        "Jackets",
                        2999.0,
                        Arrays.asList("M", "L", "XL"),
                        "/images/products/jackets/vintage-denim-jacket.jpeg"
                )
        );

        /*
         * IMPORTANT:
         * Do not use productRepository.count() == 0.
         *
         * This allows newly added products to be inserted into
         * an already-existing MongoDB database without creating
         * duplicate products.
         */
        seedProducts.forEach(product -> {
            if (!productRepository.existsById(product.getId())) {
                productRepository.save(product);
            }
        });
    }

    private Product createProduct(
            String id,
            String name,
            String category,
            Double price,
            List<String> sizes,
            String imageUrl
    ) {
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
        return productRepository.findAll()
                .stream()
                .filter(product ->
                        !isUnsupportedCategory(product.getCategory()))
                .collect(Collectors.toList());
    }

    public List<Product> getProductsByCategory(String category) {

        if (isUnsupportedCategory(category)) {
            return List.of();
        }

        return productRepository.findByCategoryIgnoreCase(category)
                .stream()
                .filter(product ->
                        !isUnsupportedCategory(product.getCategory()))
                .collect(Collectors.toList());
    }

    private boolean isUnsupportedCategory(String category) {

        return category != null &&
                UNSUPPORTED_CATEGORIES.contains(
                        category.toLowerCase()
                );
    }
}