package com.study.springdatajpa.repository;

import com.study.springdatajpa.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

// MemberRepository와 원리 동일, 제네릭만 T=Team, ID=Long.
// JpaRepository가 기본 제공하는 메서드 (CRUD별 분류):
// - Create : save, saveAll, saveAndFlush
// - Read   : findById, existsById, findAll, findAllById, count, findAll(Sort/Pageable), getReferenceById
// - Update : 없음 - 변경 감지(Dirty Checking)로 처리, flush()는 그 반영 시점을 앞당기는 것뿐
// - Delete : delete, deleteById, deleteAllById, deleteAll(Iterable), deleteAll(), deleteAllInBatch
public interface TeamRepository extends JpaRepository<Team, Long> {

}
