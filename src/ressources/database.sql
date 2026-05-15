-- =====================================================
-- SERVICE EXPRESS - BASE DE DONNÉES
-- =====================================================

-- 1. Créer la base de données (optionnel - Aiven utilise defaultdb)
CREATE DATABASE IF NOT EXISTS service_express;
USE service_express;

-- 2. Créer la table des annonces
CREATE TABLE IF NOT EXISTS ad (
                                  id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                  service_name VARCHAR(200) NOT NULL,
    description TEXT NOT NULL,
    price INT,
    location VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    category VARCHAR(50) NOT NULL,
    artisan_name VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- 3. Ajouter des index pour les recherches rapides
CREATE INDEX idx_category ON ad(category);
CREATE INDEX idx_location ON ad(location);
CREATE INDEX idx_created_at ON ad(created_at);

-- 4. Insérer quelques annonces de test (optionnel)
INSERT INTO ad (service_name, description, price, location, phone, category, artisan_name) VALUES
                                                                                               ('Coupe homme', 'Coupe professionnelle à domicile, tout quartier', 3000, 'Musaga', '61234567', 'Coiffure', 'Jean'),
                                                                                               ('Tresses', 'Tresses africaines, qualité garantie', 5000, 'Kamenge', '62345678', 'Coiffure', 'Marie'),
                                                                                               ('Réparation écran', 'Réparation écran iPhone et Samsung', 15000, 'Centre ville', '63456789', 'Réparation téléphone', 'Claude'),
                                                                                               ('Fabrication meubles', 'Meubles sur mesure (lits, armoires, tables)', NULL, 'Ngagara', '64567890', 'Menuiserie', 'Pierre'),
                                                                                               ('Couture moderne', 'Confection de pagnes, robes, costumes', 10000, 'Kinindo', '65678901', 'Couture', 'Chantal');