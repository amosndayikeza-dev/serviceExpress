// ==================== CONFIGURATION ====================
const API_BASE_URL = '/ServiceExpress/api';
let currentAdminAds = [];

// ==================== MENU HAMBURGER ====================
function initMenu() {
    const menuBtn = document.getElementById('menuBtn');
    const sideMenu = document.getElementById('sideMenu');
    const menuOverlay = document.getElementById('menuOverlay');

    if (!menuBtn || !sideMenu || !menuOverlay) {
        console.log('Menu elements not found');
        return;
    }

    function toggleMenu() {
        menuBtn.classList.toggle('active');
        sideMenu.classList.toggle('active');
        menuOverlay.classList.toggle('active');
        document.body.style.overflow = sideMenu.classList.contains('active') ? 'hidden' : '';
    }

    menuBtn.addEventListener('click', toggleMenu);
    menuOverlay.addEventListener('click', toggleMenu);

    document.querySelectorAll('.menu-item').forEach(item => {
        item.addEventListener('click', () => {
            if (window.innerWidth <= 768 && sideMenu.classList.contains('active')) {
                toggleMenu();
            }
        });
    });
}

// ==================== UTILITAIRES ====================
function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function formatDate(dateString) {
    if (!dateString) return '';
    try {
        const date = new Date(dateString);
        return date.toLocaleDateString('fr-FR');
    } catch(e) {
        return '';
    }
}

function showAdminMessage(message, type) {
    const messageDiv = document.getElementById('loginMessage');
    if (!messageDiv) return;
    messageDiv.textContent = message;
    messageDiv.className = `form-message ${type}`;
    messageDiv.style.display = 'block';
    setTimeout(() => {
        messageDiv.style.display = 'none';
    }, 5000);
}

// ==================== CONNEXION ====================
async function adminLogin(event) {
    event.preventDefault();

    const passwordInput = document.getElementById('adminPassword');
    const password = passwordInput.value;

    if (!password) {
        showAdminMessage('Veuillez entrer le mot de passe', 'error');
        return;
    }

    try {
        // Appel à votre API de login
        const response = await fetch('/ServiceExpress/admin/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ password: password })
        });

        const result = await response.json();

        if (result.success) {
            showAdminMessage('Connexion réussie !', 'success');
            document.getElementById('loginSection').style.display = 'none';
            document.getElementById('adminSection').style.display = 'block';
            loadAdminAds();
            loadAdminStats();
        } else {
            showAdminMessage(result.error || 'Mot de passe incorrect', 'error');
        }
    } catch (error) {
        console.error('Erreur:', error);
        showAdminMessage('Erreur de connexion au serveur', 'error');
    }
}

// ==================== CHARGEMENT ANNONCES ADMIN ====================
async function loadAdminAds() {
    const container = document.getElementById('adminAdsList');
    if (!container) return;

    try {
        container.innerHTML = '<div class="loading"><div class="spinner"></div><p>Chargement...</p></div>';

        const response = await fetch(`${API_BASE_URL}/ads`);
        if (!response.ok) throw new Error('Erreur de chargement');

        const data = await response.json();
        // Gérer les deux formats possibles de réponse
        currentAdminAds = Array.isArray(data) ? data : (data.data || data);
        displayAdminAds(currentAdminAds);

    } catch (error) {
        console.error('Erreur:', error);
        container.innerHTML = '<div class="alert alert-error">❌ Erreur de chargement</div>';
    }
}

function displayAdminAds(ads) {
    const container = document.getElementById('adminAdsList');
    if (!container) return;

    if (!ads || ads.length === 0) {
        container.innerHTML = '<div class="alert alert-info">Aucune annonce trouvée</div>';
        return;
    }

    container.innerHTML = ads.map(ad => `
        <div class="admin-ad-item" data-id="${ad.id}">
            <div class="admin-ad-info">
                <div class="admin-ad-title">${escapeHtml(ad.serviceName)}</div>
                <div class="admin-ad-meta">
                    ${escapeHtml(ad.category)} | ${escapeHtml(ad.location)} | ${formatDate(ad.createdAt)} | 📞 ${ad.phone}
                </div>
            </div>
            <div class="admin-ad-buttons">
                <button class="btn btn-outline btn-sm" onclick="editAd(${ad.id})">✏️ Modifier</button>
                <button class="btn btn-danger btn-sm" onclick="deleteAd(${ad.id})">🗑️ Supprimer</button>
            </div>
        </div>
    `).join('');

    // Recherche
    const searchInput = document.getElementById('adminSearchInput');
    if (searchInput) {
        searchInput.onkeyup = () => {
            const keyword = searchInput.value.toLowerCase();
            if (!keyword) {
                displayAdminAds(currentAdminAds);
            } else {
                const filtered = currentAdminAds.filter(ad =>
                    ad.serviceName.toLowerCase().includes(keyword) ||
                    ad.description.toLowerCase().includes(keyword) ||
                    ad.location.toLowerCase().includes(keyword)
                );
                displayAdminAds(filtered);
            }
        };
    }
}

