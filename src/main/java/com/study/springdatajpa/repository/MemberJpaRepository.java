package com.study.springdatajpa.repository;

import com.study.springdatajpa.entity.Member;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

// Spring Data JPA 없이 순수 JPA(EntityManager)로 짠 리포지토리.
// 수정 메서드가 없는 이유: 변경 감지(Dirty Checking)가 커밋 시점에 자동으로 UPDATE를 실행해줌.
@Repository
@RequiredArgsConstructor
public class MemberJpaRepository {

	@PersistenceContext
	private EntityManager em;

	// persist는 영속성 컨텍스트에 등록만 하지만, IDENTITY 전략이라 즉시 INSERT가 나간다.
	public Member save(Member member) {
		em.persist(member);
		return member;
	}

	public void delete(Member member) {
		em.remove(member);
	}

	// 여러 건 조회는 em.find() 대신 JPQL(엔티티 대상 쿼리) 사용.
	public List<Member> findAll() {
		return em.createQuery("select m from Member m", Member.class)
			.getResultList();
	}

	// 결과가 없을 수 있어 Optional로 감쌈.
	public Optional<Member> findById(Long id) {
		Member member = em.find(Member.class, id);
		return Optional.ofNullable(member);
	}

	public long count() {
		return em.createQuery("select count(m) from Member m", Long.class)
			.getSingleResult();
	}

	// findById와 동일하나 Optional로 감싸지 않은 버전(PDF 순수 JPA 테스트용).
	public Member find(Long id) {
		return em.find(Member.class, id);
	}
}
