package com.hls.sunflower.service.serviceImpl;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.hls.sunflower.dao.*;
import com.hls.sunflower.dto.request.CartItemQuantityRequest;
import com.hls.sunflower.dto.request.CartRequest;
import com.hls.sunflower.dto.response.CartItemResponse;
import com.hls.sunflower.dto.response.CartResponse;
import com.hls.sunflower.entity.*;
import com.hls.sunflower.exception.AppException;
import com.hls.sunflower.exception.ErrorCode;
import com.hls.sunflower.mapper.CartMapper;
import com.hls.sunflower.service.CartService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final UsersRepository usersRepository;
    private final CartItemRepository cartItemRepository;
    private final CartMapper cartMapper;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;

    @Override
    public Page<CartItemResponse> getCartItems(String field, Integer pageNumber, Integer pageSize, String sort) {
        Users user = getMyInfo();

        Sort sortable = sort.equalsIgnoreCase("ASC")
                ? Sort.by(field).ascending()
                : Sort.by(field).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortable);

        Page<CartItem> cartItems = cartItemRepository.findCartItemsWithProductAndImagesByUserId(user.getId(), pageable);

        return cartItems.map(this::mapToCartItemResponse);
    }

    private CartItemResponse mapToCartItemResponse(CartItem cartItem) {
        if (cartItem == null) {
            return null;
        }

        Product product = cartItem.getProduct();
        ProductVariant variant = cartItem.getProductVariant();
        String thumbnailUrl = "/noavatar.png";
        Double price = 0.0;
        String size = null;
        Integer availableStock = 0;
        String productName = "";

        if (product != null) {
            productName = product.getName();
            if (product.getProductImages() != null
                    && !product.getProductImages().isEmpty()) {
                thumbnailUrl = product.getProductImages().get(0).getImageUrl();
            }
        }

        if (variant != null) {
            price = variant.getPrice();
            size = variant.getSize();
            availableStock = variant.getStock();
        }

        return CartItemResponse.builder()
                .id(cartItem.getId())
                .quantity(cartItem.getQuantity())
                .thumbnailUrl(thumbnailUrl)
                .price(price)
                .productId(product != null ? product.getId() : null)
                .productName(productName)
                .variantId(variant != null ? variant.getId() : null)
                .size(size)
                .availableStock(availableStock)
                .build();
    }

    @Override
    public CartResponse getById(String id) {
        Cart cart = cartRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.CART_NOT_EXISTED));

        CartResponse cartResponse = cartMapper.toCartResponse(cart);
        Set<CartItemResponse> cartItemResponses =
                cart.getCartItems().stream().map(this::mapToCartItemResponse).collect(Collectors.toSet());
        cartResponse.setCartItems(cartItemResponses);
        return cartResponse;
    }

    @Override
    public CartResponse addCart(CartRequest request) {
        Users user = getMyInfo();
        Cart cart = cartRepository.findByUserId(user.getId()).orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setUser(user);
            return cartRepository.save(newCart);
        });

        // Support both product variant ID and product ID for backward compatibility
        String productVariantId = request.getCartItem().getProductVariantId();
        String productId = request.getCartItem().getProductId();

        ProductVariant variant = null;
        Product product = null;

        if (productVariantId != null && !productVariantId.isEmpty()) {
            // Add by specific variant (preferred method)
            variant = productVariantRepository
                    .findById(productVariantId)
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));
            product = variant.getProduct();
        } else if (productId != null && !productId.isEmpty()) {
            // Add by product ID (for backward compatibility)
            product = productRepository
                    .findById(productId)
                    .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));
            // Use first variant if available
            if (!product.getVariants().isEmpty()) {
                variant = product.getVariants().get(0);
            }
        } else {
            throw new AppException(ErrorCode.PRODUCT_NOT_EXISTED);
        }

        // Check stock availability
        if (variant != null && request.getCartItem().getQuantity() > variant.getStock()) {
            throw new AppException(ErrorCode.PRODUCT_OUT_OF_STOCK);
        }

        // Create final copies for use in lambda expressions
        final ProductVariant finalVariant = variant;
        final Product finalProduct = product;

        // Check if same variant already exists in cart
        Optional<CartItem> existingCartItem = cart.getCartItems().stream()
                .filter(item -> finalVariant != null
                        ? (item.getProductVariant() != null
                                && item.getProductVariant().getId().equals(finalVariant.getId()))
                        : item.getProduct().getId().equals(finalProduct.getId()))
                .findFirst();

        if (existingCartItem.isPresent()) {
            CartItem cartItem = existingCartItem.get();
            int newQuantity = cartItem.getQuantity() + request.getCartItem().getQuantity();

            // Check stock for updated quantity
            if (variant != null && newQuantity > variant.getStock()) {
                throw new AppException(ErrorCode.PRODUCT_OUT_OF_STOCK);
            }

            cartItem.setQuantity(newQuantity);
            cartItemRepository.save(cartItem);
        } else {
            CartItem newCartItem = new CartItem();
            newCartItem.setCart(cart);
            newCartItem.setProduct(product);
            newCartItem.setProductVariant(variant);
            newCartItem.setQuantity(request.getCartItem().getQuantity());
            cart.getCartItems().add(newCartItem);
            cartItemRepository.save(newCartItem);
        }

        // Refresh cart entity to get the updated state
        cart = cartRepository.findById(cart.getId()).orElseThrow(() -> new AppException(ErrorCode.CART_NOT_EXISTED));

        CartResponse cartResponse = cartMapper.toCartResponse(cart);
        Set<CartItemResponse> cartItemResponses =
                cart.getCartItems().stream().map(this::mapToCartItemResponse).collect(Collectors.toSet());
        cartResponse.setCartItems(cartItemResponses);
        return cartResponse;
    }

    @Override
    public CartItemResponse updateCartItemQuantity(String cartItemId, CartItemQuantityRequest request) {
        CartItem cartItem = cartItemRepository
                .findCartItemWithProductAndImagesById(cartItemId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_EXISTED));

        ProductVariant variant = cartItem.getProductVariant();

        // Check stock availability against the variant stock
        if (variant != null && request.getQuantity() > variant.getStock()) {
            throw new AppException(ErrorCode.PRODUCT_OUT_OF_STOCK);
        } else if (variant == null) {
            // Fallback for cart items without variants (backward compatibility)
            throw new AppException(ErrorCode.PRODUCT_NOT_EXISTED);
        }

        cartItem.setQuantity(request.getQuantity());
        CartItem updatedCartItem = cartItemRepository.save(cartItem);

        // Reload with product and images to ensure complete data
        updatedCartItem = cartItemRepository
                .findCartItemWithProductAndImagesById(updatedCartItem.getId())
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_EXISTED));

        return mapToCartItemResponse(updatedCartItem);
    }

    @Override
    public void deleteCartItem(String cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }

    public Users getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        return usersRepository.findByUsername(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }
}
