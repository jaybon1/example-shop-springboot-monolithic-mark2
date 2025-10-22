package com.example.shopmark2.product.application.service;

import com.example.shopmark2.product.domain.model.Product;
import com.example.shopmark2.product.domain.repository.ProductRepository;
import com.example.shopmark2.product.presentation.advice.ProductError;
import com.example.shopmark2.product.presentation.advice.ProductException;
import com.example.shopmark2.product.presentation.dto.request.ReqPostProductsDtoV1;
import com.example.shopmark2.product.presentation.dto.request.ReqPutProductsWithIdDtoV1;
import com.example.shopmark2.product.presentation.dto.response.ResGetProductsDtoV1;
import com.example.shopmark2.product.presentation.dto.response.ResGetProductsWithIdDtoV1;
import com.example.shopmark2.product.presentation.dto.response.ResPostProductsDtoV1;
import com.example.shopmark2.product.presentation.dto.response.ResPutProductsWithIdDtoV1;
import com.example.shopmark2.user.domain.model.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductServiceV1 {

    private final ProductRepository productRepository;

    public ResGetProductsDtoV1 getProducts(Pageable pageable, String name) {
        String normalizedName = normalize(name);

        Page<Product> productPage = normalizedName == null
                ? productRepository.findAll(pageable)
                : productRepository.findByNameContainingIgnoreCase(normalizedName, pageable);
        return ResGetProductsDtoV1.builder()
                .productPage(new ResGetProductsDtoV1.ProductPageDto(productPage))
                .build();
    }

    public ResGetProductsWithIdDtoV1 getProductsWithId(UUID productId) {
        Product product = findProductById(productId);
        return ResGetProductsWithIdDtoV1.of(product);
    }

    @Transactional
    public ResPostProductsDtoV1 postProducts(ReqPostProductsDtoV1 reqDto) {
        ReqPostProductsDtoV1.ProductDto reqProduct = reqDto.getProduct();
        String name = reqProduct.getName().trim();
        validateDuplicatedName(name, Optional.empty());

        Product newProduct = Product.builder()
                .name(name)
                .price(reqProduct.getPrice())
                .stock(reqProduct.getStock())
                .build();

        Product savedProduct = productRepository.save(newProduct);
        return ResPostProductsDtoV1.of(savedProduct);
    }

    @Transactional
    public ResPutProductsWithIdDtoV1 putProductsWithId(UUID authUserId, List<String> authUserRoleList, UUID productId, ReqPutProductsWithIdDtoV1 reqDto) {
        validateWriteAuthority(authUserRoleList);
        Product product = findProductById(productId);

        ReqPutProductsWithIdDtoV1.ProductDto reqProduct = reqDto.getProduct();

        String nameToUpdate = null;
        if (reqProduct.getName() != null) {
            String trimmed = reqProduct.getName().trim();
            if (!trimmed.equals(product.getName())) {
                validateDuplicatedName(trimmed, Optional.of(productId));
            }
            nameToUpdate = trimmed;
        }

        Product updatedProduct = product.update(
                nameToUpdate,
                reqProduct.getPrice(),
                reqProduct.getStock()
        );

        Product savedProduct = productRepository.save(updatedProduct);
        return ResPutProductsWithIdDtoV1.of(savedProduct);
    }

    @Transactional
    public void deleteProductsWithId(UUID authUserId, List<String> authUserRoleList, UUID productId) {
        validateWriteAuthority(authUserRoleList);
        Product product = findProductById(productId);
        if (authUserId == null) {
            throw new ProductException(ProductError.PRODUCT_BAD_REQUEST);
        }
        Product deletedProduct = product.markDeleted(Instant.now(), authUserId);
        productRepository.save(deletedProduct);
    }

    private Product findProductById(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ProductException(ProductError.PRODUCT_CAN_NOT_FOUND));
    }

    private void validateDuplicatedName(String name, Optional<UUID> excludeId) {
        productRepository.findByName(name).ifPresent(product -> {
            if (excludeId.isEmpty() || !product.getId().equals(excludeId.get())) {
                throw new ProductException(ProductError.PRODUCT_NAME_DUPLICATED);
            }
        });
    }

    private void validateWriteAuthority(List<String> authUserRoleList) {
        if (authUserRoleList == null) {
            throw new ProductException(ProductError.PRODUCT_FORBIDDEN);
        }
        boolean hasAuthority = authUserRoleList.contains(UserRole.Role.ADMIN.toString())
                || authUserRoleList.contains(UserRole.Role.MANAGER.toString());
        if (!hasAuthority) {
            throw new ProductException(ProductError.PRODUCT_FORBIDDEN);
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

}
