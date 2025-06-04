package com.example.crawlingtwo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.crawlingtwo.entity.Lecture;
@Repository
public interface LectureRepository extends JpaRepository<Lecture, Long> {
}
