package com.datn.shoppayment.controller;

import com.datn.shoppayment.service.PaymentService;
import com.datn.shoppayment.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;


@RequiredArgsConstructor
@RestController
@RequestMapping("/api/payments/vnpay")
public class VNPayCallbackController {

    private final PaymentService paymentService;
    private final VNPayService vnPayService;

    @GetMapping(value = "/return",produces = MediaType.APPLICATION_JSON_VALUE)
    public void handleReturn(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        int paymentStatus = vnPayService.orderReturn(request);

        String txnRef = request.getParameter("vnp_TxnRef");

        if (txnRef == null) {
            response.sendRedirect("http://localhost:4200/payment-result?success=false");
            return;
        }

        boolean success = paymentStatus == 1;

        if (success) {
            paymentService.handlePaymentSuccess(txnRef);
        } else {
            paymentService.handlePaymentFail(txnRef);
        }

        String orderId = txnRef.split("_")[0];

        response.sendRedirect(
                "http://localhost:4200/payment-result" +
                        "?success=" + success +
                        "&orderId=" + orderId
        );
    }
}




