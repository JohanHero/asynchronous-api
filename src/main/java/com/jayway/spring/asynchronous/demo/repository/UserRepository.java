package com.jayway.spring.asynchronous.demo.repository;

import com.jayway.spring.asynchronous.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {

  
}
