package com.gon.foodOrder.model;

import java.sql.Timestamp;
import java.util.List;

import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;

import com.gon.foodOrder.model.User.Role;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor// 빈생성자
@AllArgsConstructor
@Builder
@Entity
public class Board {

	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)// 프로젝트에서 연결된 db의 넘버링 전략 따라감
	private int id; 
	
	@Column(nullable = false, length = 100)
	private String title;
	
	@Lob
	private String content;
	
	@Column(nullable = false, length = 50) 
	private String email;
	
	@ColumnDefault("0")
	private int count;
	
	@ManyToOne // board -> user
	@JoinColumn(name="userId")
	private User userId; // db는 오브젝트를 저장할숭벗다 fk 자바는 오브젝트를 저장할수있
	
	@OneToMany(mappedBy = "board", fetch = FetchType.EAGER)// 연관관계 주인이 아닌 reply 테이블의 board컬럼따라가고 db에 컬럼을 만들지 마라
	private List<Reply> reply;
	
	@CreationTimestamp// 시간 자동 입력
	private Timestamp createDate;
	
}
