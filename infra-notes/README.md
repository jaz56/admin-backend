# Infrastructure — Ansible + Monitoring (Prometheus/Grafana/Alertmanager)

Documentation de l'infrastructure de supervision et d'automatisation du projet d'administration.

> 📌 Ce README est mis à jour **à chaque étape**, pas seulement à la fin de chaque phase. La feuille de route complète est détaillée en bas de ce document.

## ⚠️ Sécurité — à lire avant tout

- Aucun secret réel (mots de passe, tokens) ne doit être commité dans ce dossier.
- Le fichier `.env` de `monitoring-stack/` est ignoré par Git (voir `.gitignore`).
- `alertmanager/alertmanager.yml` contient des identifiants SMTP réels — également ignoré par Git.
- Ne jamais réutiliser un mot de passe déjà utilisé ailleurs (ex: mot de passe MongoDB) pour un autre service (ex: Grafana).

## Vue d'ensemble de l'infrastructure

| VM | Rôle | OS | IP | Port SSH |
|---|---|---|---|---|
| `ubuntu-monitoring` | Control node Ansible + Prometheus/Grafana/Alertmanager | Ubuntu Server | `192.168.72.10` | `22` |
| `rocky-staging` | Environnement staging | Rocky Linux 9 (minimal) | `192.168.72.20` | `2222` (port custom) |

Réseau VMware : NAT (`VMnet8`), sous-réseau `192.168.72.0/24`, passerelle `192.168.72.2`, plage DHCP `192.168.72.128`–`192.168.72.254` (IP statiques hors de cette plage).

Labo de sécurité isolé (Kali, Metasploitable2, Win10-Victim, West-Wild-Lab) : LAN Segment VMware dédié `Lab-Sec-Isole`, **jamais connecté** au réseau `192.168.72.0/24`.

## Structure du projet

```
ansible-infra/
├── ansible.cfg
├── inventory.ini
├── playbooks/
│   ├── baseline.yml           # Phase 2 : durcissement SSH, fail2ban, firewall, chrony
│   └── monitoring-agents.yml  # Phase 4 : déploiement node_exporter
└── roles/
    ├── security/
    │   ├── tasks/main.yml
    │   ├── templates/jail.local.j2
    │   └── handlers/main.yml
    └── node_exporter/
        ├── tasks/main.yml
        ├── templates/node_exporter.service.j2
        └── handlers/main.yml

monitoring-stack/
├── docker-compose.yml
├── .env                        # ⚠️ jamais commité — secrets réels
├── .gitignore
├── prometheus/
│   ├── prometheus.yml
│   └── alert_rules.yml
├── alertmanager/
│   ├── alertmanager.yml        # ⚠️ jamais commité — identifiants SMTP réels
│   └── alertmanager.yml.example
└── grafana/
    └── provisioning/
```

## Prérequis

- VMware Workstation avec VMs Ubuntu Server et Rocky Linux 9 provisionnées
- Ansible (`ansible-core`) installé sur `ubuntu-monitoring` (control node)
- Docker + Docker Compose sur `ubuntu-monitoring`
- Accès SSH par clé configuré entre le control node et toutes les VMs cibles

## Ansible — utilisation

### Tester la connectivité

```bash
cd ~/ansible-infra
ansible all -m ping
```

### Appliquer le durcissement de base (Phase 2)

```bash
ansible-playbook playbooks/baseline.yml --diff
```

Applique : fail2ban (bannissement après 5 tentatives SSH échouées), pare-feu (UFW/firewalld), désactivation de l'authentification SSH par mot de passe et de la connexion root, synchronisation horaire chrony.

### Déployer node_exporter (Phase 4)

```bash
ansible-playbook playbooks/monitoring-agents.yml --diff
```

Installe l'agent `node_exporter` (port `9100`) sur chaque VM, avec accès réseau restreint à la seule IP du serveur Prometheus.

