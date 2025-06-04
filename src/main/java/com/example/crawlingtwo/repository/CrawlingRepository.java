package com.example.crawlingtwo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.crawlingtwo.entity.Category;

@Repository
public interface CrawlingRepository extends JpaRepository<Category, Long> {
}
