package com.hls.sunflower.service.serviceImpl;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.hls.sunflower.dao.*;
import com.hls.sunflower.dao.specification.CartItemSpecification;
import com.hls.sunflower.dto.request.CartItemQuantityRequest;
import com.hls.sunflower.dto.request.CartRequest;
import com.hls.sunflower.dto.response.CartItemResponse;
import com.hls.sunflower.dto.response.CartResponse;
import com.hls.sunflower.entity.*;
import com.hls.sunflower.exception.AppException;
import com.hls.sunflower.exception.ErrorCode;
import com.hls.sunflower.mapper.CartItemMapper;
import com.hls.sunflower.mapper.CartMapper;
import com.hls.sunflower.service.CartService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final UsersRepository usersRepository;
    private final ProductItemRepository productItemRepository;
    private final CartItemRepository cartItemRepository;
    private final CartMapper cartMapper;
    private final CartItemMapper cartItemMapper;

    @Override
    public Page<CartItemResponse> getCartItems(String field, Integer pageNumber, Integer pageSize, String sort) {
        Specification<CartItem> specs = Specification.where(null);

        Users user = getMyInfo();
        specs = specs.and(CartItemSpecification.equalUserId(user.getId()));

        Sort sortable = sort.equalsIgnoreCase("ASC")
                ? Sort.by(field).ascending()
                : Sort.by(field).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortable);

        return cartItemRepository.findAll(specs, pageable).map(cartItemMapper::toCartItemResponse);
    }

    @Override
    public CartResponse getById(String id) {
        Cart cart = cartRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.CART_NOT_EXISTED));

        CartResponse cartResponse = cartMapper.toCartResponse(cart);
        Set<CartItemResponse> cartItemResponses = cart.getCartItems().stream()
                .map(cartItemMapper::toCartItemResponse)
                .collect(Collectors.toSet());
        cartResponse.setCartItems(cartItemResponses);
        return cartResponse;
    }

    @Override
    public CartResponse addCart(CartRequest request) {
        Users user = getMyInfo();
        Optional<Cart> cartOptional = cartRepository.findByUserId(user.getId());
        ProductItem productItem = productItemRepository
                .findById(request.getCartItem().getProductItemId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_ITEM_NOT_EXISTED));
        Cart cart = new Cart();
        CartItem cartItem = new CartItem();
        if (cartOptional.isEmpty()) {
            cart.setUser(user);
            if (request.getCartItem().getQuantity() > productItem.getStockQuantity()) {
                throw new AppException(ErrorCode.PRODUCT_OUT_OF_STOCK);
            }
            cartItem.setQuantity(request.getCartItem().getQuantity());
            cartItem.setProductItem(productItem);
            cartItem.setCart(cart);

            if (cart.getCartItems() == null) {
                Set<CartItem> cartItems = new HashSet<>();
                cartItems.add(cartItem);
                cart.setCartItems(cartItems);
            } else {
                cart.getCartItems().add(cartItem);
            }

        } else {
            cart = cartOptional.get();

            // neu product item da duoc them truoc do thi tang quantity cua cart item
            Optional<CartItem> cartItemOptional =
                    cartItemRepository.findByCart_IdAndProductItem_Id(cart.getId(), productItem.getId());
            if (cartItemOptional.isPresent()) {
                cartItem = cartItemOptional.get();
                int quantity = cartItem.getQuantity() + request.getCartItem().getQuantity();
                if (quantity > productItem.getStockQuantity()) {
                    throw new AppException(ErrorCode.PRODUCT_OUT_OF_STOCK);
                }
                cartItem.setQuantity(quantity);

                CartItem finalCartItem = cartItem;
                cart.getCartItems().stream()
                        .filter(item -> item.getId().equals(finalCartItem.getId()))
                        .findFirst()
                        .ifPresent(item -> item.setQuantity(finalCartItem.getQuantity()));
            } else {
                if (request.getCartItem().getQuantity() > productItem.getStockQuantity()) {
                    throw new AppException(ErrorCode.PRODUCT_OUT_OF_STOCK);
                }
                cartItem.setQuantity(request.getCartItem().getQuantity());
                cartItem.setProductItem(productItem);
                cartItem.setCart(cart);
                cart.getCartItems().add(cartItem);
            }
        }

        cartRepository.save(cart);
        CartResponse cartResponse = cartMapper.toCartResponse(cart);
        Set<CartItemResponse> cartItemResponses = cart.getCartItems().stream()
                .map(cartItemMapper::toCartItemResponse)
                .collect(Collectors.toSet());
        cartResponse.setCartItems(cartItemResponses);
        return cartResponse;
    }

    @Override
    public CartItemResponse updateCartItemQuantity(String cartItemId, CartItemQuantityRequest request) {
        CartItem cartItem = cartItemRepository
                .findById(cartItemId)
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_EXISTED));
        ProductItem productItem = productItemRepository
                .findById(cartItem.getProductItem().getId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_EXISTED));
        if (request.getQuantity() > productItem.getStockQuantity()) {
            throw new AppException(ErrorCode.PRODUCT_OUT_OF_STOCK);
        }
        cartItem.setQuantity(request.getQuantity());
        return cartItemMapper.toCartItemResponse(cartItemRepository.save(cartItem));
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
