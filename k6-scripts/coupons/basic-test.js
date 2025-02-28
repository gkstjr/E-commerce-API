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