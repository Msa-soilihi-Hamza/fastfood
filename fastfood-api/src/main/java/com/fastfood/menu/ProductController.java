package com.fastfood.menu;

import com.fastfood.menu.dto.ProductRequest;
import com.fastfood.menu.dto.ProductResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /** Menu public : les plats épuisés sont renvoyés avec available = false. */
    @GetMapping("/api/products")
    public List<ProductResponse> menu() {
        return productService.findAll();
    }

    @PostMapping("/api/admin/products")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @RequestBody ProductRequest request) {
        return productService.create(request);
    }

    @PutMapping("/api/admin/products/{id}")
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    @PatchMapping("/api/admin/products/{id}/availability")
    public ProductResponse setAvailable(@PathVariable Long id, @RequestParam boolean available) {
        return productService.setAvailable(id, available);
    }
}
