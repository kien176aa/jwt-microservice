package com.javatechie.repository;

import com.javatechie.entity.UrlAccess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UrlAccessRepository extends JpaRepository<UrlAccess, Long> {
}

