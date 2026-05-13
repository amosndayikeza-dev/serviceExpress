// ==================== CONFIGURATION ====================
const API_BASE_URL = '/ServiceExpress/api';

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

// ==================== VALIDATION EN TEMPS RÉEL ====================
function validateField(field, minLength = 3) {
    const value = field.value.trim();
    if (value.length >= minLength) {
        field.classList.add('valid');
        field.classList.remove('invalid');
        return true;
    } else {
        field.classList.add('invalid');
        field.classList.remove('valid');
        return false;
    }
}

function validatePhone(phone) {
    const phoneRegex = /^[0-9]{8,}$/;
    const isValid = phoneRegex.test(phone.value.trim());
    if (isValid) {
        phone.classList.add('valid');
        phone.classList.remove('invalid');
    } else {
        phone.classList.add('invalid');
        phone.classList.remove('valid');
    }
    return isValid;
}

// ==================== AFFICHAGE MESSAGE ====================
function showMessage(message, type) {
    const messageDiv = document.getElementById('formMessage');
    if (!messageDiv) return;

    messageDiv.textContent = message;
    messageDiv.className = `form-message ${type}`;
    messageDiv.style.display = 'block';

    setTimeout(() => {
        messageDiv.style.display = 'none';
    }, 5000);

    messageDiv.scrollIntoView({ behavior: 'smooth', block: 'center' });
}

// ==================== SOUMISSION ====================
async function submitAd(event) {
    event.preventDefault();

    const submitBtn = document.getElementById('submitBtn');
    const originalText = submitBtn.textContent;

    // Récupération des valeurs
    const serviceName = document.getElementById('serviceName');
    const description = document.getElementById('description');
    const location = document.getElementById('location');
    const phone = document.getElementById('phone');
    const category = document.getElementById('category');

    const ad = {
        serviceName: serviceName.value.trim(),
        description: description.value.trim(),
        price: document.getElementById('price').value ? parseInt(document.getElementById('price').value) : null,
        location: location.value.trim(),
        phone: phone.value.trim(),
        category: category.value,
        artisanName: document.getElementById('artisanName').value.trim() || null
    };

    // Validation
    if (!ad.serviceName || ad.serviceName.length < 3) {
        showMessage('Le nom du service doit contenir au moins 3 caractères', 'error');
        serviceName.focus();
        return;
    }

    if (!ad.description || ad.description.length < 10) {
        showMessage('La description doit contenir au moins 10 caractères', 'error');
        description.focus();
        return;
    }

    if (!ad.location) {
        showMessage('Le quartier/lieu est obligatoire', 'error');
        location.focus();
        return;
    }

    if (!ad.phone || ad.phone.length < 8) {
        showMessage('Le numéro de téléphone est obligatoire (minimum 8 chiffres)', 'error');
        phone.focus();
        return;
    }

    if (!ad.category) {
        showMessage('La catégorie est obligatoire', 'error');
        category.focus();
        return;
    }

    // Envoi
    submitBtn.disabled = true;
    submitBtn.textContent = '📢 Publication en cours...';

    try {
        const response = await fetch(`${API_BASE_URL}/ads`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(ad)
        });

        const result = await response.json();

        if (response.status === 201 || response.ok) {
            showMessage('✅ Annonce publiée avec succès ! Redirection vers l\'accueil...', 'success');
            document.getElementById('publishForm').reset();

            setTimeout(() => {
                window.location.href = 'index.html';
            }, 2000);
        } else {
            const errorMsg = result.message || result.error || 'Erreur lors de la publication';
            showMessage('❌ ' + errorMsg, 'error');
        }
    } catch (error) {
        console.error('Erreur:', error);
        showMessage('❌ Erreur de connexion au serveur', 'error');
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = originalText;
    }
}

// ==================== INITIALISATION ====================
document.addEventListener('DOMContentLoaded', () => {
    initMenu();

    const form = document.getElementById('publishForm');
    if (form) form.addEventListener('submit', submitAd);

    // Validation en temps réel
    const serviceName = document.getElementById('serviceName');
    const description = document.getElementById('description');
    const location = document.getElementById('location');
    const phone = document.getElementById('phone');

    if (serviceName) serviceName.addEventListener('input', () => validateField(serviceName, 3));
    if (description) description.addEventListener('input', () => validateField(description, 10));
    if (location) location.addEventListener('input', () => validateField(location, 2));
    if (phone) phone.addEventListener('input', () => validatePhone(phone));
});