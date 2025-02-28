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
    let res = http.get('http://localhost:8080/products?page=0&size=10');
    check(res, {
        'is status 200': (r) => r.status === 200,
    });
    sleep(1);
}