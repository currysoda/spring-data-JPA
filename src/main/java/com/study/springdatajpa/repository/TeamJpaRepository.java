package com.study.springdatajpa.repository;

import com.study.springdatajpa.entity.Team;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/**
 * MemberJpaRepository와 원리는 완전히 동일하다 (각 메서드의 동작 원리는
 * MemberJpaRepository 주석 참고). Member -> Team, JPQL의 alias만 m -> t로 바뀐 것뿐이다.
 * 이렇게 엔티티마다 CRUD 코드가 거의 동일하게 반복되는 문제를, 뒤에 나오는
 * Spring Data JPA의 JpaRepository<T, ID> 공통 인터페이스가 해결해준다.
 */
@Repository
public class TeamJpaRepository {

	@PersistenceContext
	private EntityManager em;

	public Team save(Team team) {
		em.persist(team);
		return team;
	}

	public void delete(Team team) {
		em.remove(team);
	}

	public List<Team> findAll() {
		return em.createQuery("select t from Team t", Team.class)
			.getResultList();
	}

	public Optional<Team> findById(Long id) {
		Team team = em.find(Team.class, id);
		return Optional.ofNullable(team);
	}

	public long count() {
		return em.createQuery("select count(t) from Team t", Long.class)
			.getSingleResult();
	}
}
