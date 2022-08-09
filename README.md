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
