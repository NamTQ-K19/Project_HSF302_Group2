window.openPasswordModal = function() {
    const modal = document.getElementById('passwordModal');
    if (modal) {
        modal.style.display = 'flex';
        document.body.style.overflow = 'hidden';
        const form = document.getElementById('passwordForm');
        if (form) form.reset();
    }
};

window.closePasswordModal = function() {
    const modal = document.getElementById('passwordModal');
    if (modal) {
        modal.style.display = 'none';
        document.body.style.overflow = '';
    }
};

document.addEventListener('DOMContentLoaded', function() {
    console.log('Admin Profile page loaded');

    // ===== KIỂM TRA CÓ CẦN MỞ MODAL TỰ ĐỘNG =====
    const openModalFlag = document.getElementById('openModalFlag');
    if (openModalFlag) {
        setTimeout(function() {
            window.openPasswordModal();
        }, 300);
    }

    // ===== AVATAR UPLOAD =====
    const avatarInput = document.getElementById('avatarInput');
    const avatarPreview = document.getElementById('avatarPreview');
    const avatarPlaceholder = document.getElementById('avatarPlaceholder');

    if (avatarInput) {
        avatarInput.addEventListener('change', function() {
            const file = this.files[0];
            if (file) {
                if (!file.type.startsWith('image/')) {
                    showToast('Vui lòng chọn file ảnh', 'error');
                    this.value = '';
                    return;
                }
                if (file.size > 5 * 1024 * 1024) {
                    showToast('File ảnh quá lớn (tối đa 5MB)', 'error');
                    this.value = '';
                    return;
                }

                const reader = new FileReader();
                reader.onload = function(e) {
                    if (avatarPreview) {
                        avatarPreview.src = e.target.result;
                        avatarPreview.style.display = 'block';
                    }
                    if (avatarPlaceholder) {
                        avatarPlaceholder.style.display = 'none';
                    }
                    uploadAvatar(file);
                };
                reader.readAsDataURL(file);
            }
        });
    }

    function uploadAvatar(file) {
        console.log('Uploading avatar...', file);

        const formData = new FormData();
        formData.append('avatar', file);

        fetch('/admin/profile/upload-avatar', {
            method: 'POST',
            body: formData
        })
            .then(response => {
                console.log('Response status:', response.status);
                if (!response.ok) {
                    throw new Error('HTTP ' + response.status);
                }
                return response.json();
            })
            .then(result => {
                console.log('Upload result:', result);
                if (result.success) {
                    showToast(result.message, 'success');
                    setTimeout(() => location.reload(), 1500);
                } else {
                    showToast(result.message, 'error');
                }
            })
            .catch(error => {
                console.error('Upload avatar error:', error);
                showToast('Lỗi tải ảnh lên: ' + error.message, 'error');
            });
    }

    // ===== CLOSE MODAL EVENTS =====
    document.querySelector('.modal-close')?.addEventListener('click', function() {
        window.closePasswordModal();
    });

    document.addEventListener('click', function(event) {
        const modal = document.getElementById('passwordModal');
        if (event.target === modal) {
            window.closePasswordModal();
        }
    });

    document.addEventListener('keydown', function(event) {
        if (event.key === 'Escape') {
            window.closePasswordModal();
        }
    });

    // ===== TOAST =====
    function showToast(message, type = 'success') {
        if (window.showGlobalToast) {
            window.showGlobalToast(message, type);
        } else {
            alert(message);
        }
    }
});