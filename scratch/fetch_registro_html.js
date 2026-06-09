const http = require('http');

http.get('http://localhost:8084/registro.jsp', (res) => {
    let body = '';
    res.on('data', chunk => { body += chunk; });
    res.on('end', () => {
        console.log(`STATUS: ${res.statusCode}`);
        console.log(`CSP HEADER: ${res.headers['content-security-policy']}`);
        
        // Print the lines containing stylesheet links and script tags
        const lines = body.split('\n');
        lines.forEach((line, index) => {
            if (line.includes('<link') || line.includes('<script') || line.includes('split-sidebar') || line.includes('split-form')) {
                console.log(`L${index + 1}: ${line.trim()}`);
            }
        });
    });
});
