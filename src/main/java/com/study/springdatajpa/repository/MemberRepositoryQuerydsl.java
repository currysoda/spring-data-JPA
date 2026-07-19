package com.study.springdatajpa.repository;

import com.study.springdatajpa.entity.Member;
import java.util.List;

// JpaRepository가 자동 생성해주지 않는, 직접 구현이 필요한 기능을 선언하는 인터페이스.
// (나중에 QueryDSL 도입 시 이 자리에 동적 쿼리를 구현할 예정이라 이름을 Querydsl로 붙임)
public interface MemberRepositoryQuerydsl {

	List<Member> findMemberCustom();
}