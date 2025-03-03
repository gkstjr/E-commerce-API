import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
    vus: 1000,      // 동시 사용자 10,000명
    duration: '3m'   // 3분 동안 유지
};

export default function () {
    let res = http.get('http://localhost:8080/products?page=0&size=10');
    check(res, {
        'is status 200': (r) => r.status === 200,
    });
    sleep(1); // 각 사용자가 1초마다 요청
}
