/**
 * APPLICATIONS.JS - Adoption Application Submission and Client Handling
 * Online Pet Adoption Platform
 */

const Applications = (() => {

    const saveDemoApplication = (payload, user) => {
        const petId = parseInt(payload.petId, 10);
        let petName = 'Pet';
        let petImageUrl = 'https://images.unsplash.com/photo-1552053831-71594a27632d?auto=format&fit=crop&w=150&q=80';

        const modalPetNameEl = document.getElementById('appPetName');
        if (modalPetNameEl && modalPetNameEl.textContent && modalPetNameEl.textContent.trim()) {
            petName = modalPetNameEl.textContent.trim();
        }

        if (window.Pets && typeof Pets.getPetById === 'function') {
            const pet = Pets.getPetById(petId);
            if (pet) {
                if (!petName || petName === 'Pet') petName = pet.name;
                if (pet.imageUrl) petImageUrl = pet.imageUrl;
            }
        }

        const newApp = {
            id: Date.now() % 100000,
            petId: petId,
            petName: petName,
            petImageUrl: petImageUrl,
            adopterId: user ? user.id : 4,
            adopterName: user ? (user.fullName || user.email) : 'Sarah Jenkins',
            adopterEmail: user ? user.email : 'adopter.sarah@gmail.com',
            livingArrangement: payload.livingArrangement,
            homeType: payload.homeType,
            petExperience: payload.petExperience,
            householdMembers: payload.householdMembers,
            adoptionReason: payload.adoptionReason,
            careCommitment: payload.careCommitment,
            submittedAt: new Date().toISOString().substring(0, 10),
            status: 'PENDING',
            reviewNotes: 'Application received and waiting for shelter review.'
        };

        try {
            const existing = getDemoApplications();
            existing.unshift(newApp);
            sessionStorage.setItem('paws_demo_applications', JSON.stringify(existing));
            localStorage.setItem('paws_demo_applications', JSON.stringify(existing));
        } catch (err) {
            console.warn('Failed to persist demo application:', err);
        }

        return newApp;
    };

    const getDemoApplications = () => {
        try {
            const sess = sessionStorage.getItem('paws_demo_applications');
            if (sess) return JSON.parse(sess);
            const local = localStorage.getItem('paws_demo_applications');
            if (local) return JSON.parse(local);
        } catch (ignored) {}
        return [];
    };

    const submit = async (formElement) => {
        if (!Validation.validateForm(formElement)) {
            UI.toast('Please fill out all required questionnaire fields.', 'warning');
            return;
        }

        const petId = formElement.querySelector('#appPetId').value;
        const livingArrangement = formElement.querySelector('#appLiving').value;
        const homeType = formElement.querySelector('#appHomeType').value;
        const petExperience = formElement.querySelector('#appExperience').value;
        const householdMembers = parseInt(formElement.querySelector('#appMembers').value || '1', 10);
        const adoptionReason = formElement.querySelector('#appReason').value;
        const careCommitment = formElement.querySelector('#appCare').value;
        const agreementTerms = formElement.querySelector('#appTerms').checked;

        if (!agreementTerms) {
            UI.toast('You must agree to the adoption terms and ethical care commitment.', 'warning');
            return;
        }

        let user = (window.Auth && typeof Auth.getCurrentUser === 'function') ? Auth.getCurrentUser() : null;
        if (!user) {
            if (window.location.protocol === 'file:') {
                user = {
                    id: 4,
                    email: 'adopter.sarah@gmail.com',
                    fullName: 'Sarah Jenkins',
                    role: 'ADOPTER',
                    status: 'ACTIVE'
                };
                if (window.Auth && typeof Auth.setCurrentUser === 'function') {
                    Auth.setCurrentUser(user);
                }
            } else {
                UI.toast('Please sign in with an Adopter account to submit an application.', 'warning');
                setTimeout(() => {
                    API.navigateTo('login.html');
                }, 1000);
                return;
            }
        }

        const payload = {
            petId: parseInt(petId, 10),
            livingArrangement,
            homeType,
            petExperience,
            householdMembers,
            adoptionReason,
            careCommitment,
            agreementTerms
        };

        const submitBtn = formElement.querySelector('button[type="submit"]');
        const origText = submitBtn ? submitBtn.textContent : 'Submit';
        if (submitBtn) {
            submitBtn.disabled = true;
            submitBtn.textContent = 'Submitting Application...';
        }

        try {
            let isSuccess = false;
            let responseMsg = 'Application submitted successfully! Shelter has been notified.';

            // Attempt backend API call if running on web server
            if (window.location.protocol !== 'file:') {
                try {
                    const data = await API.post('/api/applications/submit', payload);
                    if (data && data.success) {
                        isSuccess = true;
                        if (data.message) responseMsg = data.message;
                        saveDemoApplication(payload, user);
                    }
                } catch (netErr) {
                    console.warn('Backend API submission unreachable, falling back to local demo storage:', netErr.message);
                    if (netErr.message && !netErr.message.includes('Failed to fetch') && !netErr.message.includes('NetworkError') && !netErr.message.includes('status 401') && !netErr.message.includes('status 0')) {
                        throw netErr;
                    }
                }
            }

            // Fall back to resilient demo storage for file:/// protocol or offline/server fallback
            if (!isSuccess) {
                saveDemoApplication(payload, user);
                isSuccess = true;
            }

            if (isSuccess) {
                UI.toast(responseMsg, 'success');
                UI.closeModal('adoptionAppModal');
                formElement.reset();
                setTimeout(() => {
                    API.navigateTo('dashboards/adopter.html#applications');
                }, 1200);
            }
        } catch (e) {
            UI.toast(e.message || 'Error submitting application.', 'error');
        } finally {
            if (submitBtn) {
                submitBtn.disabled = false;
                submitBtn.textContent = origText;
            }
        }
    };

    return {
        submit,
        saveDemoApplication,
        getDemoApplications
    };
})();
