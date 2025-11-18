package com.datn.shopauth.controller;

import com.datn.shopauth.dto.request.AuthenticationRequest;
import com.datn.shopauth.dto.response.AuthenticationResponse;
import com.datn.shopcore.dto.ApiResponse;
import com.datn.shopauth.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/token")
    public ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        AuthenticationResponse result = authenticationService.authenticate(request);
        return ApiResponse.<AuthenticationResponse>builder().result(result).build();
    }

}