// ==================== STATISTIQUES ====================
async function loadAdminStats() {
    try {
        const response = await fetch(`${API_BASE_URL}/ads`);
        const data = await response.json();
        const ads = Array.isArray(data) ? data : (data.data || data);

        const totalAds = ads.length;
        const categories = [...new Set(ads.map(ad => ad.category))];

        const today = new Date().toDateString();
        const todayAds = ads.filter(ad => {
            const adDate = ad.createdAt ? new Date(ad.createdAt).toDateString() : '';
            return adDate === today;
        });

        document.getElementById('statTotalAds').textContent = totalAds;
        document.getElementById('statCategories').textContent = categories.length;
        document.getElementById('statTodayAds').textContent = todayAds.length;

    } catch (error) {
        console.error('Erreur stats:', error);
    }
}

// ==================== SUPPRESSION ====================
async function deleteAd(id) {
    if (!confirm('Êtes-vous sûr de vouloir supprimer cette annonce ?')) return;

    try {
        // Récupérer le mot de passe admin (stocké ou demandé)
        const adminPassword = prompt('Entrez le mot de passe admin pour confirmer la suppression :');
        if (!adminPassword) return;

        const response = await fetch(`${API_BASE_URL}/ads/${id}`, {
            method: 'DELETE',
            headers: { 'X-Admin-Password': adminPassword }
        });

        const result = await response.json();

        if (response.ok || result.success) {
            showAdminMessage('Annonce supprimée avec succès', 'success');
            loadAdminAds();
            loadAdminStats();
        } else {
            alert(result.message || result.error || 'Erreur lors de la suppression');
        }
    } catch (error) {
        console.error('Erreur:', error);
        alert('Erreur de connexion au serveur');
    }
}

// ==================== MODIFICATION ====================
async function editAd(id) {
    try {
        const response = await fetch(`${API_BASE_URL}/ads/${id}`);
        if (!response.ok) throw new Error('Annonce non trouvée');

        const ad = await response.json();

        document.getElementById('editId').value = ad.id;
        document.getElementById('editServiceName').value = ad.serviceName;
        document.getElementById('editDescription').value = ad.description;
        document.getElementById('editPrice').value = ad.price || '';
        document.getElementById('editLocation').value = ad.location;
        document.getElementById('editPhone').value = ad.phone;
        document.getElementById('editCategory').value = ad.category;
        document.getElementById('editArtisanName').value = ad.artisanName || '';

        const modal = document.getElementById('editModal');
        if (modal) {
            modal.style.display = 'block';
        }

        const closeBtn = document.querySelector('#editModal .close');
        if (closeBtn) {
            closeBtn.onclick = () => {
                modal.style.display = 'none';
            };
        }

        window.onclick = (event) => {
            if (event.target === modal) {
                modal.style.display = 'none';
            }
        };

    } catch (error) {
        console.error('Erreur:', error);
        alert('Impossible de charger l\'annonce');
    }
}

async function updateAd(event) {
    event.preventDefault();

    const ad = {
        id: parseInt(document.getElementById('editId').value),
        serviceName: document.getElementById('editServiceName').value.trim(),
        description: document.getElementById('editDescription').value.trim(),
        price: document.getElementById('editPrice').value ? parseInt(document.getElementById('editPrice').value) : null,
        location: document.getElementById('editLocation').value.trim(),
        phone: document.getElementById('editPhone').value.trim(),
        category: document.getElementById('editCategory').value,
        artisanName: document.getElementById('editArtisanName').value.trim() || null
    };

    try {
        const adminPassword = prompt('Entrez le mot de passe admin pour modifier :');
        if (!adminPassword) return;

        const response = await fetch(`/ServiceExpress/admin/update`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-Admin-Password': adminPassword
            },
            body: JSON.stringify(ad)
        });

        const result = await response.json();

        if (result.success) {
            document.getElementById('editModal').style.display = 'none';
            loadAdminAds();
            loadAdminStats();
            showAdminMessage('Annonce modifiée avec succès', 'success');
        } else {
            alert(result.error || 'Erreur lors de la modification');
        }
    } catch (error) {
        console.error('Erreur:', error);
        alert('Erreur de connexion');
    }
}

function closeEditModal() {
    const modal = document.getElementById('editModal');
    if (modal) {
        modal.style.display = 'none';
    }
}

// ==================== INITIALISATION ====================
document.addEventListener('DOMContentLoaded', () => {
    console.log('Admin page loaded');
    initMenu();

    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', adminLogin);
    }

    const editForm = document.getElementById('editForm');
    if (editForm) {
        editForm.addEventListener('submit', updateAd);
    }
});