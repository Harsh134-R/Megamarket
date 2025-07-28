package com.ecommerce.Megamarket.repository;

import com.ecommerce.Megamarket.model.Address;
import com.ecommerce.Megamarket.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByUser(User user);
}