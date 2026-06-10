<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="true" %>
<!-- Help Modal (Accessible & SEO Compliant) -->
<div id="globalHelpModal" class="hidden" style="position:fixed;inset:0;z-index:200;display:flex;align-items:center;justify-content:center;transition:opacity .3s;opacity:0;"
     role="dialog" aria-modal="true" aria-labelledby="helpModalTitle">
    <!-- Backdrop -->
    <div style="position:absolute;inset:0;background:rgba(26,29,46,0.55);backdrop-filter:blur(8px);-webkit-backdrop-filter:blur(8px);" aria-hidden="true" onclick="closeHelpModal()"></div>
    
    <!-- Modal Content -->
    <div id="helpModalDialog" style="position:relative;background:#ffffff;border:1px solid #E2E8F0;border-radius:1.25rem;box-shadow:0 25px 50px -12px rgba(91,80,240,0.18),0 8px 24px -4px rgba(0,0,0,0.08);width:90%;max-width:32rem;padding:0;transform:scale(0.95);transition:transform .3s;overflow:hidden;">

        <!-- Header gradient bar -->
        <div style="height:4px;background:linear-gradient(90deg,#5B50F0,#8B7CFF,#C4B5FD);"></div>

        <div style="padding:28px 28px 0 28px;">
            <header style="display:flex;align-items:center;justify-content:space-between;border-bottom:1px solid #F1F5F9;padding-bottom:16px;margin-bottom:20px;">
                <div style="display:flex;align-items:center;gap:12px;">
                    <div style="width:40px;height:40px;background:#EEF0FB;border-radius:12px;display:flex;align-items:center;justify-content:center;">
                        <svg width="20" height="20" fill="none" stroke="#5B50F0" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M9.879 7.519c1.171-1.025 3.071-1.025 4.242 0 1.172 1.025 1.172 2.687 0 3.712-.203.179-.43.326-.67.442-.745.361-1.45.999-1.45 1.827v.75M21 12a9 9 0 11-18 0 9 9 0 0118 0zm-9 5.25h.008v.008H12v-.008z"/></svg>
                    </div>
                    <h2 id="helpModalTitle" style="font-size:1.15rem;font-weight:900;letter-spacing:-0.02em;color:#1A1D2E;margin:0;">Ayuda ImportEase</h2>
                </div>
                <button type="button" style="color:#94A3B8;background:none;border:none;cursor:pointer;padding:6px;border-radius:8px;transition:all .2s;display:flex;align-items:center;justify-content:center;"
                        aria-label="Cerrar ayuda" onclick="closeHelpModal()"
                        onmouseover="this.style.color='#1A1D2E';this.style.background='#F1F5F9'"
                        onmouseout="this.style.color='#94A3B8';this.style.background='none'">
                    <svg width="18" height="18" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                    </svg>
                </button>
            </header>
        </div>

        <section style="padding:0 28px;max-height:55vh;overflow-y:auto;display:flex;flex-direction:column;gap:16px;">

            <!-- Primeros pasos -->
            <div style="background:linear-gradient(135deg,#F5F3FF,#EEF0FB);border:1px solid #E0D9FF;border-radius:14px;padding:18px;">
                <div style="display:flex;align-items:center;gap:8px;margin-bottom:10px;">
                    <svg width="14" height="14" fill="none" stroke="#5B50F0" stroke-width="2.5" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M13 10V3L4 14h7v7l9-11h-7z"/></svg>
                    <h3 style="font-size:0.65rem;font-weight:900;text-transform:uppercase;letter-spacing:0.12em;color:#5B50F0;margin:0;">Primeros Pasos</h3>
                </div>
                <p style="font-size:0.82rem;color:#4B5563;line-height:1.6;margin:0;font-weight:500;">Usa la seccion <strong style="color:#1A1D2E;">Importar paso a paso</strong> para declarar tu producto. El sistema te guiara sobre que documentos y permisos necesitas.</p>
            </div>

            <!-- Navegacion -->
            <div style="background:#FAFBFC;border:1px solid #E8EAF0;border-radius:14px;padding:18px;">
                <div style="display:flex;align-items:center;gap:8px;margin-bottom:12px;">
                    <svg width="14" height="14" fill="none" stroke="#5B50F0" stroke-width="2.5" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M3.75 6.75h16.5M3.75 12h16.5m-16.5 5.25H12"/></svg>
                    <h3 style="font-size:0.65rem;font-weight:900;text-transform:uppercase;letter-spacing:0.12em;color:#5B50F0;margin:0;">Navegacion Principal</h3>
                </div>
                <div style="display:flex;flex-direction:column;gap:8px;">
                    <div style="display:flex;align-items:center;gap:10px;padding:10px 12px;background:#fff;border:1px solid #F1F5F9;border-radius:10px;">
                        <div style="width:28px;height:28px;background:#EEF0FB;border-radius:8px;display:flex;align-items:center;justify-content:center;flex-shrink:0;">
                            <svg width="13" height="13" fill="none" stroke="#5B50F0" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M2.25 12l8.954-8.955c.44-.439 1.152-.439 1.591 0L21.75 12M4.5 9.75v10.125c0 .621.504 1.125 1.125 1.125H9.75v-4.875c0-.621.504-1.125 1.125-1.125h2.25c.621 0 1.125.504 1.125 1.125V21h4.125c.621 0 1.125-.504 1.125-1.125V9.75M8.25 21h8.25"/></svg>
                        </div>
                        <div>
                            <strong style="font-size:0.78rem;color:#1A1D2E;">Inicio</strong>
                            <span style="font-size:0.72rem;color:#64748B;margin-left:6px;">Resumen de tus KPIs.</span>
                        </div>
                    </div>
                    <div style="display:flex;align-items:center;gap:10px;padding:10px 12px;background:#fff;border:1px solid #F1F5F9;border-radius:10px;">
                        <div style="width:28px;height:28px;background:#EEF0FB;border-radius:8px;display:flex;align-items:center;justify-content:center;flex-shrink:0;">
                            <svg width="13" height="13" fill="none" stroke="#5B50F0" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M19.5 14.25v-2.625a3.375 3.375 0 00-3.375-3.375h-1.5A1.125 1.125 0 0113.5 7.125v-1.5a3.375 3.375 0 00-3.375-3.375H8.25m0 12.75h7.5m-7.5 3H12M10.5 2.25H5.625c-.621 0-1.125.504-1.125 1.125v17.25c0 .621.504 1.125 1.125 1.125h12.75c.621 0 1.125-.504 1.125-1.125V11.25a9 9 0 00-9-9z"/></svg>
                        </div>
                        <div>
                            <strong style="font-size:0.78rem;color:#1A1D2E;">Expediente</strong>
                            <span style="font-size:0.72rem;color:#64748B;margin-left:6px;">Documentacion agrupada.</span>
                        </div>
                    </div>
                    <div style="display:flex;align-items:center;gap:10px;padding:10px 12px;background:#fff;border:1px solid #F1F5F9;border-radius:10px;">
                        <div style="width:28px;height:28px;background:#EEF0FB;border-radius:8px;display:flex;align-items:center;justify-content:center;flex-shrink:0;">
                            <svg width="13" height="13" fill="none" stroke="#5B50F0" stroke-width="2" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M9 12.75L11.25 15 15 9.75m-3-7.036A11.959 11.959 0 013.598 6 11.99 11.99 0 003 9.749c0 5.592 3.824 10.29 9 11.623 5.176-1.332 9-6.03 9-11.622 0-1.31-.21-2.571-.598-3.751h-.152c-3.196 0-6.1-1.248-8.25-3.285z"/></svg>
                        </div>
                        <div>
                            <strong style="font-size:0.78rem;color:#1A1D2E;">Revision</strong>
                            <span style="font-size:0.72rem;color:#64748B;margin-left:6px;">Evaluacion final aduanera.</span>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Atajos -->
            <div style="background:#FAFBFC;border:1px solid #E8EAF0;border-radius:14px;padding:18px;">
                <div style="display:flex;align-items:center;gap:8px;margin-bottom:10px;">
                    <svg width="14" height="14" fill="none" stroke="#5B50F0" stroke-width="2.5" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" d="M15.59 14.37a6 6 0 01-5.84 7.38v-4.8m5.84-2.58a14.98 14.98 0 006.16-12.12A14.98 14.98 0 009.631 8.41m5.96 5.96a14.926 14.926 0 01-5.841 2.58m-.119-8.54a6 6 0 00-7.381 5.84h4.8m2.58-5.84a14.927 14.927 0 00-2.58 5.84m2.699 2.7c-.103.021-.207.041-.311.06a15.09 15.09 0 01-2.448-2.448 14.9 14.9 0 01.06-.312m-2.24 2.39a4.493 4.493 0 00-1.757 4.306 4.493 4.493 0 004.306-1.758M16.5 9a1.5 1.5 0 11-3 0 1.5 1.5 0 013 0z"/></svg>
                    <h3 style="font-size:0.65rem;font-weight:900;text-transform:uppercase;letter-spacing:0.12em;color:#5B50F0;margin:0;">Atajos</h3>
                </div>
                <p style="font-size:0.82rem;color:#4B5563;line-height:1.6;margin:0;font-weight:500;">Puedes usar las calculadoras y simuladores desde el panel lateral "Apoyos" en cualquier momento para validar costos antes de importar.</p>
            </div>

        </section>

        <footer style="margin-top:0;padding:20px 28px 24px 28px;border-top:1px solid #F1F5F9;display:flex;justify-content:flex-end;background:#FAFBFC;">
            <button type="button" onclick="closeHelpModal()"
                    style="background:linear-gradient(135deg,#5B50F0,#6652FF);color:#fff;border:none;cursor:pointer;padding:10px 28px;border-radius:10px;font-size:0.72rem;font-weight:800;text-transform:uppercase;letter-spacing:0.14em;transition:all .2s;box-shadow:0 4px 12px rgba(91,80,240,0.25);"
                    onmouseover="this.style.transform='translateY(-1px)';this.style.boxShadow='0 6px 20px rgba(91,80,240,0.35)'"
                    onmouseout="this.style.transform='translateY(0)';this.style.boxShadow='0 4px 12px rgba(91,80,240,0.25)'">
                Entendido
            </button>
        </footer>
    </div>
</div>

<script nonce="<%= request.getAttribute("csp_nonce") %>">
    function openHelpModal() {
        const modal = document.getElementById('globalHelpModal');
        const dialog = document.getElementById('helpModalDialog');
        modal.style.display = 'flex';
        modal.classList.remove('hidden');
        // Trigger reflow
        void modal.offsetWidth;
        modal.style.opacity = '1';
        dialog.style.transform = 'scale(1)';
        // Focus management
        const focusable = modal.querySelectorAll('button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])');
        if(focusable.length) focusable[0].focus();
    }

    function closeHelpModal() {
        const modal = document.getElementById('globalHelpModal');
        const dialog = document.getElementById('helpModalDialog');
        modal.style.opacity = '0';
        dialog.style.transform = 'scale(0.95)';
        setTimeout(() => {
            modal.style.display = 'none';
            modal.classList.add('hidden');
        }, 300);
    }

    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape' && !document.getElementById('globalHelpModal').classList.contains('hidden')) {
            closeHelpModal();
        }
    });
</script>
