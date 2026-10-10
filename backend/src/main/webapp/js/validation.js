/**
 * VALIDATION.JS - Client-side Form Validation & Sanitization Helpers
 */

const Validation = (() => {
    const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    const PHONE_REGEX = /^[+]?[(]?[0-9]{3}[)]?[-\s.]?[0-9]{3}[-\s.]?[0-9]{4,6}$/;

    const isValidEmail = (email) => {
        return email && EMAIL_REGEX.test(email.trim());
    };

    const isValidPhone = (phone) => {
        if (!phone || phone.trim() === '') return true; // optional
        return PHONE_REGEX.test(phone.trim().replace(/[\s()-]/g, ''));
    };

    const isNonEmpty = (str) => {
        return str !== null && str !== undefined && str.trim().length > 0;
    };

    const hasMinLength = (str, min) => {
        return str && str.trim().length >= min;
    };

    const validateForm = (formElement) => {
        let isValid = true;
        const inputs = formElement.querySelectorAll('[required]');

        inputs.forEach(input => {
            const val = input.value;
            const parent = input.closest('.form-group');
            let errorEl = parent ? parent.querySelector('.form-error') : null;

            if (!isNonEmpty(val)) {
                isValid = false;
                if (errorEl) {
                    errorEl.textContent = 'This field is required.';
                    errorEl.style.display = 'block';
                }
            } else if (input.type === 'email' && !isValidEmail(val)) {
                isValid = false;
                if (errorEl) {
                    errorEl.textContent = 'Please provide a valid email address.';
                    errorEl.style.display = 'block';
                }
            } else if (input.type === 'password' && !hasMinLength(val, 6)) {
                isValid = false;
                if (errorEl) {
                    errorEl.textContent = 'Password must be at least 6 characters.';
                    errorEl.style.display = 'block';
                }
            } else {
                if (errorEl) errorEl.style.display = 'none';
            }
        });

        return isValid;
    };

    return {
        isValidEmail,
        isValidPhone,
        isNonEmpty,
        hasMinLength,
        validateForm
    };
})();
