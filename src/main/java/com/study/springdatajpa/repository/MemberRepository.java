package com.study.springdatajpa.repository;

import com.study.springdatajpa.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * JpaRepository<T, ID> 제네릭
 * - T  : 이 리포지토리가 다룰 엔티티 타입 (Member)
 * - ID : 그 엔티티의 @Id(식별자, PK)로 매핑된 필드의 타입 (Member.id 가 Long 이므로 Long)
 * 이 두 타입을 지정해주면, JpaRepository 안의 모든 공통 메서드(save, findById, findAll,
 * count, delete 등)가 제네릭이 실체화되면서 Member 전용 메서드처럼 동작한다.
 * 예) findById(Long id) -> Optional<Member>, save(Member) -> Member
 *
 * 이 인터페이스는 메서드 몸체(구현부)가 하나도 없다. 그런데도 동작하는 이유:
 * 1. 앱 구동 시점에 스프링 데이터 JPA가 클래스패스를 스캔해서
 *    org.springframework.data.repository.Repository(그 하위 JpaRepository 포함)를
 *    상속한 인터페이스를 전부 찾아낸다.
 * 2. 찾아낸 인터페이스마다 실제 구현 클래스를 자바 프록시(Proxy) 기술로 그 자리에서 만들어낸다.
 *    이 구현체 내부에서는 MemberJpaRepository 에서 우리가 직접 짰던 것과 똑같이
 *    EntityManager.persist/find/remove, JPQL 실행 등을 대신 호출해준다.
 * 3. 만들어진 프록시 구현체를 스프링 빈으로 등록하고, @Autowired MemberRepository 자리에
 *    주입해준다. 그래서 memberRepository.getClass()를 찍어보면 MemberRepository가 아니라
 *    "class com.sun.proxy.$ProxyXXX" 같은 실제로는 존재하지 않던 클래스가 찍힌다.
 *
 * 참고: 예전 스프링 데이터 JPA에서는 이런 프록시 생성 대상 스캔 + 컴포넌트 등록 + JPA 예외를
 * 스프링 공통 예외로 변환하는 처리를 위해 @Repository 애노테이션이 필요했지만, 지금은
 * JpaRepository를 상속한 인터페이스라는 것만으로 스프링 데이터 JPA가 다 인식하기 때문에
 * @Repository를 안 붙여도 된다.
 */
public interface MemberRepository extends JpaRepository<Member, Long> {

}
