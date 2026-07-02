// D:\SU26\HSF302\Practice\Project_HSF302_Group2\src\main\resources\static\js\admin\create-account.js

document.addEventListener('DOMContentLoaded', function() {
    const form = document.getElementById('createAccountForm');
    const submitBtn = document.getElementById('submitBtn');

    // Clear errors
    function clearErrors() {
        document.querySelectorAll('.error').forEach(el => el.textContent = '');
        document.querySelectorAll('.form-group input, .form-group select').forEach(el => {
            el.classList.remove('error');
        });
    }

    // Set error
    function setError(fieldId, message) {
        const errorEl = document.getElementById(fieldId + 'Error');
        if (errorEl) {
            errorEl.textContent = message;
        }
        const inputEl = document.getElementById(fieldId);
        if (inputEl) {
            inputEl.classList.add('error');
        }
    }

    // Validate email
    function isValidEmail(email) {
        const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        return re.test(email);
    }

    // Validate phone
    function isValidPhone(phone) {
        const re = /^[0-9]{10,15}$/;
        return re.test(phone.replace(/[\s\-()]/g, ''));
    }

    // Validate username
    function isValidUsername(username) {
        const re = /^[a-zA-Z0-9_.]{3,50}$/;
        return re.test(username);
    }

    // Show toast
    function showToast(message, type = 'success') {
        if (window.showGlobalToast) {
            window.showGlobalToast(message, type);
        } else {
            alert(message);
        }
    }

    // Form submit
    form.addEventListener('submit', function(e) {
        e.preventDefault();
        clearErrors();

        const formData = {
            firstName: document.getElementById('firstName').value.trim(),
            lastName: document.getElementById('lastName').value.trim(),
            username: document.getElementById('username').value.trim(),
            email: document.getElementById('email').value.trim(),
            phone: document.getElementById('phone').value.trim(),
            role: document.getElementById('role').value
        };

        // Validate
        let hasError = false;

        // Validate firstName
        if (!formData.firstName) {
            setError('firstName', 'Vui lòng nhập họ');
            hasError = true;
        } else if (formData.firstName.length < 2) {
            setError('firstName', 'Họ phải có ít nhất 2 ký tự');
            hasError = true;
        }

        // Validate lastName
        if (!formData.lastName) {
            setError('lastName', 'Vui lòng nhập tên');
            hasError = true;
        } else if (formData.lastName.length < 2) {
            setError('lastName', 'Tên phải có ít nhất 2 ký tự');
            hasError = true;
        }

        // Validate username
        if (!formData.username) {
            setError('username', 'Vui lòng nhập tên đăng nhập');
            hasError = true;
        } else if (!isValidUsername(formData.username)) {
            setError('username', 'Tên đăng nhập phải có 3-50 ký tự (a-z, A-Z, 0-9, _, .)');
            hasError = true;
        }

        // Validate email
        if (!formData.email) {
            setError('email', 'Vui lòng nhập địa chỉ email');
            hasError = true;
        } else if (!isValidEmail(formData.email)) {
            setError('email', 'Địa chỉ email không hợp lệ');
            hasError = true;
        }

        // Validate phone
        if (!formData.phone) {
            setError('phone', 'Vui lòng nhập số điện thoại');
            hasError = true;
        } else if (!isValidPhone(formData.phone)) {
            setError('phone', 'Số điện thoại không hợp lệ (10-15 chữ số)');
            hasError = true;
        }

        // Validate role
        if (!formData.role) {
            setError('role', 'Vui lòng chọn vai trò');
            hasError = true;
        }

        if (hasError) {
            // Focus vào field đầu tiên bị lỗi
            const firstError = document.querySelector('.error');
            if (firstError) {
                const fieldId = firstError.id.replace('Error', '');
                const field = document.getElementById(fieldId);
                if (field) field.focus();
            }
            return;
        }

        // Submit
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Đang xử lý...';

        fetch('/admin/api/accounts/create', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(formData)
        })
            .then(response => response.json())
            .then(result => {
                if (result.success) {
                    showToast(result.message, 'success');
                    // Redirect đến trang xác thực OTP
                    setTimeout(() => {
                        window.location.href = `/admin/accounts/verify-otp?email=${encodeURIComponent(formData.email)}`;
                    }, 1500);
                } else {
                    showToast(result.message, 'error');
                    submitBtn.disabled = false;
                    submitBtn.innerHTML = '<i class="fas fa-paper-plane"></i> Tạo tài khoản';

                    // Nếu lỗi là email đã tồn tại
                    if (result.message && result.message.toLowerCase().includes('email')) {
                        setError('email', result.message);
                    }
                    // Nếu lỗi là username đã tồn tại
                    if (result.message && result.message.toLowerCase().includes('username')) {
                        setError('username', result.message);
                    }
                    // Nếu lỗi là phone đã tồn tại
                    if (result.message && result.message.toLowerCase().includes('phone')) {
                        setError('phone', result.message);
                    }
                }
            })
            .catch(error => {
                showToast('Lỗi hệ thống, vui lòng thử lại sau', 'error');
                submitBtn.disabled = false;
                submitBtn.innerHTML = '<i class="fas fa-paper-plane"></i> Tạo tài khoản';
                console.error('Error:', error);
            });
    });

    // ===== REAL-TIME VALIDATION =====

    // First Name
    document.getElementById('firstName').addEventListener('blur', function() {
        const value = this.value.trim();
        if (value && value.length < 2) {
            setError('firstName', 'Họ phải có ít nhất 2 ký tự');
        } else {
            document.getElementById('firstNameError').textContent = '';
            this.classList.remove('error');
        }
    });

    // Last Name
    document.getElementById('lastName').addEventListener('blur', function() {
        const value = this.value.trim();
        if (value && value.length < 2) {
            setError('lastName', 'Tên phải có ít nhất 2 ký tự');
        } else {
            document.getElementById('lastNameError').textContent = '';
            this.classList.remove('error');
        }
    });

    // Username
    document.getElementById('username').addEventListener('blur', function() {
        const value = this.value.trim();
        if (value && !isValidUsername(value)) {
            setError('username', 'Tên đăng nhập phải có 3-50 ký tự (a-z, A-Z, 0-9, _, .)');
        } else {
            document.getElementById('usernameError').textContent = '';
            this.classList.remove('error');
        }
    });

    // Email
    document.getElementById('email').addEventListener('blur', function() {
        const email = this.value.trim();
        if (email && !isValidEmail(email)) {
            setError('email', 'Địa chỉ email không hợp lệ');
        } else {
            document.getElementById('emailError').textContent = '';
            this.classList.remove('error');
        }
    });

    // Phone
    document.getElementById('phone').addEventListener('blur', function() {
        const phone = this.value.trim();
        if (phone && !isValidPhone(phone)) {
            setError('phone', 'Số điện thoại không hợp lệ (10-15 chữ số)');
        } else {
            document.getElementById('phoneError').textContent = '';
            this.classList.remove('error');
        }
    });

    // Role
    document.getElementById('role').addEventListener('change', function() {
        if (this.value) {
            document.getElementById('roleError').textContent = '';
            this.classList.remove('error');
        }
    });

    // ===== REAL-TIME INPUT CLEANING =====
    document.getElementById('phone').addEventListener('input', function() {
        // Chỉ cho phép số
        this.value = this.value.replace(/[^0-9]/g, '');
    });

    document.getElementById('username').addEventListener('input', function() {
        // Chỉ cho phép chữ cái, số, _, .
        this.value = this.value.replace(/[^a-zA-Z0-9_.]/g, '');
    });
});