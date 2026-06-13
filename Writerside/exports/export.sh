#!/bin/bash

WEASYPRINT=~/.venv/weasyprint/bin/weasyprint
HTML=Writerside/exports/pdfSourceRP.html
PDF=Writerside/exports/rapport_final.pdf

if [ ! -f "$HTML" ]; then
    echo "Erreur : $HTML introuvable. Génère d'abord le HTML depuis Writerside."
    exit 1
fi

echo "Conversion en cours..."
$WEASYPRINT "$HTML" "$PDF" 2>&1

if [ $? -eq 0 ]; then
    echo "PDF généré : $PDF"
else
    echo "Erreur lors de la conversion."
    exit 1
fi