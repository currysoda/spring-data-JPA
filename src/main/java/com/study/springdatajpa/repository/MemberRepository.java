package com.study.springdatajpa.repository;

import com.study.springdatajpa.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

// T=엔티티 타입(Member), ID=PK 타입(Long). 구현체 없이도 스프링 데이터 JPA가
// 구동 시점에 이 인터페이스를 스캔해 프록시 구현체를 만들어 빈으로 등록해준다.
// JpaRepository가 기본 제공하는 메서드 (CRUD별 분류):
// - Create : save, saveAll, saveAndFlush
// - Read   : findById, existsById, findAll, findAllById, count, findAll(Sort/Pageable), getReferenceById
// - Update : 없음 - 변경 감지(Dirty Checking)로 처리, flush()는 그 반영 시점을 앞당기는 것뿐
// - Delete : delete, deleteById, deleteAllById, deleteAll(Iterable), deleteAll(), deleteAllInBatch
// MemberRepositoryQuerydsl도 함께 상속 -> MemberRepositoryQuerydslImpl에 구현된 메서드도 여기서 바로 호출 가능.
public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryQuerydsl {

}
