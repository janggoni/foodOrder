package com.gon.foodOrder.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.gon.foodOrder.model.User;

public interface UserRepository extends JpaRepository<User, Integer> {
	
	

}
