package com.study.springdatajpa.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.study.springdatajpa.entity.Member;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

// MemberJpaRepositoryTest와 검증 내용은 완전히 동일하다. 다른 점은 단 하나,
// 직접 구현한 MemberJpaRepository 대신 구현체가 없는 MemberRepository(JpaRepository 상속)를
// 쓴다는 것뿐이다. 이 테스트가 그대로 통과한다는 것 자체가 "인터페이스만 있어도
// 스프링 데이터 JPA가 만들어준 프록시가 진짜로 동작한다"는 증거가 된다.
@SpringBootTest
@Transactional
public class MemberRepositoryTest {

	@Autowired
	MemberRepository memberRepository;

	@Test
	public void testMember() {
		Member member = new Member("memberA");
		Member savedMember = memberRepository.save(member);

		// findById(ID)는 JpaRepository가 기본 제공하는 메서드. 내부적으로 em.find()를 호출하고,
		// 결과가 없을 수도 있으니 Optional<Member>로 반환한다. 그래서 .get()으로 꺼낸다.
		Member findMember = memberRepository.findById(savedMember.getId()).get();

		assertThat(findMember.getId()).isEqualTo(member.getId());
		assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
		// 같은 트랜잭션(같은 영속성 컨텍스트) 안에서는 같은 PK로 조회한 엔티티가 항상 동일한
		// 객체 참조가 되도록 JPA가 보장한다 - "JPA 엔티티 동일성 보장".
		assertThat(findMember).isEqualTo(member);
	}

	@Test
	public void basicCRUD() {
		Member member1 = new Member("member1");
		Member member2 = new Member("member2");
		memberRepository.save(member1);
		memberRepository.save(member2);

		// 단건 조회 검증
		Member findMember1 = memberRepository.findById(member1.getId()).get();
		Member findMember2 = memberRepository.findById(member2.getId()).get();
		assertThat(findMember1).isEqualTo(member1);
		assertThat(findMember2).isEqualTo(member2);

		// 리스트 조회 검증
		List<Member> all = memberRepository.findAll();
		assertThat(all.size()).isEqualTo(2);

		// 카운트 검증
		long count = memberRepository.count();
		assertThat(count).isEqualTo(2);

		// 삭제 검증
		memberRepository.delete(member1);
		memberRepository.delete(member2);

		long deletedCount = memberRepository.count();
		assertThat(deletedCount).isEqualTo(0);
	}
}
