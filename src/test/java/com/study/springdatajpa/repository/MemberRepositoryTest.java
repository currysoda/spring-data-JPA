package com.study.springdatajpa.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.study.springdatajpa.entity.Member;
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

// MemberJpaRepositoryTest와 검증 내용 동일, 구현체 없는 MemberRepository로 대체 실행.
@SpringBootTest
@Transactional
public class MemberRepositoryTest {

	@Autowired
	MemberRepository memberRepository;

	@Autowired
	TeamRepository teamRepository;

	@PersistenceContext
	EntityManager em;

	// 매 테스트마다 team 3개 + member 10개(team에 균등 배분, username은 member01~member10)를
	// 새로 만들어준다. 클래스 레벨 @Transactional 덕분에 테스트가 끝나면 롤백되므로,
	// 각 테스트는 항상 이 상태에서 시작한다고 가정하고 given을 생략할 수 있다.
	List<Team> teams = new ArrayList<>();
	List<Member> members = new ArrayList<>();

	@BeforeEach
	void setUp() {
		for (int i = 1; i <= 3; i++) {
			teams.add(teamRepository.save(new Team("team" + i)));
		}
		for (int i = 1; i <= 10; i++) {
			Team team = teams.get(i % teams.size());
			members.add(memberRepository.save(new Member(String.format("member%02d", i), i * 10, team)));
		}
	}

	@Test
	public void printProxyClass() {
		// when
		Class<? extends MemberRepository> proxyClass = memberRepository.getClass();

		// then: 여기 찍히는 클래스가 스프링 데이터 JPA가 런타임에 만든 프록시 구현체다.
		System.out.println("memberRepository.getClass() = " + proxyClass);
	}

	@Test
	public void testMember() {
		// given
		Member newMember = new Member("memberA");

		// when
		Member savedMember = memberRepository.save(newMember);
		Member findMember = memberRepository.findById(savedMember.getId()).get();

		// then
		assertThat(findMember.getId()).isEqualTo(newMember.getId());
		assertThat(findMember.getUsername()).isEqualTo(newMember.getUsername());
		// 같은 영속성 컨텍스트에서는 같은 PK 조회 시 동일 참조 보장(JPA 엔티티 동일성).
		assertThat(findMember).isEqualTo(newMember);
	}

	@Test
	public void basicCRUD() {
		// given: setUp()에서 만든 member 10개

		// when
		List<Member> all = memberRepository.findAll();
		long count = memberRepository.count();

		// then
		assertThat(all).hasSize(10);
		assertThat(count).isEqualTo(10);

		// when
		memberRepository.deleteAll();

		// then
		assertThat(memberRepository.count()).isEqualTo(0);
	}

	// 아래부터는 PDF에는 없는, JpaRepository가 기본 제공하는 나머지 메서드들 학습용 테스트.

	@Test
	public void saveAllAndExistsById() {
		// given
		List<Member> newMembers = List.of(new Member("extra1"), new Member("extra2"));

		// when
		// saveAll도 내부적으로는 save()를 각각 반복 호출하는 것뿐(자동 배치 아님).
		List<Member> saved = memberRepository.saveAll(newMembers);

		// then
		assertThat(saved).hasSize(2);
		assertThat(memberRepository.existsById(saved.get(0).getId())).isTrue();
		assertThat(memberRepository.existsById(9999L)).isFalse();
		assertThat(memberRepository.count()).isEqualTo(members.size() + 2);
	}

	@Test
	public void findAllById() {
		// given
		List<Long> ids = List.of(members.get(0).getId(), members.get(1).getId());

		// when
		List<Member> found = memberRepository.findAllById(ids);

		// then
		assertThat(found).containsExactlyInAnyOrder(members.get(0), members.get(1));
	}

	@Test
	public void deleteByIdAndDeleteAllById() {
		// given
		Member first = members.get(0);
		List<Long> restIds = members.stream().skip(1).map(Member::getId).toList();

		// when
		memberRepository.deleteById(first.getId());

		// then
		assertThat(memberRepository.existsById(first.getId())).isFalse();

		// when
		memberRepository.deleteAllById(restIds);

		// then
		assertThat(memberRepository.count()).isEqualTo(0);
	}

	@Test
	public void deleteAllEntitiesAndDeleteAllNoArg() {
		// given
		List<Member> someMembers = members.subList(0, 4);

		// when: 넘긴 엔티티 목록만 삭제
		memberRepository.deleteAll(someMembers);

		// then
		assertThat(memberRepository.count()).isEqualTo(members.size() - 4);

		// when: 인자 없는 deleteAll()은 전체 삭제
		memberRepository.deleteAll();

		// then
		assertThat(memberRepository.count()).isEqualTo(0);
	}

	@Test
	public void saveAndFlushThenFlush() {
		// given
		Member newMember = new Member("extra");

		// when
		// saveAndFlush: save() 호출 후 바로 flush까지 해서, 리턴 시점에 이미 INSERT가 나가 있다.
		Member saved = memberRepository.saveAndFlush(newMember);

		// then
		assertThat(saved.getId()).isNotNull();

		// when
		saved.setUsername("changed");
		// flush(): 변경 감지된 내용을 커밋을 기다리지 않고 지금 즉시 DB에 반영.
		memberRepository.flush();

		// then: 변경 감지 + flush로 UPDATE가 즉시 나갔는지는 p6spy 로그로 확인 가능
	}

	@Test
	public void deleteAllInBatch() {
		// given: setUp()에서 만든 member 10개

		// when
		// deleteAll()과 달리 엔티티를 하나씩 조회/삭제하지 않고 DELETE 쿼리 한 번으로 처리(더 빠름).
		memberRepository.deleteAllInBatch();

		// then
		assertThat(memberRepository.count()).isEqualTo(0);
	}

	@Test
	public void getReferenceById() {
		// given
		Member target = members.get(0);
		em.flush();
		em.clear(); // 영속성 컨텍스트를 비워서 진짜 프록시가 반환되는지 확인

		// when
		// getReferenceById: em.getReference() 호출, 실제 값을 안 건드리면 SELECT가 안 나가는 프록시.
		Member reference = memberRepository.getReferenceById(target.getId());

		// then
		System.out.println("reference.getClass() = " + reference.getClass());
		assertThat(reference.getId()).isEqualTo(target.getId());
	}

	@Test
	public void findAllWithSort() {
		// given: setUp()에서 만든 member01~member10 (username 오름차순 = 숫자 오름차순)

		// when
		// findAll(Sort): 정렬 조건을 파라미터로 받아 ORDER BY를 붙여준다.
		List<Member> result = memberRepository.findAll(Sort.by(Sort.Direction.DESC, "username"));

		// then
		assertThat(result).hasSize(10);
		assertThat(result.get(0).getUsername()).isEqualTo("member10");
		assertThat(result.get(9).getUsername()).isEqualTo("member01");
	}

	@Test
	public void findAllWithPaging() {
		// given
		PageRequest pageRequest = PageRequest.of(0, 3, Sort.by(Sort.Direction.ASC, "username"));

		// when
		// findAll(Pageable): 페이징 + 정렬 + 전체 카운트(totalCount) 쿼리까지 알아서 처리해서 Page로 반환.
		Page<Member> page = memberRepository.findAll(pageRequest);

		// then
		assertThat(page.getContent()).hasSize(3);
		assertThat(page.getTotalElements()).isEqualTo(10);
		assertThat(page.getTotalPages()).isEqualTo(4);
		assertThat(page.isFirst()).isTrue();
		assertThat(page.hasNext()).isTrue();
	}
}
