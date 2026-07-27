# Guide de démonstration — Infrastructure DevOps/Sécurité


---

## 1. Vue d'ensemble de l'infrastructure

| VM | Rôle | IP | OS |
|---|---|---|---|
| `ubuntu-monitoring` | Control node Ansible + Monitoring | `192.168.72.10` | Ubuntu Server 24.04 |
| `rocky-staging` | Environnement Staging | `192.168.72.20` (SSH port `2222`) | Rocky Linux 9 |
| `wazuh-manager` | Sécurité — détection d'intrusion (HIDS) | `192.168.72.30` | Ubuntu Server 24.04 |
| `prod-server` | Environnement Production | `192.168.72.40` | Ubuntu Server 24.04 |

Labo pentest isolé (réseau séparé `Lab-Sec-Isole`, jamais connecté à l'infra ci-dessus) :

| VM | Rôle |
|---|---|
| Kali Linux | Machine attaquante (tests de sécurité) |
| `rocky-target-test` | Clone jetable de staging, utilisé pour valider fail2ban |

---

## 2. URLs d'accès — à ouvrir dans le navigateur

| Service | URL | Identifiants |
|---|---|---|
| **Application — Staging** | `http://192.168.72.20:4200` | `admin@entreprise.com` / mot de passe habituel |
| **Application — Production** | `http://192.168.72.40:4200` | `admin@entreprise.com` / mot de passe habituel |
| **Grafana** (dashboards monitoring) | `https://192.168.72.10` | `admin` / voir `.env` sur `ubuntu-monitoring` |
| **Prometheus** (métriques brutes) | Accessible uniquement en local sur `ubuntu-monitoring`, ou via tunnel SSH (voir §5) | — |
| **Alertmanager** (état des alertes) | Accessible uniquement en local, ou via tunnel SSH | — |
| **Wazuh Dashboard** (sécurité) | `https://192.168.72.30` | `admin` / mot de passe généré à l'installation (voir notes personnelles) |

> Grafana et Wazuh utilisent un certificat auto-signé — le navigateur affichera un avertissement de sécurité, cliquer sur **Avancé → Continuer**, c'est normal pour un labo interne.

---

## 3. Se connecter aux VMs (depuis PowerShell, sur le poste de travail)

```powershell
# Control node (Ubuntu monitoring)
ssh -i $HOME\.ssh\ansible_key yassmine@192.168.72.10

# Staging (Rocky)
ssh -i $HOME\.ssh\ansible_key -p 2222 ansible@192.168.72.20

# Wazuh Manager
ssh -i $HOME\.ssh\ansible_key ansible@192.168.72.30

# Production
ssh -i $HOME\.ssh\ansible_key ansible@192.168.72.40
```

---

## 4. Démonstration — Ansible (automatisation)

Depuis `ubuntu-monitoring`, dossier `~/ansible-infra` :

```bash
cd ~/ansible-infra

# Vérifier que toutes les VMs répondent
ansible all -m ping

# Voir l'inventaire complet
cat inventory.ini

# Relancer le durcissement de sécurité sur une VM (démonstration, sans danger — idempotent)
ansible-playbook playbooks/baseline.yml --limit prod-server --check --diff
```

**Ce que ça montre** : gestion centralisée de la configuration (SSH, fail2ban, pare-feu, synchronisation horaire) sur toutes les VMs depuis un seul point de contrôle.

---

## 5. Démonstration — Monitoring (Prometheus / Grafana / Alertmanager)

### Ouvrir Grafana dans le navigateur

```
https://192.168.72.10
```

Naviguer vers le dashboard **"Node Exporter Full"** — montrer les métriques CPU/RAM/disque en temps réel des VMs `ubuntu-monitoring` et `rocky-staging`.

### Accéder à Prometheus/Alertmanager (non exposés directement, par sécurité)

```powershell
# Tunnel SSH pour Prometheus
ssh -i $HOME\.ssh\ansible_key -L 9090:localhost:9090 yassmine@192.168.72.10
# puis ouvrir http://localhost:9090 dans le navigateur

# Tunnel SSH pour Alertmanager
ssh -i $HOME\.ssh\ansible_key -L 9093:localhost:9093 yassmine@192.168.72.10
# puis ouvrir http://localhost:9093 dans le navigateur
```

### Vérifier l'état des conteneurs de monitoring

```bash
cd ~/monitoring-stack
docker compose ps
```

### Démonstration d'une alerte en direct (optionnel)

```bash
# Sur rocky-staging, générer une charge CPU artificielle
ssh -i ~/.ssh/ansible_key -p 2222 ansible@192.168.72.20
sudo dnf install stress -y
stress --cpu 2 --timeout 300
```

Puis montrer dans Prometheus (`/alerts`) l'alerte `CPUUsageHigh` passer à `FIRING`, et l'email de notification reçu.

---

## 6. Démonstration — Sécurité (fail2ban + Wazuh)

### Vérifier fail2ban sur une VM

```bash
sudo fail2ban-client status sshd
```

### Ouvrir le Wazuh Dashboard

```
https://192.168.72.30
```

Naviguer vers **Agents management → Summary** — montrer les agents actifs (`wazuh-manager`, `rocky-staging`).

Naviguer vers **Threat Hunting** — montrer les événements de sécurité collectés en temps réel.

### Vérifier qu'un agent Wazuh est bien connecté

```bash
# Depuis wazuh-manager
sudo /var/ossec/bin/agent_control -l
```

---

## 7. Démonstration — Environnements Staging et Production

### Vérifier que les conteneurs tournent

```bash
# Sur rocky-staging
ssh -i ~/.ssh/ansible_key -p 2222 ansible@192.168.72.20
cd ~/staging-app
docker compose ps

# Sur prod-server
ssh -i ~/.ssh/ansible_key ansible@192.168.72.40
cd ~/prod-app
docker compose ps
```

### Ouvrir les deux applications côte à côte dans le navigateur

```
http://192.168.72.20:4200   (Staging)
http://192.168.72.40:4200   (Production)
```

Montrer qu'elles sont **indépendantes** : bases de données séparées, secrets distincts, mais construites à partir de la **même image Docker** — seule la configuration change entre les deux (variables d'environnement).

