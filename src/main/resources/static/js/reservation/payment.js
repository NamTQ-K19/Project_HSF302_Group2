/**
 * Reservation Payment - JavaScript
 */

(function() {
    'use strict';

    // ===== Constants =====
    const VNPAY_METHOD_ID = 2;

    // ===== QR Data Map =====
    const qrDataMap = {
        1: {
            icon: 'payments',
            name: 'Tiền mặt',
            desc: 'Thanh toán khi nhận bàn',
            hasQR: false,
            qrImage: null,
            qrLabel: 'Thanh toán tiền mặt',
            qrSub: 'Bạn sẽ thanh toán trực tiếp tại quán'
        },
        2: {
            icon: 'qr_code_scanner',
            name: 'VNPay',
            desc: 'Quét mã QR qua VNPay',
            hasQR: true,
            qrImage: '/images/qr/vnpay-qr.png',
            qrLabel: 'VNPay',
            qrSub: 'Quét mã QR bằng ứng dụng VNPay'
        },
        3: {
            icon: 'qr_code_scanner',
            name: 'Momo',
            desc: 'Quét mã QR qua Momo',
            hasQR: true,
            qrImage: '/images/qr/momo-qr.png',
            qrLabel: 'Momo',
            qrSub: 'Quét mã QR bằng ứng dụng Momo'
        },
        4: {
            icon: 'qr_code_scanner',
            name: 'ZaloPay',
            desc: 'Quét mã QR qua ZaloPay',
            hasQR: true,
            qrImage: '/images/qr/zalopay-qr.png',
            qrLabel: 'ZaloPay',
            qrSub: 'Quét mã QR bằng ứng dụng ZaloPay'
        },
        5: {
            icon: 'credit_card',
            name: 'Thẻ Visa/Mastercard',
            desc: 'Nhập thông tin thẻ',
            hasQR: false,
            qrImage: null,
            qrLabel: 'Thẻ Visa/Mastercard',
            qrSub: 'Nhập thông tin thẻ để thanh toán'
        },
        6: {
            icon: 'qr_code_scanner',
            name: 'ShopeePay',
            desc: 'Quét mã QR qua ShopeePay',
            hasQR: true,
            qrImage: '/images/qr/shopeepay-qr.png',
            qrLabel: 'ShopeePay',
            qrSub: 'Quét mã QR bằng ứng dụng ShopeePay'
        }
    };

    // ===== DOM Elements =====
    const paymentForm = document.getElementById('paymentForm');
    const paymentMethodSelect = document.getElementById('paymentMethodSelect');
    const paymentError = document.getElementById('paymentError');
    const payBtn = document.getElementById('payBtn');
    const loadingOverlay = document.getElementById('paymentLoading');
    const qrPlaceholder = document.getElementById('qrPlaceholder');
    const qrContent = document.getElementById('qrContent');
    const noQrContent = document.getElementById('noQrContent');
    const methodInfo = document.getElementById('selectedMethodInfo');
    const vnpayInfo = document.getElementById('vnpayInfo');
    const methodIcon = document.getElementById('methodIcon');
    const methodName = document.getElementById('methodName');
    const methodDesc = document.getElementById('methodDesc');
    const qrImage = document.getElementById('qrImage');
    const qrLabel = document.getElementById('qrLabel');
    const qrSub = document.getElementById('qrSub');
    const noQrTitle = document.getElementById('noQrTitle');
    const noQrDesc = document.getElementById('noQrDesc');

    // ===== Functions =====
    function onPaymentMethodChange(select) {
        const methodId = parseInt(select.value);

        // Reset error
        if (paymentError) {
            paymentError.classList.add('hidden');
        }

        if (!methodId) {
            // Show placeholder
            if (qrPlaceholder) qrPlaceholder.style.display = 'block';
            if (qrContent) qrContent.classList.remove('show');
            if (noQrContent) noQrContent.classList.remove('show');
            if (methodInfo) methodInfo.classList.remove('show');
            if (vnpayInfo) vnpayInfo.style.display = 'none';
            if (payBtn) payBtn.disabled = true;
            return;
        }

        // Enable pay button
        if (payBtn) payBtn.disabled = false;

        const data = qrDataMap[methodId];
        if (!data) return;

        // Show VNPay info if selected
        if (methodId === VNPAY_METHOD_ID) {
            if (vnpayInfo) vnpayInfo.style.display = 'block';
        } else {
            if (vnpayInfo) vnpayInfo.style.display = 'none';
        }

        // Show method info
        if (methodIcon) methodIcon.textContent = data.icon;
        if (methodName) methodName.textContent = data.name;
        if (methodDesc) methodDesc.textContent = data.desc;
        if (methodInfo) methodInfo.classList.add('show');

        // Hide all
        if (qrPlaceholder) qrPlaceholder.style.display = 'none';
        if (qrContent) qrContent.classList.remove('show');
        if (noQrContent) noQrContent.classList.remove('show');

        if (data.hasQR && data.qrImage) {
            if (qrContent) qrContent.classList.add('show');
            if (qrImage) qrImage.src = data.qrImage;
            if (qrLabel) qrLabel.textContent = data.qrLabel;
            if (qrSub) qrSub.textContent = data.qrSub;
        } else {
            if (noQrContent) noQrContent.classList.add('show');
            if (noQrTitle) noQrTitle.textContent = data.name;
            if (noQrDesc) noQrDesc.textContent = data.desc;
        }
    }

    // ===== Event Listeners =====
    if (paymentForm) {
        paymentForm.addEventListener('submit', function(e) {
            const select = document.getElementById('paymentMethodSelect');
            if (!select || !select.value) {
                e.preventDefault();
                if (paymentError) {
                    paymentError.classList.remove('hidden');
                }
                if (select) select.focus();
                return false;
            }

            // Show loading overlay
            if (loadingOverlay) {
                loadingOverlay.classList.add('show');
            }

            // Disable button
            if (payBtn) {
                payBtn.disabled = true;
                payBtn.innerHTML = `
                    <span class="material-symbols-outlined text-[18px]">refresh</span>
                    Đang xử lý...
                `;
            }

            return true;
        });
    }

    // ===== Init =====
    document.addEventListener('DOMContentLoaded', function() {
        // Auto-select first available method (optional)
        if (paymentMethodSelect && paymentMethodSelect.options.length > 1) {
            // Không auto select, để user tự chọn
        }

        // Disable pay button initially
        if (payBtn) {
            payBtn.disabled = true;
        }

        // If there's a pre-selected value, trigger change
        if (paymentMethodSelect && paymentMethodSelect.value) {
            onPaymentMethodChange(paymentMethodSelect);
        }
    });

    // ===== Expose to global =====
    window.onPaymentMethodChange = onPaymentMethodChange;

})();