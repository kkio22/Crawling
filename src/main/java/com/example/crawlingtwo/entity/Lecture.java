package com.example.crawlingtwo.entity;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.example.crawlingtwo.service.LectureCrawling;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Lecture")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Lecture {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long lectureId;
	private String name;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sub_category_id")
	private SubCategory subcategory;

	@Column(columnDefinition = "TEXT")
	//private String summary;
	private String instructor;
	//private Float price;
	@Column(length = 1000)
	private String lectureImage;
	private String lectureLink;

	public Lecture(String name, String instructor, String lectureImage, String lectureLink, SubCategory subCategory){
		this.name =name;
		this.instructor=instructor;
		this.lectureImage=lectureImage;
		this.lectureLink=lectureLink;
		this.subcategory=subCategory;
	}

}