### Voir les logs applicatifs en direct

```bash
docker logs -f staging_backend    # ou prod_backend selon la VM
```

(`Ctrl+C` pour arrêter le suivi)

---

## 8. Démonstration — Pipeline CI/CD Jenkins

Depuis le navigateur : ouvrir l'interface Jenkins (URL habituelle de ton poste), montrer :

1. **`admin-backend-pipeline`** — historique des builds, étapes (tests → analyse qualité SonarQube → build → push Docker Hub → déploiement)
2. **`admin-frontend-pipeline`** — idem
3. Déclencher un build manuel (**Build Now**) pour montrer le pipeline complet s'exécuter en direct

### Après un build réussi — redéployer sur staging pour montrer le cycle complet

```bash
cd ~/staging-app
docker compose pull backend
docker compose up -d backend
docker logs staging_backend --tail 20
```

---

## 9. Résumé de l'architecture réseau (à présenter en schéma ou verbalement)

```
Réseau NAT VMware (192.168.72.0/24)
├── ubuntu-monitoring (.10)  — Control node + Monitoring
├── rocky-staging (.20)      — Environnement Staging
├── wazuh-manager (.30)      — Sécurité / Détection d'intrusion
└── prod-server (.40)        — Environnement Production

Réseau isolé "Lab-Sec-Isole" (séparé, jamais connecté au réseau ci-dessus)
├── Kali Linux               — Machine de test d'intrusion
└── rocky-target-test        — Clone jetable pour tests de sécurité
```

---

## 10. Points clés à mettre en avant devant l'encadreur

- **Infrastructure as Code** : toute la configuration système est automatisée via Ansible (playbooks versionnés), pas de configuration manuelle non reproductible.
- **Séparation des environnements** : dev / staging / prod sur des VMs distinctes, avec des secrets et bases de données indépendants — bonne pratique standard de l'industrie.
- **Sécurité en profondeur** : durcissement SSH (clé uniquement, pas de root), fail2ban (bannissement automatique), pare-feu strict, HTTPS pour les interfaces sensibles (Grafana, Wazuh), détection d'intrusion (Wazuh).
- **Validation réelle, pas seulement théorique** : test d'attaque brute-force effectivement mené depuis un labo isolé, avec preuve du bannissement automatique (`fail2ban-client status`).
- **Observabilité complète** : monitoring temps réel (Prometheus/Grafana) avec alerting email fonctionnel, testé en conditions réelles.
- **CI/CD opérationnel** : chaque commit déclenche automatiquement tests, analyse qualité (SonarQube), build, et publication d'image Docker.