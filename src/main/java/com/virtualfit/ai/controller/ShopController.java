package com.virtualfit.ai.controller;

import com.virtualfit.ai.model.Product;
import com.virtualfit.ai.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ShopController {

    private final ProductService productService;

    public ShopController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/shop")
    public String shopPage(
            @RequestParam(required = false, defaultValue = "All")
            String category,

            @RequestParam(required = false)
            String search,

            Model model) {

        List<Product> products;

        if ("All".equalsIgnoreCase(category)) {

            products = productService.getAllProducts();

        } else {

            products = productService.getProductsByCategory(category);
        }


        // SEARCH
        if (search != null && !search.trim().isEmpty()) {

            String lowercaseSearch =
                    search.trim().toLowerCase();

            products = products.stream()
                    .filter(product ->
                            product.getName()
                                    .toLowerCase()
                                    .contains(lowercaseSearch)
                            ||
                            product.getCategory()
                                    .toLowerCase()
                                    .contains(lowercaseSearch)
                    )
                    .collect(Collectors.toList());
        }


        // ONLY SUPPORTED SHOP CATEGORIES
        List<String> categories = Arrays.asList(
                "All",
                "T-Shirts",
                "Shirts",
                "Tops",
                "Jackets",
                "Sweaters"
        );


        model.addAttribute("products", products);

        model.addAttribute(
                "categories",
                categories
        );

        model.addAttribute(
                "activeCategory",
                category
        );

        model.addAttribute(
                "searchQuery",
                search != null ? search : ""
        );


        return "shop";
    }
}