package com.maal.certifiedbuilderapi.infrastructure.client;


import com.maal.certifiedbuilderapi.infrastructure.client.response.TechOrdersResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "tech", url = "${url.service.tech}")
public interface TechFloripa {
    @GetMapping("/orders")
    List<TechOrdersResponse> getOrders(@RequestParam Integer product_id);
}
