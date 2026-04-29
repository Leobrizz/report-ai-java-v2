# report-ai-java v2

Librería y CLI en **Java 17** para:
- leer reportes de **Cucumber JSON**
- leer resultados de **Allure** (`*-result.json`)
- normalizar errores y detectar repetidos
- clasificar por reglas
- enriquecer con IA local (**Ollama**) o compatible con OpenAI
- guardar historial de ejecuciones
- generar un **reporte Word `.docx`**

## Qué resuelve
Cuando tenés 300+ casos, deja de ser práctico revisar uno por uno. Este proyecto consolida los fallos, agrupa patrones repetidos y arma un documento ejecutivo/técnico en Word.

---

## Arquitectura

```text
Cucumber JSON / Allure results
          ↓
       Parsers
          ↓
 Normalización + fingerprint
          ↓
 Agrupación de errores
          ↓
 Clasificación por reglas + IA
          ↓
 Historial JSONL
          ↓
 Exportación DOCX
```

---

## Estructura

```text
src/main/java/com/acme/reportai
 ├── ai
 ├── analyzer
 ├── cli
 ├── exporter
 ├── history
 ├── model
 ├── parser
 └── util
```

---

## Requisitos
- Java 17+
- Maven 3.9+

Verificación:

```powershell
java -version
mvn -version
```

---

## Cómo ejecutar con tu cucumber.json

### PowerShell

```powershell
cd "C:\ruta\report-ai-java-v2"
mvn clean compile exec:java "-Dexec.args=--cucumber=C:\ruta\a\tu\cucumber.json --output=output --ai.mode=mock"
```

### CMD clásico

```bat
cd C:\ruta\report-ai-java-v2
mvn clean compile exec:java -Dexec.args="--cucumber=C:\ruta\a\tu\cucumber.json --output=output --ai.mode=mock"
```

### Con Allure también

```powershell
mvn clean compile exec:java "-Dexec.args=--cucumber=C:\ruta\a\tu\cucumber.json --allure=C:\ruta\a\allure-results --output=output --ai.mode=mock"
```

---

## Modo IA

### 1. Modo mock
No consume nada externo. Sirve para probar el flujo completo.

```text
--ai.mode=mock
```

### 2. Modo Ollama local
Ejemplo con Ollama corriendo en `http://localhost:11434`.

```powershell
mvn clean compile exec:java "-Dexec.args=--cucumber=C:\ruta\cucumber.json --output=output --ai.mode=ollama --ai.baseUrl=http://localhost:11434 --ai.model=llama3.1"
```

### 3. Modo OpenAI compatible
**Importante:** tu plan ChatGPT Plus no incluye uso de API. Para usar OpenAI real necesitás API aparte. Este modo queda listo porque me pediste que todo quede hecho en Java.

```powershell
mvn clean compile exec:java "-Dexec.args=--cucumber=C:\ruta\cucumber.json --output=output --ai.mode=openai --ai.baseUrl=https://api.openai.com/v1 --ai.model=gpt-4.1-mini --ai.apiKey=TU_API_KEY"
```

También te sirve para endpoints compatibles con OpenAI montados localmente o vía gateway.

---

## Salidas
El proceso genera:

```text
output/
 ├── executive-report.docx
 ├── analysis.json
 └── history/
     └── executions.jsonl
```

### executive-report.docx
Documento Word con:
- resumen ejecutivo
- totales
- top errores repetidos
- clasificación por causa probable
- detalle por caso fallido

### analysis.json
Dump técnico del análisis.

### history/executions.jsonl
Historial incremental para comparar ejecuciones futuras.

---

## Uso rápido con los ejemplos incluidos

```powershell
cd "C:\ruta\report-ai-java-v2"
.\run-demo.ps1
```

o en CMD:

```bat
run-demo.bat
```

En PowerShell, si querés llamar un `.bat`, acordate que es con `./` o `.
`.

---

## Parámetros soportados

| Parámetro | Descripción |
|---|---|
| `--cucumber=` | ruta a `cucumber.json` |
| `--allure=` | ruta al directorio `allure-results` |
| `--output=` | carpeta de salida |
| `--project=` | nombre del proyecto |
| `--env=` | ambiente |
| `--framework=` | framework usado |
| `--ai.mode=` | `mock`, `ollama`, `openai` |
| `--ai.baseUrl=` | base URL del proveedor |
| `--ai.model=` | modelo a usar |
| `--ai.apiKey=` | api key si aplica |

---

## Cómo enchufar tu template Word real
Hoy el generador crea un DOCX directamente en Java con Apache POI. Si después querés usar tu template corporativo real, el siguiente paso es:

1. crear el `.docx` base con placeholders
2. reemplazar campos por código
3. mantener los estilos corporativos

Ejemplos de placeholders posibles:
- `${fecha}`
- `${ambiente}`
- `${total_casos}`
- `${pasados}`
- `${fallados}`
- `${errores_repetidos}`

---

## Roadmap recomendado
1. correr con tu `cucumber.json` real
2. validar clusters y normalización
3. sumar Allure real
4. ajustar prompt IA según tus errores
5. agregar comparación entre ejecuciones
6. enchufar template Word corporativo

---

## Notas técnicas
- El parser de Cucumber asume formato clásico de Cucumber JSON.
- El parser de Allure toma `*-result.json`.
- La agrupación se hace por fingerprint de mensaje normalizado.
- La clasificación por reglas se usa como base incluso si no hay IA.

---

## Próximo paso recomendado
Usalo primero con tu `cucumber.json` real. Cuando me pases un ejemplo real o la estructura exacta, el siguiente ajuste ideal es:
- mapear mejor `feature`, `suite`, `tags`
- agregar screenshots/logs de Allure al DOCX
- comparar ejecución actual vs historial
