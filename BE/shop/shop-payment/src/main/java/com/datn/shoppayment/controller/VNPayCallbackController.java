package com.datn.shoppayment.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;


@RestController
@RequestMapping("/api/payments/vnpay")
@RequiredArgsConstructor
public class VNPayCallbackController {
    
    @GetMapping("/return")
    public void handleReturn(HttpServletRequest request, HttpServletResponse response) throws IOException {

        String responseCode = request.getParameter("vnp_ResponseCode");
        String txnRef = request.getParameter("vnp_TxnRef");

        response.sendRedirect(
                "http://localhost:4200/payment-result" +
                        "?success=" + ("00".equals(responseCode)) +
                        "&orderId=" + txnRef +
                        "&transactionId=" + txnRef
        );
    }

}




