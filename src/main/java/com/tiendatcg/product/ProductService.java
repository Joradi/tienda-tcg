package com.tiendatcg.product;

import com.tiendatcg.card.Card;
import com.tiendatcg.card.CardNotFoundException;
import com.tiendatcg.card.CardService;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CardService cardService;

    public ProductService(ProductRepository productRepository, CardService cardService) {
        this.productRepository = productRepository;
        this.cardService = cardService;
    }

    private Product saveNewProduct(Product product) {
        Optional<Product> existingProduct = productRepository.findByCardAndLanguageAndVariantAndCondition(
                product.getCard(),
                product.getLanguage(),
                product.getVariant(),
                product.getCondition()
        );

        if (existingProduct.isPresent()) {
            throw new ProductAlreadyExistsException("El producto ya existe");
        }

        return productRepository.save(product);
    }

    public Product createProduct(ProductCreateRequest request) {
        Card card = cardService.findById(request.getCardId())
                .orElseThrow(() -> new CardNotFoundException("Card no encontrada"));

        Product product = new Product(
                card,
                request.getLanguage(),
                request.getVariant(),
                request.getCondition(),
                request.getStock(),
                request.getPrice(),
                LocalDate.now()
        );

        return saveNewProduct(product);
    }

    public List<Product> getAllProducts()
    {
        return productRepository.findAll();
    }

    public Product updateProduct(Long id, ProductUpdateRequest  request)
    {
        Product product = getProductById(id);

        if (request.getStock() != null)
        {
            product.setStock(request.getStock());
        }

        if(request.getPrice() != null)
        {
            product.setPrice(request.getPrice());
            product.setLastPriceReview(LocalDate.now());
        }

        return  productRepository.save(product);
    }

    public Product getProductById(Long id)
    {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Producto no encontrado"));
    }

    public List<Product> getAvailableProducts()
    {
        return productRepository.findByStockGreaterThan(0);
    }

    public List<Product> getProductsByCardId(Long cardId)
    {
        return productRepository.findByCard_Id(cardId);
    }

    public List<Product> getAvailableProductsByCardId(Long cardId)
    {
        return productRepository.findByCard_IdAndStockGreaterThan(cardId, 0);
    }
}
