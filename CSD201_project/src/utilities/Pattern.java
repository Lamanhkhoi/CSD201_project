package utilities;

public interface Pattern {
    String ORDER_ID_PATTERN = "^ORD\\d{3}$"; 
    String PHONE_PATTERN = "^0\\d{9}$";       
    String PRODUCT_SKU_PATTERN = "^P\\d{3}$"; // Thêm định dạng mã SKU sản phẩm (Ví dụ: P001, P999)
}