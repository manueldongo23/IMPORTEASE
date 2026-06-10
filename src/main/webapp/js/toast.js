function showNotification(title, message, isSuccess) {
    if (isSuccess === void 0) { isSuccess = false; }
    var toast = document.getElementById('toastNotification');
    if (!toast) return;
    toast.setAttribute('role', 'alert');
    toast.setAttribute('aria-live', 'polite');
    var icon = document.getElementById('toastIcon');
    var tTitle = document.getElementById('toastTitle');
    var tMsg = document.getElementById('toastMessage');
    icon.innerText = isSuccess ? '✨' : '⚠️';
    tTitle.innerText = title;
    tTitle.className = 'text-xs font-black uppercase tracking-wider ' + (isSuccess ? 'text-emerald-600' : 'text-rose-500');
    tMsg.innerText = message;
    toast.className = 'fixed top-6 left-1/2 -translate-x-1/2 z-50 flex items-center gap-4 px-6 py-4.5 rounded-2xl border bg-[var(--surface-1)] shadow-2xl transition-all duration-500 opacity-0 pointer-events-none scale-95 max-w-md w-[calc(100%-3rem)] ' + (isSuccess ? 'border-emerald-100 dark:border-emerald-950' : 'border-rose-100 dark:border-rose-950');
    toast.classList.add('toast-active');
    toast.style.opacity = '1';
    toast.style.transform = 'translate(-50%, 0) scale(1)';
    toast.style.pointerEvents = 'auto';
    setTimeout(function () {
        toast.style.opacity = '0';
        toast.style.transform = 'translate(-50%, 0) scale(0.95)';
        toast.style.pointerEvents = 'none';
    }, 4000);
}
