/**
 * PETS.JS - Pet Browsing, Filtering, Search, and Details View
 */

const Pets = (() => {
    let currentPage = 1;
    let currentSpecies = 'ALL';
    let searchQuery = '';

    // Built-in seed pet dataset for resilient offline / file protocol preview
    const SEED_PETS = [
        {
            id: 1,
            shelterId: 2,
            shelterName: 'Happy Paws Rescue',
            name: 'Bella',
            species: 'Dog',
            breed: 'Golden Retriever',
            ageMonths: 24,
            formattedAge: '2 yr',
            gender: 'FEMALE',
            size: 'LARGE',
            location: 'Austin, TX',
            description: 'Gentle, friendly, and eager to please. Bella gets along wonderfully with kids and other pets.',
            healthStatus: 'Fully checked, negative heartworm test',
            vaccinationStatus: 'Up to date',
            spayedNeutered: true,
            imageUrl: 'https://images.unsplash.com/photo-1552053831-71594a27632d?auto=format&fit=crop&w=800&q=80',
            status: 'AVAILABLE'
        },
        {
            id: 2,
            shelterId: 2,
            shelterName: 'Happy Paws Rescue',
            name: 'Milo',
            species: 'Dog',
            breed: 'French Bulldog',
            ageMonths: 14,
            formattedAge: '1 yr 2 mo',
            gender: 'MALE',
            size: 'SMALL',
            location: 'Austin, TX',
            description: 'Playful, comical, and loves cuddles on the sofa. Needs moderate daily exercise.',
            healthStatus: 'Healthy respiratory check',
            vaccinationStatus: 'Up to date',
            spayedNeutered: true,
            imageUrl: 'https://images.unsplash.com/photo-1583511655857-d19b40a7a54e?auto=format&fit=crop&w=800&q=80',
            status: 'AVAILABLE'
        },
        {
            id: 3,
            shelterId: 2,
            shelterName: 'Happy Paws Rescue',
            name: 'Luna',
            species: 'Cat',
            breed: 'Siamese Mix',
            ageMonths: 18,
            formattedAge: '1 yr 6 mo',
            gender: 'FEMALE',
            size: 'MEDIUM',
            location: 'Austin, TX',
            description: 'Vocal, affectionate, and deeply intelligent. Loves climbing towers and chasing laser toys.',
            healthStatus: 'FeLV/FIV negative',
            vaccinationStatus: 'Up to date',
            spayedNeutered: true,
            imageUrl: 'https://images.unsplash.com/photo-1514888286974-6c03e2ca1dba?auto=format&fit=crop&w=800&q=80',
            status: 'AVAILABLE'
        },
        {
            id: 4,
            shelterId: 3,
            shelterName: 'Safe Haven Sanctuary',
            name: 'Oliver',
            species: 'Cat',
            breed: 'British Shorthair',
            ageMonths: 36,
            formattedAge: '3 yr',
            gender: 'MALE',
            size: 'MEDIUM',
            location: 'Denver, CO',
            description: 'Placid, handsome gentleman cat with a quiet purr. Enjoys calm sunlit window sills.',
            healthStatus: 'Dental cleaning completed',
            vaccinationStatus: 'Up to date',
            spayedNeutered: true,
            imageUrl: 'https://images.unsplash.com/photo-1573865526739-10659fec78a5?auto=format&fit=crop&w=800&q=80',
            status: 'AVAILABLE'
        },
        {
            id: 5,
            shelterId: 3,
            shelterName: 'Safe Haven Sanctuary',
            name: 'Charlie',
            species: 'Dog',
            breed: 'Border Collie',
            ageMonths: 16,
            formattedAge: '1 yr 4 mo',
            gender: 'MALE',
            size: 'MEDIUM',
            location: 'Denver, CO',
            description: 'High-energy, hyper-intelligent agility enthusiast. Best suited for an active outdoor family.',
            healthStatus: 'Excellent stamina & joint health',
            vaccinationStatus: 'Up to date',
            spayedNeutered: true,
            imageUrl: 'https://images.unsplash.com/photo-1503256207526-0d5d80fa2f47?auto=format&fit=crop&w=800&q=80',
            status: 'AVAILABLE'
        },
        {
            id: 6,
            shelterId: 3,
            shelterName: 'Safe Haven Sanctuary',
            name: 'Thumper',
            species: 'Rabbit',
            breed: 'Holland Lop',
            ageMonths: 10,
            formattedAge: '10 mo',
            gender: 'MALE',
            size: 'SMALL',
            location: 'Denver, CO',
            description: 'Gentle, inquisitive bunny that loves fresh Timothy hay, romaine lettuce, and gentle head scratches.',
            healthStatus: 'Vaccinated for RHDV2',
            vaccinationStatus: 'Up to date',
            spayedNeutered: true,
            imageUrl: 'https://images.unsplash.com/photo-1585110396000-c9ffd4e4b308?auto=format&fit=crop&w=800&q=80',
            status: 'AVAILABLE'
        },
        {
            id: 7,
            shelterId: 2,
            shelterName: 'Happy Paws Rescue',
            name: 'Rio',
            species: 'Bird',
            breed: 'Cockatiel',
            ageMonths: 12,
            formattedAge: '1 yr',
            gender: 'MALE',
            size: 'SMALL',
            location: 'Austin, TX',
            description: 'Cheerful whistler who mimics household tunes and enjoys perching on friendly shoulders.',
            healthStatus: 'Avian vet check cleared',
            vaccinationStatus: 'Up to date',
            spayedNeutered: false,
            imageUrl: 'https://images.unsplash.com/photo-1522858547550-345670f99e4b?auto=format&fit=crop&w=800&q=80',
            status: 'AVAILABLE'
        },
        {
            id: 8,
            shelterId: 2,
            shelterName: 'Happy Paws Rescue',
            name: 'Rocky',
            species: 'Dog',
            breed: 'German Shepherd Mix',
            ageMonths: 30,
            formattedAge: '2 yr 6 mo',
            gender: 'MALE',
            size: 'LARGE',
            location: 'Austin, TX',
            description: 'Loyal guardian and loving family protector. Well trained in basic obedience and leash walking.',
            healthStatus: 'Microchipped, hip dysplasia screened clear',
            vaccinationStatus: 'Up to date',
            spayedNeutered: true,
            imageUrl: 'https://images.unsplash.com/photo-1589941013453-ec89f33b5455?auto=format&fit=crop&w=800&q=80',
            status: 'AVAILABLE'
        },
        {
            id: 9,
            shelterId: 3,
            shelterName: 'Safe Haven Sanctuary',
            name: 'Daisy',
            species: 'Dog',
            breed: 'Beagle',
            ageMonths: 8,
            formattedAge: '8 mo',
            gender: 'FEMALE',
            size: 'MEDIUM',
            location: 'Denver, CO',
            description: 'Curious, happy-go-lucky puppy ready to explore the world with an affectionate family.',
            healthStatus: 'First puppy series complete',
            vaccinationStatus: 'Puppy shots up to date',
            spayedNeutered: false,
            imageUrl: 'https://images.unsplash.com/photo-1537151625747-768eb6cf92b2?auto=format&fit=crop&w=800&q=80',
            status: 'AVAILABLE'
        }
    ];

    const loadFeaturedPets = async (containerId) => {
        const container = document.getElementById(containerId);
        if (!container) return;

        try {
            container.innerHTML = '<div style="grid-column: 1/-1; text-align: center; padding: 2rem;">Loading adorable pets...</div>';
            const data = await API.get('/api/pets/public?limit=6');
            if (data.success && data.pets && data.pets.length > 0) {
                renderPetGrid(container, data.pets);
                return;
            }
        } catch (e) {
            console.warn('API fetch unavailable, using seed pets:', e.message);
        }

        // Resilient fallback to seed pets
        renderPetGrid(container, SEED_PETS.slice(0, 6));
    };

    const loadCatalog = async (page = 1) => {
        const grid = document.getElementById('petsGrid');
        const paginationEl = document.getElementById('petsPagination');
        if (!grid) return;

        grid.innerHTML = '<div style="grid-column: 1/-1; text-align: center; padding: 3rem;">Searching companion listings...</div>';

        const gender = document.getElementById('filterGender') ? document.getElementById('filterGender').value : 'ALL';
        const size = document.getElementById('filterSize') ? document.getElementById('filterSize').value : 'ALL';

        let url = `/api/pets/public?page=${page}&limit=9`;
        if (currentSpecies && currentSpecies !== 'ALL') url += `&species=${encodeURIComponent(currentSpecies)}`;
        if (gender && gender !== 'ALL') url += `&gender=${encodeURIComponent(gender)}`;
        if (size && size !== 'ALL') url += `&size=${encodeURIComponent(size)}`;
        if (searchQuery) url += `&query=${encodeURIComponent(searchQuery)}`;

        try {
            const data = await API.get(url);
            if (data.success && data.pets && data.pets.length > 0) {
                renderPetGrid(grid, data.pets);
                renderPagination(paginationEl, data.currentPage, data.totalPages);
                return;
            }
        } catch (e) {
            console.warn('API catalog unavailable, filtering seed pets:', e.message);
        }

        // Resilient in-memory filtering of seed pets
        const queryLower = (searchQuery || '').trim().toLowerCase();
        const filtered = SEED_PETS.filter(p => {
            if (currentSpecies !== 'ALL' && p.species.toLowerCase() !== currentSpecies.toLowerCase()) return false;
            if (gender !== 'ALL' && p.gender.toUpperCase() !== gender.toUpperCase()) return false;
            if (size !== 'ALL' && p.size.toUpperCase() !== size.toUpperCase()) return false;
            if (queryLower) {
                const match = p.name.toLowerCase().includes(queryLower) ||
                              p.breed.toLowerCase().includes(queryLower) ||
                              p.location.toLowerCase().includes(queryLower);
                if (!match) return false;
            }
            return true;
        });

        if (filtered.length > 0) {
            const pageSize = 9;
            const start = (page - 1) * pageSize;
            const paged = filtered.slice(start, start + pageSize);
            const totalPages = Math.ceil(filtered.length / pageSize);
            renderPetGrid(grid, paged);
            renderPagination(paginationEl, page, totalPages);
        } else {
            grid.innerHTML = `
                <div class="nm-empty-state" style="grid-column: 1/-1;">
                    <div class="nm-empty-icon">🔍</div>
                    <h3 class="nm-empty-title">No Pets Found Matching Your Criteria</h3>
                    <p class="nm-empty-desc">Try resetting your filters or search terms to see other rescued friends looking for a home.</p>
                    <button type="button" class="nm-btn nm-btn-primary" onclick="Pets.resetFilters()">Clear Filters</button>
                </div>
            `;
            if (paginationEl) paginationEl.innerHTML = '';
        }
    };

    const renderPetGrid = (container, pets) => {
        container.innerHTML = pets.map(pet => {
            const defaultImg = 'https://images.unsplash.com/photo-1543466835-00a7907e9de1?auto=format&fit=crop&w=800&q=80';
            const imgUrl = pet.imageUrl && pet.imageUrl.trim() !== '' ? pet.imageUrl : defaultImg;

            return `
                <div class="pet-card">
                    <div class="pet-card-image">
                        <img src="${UI.escapeHtml(imgUrl)}" alt="${UI.escapeHtml(pet.name)}" onerror="this.src='${defaultImg}'" loading="lazy">
                        <span class="pet-card-badge">${UI.getStatusBadge(pet.status)}</span>
                    </div>
                    <div class="pet-card-body">
                        <div class="pet-card-header">
                            <h3 class="pet-card-title">${UI.escapeHtml(pet.name)}</h3>
                            <span style="font-weight: 600; color: var(--teal);">${UI.formatAge(pet.ageMonths)}</span>
                        </div>
                        <p class="pet-card-breed">${UI.escapeHtml(pet.breed)} &bull; ${UI.escapeHtml(pet.species)}</p>
                        
                        <div class="pet-tags">
                            <span class="pet-tag">${UI.escapeHtml(pet.gender)}</span>
                            <span class="pet-tag">${UI.escapeHtml(pet.size)}</span>
                            ${pet.spayedNeutered ? '<span class="pet-tag" style="background: var(--teal-light); color: var(--primary);">Spayed / Neutered</span>' : ''}
                        </div>

                        <div class="pet-card-meta">
                            <span>📍 ${UI.escapeHtml(pet.location)}</span>
                            <span>🏠 ${UI.escapeHtml(pet.shelterName || 'Shelter Partner')}</span>
                        </div>

                        <div class="pet-card-footer">
                            <a href="${API.resolvePageUrl('pet-details.html?id=' + pet.id)}" class="nm-btn nm-btn-secondary nm-btn-sm" style="flex: 1;">View Details</a>
                            <button type="button" class="nm-btn nm-btn-primary nm-btn-sm" onclick="Pets.startAdoption(${pet.id}, '${UI.escapeHtml(pet.name)}')" style="flex: 1;">Adopt Me</button>
                        </div>
                    </div>
                </div>
            `;
        }).join('');
    };

    const renderPagination = (el, current, total) => {
        if (!el || total <= 1) {
            if (el) el.innerHTML = '';
            return;
        }

        let html = '<div style="display: flex; gap: 0.5rem; justify-content: center; margin-top: 2rem;">';
        if (current > 1) {
            html += `<button class="nm-btn nm-btn-sm" onclick="Pets.goToPage(${current - 1})">&larr; Prev</button>`;
        }

        for (let i = 1; i <= total; i++) {
            const activeClass = i === current ? 'nm-btn-primary' : 'nm-btn-secondary';
            html += `<button class="nm-btn nm-btn-sm ${activeClass}" onclick="Pets.goToPage(${i})">${i}</button>`;
        }

        if (current < total) {
            html += `<button class="nm-btn nm-btn-sm" onclick="Pets.goToPage(${current + 1})">Next &rarr;</button>`;
        }
        html += '</div>';

        el.innerHTML = html;
    };

    const loadPetDetails = async () => {
        const urlParams = new URLSearchParams(window.location.search);
        const petId = urlParams.get('id');
        if (!petId) {
            API.navigateTo('pets.html');
            return;
        }

        const detailsEl = document.getElementById('petDetailContainer');
        if (!detailsEl) return;

        try {
            detailsEl.innerHTML = '<div style="text-align: center; padding: 4rem;">Loading companion profile...</div>';
            const data = await API.get(`/api/pets/detail?id=${petId}`);
            if (data.success && data.pet) {
                renderPetDetailPage(detailsEl, data.pet);
                return;
            }
        } catch (e) {
            console.warn('API detail fetch unavailable, checking seed pets:', e.message);
        }

        const fallbackPet = SEED_PETS.find(p => p.id === parseInt(petId, 10));
        if (fallbackPet) {
            renderPetDetailPage(detailsEl, fallbackPet);
        } else {
            detailsEl.innerHTML = `<div class="nm-empty-state"><h3 class="nm-empty-title">Pet Listing Not Found</h3></div>`;
        }
    };

    const renderPetDetailPage = (container, pet) => {
        const defaultImg = 'https://images.unsplash.com/photo-1543466835-00a7907e9de1?auto=format&fit=crop&w=800&q=80';
        const imgUrl = pet.imageUrl || defaultImg;

        container.innerHTML = `
            <div style="display: grid; grid-template-columns: 1fr 1.2fr; gap: 3rem; align-items: start;">
                <div>
                    <div class="nm-card" style="padding: 0.75rem; border-radius: var(--radius-xl); overflow: hidden;">
                        <img src="${UI.escapeHtml(imgUrl)}" alt="${UI.escapeHtml(pet.name)}" style="width: 100%; height: 420px; object-fit: cover; border-radius: var(--radius-lg);">
                    </div>
                    <div class="nm-card-inset" style="margin-top: 1.5rem;">
                        <h4 style="margin-bottom: 0.75rem; color: var(--primary);">🏥 Medical & Care Details</h4>
                        <p style="font-size: 0.9rem; margin-bottom: 0.5rem;"><strong>Health Status:</strong> ${UI.escapeHtml(pet.healthStatus || 'Veterinary check passed')}</p>
                        <p style="font-size: 0.9rem; margin-bottom: 0.5rem;"><strong>Vaccination:</strong> ${UI.escapeHtml(pet.vaccinationStatus || 'Up to date')}</p>
                        <p style="font-size: 0.9rem;"><strong>Spayed / Neutered:</strong> ${pet.spayedNeutered ? 'Yes' : 'No'}</p>
                    </div>
                </div>

                <div>
                    <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1rem;">
                        <span class="section-tag">${UI.escapeHtml(pet.species)}</span>
                        ${UI.getStatusBadge(pet.status)}
                    </div>
                    <h1 style="font-size: 2.75rem; margin-bottom: 0.5rem;">${UI.escapeHtml(pet.name)}</h1>
                    <p style="font-size: 1.15rem; color: var(--text-muted); margin-bottom: 1.5rem;">${UI.escapeHtml(pet.breed)} &bull; ${UI.formatAge(pet.ageMonths)} old</p>

                    <div style="display: grid; grid-template-columns: repeat(3, 1fr); gap: 1rem; margin-bottom: 2rem;">
                        <div class="hero-stat-card">
                            <span class="hero-stat-label">Gender</span>
                            <span class="hero-stat-value" style="font-size: 1.25rem;">${UI.escapeHtml(pet.gender)}</span>
                        </div>
                        <div class="hero-stat-card">
                            <span class="hero-stat-label">Size</span>
                            <span class="hero-stat-value" style="font-size: 1.25rem;">${UI.escapeHtml(pet.size)}</span>
                        </div>
                        <div class="hero-stat-card">
                            <span class="hero-stat-label">Location</span>
                            <span class="hero-stat-value" style="font-size: 1.1rem;">${UI.escapeHtml(pet.location)}</span>
                        </div>
                    </div>

                    <div class="nm-card" style="margin-bottom: 2rem;">
                        <h3 style="margin-bottom: 1rem;">About ${UI.escapeHtml(pet.name)}</h3>
                        <p style="line-height: 1.8; color: var(--text-secondary);">${UI.escapeHtml(pet.description)}</p>
                    </div>

                    <div class="nm-card-sm" style="margin-bottom: 2rem; display: flex; align-items: center; justify-content: space-between;">
                        <div>
                            <span style="font-size: 0.85rem; color: var(--text-muted);">Cared for by</span>
                            <h4 style="color: var(--primary);">${UI.escapeHtml(pet.shelterName || 'Shelter Partner')}</h4>
                        </div>
                        <span style="font-size: 0.9rem; color: var(--text-secondary);">Verified Rescue Shelter</span>
                    </div>

                    <div style="display: flex; gap: 1rem;">
                        <button type="button" class="nm-btn nm-btn-primary nm-btn-lg" onclick="Pets.startAdoption(${pet.id}, '${UI.escapeHtml(pet.name)}')" style="flex: 2;">
                            🐾 Start Adoption Application
                        </button>
                        <a href="${API.resolvePageUrl('pets.html')}" class="nm-btn nm-btn-secondary nm-btn-lg" style="flex: 1;">
                            Browse More
                        </a>
                    </div>
                </div>
            </div>
        `;
    };

    const startAdoption = (petId, petName) => {
        const user = Auth.getCurrentUser();
        if (!user) {
            UI.toast('Please sign in or create an Adopter account to apply.', 'info');
            setTimeout(() => {
                API.navigateTo(`login.html?redirect=${encodeURIComponent(window.location.pathname + window.location.search)}`);
            }, 1000);
            return;
        }

        if (user.role !== 'ADOPTER') {
            UI.toast(`Logged in as ${user.role}. Only Adopter accounts can apply for adoption.`, 'warning');
            return;
        }

        // Open Adoption Application Modal
        const modalPetIdInput = document.getElementById('appPetId');
        const modalPetNameEl = document.getElementById('appPetName');
        if (modalPetIdInput && modalPetNameEl) {
            modalPetIdInput.value = petId;
            modalPetNameEl.textContent = petName;
            UI.openModal('adoptionAppModal');
        } else {
            API.navigateTo(`dashboards/adopter.html?applyPetId=${petId}`);
        }
    };

    const setSpecies = (species, btnEl) => {
        currentSpecies = species;
        document.querySelectorAll('.filter-pill').forEach(btn => btn.classList.remove('active'));
        if (btnEl) btnEl.classList.add('active');
        loadCatalog(1);
    };

    const resetFilters = () => {
        currentSpecies = 'ALL';
        searchQuery = '';
        const searchInput = document.getElementById('searchQuery');
        if (searchInput) searchInput.value = '';
        const genderSelect = document.getElementById('filterGender');
        if (genderSelect) genderSelect.value = 'ALL';
        const sizeSelect = document.getElementById('filterSize');
        if (sizeSelect) sizeSelect.value = 'ALL';
        document.querySelectorAll('.filter-pill').forEach(btn => {
            btn.classList.toggle('active', btn.dataset.species === 'ALL');
        });
        loadCatalog(1);
    };

    const goToPage = (page) => {
        currentPage = page;
        loadCatalog(page);
        window.scrollTo({ top: 300, behavior: 'smooth' });
    };

    return {
        loadFeaturedPets,
        loadCatalog,
        loadPetDetails,
        setSpecies,
        resetFilters,
        goToPage,
        startAdoption,
        getPetById: (id) => SEED_PETS.find(p => p.id === parseInt(id, 10)) || null,
        SEED_PETS
    };
})();
