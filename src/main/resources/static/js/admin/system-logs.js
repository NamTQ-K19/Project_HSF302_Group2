/**
 * System Logs - Admin JavaScript
 * Version: 1.0.0
 */

(function() {
    'use strict';

    /**
     * Change page size
     * @param {number} size - Page size
     */
    window.changePageSize = function(size) {
        var url = new URL(window.location.href);
        url.searchParams.set('size', size);
        url.searchParams.set('page', 0);
        window.location.href = url.toString();
    };

    /**
     * Refresh logs page
     */
    window.refreshLogs = function() {
        var btn = document.querySelector('[onclick="refreshLogs()"]');
        if (btn) {
            var icon = btn.querySelector('.material-symbols-outlined');
            if (icon) {
                icon.style.animation = 'spin 0.6s ease-in-out';
                setTimeout(function() {
                    icon.style.animation = '';
                }, 600);
            }
        }
        window.location.reload();
    };

    /**
     * Auto submit filter on change
     */
    document.addEventListener('DOMContentLoaded', function() {
        var filterForm = document.getElementById('filterForm');
        if (filterForm) {
            // Auto submit on select change
            var selects = filterForm.querySelectorAll('select');
            selects.forEach(function(select) {
                select.addEventListener('change', function() {
                    filterForm.submit();
                });
            });

            // Debounce for date inputs
            var dateInputs = filterForm.querySelectorAll('input[type="datetime-local"]');
            var timeout;
            dateInputs.forEach(function(input) {
                input.addEventListener('change', function() {
                    clearTimeout(timeout);
                    timeout = setTimeout(function() {
                        filterForm.submit();
                    }, 500);
                });
            });
        }
    });

})();

// Add spin animation for refresh button
var style = document.createElement('style');
style.textContent = `
    @keyframes spin {
        from { transform: rotate(0deg); }
        to { transform: rotate(360deg); }
    }
`;
document.head.appendChild(style);