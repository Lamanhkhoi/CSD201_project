package utilities;

public interface Pattern {
    String ORDER_ID_PATTERN = "^ORD\\d{3}$"; 
    String PRODUCT_SKU_PATTERN = "^P\\d{3}$"; // Thêm định dạng mã SKU sản phẩm (Ví dụ: P001, P999)
    String PHONE_PATTERN = "^0\\d{9}$";  
    
    // Bổ sung cho phân hệ Inventory
    String BATCH_ID_PATTERN = "^BAT\\d{3}$"; // Bắt buộc BAT kèm 3 số (vd: BAT001)
    String SKU_PATTERN = "^[A-Z0-9-]{3,10}$"; // Chữ in hoa, số và dấu gạch ngang, dài 3-10 ký tự (vd: MILK-123)
    
    
}