package com.behl.cerberus.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.behl.cerberus.entity.ResidentialAddress;

@Repository
public interface ResidentialAddressRepository extends JpaRepository<ResidentialAddress, UUID> {

}
