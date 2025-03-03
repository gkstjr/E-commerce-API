import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
    stages: [
        { duration: '1m', target: 100 }, // 1분 동안 100명까지 증가
        { duration: '1m', target: 300 }, // 1분 동안 300명까지 증가
        { duration: '1m', target: 500 }, // 1분 동안 500명까지 증가 후 유지
    ],
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