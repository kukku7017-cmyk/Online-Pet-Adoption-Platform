/**
 * API.JS - Unified Fetch API Client with Dynamic Context Resolution, CSRF Interception & Navigation Helper
 * Online Pet Adoption Platform
 */

const API = (() => {
    let csrfToken = null;

    /**
     * Dynamically detects the deployment context path.
     * Examples:
     *   - Embedded Tomcat at root: ""
     *   - Standard Tomcat WAR: "/pet-adoption-platform"
     *   - Dev servers / File protocol: fallback target "http://localhost:8085"
     */
    const getContextPath = () => {
        if (typeof window.API_BASE_URL === 'string' && window.API_BASE_URL.trim() !== '') {
            return window.API_BASE_URL.trim();
        }

        // Handle direct file opening (file:///...)
        if (window.location.protocol === 'file:') {
            return 'http://localhost:8080';
        }

        // Handle independent frontend dev servers (e.g., Live Server port 5500, 3000, 5173)
        const port = window.location.port;
        if (port && ['5500', '3000', '5173', '8000'].includes(port)) {
            return 'http://localhost:8080';
        }

        const path = window.location.pathname;

        // Check if inside /dashboards/ directory
        const dashIdx = path.indexOf('/dashboards/');
        if (dashIdx !== -1) {
            return path.substring(0, dashIdx);
        }

        // Check if inside a subfolder (e.g. /pet-adoption-platform/index.html)
        const lastSlash = path.lastIndexOf('/');
        if (lastSlash > 0) {
            return path.substring(0, lastSlash);
        }

        return '';
    };

    /**
     * Resolves a backend API endpoint URL using the detected context path.
     * e.g., '/api/pets/public' -> '/pet-adoption-platform/api/pets/public'
     */
    const resolveUrl = (endpoint) => {
        if (!endpoint) return '';
        if (endpoint.startsWith('http://') || endpoint.startsWith('https://')) {
            return endpoint;
        }

        const base = getContextPath();
        const clean = endpoint.startsWith('/') ? endpoint : `/${endpoint}`;

        if (base.endsWith('/') && clean.startsWith('/')) {
            return base + clean.substring(1);
        }
        return `${base}${clean}`;
    };

    /**
     * Resolves a frontend page URL for safe navigation.
     * Works across root deployments, WAR context paths, and file:/// protocols.
     */
    const resolvePageUrl = (pagePath) => {
        if (!pagePath) return '';
        if (pagePath.startsWith('http://') || pagePath.startsWith('https://') || pagePath.startsWith('javascript:')) {
            return pagePath;
        }

        const clean = pagePath.startsWith('/') ? pagePath.substring(1) : pagePath;

        if (window.location.protocol === 'file:') {
            const inDashboards = window.location.pathname.includes('/dashboards/');
            if (inDashboards) {
                if (clean.startsWith('dashboards/')) {
                    return clean.substring('dashboards/'.length);
                }
                return `../${clean}`;
            } else {
                return clean;
            }
        }

        const base = getContextPath();
        // If base is a full URL (cross-origin backend), page paths remain relative to origin
        if (base.startsWith('http://') || base.startsWith('https://')) {
            const inDashboards = window.location.pathname.includes('/dashboards/');
            if (inDashboards) {
                if (clean.startsWith('dashboards/')) return clean.substring('dashboards/'.length);
                return `../${clean}`;
            }
            return clean;
        }

        return base ? `${base}/${clean}` : `/${clean}`;
    };

    /**
     * Safe programmatic page navigation
     */
    const navigateTo = (pagePath) => {
        window.location.href = resolvePageUrl(pagePath);
    };

    const setCsrfToken = (token) => {
        if (token) {
            csrfToken = token;
            try {
                sessionStorage.setItem('paws_csrf_token', token);
            } catch (ignored) {}
        }
    };

    const getCsrfToken = () => {
        if (!csrfToken) {
            try {
                csrfToken = sessionStorage.getItem('paws_csrf_token');
            } catch (ignored) {}
        }
        return csrfToken;
    };

    /**
     * Centralized HTTP Request Handler
     */
    const request = async (url, options = {}) => {
        const fullUrl = resolveUrl(url);

        const headers = {
            'Accept': 'application/json',
            ...(options.headers || {})
        };

        const method = (options.method || 'GET').toUpperCase();
        if (['POST', 'PUT', 'DELETE', 'PATCH'].includes(method)) {
            const token = getCsrfToken();
            if (token) {
                headers['X-CSRF-Token'] = token;
            }
            if (!(options.body instanceof FormData) && !headers['Content-Type']) {
                headers['Content-Type'] = 'application/json';
            }
        }

        try {
            const fetchOptions = {
                ...options,
                headers
            };
            if (window.location.protocol !== 'file:') {
                fetchOptions.credentials = 'include'; // Ensures session cookie (JSESSIONID) is passed on HTTP origins
            }
            const response = await fetch(fullUrl, fetchOptions);

            // Handle 401 Unauthorized
            if (response.status === 401) {
                // When browsing via file:// protocol, cross-origin cookies cannot be persisted or sent to localhost.
                // Therefore, do NOT clear sessionStorage or force redirect on file:// to preserve the active session.
                if (window.location.protocol !== 'file:') {
                    try {
                        sessionStorage.removeItem('paws_current_user');
                        sessionStorage.removeItem('paws_csrf_token');
                    } catch (ignored) {}

                    const currentPath = window.location.pathname;
                    if (!currentPath.includes('login.html') &&
                        !currentPath.includes('register.html') &&
                        !currentPath.endsWith('index.html') &&
                        !currentPath.endsWith('pets.html') &&
                        !currentPath.endsWith('about.html') &&
                        !currentPath.endsWith('contact.html')) {
                        navigateTo('login.html?expired=true');
                    }
                }
            }

            const contentType = response.headers.get('content-type');
            let data = {};
            if (contentType && contentType.includes('application/json')) {
                data = await response.json();
            } else {
                const text = await response.text();
                data = { message: text };
            }

            if (!response.ok) {
                const errorMsg = data.message || `Request failed with status ${response.status}`;
                throw new Error(errorMsg);
            }

            if (data.csrfToken) {
                setCsrfToken(data.csrfToken);
            }

            return data;
        } catch (error) {
            console.error(`API Error [${method} ${fullUrl}]:`, error.message);
            throw error;
        }
    };

    return {
        get: (url, opts) => request(url, { ...opts, method: 'GET' }),
        post: (url, body, opts) => request(url, {
            ...opts,
            method: 'POST',
            body: body instanceof FormData ? body : JSON.stringify(body)
        }),
        upload: (url, formData, opts) => request(url, {
            ...opts,
            method: 'POST',
            body: formData
        }),
        setCsrfToken,
        getCsrfToken,
        getContextPath,
        resolveUrl,
        resolvePageUrl,
        navigateTo
    };
})();
