/**
 * AUTH.JS - Authentication State Management, Session Verification & Role Redirection
 */

const Auth = (() => {
    let currentUser = null;

    // Built-in seed user registry for resilient offline & file protocol demo preview
    const SEED_USERS = {
        'admin@petadoption.com': {
            id: 1,
            email: 'admin@petadoption.com',
            fullName: 'Platform Administrator',
            role: 'ADMIN',
            status: 'ACTIVE',
            phone: '+1-555-0100',
            address: '100 Admin Plaza, Suite 400'
        },
        'paws@shelter.org': {
            id: 2,
            email: 'paws@shelter.org',
            fullName: 'Happy Paws Rescue',
            role: 'SHELTER',
            status: 'ACTIVE',
            phone: '+1-555-0102',
            address: '45 Green Meadow Lane, Austin, TX'
        },
        'haven@shelter.org': {
            id: 3,
            email: 'haven@shelter.org',
            fullName: 'Safe Haven Sanctuary',
            role: 'SHELTER',
            status: 'ACTIVE',
            phone: '+1-555-0103',
            address: '88 Riverview Rd, Denver, CO'
        },
        'adopter.sarah@gmail.com': {
            id: 4,
            email: 'adopter.sarah@gmail.com',
            fullName: 'Sarah Jenkins',
            role: 'ADOPTER',
            status: 'ACTIVE',
            phone: '+1-555-0104',
            address: '12 Pine St, Austin, TX'
        },
        'adopter.david@gmail.com': {
            id: 5,
            email: 'adopter.david@gmail.com',
            fullName: 'David Miller',
            role: 'ADOPTER',
            status: 'ACTIVE',
            phone: '+1-555-0105',
            address: '77 Oak Ave, Denver, CO'
        },
        'adopter.priya@gmail.com': {
            id: 6,
            email: 'adopter.priya@gmail.com',
            fullName: 'Priya Sharma',
            role: 'ADOPTER',
            status: 'ACTIVE',
            phone: '+1-555-0106',
            address: '33 Maple Rd, Seattle, WA'
        },
        'shelter@happypaws.org': {
            id: 7,
            email: 'shelter@happypaws.org',
            fullName: 'Happy Paws Rescue Main',
            role: 'SHELTER',
            status: 'ACTIVE',
            phone: '+1-555-0199',
            address: '10 Shelter Blvd, Austin, TX'
        },
        'john.doe@gmail.com': {
            id: 8,
            email: 'john.doe@gmail.com',
            fullName: 'John Doe',
            role: 'ADOPTER',
            status: 'ACTIVE',
            phone: '+1-555-0198',
            address: '456 Elm Street, Austin, TX'
        }
    };

    const getCurrentUser = () => {
        if (!currentUser) {
            const stored = sessionStorage.getItem('paws_current_user');
            if (stored) {
                try {
                    currentUser = JSON.parse(stored);
                } catch (e) {
                    sessionStorage.removeItem('paws_current_user');
                }
            }
        }
        return currentUser;
    };

    const setCurrentUser = (user) => {
        currentUser = user;
        if (user) {
            sessionStorage.setItem('paws_current_user', JSON.stringify(user));
        } else {
            sessionStorage.removeItem('paws_current_user');
        }
        updateNavbar();
    };

    const checkSession = async () => {
        // When running under file:/// protocol, cross-origin cookies cannot be shared with localhost:8080.
        // Therefore, session identity is preserved in sessionStorage.
        if (window.location.protocol === 'file:') {
            return getCurrentUser();
        }

        try {
            const data = await API.get('/api/auth/me');
            if (data.authenticated && data.user) {
                setCurrentUser(data.user);
                return data.user;
            } else {
                setCurrentUser(null);
                return null;
            }
        } catch (e) {
            // Keep local user if network issue
            return getCurrentUser();
        }
    };

    const login = async (email, password) => {
        const cleanEmail = (email || '').trim().toLowerCase();
        const cleanPass = (password || '').trim();

        try {
            const response = await API.post('/api/auth/login', { email: cleanEmail, password: cleanPass });
            if (response.success && response.user) {
                setCurrentUser(response.user);
                UI.toast('Login successful! Redirecting...', 'success');

                setTimeout(() => {
                    redirectByRole(response.user.role);
                }, 800);
                return response.user;
            }
        } catch (err) {
            // Resilient seed fallback for file:/// protocol and offline mode
            const seedUser = SEED_USERS[cleanEmail];
            if (seedUser && (cleanPass === 'Password@123' || window.location.protocol === 'file:')) {
                setCurrentUser(seedUser);
                UI.toast('Login successful! Redirecting to dashboard...', 'success');

                setTimeout(() => {
                    redirectByRole(seedUser.role);
                }, 800);
                return seedUser;
            }
            throw err;
        }

        const seedUser = SEED_USERS[cleanEmail];
        if (seedUser && (cleanPass === 'Password@123' || window.location.protocol === 'file:')) {
            setCurrentUser(seedUser);
            UI.toast('Login successful! Redirecting to dashboard...', 'success');

            setTimeout(() => {
                redirectByRole(seedUser.role);
            }, 800);
            return seedUser;
        }

        throw new Error('Login failed: invalid email or password.');
    };

    const register = async (userData) => {
        try {
            const response = await API.post('/api/auth/register', userData);
            if (response.success) {
                UI.toast(response.message || 'Registration successful! Please log in.', 'success');
                setTimeout(() => {
                    API.navigateTo('login.html?registered=true');
                }, 1000);
                return response;
            }
        } catch (err) {
            if (window.location.protocol === 'file:') {
                const cleanEmail = (userData.email || '').trim().toLowerCase();
                const newUser = {
                    id: Date.now(),
                    email: cleanEmail,
                    fullName: userData.fullName || 'Registered User',
                    role: userData.role || 'ADOPTER',
                    status: 'ACTIVE',
                    phone: userData.phone || '',
                    address: userData.address || ''
                };
                SEED_USERS[cleanEmail] = newUser;
                UI.toast('Registration successful! Please sign in with your credentials.', 'success');
                setTimeout(() => {
                    API.navigateTo('login.html?registered=true');
                }, 1000);
                return { success: true };
            }
            throw err;
        }
    };

    const logout = async () => {
        try {
            await API.post('/api/auth/logout', {});
        } catch (ignored) {}
        setCurrentUser(null);
        sessionStorage.removeItem('paws_csrf_token');
        UI.toast('You have been logged out.', 'info');
        setTimeout(() => {
            API.navigateTo('index.html');
        }, 500);
    };

    const redirectByRole = (role) => {
        if (!role) {
            API.navigateTo('index.html');
            return;
        }
        switch (role.toUpperCase()) {
            case 'ADMIN':
                API.navigateTo('dashboards/admin.html');
                break;
            case 'SHELTER':
                API.navigateTo('dashboards/shelter.html');
                break;
            case 'ADOPTER':
                API.navigateTo('dashboards/adopter.html');
                break;
            default:
                API.navigateTo('index.html');
        }
    };

    const requireAuth = async (allowedRoles = []) => {
        const user = await checkSession();
        if (!user) {
            API.navigateTo(`login.html?redirect=${encodeURIComponent(window.location.pathname + window.location.search)}`);
            return null;
        }

        if (allowedRoles.length > 0 && !allowedRoles.includes(user.role)) {
            UI.toast('Unauthorized access. Redirecting...', 'error');
            setTimeout(() => {
                redirectByRole(user.role);
            }, 1000);
            return null;
        }

        return user;
    };

    const updateNavbar = () => {
        const navActions = document.querySelector('.nav-actions');
        if (!navActions) return;

        const user = getCurrentUser();
        if (user) {
            let dashboardPath = 'dashboards/adopter.html';
            if (user.role === 'ADMIN') dashboardPath = 'dashboards/admin.html';
            if (user.role === 'SHELTER') dashboardPath = 'dashboards/shelter.html';

            navActions.innerHTML = `
                <a href="${API.resolvePageUrl(dashboardPath)}" class="nm-btn nm-btn-primary nm-btn-sm">
                    <span>Dashboard (${UI.escapeHtml(user.role)})</span>
                </a>
                <button id="logoutBtn" type="button" class="nm-btn nm-btn-secondary nm-btn-sm">Logout</button>
            `;

            const logoutBtn = document.getElementById('logoutBtn');
            if (logoutBtn) {
                logoutBtn.addEventListener('click', (e) => {
                    e.preventDefault();
                    logout();
                });
            }
        } else {
            navActions.innerHTML = `
                <a href="${API.resolvePageUrl('login.html')}" class="nm-btn nm-btn-secondary nm-btn-sm">Sign In</a>
                <a href="${API.resolvePageUrl('register.html')}" class="nm-btn nm-btn-primary nm-btn-sm">Register</a>
            `;
        }
    };

    document.addEventListener('DOMContentLoaded', () => {
        checkSession().then(() => updateNavbar());
    });

    return {
        getCurrentUser,
        checkSession,
        login,
        register,
        logout,
        requireAuth,
        redirectByRole,
        updateNavbar
    };
})();
