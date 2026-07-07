// ============================================================
// SYSTEM CONFIG - Interactive Features
// ============================================================

document.addEventListener('DOMContentLoaded', function() {
    'use strict';

    // ── Toggle Card Body ──────────────────────────────────────
    document.querySelectorAll('.btn-toggle').forEach(btn => {
        btn.addEventListener('click', function(e) {
            e.stopPropagation();
            const card = this.closest('.config-card');
            const body = card.querySelector('.card-body');

            body.classList.toggle('collapsed');

            if (body.classList.contains('collapsed')) {
                this.innerHTML = '<span class="material-symbols-outlined">expand_more</span> Mở rộng';
            } else {
                this.innerHTML = '<span class="material-symbols-outlined">expand_less</span> Thu gọn';
            }
        });
    });

    // ── Toggle on Header Click ────────────────────────────────
    document.querySelectorAll('.config-card-header').forEach(header => {
        header.addEventListener('click', function(e) {
            if (e.target.closest('.btn-toggle')) return;
            const btn = this.querySelector('.btn-toggle');
            if (btn) btn.click();
        });
    });

    // ── Toggle Switch Label ────────────────────────────────────
    document.querySelectorAll('.toggle-switch input[type="checkbox"]').forEach(toggle => {
        toggle.addEventListener('change', function() {
            const label = this.closest('.toggle-wrapper').querySelector('.toggle-label');
            if (label) {
                if (this.checked) {
                    label.textContent = '🟢 Đang bảo trì';
                    label.style.color = '#22c55e';
                } else {
                    label.textContent = '⚪ Bình thường';
                    label.style.color = '#64748b';
                }
            }
        });
    });

    // ── Search Filter ──────────────────────────────────────────
    const searchInput = document.getElementById('configSearch');
    if (searchInput) {
        searchInput.addEventListener('input', function() {
            const query = this.value.toLowerCase().trim();
            const cards = document.querySelectorAll('.config-card');

            cards.forEach(card => {
                const text = card.textContent.toLowerCase();
                card.style.display = text.includes(query) ? '' : 'none';
            });
        });
    }

    // ── Keyboard Shortcut: Ctrl+S ─────────────────────────────
    document.addEventListener('keydown', function(e) {
        if ((e.ctrlKey || e.metaKey) && e.key === 's') {
            e.preventDefault();

            // Find first visible save button
            const saveBtns = document.querySelectorAll('.btn-save-group');
            for (const btn of saveBtns) {
                if (btn.closest('.config-card').style.display !== 'none') {
                    btn.click();
                    btn.style.transform = 'scale(0.95)';
                    setTimeout(() => {
                        btn.style.transform = '';
                    }, 200);
                    break;
                }
            }
        }
    });

    // ── Auto-save indicator ────────────────────────────────────
    let autoSaveTimeout = null;
    document.querySelectorAll('.config-item input, .config-item textarea').forEach(input => {
        input.addEventListener('change', function() {
            this.style.borderColor = '#f59e0b';
            this.style.boxShadow = '0 0 0 4px rgba(245, 158, 11, 0.1)';

            clearTimeout(autoSaveTimeout);
            autoSaveTimeout = setTimeout(() => {
                this.style.borderColor = '';
                this.style.boxShadow = '';
            }, 3000);
        });
    });

    // ── Reset Group ─────────────────────────────────────────────
    window.resetGroup = function(btn, groupKey) {
        if (!confirm(`Bạn có chắc muốn reset tất cả cấu hình trong nhóm "${groupKey}" về giá trị mặc định?`)) {
            return;
        }

        const card = btn.closest('.config-card');

        // Xác định endpoint reset dựa trên groupKey
        let resetEndpoint = '';
        switch(groupKey.toLowerCase()) {
            case 'general':
                resetEndpoint = '/admin/config/general/reset';
                break;
            case 'system':
                resetEndpoint = '/admin/config/system/reset';
                break;
            case 'reservation':
                resetEndpoint = '/admin/config/reservation/reset';
                break;
            default:
                showNotification('Không tìm thấy nhóm cấu hình!', 'error');
                return;
        }

        // Visual feedback - shake animation
        card.style.animation = 'shake 0.5s ease';
        setTimeout(() => {
            card.style.animation = '';
        }, 500);

        // CSRF tokens
        const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

        // Disable button
        btn.disabled = true;
        btn.innerHTML = '<span class="material-symbols-outlined" style="font-size:18px;">refresh</span> Đang reset...';

        // Send reset request
        fetch(resetEndpoint, {
            method: 'POST',
            headers: {
                [csrfHeader]: csrfToken
            }
        })
            .then(response => {
                if (response.redirected) {
                    // Nếu redirect về /admin/config, reload trang
                    window.location.href = response.url;
                } else {
                    return response.text();
                }
            })
            .catch(() => {
                btn.disabled = false;
                btn.innerHTML = '<span class="material-symbols-outlined" style="font-size:18px;">restart_alt</span> Reset nhóm';
                showNotification('Có lỗi xảy ra khi reset!', 'error');
            });
    };

    // ── Notification System ────────────────────────────────────
    function showNotification(message, type = 'success') {
        const existing = document.querySelector('.custom-notification');
        if (existing) existing.remove();

        const notification = document.createElement('div');
        notification.className = `custom-notification notification-${type}`;

        const icon = type === 'success' ? 'check_circle' : 'error';
        const iconColor = type === 'success' ? '#22c55e' : '#ef4444';

        notification.innerHTML = `
            <span class="material-symbols-outlined" style="color: ${iconColor}; font-size: 24px;">${icon}</span>
            <span>${message}</span>
        `;

        Object.assign(notification.style, {
            position: 'fixed',
            bottom: '24px',
            right: '24px',
            padding: '16px 24px',
            borderRadius: '14px',
            background: type === 'success' ? '#f0fdf4' : '#fef2f2',
            border: `1px solid ${type === 'success' ? '#86efac' : '#fca5a5'}`,
            color: type === 'success' ? '#166534' : '#991b1b',
            boxShadow: '0 12px 40px rgba(0, 0, 0, 0.1)',
            zIndex: '9999',
            display: 'flex',
            alignItems: 'center',
            gap: '12px',
            fontSize: '14px',
            fontWeight: '500',
            animation: 'slideUp 0.4s cubic-bezier(0.4, 0, 0.2, 1)',
            fontFamily: 'Inter, sans-serif',
        });

        document.body.appendChild(notification);

        setTimeout(() => {
            notification.style.opacity = '0';
            notification.style.transform = 'translateY(20px) scale(0.95)';
            setTimeout(() => notification.remove(), 300);
        }, 4000);
    }

    // ── Add CSS animations ─────────────────────────────────────
    const style = document.createElement('style');
    style.textContent = `
        @keyframes shake {
            0%, 100% { transform: translateX(0); }
            20% { transform: translateX(-8px); }
            40% { transform: translateX(8px); }
            60% { transform: translateX(-4px); }
            80% { transform: translateX(4px); }
        }
        @keyframes slideUp {
            from {
                opacity: 0;
                transform: translateY(20px) scale(0.95);
            }
            to {
                opacity: 1;
                transform: translateY(0) scale(1);
            }
        }
        .custom-notification {
            transition: all 0.3s ease;
        }
    `;
    document.head.appendChild(style);
});