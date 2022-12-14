package study.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.team;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

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
    @DisplayName("What is Querydsl")
    class SimpleQuerydslTest {
        @Test
        void startJPQL() {
            String qlString = "select m from Member m where m.username = :username";
            Member findMember = em.createQuery(qlString, Member.class)
                    .setParameter("username", "member1")
                    .getSingleResult();

            assertThat(findMember.getUsername()).isEqualTo("member1");
        }

        @Test
        void startQuerydsl() {
            QMember m = new QMember("m");

            Member findMember = queryFactory
                    .select(m)
                    .from(m)
                    .where(m.username.eq("member1"))
                    .fetchOne();

            assertThat(findMember.getUsername()).isEqualTo("member1");
        }
    }

    @Nested
    @DisplayName("Search")
    class SearchTest {
        @Test
        void search() {
            Member findMember = queryFactory
                    .selectFrom(member)
                    .where(member.username.eq("member1")
                            .and(member.age.between(10, 30)))
                    .fetchOne();

            assertThat(findMember.getUsername()).isEqualTo("member1");
            assertThat(findMember.getAge()).isEqualTo(10);
        }

        @Test
        void searchAndParam() {
            Member findMember = queryFactory
                    .selectFrom(member)
                    .where(
                            member.username.eq("member1"), // and ??? ???????????? , ??? ?????? ??? ??????
                            (member.age.eq(10)))
                    .fetchOne();

            assertThat(findMember.getUsername()).isEqualTo("member1");
            assertThat(findMember.getAge()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("Fetch")
    class FetchTest {
        @Test
        void fetch() {
            List<Member> fetch = queryFactory
                    .selectFrom(member)
                    .fetch();

            Member fetchOne = queryFactory
                    .selectFrom(QMember.member)
                    .fetchOne();

            Member fetchFirst = queryFactory
                    .selectFrom(QMember.member)
                    .fetchFirst();

            /**
             * ?????? deprecate
             * fetch??? ?????? ???????????? ???????????? list ??? ???????????? ?????? ??????????????? ????????????
             * ???????????? ????????? ????????? ????????? ????????? ?????? ?????????????????? ???
             */
            QueryResults<Member> results = queryFactory
                    .selectFrom(member)
                    .fetchResults(); // ????????? ???????????????..
        }
    }

    @Nested
    @DisplayName("Sort and Paging")
    class SortPagingTest {
        @Test
        void sort() {
            em.persist(new Member(null, 100));
            em.persist(new Member("member5", 100));
            em.persist(new Member("member6", 100));

            List<Member> result = queryFactory
                    .selectFrom(member)
                    .where(member.age.eq(100))
                    .orderBy(member.age.desc(), member.username.asc().nullsLast())
                    .fetch();

            Member member5 = result.get(0);
            Member member6 = result.get(1);
            Member memberNull = result.get(2);
            assertThat(member5.getUsername()).isEqualTo("member5");
            assertThat(member6.getUsername()).isEqualTo("member6");
            assertThat(memberNull.getUsername()).isNull();
        }

        @Test
        void paging1() {
            List<Member> result = queryFactory
                    .selectFrom(member)
                    .orderBy(member.username.desc())
                    .offset(1)
                    .limit(2)
                    .fetch();

            assertThat(result.size()).isEqualTo(2);
        }

        @Test
        void paging2() {
            QueryResults<Member> queryResults = queryFactory
                    .selectFrom(member)
                    .orderBy(member.username.desc())
                    .offset(1)
                    .limit(2)
                    .fetchResults();

            assertThat(queryResults.getTotal()).isEqualTo(4);
            assertThat(queryResults.getLimit()).isEqualTo(2);
            assertThat(queryResults.getOffset()).isEqualTo(1);
            assertThat(queryResults.getResults().size()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Aggregation")
    class AggregationTest {
        @Test
        void aggregation() {
            List<Tuple> result = queryFactory
                    .select(
                            member.count(),
                            member.age.sum(),
                            member.age.avg(),
                            member.age.max(),
                            member.age.min()
                    )
                    .from(member)
                    .fetch();

            Tuple tuple = result.get(0);
            assertThat(tuple.get(member.count())).isEqualTo(4);
            assertThat(tuple.get(member.age.sum())).isEqualTo(100);
            assertThat(tuple.get(member.age.avg())).isEqualTo(25);
            assertThat(tuple.get(member.age.max())).isEqualTo(40);
            assertThat(tuple.get(member.age.min())).isEqualTo(10);
        }

        /**
         * ?????? ????????? ??? ?????? ?????? ??????
         */
        @Test
        void groupBy() {
            List<Tuple> result = queryFactory
                    .select(team.name, member.age.avg())
                    .from(member)
                    .join(member.team, team)
                    .groupBy(team.name)
                    .fetch();

            Tuple teamA = result.get(0);
            Tuple teamB = result.get(1);

            assertThat(teamA.get(team.name)).isEqualTo("teamA");
            assertThat(teamA.get(member.age.avg())).isEqualTo(15);

            assertThat(teamB.get(team.name)).isEqualTo("teamB");
            assertThat(teamB.get(member.age.avg())).isEqualTo(35);
        }
    }


    @Nested
    @DisplayName("Join")
    class JoinTest {
        /**
         * ??? A??? ????????? ?????? ??????
         */
        @Test
        void basicJoin() {
            List<Member> result = queryFactory
                    .selectFrom(member)
                    .join(member.team, team) // ?????? innerJoin
                    .where(team.name.eq("teamA"))
                    .fetch();

            assertThat(result)
                    .extracting("username")
                    .containsExactly("member1", "member2");
        }

        @Test
        void thetaJoin() {
            em.persist(new Member("teamA"));
            em.persist(new Member("teamB"));
            em.persist(new Member("teamC"));

            List<Member> result = queryFactory
                    .select(member)
                    .from(member, team)
                    .where(member.username.eq(team.name))
                    .fetch();

            assertThat(result)
                    .extracting("username")
                    .containsExactly("teamA", "teamB");
        }

        /**
         * ????????? ?????? ???????????????,
         * ??? ????????? teamA??? ?????? ??????, ????????? ?????? ??????
         * JPQL: select m, t from Member m left join m.team t on t.name = 'teamA'
         */
        @Test
        void joinOnFiltering() {
            List<Tuple> result = queryFactory
                    .select(member, team)
                    .from(member)
                    .leftJoin(member.team, team)
                    .on(team.name.eq("teamA"))
                    .fetch();

            for (Tuple tuple : result) {
                System.out.println("tuple = " + tuple);
            }
        }

        /**
         * ??????????????? ?????? ??????????????? ?????? ??????
         */
        @Test
        void outerJoinWithNoRelation() {
            em.persist(new Member("teamA"));
            em.persist(new Member("teamB"));
            em.persist(new Member("teamC"));

            List<Tuple> result = queryFactory
                    .select(member, team)
                    .from(member)
                    .leftJoin(team).on(member.username.eq(team.name)) // ?????? join ??? ???????????? ????????? ?????????!! id ???????????? ?????????!
                    .fetch();

            for (Tuple tuple : result) {
                System.out.println("tuple = " + tuple);
            }
            assertThat(result)
                    .extracting("username")
                    .containsExactly("teamA", "teamB");
        }
    }

    @Nested
    @DisplayName("FetchJoin")
    class FetchJoinTest {
        @PersistenceContext
        EntityManagerFactory emf;

        @Test
        void fetchJoinNotApplied() {
            em.flush();
            em.clear();

            Member findMember = queryFactory
                    .selectFrom(member)
                    .where(member.username.eq("member1"))
                    .fetchOne();

            boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
            assertThat(loaded).as("fetch join not applied").isFalse();
        }

        @Test
        void fetchJoinApplied() {
            em.flush();
            em.clear();

            Member findMember = queryFactory
                    .selectFrom(member)
                    .join(member.team, team).fetchJoin()
                    .where(member.username.eq("member1"))
                    .fetchOne();

            boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
            assertThat(loaded).as("fetch join applied").isTrue();
        }
    }

    @Nested
    @DisplayName("SubQuery")
    class SubQueryTest {
        /**
         * ????????? ?????? ?????? ?????? ??????
         */
        @Test
        void subQuery() {

            QMember memberSub = new QMember("memberSub");

            List<Member> result = queryFactory
                    .selectFrom(member)
                    .where(
                            member.age.eq(
                                    JPAExpressions
                                            .select(memberSub.age.max())
                                            .from(memberSub)
                            )
                    )
                    .fetch();

            assertThat(result).extracting("age")
                    .containsExactly(40);
        }

        /**
         * ????????? ?????? ????????? ??????
         */
        @Test
        void subQueryGoe() {

            QMember memberSub = new QMember("memberSub");

            List<Member> result = queryFactory
                    .selectFrom(member)
                    .where(
                            member.age.goe(
                                    JPAExpressions
                                            .select(memberSub.age.avg())
                                            .from(memberSub)
                            )
                    )
                    .fetch();

            assertThat(result).extracting("age")
                    .containsExactly(30, 40);
        }

        /**
         * ????????? ?????? ????????? ??????
         */
        @Test
        void subQueryIn() {

            QMember memberSub = new QMember("memberSub");

            List<Member> result = queryFactory
                    .selectFrom(member)
                    .where(
                            member.age.in(
                                    JPAExpressions
                                            .select(memberSub.age)
                                            .from(memberSub)
                                            .where(memberSub.age.gt(10))
                            )
                    )
                    .fetch();

            assertThat(result).extracting("age")
                    .containsExactly(20, 30, 40);
        }

        @Test
        void subQueryInSelectClause() {
            QMember memberSub = new QMember("memberSub");

            List<Tuple> result = queryFactory
                    .select(member.username,
                            JPAExpressions
                                    .select(memberSub.age.avg())
                                    .from(memberSub))
                    .from(member)
                    .fetch();

            for (Tuple tuple : result) {
                System.out.println("tuple = " + tuple);
            }
        }
    }

    @Nested
    @DisplayName("Case and Constant")
    class CaseConstantTest {
        /**
         * ???????????? ?????? ???????????? ??????????????? ???????????? ?????????
         * ???????????? ????????? ????????? ????????????
         */
        @Test
        void basicCase() {
            List<String> result = queryFactory
                    .select(member.age
                            .when(10).then("??????")
                            .when(20).then("?????????")
                            .otherwise("??????")
                    ).from(member)
                    .fetch();

            for (String s : result) {
                System.out.println("s = " + s);
            }
        }

        @Test
        void complexCase() {
            List<String> result = queryFactory
                    .select(new CaseBuilder()
                            .when(member.age.between(0, 20)).then("0~20???")
                            .when(member.age.between(21, 30)).then("21~30???")
                            .otherwise("??????"))
                    .from(member)
                    .fetch();

            for (String s : result) {
                System.out.println("s = " + s);
            }
        }

        @Test
        void constant() {
            List<Tuple> result = queryFactory
                    .select(member.username, Expressions.constant("A"))
                    .from(member)
                    .fetch();

            for (Tuple tuple : result) {
                System.out.println("tuple = " + tuple);
            }
        }

        @Test
        void concat() {
            //username_age
            List<String> result = queryFactory
                    .select(member.username.concat("_").concat(member.age.stringValue()))
                    .from(member)
                    .where(member.username.eq("member1"))
                    .fetch();

            for (String s : result) {
                System.out.println("s = " + s);
            }
        }
    }
}
