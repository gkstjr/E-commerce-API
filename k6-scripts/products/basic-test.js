import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
    vus: 500,       // 50명의 동시 사용자
    duration: '3m' // 3분 동안 실행
};

export default function () {
    let res = http.get('http://localhost:8080/products?page=0&size=10');
    check(res, {
        'is status 200': (r) => r.status === 200,
    });
    sleep(1);
}