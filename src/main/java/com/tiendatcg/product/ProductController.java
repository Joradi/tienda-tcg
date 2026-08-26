package com.tiendatcg.product;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ProductResponseDto createProduct(@Valid  @RequestBody ProductCreateRequest request)
    {
        Product product = productService.createProduct(request);

        return toResponseDto(product);
    }

    @GetMapping("/{id}")
    public ProductResponseDto getProductById(@PathVariable Long id)
    {
        Product product = productService.getProductById(id);

        return toResponseDto(product);
    }

    @GetMapping("/card/{cardId}")
    public List<ProductResponseDto> getProductsByCardId(@PathVariable Long cardId)
    {
        return productService.getProductsByCardId(cardId)
                .stream()
                .map(this::toResponseDto)
                .toList();

    }

    @GetMapping("/available")
    public List<ProductResponseDto> getAvailableProducts()
    {
        return productService.getAvailableProducts()
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @GetMapping
    public List<ProductResponseDto> getAllProducts()
    {
        return productService.getAllProducts()
                .stream()
                .map(this::toResponseDto)
                .toList();
    }
    @GetMapping("/card/{cardId}/available")
    public List<ProductResponseDto> getAvailableProductsByCardId(@PathVariable Long cardId)
    {
        return productService.getAvailableProductsByCardId(cardId)
                .stream()
                .map(this::toResponseDto)
                .toList();
    }

    @PatchMapping("/{id}")
    public ProductResponseDto updateProduct(@PathVariable Long id,
                                            @Valid @RequestBody ProductUpdateRequest request)
    {
        Product product = productService.updateProduct(id, request);

        return toResponseDto(product);
    }

    private ProductResponseDto toResponseDto(Product product)
    {
        return new ProductResponseDto(
                product.getId(),
                product.getCard().getId(),
                product.getCard().getName(),
                product.getCard().getImageUrl(),
                product.getLanguage(),
                product.getVariant(),
                product.getCondition(),
                product.getStock(),
                product.getPrice(),
                product.getLastPriceReview()
        );
    }
}
