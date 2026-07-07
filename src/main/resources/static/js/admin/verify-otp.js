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
    const resultContainer = document.getElementById('resultContainer');

    console.log('Email:', email);

    // ===== CSRF TOKEN =====
    const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content;

    // ===== BIẾN TIMER =====
    let timerInterval = null;
    let timeLeft = 300;
    let isTimerExpired = false;
    let isSuccess = false;
    let isSubmitting = false;

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
            resendBtn.innerHTML = '<span class="material-symbols-outlined text-[18px]">schedule</span> Chờ hết thời gian';
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
                    resendBtn.innerHTML = '<span class="material-symbols-outlined text-[18px]">send</span> Gửi lại mã OTP';
                    resendBtn.style.opacity = '1';
                    resendBtn.style.cursor = 'pointer';
                }

                showToast('Mã OTP đã hết hạn. Vui lòng gửi lại mã mới.', 'warning');
            }
        }, 1000);
    }

    // ===== TOAST =====
    function showToast(message, type = 'success') {
        // Kiểm tra xem có toast global không
        if (window.showGlobalToast) {
            window.showGlobalToast(message, type);
            return;
        }

        // Tạo toast nếu chưa có
        let toast = document.getElementById('toast');
        if (!toast) {
            toast = document.createElement('div');
            toast.id = 'toast';
            toast.className = 'toast';
            toast.style.cssText = `
                position: fixed;
                bottom: 24px;
                right: 24px;
                background: white;
                padding: 16px 24px;
                border-radius: 12px;
                box-shadow: 0 10px 25px -5px rgba(0,0,0,0.1);
                display: none;
                align-items: center;
                gap: 16px;
                z-index: 9999;
                max-width: 400px;
                transform: translateY(20px);
                opacity: 0;
                transition: all 0.3s ease;
            `;
            toast.innerHTML = `
                <span id="toastMessage" style="font-size:14px;font-weight:500;"></span>
                <button id="toastClose" style="background:none;border:none;font-size:20px;cursor:pointer;color:#64748b;">&times;</button>
            `;
            document.body.appendChild(toast);

            document.getElementById('toastClose')?.addEventListener('click', function() {
                hideToast();
            });
        }

        const toastMessage = document.getElementById('toastMessage');
        if (toastMessage) {
            toastMessage.textContent = message;
        }

        // Reset classes
        toast.className = 'toast';
        if (type === 'success') {
            toast.style.borderLeft = '4px solid #22c55e';
        } else if (type === 'error') {
            toast.style.borderLeft = '4px solid #ef4444';
        } else if (type === 'warning') {
            toast.style.borderLeft = '4px solid #f59e0b';
        } else {
            toast.style.borderLeft = '4px solid #3b82f6';
        }

        toast.style.display = 'flex';
        setTimeout(function() {
            toast.style.opacity = '1';
            toast.style.transform = 'translateY(0)';
        }, 10);

        // Auto hide after 4 seconds
        clearTimeout(toast._hideTimeout);
        toast._hideTimeout = setTimeout(function() {
            hideToast();
        }, 4000);
    }

    function hideToast() {
        const toast = document.getElementById('toast');
        if (toast) {
            toast.style.opacity = '0';
            toast.style.transform = 'translateY(20px)';
            setTimeout(function() {
                toast.style.display = 'none';
            }, 300);
        }
    }

    // ===== HIỂN THỊ LỖI =====
    function setError(message) {
        if (errorDisplay) {
            errorDisplay.textContent = message;
            errorDisplay.style.color = '#ef4444';
            errorDisplay.classList.remove('hidden');
        }
        if (otpInput) {
            otpInput.classList.add('error');
            otpInput.style.borderColor = '#ef4444';
            otpInput.style.boxShadow = '0 0 0 3px rgba(239, 68, 68, 0.1)';
            otpInput.focus();
        }
    }

    function clearError() {
        if (errorDisplay) {
            errorDisplay.textContent = '';
            errorDisplay.classList.add('hidden');
        }
        if (otpInput) {
            otpInput.classList.remove('error');
            otpInput.style.borderColor = '';
            otpInput.style.boxShadow = '';
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
                    // Tự động xác thực khi đã có 6 số
                    if (verifyBtn && !verifyBtn.disabled) {
                        verifyBtn.click();
                    }
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
            if (isSubmitting) return;

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
                otpInput.value = '';
                return;
            }

            if (isTimerExpired) {
                setError('Mã OTP đã hết hạn. Vui lòng gửi lại mã mới.');
                return;
            }

            isSubmitting = true;
            verifyBtn.disabled = true;
            verifyBtn.innerHTML = '<span class="material-symbols-outlined text-[18px]">refresh</span> Đang xác thực...';

            // Tạo FormData
            const formData = new URLSearchParams();
            formData.append('email', email || '');
            formData.append('otpCode', otpCode);

            fetch('/admin/accounts/verify-otp', {
                method: 'POST',
                headers: {
                    [csrfHeader]: csrfToken,
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: formData.toString()
            })
                .then(response => {
                    // Kiểm tra content-type
                    const contentType = response.headers.get('content-type');
                    if (contentType && contentType.includes('application/json')) {
                        return response.json();
                    } else {
                        // Nếu không phải JSON, có thể là redirect hoặc HTML
                        throw new Error('Server trả về response không phải JSON. Vui lòng kiểm tra lại.');
                    }
                })
                .then(result => {
                    isSubmitting = false;
                    verifyBtn.disabled = false;
                    verifyBtn.innerHTML = '<span class="material-symbols-outlined text-[18px]">check_circle</span> Xác thực';

                    if (result.success) {
                        isSuccess = true;
                        showToast(result.message || 'Tài khoản đã được tạo thành công!', 'success');
                        verifyBtn.innerHTML = '<span class="material-symbols-outlined text-[18px]">check</span> Thành công!';
                        verifyBtn.style.background = '#22c55e';
                        verifyBtn.disabled = true;

                        // Hiển thị thông báo thành công
                        if (resultContainer) {
                            resultContainer.innerHTML = `
                            <div class="bg-green-50 border border-green-200 rounded-xl p-4 flex items-center gap-3 text-green-700">
                                <span class="material-symbols-outlined text-green-500">check_circle</span>
                                <span>${result.message || 'Tài khoản đã được tạo thành công!'}</span>
                            </div>
                        `;
                        }

                        setTimeout(function() {
                            window.location.href = '/admin/accounts';
                        }, 2500);
                    } else {
                        setError(result.message || 'Xác thực OTP thất bại');
                        showToast(result.message || 'Xác thực OTP thất bại', 'error');
                        otpInput.value = '';
                        otpInput.focus();
                    }
                })
                .catch(error => {
                    console.error('Verify OTP error:', error);
                    isSubmitting = false;
                    verifyBtn.disabled = false;
                    verifyBtn.innerHTML = '<span class="material-symbols-outlined text-[18px]">check_circle</span> Xác thực';

                    // Hiển thị lỗi chi tiết
                    const errorMsg = error.message || 'Lỗi hệ thống, vui lòng thử lại sau';
                    showToast(errorMsg, 'error');
                    setError(errorMsg);

                    if (resultContainer) {
                        resultContainer.innerHTML = `
                        <div class="bg-red-50 border border-red-200 rounded-xl p-4 flex items-center gap-3 text-red-700">
                            <span class="material-symbols-outlined text-red-500">error</span>
                            <span>${errorMsg}</span>
                        </div>
                    `;
                    }
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

            if (isSubmitting) return;
            isSubmitting = true;

            resendBtn.disabled = true;
            resendBtn.innerHTML = '<span class="material-symbols-outlined text-[18px]">refresh</span> Đang gửi...';
            resendBtn.style.opacity = '0.6';

            const formData = new URLSearchParams();
            formData.append('email', email);

            fetch('/admin/accounts/resend-otp', {
                method: 'POST',
                headers: {
                    [csrfHeader]: csrfToken,
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: formData.toString()
            })
                .then(response => {
                    const contentType = response.headers.get('content-type');
                    if (contentType && contentType.includes('application/json')) {
                        return response.json();
                    } else {
                        throw new Error('Server trả về response không phải JSON');
                    }
                })
                .then(result => {
                    isSubmitting = false;
                    resendBtn.disabled = false;
                    resendBtn.innerHTML = '<span class="material-symbols-outlined text-[18px]">send</span> Gửi lại mã OTP';
                    resendBtn.style.opacity = '1';

                    if (result.success) {
                        showToast(result.message || 'Mã OTP mới đã được gửi!', 'success');
                        startTimer(); // Reset timer khi gửi lại thành công
                        clearError();
                        if (otpInput) {
                            otpInput.value = '';
                            otpInput.focus();
                        }
                        if (verifyBtn) verifyBtn.disabled = false;
                    } else {
                        showToast(result.message || 'Gửi lại mã OTP thất bại', 'error');
                    }
                })
                .catch(error => {
                    console.error('Resend OTP error:', error);
                    isSubmitting = false;
                    resendBtn.disabled = false;
                    resendBtn.innerHTML = '<span class="material-symbols-outlined text-[18px]">send</span> Gửi lại mã OTP';
                    resendBtn.style.opacity = '1';
                    showToast('Lỗi gửi lại mã OTP: ' + error.message, 'error');
                });
        });
    }

    // ===== KEYBOARD SHORTCUTS =====
    if (otpInput) {
        // Chỉ cho phép nhập số
        otpInput.addEventListener('input', function() {
            this.value = this.value.replace(/\D/g, '');
            if (this.value.length > 6) {
                this.value = this.value.substring(0, 6);
            }
            if (this.value.length > 0) {
                clearError();
            }
            // Tự động xác thực khi đủ 6 số
            if (this.value.length === 6 && !isTimerExpired && !isSubmitting) {
                if (verifyBtn && !verifyBtn.disabled) {
                    verifyBtn.click();
                }
            }
        });

        // Xử lý phím Enter
        otpInput.addEventListener('keydown', function(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                if (verifyBtn && !verifyBtn.disabled) {
                    verifyBtn.click();
                }
                return;
            }

            // Chỉ cho phép số, Backspace, Delete, Tab, Arrow keys
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

        // Focus khi load
        otpInput.focus();
    }

    // ===== BẮT ĐẦU TIMER =====
    console.log('Calling startTimer...');
    startTimer();

    // ===== CHECK SERVER MESSAGE =====
    const serverMessage = document.getElementById('serverMessage');
    if (serverMessage) {
        const message = serverMessage.dataset.message;
        const type = serverMessage.dataset.type || 'info';
        if (message) {
            setTimeout(function() {
                showToast(message, type);
            }, 500);
        }
    }

    console.log('✅ Verify OTP page initialized');
    console.log('📧 Email:', email);
});