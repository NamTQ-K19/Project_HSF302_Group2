/**
 * Create Reservation - JavaScript
 */

(function() {
    'use strict';

    let selectedTableId = null;
    let currentMap = 1;
    let isMapVisible = false;
    let tableData = [];

    // ===== DOM Elements =====
    const reservationForm = document.getElementById('reservationForm');
    const reservationDate = document.getElementById('reservationDate');
    const reservationTime = document.getElementById('reservationTime');
    const partySize = document.getElementById('partySize');
    const stepMap = document.getElementById('stepMap');
    const mapImage = document.getElementById('mapImage');
    const tableList = document.getElementById('tableList');
    const selectedTableInfo = document.getElementById('selectedTableInfo');
    const selectedTableIdInput = document.getElementById('selectedTableId');
    const selectedTableName = document.getElementById('selectedTableName');
    const selectedTableCapacity = document.getElementById('selectedTableCapacity');

    // ===== Toggle Map =====
    window.toggleMap = function() {
        if (stepMap.style.display === 'none') {
            stepMap.style.display = 'block';
            isMapVisible = true;
            loadTables();
        } else {
            stepMap.style.display = 'none';
            isMapVisible = false;
        }
    };

    window.hideMap = function() {
        stepMap.style.display = 'none';
        isMapVisible = false;
    };

    // ===== Load Map =====
    window.loadMap = function(mapId) {
        currentMap = mapId;
        mapImage.src = '/images/map/floor' + mapId + '.png';

        document.querySelectorAll('.map-selector-btn').forEach(function(btn) {
            btn.classList.remove('active');
            if (parseInt(btn.dataset.map) === mapId) {
                btn.classList.add('active');
            }
        });

        if (isMapVisible) {
            loadTables();
        }
    };

    // ===== Load Tables =====
    function loadTables() {
        const date = reservationDate.value;
        const time = reservationTime.value;
        const party = partySize.value;

        if (!date || !time || !party) {
            tableList.innerHTML = '<div class="col-span-full text-center py-8 text-on-surface-variant">Vui lòng điền đầy đủ thông tin</div>';
            return;
        }

        tableList.innerHTML = '<div class="col-span-full text-center py-8 text-on-surface-variant">⏳ Đang tải...</div>';

        const csrfToken = document.querySelector('meta[name="_csrf"]')?.content || '';
        const csrfHeader = document.querySelector('meta[name="_csrf_header"]')?.content || 'X-CSRF-TOKEN';

        fetch('/customer/reservations/tables?date=' + encodeURIComponent(date) + '&time=' + encodeURIComponent(time) + '&partySize=' + encodeURIComponent(party), {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                [csrfHeader]: csrfToken
            }
        })
            .then(function(response) {
                if (!response.ok) {
                    throw new Error('Network response was not ok');
                }
                return response.json();
            })
            .then(function(data) {
                if (data.success) {
                    tableData = data.tables;
                    renderTables(data.tables);
                } else {
                    tableList.innerHTML = '<div class="col-span-full text-center py-8 text-error">❌ ' + data.message + '</div>';
                }
            })
            .catch(function(error) {
                console.error('Error:', error);
                tableList.innerHTML = '<div class="col-span-full text-center py-8 text-error">❌ Có lỗi xảy ra khi tải danh sách bàn</div>';
            });
    }

    // ===== Render Tables =====
    function renderTables(tables) {
        tableList.innerHTML = '';

        if (!tables || tables.length === 0) {
            tableList.innerHTML = '<div class="col-span-full text-center py-8 text-on-surface-variant">Không có bàn nào</div>';
            return;
        }

        const hasSelected = tables.some(function(t) { return t.tableId === selectedTableId; });
        if (!hasSelected) {
            selectedTableId = null;
            selectedTableIdInput.value = '';
            selectedTableInfo.style.display = 'none';
        }

        tables.forEach(function(table) {
            const div = document.createElement('div');
            const isAvailable = table.isAvailable;
            const isSelected = selectedTableId === table.tableId;

            div.className = 'table-item ' + (isAvailable ? 'available' : 'booked') + (isSelected ? ' selected' : '');
            div.innerHTML =
                '<div class="table-number">Bàn ' + table.tableId + '</div>' +
                '<div class="table-capacity">' + table.capacity + ' khách</div>' +
                '<div class="table-status ' + (isAvailable ? 'available' : 'booked') + '">' + (isAvailable ? '✓ Còn trống' : '✗ Đã đặt') + '</div>';

            if (isAvailable) {
                div.onclick = function() {
                    selectTable(table.tableId, table.capacity);
                };
            } else {
                div.title = 'Bàn này đã được đặt';
            }

            tableList.appendChild(div);
        });
    }

    // ===== Select Table =====
    function selectTable(tableId, capacity) {
        document.querySelectorAll('.table-item').forEach(function(el) {
            el.classList.remove('selected');
        });

        document.querySelectorAll('.table-item').forEach(function(el) {
            var numberDiv = el.querySelector('.table-number');
            if (numberDiv && numberDiv.textContent.includes('Bàn ' + tableId)) {
                el.classList.add('selected');
            }
        });

        selectedTableId = tableId;
        selectedTableIdInput.value = tableId;

        selectedTableName.textContent = 'Bàn ' + tableId;
        selectedTableCapacity.textContent = 'Sức chứa: ' + capacity + ' khách';
        selectedTableInfo.style.display = 'block';
    }

    // ===== Clear Selection =====
    window.clearSelection = function() {
        selectedTableId = null;
        selectedTableIdInput.value = '';
        selectedTableInfo.style.display = 'none';
        document.querySelectorAll('.table-item').forEach(function(el) {
            el.classList.remove('selected');
        });
    };

    // ===== Event Listeners =====
    // Khi thay đổi ngày/giờ/số khách, reset selection và reload tables
    reservationDate.addEventListener('change', function() {
        clearSelection();
        if (isMapVisible) loadTables();
    });

    reservationTime.addEventListener('change', function() {
        clearSelection();
        if (isMapVisible) loadTables();
    });

    partySize.addEventListener('change', function() {
        clearSelection();
        if (isMapVisible) loadTables();
    });

    // Kiểm tra trước khi submit
    reservationForm.addEventListener('submit', function(e) {
        if (selectedTableId) {
            return true;
        }

        if (!confirm('Bạn chưa chọn bàn cụ thể. Hệ thống sẽ tự động sắp xếp bàn phù hợp. Bạn có muốn tiếp tục?')) {
            e.preventDefault();
            return false;
        }
        return true;
    });

    // ===== Load bàn khi trang load =====
    document.addEventListener('DOMContentLoaded', function() {
        if (reservationDate && !reservationDate.value) {
            var today = new Date();
            reservationDate.value = today.toISOString().split('T')[0];
        }
        if (reservationTime && !reservationTime.value) {
            reservationTime.value = '19:00';
        }

        setTimeout(function() {
            if (reservationDate.value && reservationTime.value && partySize.value) {
                loadTables();
            }
        }, 500);
    });

    // Expose loadTables to window for debugging
    window.loadTables = loadTables;

})();