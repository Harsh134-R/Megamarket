package com.ecommerce.Megamarket.controller;


import com.ecommerce.Megamarket.model.Address;
import com.ecommerce.Megamarket.model.User;
import com.ecommerce.Megamarket.repository.AddressRepository;
import com.ecommerce.Megamarket.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {
    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public AddressController(AddressRepository addressRepository, UserRepository userRepository) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email).orElseThrow();
    }

    @GetMapping
    public List<Address> getAddresses() {
        return addressRepository.findByUser(getCurrentUser());
    }

    @PostMapping
    public Address addAddress(@RequestBody Address address) {
        address.setUser(getCurrentUser());
        return addressRepository.save(address);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Address> updateAddress(@PathVariable Long id, @RequestBody Address updatedAddress) {
        Address address = addressRepository.findById(id).orElseThrow();
        if (!address.getUser().getId().equals(getCurrentUser().getId())) {
            return ResponseEntity.status(403).build();
        }
        address.setStreet(updatedAddress.getStreet());
        address.setCity(updatedAddress.getCity());
        address.setState(updatedAddress.getState());
        address.setPostalCode(updatedAddress.getPostalCode());
        address.setCountry(updatedAddress.getCountry());
        address.setLabel(updatedAddress.getLabel());
        return ResponseEntity.ok(addressRepository.save(address));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAddress(@PathVariable Long id) {
        Address address = addressRepository.findById(id).orElseThrow();
        if (!address.getUser().getId().equals(getCurrentUser().getId())) {
            return ResponseEntity.status(403).build();
        }
        addressRepository.delete(address);
        return ResponseEntity.ok().build();
    }
}
