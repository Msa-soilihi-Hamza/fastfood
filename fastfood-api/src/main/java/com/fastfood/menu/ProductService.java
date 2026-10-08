package com.fastfood.menu;

import com.fastfood.common.ApiException;
import com.fastfood.menu.dto.ProductRequest;
import com.fastfood.menu.dto.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository products;

    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {
        return products.findAllByOrderByCategoryAscNameAsc().stream().map(ProductResponse::from).toList();
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        Product product = new Product();
        apply(product, request);
        return ProductResponse.from(products.save(product));
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = get(id);
        apply(product, request);
        return ProductResponse.from(product);
    }

    @Transactional
    public ProductResponse setAvailable(Long id, boolean available) {
        Product product = get(id);
        product.setAvailable(available);
        return ProductResponse.from(product);
    }

    private Product get(Long id) {
        return products.findById(id).orElseThrow(() -> ApiException.notFound("Plat introuvable : " + id));
    }

    private static void apply(Product product, ProductRequest request) {
        product.setName(request.name().trim());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setCategory(request.category().trim());
        product.setAvailable(request.available());
    }
}
