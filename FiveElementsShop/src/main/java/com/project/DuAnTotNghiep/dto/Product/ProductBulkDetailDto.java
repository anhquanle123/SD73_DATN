package com.project.DuAnTotNghiep.dto.Product;

import lombok.Data;
import java.util.List;

@Data
public class ProductBulkDetailDto {
    private Long productId;          // id sản phẩm cha
    private List<Long> colorIds;     // danh sách id màu được chọn
    private List<Long> sizeIds;      // danh sách id size được chọn
    private Integer quantity;        // số lượng chung
    private Double price;            // giá bán chung
}