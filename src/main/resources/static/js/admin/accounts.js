// D:\SU26\HSF302\Practice\Project_HSF302_Group2\src\main\resources\static\js\admin\accounts.js

let pendingAction = null;
let scrollbarWidth = 0;

document.addEventListener('DOMContentLoaded', function() {
    console.log('Account Management page loaded');

    // Kiểm tra có message từ server không
    const messageElement = document.getElementById('serverMessage');
    if (messageElement) {
        const message = messageElement.dataset.message;
        const type = messageElement.dataset.type;
        if (message) {
            showToast(message, type);
        }
    }
});

// ===== CONFIRMATION MODAL =====
function openConfirmModal(button) {
    const userId = button.dataset.userid;
    const action = button.dataset.action;
    const userName = button.dataset.name || 'Người dùng';

    const modal = document.getElementById('confirmModal');
    const modalTitle = document.getElementById('modalTitle');
    const modalMessage = document.getElementById('modalMessage');
    const modalUserName = document.getElementById('modalUserName');
    const modalUserInfo = document.getElementById('modalUserInfo');
    const modalReason = document.getElementById('modalReason');
    const modalReasonError = document.getElementById('modalReasonError');
    const confirmBtn = document.getElementById('modalConfirmBtn');

    // Reset lỗi và textarea
    modalReason.value = '';
    modalReasonError.textContent = '';
    modalReason.classList.remove('error');

    if (action === 'lock') {
        modalTitle.textContent = '🔒 Xác nhận khóa tài khoản';
        modalMessage.textContent = 'Bạn có chắc chắn muốn khóa tài khoản này?';
        confirmBtn.className = 'btn btn-confirm btn-confirm-danger';
        confirmBtn.innerHTML = '<i class="fas fa-lock"></i> Xác nhận khóa';
    } else {
        modalTitle.textContent = '🔓 Xác nhận mở khóa tài khoản';
        modalMessage.textContent = 'Bạn có chắc chắn muốn mở khóa tài khoản này?';
        confirmBtn.className = 'btn btn-confirm btn-confirm-success';
        confirmBtn.innerHTML = '<i class="fas fa-unlock"></i> Xác nhận mở khóa';
    }

    modalUserName.textContent = userName;
    modalUserInfo.style.display = 'flex';

    pendingAction = {
        userId: userId,
        action: action
    };

    // Xử lý thanh cuộn
    scrollbarWidth = window.innerWidth - document.documentElement.clientWidth;
    document.body.style.overflow = 'hidden';
    document.body.style.paddingRight = scrollbarWidth + 'px';

    modal.style.display = 'flex';

    // Focus vào textarea reason
    setTimeout(() => {
        modalReason.focus();
    }, 300);
}

function closeConfirmModal() {
    const modal = document.getElementById('confirmModal');
    modal.style.display = 'none';

    document.body.style.overflow = '';
    document.body.style.paddingRight = '';

    pendingAction = null;
}

function confirmAction() {
    if (!pendingAction) return;

    const { userId, action } = pendingAction;
    const reason = document.getElementById('modalReason').value.trim();
    const reasonError = document.getElementById('modalReasonError');
    const reasonInput = document.getElementById('modalReason');

    // Validate reason
    if (!reason) {
        reasonError.textContent = 'Vui lòng nhập lý do';
        reasonInput.classList.add('error');
        reasonInput.focus();
        return;
    }

    if (reason.length < 5) {
        reasonError.textContent = 'Lý do phải có ít nhất 5 ký tự';
        reasonInput.classList.add('error');
        reasonInput.focus();
        return;
    }

    reasonError.textContent = '';
    reasonInput.classList.remove('error');

    // ===== QUAN TRỌNG: Chuyển hướng đến link GET (KHÔNG GỌI API) =====
    const url = `/admin/accounts/toggle/${userId}?lock=${action === 'lock'}&reason=${encodeURIComponent(reason)}`;

    const confirmBtn = document.getElementById('modalConfirmBtn');
    confirmBtn.disabled = true;
    confirmBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang xử lý...';

    // Chuyển hướng trang
    window.location.href = url;
}

// Đóng modal khi click ra ngoài
document.addEventListener('click', function(event) {
    const modal = document.getElementById('confirmModal');
    if (event.target === modal) {
        closeConfirmModal();
    }
});

// Đóng modal khi nhấn ESC
document.addEventListener('keydown', function(event) {
    if (event.key === 'Escape') {
        closeConfirmModal();
    }
});

// Ctrl + Enter để submit
document.addEventListener('keydown', function(event) {
    if (event.key === 'Enter' && event.ctrlKey) {
        const modal = document.getElementById('confirmModal');
        if (modal && modal.style.display === 'flex') {
            confirmAction();
        }
    }
});

// ===== TOAST =====
function showToast(message, type) {
    const toast = document.getElementById('toast');
    const toastMessage = document.getElementById('toastMessage');
    const toastClose = document.getElementById('toastClose');

    if (!toast) return;

    toastMessage.textContent = message;
    toast.className = 'toast';
    toast.classList.add(type === 'success' ? 'toast-success' : 'toast-error');
    toast.style.display = 'flex';

    clearTimeout(toast._timeout);
    toast._timeout = setTimeout(() => {
        toast.style.display = 'none';
    }, 3000);
}

// Close toast
document.getElementById('toastClose')?.addEventListener('click', function() {
    document.getElementById('toast').style.display = 'none';
});