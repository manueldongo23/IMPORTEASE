<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="true" %>
<%
    if (session.getAttribute("usuarioId") == null) {
        response.sendRedirect("login.jsp"); return;
    }
%>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ImportEase - Compra personal</title>
    <link href="https://fonts.googleapis.com/css2?family=Outfit:wght@300;400;600;700;900&display=swap" rel="stylesheet">
    <link href="css/tailwind-output.css" rel="stylesheet">
    <link href="css/main.css" rel="stylesheet">
    <script nonce="<%= request.getAttribute("csp_nonce") %>" src="js/toast.js"></script>
    <style>
        .courier-card {
            background: var(--surface-1);
            border: 1px solid var(--border);
            border-radius: 2rem;
            transition: all 0.4s var(--ease-out-expo);
            box-shadow: var(--shadow-card);
        }
        .courier-card:hover {
            border-color: var(--accent) !important;
            transform: translateY(-2px);
            box-shadow: var(--shadow-card-hover);
        }
        .courier-input {
            width: 100%;
            background: var(--surface-1);
            border: 1px solid var(--border);
            border-radius: 1rem;
            padding: 1rem 1.5rem;
            color: var(--text-primary);
            font-weight: 700;
            outline: none;
            transition: all var(--duration-fast) var(--ease-out-quart);
        }
        .courier-input:focus {
            border-color: var(--accent) !important;
            box-shadow: 0 0 0 4px var(--accent-glow) !important;
        }
        .tone-warning {
            background: var(--warning-soft);
            border: 1px solid rgba(217, 119, 6, 0.15);
        }
        .tone-warning-text {
            color: var(--warning);
        }
        .tone-danger {
            background: var(--danger-soft);
            border: 1px solid rgba(220, 38, 38, 0.15);
        }
        .tone-danger-strong {
            color: var(--danger);
        }
        .surface-chip {
            background: var(--accent-soft);
            border: 1px solid var(--accent-glow);
            color: var(--accent);
        }
        .panel-state {
            background: var(--surface-1);
            border: 1px solid var(--border);
            box-shadow: var(--shadow-card);
        }
        .panel-state.free {
            background: var(--accent-soft);
            border-color: var(--accent-glow);
        }
        .panel-state.taxed {
            background: var(--warning-soft);
            border-color: rgba(217, 119, 6, 0.25);
        }
        .state-icon {
            color: var(--accent);
            background: var(--accent-soft);
            box-shadow: 0 4px 12px var(--accent-soft);
        }
        .state-icon.taxed {
            color: var(--warning);
            background: var(--warning-soft);
            box-shadow: 0 4px 12px var(--warning-soft);
        }
        .state-title {
            color: var(--text-primary);
        }
        .state-title.taxed {
            color: var(--warning);
        }
        .state-total {
            color: var(--accent);
        }
    </style>
