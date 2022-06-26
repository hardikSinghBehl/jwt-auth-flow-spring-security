package com.behl.cerberus.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.behl.cerberus.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

}
