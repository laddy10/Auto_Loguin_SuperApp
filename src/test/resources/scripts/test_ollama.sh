#!/bin/bash

echo "🔍 Verificando instalación de Ollama..."

# Verificar si Ollama está instalado
if ! command -v ollama &> /dev/null; then
    echo "❌ Ollama no está instalado"
    echo "Instalar con: curl https://ollama.ai/install.sh | sh"
    exit 1
fi

echo "✅ Ollama está instalado"

# Verificar si el servicio está corriendo
if curl -s http://localhost:11434/api/tags > /dev/null; then
    echo "✅ Servicio de Ollama está corriendo"
else
    echo "❌ Servicio de Ollama NO está corriendo"
    echo "Iniciar con: ollama serve"
    exit 1
fi

# Verificar modelo mistral
if ollama list | grep -q "mistral"; then
    echo "✅ Modelo 'mistral' está disponible"
else
    echo "⚠️  Modelo 'mistral' NO está disponible"
    echo "Descargar con: ollama pull mistral"
    exit 1
fi

echo ""
echo "🎉 Ollama configurado correctamente"
echo "Puedes ejecutar las pruebas ahora"