</head>
<body class="flex h-screen overflow-hidden bg-grid font-sans text-[var(--text-primary)] bg-[var(--surface-0)]">
    <jsp:include page="/fragments/toast.jsp" />
    <% request.setAttribute("activePage", "calculadora"); %>
    <jsp:include page="/fragments/sidebar.jsp" />

    <main class="flex-1 overflow-y-auto custom-scrollbar flex flex-col">
        <!-- Top Header Bar -->
        <header class="h-16 border-b border-[var(--border)] px-10 flex items-center justify-between bg-white/40 backdrop-blur-xl z-10 sticky top-0 shrink-0">
            <div class="px-4 py-1.5 bg-[var(--accent-soft)] rounded-full flex items-center gap-3 border border-[var(--accent-glow)]">
                <span class="w-2 h-2 rounded-full bg-[var(--accent)] animate-pulse"></span>
                <span class="text-[11px] font-black text-[var(--accent)] uppercase tracking-[0.2em]">Compra personal guiada</span>
            </div>
        </header>

        <!-- Content Area -->
        <div class="p-12 max-w-4xl mx-auto space-y-10 w-full flex-1">
            <a href="evaluacion.jsp" class="inline-flex items-center gap-2 text-xs font-black text-[var(--accent)] hover:underline uppercase tracking-widest">&larr; Volver a Importar paso a paso</a>
            <div class="tone-warning rounded-2xl p-4 flex items-center gap-4 backdrop-blur-md">
                <span class="text-2xl">⚠️</span>
                <div class="text-xs tone-warning-text font-medium">
                    <strong class="tone-warning-text font-bold uppercase tracking-widest text-[10px] block mb-1">Calculo referencial</strong>
                    Usa esta pantalla para compras personales por courier. Antes de pagar, revisa si el producto necesita permiso o tiene limites especiales.
                </div>
            </div>

            <div class="flex justify-between items-start gap-6 border-b border-[var(--border)] pb-6">
                <div>
                    <span class="pill-heading">Compra personal</span>
                    <h2 class="text-4xl font-black mb-2 tracking-tight text-[var(--text-primary)] mt-3">Calcula antes de comprar</h2>
                    <p class="text-[var(--text-secondary)] text-sm font-semibold">Estimador simple para compras por courier menores a USD 2,000.</p>
                </div>
                <div class="flex flex-col items-end gap-2">
                    <div id="catBadge" class="surface-chip px-4 py-1.5 rounded-full text-[10px] font-black uppercase tracking-widest border border-[var(--accent-glow)]">
                        Hasta USD 200
                    </div>
                </div>
            </div>

            <div id="costPanel" class="panel-state free p-8 rounded-[2rem] flex items-center gap-8 transition-all">
                <div id="costIcon" class="state-icon w-20 h-20 rounded-3xl flex items-center justify-center text-4xl font-black shadow-lg">*</div>
                <div class="flex-1">
                    <h3 id="costTitle" class="state-title text-2xl font-black mb-1">Cero impuestos</h3>
                    <p id="costSub" class="text-sm text-[var(--text-secondary)] max-w-md font-semibold">Tu importación está por debajo de USD 200. En ese rango normalmente no pagas tributos de aduanas.</p>
                </div>
                <div class="text-right">
                    <p class="text-[10px] font-black text-[var(--text-tertiary)] uppercase mb-1">Pago estimado</p>
                    <p id="costTotal" class="state-total text-4xl font-black">$ 0.00</p>
                </div>
            </div>

            <div id="vuceSection" class="tone-danger hidden p-8 rounded-[2rem]">
                <div class="flex items-center gap-4 mb-4">
                    <span class="text-2xl">!</span>
                    <h3 class="tone-danger-strong text-lg font-black uppercase tracking-widest">Revisa permiso antes de comprar</h3>
                </div>
                <p class="text-sm text-[var(--text-secondary)] mb-6 font-semibold">El producto "<span id="prodName" class="text-[var(--text-primary)] font-bold"></span>" puede necesitar una revision previa. Mira la entidad <span id="vuceEntidad" class="text-[var(--text-primary)] font-black underline"></span> antes de pagar o enviarlo.</p>
            </div>

            <div class="courier-card p-10 space-y-8">
                <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
                    <div class="space-y-3">
                        <label class="text-xs font-black text-[var(--text-secondary)] uppercase tracking-widest ml-1">Precio del producto (USD)</label>
                        <input type="number" id="valFob" oninput="recalcular()" class="courier-input text-gray-800">
                    </div>
                    <div class="space-y-3">
                        <label class="text-xs font-black text-[var(--text-secondary)] uppercase tracking-widest ml-1">País de origen</label>
                        <select id="paisOrigen" class="courier-input text-gray-800 cursor-pointer">
                            <option value="US">Estados Unidos</option>
                            <option value="CN">China</option>
                            <option value="ES">España</option>
                            <option value="Otros">Otros</option>
                        </select>
                    </div>
                </div>

                <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
                    <div class="p-4 rounded-2xl bg-[var(--accent-soft)] border border-[var(--accent-glow)]">
                        <p class="text-[10px] font-black uppercase tracking-widest text-[var(--accent)]">Paso 1</p>
                        <p class="text-xs text-[var(--text-secondary)] font-semibold mt-2">Coloca el valor real del producto sin inventar montos.</p>
                    </div>
                    <div class="p-4 rounded-2xl bg-[var(--accent-soft)] border border-[var(--accent-glow)]">
                        <p class="text-[10px] font-black uppercase tracking-widest text-[var(--accent)]">Paso 2</p>
                        <p class="text-xs text-[var(--text-secondary)] font-semibold mt-2">Si tu producto es sensible, revisa permiso antes de pagarle al proveedor.</p>
                    </div>
                    <div class="p-4 rounded-2xl bg-[var(--accent-soft)] border border-[var(--accent-glow)]">
                        <p class="text-[10px] font-black uppercase tracking-widest text-[var(--accent)]">Paso 3</p>
                        <p class="text-xs text-[var(--text-secondary)] font-semibold mt-2">Guarda la simulación para compararla luego con el flujo comercial.</p>
                    </div>
                </div>

                <button onclick="guardar()" class="primary-button w-full py-5 text-xs font-black uppercase tracking-widest transition-all">
                    Guardar compra personal
                </button>
            </div>
        </div>
    </main>

    <script nonce="<%= request.getAttribute("csp_nonce") %>">
        const params = new URLSearchParams(window.location.search);

        function init() {
            const fob = parseFloat(params.get('fob')) || 50;
            document.getElementById('valFob').value = fob;

            const desc = decodeURIComponent(params.get('desc') || 'Producto');
            document.getElementById('prodName').innerText = desc;

            checkVuce(desc);
            recalcular();
        }

        function recalcular() {
            const fob = parseFloat(document.getElementById('valFob').value) || 0;
            const panel = document.getElementById('costPanel');
            const icon = document.getElementById('costIcon');
            const title = document.getElementById('costTitle');
            const sub = document.getElementById('costSub');
            const total = document.getElementById('costTotal');
            const badge = document.getElementById('catBadge');

            if (fob < 200) {
                panel.className = "panel-state free p-8 rounded-[2rem] flex items-center gap-8 transition-all";
                icon.innerText = "*";
                icon.className = "state-icon w-20 h-20 rounded-3xl flex items-center justify-center text-4xl font-black shadow-lg";
                title.innerText = "Cero impuestos";
                title.className = "state-title text-2xl font-black mb-1";
                sub.innerText = "Tu compra esta en el tramo menor a USD 200. Normalmente no pagas impuestos de aduanas en este rango.";
                total.innerText = "$ 0.00";
                badge.innerText = "Hasta USD 200";
            } else if (fob <= 2000) {
                const cif = fob * 1.1;
                const imp = cif * 0.22;

                panel.className = "panel-state taxed p-8 rounded-[2rem] flex items-center gap-8 transition-all";
                icon.innerText = "$";
                icon.className = "state-icon taxed w-20 h-20 rounded-3xl flex items-center justify-center text-4xl font-black shadow-lg";
                title.innerText = "Impuesto simplificado";
                title.className = "state-title taxed text-2xl font-black mb-1";
                sub.innerText = "Tu compra entra al tramo de USD 200 a USD 2000. El calculo usa una base estimada y puede variar.";
                total.innerText = "$ " + imp.toFixed(2);
                badge.innerText = "USD 200 - USD 2000";
            } else {
                showNotification('Atención', 'Compras mayores a USD 2000 deben revisarse como importación comercial.', false);
                window.location.href = "calculadora-negocio.jsp" + window.location.search;
            }
        }

        function checkVuce(desc) {
            const d = desc.toLowerCase();
            let entidad = "";

            if (d.includes('celular') || d.includes('radio') || d.includes('telecom')) entidad = "MTC";
            if (d.includes('cosmet') || d.includes('perfum') || d.includes('medic')) entidad = "DIGEMID";
            if (d.includes('suplement') || d.includes('vitamin') || d.includes('alimento')) entidad = "DIGESA / SENASA";

            if (entidad) {
                document.getElementById('vuceSection').classList.remove('hidden');
                document.getElementById('vuceEntidad').innerText = entidad;
            }
        }

        async function guardar() {
            const data = {
                hsCode: params.get('hsCode') || 'PERSONAL',
                fob: document.getElementById('valFob').value,
                flete: 0,
                seguro: 0,
                tipo: 'PERSONAL',
                productoDesc: decodeURIComponent(params.get('desc') || 'Compra personal'),
                paisOrigen: document.getElementById('paisOrigen').value
            };

            try {
                const res = await fetch('<%= request.getContextPath() %>/api/importacion/cotizar', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'X-CSRF-TOKEN': window.csrfToken || ''
                    },
                    body: JSON.stringify(data)
                });

                if (res.ok) {
                    window.location.href = 'dashboard.jsp';
                } else {
                    showNotification('Error', 'No pudimos guardar la compra', false);
                }
            } catch (e) {
                showNotification('Error', 'No pudimos conectar para guardar', false);
            }
        }

        window.onload = init;
    </script>
</body>
</html>
