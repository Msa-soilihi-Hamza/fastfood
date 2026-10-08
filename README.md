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

L'API écoute sur `http://localhost:8080`. Au premier démarrage, Flyway crée les tables, le menu et les cadeaux de départ, et un compte restaurateur est créé.

### Fichier `.env` (facultatif, jamais commité)

Les secrets se placent dans un fichier `.env` à la racine du projet, ignoré par Git :

```properties
RESEND_API_KEY=re_xxxxxxxx              # envoi réel des e-mails avec Resend
ADMIN_EMAIL=votre.adresse@exemple.fr    # compte restaurateur : doit recevoir ses codes
ADMIN_PASSWORD=<mot de passe de 12 caractères avec un symbole>
JWT_SECRET=<chaîne aléatoire d'au moins 32 caractères>
```

**Sans clé Resend**, rien n'est envoyé : chaque e-mail, avec son code, est affiché dans la console de l'API. C'est le plus simple pour tester le projet.

Avec l'adresse d'expédition de test de Resend (`onboarding@resend.dev`), Resend n'envoie qu'à l'adresse du compte Resend. Pour écrire à n'importe quel client, il faut vérifier un domaine dans Resend et définir `MAIL_FROM`.

Sans `ADMIN_EMAIL`, le compte restaurateur est `resto@fastfood.local` : ses codes ne sont lisibles que dans la console. Sans `ADMIN_PASSWORD`, son mot de passe est généré aléatoirement au premier démarrage et affiché une seule fois dans la console : aucun mot de passe n'est écrit dans le code.

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

## Authentification

Connexion en deux étapes, avec un code à 6 chiffres reçu par e-mail :

1. **Inscription** : le compte est créé non confirmé, un code est envoyé. Le compte n'est actif qu'une fois le code saisi.
2. **Connexion** : e-mail + mot de passe, puis le code reçu par e-mail. Le jeton JWT n'est délivré qu'après le code.

Protections :

| Menace | Protection |
|---|---|
| Mot de passe faible | 12 caractères minimum dont un symbole, vérifié côté serveur (`@StrongPassword`) et affiché en direct dans le formulaire |
| Force brute sur un compte | 5 mauvais mots de passe → compte bloqué 15 minutes, même avec le bon mot de passe ensuite |
| Force brute depuis une connexion | Limites par adresse IP sur l'inscription, la connexion, la saisie et le renvoi de code (HTTP 429 + `Retry-After`) |
| Deviner un code | 5 essais par code, validité 10 minutes, usage unique, nouveau code = ancien annulé |
| Inonder une boîte mail | 60 secondes entre deux renvois, 5 envois maximum par code |
| Savoir si un e-mail a un compte | Même message et même temps de réponse pour un e-mail inconnu ou un mauvais mot de passe |
| Fuite de la base | Mots de passe et codes stockés uniquement sous forme d'empreinte BCrypt |
| Fuite de secrets | Clés dans `.env` (ignoré par Git), avertissement au démarrage si le secret JWT de développement est utilisé |

Les compteurs anti-abus sont en mémoire (`RateLimiter`) : ils suffisent pour un seul serveur. Avec plusieurs instances, il faudrait les partager (Redis).

## Parcours

1. Le client s'inscrit, confirme son e-mail et reçoit un code fidélité (ex. `K7P2QX`).
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
| POST | `/api/auth/register` · `/api/auth/login` (envoient un code) | public |
| POST | `/api/auth/verify` (code → jeton) · `/api/auth/resend` | public |
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

Le jeton reçu après `/api/auth/verify` s'envoie dans l'en-tête `Authorization: Bearer <jeton>`.

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
