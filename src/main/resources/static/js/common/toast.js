/**
 * BrewMaster Universal Glassmorphic Popup Modal System
 * Guarantees that ALL notifications and confirmations use a unified, premium centered Popup Modal.
 */

// Intercept alert immediately to prevent "localhost says..." system popups
const originalAlert = window.alert;
window.alert = function(msg) {
    if (typeof window.showGlobalToast === 'function') {
        window.showGlobalToast(msg, 'warning');
    } else {
        originalAlert(msg);
    }
};

// Override SweetAlert2 (Swal.fire) to unify all dialogs into our custom UI
window.Swal = window.Swal || {};
window.Swal.fire = function(options) {
    if (typeof options === 'string') {
        window.showGlobalToast(options, 'info');
        return Promise.resolve({ isConfirmed: true });
    }
    const icon = options.icon || 'info';
    const title = options.title || '';
    const text = options.text || '';
    const html = options.html || null;
    const preConfirm = options.preConfirm || null;
    const showCancelButton = options.showCancelButton || false;
    const inputType = options.input || null;
    const inputPlaceholder = options.inputPlaceholder || 'Nhập lý do...';
    const confirmText = options.confirmButtonText || 'Đồng ý';
    const cancelText = options.cancelButtonText || 'Hủy bỏ';

    if (showCancelButton || inputType === 'text' || html || preConfirm) {
        return new Promise((resolve) => {
            renderPopupModal({
                title: title || 'Xác nhận',
                message: text || '',
                html: html,
                preConfirm: preConfirm,
                type: icon,
                isConfirm: true,
                inputType: inputType,
                inputPlaceholder: inputPlaceholder,
                confirmButtonText: confirmText,
                cancelButtonText: cancelText,
                allowAutoClose: options.timer !== undefined ? (options.timer > 0) : false,
                onConfirm: (val) => {
                    resolve({ isConfirmed: true, value: val });
                },
                onCancel: () => {
                    resolve({ isConfirmed: false, isDismissed: true });
                }
            });
        });
    } else {
        return new Promise((resolve) => {
            renderPopupModal({
                title: title || (icon === 'success' ? 'Thành công!' : (icon === 'error' ? 'Có lỗi xảy ra!' : (icon === 'warning' ? 'Cảnh báo!' : 'Thông báo'))),
                message: text || title,
                html: html,
                preConfirm: preConfirm,
                type: icon,
                isConfirm: false,
                confirmButtonText: confirmText,
                allowAutoClose: options.timer !== undefined ? (options.timer > 0) : false,
                onConfirm: (val) => {
                    resolve({ isConfirmed: true, value: val });
                }
            });
        });
    }
};

document.addEventListener('DOMContentLoaded', () => {
    // Check if there are any flash messages embedded in the DOM from Spring Boot
    const flashMessagesContainer = document.getElementById('spring-flash-messages');
    if (flashMessagesContainer) {
        const successMsg = flashMessagesContainer.getAttribute('data-success');
        const errorMsg = flashMessagesContainer.getAttribute('data-error');
        
        if (successMsg && successMsg.trim() !== '' && successMsg !== 'null') {
            flashMessagesContainer.removeAttribute('data-success');
            window.showGlobalToast(successMsg, 'success');
        }
        if (errorMsg && errorMsg.trim() !== '' && errorMsg !== 'null') {
            flashMessagesContainer.removeAttribute('data-error');
            window.showGlobalToast(errorMsg, 'error');
        }
    }

    const serverMessage = document.getElementById('serverMessage');
    if (serverMessage) {
        const msg = serverMessage.getAttribute('data-message');
        const type = serverMessage.getAttribute('data-type') || 'info';
        if (msg && msg.trim() !== '' && msg !== 'null') {
            serverMessage.removeAttribute('data-message');
            window.showGlobalToast(msg, type);
        }
    }
});

/**
 * Display a unified, premium centered Popup Modal for notifications.
 * @param {string} message - The message to display.
 * @param {string} type - 'success', 'error', 'info', 'warning'
 */
window.showGlobalToast = function(message, type = 'success') {
    renderPopupModal({
        title: type === 'success' ? 'Thành công!' : (type === 'error' ? 'Có lỗi xảy ra!' : (type === 'warning' ? 'Cảnh báo!' : 'Thông báo')),
        message: message,
        type: type,
        isConfirm: false
    });
};

/**
 * Display a unified, premium centered Popup Modal for confirmations.
 */
window.showGlobalConfirm = function(title, message, onConfirm, onCancel) {
    renderPopupModal({
        title: title || 'Xác nhận',
        message: message || '',
        type: 'warning',
        isConfirm: true,
        onConfirm: onConfirm,
        onCancel: onCancel
    });
};

