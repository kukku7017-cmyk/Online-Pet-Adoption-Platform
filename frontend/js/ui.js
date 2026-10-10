/**
 * UI.JS - User Interface Utilities, Toasts, Modals, and Micro-interactions
 */

const UI = (() => {
    const getToastContainer = () => {
        let container = document.querySelector('.nm-toast-container');
        if (!container && document.body) {
            container = document.createElement('div');
            container.className = 'nm-toast-container';
            document.body.appendChild(container);
        }
        return container;
    };

    const toast = (message, type = 'info', duration = 3500) => {
        const container = getToastContainer();
        if (!container) return;
        const toastEl = document.createElement('div');
        toastEl.className = `nm-toast toast-${type}`;
        
        let icon = 'ℹ️';
        if (type === 'success') icon = '✅';
        if (type === 'error') icon = '⚠️';
        if (type === 'warning') icon = '🔔';

        toastEl.innerHTML = `
            <span style="font-size: 1.25rem;">${icon}</span>
            <span style="font-size: 0.9rem; font-weight: 500;">${escapeHtml(message)}</span>
        `;

        container.appendChild(toastEl);

        setTimeout(() => {
            toastEl.style.opacity = '0';
            toastEl.style.transform = 'translateX(100%)';
            setTimeout(() => toastEl.remove(), 300);
        }, duration);
    };

    const openModal = (modalId) => {
        const modal = document.getElementById(modalId);
        if (modal) {
            modal.classList.add('open');
            document.body.style.overflow = 'hidden';
        }
    };

    const closeModal = (modalId) => {
        const modal = document.getElementById(modalId);
        if (modal) {
            modal.classList.remove('open');
            document.body.style.overflow = '';
        }
    };

    const escapeHtml = (str) => {
        if (!str) return '';
        return String(str)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
    };

    const getStatusBadge = (status) => {
        if (!status) return '';
        const s = status.toUpperCase();
        if (s === 'AVAILABLE' || s === 'APPROVED' || s === 'ACTIVE') {
            return `<span class="nm-badge nm-badge-success">${escapeHtml(s.replace('_', ' '))}</span>`;
        } else if (s === 'PENDING' || s === 'PENDING_REVIEW' || s === 'UNDER_REVIEW') {
            return `<span class="nm-badge nm-badge-warning">${escapeHtml(s.replace('_', ' '))}</span>`;
        } else if (s === 'REJECTED' || s === 'INACTIVE') {
            return `<span class="nm-badge nm-badge-danger">${escapeHtml(s.replace('_', ' '))}</span>`;
        } else {
            return `<span class="nm-badge nm-badge-info">${escapeHtml(s.replace('_', ' '))}</span>`;
        }
    };

    const formatAge = (months) => {
        if (months === null || months === undefined) return 'Unknown';
        if (months < 12) return `${months} mo${months === 1 ? '' : 's'}`;
        const yrs = Math.floor(months / 12);
        const rem = months % 12;
        if (rem === 0) return `${yrs} yr${yrs === 1 ? '' : 's'}`;
        return `${yrs} yr ${rem} mo`;
    };

    const initMobileMenu = () => {
        const toggleBtn = document.querySelector('.mobile-nav-toggle');
        const navLinks = document.querySelector('.nav-links');
        if (toggleBtn && navLinks) {
            toggleBtn.addEventListener('click', (e) => {
                e.stopPropagation();
                navLinks.classList.toggle('open');
            });
            navLinks.querySelectorAll('a').forEach(a => {
                a.addEventListener('click', () => {
                    navLinks.classList.remove('open');
                });
            });
            document.addEventListener('click', (e) => {
                if (!navLinks.contains(e.target) && !toggleBtn.contains(e.target)) {
                    navLinks.classList.remove('open');
                }
            });
        }
    };

    // Close modals on backdrop click or close button click
    document.addEventListener('click', (e) => {
        if (e.target.classList.contains('nm-modal-backdrop')) {
            e.target.classList.remove('open');
            document.body.style.overflow = '';
        }
        if (e.target.closest('.nm-modal-close')) {
            const backdrop = e.target.closest('.nm-modal-backdrop');
            if (backdrop) {
                backdrop.classList.remove('open');
                document.body.style.overflow = '';
            }
        }
    });

    document.addEventListener('DOMContentLoaded', () => {
        initMobileMenu();
    });

    return {
        toast,
        openModal,
        closeModal,
        escapeHtml,
        getStatusBadge,
        formatAge
    };
})();
