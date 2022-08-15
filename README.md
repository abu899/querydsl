# Querydsl

## 기본 문법

### 기본 Q-Type 활용

`QClass 인스턴스`를 사용하는 2가지 방법
- `QMember m = new QMember("m");`
- `QMember m = QMember.member;`
  - static 으로 만들어진 변수를 사용하기를 권장한다
  - 다만, alias 가 변경이 불가능하기 때문에 같은 테이블을 조인하는 상황에서는 위에 방법을 사용한다

### 검색 조건 쿼리

JPQL 이 제공하는 모든 검색조건을 제공한다
```
member.username.eq("member1") // username = 'member1'
member.username.ne("member1") //username != 'member1'
member.username.eq("member1").not() // username != 'member1'

member.username.isNotNull() //이름이 is not null

member.age.in(10, 20) // age in (10,20)
member.age.notIn(10, 20) // age not in (10, 20)
member.age.between(10,30) //between 10, 30

member.age.goe(30) // age >= 30
member.age.gt(30) // age > 30
member.age.loe(30) // age <= 30
member.age.lt(30) // age < 30

member.username.like("member%") //like 검색
member.username.contains("member") // like ‘%member%’ 검색
member.username.startsWith("member") //like ‘member%’ 검색
```

### 결과 조회

- fetch
  - 리스트 조회
  - 데이터가 없다면 빈 리스트가 반환된다
- fetchOne
  - 결과가 없으면 null
  - 둘 이상이면 `com.querydsl.core.NonUniqueResultException`
- fetchFirst
  - `limit(1).fetchOne()`
- fetchResult
  - 페이징 정보를 포함하고, total count 쿼리를 추가 실행한다
- fetchCount
  - count 쿼리로 변경해서 count 수를 조회

### Join

조인의 기본 문법

- 첫 번째 파라미터에 조인 대상을 지정
- 두 번째 파라미터에 별칭으로 사용할 Q 타입을 지정
- `join(조인 대상, 별칭으로 사용할 Q타입)`

### Theta join

연관관계가 없는 필드로 조인하는 방법

- from 절에 여러 엔티티를 선택해서 세타 조인을 할 수 있다
- 하지만, 외부 조인(outer join)이 불가능하다.
  - `on` 조건을 사용하여 외부 조인이 가능하다

### On 절을 이용한 join

On 절을 활용하면 다음 두가지 기능을 사용할 수 있다

- 조인 대상에 대한 필터링
  - But, inner join 의 경우에는 where 절로 필터링하는 것과 동일함.
  - 외부조인이 필요한 경우에만 사용하면 된다!
- 연관관계가 없는 엔티티간의 외부 조인
  - 일반 조인과 다르게 파라미터가 달라지니 주의해야한다
  - 일반 조인 : `leftJoin(member.team, team)`
  - on theta join : `leftJoin(team).on(xxx)`

### 서브 쿼리

`com.querydsl.jpa.JPAExpressions`를 사용하여 구현한다.

- JPA JPQL 서브쿼리는 from 절 서브쿼리의 한계가 존재한다
  - select 나 where 절에는 서브쿼리가 되지만, from 은 안됨
- 해결 방안
  - 서브쿼리를 join 으로 변경
  - 어플리케이션상에서 쿼리를 2번 분리해서 실행한다
  - native query 를 사용한다

---

## 중급 문법

### DTO 로 조회

일반적인 코드에서 Repository 까지는 엔티티를 가지고 사용해도 무방하지만,
Service 나 Controller 는 Repository 에 의존적으로 설계하면 안되기 때문에 DTO 를 통해 핸들링하는 것을 권장한다. 

JPQL 이나 순수 JPA 를 이용한 DTO 를 조회하는 코드는 new 명령어를 사용해야한다.
```text
em
    .createQuery("select new study.querydsl.dto.MemberDto(m.username, m.age) from Member m", MemberDto.class)
    .getResultList();
```
- DTO package 이름을 다 적어줘야해서 사용하기 불편하며 복잡하다
- 생성자 방식만 지원한다

### Querydsl 빈 생성 (Bean population)

- 프로퍼티 접근(Setter)
```text
Projections.bean(
    MemberDto.class,
    member.username,
    member.age)
```
- 필드 직접 접근(No Setter)
```text
Projections.fields(
  MemberDto.class,
  member.username,
  member.age)
```
- 생성자 사용
```text
Projections.constructor(
  MemberDto.class,
  member.username,
  member.age)
```
- `@QueryProjection` 사용
  - Dto 에 `@QueryProjection` 어노테이션을 추가하고 querydsl 을 컴파일하면 DTO 의 Q 파일이 생성된다
  - 이 Q 파일을 이용해 new 명령어와 함께 사용하는 편리한 방법
  - 생성자 사용 방법과 차이점
    - 생성자 사용법은 만약 생성자의 파라미터보다 많거나 적은 수의 값이 들어가도 `컴파일이 아닌 런타임에 오류`를 발생한다
    - @QueryProjection 은 컴파일 오류를 발생시켜서 실수를 줄일 수 있다
  - 단점
    - Q 파일을 생성해야한다
    - Dto 가 Querydsl 에 대한 의존관계가 생겨버린다
    - 즉, 만약 Querydsl 을 제거하게 된다면 이 dto 를 사용한 코드를 모두 변경해야함
    - 또한, 컨트롤러나 서비스에서 이 dto 가 계속 사용되는데, dto 안에 @QueryProjection 들어간 상태로 동작하기에 아키텍쳐적 관점에서 문제가 있을 수 있음
  - 만약 Dto 를 깔끔하고 의존적이여도 된다면 이 방법이 가장 좋은 방법이다