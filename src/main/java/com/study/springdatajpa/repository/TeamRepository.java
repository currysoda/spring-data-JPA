package com.study.springdatajpa.repository;

import com.study.springdatajpa.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * MemberRepository와 원리는 완전히 동일하다 (자세한 설명은 MemberRepository 주석 참고).
 * 여기서는 제네릭만 바뀐다.
 * - T  : Team (이 리포지토리가 다룰 엔티티)
 * - ID : Team.id 의 타입인 Long
 *
 * 구현체가 없어도, 스프링 데이터 JPA가 앱 구동 시 이 인터페이스를 보고
 * Team 전용 CRUD 프록시 구현체를 만들어서 빈으로 등록해준다.
 */
public interface TeamRepository extends JpaRepository<Team, Long> {

}
