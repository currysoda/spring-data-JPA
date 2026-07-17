package com.study.springdatajpa.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.study.springdatajpa.entity.Team;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

// MemberRepositoryTest와 같은 방식(고정 fixture + given/when/then)으로 TeamRepository도 검증.
@SpringBootTest
@Transactional
public class TeamRepositoryTest {

	@Autowired
	TeamRepository teamRepository;

	@PersistenceContext
	EntityManager em;

	// 매 테스트마다 team 3개(team01~team03)를 새로 만들어준다. 클래스 레벨 @Transactional
	// 덕분에 테스트가 끝나면 롤백되므로, 각 테스트는 항상 이 상태에서 시작한다고 가정한다.
	List<Team> teams = new ArrayList<>();

	@BeforeEach
	void setUp() {
		for (int i = 1; i <= 3; i++) {
			teams.add(teamRepository.save(new Team(String.format("team%02d", i))));
		}
	}

	@Test
	public void printProxyClass() {
		// when
		Class<? extends TeamRepository> proxyClass = teamRepository.getClass();

		// then: 여기 찍히는 클래스가 스프링 데이터 JPA가 런타임에 만든 프록시 구현체다.
		System.out.println("teamRepository.getClass() = " + proxyClass);
	}

	@Test
	public void testTeam() {
		// given
		Team newTeam = new Team("teamA");

		// when
		Team savedTeam = teamRepository.save(newTeam);
		Team findTeam = teamRepository.findById(savedTeam.getId()).get();

		// then
		assertThat(findTeam.getId()).isEqualTo(newTeam.getId());
		assertThat(findTeam.getName()).isEqualTo(newTeam.getName());
		// 같은 영속성 컨텍스트에서는 같은 PK 조회 시 동일 참조 보장(JPA 엔티티 동일성).
		assertThat(findTeam).isEqualTo(newTeam);
	}

	@Test
	public void basicCRUD() {
		// given: setUp()에서 만든 team 3개

		// when
		List<Team> all = teamRepository.findAll();
		long count = teamRepository.count();

		// then
		assertThat(all).hasSize(3);
		assertThat(count).isEqualTo(3);

		// when
		teamRepository.deleteAll();

		// then
		assertThat(teamRepository.count()).isEqualTo(0);
	}

	@Test
	public void saveAllAndExistsById() {
		// given
		List<Team> newTeams = List.of(new Team("extra1"), new Team("extra2"));

		// when
		// saveAll도 내부적으로는 save()를 각각 반복 호출하는 것뿐(자동 배치 아님).
		List<Team> saved = teamRepository.saveAll(newTeams);

		// then
		assertThat(saved).hasSize(2);
		assertThat(teamRepository.existsById(saved.get(0).getId())).isTrue();
		assertThat(teamRepository.existsById(9999L)).isFalse();
		assertThat(teamRepository.count()).isEqualTo(teams.size() + 2);
	}

	@Test
	public void findAllById() {
		// given
		List<Long> ids = List.of(teams.get(0).getId(), teams.get(1).getId());

		// when
		List<Team> found = teamRepository.findAllById(ids);

		// then
		assertThat(found).containsExactlyInAnyOrder(teams.get(0), teams.get(1));
	}

	@Test
	public void deleteByIdAndDeleteAllById() {
		// given
		Team first = teams.get(0);
		List<Long> restIds = teams.stream().skip(1).map(Team::getId).toList();

		// when
		teamRepository.deleteById(first.getId());

		// then
		assertThat(teamRepository.existsById(first.getId())).isFalse();

		// when
		teamRepository.deleteAllById(restIds);

		// then
		assertThat(teamRepository.count()).isEqualTo(0);
	}

	@Test
	public void deleteAllEntitiesAndDeleteAllNoArg() {
		// given
		List<Team> someTeams = teams.subList(0, 2);

		// when: 넘긴 엔티티 목록만 삭제
		teamRepository.deleteAll(someTeams);

		// then
		assertThat(teamRepository.count()).isEqualTo(teams.size() - 2);

		// when: 인자 없는 deleteAll()은 전체 삭제
		teamRepository.deleteAll();

		// then
		assertThat(teamRepository.count()).isEqualTo(0);
	}

	@Test
	public void saveAndFlushThenFlush() {
		// given
		Team newTeam = new Team("extra");

		// when
		// saveAndFlush: save() 호출 후 바로 flush까지 해서, 리턴 시점에 이미 INSERT가 나가 있다.
		Team saved = teamRepository.saveAndFlush(newTeam);

		// then
		assertThat(saved.getId()).isNotNull();

		// when
		saved.setName("changed");
		// flush(): 변경 감지된 내용을 커밋을 기다리지 않고 지금 즉시 DB에 반영.
		teamRepository.flush();

		// then: 변경 감지 + flush로 UPDATE가 즉시 나갔는지는 p6spy 로그로 확인 가능
	}

	@Test
	public void deleteAllInBatch() {
		// given: setUp()에서 만든 team 3개

		// when
		// deleteAll()과 달리 엔티티를 하나씩 조회/삭제하지 않고 DELETE 쿼리 한 번으로 처리(더 빠름).
		teamRepository.deleteAllInBatch();

		// then
		assertThat(teamRepository.count()).isEqualTo(0);
	}

	@Test
	public void getReferenceById() {
		// given
		Team target = teams.get(0);
		em.flush();
		em.clear(); // 영속성 컨텍스트를 비워서 진짜 프록시가 반환되는지 확인

		// when
		// getReferenceById: em.getReference() 호출, 실제 값을 안 건드리면 SELECT가 안 나가는 프록시.
		Team reference = teamRepository.getReferenceById(target.getId());

		// then
		System.out.println("reference.getClass() = " + reference.getClass());
		assertThat(reference.getId()).isEqualTo(target.getId());
	}

	@Test
	public void findAllWithSort() {
		// given: setUp()에서 만든 team01~team03 (name 오름차순 = 숫자 오름차순)

		// when
		// findAll(Sort): 정렬 조건을 파라미터로 받아 ORDER BY를 붙여준다.
		List<Team> result = teamRepository.findAll(Sort.by(Sort.Direction.DESC, "name"));

		// then
		assertThat(result).hasSize(3);
		assertThat(result.get(0).getName()).isEqualTo("team03");
		assertThat(result.get(2).getName()).isEqualTo("team01");
	}

	@Test
	public void findAllWithPaging() {
		// given
		PageRequest pageRequest = PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "name"));

		// when
		// findAll(Pageable): 페이징 + 정렬 + 전체 카운트(totalCount) 쿼리까지 알아서 처리해서 Page로 반환.
		Page<Team> page = teamRepository.findAll(pageRequest);

		// then
		assertThat(page.getContent()).hasSize(2);
		assertThat(page.getTotalElements()).isEqualTo(3);
		assertThat(page.getTotalPages()).isEqualTo(2);
		assertThat(page.isFirst()).isTrue();
		assertThat(page.hasNext()).isTrue();
	}
}
