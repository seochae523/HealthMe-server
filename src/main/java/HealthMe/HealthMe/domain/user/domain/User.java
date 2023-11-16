package HealthMe.HealthMe.domain.user.domain;

import HealthMe.HealthMe.domain.exercise.domain.ExerciseProgressList;
import HealthMe.HealthMe.domain.food.domain.IngestionList;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @Getter : 객체의 속성(property) 값을 반환하는 메서드를 어노테이션으로 지원 (lombok)
 * @Setter : 객체의 속성 값을 설정, 변경하는 메서드를 어노테이션으로 지원 (lombok)
 * @Builder : 객체의 생성자를 자동으로 만들어주는 어노테이션 (lombok)
 * @Entity : JPA에서 지원하는 어노테이션으로, DB에서 Table을 생성
 * @Entity(name = value) : name = 속성 사용시 해당 Table의 이름을 클래스 명이 아닌 value로 지정가능
 * @ID : 해당 필드를 Primary key로 설정하는 어노테이션
 * @Column(name = value) : 해당 컬럼의 이름을 value로 지정해주는 어노테이션의 속성
 * @CreationTimestamp : 타임스테프를 찍기 위한 어노테이션
 * @OneToOne : 1:1 매핑방식 (JPA 매핑 전략 참고) 부모와 자식을 1:1로 매핑시켜줌
 * @ManyToOne : N:1 매핑방식 외래키(자식)을 N으로, 부모를 1로 지정하고 매핑시켜줌
 * ex) team과 member의 경우 1팀에(one) 여러 member들(many)이 있을 수 있음.
 *      이때 menber와 team을 ManyToOne으로 mapping
 *      앞 단어가 주체가 됨. (ManyToOne -> Many가 주체)
 * @OneToMany, @ManyToMany : 잘 안씀
 * @JoinColumn(name = value) : 해당 value와 mapping을 도와주는 어노테이션
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor

@Entity(name = "USER")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_ID")
    private Long id;    // pk를 위한 id

    @Column(unique = true)
    private String email;   // email

    private String password;    // 비밀번호
    private String nickname;    // 별명
    private String name;        // 본명

    @OneToMany(mappedBy = "user")
    private List<IngestionList> ingestionLists = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<ExerciseProgressList> exerciseProgressLists = new ArrayList<>();
}


