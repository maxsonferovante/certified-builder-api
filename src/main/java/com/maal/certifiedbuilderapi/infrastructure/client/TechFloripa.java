package com.maal.certifiedbuilderapi.infrastructure.client;


import com.maal.certifiedbuilderapi.business.dto.RecoverCertificatesResponse;
import com.maal.certifiedbuilderapi.infrastructure.client.response.TechOrdersResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "tech", url = "${url.service.tech}")
public interface TechFloripa {
    @GetMapping("/orders")
    List<TechOrdersResponse> getOrders(@RequestParam Integer product_id);

    @PostMapping("/order")
    void notifiesCertificateGeneration (@RequestBody RecoverCertificatesResponse recoverCertificatesResponse);

    @PostMapping("/orders")
    void notifiesCertificatesGeneration (@RequestBody List<RecoverCertificatesResponse> recoverCertificatesResponses);
}
