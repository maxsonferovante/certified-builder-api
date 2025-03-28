package com.maal.certifiedbuilderapi.api.certified;


import com.maal.certifiedbuilderapi.business.dto.BuildOrdersRequest;
import com.maal.certifiedbuilderapi.business.dto.BuildOrdersResponse;
import com.maal.certifiedbuilderapi.business.usecases.CertificateConstructionOrder;
import com.maal.certifiedbuilderapi.infrastructure.client.TechFloripa;
import com.maal.certifiedbuilderapi.infrastructure.client.response.TechOrdersResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/certified")
@RequiredArgsConstructor
public class Certified {

    private final TechFloripa techFloripa;
    private final CertificateConstructionOrder  certificateConstructionOrder;

    @GetMapping
    public List<TechOrdersResponse> getOrders(@RequestParam Integer product_id) {
        return techFloripa.getOrders(product_id);
    }

    @PostMapping("/build")
    public ResponseEntity<BuildOrdersResponse> buildOrders(@RequestBody BuildOrdersRequest buildOrdersRequest) {
        return ResponseEntity.ok(certificateConstructionOrder.execute(buildOrdersRequest));
    }

}
