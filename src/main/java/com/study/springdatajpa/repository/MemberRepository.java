package com.study.springdatajpa.repository;

import com.study.springdatajpa.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

// T=엔티티 타입(Member), ID=PK 타입(Long). 구현체 없이도 스프링 데이터 JPA가
// 구동 시점에 이 인터페이스를 스캔해 프록시 구현체를 만들어 빈으로 등록해준다.
// JpaRepository가 기본 제공하는 메서드: save, saveAll, saveAndFlush, findById,
// existsById, findAll, findAllById, count, delete, deleteById, deleteAll, flush 등
public interface MemberRepository extends JpaRepository<Member, Long> {

}
