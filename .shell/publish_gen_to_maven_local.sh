cd build/gen && mvn clean install
set -e

# Source et destination
SRC="./src/main/java/com/example/demo/client"
DEST="../../src/main/java/com/example/demo/client"

echo "📦 Suppression de l'ancien dossier..."
rm -rf "$DEST"

echo "📁 Copie du nouveau dossier..."
mkdir -p "$(dirname "$DEST")"
cp -r "$SRC" "$DEST"

echo "✅ Copie terminée avec succès."