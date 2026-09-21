# SPI-PME — Application Android (Phase 5, modules Identité & Accès + Registre)

Pile : **Kotlin + Jetpack Compose** (Material3) + Hilt + Retrofit + Room +
Coroutines/Flow + DataStore. MVVM. Choix validé avec le porteur de projet
(modernité/robustesse/performance > conformité littérale du CDC qui
spécifiait Java/XML).

## ⚠️ État de vérification — à lire avant tout

**Ce projet n'a pas été compilé.** Contrairement au backend Django (où
`pytest` a réellement été exécuté à chaque étape), cet environnement n'a pas
accès à Maven Central ni au dépôt Google Maven (réseau restreint) — donc
`./gradlew build` n'a jamais pu être lancé ici.

Ce qui a été fait à la place, et qui a une valeur réelle mais n'égale pas une
compilation :
- Relecture manuelle de chaque fichier après écriture.
- Vérification automatisée de l'équilibre des accolades sur tous les fichiers.
- Recherche de références résiduelles à du code supprimé.
- **Deux bugs réels trouvés et corrigés** pendant cette relecture (voir plus
  bas) — donc le processus a une valeur démontrée, mais rien ne garantit
  qu'il n'en reste pas d'autres qu'une compilation aurait attrapés
  immédiatement (typage strict, résolution Hilt au moment de la
  compilation d'annotations, etc.).

**Avant tout usage réel : ouvre le projet dans Android Studio (Ladybug ou
plus récent) et laisse-le synchroniser Gradle.** C'est la vraie vérification.
Si Android Studio signale des erreurs, elles seront probablement mineures
(un import, une signature) vu le niveau de relecture déjà fait — mais je ne
peux pas l'affirmer avec la même certitude que pour le backend.

## Audit robustesse/performance/sécurité (à la demande du porteur de projet)

**Faille de sécurité réelle trouvée et corrigée** : les tokens JWT étaient
stockés en clair dans DataStore (préférences non chiffrées). Le token
d'accès est de courte durée (15 min) donc peu critique, mais le refresh
token vit **14 jours** (§8.3 backend) — le laisser en clair sur disque était
une vraie faiblesse en cas d'appareil root ou d'extraction de sauvegarde non
exclue. Corrigé avec `EncryptedSharedPreferences` (AES-256-GCM, clé
maîtresse dans l'Android Keystore matériel quand disponible) —
**uniquement pour les tokens**, pas pour le reste des préférences (profil,
secteur actif) qui ne présentent aucun risque : chiffrer indifféremment
tout aurait été un coût de performance sans bénéfice de sécurité réel.

Effet de bord positif sur la **performance** : `TokenManager` n'a plus
besoin d'être `suspend` (accès `SharedPreferences` synchrone, contrairement
à DataStore) — j'ai supprimé les `runBlocking` désormais inutiles dans
`AuthInterceptor` (appelé à chaque requête réseau) et `TokenAuthenticator`,
qui créaient un scope de coroutine pour une simple lecture mémoire.

⚠️ `androidx.security:security-crypto:1.1.0-alpha06` est une bibliothèque
en alpha — vérifier dans Android Studio si une version plus récente
(idéalement stable) est disponible au moment du build ; je ne peux pas
interroger Maven Central depuis cet environnement pour confirmer le
numéro de version le plus actuel.

## Manques corrigés depuis la dernière livraison

1. **Backend** : ajout de `GET /api/v1/entities/summary/` (`registry` app),
   testé réellement (2 nouveaux tests, 134/134 sur l'ensemble du backend,
   aucune régression). Regroupe par valeur de `type` réellement présente
   dans le périmètre — pas de liste blanche inventée, puisque `Entite.type`
   reste un champ libre par secteur (§10.2 du CDC).
2. **Android** : `EntiteRepositoryImpl.obtenirResume` simplifié — une seule
   requête réelle au lieu du contournement à 4 requêtes précédent.
3. **Android** : formulaire de création d'entité (`CreerEntiteScreen`)
   construit et câblé — le bouton "Nouvelle entité" de l'écran Registre
   n'est plus un `TODO`. Retour à la liste avec rafraîchissement automatique
   via `SavedStateHandle` (pattern standard Compose Navigation pour
   propager un résultat entre écrans).

### Bugs trouvés et corrigés pendant cette passe (cumulé)

1-4. (voir versions précédentes du README)
5. `SpiPmeNavGraph.kt` : import `LaunchedEffect` manquant dans
   `RegistryScreen.kt` après l'ajout du mécanisme de rafraîchissement —
   trouvé à la relecture, corrigé avant empaquetage.
6. Backend `test_registry.py` : une erreur d'édition avait dupliqué un
   décorateur `@pytest.mark.django_db` sur une classe et fait disparaître
   celui d'une autre — corrigé, confirmé par l'exécution réelle des tests.


## Modules livrés

### Ce qui manque pour un premier build

- Le wrapper Gradle (`gradle-wrapper.jar`) n'a pas pu être téléchargé ici
  (même restriction réseau). Ouvrir le projet dans Android Studio le
  régénère automatiquement ; en ligne de commande, lancer `gradle wrapper`
  une fois avec un Gradle local avant `./gradlew build`.
- Icônes de lanceur (`mipmap/ic_launcher`) non fournies — Android Studio en
  propose par défaut à la création, ou l'Image Asset Studio pour une vraie
  icône de marque.
- `local.properties` (chemin du SDK Android) : généré automatiquement par
  Android Studio à l'ouverture.

### Identité & Accès
- **Fondations projet** : version catalog Gradle, configuration multi-build-type
  (URL d'API différente en debug/release, jamais codée en dur dans le code source).
- **Thème Compose** : palette exacte du `Guide_Palette_Couleurs_SPI_PME.docx`
  (clair + sombre), typographie Material3.
- **Réseau** : deux clients OkHttp/Retrofit distincts (brut vs authentifié)
  pour éviter toute récursion du rafraîchissement de token ; intercepteur
  JWT ; `Authenticator` avec garde anti-boucle infinie ; gestion d'erreur
  réutilisant le format homogène du backend (`code`/`message`/`champs_invalides`).
- **Sécurité** : tokens en DataStore (jamais en `SharedPreferences` en
  clair), mot de passe local haché en PBKDF2-HMAC-SHA256 (120 000
  itérations, sel aléatoire) pour la connexion hors ligne.
- **Session** : `SessionManager` conserve le profil (secteur actif, rôle)
  après connexion — nécessaire à tous les écrans suivants, ajouté après
  avoir identifié le manque en préparant le module Registre.
- **Écrans** : Connexion, Connexion hors ligne.
- **Test unitaire réel** (JVM pur) : `HachageMotDePasseTest`, 4 cas.

### Registre
- **Écran** : cartes de résumé (Clients/Fournisseurs/Partenaires/Total),
  onglets filtrants, recherche avec anti-rebond (debounce 350ms, pas une
  requête réseau à chaque frappe), liste d'entités avec badges type/statut.
- **Composant partagé** `SpiPmeTopBar` : sélecteur de secteur actif,
  notifications, avatar — identique sur les 16 maquettes, un seul composant
  réutilisé plutôt que dupliqué écran par écran.
- Création d'entité : écran complet (`CreerEntiteScreen`), câblé au bouton
  "Nouvelle entité" avec rafraîchissement automatique de la liste au retour.

## Prochaine étape suggérée

`audit` (Journal d'audit, lecture seule) — dernier module backend restant
côté Android. Une fois fait, tous les modules métier auront leur écran ;
resteront les écrans transversaux (Accueil définitif, Notifications,
Profil, Paramètres, Administration) et la compilation réelle en Android
Studio, jamais faite dans cet environnement.

### Import de données (`imports`)

- **Sélection de fichier via Storage Access Framework**
  (`ActivityResultContracts.OpenDocument`) — aucune permission de stockage
  requise (vérifié : rien dans le manifeste), contrairement à l'ancienne
  API `READ_EXTERNAL_STORAGE`. Lecture des octets et du nom de fichier
  confinée à la couche UI (`core.files.LecteurFichier`, prend un `Context`
  en paramètre plutôt que d'en injecter un dans un ViewModel — évite toute
  fuite de `Context` dans une classe à portée plus longue que l'écran).
- **Upload multipart réel** : construction de `MultipartBody.Part` avec
  détection du type MIME par extension (`.csv`/`.xlsx`/`.xls`), en
  respectant le flux `preview` (sans écriture) puis `commit` (persiste)
  du backend — jamais une seule requête ambiguë.
- **Correctif de robustesse trouvé en le construisant** : le client HTTP
  authentifié n'avait pas de délai d'écriture (`writeTimeout`) explicite —
  le défaut OkHttp de 10 s est trop court pour l'upload d'un fichier Excel
  de plusieurs Mo sur un réseau lent. Porté à 60 s, spécifiquement sur ce
  client (celui utilisé pour l'upload), pas sur le client brut (qui ne
  transporte que de petits JSON de connexion).
- **Piège Kotlin évité proactivement** : `FichierSelectionne` contient un
  `ByteArray` — une `data class` standard aurait une égalité/`hashCode()`
  par référence, pas par contenu (piège classique et documenté de Kotlin).
  `equals()`/`hashCode()` redéfinis explicitement avec `contentEquals()`.
- Aperçu générique des lignes importées (même logique clé/valeur que les
  facteurs de suggestion IA — le format de colonnes dépend du fichier).

### Alertes (`alerts`)

- Même garantie structurelle que `intelligence` : aucune route de
  modification générique, seulement `generate`, `resolve`, `ignore`.
- **Réconciliation visible côté UI** : le message après génération indique
  à la fois les alertes nouvellement créées et celles résolues
  automatiquement (le backend detecte quand une condition a disparu, cf.
  `apps.alerts.services.generer_pour_secteur` — voir le backend), pas
  seulement les nouvelles.
- Onglets combinant niveau ET statut (`Critiques`/`Élevées`/`Modérées`
  filtrent sur `statut=active`, `Résolues` filtre sur `statut=traitee`) —
  reflète exactement les 4 cartes de la maquette `alertes.png`.
- Chaque alerte affiche son contexte (ressource, entité, tâche ou facture
  liée — le premier trouvé, puisqu'une alerte n'est jamais liée à plusieurs
  objets à la fois côté backend).

### Suggestions IA (`intelligence`)

- **Garantie FR-SUG-03 préservée côté client aussi** : aucune route de
  modification générique n'est appelée — seulement `generate`, `validate`,
  `reject` (avec motif obligatoire, boîte de dialogue dédiée). Le repository
  n'expose même pas de fonction "modifier" générique, pour qu'il soit
  structurellement impossible d'introduire un appel non conforme plus tard.
- **Facteurs explicatifs génériques** : `facteurs` varie selon l'algorithme
  côté backend (seuil vs régression linéaire) — plutôt que des champs
  typés à maintenir en double à chaque nouvel algorithme, converti en
  paires clé/valeur affichées en puces, à partir du `JsonObject` brut
  (kotlinx.serialization).
- **Vrai bug d'API expérimentale trouvé et corrigé avant même d'essayer de
  compiler** : `FlowRow` (mise en page automatique des puces de facteurs)
  est une API expérimentale de Compose Foundation qui exige une annotation
  `@OptIn` — remplacé par `LazyRow`, déjà utilisé ailleurs dans ce projet
  pour ce même genre de rangée de puces, ce qui élimine le risque plutôt
  que de simplement le supprimer.

### Tâches

- **Écran** : cartes (Toutes/Terminées/En cours/En retard), onglets,
  liste avec case à cocher pour marquer une tâche terminée (action directe
  depuis la liste, pas seulement depuis un détail), badge de priorité,
  échéance mise en évidence en rouge si en retard.
- **`PATCH /tasks/{id}/`** utilisé pour le changement de statut — contrairement
  à `intelligence`/`alerts`, le backend `tasks` est un `ModelViewSet`
  classique (pas de restriction en lecture seule), donc pas d'action
  dédiée nécessaire côté API.
- Formulaire de création avec validation de format de date (`AAAA-MM-JJ`)
  côté client avant envoi.

**Note technique** : cette session a subi une réinitialisation du
bac à sable en cours de construction de ce module — le travail non encore
empaqueté a été perdu et reconstruit à l'identique à partir des zips déjà
livrés. Aucun impact sur le contenu final, mais je le mentionne par souci
de transparence.

### Trésorerie

- **Écran** : cartes de résumé (Entrées/Sorties du mois, solde net, solde
  disponible), **graphe d'évolution du solde dessiné en `Canvas` natif**
  (pas de dépendance de graphique supplémentaire — juste un `Path` avec
  normalisation min/max, borné contre la division par zéro si tous les
  points sont égaux), liste de transactions, formulaire de création.
- **Bug transversal trouvé en construisant ce module, corrigé sur le
  backend** : DRF sérialise un `Decimal` renvoyé dans un dict brut
  (`Response({...})`, hors `Serializer`) en **nombre flottant**, pas en
  chaîne comme je le pensais initialement — contrairement à un champ
  `DecimalField` d'un vrai `Serializer`. Un test volontairement strict
  (vérification du JSON brut, pas de `response.data` déjà re-parsé côté
  Python qui aurait masqué le problème) l'a révélé. Corrigé en forçant
  `str(...)` explicitement sur toutes les valeurs `Decimal` des réponses
  brutes (`treasury` : summary/evolution ; `resources` : evolution/
  seuils-recommandes) — **135/135 tests backend** après correction.
- **Performance** : les trois ViewModels de liste (Registre, Ressources,
  Trésorerie) chargeaient résumé + liste **séquentiellement** alors que ce
  sont des appels réseau indépendants — corrigé pour les exécuter en
  parallèle (`coroutineScope` + `launch`), dans les trois modules pour
  rester cohérent, pas seulement le plus récent.

### Ressources

- **Écran** : mêmes fondations que Registre (cartes de résumé Critique/À
  surveiller/Stable/Total, onglets, recherche avec anti-rebond), avec en
  plus une **barre de progression par ressource** (niveau vs seuil
  d'alerte) et un badge "Seuils non configurés" quand ils sont absents —
  cohérent avec le choix backend de ne jamais inventer de seuils par défaut.
- **`BigDecimal` partout**, jamais `Double`, sur les quantités/montants —
  les DTO reçoivent les `DecimalField` du backend comme `String` (c'est le
  rendu JSON par défaut de DRF pour ce type de champ, précisément pour
  éviter les pertes de précision en flottant) et les convertissent en
  `BigDecimal` côté domaine.
- **Formulaire de création** avec validation numérique explicite côté
  client (quantité invalide, seuil critique > seuil d'alerte) — échoue vite
  et clairement plutôt que de laisser le serveur être le seul filet.
- Même mécanisme de rafraîchissement au retour (`SavedStateHandle`) que
  pour la création d'entité.
