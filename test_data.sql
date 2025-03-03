SET SESSION cte_max_recursion_depth = 10000000;

-- 상품 가상 데이터 삽입
INSERT INTO hhplus.product (created_at, updated_at, price, name)
WITH RECURSIVE cte (n) AS (
    SELECT 1
    UNION ALL
    SELECT n + 1 FROM cte WHERE n < 100000
)
SELECT
    NOW() - INTERVAL FLOOR(RAND() * 365) DAY AS created_at, -- 랜덤 생성일 (최근 1년 이내)
    NOW() AS updated_at, -- 현재 시간 기준
    FLOOR(RAND() * 99000 + 1000) AS price, -- 1,000 ~ 100,000원 사이 가격
    CONCAT('상품_', LPAD(n, 4, '0')) AS name -- "상품_0001" ~ "상품_1000" 형태의 이름
FROM cte;


-- 가상 데이터 삽입 쿼리 틀
INSERT INTO 테이블명 (컬럼1명, 컬럼2명, 컬럼3명)
WITH RECURSIVE cte (n) AS (
    SELECT 1
    UNION ALL
    SELECT n + 1 FROM cte WHERE n < 반복횟수
)
SELECT
    컬럼1값,
    컬럼2값,
    컬럼3값
FROM cte;