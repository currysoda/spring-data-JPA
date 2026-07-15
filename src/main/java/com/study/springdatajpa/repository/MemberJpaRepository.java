package com.study.springdatajpa.repository;

import com.study.springdatajpa.entity.Member;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA 없이, 순수 JPA(EntityManager)만으로 짠 리포지토리.
 * 뒤에 나오는 MemberRepository(JpaRepository 상속본)가 내부적으로 대략 이런 코드를
 * 프록시로 생성해서 대신 실행해준다고 보면 된다. 즉 이 클래스는 "스프링 데이터 JPA가
 * 감춰주는 실제 구현"을 눈으로 확인하기 위한 버전이다.
 *
 * 참고로 수정(Update) 메서드가 없는 이유: JPA는 트랜잭션 안에서 조회해온 엔티티의 필드를
 * 직접 바꾸기만 하면, 트랜잭션 커밋 시점에 변경 감지(Dirty Checking)가 동작해서
 * 자동으로 UPDATE SQL을 실행해준다. 자바 컬렉션에서 꺼낸 객체의 필드를 바꾸면 그
 * 컬렉션 안의 값도 바뀌어 있는 것과 같은 원리 - 그래서 별도의 update() 메서드가 필요 없다.
 */
@Repository
public class MemberJpaRepository {

	// JPA의 핵심 객체. 영속성 컨텍스트(1차 캐시)를 통해 엔티티의 등록/조회/수정/삭제를 관리한다.
	// 스프링이 트랜잭션 범위에 맞는 실제 EntityManager를 주입해준다.
	@PersistenceContext
	private EntityManager em;

	// 저장: em.persist()는 엔티티를 영속성 컨텍스트에 등록만 하고, 실제 INSERT SQL은
	// 트랜잭션 커밋(또는 flush) 시점에 나간다. 단, Member.id 전략이 IDENTITY라서
	// DB가 채번한 pk 값을 즉시 알아야 하므로 이 경우는 persist() 호출 시점에 바로 INSERT가 나간다.
	public Member save(Member member) {
		em.persist(member);
		return member;
	}

	// 삭제: em.remove()도 즉시 DELETE가 나가는 게 아니라, 커밋/flush 시점에 반영된다.
	public void delete(Member member) {
		em.remove(member);
	}

	// 전체 조회: em.find()처럼 PK로 단건 조회하는 게 아니라 "조건에 맞는 여러 건"을 뽑아야
	// 하므로 JPQL(Java Persistence Query Language)을 사용한다.
	// "select m from Member m"에서 Member는 테이블이 아니라 엔티티(객체) 자체를 가리키고,
	// 이 JPQL이 실행 시점에 실제 SQL(select * from member ...)로 번역되어 DB에 나간다.
	public List<Member> findAll() {
		return em.createQuery("select m from Member m", Member.class)
			.getResultList();
	}

	// 단건 조회(PK): em.find()는 PK로 엔티티 하나를 찾는 가장 기본적인 JPA API.
	// 결과가 없을 수도 있으므로(null) 그대로 반환하지 않고 Optional로 감싸서,
	// 호출하는 쪽에서 "값이 없을 수도 있다"는 걸 타입만 보고 알 수 있게 해준다.
	public Optional<Member> findById(Long id) {
		Member member = em.find(Member.class, id);
		return Optional.ofNullable(member);
	}

	// 카운트: count(m) 같은 JPQL의 집계 함수도 SQL처럼 그대로 사용할 수 있다.
	// 결과가 항상 1건(숫자 하나)이므로 getResultList() 대신 getSingleResult()를 쓴다.
	public long count() {
		return em.createQuery("select count(m) from Member m", Long.class)
			.getSingleResult();
	}

	// findById와 동일하게 em.find()를 쓰지만 Optional로 감싸지 않은 버전.
	// (PDF 예제의 순수 JPA 테스트 코드가 이 메서드를 그대로 사용하기 때문에 남겨둔 것)
	public Member find(Long id) {
		return em.find(Member.class, id);
	}
}
