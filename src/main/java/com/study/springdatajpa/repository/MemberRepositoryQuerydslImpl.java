package com.study.springdatajpa.repository;

import com.study.springdatajpa.entity.Member;
import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;

// 이름 규칙(최신 방식): "커스텀 인터페이스 이름(MemberRepositoryQuerydsl) + Impl" - 이 이름이어야
// 스프링 데이터 JPA가 MemberRepositoryQuerydsl의 구현체로 자동 인식해서 빈으로 등록해준다.
// 지금은 QueryDSL 없이 EntityManager로 직접 짰지만, 나중에 QueryDSL로 교체할 자리.
@RequiredArgsConstructor
public class MemberRepositoryQuerydslImpl implements MemberRepositoryQuerydsl {

	private final EntityManager em;

	@Override
	public List<Member> findMemberCustom() {
		return em.createQuery("select m from Member m", Member.class)
			.getResultList();
	}
}