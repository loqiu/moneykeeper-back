package com.loqiu.moneykeeper.service.impl;

import com.alibaba.fastjson.JSON;
import com.loqiu.moneykeeper.constant.KafkaTopicConstant;
import com.loqiu.moneykeeper.dto.MkCheckoutSession;
import com.loqiu.moneykeeper.dto.MkPaymentIntentDTO;
import com.loqiu.moneykeeper.response.MkApiResponse;
import com.loqiu.moneykeeper.response.ResponseConverter;
import com.loqiu.moneykeeper.service.KafkaConsumerService;
import com.loqiu.moneykeeper.service.KafkaProducerService;
import com.loqiu.moneykeeper.service.PaymentStripeService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rochetec.model.dto.PayCheckoutSession;
import org.rochetec.model.dto.PaymentIntentDTO;
import org.rochetec.model.response.PayApiResponse;
import org.rochetec.service.StripePaymentService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PaymentStripeServiceImpl implements PaymentStripeService {
    
    private static final Logger logger = LogManager.getLogger(PaymentStripeServiceImpl.class);

    @DubboReference
    private StripePaymentService paymentService;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Autowired
    private KafkaConsumerService kafkaConsumerService;

    @Override
    public MkApiResponse<MkPaymentIntentDTO> createPaymentIntent(Long amount, String currency) {
        logger.info("创建支付意向 - 金额: {}, 货币: {}", amount, currency);
        
        try {
            kafkaProducerService.sendMessage(KafkaTopicConstant.QUICKSTART_EVENTS, "createPaymentIntent", "amount: " + amount + ", currency: " + currency);
            PayApiResponse<PaymentIntentDTO> response = paymentService.createPaymentIntent(amount, currency);
            
            logger.info("支付意向创建成功 - paymentIntentId: {}", response.getData().getId());
            return  ResponseConverter.convertApiResponse(response, this::convertPaymentIntentDTOToMkPaymentIntentDTO);
        } catch (Exception e) {
            logger.error("创建支付意向失败 - 错误: {}", e.getMessage());
            throw new RuntimeException("创建支付意向失败", e);
        }
    }

    @Override
    public MkApiResponse<MkPaymentIntentDTO> confirmPaymentIntent(String paymentIntentId) {
        logger.info("确认支付 - paymentIntentId: {}", paymentIntentId);
        
        try {
            PayApiResponse<PaymentIntentDTO> response = paymentService.confirmPaymentIntent(paymentIntentId);
            logger.info("支付确认成功 - paymentIntentId: {}", paymentIntentId);
            return ResponseConverter.convertApiResponse(response, this::convertPaymentIntentDTOToMkPaymentIntentDTO);
        } catch (Exception e) {
            logger.error("支付确认失败 - paymentIntentId: {}, 错误: {}", paymentIntentId, e.getMessage());
            throw new RuntimeException("支付确认失败", e);
        }
    }

    @Override
    public MkApiResponse<MkPaymentIntentDTO> cancelPaymentIntent(String paymentIntentId) {
        logger.info("取消支付 - paymentIntentId: {}", paymentIntentId);
        
        try {
            PayApiResponse<PaymentIntentDTO> response = paymentService.cancelPaymentIntent(paymentIntentId);
            logger.info("支付取消成功 - paymentIntentId: {}", paymentIntentId);
            return ResponseConverter.convertApiResponse(response, this::convertPaymentIntentDTOToMkPaymentIntentDTO);
        } catch (Exception e) {
            logger.error("支付取消失败 - paymentIntentId: {}, 错误: {}", paymentIntentId, e.getMessage());
            throw new RuntimeException("支付取消失败", e);
        }
    }

    @Override
    public MkApiResponse<MkPaymentIntentDTO> createCheckoutSession(MkCheckoutSession payload) {
        logger.info("createCheckoutSession - payload: {}", JSON.toJSONString(payload));

        try {
            PayCheckoutSession payCheckoutSession = convertMkCheckoutSessionToPayCheckoutSession(payload);
            logger.info("createCheckoutSession - payCheckoutSession: {}", JSON.toJSONString(payCheckoutSession));
            PayApiResponse<PaymentIntentDTO> response = paymentService.createCheckoutSession(payCheckoutSession);
            logger.info("createCheckoutSession success - paymentIntentId: {}", response.getData().getId());
            return ResponseConverter.convertApiResponse(response, this::convertPaymentIntentDTOToMkPaymentIntentDTO);
        } catch (Exception e) {
            logger.error("createCheckoutSession error - payload: {}, error: {}", JSON.toJSONString(payload), e.getMessage());
            throw new RuntimeException("createCheckoutSession error", e);
        }
    }


    // 将支付服务的DTO转换为本地DTO
    private MkPaymentIntentDTO convertPaymentIntentDTOToMkPaymentIntentDTO(PaymentIntentDTO source) {
        if (source == null) {
            return null;
        }
        MkPaymentIntentDTO target = new MkPaymentIntentDTO();
        BeanUtils.copyProperties(source, target);
        return target;
    }

    private PayCheckoutSession convertMkCheckoutSessionToPayCheckoutSession(MkCheckoutSession source) {
        if (source == null) {
            return null;
        }
        PayCheckoutSession target = new PayCheckoutSession();

        // 使用 BeanUtils 浅拷贝顶层属性
        BeanUtils.copyProperties(source, target);

        // 手动处理嵌套对象
        if (source.getLine_items() != null) {
            List<PayCheckoutSession.LineItem> lineItems = new ArrayList<>();
            for (MkCheckoutSession.LineItem sourceLineItem : source.getLine_items()) {
                PayCheckoutSession.LineItem targetLineItem = new PayCheckoutSession.LineItem();
                BeanUtils.copyProperties(sourceLineItem, targetLineItem);

                // 处理 LineItem 的嵌套对象 PriceData
                if (sourceLineItem.getPrice_data() != null) {
                    PayCheckoutSession.LineItem.PriceData targetPriceData = new PayCheckoutSession.LineItem.PriceData();
                    BeanUtils.copyProperties(sourceLineItem.getPrice_data(), targetPriceData);

                    // 处理 ProductData
                    if (sourceLineItem.getPrice_data().getProduct_data() != null) {
                        PayCheckoutSession.LineItem.PriceData.ProductData targetProductData =
                                new PayCheckoutSession.LineItem.PriceData.ProductData();
                        BeanUtils.copyProperties(sourceLineItem.getPrice_data().getProduct_data(), targetProductData);
                        targetPriceData.setProduct_data(targetProductData);
                    }
                    targetLineItem.setPrice_data(targetPriceData);
                }

                lineItems.add(targetLineItem);
            }
            target.setLine_items(lineItems);
        }

        return target;
    }
}
