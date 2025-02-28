import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
    vus: 10000,       // 동시 사용자 10000명
    duration: '3m'   // 3분 동안 유지
};

export default function () {
    let couponId = 1;
    let url = `http://localhost:8080/coupons/${couponId}`;

    let payload = JSON.stringify({
        userId: 1
    });

    let params = {
        headers: {
            'Content-Type': 'application/json'
        }
    };

    let res = http.post(url, payload, params);
    check(res, {
        'is status 200': (r) => r.status === 200,
    });
    sleep(1);
}