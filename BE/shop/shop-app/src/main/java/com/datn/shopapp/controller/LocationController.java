package com.datn.shopapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;


@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
@CrossOrigin
public class LocationController {

    private final RestTemplate restTemplate;

    private static final String BASE_URL = "https://provinces.open-api.vn/api";

    /** Tỉnh / Thành */
    @GetMapping("/provinces")
    public ResponseEntity<?> provinces() {
        return ResponseEntity.ok(
                restTemplate.getForObject(BASE_URL + "/p/", Object.class)
        );
    }

    /** Quận / Huyện theo tỉnh */
    @GetMapping("/districts/{provinceCode}")
    public ResponseEntity<?> districts(@PathVariable int provinceCode) {
        return ResponseEntity.ok(
                restTemplate.getForObject(
                        BASE_URL + "/p/" + provinceCode + "?depth=2",
                        Object.class
                )
        );
    }

    /** Phường / Xã theo quận */
    @GetMapping("/wards/{districtCode}")
    public ResponseEntity<?> wards(@PathVariable int districtCode) {
        return ResponseEntity.ok(
                restTemplate.getForObject(
                        BASE_URL + "/d/" + districtCode + "?depth=2",
                        Object.class
                )
        );
    }
}

