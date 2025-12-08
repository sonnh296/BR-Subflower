package com.hls.sunflower.mapper;

import java.sql.Timestamp;

import org.springframework.stereotype.Component;

import com.hls.sunflower.dto.response.CartItemResponse;
import com.hls.sunflower.entity.CartItem;
import com.hls.sunflower.entity.Product;
import com.hls.sunflower.entity.ProductVariant;
import com.hls.sunflower.util.TimestampUtil;

@Component
public class CartItemMapper {

    public CartItemResponse toCartItemResponse(CartItem cartItem) {
        if (cartItem == null) {
            return null;
        }

        Product product = cartItem.getProduct();
        ProductVariant variant = cartItem.getProductVariant();

        CartItemResponse.CartItemResponseBuilder builder = CartItemResponse.builder();

        builder.id(cartItem.getId());
        builder.quantity(cartItem.getQuantity());
        builder.addedAt(timestampToString(cartItem.getAddedAt()));
        builder.thumbnailUrl(getProductThumbnailUrl(product));
        builder.price(getVariantPrice(variant));

        // Add product and variant information
        if (product != null) {
            builder.productId(product.getId());
            builder.productName(product.getName());
        }

        if (variant != null) {
            builder.variantId(variant.getId());
            builder.size(variant.getSize() != null ? variant.getSize().getName() : null);
            builder.availableStock(variant.getStock());
        }

        return builder.build();
    }

    private String timestampToString(Timestamp timestamp) {
        return TimestampUtil.timestampToString(timestamp);
    }

    private String getProductThumbnailUrl(Product product) {
        if (product == null) {
            return "/noavatar.png";
        }

        if (product.getProductImages() == null || product.getProductImages().isEmpty()) {
            return "/noavatar.png";
        }

        return product.getProductImages().get(0).getImageUrl();
    }

    private Double getVariantPrice(ProductVariant variant) {
        if (variant == null) {
            return 0.0;
        }
        return variant.getPrice();
    }
}
