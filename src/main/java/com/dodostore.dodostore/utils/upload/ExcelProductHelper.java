package com.dodostore.dodostore.utils.upload;

import com.dodostore.dodostore.entity.BrandEntity;
import com.dodostore.dodostore.entity.CategoryEntity;
import com.dodostore.dodostore.entity.ProductEntity;
import com.dodostore.dodostore.enums.StatusEnum;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class ExcelProductHelper {

    public static String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    public static boolean hasExcelFormat(MultipartFile file) {
        return TYPE.equals(file.getContentType());
    }

    public static List<ProductEntity> excelToProducts(InputStream is) {
        try (Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();

            List<ProductEntity> products = new ArrayList<>();
            int rowNumber = 0;

            while (rows.hasNext()) {
                Row row = rows.next();

                if (rowNumber == 0) {
                    rowNumber++;
                    continue; // bỏ qua tiêu đề
                }

                ProductEntity product = new ProductEntity();
                product.setCreateDate(new Date()); // mặc định ngày tạo
                product.setUpdateDate(new Date()); // mặc định ngày cập nhật

                product.setName(row.getCell(0).getStringCellValue());
                product.setPrice((long) row.getCell(1).getNumericCellValue());
                product.setDiscount((long) row.getCell(2).getNumericCellValue());
                product.setDescription(row.getCell(3).getStringCellValue());
                product.setColor(row.getCell(4).getStringCellValue());
                product.setQuantity((long) row.getCell(5).getNumericCellValue());

                String statusStr = row.getCell(6).getStringCellValue();
                product.setStatus(StatusEnum.valueOf(statusStr));

                String categoryName = row.getCell(7).getStringCellValue();
                CategoryEntity category = new CategoryEntity();
                category.setName(categoryName);
                product.setCategory(category);

                String brandName = row.getCell(8).getStringCellValue();
                BrandEntity brand = new BrandEntity();
                brand.setName(brandName);
                product.setBrand(brand);

                products.add(product);
            }

            return products;

        } catch (IOException e) {
            throw new RuntimeException("Không thể đọc file Excel: " + e.getMessage());
        }
    }
}
