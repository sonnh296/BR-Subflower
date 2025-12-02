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
        String thumbnailUrl = "/noavatar.png";
        Double price = 0.0;

        if (product != null) {
            price = product.getPrice();
                    + (product.getProductImages() != null
            if (product.getProductImages() != null
                    && !product.getProductImages().isEmpty()) {
                            : "null"));
                thumbnailUrl = product.getProductImages().get(0).getImageUrl();
                System.out.println("DEBUG: No images found, using default /noavatar.png");
            }
        return CartItemResponse.builder()
            System.out.println("DEBUG: Product is null! Cart item has no associated product!");
        }

        CartItemResponse response = CartItemResponse.builder()
                .id(cartItem.getId())
                .quantity(cartItem.getQuantity())
                .build();

        System.out.println("DEBUG: Final response - thumbnailUrl: " + response.getThumbnailUrl() + ", price: "
                + response.getPrice());
        return response;
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

        String productId = request.getCartItem().getProductId();
        if (productId == null || productId.isEmpty()) {
            throw new AppException(ErrorCode.PRODUCT_NOT_EXISTED);
        }

        Product product = productRepository
                .findById(productId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));

        Optional<CartItem> existingCartItem = cart.getCartItems().stream()
                .filter(item -> item.getProduct().getId().equals(productId))
                .findFirst();

        if (existingCartItem.isPresent()) {
            CartItem cartItem = existingCartItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + request.getCartItem().getQuantity());
            cartItemRepository.save(cartItem);
        } else {
            CartItem newCartItem = new CartItem();
            newCartItem.setCart(cart);
            newCartItem.setProduct(product);
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
        Product product = cartItem.getProduct();
        if (request.getQuantity() > product.getQuantity()) {
            throw new AppException(ErrorCode.PRODUCT_OUT_OF_STOCK);
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
