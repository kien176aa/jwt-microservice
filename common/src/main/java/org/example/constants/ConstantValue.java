package org.example.constants;

import java.net.URI;

@Component
public class ConstantValue {

    @Value("${allow.url}")
    public String ALLOW_URL;

    @Value("${url.validate.token}")
    public String URL_VALIDATE_TOKEN;

    @Value("${url.identity.service}")
    public String URL_IDENTITY_SERVICE;

    @Value("${url.product.service}")
    public String URL_PRODUCT_SERVICE;
}