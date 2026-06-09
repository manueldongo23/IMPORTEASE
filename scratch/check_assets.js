const http = require('http');

const urls = [
    'http://localhost:8084/registro.jsp',
    'http://localhost:8084/css/registro.css',
    'http://localhost:8084/js/registro.js',
    'http://localhost:8084/css/main.css',
    'http://localhost:8084/css/tailwind-output.css'
];

function checkUrl(url) {
    return new Promise((resolve) => {
        http.get(url, (res) => {
            console.log(`URL: ${url}`);
            console.log(`Status: ${res.statusCode}`);
            console.log(`Headers: ${JSON.stringify(res.headers, null, 2)}`);
            let body = '';
            res.on('data', chunk => { body += chunk; });
            res.on('end', () => {
                console.log(`Body Length: ${body.length}`);
                console.log(`First 200 chars: ${body.substring(0, 200).replace(/\s+/g, ' ')}`);
                console.log('--------------------------------------------------');
                resolve();
            });
        }).on('error', (err) => {
            console.log(`URL: ${url} failed: ${err.message}`);
            console.log('--------------------------------------------------');
            resolve();
        });
    });
}

async function run() {
    for (const url of urls) {
        await checkUrl(url);
    }
}

run();
