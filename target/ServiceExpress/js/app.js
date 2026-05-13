// ==================== CONFIGURATION ====================
const API_BASE_URL = '/ServiceExpress/api';

// ==================== ÉTAT ====================
let currentAds = [];

// ==================== MENU HAMBURGER ====================
function initMenu() {
    const menuBtn = document.getElementById('menuBtn');
    const sideMenu = document.getElementById('sideMenu');
    const menuOverlay = document.getElementById('menuOverlay');

    if (!menuBtn || !sideMenu || !menuOverlay) return;

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
function formatPrice(price) {
    if (!price) return 'Prix sur demande';
    return new Intl.NumberFormat('fr-BI').format(price) + ' FBu';
}

function getCategoryIcon(category) {
    const icons = {
        'Coiffure': '💇',
        'Réparation téléphone': '📱',
        'Menuiserie': '🪑',
        'Couture': '👗',
        'Plomberie': '🔧',
        'Électricité': '⚡',
        'Autre': '📌'
    };
    return icons[category] || '📌';
}

function getAvatarClass(category) {
    const cat = (category || '').toLowerCase();
    if (cat.includes('coiffure')) return 'coiffure';
    if (cat.includes('r')) return 'reparation';
    if (cat.includes('menuiserie')) return 'menuiserie';
    if (cat.includes('couture')) return 'couture';
    if (cat.includes('plomberie')) return 'plomberie';
    if (cat.includes('électricité') || cat.includes('electricite')) return 'electricite';
    return 'default';
}

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function updateStatsBar(message, type = 'info') {
    const statsBar = document.getElementById('statsBar');
    if (statsBar) {
        statsBar.innerHTML = message;
        statsBar.style.color = type === 'error' ? 'var(--danger)' : 'var(--text-light)';
    }
}

function showLoading(show) {
    const container = document.getElementById('adsContainer');
    if (!container) return;
    if (show) {
        container.innerHTML = `
            <div class="loading">
                <div class="spinner"></div>
                <p>Chargement des services...</p>
            </div>
        `;
    }
}

// ==================== AFFICHAGE ====================
function displayAds(ads) {
    const container = document.getElementById('adsContainer');
    if (!container) return;

    if (!ads || ads.length === 0) {
        container.innerHTML = `
            <div class="alert alert-info">
                😕 Aucune annonce trouvée<br>
                <a href="publier.html" style="color: var(--primary);">Soyez le premier à publier une annonce !</a>
            </div>
        `;
        return;
    }

    const grouped = {};
    ads.forEach(ad => {
        const cat = ad.category || 'Autre';
        if (!grouped[cat]) grouped[cat] = [];
        grouped[cat].push(ad);
    });

    let html = '';
    for (const [category, categoryAds] of Object.entries(grouped)) {
        const icon = getCategoryIcon(category);

        html += `
            <div class="category-section">
                <div class="category-header" onclick="toggleCategory(this)">
                    <div class="category-title">
                        <span>${icon}</span>
                        <span>${escapeHtml(category)}</span>
                        <span class="category-count">${categoryAds.length}</span>
                    </div>
                    <div class="category-arrow">▼</div>
                </div>
                <div class="category-content">
                    <div class="services-grid">
        `;

        categoryAds.forEach(ad => {
            const avatarClass = getAvatarClass(ad.category);
            const artisanDisplay = ad.artisanName ? `👤 ${escapeHtml(ad.artisanName)}` : '👤 Artisan local';
            const priceDisplay = formatPrice(ad.price);

            html += `
                <div class="service-card">
                    <div class="service-avatar ${avatarClass}">
                        <div class="service-avatar-icon">${icon}</div>
                        <span class="category-badge">${escapeHtml(ad.category)}</span>
                    </div>
                    <div class="service-content">
                        <h3 class="service-title">${escapeHtml(ad.serviceName)}</h3>
                        <div class="service-artisan">${artisanDisplay}</div>
                        <div class="service-description">
                            ${escapeHtml(ad.description.substring(0, 100))}${ad.description.length > 100 ? '...' : ''}
                        </div>
                        <div class="service-info">
                            <span class="service-price">💰 ${priceDisplay}</span>
                            <span class="service-location">📍 ${escapeHtml(ad.location)}</span>
                        </div>
                        <button onclick="showPhoneModal(${ad.id})" class="service-phone-btn">
                            📞 Voir le numéro
                        </button>
                    </div>
                </div>
            `;
        });

        html += `
                    </div>
                </div>
            </div>
        `;
    }

    container.innerHTML = html;

    setTimeout(() => {
        document.querySelectorAll('.category-content').forEach(content => {
            content.style.maxHeight = content.scrollHeight + 'px';
        });
    }, 50);
}

// ==================== TOGGLE CATÉGORIE ====================
function toggleCategory(header) {
    const content = header.nextElementSibling;
    const arrow = header.querySelector('.category-arrow');
    if (!content) return;
    content.classList.toggle('collapsed');
    if (arrow) arrow.classList.toggle('collapsed');
    if (content.classList.contains('collapsed')) {
        content.style.maxHeight = '0';
    } else {
        content.style.maxHeight = content.scrollHeight + 'px';
    }
}

// ==================== MODALE TÉLÉPHONE ====================
async function showPhoneModal(adId) {
    try {
        const response = await fetch(`${API_BASE_URL}/ads/${adId}`);
        if (!response.ok) throw new Error('Annonce non trouvée');
        const ad = await response.json();

        const modal = document.getElementById('phoneModal');
        const modalServiceName = document.getElementById('modalServiceName');
        const modalArtisanName = document.getElementById('modalArtisanName');
        const modalPhone = document.getElementById('modalPhone');
        const callBtn = document.getElementById('callBtn');

        if (modalServiceName) modalServiceName.textContent = ad.serviceName;
        if (modalArtisanName) modalArtisanName.textContent = ad.artisanName || 'Anonyme';
        if (modalPhone) modalPhone.textContent = ad.phone;
        if (callBtn) callBtn.onclick = () => window.location.href = `tel:${ad.phone.replace(/[\s\-]/g, '')}`;

        modal.style.display = 'block';

        const closeBtn = modal.querySelector('.close');
        if (closeBtn) closeBtn.onclick = () => modal.style.display = 'none';
        window.onclick = (event) => { if (event.target === modal) modal.style.display = 'none'; };

    } catch (error) {
        console.error('Erreur:', error);
        updateStatsBar('Impossible de charger les détails', 'error');
    }
}

// ==================== CHARGEMENT ====================
async function loadAds() {
    const container = document.getElementById('adsContainer');
    if (!container) return;

    try {
        showLoading(true);
        const response = await fetch(`${API_BASE_URL}/ads`);
        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
        currentAds = await response.json();
        displayAds(currentAds);
        updateStatsBar(`📊 ${currentAds.length} service${currentAds.length > 1 ? 's' : ''} disponible${currentAds.length > 1 ? 's' : ''}`);
    } catch (error) {
        console.error('Erreur:', error);
        container.innerHTML = `<div class="alert alert-error">❌ Erreur de chargement<br><button onclick="loadAds()" style="margin-top:1rem;padding:0.5rem 1rem;background:var(--primary);color:white;border:none;border-radius:0.5rem;">Réessayer</button></div>`;
        updateStatsBar('Erreur de connexion', 'error');
    } finally {
        showLoading(false);
    }
}

async function loadAdsByCategory(category) {
    document.querySelectorAll('.filter-chip').forEach(chip => {
        chip.classList.remove('active');
        if (chip.dataset.category === category) chip.classList.add('active');
    });

    try {
        showLoading(true);
        let url = `${API_BASE_URL}/ads`;
        if (category !== 'all') url += `?category=${encodeURIComponent(category)}`;
        const response = await fetch(url);
        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
        currentAds = await response.json();
        displayAds(currentAds);
        if (category === 'all') {
            updateStatsBar(`📊 ${currentAds.length} service${currentAds.length > 1 ? 's' : ''} disponible${currentAds.length > 1 ? 's' : ''}`);
        } else {
            updateStatsBar(`📊 ${currentAds.length} service${currentAds.length > 1 ? 's' : ''} en ${category}`);
        }
    } catch (error) {
        console.error('Erreur:', error);
        document.getElementById('adsContainer').innerHTML = `<div class="alert alert-error">❌ Erreur de chargement</div>`;
    } finally {
        showLoading(false);
    }
}

async function searchAds(keyword) {
    if (!keyword || keyword.trim() === '') { loadAds(); return; }
    if (keyword.length < 2) { updateStatsBar('Le mot-clé doit contenir au moins 2 caractères', 'error'); return; }

    document.querySelectorAll('.filter-chip').forEach(chip => {
        chip.classList.remove('active');
        if (chip.dataset.category === 'all') chip.classList.add('active');
    });

    try {
        showLoading(true);
        const response = await fetch(`${API_BASE_URL}/ads?search=${encodeURIComponent(keyword)}`);
        if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
        currentAds = await response.json();
        displayAds(currentAds);
        updateStatsBar(`🔍 "${escapeHtml(keyword)}" - ${currentAds.length} résultat${currentAds.length > 1 ? 's' : ''}`);
    } catch (error) {
        console.error('Erreur:', error);
        document.getElementById('adsContainer').innerHTML = `<div class="alert alert-error">❌ Erreur de recherche</div>`;
    } finally {
        showLoading(false);
    }
}

// ==================== INITIALISATION ====================
function initFilters() {
    const categories = ['Coiffure', 'Réparation téléphone', 'Menuiserie', 'Couture', 'Plomberie', 'Électricité', 'Autre'];
    const filtersContainer = document.getElementById('quickFilters');
    if (!filtersContainer) return;

    filtersContainer.innerHTML = '<div class="filter-chip active" data-category="all">⭐ Tous</div>';
    categories.forEach(cat => {
        const chip = document.createElement('div');
        chip.className = 'filter-chip';
        chip.dataset.category = cat;
        chip.innerHTML = `${getCategoryIcon(cat)} ${cat}`;
        chip.onclick = () => loadAdsByCategory(cat);
        filtersContainer.appendChild(chip);
    });
}

document.addEventListener('DOMContentLoaded', () => {
    initMenu();
    initFilters();
    loadAds();

    const searchBtn = document.getElementById('searchBtn');
    const searchInput = document.getElementById('searchInput');

    if (searchBtn) searchBtn.addEventListener('click', () => searchAds(searchInput.value));
    if (searchInput) searchInput.addEventListener('keypress', (e) => { if (e.key === 'Enter') searchAds(searchInput.value); });
});