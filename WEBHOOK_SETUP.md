# Configuration du webhook GitHub → Jenkins (via ngrok)

Ce guide permet de déclencher les builds Jenkins **immédiatement** après un `git push`, au lieu d'attendre le prochain cycle de Poll SCM (5 min).

> ⚠️ Ce fichier ne contient aucune donnée sensible et peut être commité en toute sécurité. Seule l'URL ngrok change à chaque redémarrage — elle n'est pas un secret mais n'est valide que temporairement.

## Prérequis

- Jenkins tourne sur `localhost:8090`
- [ngrok](https://ngrok.com/) installé localement
- Accès **Settings → Webhooks** sur les dépôts `bsfag/Back_Admin` et `bsfag/front_Admin` (nécessite les droits d'administration du dépôt)

## Étape 1 — Lancer ngrok pour exposer Jenkins

```powershell
.\ngrok http 8090
```

Tu vas voir un écran avec une URL publique du type :

```
Forwarding   https://abc123.ngrok-free.app -> http://localhost:8090
```

**Copie cette URL** (ex: `https://abc123.ngrok-free.app`). Garde ce terminal ouvert tant que tu testes.

## Étape 2 — Ajouter le webhook sur GitHub (backend)

1. Va sur `https://github.com/bsfag/Back_Admin`
2. **Settings** → **Webhooks** → **Add webhook**

Remplis :

```
Payload URL: https://abc123.ngrok-free.app/github-webhook/
Content type: application/json
Secret: (laisser vide)
Which events: Just the push event
Active: ✓
```

3. Clique **Add webhook**

> ⚠️ N'oublie pas le `/` final dans l'URL : `/github-webhook/`

## Étape 3 — Répéter pour le frontend

1. Va sur `https://github.com/bsfag/front_Admin`
2. **Settings** → **Webhooks** → **Add webhook**
3. Même configuration que l'étape 2

## Étape 4 — Activer le déclenchement dans Jenkins

Pour **chaque job** (`admin-backend-pipeline` et `admin-frontend-pipeline`) :

1. Jenkins → clique sur le job → **Configure**
2. Section **Build Triggers**
3. Coche : **GitHub hook trigger for GITScm polling**
4. Décoche (optionnel) **Poll SCM** puisque le webhook va le remplacer
5. **Save**

## Étape 5 — Tester

```bash
cd "chemin/vers/le/repo"
echo "# test webhook" >> README.md
git add README.md
git commit -m "test: trigger webhook"
git push origin main
```

Le build devrait se déclencher **immédiatement** dans Jenkins (pas besoin d'attendre les 5 minutes du Poll SCM).

## ⚠️ Point important — ngrok change d'URL à chaque redémarrage

Avec la version gratuite de ngrok, l'URL change si tu redémarres `ngrok http 8090`. Il faudra alors **mettre à jour les 2 webhooks GitHub** avec la nouvelle URL à chaque fois.

### Alternative pour éviter ce problème

Pour un usage plus régulier, deux options :
- **ngrok avec un domaine réservé** (plan payant) : l'URL reste fixe entre les redémarrages.
- **Exposer Jenkins directement** via un nom de domaine + reverse proxy (nginx/Caddy) si le poste a une IP publique ou est accessible depuis l'extérieur — solution plus robuste pour un usage long terme, à privilégier une fois le stage terminé si le projet doit rester en fonctionnement.

En attendant, le **Poll SCM toutes les 5 minutes** (déjà en place) reste un filet de sécurité fonctionnel même sans webhook actif.