document.addEventListener('DOMContentLoaded', function() {
    console.log('BrewMaster Dashboard loaded');

    // Vẽ biểu đồ với dữ liệu từ Thymeleaf
    if (typeof chartLabels !== 'undefined' && chartLabels && chartLabels.length > 0) {
        renderChart(chartLabels, chartSalesData, chartOrderCounts);
    } else {
        console.warn('Không có dữ liệu biểu đồ');
        const canvas = document.getElementById('salesChart');
        if (canvas) {
            canvas.style.display = 'none';
            const parent = canvas.parentElement;
            const msg = document.createElement('div');
            msg.style.cssText = 'text-align:center; color:#94a3b8; padding:40px;';
            msg.textContent = '📊 Chưa có dữ liệu doanh số';
            parent.appendChild(msg);
        }
    }
});

// Biến toàn cục cho biểu đồ
let salesChartInstance = null;

function renderChart(labels, salesData, orderCounts) {
    const ctx = document.getElementById('salesChart');
    if (!ctx) {
        console.warn('Không tìm thấy canvas salesChart');
        return;
    }

    // Hủy biểu đồ cũ nếu có
    if (salesChartInstance) {
        salesChartInstance.destroy();
    }

    const formatter = new Intl.NumberFormat('vi-VN', {
        style: 'currency',
        currency: 'VND',
        minimumFractionDigits: 0
    });

    salesChartInstance = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels || [],
            datasets: [
                {
                    label: 'Doanh số (đ)',
                    data: salesData || [],
                    backgroundColor: 'rgba(200, 168, 130, 0.6)',
                    borderColor: '#c8a882',
                    borderWidth: 2,
                    borderRadius: 4,
                    order: 1
                },
                {
                    label: 'Số đơn hàng',
                    data: orderCounts || [],
                    type: 'line',
                    backgroundColor: 'rgba(74, 44, 27, 0.1)',
                    borderColor: '#4a2c1b',
                    borderWidth: 2,
                    pointBackgroundColor: '#4a2c1b',
                    pointRadius: 4,
                    tension: 0.3,
                    order: 0,
                    yAxisID: 'y1'
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            interaction: {
                mode: 'index',
                intersect: false
            },
            plugins: {
                legend: {
                    position: 'top',
                    labels: {
                        usePointStyle: true,
                        padding: 20,
                        font: {
                            size: 11,
                            family: 'Inter'
                        }
                    }
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            let label = context.dataset.label || '';
                            let value = context.raw;
                            if (context.dataset.label.includes('Doanh số')) {
                                return label + ': ' + formatter.format(value);
                            }
                            return label + ': ' + value + ' đơn';
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    grid: {
                        drawBorder: false,
                        color: 'rgba(0,0,0,0.05)'
                    },
                    ticks: {
                        callback: function(value) {
                            if (value >= 1000000) {
                                return (value / 1000000).toFixed(1) + 'tr';
                            } else if (value >= 1000) {
                                return (value / 1000).toFixed(0) + 'k';
                            }
                            return value;
                        }
                    }
                },
                y1: {
                    position: 'right',
                    beginAtZero: true,
                    grid: {
                        drawOnChartArea: false
                    },
                    ticks: {
                        stepSize: 1
                    }
                },
                x: {
                    grid: {
                        display: false
                    }
                }
            }
        }
    });

    console.log('Biểu đồ đã được vẽ thành công!');
}