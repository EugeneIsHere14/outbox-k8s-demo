package com.demo.order.client;

import com.demo.order.dto.PaymentDto;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Optional;

@Service
public class PaymentServiceClient {

    private final RestClient paymentRestClient;

    public PaymentServiceClient(RestClient paymentRestClient) {
        this.paymentRestClient = paymentRestClient;
    }

    public Optional<PaymentDto> getPaymentByOrderId(Long id) {
        try {
            PaymentDto paymentDto = paymentRestClient.get()
                    .uri("/payments/order/{orderId}", id)
                    .retrieve()
                    .body(PaymentDto.class);

            return Optional.ofNullable(paymentDto);
        } catch (RestClientResponseException ex) {
            return Optional.empty();
        }
    }
}
