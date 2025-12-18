package com.datn.shopdatabase.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(400, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Invalid message key", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "Username must be at least {min} character", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Password must be at least {min} character", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_DOB(1008, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),
    ROLE_NOT_EXISTED(109,"Role not existed", HttpStatus.NOT_FOUND),
    UNCATEGORIZED(1010,"Chuă phân loại",HttpStatus.BAD_REQUEST),
    PRODUCT_NOT_FOUND(1011,"Product not found",HttpStatus.NOT_FOUND),
    CATEGORY_NOT_FOUND(1012,"Category not found",HttpStatus.NOT_FOUND),
    INVALID_REQUEST(1013,"Request invalid",HttpStatus.BAD_REQUEST),
    ORDER_NOT_FOUND(1014,"Order not found",HttpStatus.NOT_FOUND),
    INVALID_ORDER_REQUEST(1015,"Invalid order status: ",HttpStatus.NOT_FOUND),
    INVALID_PAYMENT_REQUEST(1016,"Invalid payment status: ",HttpStatus.NOT_FOUND),
    SLUG_EXISTED(1017,"Slug already exists",HttpStatus.BAD_REQUEST),
    PAGE_NOT_FOUND(1018,"Page not found",HttpStatus.NOT_FOUND),
    INVALID_INPUT(1019,"Input invalid",HttpStatus.BAD_REQUEST),
    FILE_UPLOAD_FAILED(1020,"Input invalid",HttpStatus.BAD_REQUEST),
    MEDIA_NOT_FOUND(1021,"Media not found",HttpStatus.NOT_FOUND),
    UNABLE_UPLOAD_FILE(1022,"Unable to upload file",HttpStatus.BAD_REQUEST),
    CART_EMPTY(1023,"Cart hasn't any thing",HttpStatus.BAD_REQUEST),
    CANNOT_CANCEL_ORDER(1024, "Không thể hủy đơn hàng ở trạng thái hiện tại",HttpStatus.NOT_FOUND),
    FORBIDDEN(1025, "Không đủ quyền truy cập",HttpStatus.FORBIDDEN),
    ;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;


}

