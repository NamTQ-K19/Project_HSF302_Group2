/**
 * BrewMaster Order List & Management Script
 * Handles invoice status updates, payment confirmations, and order details modal.
 */

const csrfToken = document.querySelector('meta[name="_csrf"]')?.getAttribute('content');
const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.getAttribute('content') || 'X-CSRF-TOKEN';

function getHeaders() {
    const headers = { 'Content-Type': 'application/json' };
    if (csrfToken && csrfHeader) {
        headers[csrfHeader] = csrfToken;
    }
    return headers;
}

function openDetailModal(orderId) {
    const modal = document.getElementById('detail-modal');
    const content = document.getElementById('detail-modal-content');
    if (!modal || !content) return;

    content.innerHTML = `<div class="p-12 text-center text-on-surface-variant"><span class="material-symbols-outlined text-4xl animate-spin">refresh</span><p class="mt-2 text-sm">Đang tải chi tiết đơn hàng...</p></div>`;
    modal.classList.remove('hidden');
    modal.classList.add('flex');

    fetch(`/order/api/${orderId}`, { headers: getHeaders() })
        .then(res => res.json())
        .then(order => {
            renderOrderDetailContent(order);
        })
        .catch(err => {
            console.error('Error fetching order detail:', err);
            content.innerHTML = `<div class="p-12 text-center text-error"><span class="material-symbols-outlined text-4xl">error</span><p class="mt-2 text-sm">Không thể tải thông tin đơn hàng</p></div>`;
        });
}

function renderOrderDetailContent(order) {
    const content = document.getElementById('detail-modal-content');
    if (!content) return;

    const isPaid = order.paymentStatus === 'SUCCESS';
    const isCompleted = order.orderStatus === 'COMPLETED' || order.orderStatus === 'CANCELLED';

    content.innerHTML = `
        <div class="p-6 border-b border-outline-variant/40 flex items-center justify-between bg-surface-container/30">
            <div>
                <div class="flex items-center gap-2">
                    <span class="font-headline-md font-bold text-primary text-xl">Đơn hàng #ORD-${order.orderId}</span>
                    <span class="status-badge status-${order.orderStatus}">${order.orderStatus}</span>
                    <span class="status-badge payment-${order.paymentStatus}">${order.paymentStatus}</span>
                </div>
                <div class="text-xs text-on-surface-variant mt-1">Ngày tạo: ${formatDateTime(order.createdAt)} • Loại: <b>${order.orderType}</b> ${order.tableId ? '• Bàn số ' + order.tableId : ''}</div>
            </div>
            <button onclick="closeDetailModal()" class="w-8 h-8 rounded-full bg-surface-container flex items-center justify-center text-on-surface-variant hover:text-on-surface border-none cursor-pointer">
                <span class="material-symbols-outlined text-[20px]">close</span>
            </button>
        </div>

        <div class="p-6 max-h-[60vh] overflow-y-auto">
            <!-- Customer Info -->
            <div class="p-4 rounded-2xl bg-surface-container-lowest border border-outline-variant/60 mb-6 flex items-center justify-between">
                <div>
                    <div class="text-xs font-bold text-on-surface-variant uppercase tracking-wider mb-1">Khách hàng / Người đặt</div>
                    <div class="font-bold text-on-surface text-sm">${order.customerName || 'Khách vãng lai'}</div>
                    <div class="text-xs text-on-surface-variant">${order.customerPhone || 'Không có số điện thoại'}</div>
                </div>
                <div class="text-right">
                    <div class="text-xs font-bold text-on-surface-variant uppercase tracking-wider mb-1">Thanh toán</div>
                    <div class="font-bold text-on-surface text-sm">${order.paymentMethodName || 'Tiền mặt'}</div>
                    <div class="text-xs text-on-surface-variant">${order.transactionRef || ''}</div>
                </div>
            </div>

            <!-- Items -->
            <h4 class="text-xs font-bold text-on-surface-variant uppercase tracking-wider mb-3">Danh sách món (${order.items ? order.items.length : 0})</h4>
            <div class="space-y-2 mb-6">
                ${(order.items || []).map(i => `
                    <div class="p-3 rounded-xl bg-surface-container-lowest border border-outline-variant/40 flex items-center justify-between">
                        <div>
                            <span class="font-bold text-sm text-on-surface">${i.productName}</span>
                            <span class="text-xs text-primary font-medium ml-2">(${i.variantName})</span>
                            ${i.specialNote ? `<div class="text-[11px] text-on-surface-variant italic mt-0.5">"${i.specialNote}"</div>` : ''}
                        </div>
                        <div class="text-right">
                            <span class="text-xs text-on-surface-variant">${formatCurrency(i.price)} x ${i.quantity}</span>
                            <div class="font-bold text-sm text-on-surface">${formatCurrency(i.itemTotal)}</div>
                        </div>
                    </div>
                `).join('')}
            </div>

            <!-- Total -->
            <div class="p-4 rounded-2xl bg-primary/5 border border-primary/20 flex items-center justify-between">
                <span class="font-bold text-on-surface text-base">Tổng tiền thanh toán:</span>
                <span class="font-headline-md text-primary font-bold text-xl">${formatCurrency(order.totalAmount)}</span>
            </div>
        </div>

        <div class="p-6 border-t border-outline-variant/60 bg-surface-container/30 flex flex-wrap items-center justify-between gap-3">
            <div class="flex items-center gap-2">
                ${!isCompleted ? `
                    <select id="modal-status-select-${order.orderId}" class="px-3 py-2 rounded-xl border border-outline-variant bg-surface-container-lowest text-xs font-semibold text-on-surface">
                        <option value="CONFIRMED" ${order.orderStatus === 'CONFIRMED' ? 'selected' : ''}>Xác nhận (CONFIRMED)</option>
                        <option value="PREPARING" ${order.orderStatus === 'PREPARING' ? 'selected' : ''}>Pha chế (PREPARING)</option>
                        <option value="READY" ${order.orderStatus === 'READY' ? 'selected' : ''}>Sẵn sàng (READY)</option>
                        <option value="COMPLETED" ${order.orderStatus === 'COMPLETED' ? 'selected' : ''}>Hoàn thành (COMPLETED)</option>
                        <option value="CANCELLED" ${order.orderStatus === 'CANCELLED' ? 'selected' : ''}>Hủy đơn (CANCELLED)</option>
                    </select>
                    <button onclick="updateStatusFromModal(${order.orderId})" class="px-4 py-2 rounded-xl bg-secondary text-on-secondary font-bold text-xs hover:bg-secondary/90 transition-all border-none cursor-pointer">
                        Cập nhật hóa đơn
                    </button>
                ` : ''}
            </div>

            <div class="flex items-center gap-2">
                ${!isPaid && order.orderStatus !== 'CANCELLED' ? `
                    <button onclick="confirmPaymentAction(${order.orderId}, ${order.totalAmount})" class="px-5 py-2.5 rounded-xl bg-emerald-600 text-white font-bold text-xs hover:bg-emerald-700 transition-all shadow-md shadow-emerald-600/20 border-none cursor-pointer flex items-center gap-1.5">
                        <span class="material-symbols-outlined text-[18px]">paid</span>
                        Xác nhận thanh toán
                    </button>
                ` : ''}
                ${isPaid ? `
                    <button onclick="printInvoice(${order.orderId})" class="px-5 py-2.5 rounded-xl bg-blue-600 text-white font-bold text-xs hover:bg-blue-700 transition-all shadow-md shadow-blue-600/20 border-none cursor-pointer flex items-center gap-1.5">
                        <span class="material-symbols-outlined text-[18px]">print</span>
                        In hóa đơn
                    </button>
                ` : ''}
                <button onclick="closeDetailModal()" class="px-5 py-2.5 rounded-xl bg-surface-container text-on-surface font-semibold text-xs hover:bg-surface-container-high transition-all border border-outline-variant cursor-pointer">
                    Đóng
                </button>
            </div>
        </div>
    `;
}

