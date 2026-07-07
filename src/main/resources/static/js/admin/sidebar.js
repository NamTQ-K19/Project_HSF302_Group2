(function() {
    'use strict';

    // Lấy CSRF token
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content || '';
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content || '';

    // Tạo modal
    function createModal() {
        // Kiểm tra modal đã tồn tại chưa
        if (document.getElementById('logoutModalDynamic')) {
            return;
        }

        const modal = document.createElement('div');
        modal.id = 'logoutModalDynamic';
        modal.className = 'fixed inset-0 z-[99999] flex items-center justify-center bg-black/50 backdrop-blur-sm transition-all duration-300';
        modal.style.display = 'none';
        modal.style.opacity = '0';

        modal.innerHTML = `
            <div class="bg-white rounded-2xl max-w-md w-full mx-4 p-6 shadow-2xl transform transition-all duration-300 scale-95">
                <!-- Header -->
                <div class="flex items-center gap-4 mb-4">
                    <div class="w-14 h-14 rounded-full bg-red-50 flex items-center justify-center flex-shrink-0">
                        <span class="material-symbols-outlined text-red-500 text-[32px]">logout</span>
                    </div>
                    <div>
                        <h3 class="text-xl font-bold text-gray-900 m-0">Xác nhận đăng xuất</h3>
                        <p class="text-sm text-gray-500 mt-1 m-0">Bạn có chắc chắn muốn đăng xuất?</p>
                    </div>
                </div>

                <!-- Body -->
                <div class="mb-6 p-4 bg-gray-50 rounded-xl border border-gray-100">
                    <div class="flex items-start gap-3">
                        <span class="material-symbols-outlined text-amber-500 text-[20px] flex-shrink-0 mt-0.5">info</span>
                        <p class="text-sm text-gray-600 m-0">
                            Bạn sẽ cần đăng nhập lại để truy cập hệ thống. 
                            <br>
                            <span class="text-xs text-gray-400">Mọi thay đổi chưa lưu sẽ bị mất.</span>
                        </p>
                    </div>
                </div>

                <!-- Actions -->
                <div class="flex gap-3">
                    <button id="cancelLogoutDynamic" 
                            class="flex-1 px-4 py-2.5 rounded-xl font-medium text-gray-600 bg-gray-100 hover:bg-gray-200 transition-all duration-200 cursor-pointer border-none">
                        Hủy bỏ
                    </button>
                    <form action="/logout" method="post" class="flex-1">
                        <input type="hidden" name="_csrf" value="${csrfToken}" />
                        <button type="submit" 
                                class="w-full px-4 py-2.5 rounded-xl font-medium text-white bg-gradient-to-r from-red-500 to-red-600 hover:from-red-600 hover:to-red-700 transition-all duration-200 cursor-pointer flex items-center justify-center gap-2 border-none shadow-lg shadow-red-500/25 hover:shadow-red-500/40">
                            <span class="material-symbols-outlined text-[18px]">logout</span>
                            Đăng xuất
                        </button>
                    </form>
                </div>
            </div>
        `;

        document.body.appendChild(modal);

        // Event listeners
        const cancelBtn = document.getElementById('cancelLogoutDynamic');
        if (cancelBtn) {
            cancelBtn.addEventListener('click', hideModal);
        }

        // Click outside để đóng
        modal.addEventListener('click', function(e) {
            if (e.target === this) {
                hideModal();
            }
        });

        // Phím Escape để đóng
        document.addEventListener('keydown', function(e) {
            if (e.key === 'Escape') {
                hideModal();
            }
        });
    }

    // Hiển thị modal với animation
    function showModal() {
        let modal = document.getElementById('logoutModalDynamic');

        if (!modal) {
            createModal();
            modal = document.getElementById('logoutModalDynamic');
        }

        // Hiển thị modal
        modal.style.display = 'flex';
        document.body.style.overflow = 'hidden';

        // Force reflow để animation chạy
        modal.offsetHeight;

        // Fade in
        requestAnimationFrame(() => {
            modal.style.opacity = '1';
            const content = modal.querySelector('.bg-white');
            if (content) {
                content.classList.remove('scale-95');
                content.classList.add('scale-100');
            }
        });
    }

    // Ẩn modal với animation
    function hideModal() {
        const modal = document.getElementById('logoutModalDynamic');

        if (!modal) return;

        // Fade out
        modal.style.opacity = '0';
        const content = modal.querySelector('.bg-white');
        if (content) {
            content.classList.remove('scale-100');
            content.classList.add('scale-95');
        }

        // Ẩn sau animation
        setTimeout(() => {
            modal.style.display = 'none';
            document.body.style.overflow = '';
        }, 300);
    }

    // Khởi tạo khi DOM ready
    document.addEventListener('DOMContentLoaded', function() {
        // Tạo modal
        createModal();

        // Bắt sự kiện click nút đăng xuất
        const logoutBtn = document.getElementById('logoutBtn');
        if (logoutBtn) {
            logoutBtn.addEventListener('click', function(e) {
                e.preventDefault();
                e.stopPropagation();
                showModal();
            });
        }

        // Tìm tất cả nút đăng xuất khác (nếu có)
        document.querySelectorAll('[data-logout-trigger]').forEach(btn => {
            btn.addEventListener('click', function(e) {
                e.preventDefault();
                e.stopPropagation();
                showModal();
            });
        });
    });

    // Export ra global
    window.showLogoutModal = showModal;
    window.hideLogoutModal = hideModal;

})();