function renderPopupModal({ title, message, html, preConfirm, type, isConfirm, inputType, inputPlaceholder, confirmButtonText = 'Đồng ý', cancelButtonText = 'Hủy bỏ', allowAutoClose = true, onConfirm, onCancel }) {
    // Remove existing popup if any
    let existing = document.getElementById('brewmaster-universal-popup');
    if (existing) existing.remove();

    let iconName = 'info';
    let iconColor = 'text-primary';
    let bgColor = 'bg-primary/10';
    let borderColor = 'border-primary/20';
    let confirmBtnClass = 'bg-primary text-white hover:bg-primary/90 shadow-lg shadow-primary/20';
    
    if (type === 'success') {
        iconName = 'check_circle';
        iconColor = 'text-primary';
        bgColor = 'bg-primary/10';
        borderColor = 'border-primary/20';
    } else if (type === 'error') {
        iconName = 'error';
        iconColor = 'text-error';
        bgColor = 'bg-error/10';
        borderColor = 'border-error/20';
        confirmBtnClass = 'bg-error text-white hover:bg-error/90 shadow-lg shadow-error/20';
    } else if (type === 'warning' || (confirmButtonText && confirmButtonText.toLowerCase().includes('hủy'))) {
        iconName = 'warning';
        iconColor = 'text-primary';
        bgColor = 'bg-primary/10';
        borderColor = 'border-primary/20';
        if (confirmButtonText && confirmButtonText.toLowerCase().includes('hủy')) {
            confirmBtnClass = 'bg-primary text-white hover:bg-primary/90 shadow-lg shadow-primary/20';
        }
    }

    const overlay = document.createElement('div');
    overlay.id = 'brewmaster-universal-popup';
    overlay.className = 'fixed inset-0 z-[99999] flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm opacity-0 transition-opacity duration-300';

    let inputHtml = '';
    if (inputType === 'text') {
        inputHtml = `
            <div class="w-full mt-4 mb-2 text-left">
                <textarea id="popup-input-val" rows="3" placeholder="${inputPlaceholder || 'Nhập lý do...'}" 
                    class="w-full px-4 py-3 rounded-2xl border border-outline bg-surface-container text-on-surface text-body-md focus:outline-none focus:ring-2 focus:ring-primary/40 focus:border-primary transition-all placeholder:text-on-surface-variant/50 resize-none font-sans"></textarea>
            </div>
        `;
    }

    let buttonsHtml = '';
    if (isConfirm || inputType === 'text') {
        buttonsHtml = `
            <div class="flex items-center justify-center gap-3 w-full mt-4">
                <button id="popup-btn-cancel" class="flex-1 py-3.5 px-6 rounded-2xl border border-outline text-on-surface-variant hover:bg-surface-container font-semibold transition-all cursor-pointer bg-transparent text-body-md">
                    ${cancelButtonText}
                </button>
                <button id="popup-btn-ok" class="flex-1 py-3.5 px-6 rounded-2xl ${confirmBtnClass} font-semibold shadow-lg transition-all cursor-pointer border-none text-body-md flex items-center justify-center gap-2">
                    ${confirmButtonText}
                </button>
            </div>
        `;
    } else {
        buttonsHtml = `
            <div class="w-full mt-4">
                <button id="popup-btn-ok" class="w-full py-3.5 px-6 rounded-2xl ${confirmBtnClass} font-semibold shadow-xl transition-all cursor-pointer border-none text-body-lg">
                    ${confirmButtonText}
                </button>
            </div>
        `;
    }

    overlay.innerHTML = `
        <div class="bg-surface-container-lowest border ${borderColor} rounded-[32px] p-8 max-w-md w-full shadow-2xl transform scale-90 transition-transform duration-300 flex flex-col items-center text-center relative">
            <div class="w-16 h-16 rounded-full flex items-center justify-center shrink-0 ${bgColor} mb-5 shadow-inner">
                <span class="material-symbols-outlined ${iconColor} text-[36px]" style="font-variation-settings: 'wght' 600;">${iconName}</span>
            </div>
            <h3 class="font-headline-sm text-[22px] font-bold text-on-surface m-0 mb-2">${title}</h3>
            ${message ? `<p class="text-body-md text-on-surface-variant mb-2 m-0 leading-relaxed px-2">${message}</p>` : ''}
            ${html ? `<div class="w-full mt-2 text-left">${html}</div>` : ''}
            ${inputHtml}
            ${buttonsHtml}
        </div>
    `;

    document.body.appendChild(overlay);

    const card = overlay.firstElementChild;
    const btnOk = overlay.querySelector('#popup-btn-ok');
    const btnCancel = overlay.querySelector('#popup-btn-cancel');
    const inputEl = overlay.querySelector('#popup-input-val');

    if (inputEl) {
        setTimeout(() => inputEl.focus(), 100);
    }

    const closePopup = () => {
        overlay.classList.remove('opacity-100');
        overlay.classList.add('opacity-0');
        if (card) {
            card.classList.remove('scale-100');
            card.classList.add('scale-90');
        }
        setTimeout(() => {
            if (document.body.contains(overlay)) overlay.remove();
        }, 300);
    };

    if (btnOk) {
        btnOk.addEventListener('click', () => {
            let val = inputEl ? inputEl.value : null;
            if (typeof preConfirm === 'function') {
                try {
                    val = preConfirm();
                } catch (e) {
                    console.error('preConfirm error:', e);
                    return;
                }
            }
            closePopup();
            if (typeof onConfirm === 'function') onConfirm(val);
        });
    }

    if (btnCancel) {
        btnCancel.addEventListener('click', () => {
            closePopup();
            if (typeof onCancel === 'function') onCancel();
        });
    }

    // Auto close non-confirm popups after 4 seconds if user doesn't click and allowAutoClose is true
    if (!isConfirm && !inputType && allowAutoClose) {
        setTimeout(() => {
            if (document.body.contains(overlay)) closePopup();
        }, 4000);
    }

    // Animate in
    requestAnimationFrame(() => {
        requestAnimationFrame(() => {
            overlay.classList.remove('opacity-0');
            overlay.classList.add('opacity-100');
            if (card) {
                card.classList.remove('scale-90');
                card.classList.add('scale-100');
            }
        });
    });
}
