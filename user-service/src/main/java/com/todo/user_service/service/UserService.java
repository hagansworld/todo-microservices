package com.todo.user_service.service;

import com.todo.user_service.dto.AddressDto;
import com.todo.user_service.dto.UserRequestDto;
import com.todo.user_service.dto.UserResponseDto;
import com.todo.user_service.entity.Address;
import com.todo.user_service.entity.User;
import com.todo.user_service.exception.NotFoundException;
import com.todo.user_service.exception.ResourceAlreadyExistsException;
import com.todo.user_service.mapper.UserMapper;
import com.todo.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * Create a new User
     * @param userRequestDto - AdminUserRequestDto Object to create and save users
     * @return UserResponseDto
     * @author - Isaac Hagan
     */
    public UserResponseDto createUser(UserRequestDto userRequestDto){

        if (userRepository.existsByEmail(userRequestDto.getEmail()) ||
            userRepository.existsByUsername(userRequestDto.getUsername())){
            throw new ResourceAlreadyExistsException("User with email or username already exist");
        }
        User newUser = userMapper.mapToRegisterAdminUserRequest(userRequestDto);
        newUser.setPassword(passwordEncoder.encode(userRequestDto.getPassword()));
        newUser.setRole(userRequestDto.getRole());

        User savedUser = userRepository.save(newUser);
        log.info("User created successfully {} ", savedUser);

        return userMapper.mapToUserResponse(savedUser);
    }

    /**
     * fetch all users
     * @return UserResponseDto
     * @author - Isaac Hagan
     */
    public List<UserResponseDto>getAllUsers(){
        return userRepository.findAll()
                .stream()
                .map(userMapper::mapToUserResponse)
                .toList();
    }


    /**
     * Get User by id
     * @param id - id of the user
     * @return UserResponse
     * @author - Isaac Hagan
     */
    public UserResponseDto getUserById(UUID id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User with " + id + " not found"));

        return userMapper.mapToUserResponse(user);
    }

    /**
     * Update User By id
     * @param id - id of the updated User
     * @param userRequestDto - userRequestDto to update and save users
     * @return UserResponse
     * @author - Isaac Hagan
     */
    public UserResponseDto updateUserById(UUID id, UserRequestDto userRequestDto){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User with " + id + " not found"));

        user.setFullName(userRequestDto.getFullName() !=null ? userRequestDto.getFullName() : user.getFullName());
        user.setUsername(userRequestDto.getUsername() !=null ? userRequestDto.getUsername() : user.getUsername());
        user.setEmail(userRequestDto.getEmail() !=null ? userRequestDto.getEmail() : user.getEmail());
        user.setPhoneNumber(userRequestDto.getPhoneNumber() !=null ? userRequestDto.getPhoneNumber() : user.getPhoneNumber());
        user.setRole(userRequestDto.getRole() !=null ? userRequestDto.getRole() : user.getRole());

        if (userRequestDto.getAddress() != null) {
            Address address = updateAddress(user, userRequestDto.getAddress());
            user.setAddress(address);
        }

        user.setStatus(userRequestDto.getStatus() !=null ? userRequestDto.getStatus() : user.getStatus());

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully {} ", updatedUser);

        return userMapper.mapToUserResponse(updatedUser);

    }

    // Address helper
    private Address updateAddress(User user, AddressDto reqAddress) {
        Address address = user.getAddress() != null ? user.getAddress() : new Address();

        if (reqAddress.getStreet() != null) address.setStreet(reqAddress.getStreet());
        if (reqAddress.getCity() != null) address.setCity(reqAddress.getCity());
        if (reqAddress.getState() != null) address.setState(reqAddress.getState());
        if (reqAddress.getCountry() != null) address.setCountry(reqAddress.getCountry());
        if (reqAddress.getZipcode() != null) address.setZipcode(reqAddress.getZipcode());

        return address;
    }

    /**
     * Delete  user By Id
     * @param id - id of the user
     * @return null
     * author - Isaac Hagan
     */
    public UserResponseDto deleteUser(UUID id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User with " + id + " not found"));

        userRepository.delete(user);
        log.info("User with id {} deleted", id);

        return  null;
    }

    /**
     * Search for users
     * @param Keyword -keyword param for searching
     * @return - list of users
     */
    public List<UserResponseDto> searchUsers(String Keyword){
        List<User>users=userRepository.searchUsers(Keyword);
        return users
                .stream()
                .map(userMapper::mapToUserResponse)
                .toList();
    }

}
