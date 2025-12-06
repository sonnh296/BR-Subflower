package com.hls.sunflower.service.serviceImpl;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hls.sunflower.dao.CartItemRepository;
import com.hls.sunflower.dao.CartRepository;
import com.hls.sunflower.dao.OrderItemRepository;
import com.hls.sunflower.dao.OrderRepository;
import com.hls.sunflower.dao.UsersRepository;
import com.hls.sunflower.dto.request.OrderCreationRequest;
import com.hls.sunflower.dto.request.OrderStatusUpdateRequest;
import com.hls.sunflower.dto.response.OrderResponse;
import com.hls.sunflower.entity.Cart;
import com.hls.sunflower.entity.CartItem;
import com.hls.sunflower.entity.Order;
import com.hls.sunflower.entity.OrderItem;
import com.hls.sunflower.entity.ProductVariant;
import com.hls.sunflower.entity.Users;
import com.hls.sunflower.enums.OrderStatus;
import com.hls.sunflower.exception.AppException;
import com.hls.sunflower.exception.ErrorCode;
import com.hls.sunflower.mapper.OrderMapper;
import com.hls.sunflower.service.OrderService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UsersRepository usersRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderResponse createOrder(OrderCreationRequest request) {
        // Get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Users user = usersRepository
                .findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Get user's cart
        Cart cart = cartRepository
                .findByUserId(user.getId())
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_EXISTED));

        // Check if cart has items
        Set<CartItem> cartItems = cart.getCartItems();
        if (cartItems == null || cartItems.isEmpty()) {
            throw new AppException(ErrorCode.CART_IS_EMPTY);
        }

        // Calculate total price
        double totalPrice = cartItems.stream()
                .mapToDouble(item -> {
                    ProductVariant variant = item.getProductVariant();
                    if (variant != null) {
                        return variant.getPrice() * item.getQuantity();
                    } else {
                        // Fallback for cart items without variants (backward compatibility)
                        // You might want to handle this case differently based on your requirements
                        throw new AppException(ErrorCode.PRODUCT_NOT_EXISTED);
                    }
                })
                .sum();

        // Create order
        Order order = Order.builder()
                .user(user)
                .totalPrice(totalPrice)
                // Since there is no online payment integrated, mark orders as PLACED when user creates them
                .status(OrderStatus.PLACED)
                .deliveryAddress(request.getDeliveryAddress())
                .phoneNumber(request.getPhoneNumber())
                .notes(request.getNotes())
                .build();

        // Create order items from cart items
        Set<OrderItem> orderItems = new HashSet<>();
        for (CartItem cartItem : cartItems) {
            ProductVariant variant = cartItem.getProductVariant();
            double priceAtOrder = 0.0;

            if (variant != null) {
                priceAtOrder = variant.getPrice();
            } else {
                // Fallback for cart items without variants (backward compatibility)
                throw new AppException(ErrorCode.PRODUCT_NOT_EXISTED);
            }

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(cartItem.getProduct())
                    .productVariant(variant) // Add variant to order item
                    .quantity(cartItem.getQuantity())
                    .priceAtOrder(priceAtOrder)
                    .build();
            orderItems.add(orderItem);
        }

        order.setOrderItems(orderItems);

        // Save order
        Order savedOrder = orderRepository.save(order);

        // Clear cart after order creation
        cartItemRepository.deleteAll(cartItems);

        return orderMapper.toOrderResponse(savedOrder);
    }

    @Override
    public OrderResponse getOrderById(String orderId) {
        Order order =
                orderRepository.findById(orderId).orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXISTED));

        // Check if user has permission to view this order
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Users user = usersRepository
                .findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Check if user is admin or the order owner
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("SCOPE_ADMIN"));

        if (!isAdmin && !order.getUser().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        return orderMapper.toOrderResponse(order);
    }

    @Override
    public Page<OrderResponse> getMyOrders(String field, Integer pageNumber, Integer pageSize, String sort) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Users user = usersRepository
                .findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Sort sortable =
                sort.equals("ASC") ? Sort.by(field).ascending() : Sort.by(field).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortable);

        Page<Order> orders = orderRepository.findByUserId(user.getId(), pageable);
        return orders.map(orderMapper::toOrderResponse);
    }

    @Override
    public Page<OrderResponse> getAllOrders(String field, Integer pageNumber, Integer pageSize, String sort) {
        Sort sortable =
                sort.equals("ASC") ? Sort.by(field).ascending() : Sort.by(field).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortable);

        Page<Order> orders = orderRepository.findAll(pageable);
        return orders.map(orderMapper::toOrderResponse);
    }

    @Override
    public Page<OrderResponse> getOrdersByStatus(
            OrderStatus status, String field, Integer pageNumber, Integer pageSize, String sort) {
        Sort sortable =
                sort.equals("ASC") ? Sort.by(field).ascending() : Sort.by(field).descending();
        Pageable pageable = PageRequest.of(pageNumber, pageSize, sortable);

        Page<Order> orders = orderRepository.findByStatus(status, pageable);
        return orders.map(orderMapper::toOrderResponse);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(String orderId, OrderStatusUpdateRequest request) {
        Order order =
                orderRepository.findById(orderId).orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXISTED));

        // Only allow certain status transitions
        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new AppException(ErrorCode.INVALID_ORDER_STATUS_TRANSITION);
        }

        order.setStatus(request.getStatus());
        Order updatedOrder = orderRepository.save(order);

        return orderMapper.toOrderResponse(updatedOrder);
    }

    @Override
    @Transactional
    public void cancelOrder(String orderId) {
        Order order =
                orderRepository.findById(orderId).orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_EXISTED));

        // Get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Users user = usersRepository
                .findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Only order owner can cancel their order
        if (!order.getUser().getId().equals(user.getId())) {
            throw new AppException(ErrorCode.UNAUTHORIZED);
        }

        // Can only cancel placed or confirmed orders
        if (order.getStatus() != OrderStatus.PLACED && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new AppException(ErrorCode.ORDER_CANNOT_BE_CANCELLED);
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }
}
