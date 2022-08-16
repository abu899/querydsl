package study.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static study.querydsl.entity.QMember.member;

@SpringBootTest
@Transactional
public class DynamicQueryTest {
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
    @DisplayName("Boolean Builder")
    class BooleanBuilderTest {

        @Test
        void basicBooleanBuilder() {
            String usernameParam = "member1";
            Integer ageParam = 10;

            List<Member> result = searchMember1(usernameParam, ageParam);
            assertThat(result.size()).isEqualTo(1);
        }

        private List<Member> searchMember1(String usernameParam, Integer ageParam) {

            BooleanBuilder builder = new BooleanBuilder();
            if (usernameParam != null) {
                builder.and(member.username.eq(usernameParam));
            }

            if (ageParam != null) {
                builder.and(member.age.eq(ageParam));
            }

            return queryFactory
                    .selectFrom(member)
                    .where(builder)
                    .fetch();
        }
    }

    @Nested
    @DisplayName("Where 다중 파라미터")
    class MultipleWhereTest {
        @Test
        void basicMultipleWhere() {
            String usernameParam = "member1";
            Integer ageParam = 10;

            List<Member> result = searchMember2(usernameParam, ageParam);
            assertThat(result.size()).isEqualTo(1);
        }

        private List<Member> searchMember2(String usernameParam, Integer ageParam) {
            return queryFactory
                    .selectFrom(member)
                    .where(
//                            usernameEq(usernameParam), ageEq(ageParam)
                            allEq(usernameParam, ageParam)
                    )
                    .fetch();
        }

        private BooleanExpression usernameEq(String usernameParam) {
            if(usernameParam != null) {
                return member.username.eq(usernameParam);
            }

            return null;
        }

        private BooleanExpression ageEq(Integer ageParam) {
            if(ageParam != null) {
                return member.age.eq(ageParam);
            }

            return null;
        }

        private Predicate allEq(String usernameParam, Integer ageParam) {
            return usernameEq(usernameParam).and(ageEq(ageParam));
        }
    }
}
