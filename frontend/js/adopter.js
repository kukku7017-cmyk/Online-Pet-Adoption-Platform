/**
 * ADOPTER.JS - Adopter Portal: Application Tracker, Chat, and Profile Management
 */

const Adopter = (() => {
    let currentConversationId = null;

    const init = async () => {
        const user = await Auth.requireAuth(['ADOPTER']);
        if (!user) return;

        document.getElementById('adopterName').textContent = user.fullName || user.email;

        loadApplications();
        loadProfile(user);
        loadConversations();
        setupNavigation();
    };

    const setupNavigation = () => {
        const links = document.querySelectorAll('.sidebar-link[data-tab]');
        links.forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                links.forEach(l => l.classList.remove('active'));
                link.classList.add('active');

                const tab = link.dataset.tab;
                document.querySelectorAll('.tab-content').forEach(c => c.style.display = 'none');
                const target = document.getElementById(`tab-${tab}`);
                if (target) target.style.display = 'block';
                if (window.location.hash !== '#' + tab) {
                    if (window.history && window.history.replaceState) {
                        window.history.replaceState(null, null, '#' + tab);
                    }
                }
            });
        });

        if (window.location.hash) {
            const hashTab = window.location.hash.replace('#', '');
            const targetLink = document.querySelector(`.sidebar-link[data-tab="${hashTab}"]`);
            if (targetLink) targetLink.click();
        }
    };

    const loadApplications = async () => {
        const tbody = document.getElementById('adopterAppsTableBody');
        if (!tbody) return;

        let demoApps = [];
        if (window.Applications && typeof Applications.getDemoApplications === 'function') {
            demoApps = Applications.getDemoApplications();
        } else {
            try {
                demoApps = JSON.parse(sessionStorage.getItem('paws_demo_applications') || localStorage.getItem('paws_demo_applications') || '[]');
            } catch (ignored) {}
        }

        try {
            tbody.innerHTML = '<tr><td colspan="6" style="text-align: center;">Loading your adoption applications...</td></tr>';
            const data = await API.get('/api/applications/my');
            if (data.success && data.applications && data.applications.length > 0) {
                const serverIds = new Set(data.applications.map(a => a.id));
                const uniqueDemo = demoApps.filter(d => !serverIds.has(d.id));
                renderAdopterApps(tbody, [...uniqueDemo, ...data.applications]);
                return;
            } else if (data.success && demoApps.length > 0) {
                renderAdopterApps(tbody, demoApps);
                return;
            } else if (data.success) {
                tbody.innerHTML = `
                    <tr>
                        <td colspan="6" style="text-align: center; padding: 2.5rem;">
                            <div style="font-size: 2rem; margin-bottom: 0.5rem;">🐾</div>
                            <h4 style="margin-bottom: 0.5rem;">No Applications Submitted Yet</h4>
                            <p style="color: var(--text-muted); margin-bottom: 1rem;">Browse available pets and submit your first adoption request!</p>
                            <a href="${API.resolvePageUrl('pets.html')}" class="nm-btn nm-btn-primary nm-btn-sm">Browse Available Pets</a>
                        </td>
                    </tr>
                `;
                return;
            }
        } catch (e) {
            console.warn('Live adopter applications fetch unavailable, using seed applications');
        }

        const seedMyApps = [
            {
                id: 1,
                petId: 1,
                petName: 'Bella',
                petImageUrl: 'https://images.unsplash.com/photo-1552053831-71594a27632d?auto=format&fit=crop&w=150&q=80',
                submittedAt: '2026-10-09',
                status: 'APPROVED',
                reviewNotes: 'Fantastic applicant. Spacious yard and extensive Golden Retriever experience.'
            }
        ];
        const seenIds = new Set();
        const combined = [];
        for (const app of [...demoApps, ...seedMyApps]) {
            if (!seenIds.has(app.id)) {
                seenIds.add(app.id);
                combined.push(app);
            }
        }
        renderAdopterApps(tbody, combined);
    };

    const renderAdopterApps = (tbody, apps) => {
        tbody.innerHTML = apps.map(app => `
            <tr>
                <td>#${app.id}</td>
                <td>
                    <div style="display: flex; align-items: center; gap: 0.75rem;">
                        <img src="${UI.escapeHtml(app.petImageUrl || 'https://images.unsplash.com/photo-1543466835-00a7907e9de1?auto=format&fit=crop&w=150&q=80')}"
                             alt="${UI.escapeHtml(app.petName)}" style="width: 44px; height: 44px; object-fit: cover; border-radius: var(--radius-sm);">
                        <strong>${UI.escapeHtml(app.petName)}</strong>
                    </div>
                </td>
                <td>${UI.escapeHtml(app.submittedAt ? app.submittedAt.substring(0, 10) : '')}</td>
                <td>${UI.getStatusBadge(app.status)}</td>
                <td>
                    ${app.reviewNotes 
                        ? `<span style="font-size: 0.85rem; color: var(--text-secondary);">${UI.escapeHtml(app.reviewNotes)}</span>`
                        : '<span style="color: var(--text-muted); font-size: 0.85rem;">Pending review</span>'
                    }
                </td>
                <td>
                    <a href="${API.resolvePageUrl('pet-details.html?id=' + app.petId)}" class="nm-btn nm-btn-secondary nm-btn-sm">View Pet</a>
                </td>
            </tr>
        `).join('');
    };

    const loadProfile = (user) => {
        const nameInput = document.getElementById('profileFullName');
        const emailInput = document.getElementById('profileEmail');
        const phoneInput = document.getElementById('profilePhone');
        const addressInput = document.getElementById('profileAddress');

        if (nameInput) nameInput.value = user.fullName || '';
        if (emailInput) emailInput.value = user.email || '';
        if (phoneInput) phoneInput.value = user.phone || '';
        if (addressInput) addressInput.value = user.address || '';
    };

    const saveProfile = async (e) => {
        e.preventDefault();
        const fullName = document.getElementById('profileFullName').value;
        const phone = document.getElementById('profilePhone').value;
        const address = document.getElementById('profileAddress').value;

        try {
            const data = await API.post('/api/auth/profile', { fullName, phone, address });
            if (data.success) {
                UI.toast('Profile details updated successfully!', 'success');
                document.getElementById('adopterName').textContent = fullName;
            }
        } catch (err) {
            UI.toast(err.message || 'Error updating profile.', 'error');
        }
    };

    const loadConversations = async () => {
        const listEl = document.getElementById('adopterConversationsList');
        if (!listEl) return;

        try {
            const data = await API.get('/api/messages/conversations');
            if (data.success && data.conversations && data.conversations.length > 0) {
                listEl.innerHTML = data.conversations.map(c => `
                    <div class="nm-card-sm conversation-item ${currentConversationId === c.id ? 'active' : ''}"
                         onclick="Adopter.openThread(${c.id}, '${UI.escapeHtml(c.shelterName)}', '${UI.escapeHtml(c.petName)}')"
                         style="cursor: pointer; margin-bottom: 0.75rem;">
                        <strong>${UI.escapeHtml(c.shelterName)}</strong>
                        <div style="font-size: 0.8rem; color: var(--primary);">Pet: ${UI.escapeHtml(c.petName)}</div>
                    </div>
                `).join('');
            } else {
                listEl.innerHTML = '<div style="color: var(--text-muted); font-size: 0.9rem; padding: 1rem;">No conversation threads yet. Conversations are created automatically upon submitting an adoption application.</div>';
            }
        } catch (e) {
            console.error('Error loading conversations:', e);
        }
    };

    const openThread = async (convId, shelterName, petName) => {
        currentConversationId = convId;
        document.getElementById('adopterChatRecipient').textContent = `${shelterName} (Regarding ${petName})`;
        loadMessages();
    };

    const loadMessages = async () => {
        if (!currentConversationId) return;
        const messagesEl = document.getElementById('adopterMessagesContainer');
        if (!messagesEl) return;

        try {
            const data = await API.get(`/api/messages/thread?conversationId=${currentConversationId}`);
            if (data.success && data.messages) {
                const currentUser = Auth.getCurrentUser();
                messagesEl.innerHTML = data.messages.map(m => {
                    const isMe = m.senderId === currentUser.id;
                    return `
                        <div style="display: flex; justify-content: ${isMe ? 'flex-end' : 'flex-start'}; margin-bottom: 0.75rem;">
                            <div style="max-width: 70%; padding: 0.75rem 1rem; border-radius: var(--radius-md); background: ${isMe ? 'var(--teal-light)' : 'var(--bg-surface)'}; box-shadow: var(--nm-flat-sm);">
                                <div style="font-size: 0.75rem; font-weight: 700; color: ${isMe ? 'var(--primary)' : 'var(--text-muted)'}; margin-bottom: 0.25rem;">
                                    ${UI.escapeHtml(m.senderName)}
                                </div>
                                <div style="font-size: 0.9rem;">${UI.escapeHtml(m.content)}</div>
                            </div>
                        </div>
                    `;
                }).join('');
                messagesEl.scrollTop = messagesEl.scrollHeight;
            }
        } catch (e) {
            console.error('Error loading messages:', e);
        }
    };

    const sendMessage = async () => {
        if (!currentConversationId) {
            UI.toast('Please select an active conversation first.', 'warning');
            return;
        }

        const input = document.getElementById('adopterChatInput');
        const content = input.value;
        if (!content || !content.trim()) return;

        try {
            const data = await API.post('/api/messages/send', {
                conversationId: currentConversationId,
                content: content.trim()
            });
            if (data.success) {
                input.value = '';
                loadMessages();
            }
        } catch (e) {
            UI.toast(e.message || 'Failed to send message.', 'error');
        }
    };

    return {
        init,
        saveProfile,
        openThread,
        sendMessage
    };
})();
