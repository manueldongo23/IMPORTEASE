<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="true" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ImportEase - Enterprise Access</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;600;700;900&family=JetBrains+Mono:wght@500;800&display=swap" rel="stylesheet">
    <link rel="icon" type="image/svg+xml" href="favicon.svg">
    <style>
        *, *::before, *::after { box-sizing: border-box; margin: 0; padding: 0; }

        body {
            font-family: 'Outfit', system-ui, sans-serif;
            min-height: 100vh;
            display: flex;
            background: #f5f0eb;
            -webkit-font-smoothing: antialiased;
        }

        /* ── LEFT PANEL ── */
        .login-left {
            flex: 1.15;
            position: relative;
            display: flex;
            flex-direction: column;
            justify-content: space-between;
            padding: 2.5rem 3rem;
            background: #0d0f14;
            overflow: hidden;
            min-height: 100vh;
        }

        /* Animated neural network canvas */
        #neuralCanvas {
            position: absolute;
            inset: 0;
            width: 100%;
            height: 100%;
            opacity: 0.85;
            z-index: 0;
        }

        /* Subtle radial gradient overlay */
        .login-left::after {
            content: '';
            position: absolute;
            inset: 0;
            background:
                radial-gradient(ellipse 80% 60% at 30% 40%, rgba(59,130,246,0.12) 0%, transparent 70%),
                radial-gradient(ellipse 60% 40% at 70% 70%, rgba(139,92,246,0.08) 0%, transparent 60%),
                linear-gradient(to bottom, rgba(13,15,20,0.15) 0%, rgba(13,15,20,0.5) 100%);
            pointer-events: none;
            z-index: 1;
        }

        .left-content {
            position: relative;
            z-index: 2;
            display: flex;
            flex-direction: column;
            justify-content: space-between;
            height: 100%;
        }

        /* Brand */
        .brand {
            display: flex;
            align-items: center;
            gap: 0.75rem;
        }

        .brand-icon {
            width: 2.75rem;
            height: 2.75rem;
            background: rgba(255,255,255,0.08);
            border: 1px solid rgba(255,255,255,0.12);
            border-radius: 0.875rem;
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .brand-icon svg {
            width: 1.25rem;
            height: 1.25rem;
            color: #6ee7f7;
        }

        .brand-name {
            font-size: 1.2rem;
            font-weight: 900;
            color: #fff;
            letter-spacing: -0.01em;
        }

        .brand-name span {
            color: #6ee7f7;
        }

        /* Hero text */
        .hero-block {
            margin-top: auto;
            padding-bottom: 1.5rem;
        }

        .hero-title {
            font-size: clamp(2.8rem, 5vw, 3.75rem);
            font-weight: 900;
            line-height: 1.05;
            letter-spacing: -0.03em;
            color: #fff;
        }

        .hero-title .accent {
            color: #6ee7f7;
        }

        .hero-desc {
            margin-top: 1.25rem;
            font-size: 0.9rem;
            line-height: 1.7;
            font-weight: 600;
            color: rgba(255,255,255,0.45);
            max-width: 26rem;
        }

        /* Bottom stats */
        .left-stats {
            display: grid;
            grid-template-columns: 1fr 1fr;
            gap: 1.5rem;
            border-top: 1px solid rgba(255,255,255,0.06);
            padding-top: 1.75rem;
            margin-top: 2.5rem;
        }

        .stat-item {
            display: flex;
            align-items: center;
            gap: 1rem;
        }

        .stat-ring {
            width: 2.75rem;
            height: 2.75rem;
            flex-shrink: 0;
        }

        .stat-ring svg {
            width: 100%;
            height: 100%;
            transform: rotate(-90deg);
        }

        .stat-ring .ring-bg {
            fill: none;
            stroke: rgba(255,255,255,0.08);
            stroke-width: 3.5;
        }

        .stat-ring .ring-fill {
            fill: none;
            stroke-width: 3.5;
            stroke-linecap: round;
            transition: stroke-dashoffset 1s cubic-bezier(0.16,1,0.3,1);
        }

        .stat-ring .ring-fill.cyan { stroke: #6ee7f7; }
        .stat-ring .ring-fill.violet { stroke: #a78bfa; }

        .stat-text .value {
            font-size: 1.6rem;
            font-weight: 900;
            color: #fff;
            line-height: 1;
            letter-spacing: -0.02em;
        }

        .stat-text .value span {
            font-size: 0.85rem;
            font-weight: 700;
            color: #6ee7f7;
        }

        .stat-text .label {
            font-size: 0.55rem;
            font-weight: 900;
            text-transform: uppercase;
            letter-spacing: 0.18em;
            color: rgba(255,255,255,0.35);
            margin-top: 0.2rem;
        }

        /* ── RIGHT PANEL ── */
        .login-right {
            flex: 1;
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 3rem 2.5rem;
            background: #f5f0eb;
            position: relative;
        }

        .login-right::before {
            content: '';
            position: absolute;
            inset: 0;
            background: radial-gradient(ellipse 70% 50% at 50% 0%, rgba(99,102,241,0.04) 0%, transparent 70%);
            pointer-events: none;
        }

        .form-card {
            width: 100%;
            max-width: 400px;
            position: relative;
            z-index: 1;
        }

        /* Status badge */
        .status-badge {
            display: inline-flex;
            align-items: center;
            gap: 0.5rem;
            padding: 0.35rem 0.875rem;
            background: rgba(5, 150, 105, 0.1);
            border: 1px solid rgba(5, 150, 105, 0.25);
            border-radius: 100px;
            margin-bottom: 1.25rem;
        }

        .status-dot {
            width: 7px;
            height: 7px;
            border-radius: 50%;
            background: #059669;
            animation: pulse-dot 2s ease-in-out infinite;
        }

        @keyframes pulse-dot {
            0%, 100% { opacity: 1; transform: scale(1); }
            50% { opacity: 0.65; transform: scale(0.85); }
        }

        .status-text {
            font-size: 0.65rem;
            font-weight: 900;
            text-transform: uppercase;
            letter-spacing: 0.12em;
            color: #059669;
        }

        /* Form header */
        .form-title {
            font-size: 2rem;
            font-weight: 900;
            color: #18181b;
            letter-spacing: -0.03em;
            line-height: 1.1;
            margin-bottom: 0.5rem;
        }

        .form-subtitle {
            font-size: 0.82rem;
            font-weight: 600;
            color: #71717a;
            margin-bottom: 2rem;
        }

        /* Field */
        .field {
            margin-bottom: 1.125rem;
        }

        .field-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 0.5rem;
        }

        .field-label {
            font-size: 0.6rem;
            font-weight: 900;
            text-transform: uppercase;
            letter-spacing: 0.18em;
            color: #a1a1aa;
        }

        .field-link {
            font-size: 0.6rem;
            font-weight: 900;
            text-transform: uppercase;
            letter-spacing: 0.12em;
            color: #6366f1;
            text-decoration: none;
            transition: opacity 0.15s;
        }

        .field-link:hover { opacity: 0.7; }

        .input-wrap {
            position: relative;
        }

        .input-icon {
            position: absolute;
            left: 1.1rem;
            top: 50%;
            transform: translateY(-50%);
            display: flex;
            align-items: center;
            color: #a1a1aa;
            pointer-events: none;
            transition: color 0.2s;
        }

        .input-icon svg { width: 1.1rem; height: 1.1rem; }

        .form-input {
            width: 100%;
            padding: 0.9rem 1rem 0.9rem 2.9rem;
            border: 1.5px solid #e4e4e7;
            border-radius: 0.875rem;
            background: #fff;
            font-family: 'Outfit', sans-serif;
            font-size: 0.85rem;
            font-weight: 600;
            color: #18181b;
            outline: none;
            transition: border-color 0.2s, box-shadow 0.2s;
        }

        .form-input::placeholder { color: #c4c4cc; font-weight: 500; }

        .form-input:focus {
            border-color: #6366f1;
            box-shadow: 0 0 0 4px rgba(99,102,241,0.1);
        }

        .form-input:focus ~ .input-icon { color: #6366f1; }
        .input-wrap:focus-within .input-icon { color: #6366f1; }

        .btn-toggle-pw {
            position: absolute;
            right: 1rem;
            top: 50%;
            transform: translateY(-50%);
            background: none;
            border: none;
            cursor: pointer;
            color: #a1a1aa;
            display: flex;
            align-items: center;
            transition: color 0.2s;
            padding: 0.25rem;
        }

        .btn-toggle-pw:hover { color: #6366f1; }
        .btn-toggle-pw svg { width: 1.1rem; height: 1.1rem; }

        /* Captcha */
        .captcha-box {
            background: #fff;
            border: 1.5px solid #e4e4e7;
            border-radius: 0.875rem;
            padding: 1rem 1.125rem;
            margin-bottom: 1.5rem;
        }

        .captcha-header {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-bottom: 0.75rem;
        }

        .captcha-label {
            font-size: 0.6rem;
            font-weight: 900;
            text-transform: uppercase;
            letter-spacing: 0.18em;
            color: #a1a1aa;
        }

        .captcha-refresh {
            font-size: 0.6rem;
            font-weight: 900;
            text-transform: uppercase;
            letter-spacing: 0.12em;
            color: #6366f1;
            background: none;
            border: none;
            cursor: pointer;
            transition: opacity 0.15s;
            font-family: 'Outfit', sans-serif;
        }

        .captcha-refresh:hover { opacity: 0.7; }

        .captcha-row {
            display: flex;
            gap: 0.875rem;
            align-items: center;
        }

        .captcha-img-wrap {
            flex: 0 0 48%;
            height: 46px;
            border-radius: 0.65rem;
            overflow: hidden;
            border: 1px solid #e4e4e7;
            background: #0d0f14;
        }

        .captcha-img-wrap img {
            width: 100%;
            height: 100%;
            object-fit: cover;
        }

        .captcha-input {
            flex: 1;
            padding: 0.75rem 1rem;
            border: 1.5px solid #e4e4e7;
            border-radius: 0.65rem;
            background: #f4f4f5;
            font-family: 'JetBrains Mono', monospace;
            font-size: 0.9rem;
            font-weight: 700;
            letter-spacing: 0.3em;
            text-transform: uppercase;
            text-align: center;
            color: #18181b;
            outline: none;
            transition: border-color 0.2s, box-shadow 0.2s;
        }

        .captcha-input::placeholder { color: #c4c4cc; letter-spacing: 0.15em; font-size: 0.8rem; }
        .captcha-input:focus { border-color: #6366f1; box-shadow: 0 0 0 3px rgba(99,102,241,0.1); }

        /* Submit button */
        .btn-submit {
            width: 100%;
            padding: 1rem;
            background: #6366f1;
            color: #fff;
            border: none;
            border-radius: 0.875rem;
            font-family: 'Outfit', sans-serif;
            font-size: 0.72rem;
            font-weight: 900;
            letter-spacing: 0.2em;
            text-transform: uppercase;
            cursor: pointer;
            transition: background 0.2s, transform 0.15s, box-shadow 0.2s;
            box-shadow: 0 4px 18px -4px rgba(99,102,241,0.45);
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 0.6rem;
        }

        .btn-submit:hover {
            background: #4f46e5;
            box-shadow: 0 6px 24px -4px rgba(99,102,241,0.55);
        }

        .btn-submit:active { transform: scale(0.98); }

        .btn-submit:disabled {
            opacity: 0.7;
            cursor: not-allowed;
        }

        /* Register link */
        .register-link {
            text-align: center;
            margin-top: 1rem;
            font-size: 0.65rem;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 0.1em;
            color: #a1a1aa;
        }

        .register-link a {
            color: #6366f1;
            font-weight: 900;
            text-decoration: none;
            margin-left: 0.25rem;
        }

        .register-link a:hover { text-decoration: underline; }

        /* Partners */
        .partners {
            display: flex;
            align-items: center;
            justify-content: center;
            gap: 2rem;
            margin-top: 2rem;
            padding-top: 1.25rem;
            border-top: 1px solid rgba(0,0,0,0.06);
        }

        .partner-badge {
            display: flex;
            align-items: center;
            gap: 0.5rem;
            font-size: 0.78rem;
            font-weight: 900;
            letter-spacing: -0.01em;
        }

        .partner-badge .p-icon {
            width: 1.5rem;
            height: 1.5rem;
            border-radius: 50%;
            display: flex;
            align-items: center;
            justify-content: center;
            font-size: 0.75rem;
        }

        .partner-vuce { color: #1a5276; }
        .partner-vuce .p-icon { background: rgba(26,82,118,0.1); }
        .partner-sunat { color: #922b21; }
        .partner-sunat .p-icon { background: rgba(146,43,33,0.1); }

        /* Toast */
        .toast {
            position: fixed;
            top: 1.5rem;
            left: 50%;
            transform: translate(-50%, 0) scale(0.95);
            z-index: 9999;
            display: flex;
            align-items: center;
            gap: 1rem;
            padding: 1rem 1.5rem;
            border-radius: 1rem;
            background: #fff;
            border: 1px solid #e4e4e7;
            box-shadow: 0 20px 40px -8px rgba(0,0,0,0.12);
            opacity: 0;
            pointer-events: none;
            transition: all 0.4s cubic-bezier(0.16,1,0.3,1);
            max-width: 380px;
            width: calc(100% - 3rem);
        }

        .toast.show {
            opacity: 1;
            pointer-events: auto;
            transform: translate(-50%, 0) scale(1);
        }

        .toast-icon { font-size: 1.25rem; flex-shrink: 0; }
        .toast-title { font-size: 0.72rem; font-weight: 900; text-transform: uppercase; letter-spacing: 0.12em; }
        .toast-msg { font-size: 0.75rem; font-weight: 600; color: #71717a; margin-top: 0.15rem; }

        /* Spinner */
        .spinner {
            width: 1rem; height: 1rem;
            border: 2px solid rgba(255,255,255,0.3);
            border-top-color: #fff;
            border-radius: 50%;
            animation: spin 0.6s linear infinite;
        }

        @keyframes spin { to { transform: rotate(360deg); } }

        /* Responsive */
        @media (max-width: 900px) {
            .login-left { display: none; }
            .login-right { flex: 1; }
            body { background: #f5f0eb; }
        }

        /* Fade-up entry animation */
        .fade-up {
            animation: fadeUp 0.6s cubic-bezier(0.16,1,0.3,1) both;
        }

        @keyframes fadeUp {
            from { opacity: 0; transform: translateY(20px); }
            to   { opacity: 1; transform: translateY(0); }
        }
    </style>
</head>
<body>

    <!-- Toast -->
    <div id="toast" class="toast">
        <span class="toast-icon" id="toastIcon"></span>
        <div>
            <p class="toast-title" id="toastTitle"></p>
            <p class="toast-msg" id="toastMsg"></p>
        </div>
    </div>

    <!-- LEFT PANEL -->
    <div class="login-left">
        <canvas id="neuralCanvas"></canvas>

        <div class="left-content">
            <!-- Brand -->
            <div class="brand">
                <div class="brand-icon">
                    <svg fill="none" stroke="currentColor" stroke-width="2" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" d="M3 12h18M12 3c4.971 0 9 4.029 9 9s-4.029 9-9 9-9-4.029-9-9 4.029-9 9-9z"/>
                    </svg>
                </div>
                <span class="brand-name">ImportEase <span>Enterprise</span></span>
            </div>

            <!-- Hero -->
            <div class="hero-block">
                <h1 class="hero-title">
                    Logistics<br>
                    <span class="accent">Intelligence.</span>
                </h1>
                <p class="hero-desc">
                    Plataforma unificada para el control técnico de importaciones, clasificación arancelaria y cumplimiento regulatorio en tiempo real.
                </p>

                <!-- Stats -->
                <div class="left-stats">
                    <div class="stat-item">
                        <div class="stat-ring">
                            <svg viewBox="0 0 36 36">
                                <circle class="ring-bg" cx="18" cy="18" r="15.5"/>
                                <circle class="ring-fill cyan" cx="18" cy="18" r="15.5"
                                    stroke-dasharray="97.4"
                                    stroke-dashoffset="85"/>
                            </svg>
                        </div>
                        <div class="stat-text">
                            <div class="value">2.4<span>s</span></div>
                            <div class="label">Sincronización SUNAT</div>
                        </div>
                    </div>
                    <div class="stat-item">
                        <div class="stat-ring">
                            <svg viewBox="0 0 36 36">
                                <circle class="ring-bg" cx="18" cy="18" r="15.5"/>
                                <circle class="ring-fill violet" cx="18" cy="18" r="15.5"
                                    stroke-dasharray="97.4"
                                    stroke-dashoffset="0"/>
                            </svg>
                        </div>
                        <div class="stat-text">
                            <div class="value">100<span>%</span></div>
                            <div class="label">Cumplimiento VUCE</div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- RIGHT PANEL -->
    <div class="login-right">
        <div class="form-card fade-up">
            <!-- Status -->
            <div class="status-badge">
                <div class="status-dot"></div>
                <span class="status-text">Systems Operational</span>
            </div>

            <h2 class="form-title">Bienvenido al Cockpit</h2>
            <p class="form-subtitle">Ingresa tus credenciales corporativas para continuar.</p>

            <form id="loginForm">
                <!-- Email -->
                <div class="field">
                    <div class="field-header">
                        <label class="field-label" for="email">Email Corporativo</label>
                    </div>
                    <div class="input-wrap">
                        <span class="input-icon">
                            <svg fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" d="M21.75 6.75v10.5a2.25 2.25 0 01-2.25 2.25h-15a2.25 2.25 0 01-2.25-2.25V6.75m19.5 0A2.25 2.25 0 0019.5 4.5h-15a2.25 2.25 0 00-2.25 2.25m19.5 0v.243a2.25 2.25 0 01-1.07 1.916l-7.5 4.615a2.25 2.25 0 01-2.36 0L3.32 8.91a2.25 2.25 0 01-1.07-1.916V6.75"/>
                            </svg>
                        </span>
                        <input type="email" id="email" class="form-input" placeholder="ejemplo@empresa.com" required autocomplete="email">
                    </div>
                </div>

                <!-- Password -->
                <div class="field">
                    <div class="field-header">
                        <label class="field-label" for="password">Clave de Acceso</label>
                        <a href="recuperar.jsp" class="field-link">Recuperar</a>
                    </div>
                    <div class="input-wrap">
                        <span class="input-icon">
                            <svg fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" d="M15.75 5.25a3 3 0 013 3m3 0a6 6 0 01-7.029 5.912c-.563-.097-1.159.026-1.563.43L10.5 17.25H8.25v2.25H6v2.25H2.25v-2.818c0-.597.237-1.17.659-1.591l6.499-6.499c.404-.404.527-1 .43-1.563A6 6 0 1121.75 8.25z"/>
                            </svg>
                        </span>
                        <input type="password" id="password" class="form-input" placeholder="••••••••" required autocomplete="current-password">
                        <button type="button" class="btn-toggle-pw" id="togglePw" aria-label="Mostrar contraseña">
                            <svg fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" d="M2.036 12.322a1.012 1.012 0 010-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178z"/>
                                <path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/>
                            </svg>
                        </button>
                    </div>
                </div>

                <!-- Captcha -->
                <div class="captcha-box">
                    <div class="captcha-header">
                        <span class="captcha-label">Human Verification</span>
                        <button type="button" class="captcha-refresh" onclick="refreshCaptcha()">Actualizar</button>
                    </div>
                    <div class="captcha-row">
                        <div class="captcha-img-wrap">
                            <img id="captchaImg" src="captcha" alt="captcha">
                        </div>
                        <input type="text" id="captcha" class="captcha-input" placeholder="Input ···" maxlength="5" required autocomplete="off">
                    </div>
                </div>

                <!-- Submit -->
                <button type="submit" id="btnLogin" class="btn-submit">
                    Acceder al Cockpit
                </button>

                <p class="register-link">
                    ¿Sin cuenta?
                    <a href="registro.jsp">Registrar Empresa</a>
                </p>
            </form>

            <!-- Partners -->
            <div class="partners">
                <div class="partner-badge partner-vuce">
                    <div class="p-icon">🏛️</div>
                    VUCE
                </div>
                <div class="partner-badge partner-sunat">
                    <div class="p-icon">📋</div>
                    SUNAT
                </div>
            </div>
        </div>
    </div>

    <script nonce="<%= request.getAttribute("csp_nonce") %>">
        /* ── Neural Network Canvas ── */
        (function() {
            const canvas = document.getElementById('neuralCanvas');
            const ctx = canvas.getContext('2d');

            let W, H, nodes = [], frame = 0;

            function resize() {
                W = canvas.width  = canvas.offsetWidth;
                H = canvas.height = canvas.offsetHeight;
                initNodes();
            }

            function initNodes() {
                nodes = [];
                const count = Math.floor((W * H) / 9000);
                for (let i = 0; i < count; i++) {
                    nodes.push({
                        x: Math.random() * W,
                        y: Math.random() * H,
                        vx: (Math.random() - 0.5) * 0.35,
                        vy: (Math.random() - 0.5) * 0.35,
                        r: Math.random() * 1.8 + 0.6,
                        hue: Math.random() > 0.5 ? 195 : 260,
                        pulse: Math.random() * Math.PI * 2
                    });
                }
            }

            function draw() {
                ctx.clearRect(0, 0, W, H);
                frame++;

                nodes.forEach(n => {
                    n.x += n.vx;
                    n.y += n.vy;
                    if (n.x < 0 || n.x > W) n.vx *= -1;
                    if (n.y < 0 || n.y > H) n.vy *= -1;
                    n.pulse += 0.018;
                });

                // Draw connections
                const maxDist = Math.min(W, H) * 0.28;
                for (let i = 0; i < nodes.length; i++) {
                    for (let j = i + 1; j < nodes.length; j++) {
                        const a = nodes[i], b = nodes[j];
                        const dx = a.x - b.x, dy = a.y - b.y;
                        const dist = Math.sqrt(dx*dx + dy*dy);
                        if (dist < maxDist) {
                            const alpha = (1 - dist / maxDist) * 0.18;
                            const midHue = (a.hue + b.hue) / 2;
                            const grad = ctx.createLinearGradient(a.x, a.y, b.x, b.y);
                            grad.addColorStop(0, `hsla(${a.hue}, 80%, 70%, ${alpha})`);
                            grad.addColorStop(1, `hsla(${b.hue}, 80%, 70%, ${alpha})`);
                            ctx.beginPath();
                            ctx.moveTo(a.x, a.y);
                            ctx.lineTo(b.x, b.y);
                            ctx.strokeStyle = grad;
                            ctx.lineWidth = 0.7;
                            ctx.stroke();
                        }
                    }
                }

                // Draw nodes
                nodes.forEach(n => {
                    const glow = (Math.sin(n.pulse) * 0.5 + 0.5) * 0.6 + 0.2;
                    ctx.beginPath();
                    ctx.arc(n.x, n.y, n.r, 0, Math.PI * 2);
                    ctx.fillStyle = `hsla(${n.hue}, 80%, 72%, ${glow})`;
                    ctx.fill();

                    // Soft glow
                    ctx.beginPath();
                    ctx.arc(n.x, n.y, n.r * 3.5, 0, Math.PI * 2);
                    const radGrad = ctx.createRadialGradient(n.x, n.y, 0, n.x, n.y, n.r * 3.5);
                    radGrad.addColorStop(0, `hsla(${n.hue}, 80%, 72%, ${glow * 0.12})`);
                    radGrad.addColorStop(1, `hsla(${n.hue}, 80%, 72%, 0)`);
                    ctx.fillStyle = radGrad;
                    ctx.fill();
                });

                requestAnimationFrame(draw);
            }

            window.addEventListener('resize', resize);
            resize();
            draw();
        })();

        /* ── Toggle password ── */
        document.getElementById('togglePw').addEventListener('click', function() {
            const inp = document.getElementById('password');
            const isText = inp.type === 'text';
            inp.type = isText ? 'password' : 'text';
            this.innerHTML = isText
                ? `<svg fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M2.036 12.322a1.012 1.012 0 010-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178z"/><path stroke-linecap="round" stroke-linejoin="round" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"/></svg>`
                : `<svg fill="none" stroke="currentColor" stroke-width="1.8" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M3.98 8.223A10.477 10.477 0 001.934 12C3.226 16.338 7.244 19.5 12 19.5c.993 0 1.953-.138 2.863-.395M6.228 6.228A10.45 10.45 0 0112 4.5c4.756 0 8.773 3.162 10.065 7.498a10.523 10.523 0 01-4.293 5.774M6.228 6.228L3 3m3.228 3.228l3.65 3.65m7.894 7.894L21 21m-3.228-3.228l-3.65-3.65m0 0a3 3 0 10-4.243-4.243m4.242 4.242L9.88 9.88" /></svg>`;
        });

        /* ── Captcha ── */
        function refreshCaptcha() {
            document.getElementById('captchaImg').src = 'captcha?' + Date.now();
            document.getElementById('captcha').value = '';
        }

        /* ── Toast ── */
        let toastTimer;
        function showToast(title, msg, success) {
            const el = document.getElementById('toast');
            document.getElementById('toastIcon').textContent = success ? '✨' : '⚠️';
            const tEl = document.getElementById('toastTitle');
            tEl.textContent = title;
            tEl.style.color = success ? '#059669' : '#dc2626';
            document.getElementById('toastMsg').textContent = msg;
            el.classList.add('show');
            clearTimeout(toastTimer);
            toastTimer = setTimeout(() => el.classList.remove('show'), 4000);
        }

        /* ── Login form ── */
        document.getElementById('loginForm').addEventListener('submit', async (e) => {
            e.preventDefault();
            const btn = document.getElementById('btnLogin');
            btn.disabled = true;
            btn.innerHTML = `<div class="spinner"></div> AUTENTICANDO...`;

            try {
                const res = await fetch('<%= request.getContextPath() %>/api/usuario/login', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({
                        email: document.getElementById('email').value,
                        password: document.getElementById('password').value,
                        captcha: document.getElementById('captcha').value
                    })
                });

                const data = await res.json();
                if (data.success) {
                    btn.style.background = '#059669';
                    btn.innerHTML = '✓ CONEXIÓN ESTABLECIDA';
                    showToast('Sesión Autorizada', 'Redirigiendo al cockpit de aduanas...', true);
                    setTimeout(() => window.location.href = 'dashboard.jsp', 150);
                } else {
                    showToast('Autenticación Fallida', data.mensaje || 'Credenciales inválidas', false);
                    refreshCaptcha();
                    btn.disabled = false;
                    btn.innerHTML = 'Acceder al Cockpit';
                }
            } catch {
                showToast('Fallo de Red', 'Error de conexión con el servidor.', false);
                btn.disabled = false;
                btn.innerHTML = 'Acceder al Cockpit';
            }
        });
    </script>
</body>
</html>
