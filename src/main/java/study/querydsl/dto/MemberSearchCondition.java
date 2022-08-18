package study.querydsl.dto;

import lombok.Data;

/**
 * 동적 쿼리의 컨디션
 * 동적쿼리를 만들 땐, 최소한의 기본 조건이 있거나 limit, paging 을 해주는걸 추천한다
 * 만약 동적쿼리인데 조건이 아무것도 안들어가면 쿼리 한번에 모든 데이터를 다 가져오는 불상사가 발생한다.
 */
@Data
public class MemberSearchCondition {

    private String username;
    private String teamName;
    private Integer ageGoe;
    private Integer ageLoe;
}
