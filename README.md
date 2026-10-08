# Fastfood — commande à emporter ou sur place

Application de commande pour **un fast-food qui veut sa propre appli** au lieu de payer la commission d'Uber Eats.

- Le client commande depuis l'appli, **à emporter ou sur place** (le restaurateur choisit les modes qu'il propose).
- Il **paie au comptoir** : aucun paiement en ligne.
- **Fidélité** : à la remise, le client donne son **code unique** ; le restaurateur le saisit et les points sont crédités (1 point par euro par défaut). Les points s'échangent contre des cadeaux.

## Stack

| Partie | Technologies |
|---|---|
| API (`fastfood-api/`) | Java 21, Spring Boot 4, Spring Security + JWT, Spring Data JPA, Bean Validation, Lombok |
| Frontend (`fastfood-web/`) | React 19, TypeScript, Vite, React Router, Tailwind CSS |
| Base de données | PostgreSQL 17 via Docker, migrations Flyway |
| Tests | JUnit 5, Mockito, AssertJ |

## Lancer le projet

Prérequis : Java 21 (avec `JAVA_HOME` défini), Docker. La base Docker écoute sur le port **5433**, pour ne pas entrer en conflit avec un PostgreSQL déjà installé sur la machine.

```bash
docker compose up -d
cd fastfood-api
./mvnw spring-boot:run
```

L'API écoute sur `http://localhost:8080`. Au premier démarrage, Flyway crée les tables, le menu et les cadeaux de départ, et un compte restaurateur est créé :

- e-mail : `resto@fastfood.local`
- mot de passe : `resto1234` *(développement uniquement, modifiable via `ADMIN_EMAIL` / `ADMIN_PASSWORD`)*

Frontend (Node.js requis), dans un second terminal :

```bash
cd fastfood-web
npm install
npm run dev
```

L'interface s'ouvre sur `http://localhost:5173`. En développement, Vite redirige les appels `/api` vers Spring Boot.

- **Client** : menu, panier, choix à emporter ou sur place, suivi des commandes en direct, code et points de fidélité.
- **Restaurateur** : tableau des commandes (reçues, en préparation, prêtes), remise avec le code client, comptoir fidélité, plats épuisés.

Tests de l'API :

```bash
cd fastfood-api
./mvnw test
```

## Parcours

1. Le client s'inscrit et reçoit un code fidélité (ex. `K7P2QX`).
2. Il commande : `RECEIVED`.
3. Le restaurateur fait avancer la commande : `PREPARING` puis `READY`.
4. Au comptoir, le client paie et donne son code. Le restaurateur valide la remise : `COMPLETED`, points crédités.
5. Le client demande un cadeau ; le restaurateur saisit son code et le cadeau, les points sont débités.

```
RECEIVED ──► PREPARING ──► READY ──► COMPLETED
    │            │
    └────────────┴──► CANCELLED
```

## API

| Méthode | Route | Accès |
|---|---|---|
| POST | `/api/auth/register` · `/api/auth/login` | public |
| GET | `/api/auth/me` | connecté |
| GET | `/api/products` · `/api/settings` · `/api/rewards` | public |
| POST | `/api/orders` | client |
| GET | `/api/orders/mine` | client |
| POST | `/api/orders/{id}/cancel` | client (tant que `RECEIVED`) |
| GET | `/api/loyalty/me` | client |
| GET | `/api/admin/orders` | restaurateur |
| PATCH | `/api/admin/orders/{id}/status` | restaurateur |
| POST | `/api/admin/orders/{id}/complete` | restaurateur (avec le code client) |
| POST / PUT / PATCH | `/api/admin/products…` | restaurateur |
| GET | `/api/admin/loyalty/customers/{code}` | restaurateur |
| POST | `/api/admin/loyalty/redeem` | restaurateur |
| GET / POST / PUT | `/api/admin/rewards…` | restaurateur |
| PUT | `/api/admin/settings` | restaurateur |

Le jeton reçu au login s'envoie dans l'en-tête `Authorization: Bearer <jeton>`.

## Choix techniques

- **Rangement par fonctionnalité** (`auth`, `menu`, `order`, `loyalty`, `restaurant`) plutôt que par type de fichier.
- **Machine à états** dans `OrderStatus` : une commande ne peut pas sauter d'étape ni revenir en arrière.
- **Les points ne sont crédités qu'à la remise**, avec le code du client qui a passé la commande, et une seule fois par commande (contrôlé en Java et par un index unique en base).
- **Prix copiés dans la commande** : modifier le menu ne change pas les commandes passées.
- **Verrou optimiste** (`@Version`) sur les utilisateurs et les commandes : deux employés qui cliquent en même temps ne s'écrasent pas.
- **Erreurs au format RFC 9457** (`ProblemDetail`) grâce à un `@RestControllerAdvice`.
- **Origine CORS configurable** (`CORS_ORIGIN`) : l'API est prête à être appelée par un frontend web ou mobile.

## Utilisation de l'IA

*(À compléter : ce qui a été généré avec Claude Code, ce qui a été relu, corrigé ou réécrit à la main.)*
