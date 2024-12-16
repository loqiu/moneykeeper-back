package com.loqiu.moneykeeper.service;

import com.loqiu.moneykeeper.dto.MkCheckoutSession;
import com.loqiu.moneykeeper.dto.MkPaymentIntentDTO;
import com.loqiu.moneykeeper.response.MkApiResponse;

public interface PaymentStripeService {
    MkApiResponse<MkPaymentIntentDTO> createPaymentIntent(Long var1, String var2);

    MkApiResponse<MkPaymentIntentDTO> confirmPaymentIntent(String var1);

    MkApiResponse<MkPaymentIntentDTO> cancelPaymentIntent(String var1);

    MkApiResponse<MkPaymentIntentDTO> createCheckoutSession(MkCheckoutSession var1);
}
