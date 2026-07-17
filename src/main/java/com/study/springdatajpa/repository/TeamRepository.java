package com.study.springdatajpa.repository;

import com.study.springdatajpa.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

// MemberRepository와 원리 동일, 제네릭만 T=Team, ID=Long.
// JpaRepository가 기본 제공하는 메서드: save, saveAll, saveAndFlush, findById,
// existsById, findAll, findAllById, count, delete, deleteById, deleteAll, flush 등
public interface TeamRepository extends JpaRepository<Team, Long> {

}
