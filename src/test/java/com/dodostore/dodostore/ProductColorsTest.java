package com.dodostore.dodostore;

import com.dodostore.dodostore.utils.ProductColors;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProductColorsTest {
    @Test void preservesVietnameseNamesAndDeduplicates() {
        assertEquals("Đen,Trắng,Xanh navy", ProductColors.normalize(" Đen,Trắng,đen,Xanh navy "));
        assertEquals("Đen", ProductColors.select("Đen,Trắng", " đen "));
    }
    @Test void rejectsUnknownOrMissingSelection() {
        assertNull(ProductColors.select("Đen,Trắng", "Đỏ"));
        assertNull(ProductColors.select("Đen,Trắng", null));
        assertNull(ProductColors.select(null, "Đen"));
    }
    @Test void validatesDatabaseLengths() {
        assertThrows(IllegalArgumentException.class, () -> ProductColors.normalize(" "));
        assertThrows(IllegalArgumentException.class, () -> ProductColors.normalize("a".repeat(61)));
    }
}
