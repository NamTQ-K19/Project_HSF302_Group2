// D:\SU26\HSF302\Practice\Project_HSF302_Group2\src\main\resources\static\js\admin\verify-otp.js

console.log('Verify OTP JS loaded!');

document.addEventListener('DOMContentLoaded', function() {
    console.log('DOM loaded, starting timer...');

    const timerDisplay = document.getElementById('timer');
    const verifyBtn = document.getElementById('verifyBtn');
    const resendBtn = document.getElementById('resendBtn');
    const otpInput = document.getElementById('otpCode');
    const errorDisplay = document.getElementById('otpError');
    const emailInput = document.querySelector('input[name="email"]');
    const email = emailInput ? emailInput.value : null;

    console.log('Email:', email);

    // ===== BIẾN TIMER =====
    let timerInterval = null;
    let timeLeft = 300;
    let isTimerExpired = false;
    let isSuccess = false;

    // ===== HÀM CẬP NHẬT HIỂN THỊ =====
    function updateTimerDisplay() {
        if (!timerDisplay) return;
        const minutes = Math.floor(timeLeft / 60);
        const seconds = timeLeft % 60;
        timerDisplay.textContent = String(minutes).padStart(2, '0') + ':' + String(seconds).padStart(2, '0');
    }

    // ===== HÀM BẮT ĐẦU TIMER =====
    function startTimer() {
        console.log('startTimer called!');

        timeLeft = 300;
        isTimerExpired = false;

        if (timerDisplay) {
            timerDisplay.classList.remove('expired');
            timerDisplay.style.color = '#4a2c1b';
        }

        if (verifyBtn) verifyBtn.disabled = false;
        if (resendBtn) {
            resendBtn.disabled = true;
            resendBtn.innerHTML = '<i class="fas fa-clock"></i> Chờ hết thời gian';
            resendBtn.style.opacity = '0.6';
            resendBtn.style.cursor = 'not-allowed';
        }

        updateTimerDisplay();

        if (timerInterval) {
            clearInterval(timerInterval);
            timerInterval = null;
        }

        timerInterval = setInterval(function() {
            timeLeft--;
            updateTimerDisplay();

            if (timeLeft <= 0) {
                clearInterval(timerInterval);
                timerInterval = null;
                isTimerExpired = true;

                if (timerDisplay) {
                    timerDisplay.classList.add('expired');
                    timerDisplay.style.color = '#ef4444';
                    timerDisplay.textContent = '00:00';
                }

                if (verifyBtn) verifyBtn.disabled = true;
                if (resendBtn) {
                    resendBtn.disabled = false;
                    resendBtn.innerHTML = '<i class="fas fa-redo"></i> Gửi lại mã';
                    resendBtn.style.opacity = '1';
                    resendBtn.style.cursor = 'pointer';
                }

                showToast('Mã OTP đã hết hạn. Vui lòng gửi lại mã mới.', 'warning');
            }
        }, 1000);
    }

    // ===== TOAST =====
    function showToast(message, type = 'success') {
        if (window.showGlobalToast) {
            window.showGlobalToast(message, type);
        } else {
            alert(message);
        }
    }

    // ===== HIỂN THỊ LỖI =====
    function setError(message) {
        if (errorDisplay) {
            errorDisplay.textContent = message;
            errorDisplay.style.color = '#ef4444';
        }
        if (otpInput) {
            otpInput.classList.add('error');
            otpInput.focus();
        }
    }

    function clearError() {
        if (errorDisplay) {
            errorDisplay.textContent = '';
        }
        if (otpInput) {
            otpInput.classList.remove('error');
        }
    }

    // ===== PASTE BUTTON =====
    const pasteBtn = document.getElementById('pasteBtn');
    if (pasteBtn) {
        pasteBtn.addEventListener('click', async function() {
            try {
                const text = await navigator.clipboard.readText();
                const cleaned = text.trim().replace(/\s/g, '');
                if (/^\d{6}$/.test(cleaned) && otpInput) {
                    otpInput.value = cleaned;
                    clearError();
                } else if (otpInput) {
                    setError('Nội dung dán không phải mã OTP hợp lệ (6 chữ số)');
                }
            } catch (err) {
                if (otpInput) setError('Không thể đọc dữ liệu từ clipboard');
                console.error('Paste error:', err);
            }
        });
    }

    // ===== VERIFY OTP =====
    if (verifyBtn) {
        verifyBtn.addEventListener('click', function() {
            if (isSuccess) {
                showToast('Tài khoản đã được tạo thành công!', 'success');
                return;
            }

            clearError();

            if (!otpInput) return;
            const otpCode = otpInput.value.trim();

            if (!otpCode) {
                setError('Vui lòng nhập mã OTP');
                return;
            }

            if (otpCode.length !== 6 || !/^\d+$/.test(otpCode)) {
                setError('Mã OTP phải là 6 chữ số');
                return;
            }

            if (isTimerExpired) {
                setError('Mã OTP đã hết hạn. Vui lòng gửi lại mã mới.');
                return;
            }

            verifyBtn.disabled = true;
            verifyBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang xác thực...';

            const formData = new FormData();
            formData.append('email', email || '');
            formData.append('otpCode', otpCode);

            fetch('/admin/accounts/verify-otp', {
                method: 'POST',
                body: formData
            })
                .then(response => response.json())
                .then(result => {
                    verifyBtn.disabled = false;
                    verifyBtn.innerHTML = '<i class="fas fa-check-circle"></i> Xác thực';

                    if (result.success) {
                        isSuccess = true;
                        showToast(result.message, 'success');
                        verifyBtn.innerHTML = '<i class="fas fa-check"></i> Thành công!';
                        setTimeout(function() {
                            window.location.href = '/admin/accounts';
                        }, 2000);
                    } else {
                        setError(result.message);
                        showToast(result.message, 'error');
                        if (otpInput) otpInput.focus();
                    }
                })
                .catch(error => {
                    console.error('Verify OTP error:', error);
                    showToast('Lỗi hệ thống, vui lòng thử lại sau', 'error');
                    verifyBtn.disabled = false;
                    verifyBtn.innerHTML = '<i class="fas fa-check-circle"></i> Xác thực';
                });
        });
    }

    // ===== RESEND OTP =====
    if (resendBtn) {
        resendBtn.addEventListener('click', function(e) {
            e.preventDefault();

            if (!isTimerExpired) {
                showToast('Vui lòng đợi mã OTP hiện tại hết hạn trước khi gửi lại!', 'warning');
                return;
            }

            if (!email) {
                showToast('Không tìm thấy địa chỉ email', 'error');
                return;
            }

            resendBtn.disabled = true;
            resendBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang gửi...';
            resendBtn.style.opacity = '0.6';

            const formData = new FormData();
            formData.append('email', email);

            fetch('/admin/accounts/resend-otp', {
                method: 'POST',
                body: formData
            })
                .then(response => response.json())
                .then(result => {
                    resendBtn.disabled = false;
                    resendBtn.innerHTML = '<i class="fas fa-redo"></i> Gửi lại mã';
                    resendBtn.style.opacity = '1';

                    if (result.success) {
                        showToast(result.message, 'success');
                        startTimer(); // Reset timer khi gửi lại thành công
                    } else {
                        showToast(result.message, 'error');
                    }
                })
                .catch(error => {
                    console.error('Resend OTP error:', error);
                    showToast('Lỗi gửi lại mã OTP', 'error');
                    resendBtn.disabled = false;
                    resendBtn.innerHTML = '<i class="fas fa-redo"></i> Gửi lại mã';
                    resendBtn.style.opacity = '1';
                });
        });
    }

    // ===== KEYBOARD SHORTCUTS =====
    if (otpInput) {
        otpInput.addEventListener('keydown', function(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                if (verifyBtn) verifyBtn.click();
                return;
            }

            if (!/^\d$/.test(e.key) &&
                e.key !== 'Backspace' &&
                e.key !== 'Delete' &&
                e.key !== 'Tab' &&
                e.key !== 'ArrowLeft' &&
                e.key !== 'ArrowRight' &&
                e.key !== 'Home' &&
                e.key !== 'End') {
                e.preventDefault();
            }
        });

        otpInput.addEventListener('input', function() {
            this.value = this.value.replace(/\D/g, '');
        });
    }

    // ===== BẮT ĐẦU TIMER =====
    console.log('Calling startTimer...');
    startTimer();

    if (otpInput) otpInput.focus();
});