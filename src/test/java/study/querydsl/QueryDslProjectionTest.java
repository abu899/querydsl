package study.querydsl;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberDto;
import study.querydsl.dto.QMemberDto;
import study.querydsl.dto.UserDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static study.querydsl.entity.QMember.member;

@SpringBootTest
@Transactional
public class QueryDslProjectionTest {

    @PersistenceContext
    EntityManager em;

    JPAQueryFactory queryFactory;

    @BeforeEach
    public void before() {
        queryFactory = new JPAQueryFactory(em);

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Nested
    @DisplayName("Basic Projection")
    class BasicProjectionTest {
        @Test
        void simpleProjection() {
            List<Member> result = queryFactory
                    .select(member)
                    .from(member)
                    .fetch();

            for (Member member1 : result) {
                System.out.println("member1 = " + member1);
            }
        }

        @Test
        void tupleProjection() {
            List<Tuple> result = queryFactory
                    .select(member.username, member.age)
                    .from(member)
                    .fetch();

            for (Tuple tuple : result) {
                String username = tuple.get(member.username);
                Integer age = tuple.get(member.age);
                System.out.println("username = " + username);
                System.out.println("age = " + age);
            }
        }
    }

    @Nested
    @DisplayName("Dto Projection")
    class DtoProjectionTest {

        @Test
        void findDtoByJPQL() {
            List<MemberDto> resultList = em
                    .createQuery("select new study.querydsl.dto.MemberDto(m.username, m.age) from Member m", MemberDto.class)
                    .getResultList();

            for (MemberDto memberDto : resultList) {
                System.out.println("memberDto = " + memberDto);
            }
        }

        @Test
        void findDtoByQuerydslSetter() {
            List<MemberDto> result = queryFactory
                    .select(Projections.bean(MemberDto.class,
                            member.username,
                            member.age))
                    .from(member)
                    .fetch();

            for (MemberDto memberDto : result) {
                System.out.println("memberDto = " + memberDto);
            }
        }

        @Test
        void findDtoByQuerydslByField() {
            List<MemberDto> result = queryFactory
                    .select(Projections.fields(MemberDto.class,
                            member.username,
                            member.age))
                    .from(member)
                    .fetch();

            for (MemberDto memberDto : result) {
                System.out.println("memberDto = " + memberDto);
            }
        }

        @Test
        void findDtoByQuerydslByConstructor() {
            List<MemberDto> result = queryFactory
                    .select(Projections.constructor(MemberDto.class,
                            member.username,
                            member.age))
                    .from(member)
                    .fetch();

            for (MemberDto memberDto : result) {
                System.out.println("memberDto = " + memberDto);
            }
        }

        @Test
        @DisplayName("멤버의 이름이 QMember 의 이름과 다른 경우(Setter, Field)")
        void findUserDto() {
            List<UserDto> result = queryFactory
                    .select(Projections.fields(UserDto.class,
                            member.username.as("name"),
                            member.age))
                    .from(member)
                    .fetch();

            for (UserDto userDto : result) {
                System.out.println("userD = " + userDto);
            }
        }

        @Test
        @DisplayName("멤버의 이름이 QMember 의 이름과 다른 경우(Constructor)")
        void findUserDtoByConstructor() {
            List<UserDto> result = queryFactory
                    .select(Projections.constructor(UserDto.class,
                            member.username,
                            member.age))
                    .from(member)
                    .fetch();

            for (UserDto userDto : result) {
                System.out.println("userD = " + userDto);
            }
        }

        @Test
        void findDtoByQueryProjection() {
            List<MemberDto> result = queryFactory
                    .select(new QMemberDto(member.username, member.age))
                    .from(member)
                    .fetch();

            for (MemberDto memberDto : result) {
                System.out.println("memberDto = " + memberDto);
            }
        }
    }
}