⚠️ **Limitation connue** : le mode `--check` (dry-run) échoue sur les tâches en plusieurs étapes (ex: `chrony`, installation `node_exporter` via `unarchive`) avec des erreurs du type `Could not find the requested service` ou `Source not found`. C'est un comportement connu d'Ansible en mode simulation avec des tâches interdépendantes — pas un bug réel. Exécuter directement sans `--check` si le `--check` échoue sur ce type de tâche.

## Stack de monitoring — Prometheus / Grafana / Alertmanager

### Lancer la stack

```bash
cd ~/monitoring-stack
docker compose up -d
docker compose ps
```

### Accès aux interfaces

| Service | URL | Identifiants |
|---|---|---|
| Prometheus | `http://192.168.72.10:9090` | — |
| Alertmanager | `http://192.168.72.10:9093` | — |
| Grafana | `http://192.168.72.10:3000` | `admin` / voir `.env` local |

### Règles d'alerte configurées (`prometheus/alert_rules.yml`)

| Alerte | Seuil | Durée avant déclenchement |
|---|---|---|
| `CPUUsageHigh` | CPU > 60% | 5 min |
| `MemoryUsageHigh` | RAM > 80% | 5 min |
| `DiskSpaceLow` | Disque > 85% | 1 min |
| `InstanceDown` | Cible injoignable | 1 min |
| `LoadAverageHigh` | Charge > 1.5x nb coeurs | 5 min |

Les alertes sont envoyées par email via Alertmanager (SMTP Gmail), avec notification de résolution automatique (`send_resolved: true`).

### Dashboard Grafana

