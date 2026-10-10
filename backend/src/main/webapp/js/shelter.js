/**
 * SHELTER.JS - Shelter Portal: Pet Inventory, Application Reviews, and Adopter Chat
 */

const Shelter = (() => {
    let currentConversationId = null;

    const init = async () => {
        const user = await Auth.requireAuth(['SHELTER']);
        if (!user) return;

        document.getElementById('shelterName').textContent = user.fullName || user.email;

        loadStats();
        loadPets();
        loadApplications();
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

    const SEED_APPLICATIONS = [
        {
            id: 1,
            petId: 1,
            petName: 'Bella',
            adopterName: 'Sarah Jenkins',
            adopterEmail: 'adopter.sarah@gmail.com',
            adopterPhone: '+1-555-0104',
            homeType: 'Single Family Home',
            livingArrangement: 'Single Family Home with fenced yard',
            householdMembers: 2,
            petExperience: '5 years with dogs',
            adoptionReason: 'Companion for family',
            careCommitment: '2+ hours daily',
            status: 'APPROVED',
            reviewNotes: 'Approved applicant. Spacious yard and extensive Golden Retriever experience.'
        },
        {
            id: 2,
            petId: 2,
            petName: 'Milo',
            adopterName: 'David Miller',
            adopterEmail: 'adopter.david@gmail.com',
            adopterPhone: '+1-555-0105',
            homeType: 'Apartment',
            livingArrangement: 'Apartment (landlord approved)',
            householdMembers: 1,
            petExperience: 'First time dog parent',
            adoptionReason: 'Work from home companion',
            careCommitment: '1 hour walk daily',
            status: 'UNDER_REVIEW',
            reviewNotes: 'Application looks promising. Verifying pet deposit approval with landlord.'
        },
        {
            id: 3,
            petId: 3,
            petName: 'Luna',
            adopterName: 'Priya Sharma',
            adopterEmail: 'adopter.priya@gmail.com',
            adopterPhone: '+1-555-0106',
            homeType: 'Townhouse',
            livingArrangement: 'Townhouse, indoor cat proofed',
            householdMembers: 3,
            petExperience: 'Previous cat owner',
            adoptionReason: 'Loving family pet',
            careCommitment: 'Daily indoor play',
            status: 'PENDING',
            reviewNotes: ''
        }
    ];

    const getStoredApplications = () => {
        let stored = [];
        try {
            stored = JSON.parse(sessionStorage.getItem('paws_demo_applications') || localStorage.getItem('paws_demo_applications') || 'null');
        } catch (e) {}

        if (!stored || !Array.isArray(stored) || stored.length === 0) {
            stored = JSON.parse(JSON.stringify(SEED_APPLICATIONS));
            try {
                sessionStorage.setItem('paws_demo_applications', JSON.stringify(stored));
                localStorage.setItem('paws_demo_applications', JSON.stringify(stored));
            } catch (e) {}
            return stored;
        }

        const existingIds = new Set(stored.map(a => a.id));
        let updated = false;
        for (const seed of SEED_APPLICATIONS) {
            if (!existingIds.has(seed.id)) {
                stored.push(JSON.parse(JSON.stringify(seed)));
                updated = true;
            }
        }
        if (updated) {
            try {
                sessionStorage.setItem('paws_demo_applications', JSON.stringify(stored));
                localStorage.setItem('paws_demo_applications', JSON.stringify(stored));
            } catch (e) {}
        }
        return stored;
    };

    const getStoredPets = () => {
        let stored = [];
        try {
            stored = JSON.parse(sessionStorage.getItem('paws_demo_pets') || localStorage.getItem('paws_demo_pets') || 'null');
        } catch (e) {}

        const seedShelterPets = [
            { id: 1, name: 'Bella', species: 'Dog', breed: 'Golden Retriever', ageMonths: 24, status: 'AVAILABLE', rejectionReason: null, imageUrl: 'https://images.unsplash.com/photo-1552053831-71594a27632d?auto=format&fit=crop&w=150&q=80' },
            { id: 2, name: 'Milo', species: 'Dog', breed: 'French Bulldog', ageMonths: 14, status: 'AVAILABLE', rejectionReason: null, imageUrl: 'https://images.unsplash.com/photo-1583511655857-d19b40a7a54e?auto=format&fit=crop&w=150&q=80' },
            { id: 3, name: 'Luna', species: 'Cat', breed: 'Siamese Mix', ageMonths: 18, status: 'AVAILABLE', rejectionReason: null, imageUrl: 'https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?auto=format&fit=crop&w=150&q=80' },
            { id: 7, name: 'Rio', species: 'Bird', breed: 'Cockatiel', ageMonths: 12, status: 'AVAILABLE', rejectionReason: null, imageUrl: 'https://images.unsplash.com/photo-1522858547550-345670f99e4b?auto=format&fit=crop&w=150&q=80' },
            { id: 8, name: 'Rocky', species: 'Dog', breed: 'German Shepherd Mix', ageMonths: 30, status: 'AVAILABLE', rejectionReason: null, imageUrl: 'https://images.unsplash.com/photo-1589941013453-ec89f33b5455?auto=format&fit=crop&w=150&q=80' },
            { id: 10, name: 'Shadow', species: 'Cat', breed: 'Domestic Shorthair', ageMonths: 20, status: 'PENDING_REVIEW', rejectionReason: null, imageUrl: 'https://images.unsplash.com/photo-1518791841217-8f162f1e1131?auto=format&fit=crop&w=150&q=80' }
        ];

        if (!stored || !Array.isArray(stored) || stored.length === 0) {
            stored = JSON.parse(JSON.stringify(seedShelterPets));
            try {
                sessionStorage.setItem('paws_demo_pets', JSON.stringify(stored));
                localStorage.setItem('paws_demo_pets', JSON.stringify(stored));
            } catch (e) {}
            return stored;
        }

        const existingIds = new Set(stored.map(p => p.id));
        let updated = false;
        for (const seed of seedShelterPets) {
            if (!existingIds.has(seed.id)) {
                stored.push(JSON.parse(JSON.stringify(seed)));
                updated = true;
            }
        }
        if (updated) {
            try {
                sessionStorage.setItem('paws_demo_pets', JSON.stringify(stored));
                localStorage.setItem('paws_demo_pets', JSON.stringify(stored));
            } catch (e) {}
        }
        return stored;
    };

    const updateDemoPetStatus = (petId, newStatus) => {
        if (!petId) return;
        const pets = getStoredPets();
        const pet = pets.find(p => p.id === parseInt(petId, 10));
        if (pet) {
            pet.status = newStatus;
            try {
                localStorage.setItem('paws_demo_pets', JSON.stringify(pets));
                sessionStorage.setItem('paws_demo_pets', JSON.stringify(pets));
            } catch (e) {}
        }
    };

    const persistApplicationStatus = async (appId, newStatus, reviewNotes) => {
        const idNum = parseInt(appId, 10);

        if (window.location.protocol !== 'file:') {
            try {
                await API.post('/api/applications/review', {
                    applicationId: idNum,
                    status: newStatus,
                    reviewNotes: reviewNotes || ''
                });
            } catch (e) {
                console.warn('Backend review endpoint unavailable, persisting locally:', e.message);
            }
        }

        // Always update local/demo storage
        const apps = getStoredApplications();
        const app = apps.find(a => a.id === idNum);
        if (app) {
            app.status = newStatus;
            if (reviewNotes !== undefined && reviewNotes !== null) {
                app.reviewNotes = reviewNotes;
            }
            if (newStatus === 'APPROVED' && app.petId) {
                for (const other of apps) {
                    if (other.id !== app.id && other.petId === app.petId && (other.status === 'PENDING' || other.status === 'UNDER_REVIEW')) {
                        other.status = 'REJECTED';
                        other.reviewNotes = 'Pet has been adopted by another applicant.';
                    }
                }
                updateDemoPetStatus(app.petId, 'ADOPTED');
            }
            try {
                sessionStorage.setItem('paws_demo_applications', JSON.stringify(apps));
                localStorage.setItem('paws_demo_applications', JSON.stringify(apps));
            } catch (e) {}
        }
        return true;
    };

    const loadStats = async () => {
        try {
            const data = await API.get('/api/pets/my-listings');
            const appsData = await API.get('/api/applications/shelter');

            if (data.success && data.pets) {
                const total = data.pets.length;
                const available = data.pets.filter(p => p.status === 'AVAILABLE').length;
                const pending = data.pets.filter(p => p.status === 'PENDING_REVIEW').length;
                const adopted = data.pets.filter(p => p.status === 'ADOPTED').length;

                document.getElementById('statTotalPets').textContent = total;
                document.getElementById('statAvailablePets').textContent = available;
                document.getElementById('statPendingPets').textContent = pending;
                document.getElementById('statAdoptedPets').textContent = adopted;
            }

            if (appsData.success && appsData.applications) {
                const pendingApps = appsData.applications.filter(a => a.status === 'PENDING').length;
                document.getElementById('statPendingApps').textContent = pendingApps;
                return;
            }
        } catch (e) {
            console.warn('Live shelter stats unavailable, using stored stats');
        }

        const el = (id, val) => { const node = document.getElementById(id); if (node) node.textContent = val; };
        const currentApps = getStoredApplications();
        const pendingCount = currentApps.filter(a => a.status === 'PENDING').length;

        const currentPets = getStoredPets();
        const totalPets = currentPets.length;
        const availablePets = currentPets.filter(p => p.status === 'AVAILABLE').length;
        const pendingPets = currentPets.filter(p => p.status === 'PENDING_REVIEW').length;
        const adoptedPets = currentPets.filter(p => p.status === 'ADOPTED').length;

        el('statTotalPets', String(totalPets));
        el('statAvailablePets', String(availablePets));
        el('statPendingPets', String(pendingPets));
        el('statAdoptedPets', String(adoptedPets));
        el('statPendingApps', String(pendingCount));
    };

    const loadPets = async () => {
        const tbody = document.getElementById('shelterPetsTableBody');
        if (!tbody) return;

        try {
            tbody.innerHTML = '<tr><td colspan="7" style="text-align: center;">Loading shelter listings...</td></tr>';
            const data = await API.get('/api/pets/my-listings');
            if (data.success && data.pets && data.pets.length > 0) {
                renderShelterPets(tbody, data.pets);
                return;
            } else if (data.success) {
                tbody.innerHTML = '<tr><td colspan="7" style="text-align: center; color: var(--text-muted); padding: 2rem;">No pets listed yet. Click "+ Add New Pet" to list your first rescue!</td></tr>';
                return;
            }
        } catch (e) {
            console.warn('Live shelter pets fetch unavailable, using stored listings');
        }

        renderShelterPets(tbody, getStoredPets());
    };

    const renderShelterPets = (tbody, pets) => {
        tbody.innerHTML = pets.map(pet => `
            <tr>
                <td>
                    <img src="${UI.escapeHtml(pet.imageUrl || 'https://images.unsplash.com/photo-1543466835-00a7907e9de1?auto=format&fit=crop&w=150&q=80')}"
                         alt="${UI.escapeHtml(pet.name)}" style="width: 50px; height: 50px; object-fit: cover; border-radius: var(--radius-sm);">
                </td>
                <td><strong>${UI.escapeHtml(pet.name)}</strong></td>
                <td>${UI.escapeHtml(pet.species)} (${UI.escapeHtml(pet.breed)})</td>
                <td>${UI.formatAge(pet.ageMonths)}</td>
                <td>${UI.getStatusBadge(pet.status)}</td>
                <td>
                    ${pet.rejectionReason 
                        ? `<span style="font-size: 0.8rem; color: var(--danger);">Reason: ${UI.escapeHtml(pet.rejectionReason)}</span>` 
                        : '<span style="color: var(--text-muted);">-</span>'
                    }
                </td>
                <td>
                    <button class="nm-btn nm-btn-secondary nm-btn-sm" onclick="Shelter.openEditModal(${pet.id})">Edit</button>
                </td>
            </tr>
        `).join('');
    };

    const openAddModal = () => {
        document.getElementById('petForm').reset();
        document.getElementById('petFormId').value = '';
        document.getElementById('petModalTitle').textContent = 'Add New Rescue Pet';
        UI.openModal('petModal');
    };

    const openEditModal = async (petId) => {
        try {
            const data = await API.get(`/api/pets/detail?id=${petId}`);
            if (data.success && data.pet) {
                const pet = data.pet;
                document.getElementById('petFormId').value = pet.id;
                document.getElementById('petFormName').value = pet.name;
                document.getElementById('petFormSpecies').value = pet.species;
                document.getElementById('petFormBreed').value = pet.breed;
                document.getElementById('petFormAge').value = pet.ageMonths;
                document.getElementById('petFormGender').value = pet.gender;
                document.getElementById('petFormSize').value = pet.size;
                document.getElementById('petFormLocation').value = pet.location;
                document.getElementById('petFormImage').value = pet.imageUrl || '';
                document.getElementById('petFormHealth').value = pet.healthStatus || '';
                document.getElementById('petFormVaccination').value = pet.vaccinationStatus || '';
                document.getElementById('petFormSpayed').checked = pet.spayedNeutered;
                document.getElementById('petFormDesc').value = pet.description;

                document.getElementById('petModalTitle').textContent = `Edit Pet Listing: ${pet.name}`;
                UI.openModal('petModal');
            }
        } catch (e) {
            UI.toast('Failed to load pet details for editing.', 'error');
        }
    };

    const savePet = async (formElement) => {
        const petId = document.getElementById('petFormId').value;
        const isEdit = petId && petId.trim() !== '';

        const payload = {
            id: isEdit ? parseInt(petId, 10) : null,
            name: document.getElementById('petFormName').value,
            species: document.getElementById('petFormSpecies').value,
            breed: document.getElementById('petFormBreed').value,
            ageMonths: parseInt(document.getElementById('petFormAge').value || '0', 10),
            gender: document.getElementById('petFormGender').value,
            size: document.getElementById('petFormSize').value,
            location: document.getElementById('petFormLocation').value,
            imageUrl: document.getElementById('petFormImage').value,
            healthStatus: document.getElementById('petFormHealth').value,
            vaccinationStatus: document.getElementById('petFormVaccination').value,
            spayedNeutered: document.getElementById('petFormSpayed').checked,
            description: document.getElementById('petFormDesc').value
        };

        try {
            const endpoint = isEdit ? '/api/pets/update' : '/api/pets/create';
            const data = await API.post(endpoint, payload);
            if (data.success) {
                UI.toast(data.message || 'Pet saved successfully!', 'success');
                UI.closeModal('petModal');
                loadPets();
                loadStats();
            }
        } catch (e) {
            UI.toast(e.message || 'Error saving pet.', 'error');
        }
    };

    const loadApplications = async () => {
        const tbody = document.getElementById('shelterAppsTableBody');
        if (!tbody) return;

        const demoApps = getStoredApplications();

        try {
            tbody.innerHTML = '<tr><td colspan="6" style="text-align: center;">Loading received applications...</td></tr>';
            const data = await API.get('/api/applications/shelter');
            if (data.success && data.applications && data.applications.length > 0) {
                const serverIds = new Set(data.applications.map(a => a.id));
                const uniqueDemo = demoApps.filter(d => !serverIds.has(d.id));
                renderShelterApps(tbody, [...uniqueDemo, ...data.applications]);
                return;
            } else if (data.success && demoApps.length > 0) {
                renderShelterApps(tbody, demoApps);
                return;
            } else if (data.success) {
                tbody.innerHTML = '<tr><td colspan="6" style="text-align: center; color: var(--text-muted); padding: 2rem;">No applications received yet.</td></tr>';
                return;
            }
        } catch (e) {
            console.warn('Live shelter applications fetch unavailable, using stored applications');
        }

        renderShelterApps(tbody, demoApps);
    };

    const renderShelterApps = (tbody, apps) => {
        tbody.innerHTML = apps.map(app => `
            <tr>
                <td>#${app.id}</td>
                <td><strong>${UI.escapeHtml(app.petName)}</strong></td>
                <td>${UI.escapeHtml(app.adopterName)} (${UI.escapeHtml(app.adopterEmail)})</td>
                <td>${UI.escapeHtml(app.livingArrangement)}</td>
                <td id="app-status-${app.id}">${UI.getStatusBadge(app.status)}</td>
                <td>
                    <button class="nm-btn nm-btn-primary nm-btn-sm" onclick="Shelter.openReviewModal(${app.id})">Review Application</button>
                </td>
            </tr>
        `).join('');
    };

    const openReviewModal = async (appId) => {
        const idNum = parseInt(appId, 10);
        let app = null;

        try {
            const data = await API.get(`/api/applications/detail?id=${idNum}`);
            if (data.success && data.application) {
                app = data.application;
            }
        } catch (e) {
            console.warn('API application detail failed, searching local fallback');
        }

        if (!app) {
            const apps = getStoredApplications();
            app = apps.find(a => a.id === idNum);
        }

        if (!app) {
            UI.toast('Application details not found.', 'error');
            return;
        }

        // When reviewer clicks "Review Application" on a PENDING application, transition it to UNDER_REVIEW
        if (app.status === 'PENDING') {
            app.status = 'UNDER_REVIEW';
            if (!app.reviewNotes) {
                app.reviewNotes = 'Application under active review by shelter staff.';
            }
            await persistApplicationStatus(idNum, 'UNDER_REVIEW', app.reviewNotes);
            UI.toast(`Application #${idNum} is now under review!`, 'info');
            loadApplications();
            loadStats();
        }

        document.getElementById('reviewAppId').value = app.id;
        document.getElementById('reviewPetName').textContent = app.petName || 'Pet';
        document.getElementById('reviewAdopterName').textContent = app.adopterName || 'Adopter';
        document.getElementById('reviewAdopterContact').textContent = `${app.adopterEmail || 'N/A'} | Phone: ${app.adopterPhone || 'N/A'}`;
        document.getElementById('reviewLiving').textContent = `${app.homeType || ''} - ${app.livingArrangement || ''} (${app.householdMembers || 1} household members)`;
        document.getElementById('reviewExperience').textContent = app.petExperience || 'N/A';
        document.getElementById('reviewReason').textContent = app.adoptionReason || 'N/A';
        document.getElementById('reviewCare').textContent = app.careCommitment || 'N/A';
        document.getElementById('reviewCurrentStatus').innerHTML = UI.getStatusBadge(app.status);
        document.getElementById('reviewNotesInput').value = app.reviewNotes || '';

        UI.openModal('applicationReviewModal');
    };

    const submitReview = async (newStatus) => {
        const appId = document.getElementById('reviewAppId').value;
        const reviewNotes = document.getElementById('reviewNotesInput').value;

        if (newStatus === 'REJECTED' && (!reviewNotes || !reviewNotes.trim())) {
            UI.toast('Please provide a brief review note explaining the decision.', 'warning');
            return;
        }

        await persistApplicationStatus(appId, newStatus, reviewNotes);

        UI.toast(`Application marked as ${newStatus.replace('_', ' ')}!`, 'success');
        UI.closeModal('applicationReviewModal');
        loadApplications();
        loadStats();
        loadPets();
    };

    const loadConversations = async () => {
        const listEl = document.getElementById('conversationsList');
        if (!listEl) return;

        try {
            const data = await API.get('/api/messages/conversations');
            if (data.success && data.conversations && data.conversations.length > 0) {
                listEl.innerHTML = data.conversations.map(c => `
                    <div class="nm-card-sm conversation-item ${currentConversationId === c.id ? 'active' : ''}"
                         onclick="Shelter.openThread(${c.id}, '${UI.escapeHtml(c.adopterName)}', '${UI.escapeHtml(c.petName)}')"
                         style="cursor: pointer; margin-bottom: 0.75rem;">
                        <strong>${UI.escapeHtml(c.adopterName)}</strong>
                        <div style="font-size: 0.8rem; color: var(--primary);">Regarding: ${UI.escapeHtml(c.petName)}</div>
                    </div>
                `).join('');
            } else {
                listEl.innerHTML = '<div style="color: var(--text-muted); font-size: 0.9rem; padding: 1rem;">No conversation threads yet.</div>';
            }
        } catch (e) {
            console.error('Error loading conversations:', e);
        }
    };

    const openThread = async (convId, adopterName, petName) => {
        currentConversationId = convId;
        document.getElementById('chatRecipient').textContent = `${adopterName} (Regarding ${petName})`;
        loadMessages();
    };

    const loadMessages = async () => {
        if (!currentConversationId) return;
        const messagesEl = document.getElementById('messagesContainer');
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

        const input = document.getElementById('chatInput');
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
        openAddModal,
        openEditModal,
        savePet,
        openReviewModal,
        submitReview,
        openThread,
        sendMessage
    };
})();
