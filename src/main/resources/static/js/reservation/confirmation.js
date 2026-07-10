/**
 * Reservation Confirmation - JavaScript
 */

(function() {
    'use strict';

    // ===== DOM Elements =====
    const reservationId = document.getElementById('reservationId');
    const printBtn = document.getElementById('printBtn');

    // ===== Print Confirmation =====
    if (printBtn) {
        printBtn.addEventListener('click', function() {
            window.print();
        });
    }

    // ===== Auto redirect to reservations list after 10 seconds =====
    const autoRedirect = document.getElementById('autoRedirect');
    if (autoRedirect) {
        const seconds = autoRedirect.dataset.seconds || 10;
        let countdown = seconds;
        const counter = document.getElementById('redirectCounter');

        const interval = setInterval(function() {
            countdown--;
            if (counter) {
                counter.textContent = countdown;
            }
            if (countdown <= 0) {
                clearInterval(interval);
                window.location.href = '/customer/reservations';
            }
        }, 1000);
    }

    // ===== Copy Reservation ID =====
    const copyBtn = document.getElementById('copyIdBtn');
    if (copyBtn && reservationId) {
        copyBtn.addEventListener('click', function() {
            const id = reservationId.textContent.trim();
            navigator.clipboard.writeText(id).then(function() {
                // Show toast notification
                showToast('Đã sao chép mã đặt bàn: ' + id, 'success');
            }).catch(function() {
                // Fallback
                const textArea = document.createElement('textarea');
                textArea.value = id;
                document.body.appendChild(textArea);
                textArea.select();
                document.execCommand('copy');
                document.body.removeChild(textArea);
                showToast('Đã sao chép mã đặt bàn: ' + id, 'success');
            });
        });
    }

    // ===== Toast Notification =====
    function showToast(message, type) {
        const existing = document.querySelector('.custom-toast');
        if (existing) existing.remove();

        const toast = document.createElement('div');
        toast.className = 'custom-toast';
        toast.style.cssText = `
            position: fixed;
            bottom: 24px;
            right: 24px;
            background: ${type === 'success' ? '#dcfce7' : '#fee2e2'};
            color: ${type === 'success' ? '#166534' : '#991b1b'};
            padding: 12px 24px;
            border-radius: 12px;
            box-shadow: 0 4px 12px rgba(0,0,0,0.15);
            z-index: 9999;
            font-size: 14px;
            font-weight: 500;
            animation: slideUp 0.3s ease;
            border: 1px solid ${type === 'success' ? '#86efac' : '#fca5a5'};
        `;
        toast.textContent = message;

        // Add keyframe if not exists
        if (!document.getElementById('toastStyles')) {
            const style = document.createElement('style');
            style.id = 'toastStyles';
            style.textContent = `
                @keyframes slideUp {
                    from {
                        opacity: 0;
                        transform: translateY(20px);
                    }
                    to {
                        opacity: 1;
                        transform: translateY(0);
                    }
                }
            `;
            document.head.appendChild(style);
        }

        document.body.appendChild(toast);

        setTimeout(function() {
            toast.style.opacity = '0';
            toast.style.transform = 'translateY(20px)';
            setTimeout(function() {
                toast.remove();
            }, 300);
        }, 3000);
    }

})();