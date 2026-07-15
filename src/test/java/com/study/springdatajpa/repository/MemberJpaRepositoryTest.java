package com.study.springdatajpa.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.study.springdatajpa.entity.Member;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

// @Transactional을 테스트 클래스/메서드에 붙이면, 각 테스트가 자체 트랜잭션 안에서 실행되고
// 기본적으로 테스트가 끝난 뒤 자동 롤백된다(DB에 흔적이 안 남음). 여기서는 그 롤백 특성을
// 그대로 이용해서 트랜잭션 하나 안에서 저장->조회->삭제까지 검증한다.
@SpringBootTest
@Transactional
public class MemberJpaRepositoryTest {

	@Autowired
	MemberJpaRepository memberJpaRepository;

	@Test
	public void testMember() {
		Member member = new Member("memberA");
		Member savedMember = memberJpaRepository.save(member);

		Member findMember = memberJpaRepository.find(savedMember.getId());

		assertThat(findMember.getId()).isEqualTo(member.getId());
		assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
		// findMember와 member는 사실 완전히 다른 em.find() 호출 결과인데도 같다고 나온다.
		// 같은 트랜잭션(같은 영속성 컨텍스트) 안에서는 같은 PK로 조회하면 항상 "동일한 객체
		// 참조"를 반환하도록 JPA(1차 캐시)가 보장해주기 때문 - 이게 "JPA 엔티티 동일성 보장".
		assertThat(findMember).isEqualTo(member);
	}

	@Test
	public void basicCRUD() {
		Member member1 = new Member("member1");
		Member member2 = new Member("member2");
		memberJpaRepository.save(member1);
		memberJpaRepository.save(member2);

		// 단건 조회 검증
		Member findMember1 = memberJpaRepository.findById(member1.getId()).get();
		Member findMember2 = memberJpaRepository.findById(member2.getId()).get();
		assertThat(findMember1).isEqualTo(member1);
		assertThat(findMember2).isEqualTo(member2);

		// 리스트 조회 검증
		List<Member> all = memberJpaRepository.findAll();
		assertThat(all.size()).isEqualTo(2);

		// 카운트 검증
		long count = memberJpaRepository.count();
		assertThat(count).isEqualTo(2);

		// 삭제 검증
		memberJpaRepository.delete(member1);
		memberJpaRepository.delete(member2);

		long deletedCount = memberJpaRepository.count();
		assertThat(deletedCount).isEqualTo(0);
	}
}