function closeDetailModal() {
    const modal = document.getElementById('detail-modal');
    if (modal) {
        modal.classList.add('hidden');
        modal.classList.remove('flex');
    }
}

function updateStatusFromModal(orderId) {
    const sel = document.getElementById(`modal-status-select-${orderId}`);
    if (!sel) return;
    updateOrderStatus(orderId, sel.value);
}

function updateOrderStatus(orderId, newStatus) {
    const executeUpdate = () => {
        fetch(`/order/api/${orderId}/status`, {
            method: 'PUT',
            headers: getHeaders(),
            body: JSON.stringify({ status: newStatus })
        })
        .then(res => {
            if (!res.ok) throw new Error('Cập nhật trạng thái thất bại');
            return res.json();
        })
        .then(order => {
            closeDetailModal();
            if (typeof Swal !== 'undefined') {
                Swal.fire({
                    title: 'Cập nhật thành công!',
                    text: `Đã cập nhật trạng thái đơn #${orderId} thành ${newStatus}`,
                    icon: 'success',
                    confirmButtonText: 'Đóng & Tải lại'
                }).then(() => {
                    location.reload();
                });
            } else if (typeof window.showGlobalConfirm === 'function') {
                window.showGlobalConfirm(
                    'Cập nhật thành công!',
                    `Đã cập nhật trạng thái đơn #${orderId} thành ${newStatus}.<br>Ấn Xác nhận để tiếp tục.`,
                    () => location.reload()
                );
            } else {
                alert(`Đã cập nhật trạng thái đơn #${orderId} thành ${newStatus}`);
                location.reload();
            }
        })
        .catch(err => {
            console.error('Update status error:', err);
            if (typeof window.showGlobalToast === 'function') {
                window.showGlobalToast(err.message, 'error');
            }
        });
    };

    if (typeof window.showGlobalConfirm === 'function') {
        window.showGlobalConfirm(
            'Xác nhận cập nhật hóa đơn',
            `Chuyển trạng thái đơn hàng <b>#${orderId}</b> thành <span class="status-badge status-${newStatus}">${newStatus}</span>?`,
            executeUpdate
        );
    } else {
        if (confirm(`Chuyển trạng thái đơn #${orderId} thành ${newStatus}?`)) {
            executeUpdate();
        }
    }
}

