<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="true" %>
<!-- Help Modal (Accessible & SEO Compliant) -->
<div id="globalHelpModal" class="hidden fixed inset-0 z-[200] flex items-center justify-center transition-opacity duration-300 opacity-0"
     role="dialog" aria-modal="true" aria-labelledby="helpModalTitle">
    <!-- Backdrop -->
    <div class="absolute inset-0 bg-[var(--nav-bg)]/60 backdrop-blur-md" aria-hidden="true" onclick="closeHelpModal()"></div>
    
    <!-- Modal Content -->
    <div class="relative bg-[var(--surface-1)] border border-[var(--border)] rounded-2xl shadow-xl w-[90%] max-w-lg p-6 transform scale-95 transition-transform duration-300"
         id="helpModalDialog">
        <header class="flex items-center justify-between border-b border-[var(--border)] pb-4 mb-4">
            <h2 id="helpModalTitle" class="text-xl font-black tracking-tight text-[var(--text-primary)]">Ayuda ImportEase</h2>
            <button type="button" class="text-[var(--text-tertiary)] hover:text-[var(--text-primary)] transition-colors p-2 -mr-2"
                    aria-label="Cerrar ayuda" onclick="closeHelpModal()">
                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                </svg>
            </button>
        </header>

        <section class="space-y-4 text-sm text-[var(--text-secondary)] font-medium max-h-[60vh] overflow-y-auto custom-scrollbar pr-2">
            <div>
                <h3 class="text-xs font-black uppercase tracking-widest text-[var(--accent)] mb-2">Primeros Pasos</h3>
                <p>Usa la seccion <strong>Importar paso a paso</strong> para declarar tu producto. El sistema te guiara sobre que documentos y permisos necesitas.</p>
            </div>
            <div class="p-4 bg-[var(--surface-2)] rounded-xl border border-[var(--border-subtle)]">
                <h3 class="text-xs font-black uppercase tracking-widest text-[var(--accent)] mb-2">Navegacion Principal</h3>
                <ul class="list-disc list-inside space-y-1">
                    <li><strong class="text-[var(--text-primary)]">Inicio:</strong> Resumen de tus KPIs.</li>
                    <li><strong class="text-[var(--text-primary)]">Expediente:</strong> Documentacion agrupada.</li>
                    <li><strong class="text-[var(--text-primary)]">Revision:</strong> Evaluacion final aduanera.</li>
                </ul>
            </div>
            <div>
                <h3 class="text-xs font-black uppercase tracking-widest text-[var(--accent)] mb-2">Atajos</h3>
                <p>Puedes usar las calculadoras y simuladores desde el panel lateral "Apoyos" en cualquier momento para validar costos antes de importar.</p>
            </div>
        </section>

        <footer class="mt-6 pt-4 border-t border-[var(--border)] flex justify-end">
            <button type="button" class="btn-primary py-2 px-5 text-xs uppercase tracking-widest" onclick="closeHelpModal()">Entendido</button>
        </footer>
    </div>
</div>

<script nonce="<%= request.getAttribute("csp_nonce") %>">
    function openHelpModal() {
        const modal = document.getElementById('globalHelpModal');
        const dialog = document.getElementById('helpModalDialog');
        modal.classList.remove('hidden');
        // Trigger reflow
        void modal.offsetWidth;
        modal.classList.remove('opacity-0');
        dialog.classList.remove('scale-95');
        dialog.classList.add('scale-100');
        // Focus management
        const focusable = modal.querySelectorAll('button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])');
        if(focusable.length) focusable[0].focus();
    }

    function closeHelpModal() {
        const modal = document.getElementById('globalHelpModal');
        const dialog = document.getElementById('helpModalDialog');
        modal.classList.add('opacity-0');
        dialog.classList.remove('scale-100');
        dialog.classList.add('scale-95');
        setTimeout(() => {
            modal.classList.add('hidden');
        }, 300);
    }

    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape' && !document.getElementById('globalHelpModal').classList.contains('hidden')) {
            closeHelpModal();
        }
    });
</script>
