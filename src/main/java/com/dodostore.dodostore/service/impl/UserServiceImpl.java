package com.dodostore.dodostore.service.impl;

import com.dodostore.dodostore.dto.request.AddressRequest;
import com.dodostore.dodostore.dto.request.CreateUserRequest;
import com.dodostore.dodostore.dto.request.UpdateUserRequest;
import com.dodostore.dodostore.dto.response.UserResponse;
import com.dodostore.dodostore.entity.*;
import com.dodostore.dodostore.enums.RoleEnum;
import com.dodostore.dodostore.enums.StatusEnum;
import com.dodostore.dodostore.enums.StatusOrderEnum;
import com.dodostore.dodostore.exception.AppException;
import com.dodostore.dodostore.exception.ErrorCode;
import com.dodostore.dodostore.mapper.UserMapper;
import com.dodostore.dodostore.repository.*;
import com.dodostore.dodostore.service.CloudinaryService;
import com.dodostore.dodostore.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private CartOrderService cartOrders;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    // View user quantity
    @Override
    public int countUsers() {
        return userRepository.findByStatus(StatusEnum.Active).size();
    }

    // Find By Username
    @Override
    public UserEntity getUserByUsername(String username) {
        if(!userRepository.existsByUsernameAndStatus(username, StatusEnum.Active)){
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
        return userRepository.findByUsernameAndStatus(username, StatusEnum.Active);
    }

    // View user by Id
    @Override
    public UserResponse getUserById(Long userId) {
        if(!userRepository.existsByIdAndStatus(userId, StatusEnum.Active)){
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }

        UserResponse userResponse =userMapper.toUserResponse(userRepository.findById(userId).get()) ;
        return userResponse ;
    }

    // View all users
    @Override
    public List<UserResponse> getUsers() {
        List<UserResponse> users = new ArrayList<>();
        for(UserEntity user: userRepository.findByStatus(StatusEnum.Active)){
            users.add(userMapper.toUserResponse(user));
        }
        return users;
    }

    // Register user
    @Override
    public UserEntity registerUser(CreateUserRequest request) {
        if (request.getFullName() != null) request.setFullName(request.getFullName().trim());
        if (request.getEmail() != null) request.setEmail(request.getEmail().trim().toLowerCase(java.util.Locale.ROOT));
        if(userRepository.existsByUsernameAndStatus(request.getUsername(), StatusEnum.Active)){
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        UserEntity newUser = userMapper.toUserEntity(request);

        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        newUser.setCreateDate(new Date());
        newUser.setRoles(RoleEnum.USER);
        newUser.setStatus(StatusEnum.Active);

        return userRepository.save(newUser);
    }


    // Create user
    @Override
    public UserEntity createUser(CreateUserRequest request, MultipartFile file) {
        if(userRepository.existsByUsernameAndStatus(request.getUsername(), StatusEnum.Active)){
            throw new AppException(ErrorCode.USER_EXISTED);
        }

        UserEntity newUser = userMapper.toUserEntity(request);

        if(!file.isEmpty() && file != null){
            try{
                newUser.setAvatar(cloudinaryService.uploadFile(file));
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        newUser.setCreateDate(new Date());
        newUser.setRoles(RoleEnum.USER);
        newUser.setStatus(StatusEnum.Active);

        return userRepository.save(newUser);
    }

    // Update user
    @Override
    public UserResponse updateUser(UpdateUserRequest request, Long userId, MultipartFile file) {
        if(!userRepository.existsByIdAndStatus(userId, StatusEnum.Active)){
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
        UserEntity updateUser = userRepository.findByIdAndStatus(userId, StatusEnum.Active);

        if(!file.isEmpty() && file != null){
            try{
                updateUser.setAvatar(cloudinaryService.uploadFile(file));
            } catch (Exception e){
                e.printStackTrace();
            }
        }

        if(request.getPassword() != null && !request.getPassword().isEmpty()){
            updateUser.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if(request.getPhone() != null && !request.getPhone().isEmpty()){
            updateUser.setPhone(request.getPhone());
        }
        if(request.getEmail() != null && !request.getEmail().isEmpty()){
            updateUser.setEmail(request.getEmail());
        }
        if(request.getFullName() != null && !request.getFullName().isEmpty()){
            updateUser.setFullName(request.getFullName());
        }

        updateUser.setGender(request.getGender());
        updateUser.setUpdateDate(new Date());

        userRepository.save(updateUser);

        return userMapper.toUserResponse(updateUser);

    }

    // Delete user
    @Override
    public void deleteUser(Long userId) {
        UserEntity user = userRepository.findById(userId).get();
        if(user == null){
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
        user.setStatus(StatusEnum.Closed);

        userRepository.save(user);
    }

    @Override
    public void addAddress(Long userId, AddressRequest request){
        AddressEntity newAddress = new AddressEntity();

        newAddress.setAddress(request.getAddress());
        newAddress.setPhone(request.getPhone());

        newAddress.setUser(userRepository.findByIdAndStatus(userId, StatusEnum.Active));

        addressRepository.save(newAddress);
    }

    @Override
    public void deleteAddress(Long addressId){
        addressRepository.deleteById(addressId);
    }

    @Override
    @Transactional
    public String checkout(Long userId, Long addressId){
        UserEntity user = entityManager.find(UserEntity.class, userId, jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
        AddressEntity address = addressRepository.findById(addressId).orElseThrow();
        if (user == null || user.getStatus() != StatusEnum.Active || !address.getUser().getId().equals(userId))
            throw new IllegalArgumentException("Địa chỉ hoặc tài khoản không hợp lệ");
        List<CartEntity> checkoutItems = new ArrayList<>(cartRepository.findByUserId(userId));
        if (checkoutItems.isEmpty()) throw new IllegalArgumentException("Giỏ hàng trống");
        checkoutItems.sort(java.util.Comparator.comparing(c -> c.getProduct().getId()));
        OrderEntity newOrder = cartOrders.submit(user);
        newOrder.setUser(user);
        orderRepository.save(newOrder);
        List<OrderDetailEntity> orderDetails = new ArrayList<>();

        Long totalOrder = 0L;
        for(CartEntity cart : checkoutItems){
            entityManager.refresh(cart.getProduct(), jakarta.persistence.LockModeType.PESSIMISTIC_WRITE);
            com.dodostore.dodostore.utils.ProductSizes.requireSelection(cart);
            if (cart.getQuantity() == null || cart.getQuantity() <= 0 || cart.getProduct().getStatus() != StatusEnum.Active
                    || cart.getProduct().getQuantity() < cart.getQuantity())
                throw new IllegalArgumentException("Sản phẩm không đủ tồn kho");
            OrderDetailEntity item = new OrderDetailEntity();

            item.setProduct(cart.getProduct());
            item.setQuantity(cart.getQuantity());
            item.setSize(cart.getSize());
            item.setColor(cart.getColor());
            item.setPrice(cart.getProduct().getPrice());
            item.setDiscount(cart.getProduct().getEffectiveDiscount());
            item.setFlashSaleCampaignId(com.dodostore.dodostore.utils.CampaignAttribution.appliedCampaign(cart.getProduct(), item.getDiscount()));
            item.setTotal(Math.multiplyExact(Math.multiplyExact(cart.getProduct().getPrice(), 100 - item.getDiscount()) / 100, cart.getQuantity()));
            item.setOrder(newOrder);

            totalOrder += item.getTotal();

            orderDetails.add(item);
            orderDetailRepository.save(item);
            
            ProductEntity product = cart.getProduct();
            product.reserveVariant(cart.getColor(), cart.getSize(), cart.getQuantity());
            product.setQuantitySell((product.getQuantitySell() == null ? 0 : product.getQuantitySell()) + cart.getQuantity());
            productRepository.saveAndFlush(product);
        }

        newOrder.setUser(user);
        newOrder.setTotal(totalOrder);
        newOrder.setQuantity(Long.valueOf(orderDetails.size()));
        newOrder.setDate(new Date());
        newOrder.setAddress(address.getAddress());
        newOrder.setPhone(address.getPhone());
        newOrder.setStatus(StatusOrderEnum.Dang_Giao);

        orderRepository.save(newOrder);

        cartRepository.deleteByUserId(userId);

        return "Sản phẩm đang được chuẩn bị để giao, Xem chi tiết tại lịch sử mua hàng";
    }
}
