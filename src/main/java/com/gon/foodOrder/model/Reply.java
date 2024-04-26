package com.gon.foodOrder.model;

import java.sql.Timestamp;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor// 빈생성자
@AllArgsConstructor
@Builder
@Entity
public class Reply {

	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)// 프로젝트에서 연결된 db의 넘버링 전략 따라감
	private int id; 
	
	@Column(nullable = false, length = 200)
	private String content;
	
	@ManyToOne // board -> user
	@JoinColumn(name="boardId")
	private Board board;
	
	@ManyToOne // board -> user
	@JoinColumn(name="userId")
	private User user;
	
	@CreationTimestamp// 시간 자동 입력
	private Timestamp createDate;
}
