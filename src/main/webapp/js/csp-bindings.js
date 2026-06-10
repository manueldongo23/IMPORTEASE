/**
 * CSP compatibility bindings.
 *
 * This file restores interactive controls that were previously wired with
 * inline handlers. The app uses a strict CSP, so inline handlers are ignored
 * in production unless they are rebound with JavaScript.
 */
(function () {
    'use strict';

    function safeCall(fn) {
        if (typeof fn === 'function') {
            return fn.apply(window, Array.prototype.slice.call(arguments, 1));
        }
        return undefined;
    }

    function bindClick(selector, handler) {
        document.querySelectorAll(selector).forEach(function (el) {
            if (el.dataset.cspBound === '1') return;
            el.dataset.cspBound = '1';
            el.addEventListener('click', handler);
        });
    }

    function bindInput(selector, handler, eventName) {
        document.querySelectorAll(selector).forEach(function (el) {
            if (el.dataset.cspBound === '1') return;
            el.dataset.cspBound = '1';
            el.addEventListener(eventName || 'input', handler);
        });
    }

    function parseQuotedArgs(source, fnName, argCount) {
        if (!source || source.indexOf(fnName + '(') === -1) return null;
        var re = new RegExp(fnName.replace(/[.*+?^${}()|[\]\\]/g, '\\$&') + "\\(\\s*'([^']*)'" + (argCount > 1 ? "\\s*,\\s*'([^']*)'" : '') + (argCount > 2 ? "\\s*,\\s*'([^']*)'" : '') + "\\s*\\)");
        var match = source.match(re);
        return match ? match.slice(1, argCount + 1) : null;
    }

    function handleClick(event) {
        var target = event.target && event.target.closest ? event.target.closest('button, a, [role="button"]') : null;
        if (!target) return;

        var onclick = target.getAttribute('onclick') || '';

        if (onclick.indexOf('toggleSidebar()') !== -1 || target.classList.contains('mobile-toggle')) {
            event.preventDefault();
            safeCall(window.toggleSidebar);
            return;
        }

        if (onclick.indexOf('toggleUserDropdown()') !== -1 || target.classList.contains('user-dropdown__trigger')) {
            event.preventDefault();
            safeCall(window.toggleUserDropdown);
            return;
        }

        if (onclick.indexOf('doLogout()') !== -1 || target.id === 'header-logout-btn') {
            event.preventDefault();
            if (typeof window.doLogout === 'function') {
                window.doLogout();
            }
            return;
        }

        if (onclick.indexOf('openHelpModal()') !== -1) {
            event.preventDefault();
            safeCall(window.openHelpModal);
            return;
        }

        if (onclick.indexOf('closeHelpModal()') !== -1 || target.id === 'helpModalCloseBtn' || target.id === 'helpModalFooterCloseBtn') {
            event.preventDefault();
            safeCall(window.closeHelpModal);
            return;
        }

        if (onclick.indexOf("openKnowledgePanel('dam'") !== -1) {
            event.preventDefault();
            safeCall(window.openKnowledgePanel, 'dam', {
                actionLabel: 'Ver ejemplo de declaracion',
                actionCallback: window.verDAM
            });
            return;
        }

        if (onclick.indexOf("openKnowledgePanel('factura_comercial'") !== -1 ||
            onclick.indexOf("openKnowledgePanel('bill_of_lading'") !== -1 ||
            onclick.indexOf("openKnowledgePanel('certificado_origen'") !== -1) {
            // These were already migrated on documentacion, but we keep this
            // guard to avoid dead clicks if an old template survives.
            event.preventDefault();
            return;
        }

        if (onclick.indexOf('verDAM()') !== -1) {
            event.preventDefault();
            safeCall(window.verDAM);
            return;
        }

        if (onclick.indexOf('cerrarDAM()') !== -1) {
            event.preventDefault();
            safeCall(window.cerrarDAM);
            return;
        }

        if (onclick.indexOf('cargarFichaAsesoria(') !== -1) {
            var args = parseQuotedArgs(onclick, 'cargarFichaAsesoria', 1);
            if (args && args[0]) {
                event.preventDefault();
                safeCall(window.cargarFichaAsesoria, args[0]);
                return;
            }
        }

        if (onclick.indexOf('openStageHelp(') !== -1) {
            var stageArgs = parseQuotedArgs(onclick, 'openStageHelp', 3);
            if (stageArgs && stageArgs.length === 3) {
                event.preventDefault();
                safeCall(window.openStageHelp, stageArgs[0], stageArgs[1], stageArgs[2]);
                return;
            }
        }

        var locationMatch = onclick.match(/window\.location\.href\s*=\s*['"]([^'"]+)['"]/);
        if (locationMatch) {
            event.preventDefault();
            window.location.href = locationMatch[1];
        }
    }

    function bindCalculatedInputs() {
        if (typeof window.recalcular === 'function') {
            bindInput('#valFob', function () { window.recalcular(); });
            bindInput('#paisOrigen', function () { window.recalcular(); }, 'change');
        }

        if (typeof window.guardar === 'function') {
            bindClick('button[onclick="guardar()"], #btnGuardar', function (event) {
                event.preventDefault();
                window.guardar();
            });
        }

        if (typeof window.actualizarComparacion === 'function') {
            bindInput('#hsCodeSelect', function () { window.actualizarComparacion(); }, 'change');
            bindInput('#fobSlider', function () {
                if (typeof window.onSliderInput === 'function') window.onSliderInput('fob');
                window.actualizarComparacion();
            });
            bindInput('#fleteSlider', function () {
                if (typeof window.onSliderInput === 'function') window.onSliderInput('flete');
                window.actualizarComparacion();
            });
            bindInput('#seguroSlider', function () {
                if (typeof window.onSliderInput === 'function') window.onSliderInput('seguro');
                window.actualizarComparacion();
            });
        }

        if (typeof window.calcular === 'function') {
            bindInput('#valFob, #valCant, #valFlete, #valSeguro, #valPrecioVenta', function () {
                window.calcular();
            }, 'input');
            bindInput('#checkPrimeraVez', function () {
                window.calcular();
            }, 'change');
        }
    }

    function init() {
        bindCalculatedInputs();

        // Delegate clicks for any inline handlers left in shared fragments.
        document.addEventListener('click', handleClick);

        // Re-bind if the DOM is updated later (lazy-rendered cards, etc.).
        document.addEventListener('DOMContentLoaded', bindCalculatedInputs);
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