function confirmPaymentAction(orderId, amount) {
    const executeConfirm = (methodId, txRef) => {
        fetch(`/order/api/${orderId}/confirm-payment`, {
            method: 'PUT',
            headers: getHeaders(),
            body: JSON.stringify({
                paymentMethodId: methodId ? parseInt(methodId) : null,
                transactionRef: txRef || ''
            })
        })
        .then(res => {
            if (!res.ok) return res.json().then(e => { throw new Error(e.message || 'Lỗi thanh toán'); });
            return res.json();
        })
        .then(order => {
            closeDetailModal();
            if (typeof Swal !== 'undefined') {
                Swal.fire({
                    title: 'Thanh toán thành công!',
                    text: `Đã xác nhận thu đủ tiền cho hóa đơn #${orderId}`,
                    icon: 'success',
                    confirmButtonText: 'Đóng & Tải lại'
                }).then(() => {
                    location.reload();
                });
            } else if (typeof window.showGlobalConfirm === 'function') {
                window.showGlobalConfirm(
                    'Thanh toán thành công!',
                    `Đã xác nhận thu đủ tiền cho hóa đơn #${orderId}.<br>Ấn Xác nhận để tiếp tục.`,
                    () => location.reload()
                );
            } else {
                alert(`Đã xác nhận thanh toán thành công cho đơn #${orderId}`);
                location.reload();
            }
        })
        .catch(err => {
            console.error('Confirm payment error:', err);
            if (typeof window.showGlobalToast === 'function') {
                window.showGlobalToast(err.message, 'error');
            }
        });
    };

    if (typeof Swal !== 'undefined') {
        Swal.fire({
            title: `Xác nhận thanh toán #${orderId}`,
            html: `
                <div class="text-left mt-2">
                    <p class="text-sm mb-3">Số tiền cần thu: <strong class="text-primary text-base">${formatCurrency(amount)}</strong></p>
                    <label class="block text-xs font-bold uppercase mb-1 text-on-surface-variant">Phương thức thu tiền:</label>
                    <select id="swal-pm-select" class="w-full px-3 py-2 rounded-xl border border-outline-variant bg-surface-container-lowest text-sm mb-3">
                        <option value="1">Tiền mặt (Cash)</option>
                        <option value="2">Chuyển khoản / QR (Bank Transfer)</option>
                        <option value="3">Thẻ ngân hàng (Card)</option>
                    </select>
                    <label class="block text-xs font-bold uppercase mb-1 text-on-surface-variant">Mã giao dịch / Ghi chú (tùy chọn):</label>
                    <input type="text" id="swal-tx-input" placeholder="Nhập mã GD nếu có..." class="w-full px-3 py-2 rounded-xl border border-outline-variant bg-surface-container-lowest text-sm">
                </div>
            `,
            icon: 'info',
            showCancelButton: true,
            confirmButtonColor: '#059669',
            cancelButtonColor: '#706d6c',
            confirmButtonText: 'Đã nhận tiền',
            cancelButtonText: 'Hủy',
            preConfirm: () => {
                const pmEl = document.getElementById('swal-pm-select');
                const txEl = document.getElementById('swal-tx-input');
                return {
                    methodId: pmEl ? pmEl.value : 1,
                    txRef: txEl ? txEl.value : ''
                };
            }
        }).then((result) => {
            if (result && result.isConfirmed) {
                const mId = result.value ? result.value.methodId : (document.getElementById('swal-pm-select')?.value || 1);
                const tRef = result.value ? result.value.txRef : (document.getElementById('swal-tx-input')?.value || '');
                executeConfirm(mId, tRef);
            }
        });
    } else if (typeof window.showGlobalConfirm === 'function') {
        window.showGlobalConfirm(
            `Xác nhận thu tiền #${orderId}`,
            `Xác nhận đã nhận đủ <b>${formatCurrency(amount)}</b> cho đơn hàng này?`,
            () => executeConfirm(1, '')
        );
    } else {
        if (confirm(`Xác nhận đã nhận đủ ${formatCurrency(amount)} cho đơn #${orderId}?`)) {
            executeConfirm(1, '');
        }
    }
}

function formatCurrency(val) {
    if (val === null || val === undefined) return '0 ₫';
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);
}

function formatDateTime(dtStr) {
    if (!dtStr) return '';
    const d = new Date(dtStr);
    return d.toLocaleString('vi-VN', { hour: '2-digit', minute: '2-digit', day: '2-digit', month: '2-digit', year: 'numeric' });
}

function printInvoice(orderId) {
    window.open(`/cashier/invoices/${orderId}`, '_blank');
}
