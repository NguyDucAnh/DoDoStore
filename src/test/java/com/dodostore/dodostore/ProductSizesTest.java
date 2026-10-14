package com.dodostore.dodostore;

import com.dodostore.dodostore.entity.CartEntity;
import com.dodostore.dodostore.entity.ProductEntity;
import com.dodostore.dodostore.utils.ProductSizes;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProductSizesTest {
    @Test void normalizesAndDeduplicatesSizes() {
        assertEquals("S,M,XL", ProductSizes.normalize(" s, M, s, xl "));
        assertEquals("38,39,40", ProductSizes.normalize("38,39,40"));
    }
    @Test void rejectsEmptyAndOversizedLabels() {
        assertThrows(IllegalArgumentException.class, () -> ProductSizes.normalize(" , "));
        assertThrows(IllegalArgumentException.class, () -> ProductSizes.normalize("X".repeat(31)));
    }
    @Test void rejectsLegacyOrRemovedSizeAtCheckout() {
        ProductEntity product = new ProductEntity();
        product.setSizes("S,M");
        CartEntity cart = new CartEntity();
        cart.setProduct(product);
        product.setColor("Đen");
        cart.setColor("Đen");
        assertThrows(IllegalArgumentException.class, () -> ProductSizes.requireSelection(cart));
        cart.setSize("L");
        assertThrows(IllegalArgumentException.class, () -> ProductSizes.requireSelection(cart));
        cart.setSize("M");
        assertDoesNotThrow(() -> ProductSizes.requireSelection(cart));
    }
}
