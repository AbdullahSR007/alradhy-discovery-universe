#!/data/data/com.termux/files/usr/bin/bash
# One-time setup (run in Termux): creates YOUR permanent signing key and stores it in
# GitHub Actions secrets, so every future build installs as an update over the previous one.
set -euo pipefail
REPO="AbdullahSR007/alradhy-discovery-universe"
KS="$HOME/alradhy-release.jks"
PASSFILE="$HOME/alradhy-release.pass"
[ -f "$KS" ] && { echo "Key already exists at $KS - not overwriting."; exit 1; }
pkg install -y openjdk-17 gh >/dev/null
PASS=$(head -c 48 /dev/urandom | base64 | tr -dc 'A-Za-z0-9' | head -c 24)
keytool -genkeypair -keystore "$KS" -alias alradhy -keyalg RSA -keysize 2048 -validity 36500 \
  -storepass "$PASS" -keypass "$PASS" -dname "CN=ALRADHY Discovery, O=ALRADHY, C=SA"
umask 077; printf '%s' "$PASS" > "$PASSFILE"
gh auth status >/dev/null 2>&1 || gh auth login
base64 -w0 "$KS" | gh secret set ALRADHY_KEYSTORE_B64 -R "$REPO"
printf '%s' "$PASS"    | gh secret set ALRADHY_KEYSTORE_PASSWORD -R "$REPO"
printf '%s' "$PASS"    | gh secret set ALRADHY_KEY_PASSWORD -R "$REPO"
printf '%s' "alradhy"  | gh secret set ALRADHY_KEY_ALIAS -R "$REPO"
echo "Done. BACK UP these two files somewhere safe (losing them means a reinstall again):"
echo "  $KS"; echo "  $PASSFILE"
