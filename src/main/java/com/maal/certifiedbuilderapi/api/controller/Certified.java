package com.maal.certifiedbuilderapi.api.controller;


import com.maal.certifiedbuilderapi.infrastructure.client.TechFloripa;
import com.maal.certifiedbuilderapi.infrastructure.client.response.TechOrdersResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/certified")
@RequiredArgsConstructor
public class Certified {

    private final TechFloripa techFloripa;

    @GetMapping
    public List<TechOrdersResponse> getOrders(@RequestParam Integer product_id) {
        return techFloripa.getOrders(product_id);
    }
}
