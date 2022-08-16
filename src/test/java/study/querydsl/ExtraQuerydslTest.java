package study.querydsl;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.List;

import static study.querydsl.entity.QMember.member;

public class ExtraQuerydslTest {
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
    @DisplayName("Bulk Operation")
    class BulkOperationTest {

        /**
         * 벌크 연산 시, 조심해야할 사항
         * 벌크 연산은 DB에 바로 쿼리가 나가기 때문에, DB의 상태와 영속성 컨텍스트(1차캐시)의 상태가 달라지게된다
         * 벌크 연산 전후로 트랜잭션이 끝나기전에 영속성 컨텍스트로부터 데이터를 가져오면 예상했던 값과 다른 값을 가져온다.
         * 영속성 컨텍스트가 DB의 값보다 우선권을 가진다.
         * 따라서 벌크 연산 후에는 무조건 em.flush() 및 em.clear()를 진행해준다.
         */

        @Test
        void bulkUpdate() {
            long count = queryFactory
                    .update(member)
                    .set(member.username, "비회원")
                    .where(member.age.lt(20))
                    .execute();
        }

        @Test
        void bulkAdd() {
            long count = queryFactory
                    .update(member)
                    .set(member.age, member.age.add(1))
                    .execute();
        }

        @Test
        void bulkDelete() {
            long count = queryFactory
                    .delete(member)
                    .where(member.age.gt(18))
                    .execute();
        }
    }

    @Nested
    @DisplayName("Sql Function")
    class SqlFunctionTest {

        /**
         * SQL Function 은 DB Dialect(방언)에 따라 사용할 수 있는 함수가 다르다다
        */

        @Test
        void sqlFunction1() {
            List<String> result = queryFactory
                    .select(
                            Expressions.stringTemplate(
                                    "function('replace', {0}, {1}, {2}"
                                    , member.username, "member", "m"))
                    .from(member)
                    .fetch();

            for (String s : result) {
                System.out.println("s = " + s);
            }
        }

        @Test
        void sqlFunction2() {
            List<String> result = queryFactory
                    .select(member.username)
                    .from(member)
                    .where(member.username.eq(
//                            Expressions.stringTemplate("function('lower', {0}", member.username))
                            member.username.lower())
                    )
                    .fetch();

            for (String s : result) {
                System.out.println("s = " + s);
            }
        }
    }
}
