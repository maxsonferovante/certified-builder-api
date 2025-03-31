package com.maal.certifiedbuilderapi.api.certified;

import com.maal.certifiedbuilderapi.business.dto.BuildOrdersRequest;
import com.maal.certifiedbuilderapi.business.dto.BuildOrdersResponse;
import com.maal.certifiedbuilderapi.business.dto.CertificateStatisticsResponse;
import com.maal.certifiedbuilderapi.business.dto.DeleteProductResponse;
import com.maal.certifiedbuilderapi.business.dto.RecoverCertificatesResponse;
import com.maal.certifiedbuilderapi.business.usecase.certificate.CertificateConstructionOrder;
import com.maal.certifiedbuilderapi.business.usecase.certificate.DeleteProduct;
import com.maal.certifiedbuilderapi.business.usecase.certificate.GetCertificateStatistics;
import com.maal.certifiedbuilderapi.business.usecase.certificate.RecoverCertificates;
import com.maal.certifiedbuilderapi.infrastructure.client.TechFloripa;
import com.maal.certifiedbuilderapi.infrastructure.client.response.TechOrdersResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/certified")
@RequiredArgsConstructor
public class Certified {

    private final CertificateConstructionOrder certificateConstructionOrder;
    private final RecoverCertificates recoverCertificates;
    private final DeleteProduct deleteProduct;
    private final GetCertificateStatistics getCertificateStatistics;
    private final TechFloripa techFloripa;

    @GetMapping("/test")
    public ResponseEntity<List<TechOrdersResponse>> test(@RequestParam Integer productId) {
        return ResponseEntity.ok(techFloripa.getOrders(productId));
    }

    @PostMapping("/build-orders")
    public ResponseEntity<BuildOrdersResponse> buildOrders(@RequestBody BuildOrdersRequest request) {
        return ResponseEntity.ok(certificateConstructionOrder.execute(request));
    }

    @GetMapping("/recover-certificates")
    public ResponseEntity<List<RecoverCertificatesResponse>> recoverCertificates(@RequestParam Integer productId) {
        return ResponseEntity.ok(recoverCertificates.execute(productId));
    }

    @DeleteMapping("/product")
    public ResponseEntity<DeleteProductResponse> deleteProduct(@RequestParam Integer productId) {
        return ResponseEntity.ok(deleteProduct.execute(productId));
    }

    @GetMapping("/statistics")
    public ResponseEntity<CertificateStatisticsResponse> getCertificateStatistics(@RequestParam Integer productId) {
        return ResponseEntity.ok(getCertificateStatistics.execute(productId));
    }
}