Dashboard communautaire **"Node Exporter Full"** (ID `1860`) importé, connecté à la datasource Prometheus (`http://prometheus:9090` — nom de service Docker interne, pas l'IP externe).

### Configurer le mot de passe admin Grafana

⚠️ `GF_SECURITY_ADMIN_PASSWORD` (dans `.env`) ne s'applique qu'à la toute première création de la base Grafana. Pour changer le mot de passe après coup :

```bash
docker exec -it grafana grafana cli admin reset-admin-password "NouveauMotDePasse"
```

### Piège réseau Docker — accès à node_exporter depuis Prometheus

Quand Prometheus (dans un conteneur Docker) scrape un `node_exporter` tournant sur **la même VM hôte**, le trafic passe par le réseau interne Docker (`docker compose` crée un sous-réseau dédié, ex: `172.18.0.0/16` — vérifiable via `docker network inspect monitoring-stack_default`), **pas** par `127.0.0.1` ni l'IP externe de la VM. Il faut donc autoriser ce sous-réseau Docker dans le pare-feu de l'hôte :

```bash
docker network inspect monitoring-stack_default | grep Subnet
sudo ufw allow from <subnet_affiché>/16 to any port 9100 proto tcp
```

## Durcissement de l'accès (post-Phase 4)

⚠️ **Piège important — UFW ne bloque PAS les ports publiés par Docker.** Quand `docker-compose.yml` utilise `ports: - "9090:9090"`, Docker manipule directement iptables pour rediriger le trafic, et ces règles passent **devant** celles d'UFW. Une règle `ufw deny` sur un port publié par Docker ne bloque donc rien en pratique — piège classique et sournois.

### Solution appliquée

1. **Tous les ports Docker (Prometheus `9090`, Alertmanager `9093`, Grafana `3000`) sont désormais liés à `127.0.0.1` uniquement** dans `docker-compose.yml` (`"127.0.0.1:3000:3000"` au lieu de `"3000:3000"`) — plus jamais exposés sur le réseau, indépendamment d'UFW.
2. **Nginx** (processus système normal, pas dans Docker — donc UFW fonctionne correctement dessus) sert de reverse proxy HTTPS unique sur le port `443`, avec certificat auto-signé (labo interne, pas de nom de domaine public).
3. **Seul Grafana est exposé** via Nginx — Prometheus et Alertmanager restent accessibles uniquement en local sur la VM, ou via tunnel SSH pour du debug ponctuel :
   ```bash
   ssh -i ~/.ssh/ansible_key -L 9090:localhost:9090 ansible@192.168.72.10
   # puis http://localhost:9090 côté client
   ```
4. **UFW restreint l'accès HTTPS/HTTP (443/80)** à la seule IP de la machine hôte VMware (`192.168.72.1` — adresse standard côté hôte du NAT VMware, visible dans les logs de connexion).

### Accès Grafana durci

```
https://192.168.72.10
```

(avertissement de certificat auto-signé attendu et normal — "Avancé → Continuer")

### Configuration Nginx

Fichier : `/etc/nginx/sites-available/monitoring` (lien symbolique actif dans `sites-enabled/`). Redirection HTTP → HTTPS automatique, `proxy_pass` vers `127.0.0.1:3000`.

Certificat auto-signé généré via :
```bash
sudo openssl req -x509 -nodes -days 825 -newkey rsa:2048 \
  -keyout /etc/nginx/ssl/monitoring.key \
  -out /etc/nginx/ssl/monitoring.crt \
  -subj "/C=TN/ST=Tunis/L=Tunis/O=StageInfra/CN=192.168.72.10"
```

## Accès SSH depuis Windows — récupérer la clé privée sur le poste client

La clé `ansible_key` est générée sur le control node (Ubuntu). Pour l'utiliser aussi depuis PowerShell (Windows), copier son contenu :

```bash
# Sur Ubuntu (console VMware ou SSH déjà connecté)
cat ~/.ssh/ansible_key
```

Puis côté Windows :
```powershell
mkdir $HOME\.ssh -Force
notepad $HOME\.ssh\ansible_key
# Coller le contenu, Ctrl+S pour sauvegarder AVANT de fermer Notepad
# ⚠️ Notepad ajoute .txt automatiquement — vérifier et renommer si besoin :
Rename-Item $HOME\.ssh\ansible_key.txt ansible_key -ErrorAction SilentlyContinue

icacls $HOME\.ssh\ansible_key /inheritance:r
icacls $HOME\.ssh\ansible_key /grant:r "$($env:USERNAME):(R)"
```

⚠️ Après le durcissement Phase 2 (`PasswordAuthentication no`), **seul le compte `ansible`** peut se connecter en SSH (celui avec la clé déployée) — les comptes personnels (`adminuser`, `yassmine`, etc.) n'ont plus d'accès SSH du tout, seulement l'accès console direct via VMware.

## Tester le système d'alerte

```bash
# Sur la VM cible (ex: Rocky)
sudo dnf install stress -y      # ou apt install stress sur Ubuntu
stress --cpu 2 --timeout 480
```

Observer la montée en charge dans Grafana, puis vérifier le déclenchement de `CPUUsageHigh` dans Prometheus (`/alerts`) et la réception de l'email d'alerte.

## Dépannage rapide

| Problème | Solution |
|---|---|
| `Hash Sum mismatch` lors de `apt update` | Mirror en cours de synchronisation — relancer `apt update` une seconde fois, ou changer de mirror (`tn.archive.ubuntu.com` ↔ `archive.ubuntu.com`) |
| `Command 'ansible' not found` après `apt install ansible` | Installer via pip à la place : `pip3 install --user ansible-core` |
| `ERROR! the role 'X' was not found` | Créer `ansible.cfg` à la racine du projet avec `roles_path = roles` |
| `couldn't resolve module/action 'firewalld'` | Le module a été déplacé vers `ansible.posix` — installer la collection et utiliser le FQCN `ansible.posix.firewalld` |
| `Missing sudo password` lors d'un playbook | Configurer `NOPASSWD:ALL` pour le compte `ansible` via `sudo visudo -f /etc/sudoers.d/ansible-nopasswd` |
| `ssh-copy-id` échoue avec `Connection refused` sur Rocky | Vérifier le port SSH réel (`sudo ss -tlnp \| grep ssh`) — Rocky peut utiliser un port custom (ex: `2222`), préciser `-p <port>` |
| node_exporter `DOWN` dans Prometheus alors qu'il répond en local | Vérifier le pare-feu contre le bon sous-réseau Docker (voir section ci-dessus) |
| Grafana refuse le nouveau mot de passe après changement de `.env` | Utiliser `docker exec -it grafana grafana cli admin reset-admin-password` — `.env` ne s'applique qu'à la première création |

## Feuille de route — état d'avancement

| Phase | Contenu | Statut |
|---|---|---|
| 0 | Planification réseau et adressage IP | ✅ Fait |
| 1 | Provisionnement VMs, SSH, Ansible | ✅ Fait |
| 2 | Durcissement de base (SSH, fail2ban, firewall, chrony) | ✅ Fait |
| 3 | Environnements dev / staging / prod | ⏳ À faire |
| 4 | Stack de monitoring (Prometheus/Grafana/Alertmanager) | ✅ Fait |
| 5 | IDS/IPS (Wazuh/Suricata) + validation via labo pentest (Kali) | 🟡 fail2ban validé, Wazuh/Suricata à faire |
| 6 | Intégration Jenkins (déploiement auto par environnement) | ⏳ À faire |
| 7 | Documentation continue | 🟡 En cours (ce fichier) |

## Prochaine étape immédiate

Déploiement de Wazuh (HIDS) sur `rocky-staging` pour la détection d'intrusion basée sur l'hôte.

## Phase 5 — Validation fail2ban via labo pentest isolé (Kali)

### Méthodologie

Pour valider fail2ban en conditions réelles sans jamais exposer les vraies VMs infra au réseau pentest :

1. **Clone jetable** de `rocky-staging` → `rocky-target-test`, réseau basculé sur le LAN Segment isolé `Lab-Sec-Isole` (jamais connecté à `192.168.72.0/24`).
2. Sous-réseau dédié au labo : `10.10.10.0/24` (Kali `.5`, `rocky-target-test` `.20`), IP assignées manuellement (`nmcli connection modify` sur Rocky — persistant ; `ip addr add` sur Kali — non persistant, à refaire si la VM redémarre).
3. Réactivation **temporaire** de `PasswordAuthentication yes` sur le clone uniquement (pour permettre un vrai test brute-force — la config normale `no` empêche même la tentative, ce qui est la protection attendue en prod).

### Résultat du test

```bash
# Reconnaissance
nmap -sV -p- 10.10.10.20
# → port 2222/tcp open (ssh, OpenSSH 9.9), 80/tcp et 8080/tcp (Apache) également ouverts

# Attaque brute-force
hydra -l adminuser -P /tmp/passlist.txt ssh://10.10.10.20 -s 2222 -t 4
# → 0 mot de passe trouvé (attendu, liste de test)

# Vérification fail2ban
sudo fail2ban-client status sshd
# → Currently banned: 1
# → Banned IP list: 10.10.10.5
```

✅ **fail2ban bannit correctement l'IP attaquante après dépassement du seuil `maxretry = 5`**, confirmant que la chaîne complète (détection des échecs → bannissement → blocage réseau) fonctionne en conditions réelles.

### Piège rencontré — installation Rocky minimale sans copier-coller

Une installation Rocky "minimal" n'inclut pas `open-vm-tools` par défaut, empêchant le partage de presse-papiers avec l'hôte VMware. Cause de plusieurs erreurs de frappe lors de la saisie manuelle de commandes complexes dans la console. Fix :
```bash
sudo dnf install open-vm-tools -y
sudo systemctl enable --now vmtoolsd
```

### Nettoyage post-test

```bash
sudo sed -i 's/^PasswordAuthentication yes/PasswordAuthentication no/' /etc/ssh/sshd_config
sudo systemctl restart sshd
sudo fail2ban-client set sshd unbanip 10.10.10.5
```