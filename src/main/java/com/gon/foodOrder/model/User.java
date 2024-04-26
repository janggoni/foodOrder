package com.gon.foodOrder.model;

import java.sql.Timestamp;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@NoArgsConstructor// 빈생성자
@AllArgsConstructor
@Builder
@Entity
public class User {
	
	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)// 프로젝트에서 연결된 db의 넘버링 전략 따라감
	private int id; 
	
	@Column(nullable = false, length = 30)
	private String userName;
	
	@Column(nullable = false, length = 100)
	private String passWord;
	
	@Column(nullable = false, length = 50)
	private String email;
	
	public enum Role {
	    USER, ADMIN, MANAGER
	}

	@Enumerated(EnumType.STRING)
	private Role role = Role.USER;
	
	@CreationTimestamp// 시간 자동 입력
	private Timestamp createDate;

}
