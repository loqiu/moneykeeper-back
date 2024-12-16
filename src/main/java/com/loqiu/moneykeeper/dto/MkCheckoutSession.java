package com.loqiu.moneykeeper.dto;

import java.util.List;
import java.util.Map;
public class MkCheckoutSession {
    private List<String> payment_method_types; // 支持的支付方式
    private List<LineItem> line_items; // 商品列表
    private String mode; // 支付模式
    private String success_url; // 成功跳转的 URL
    private String cancel_url; // 取消跳转的 URL
    private Map<String, String> metadata; // 元数据
    private String customer_email; // 可选字段

    // Getters 和 Setters

    public List<String> getPayment_method_types() {
        return payment_method_types;
    }

    public void setPayment_method_types(List<String> payment_method_types) {
        this.payment_method_types = payment_method_types;
    }

    public List<LineItem> getLine_items() {
        return line_items;
    }

    public void setLine_items(List<LineItem> line_items) {
        this.line_items = line_items;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getSuccess_url() {
        return success_url;
    }

    public void setSuccess_url(String success_url) {
        this.success_url = success_url;
    }

    public String getCancel_url() {
        return cancel_url;
    }

    public void setCancel_url(String cancel_url) {
        this.cancel_url = cancel_url;
    }

    public Map<String, String> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
    }

    public String getCustomer_email() {
        return customer_email;
    }

    public void setCustomer_email(String customer_email) {
        this.customer_email = customer_email;
    }

    public static class LineItem {
        private PriceData price_data;
        private int quantity;

        // Getters 和 Setters
        public static class PriceData {
            private String currency;
            private int unit_amount;
            private ProductData product_data;

            // Getters 和 Setters
            public static class ProductData {
                private String name;
                private String description;

                // Getters 和 Setters

                public String getName() {
                    return name;
                }

                public void setName(String name) {
                    this.name = name;
                }

                public String getDescription() {
                    return description;
                }

                public void setDescription(String description) {
                    this.description = description;
                }
            }

            public String getCurrency() {
                return currency;
            }

            public void setCurrency(String currency) {
                this.currency = currency;
            }

            public int getUnit_amount() {
                return unit_amount;
            }

            public void setUnit_amount(int unit_amount) {
                this.unit_amount = unit_amount;
            }

            public ProductData getProduct_data() {
                return product_data;
            }

            public void setProduct_data(ProductData product_data) {
                this.product_data = product_data;
            }
        }

        public PriceData getPrice_data() {
            return price_data;
        }

        public void setPrice_data(PriceData price_data) {
            this.price_data = price_data;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }

}
