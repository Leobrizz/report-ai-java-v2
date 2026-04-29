# report-ai-java v2

Librería y CLI en **Java 17** para:
- leer reportes de **Cucumber JSON**
- leer resultados de **Allure** (`*-result.json`)
- normalizar errores y detectar repetidos
- clasificar por reglas
- enriquecer con IA local (**Ollama / Hollama**) o cloud (**OpenAI, Codex, Gemini**)
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

### Con Allure también (carpeta `allure-results`)

```powershell
mvn clean compile exec:java "-Dexec.args=--cucumber=C:\ruta\a\tu\cucumber.json --allure=C:\ruta\a\allure-results --output=output --ai.mode=mock"
```

### Solo con Allure (`*-result.json`)

Si no usás Cucumber, podés ejecutar solo con la carpeta de Allure:

```powershell
mvn clean compile exec:java "-Dexec.args=--allure=C:\ruta\a\allure-results --output=output --project=MiProyecto --env=QA --framework=Playwright --ai.mode=mock"
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

También acepta `--ai.mode=hollama` como alias.

### 3. Modo OpenAI compatible
**Importante:** tu plan ChatGPT Plus no incluye uso de API. Para usar OpenAI real necesitás API aparte. Este modo queda listo porque me pediste que todo quede hecho en Java.

```powershell
mvn clean compile exec:java "-Dexec.args=--cucumber=C:\ruta\cucumber.json --output=output --ai.mode=openai --ai.baseUrl=https://api.openai.com/v1 --ai.model=gpt-4.1-mini --ai.apiKey=TU_API_KEY"
```

También te sirve para endpoints compatibles con OpenAI montados localmente o vía gateway.

### 4. Modo Codex (nuevo)
Este modo está separado de `openai` en la CLI (`--ai.mode=codex`) para que puedas rutear y configurar Codex explícitamente.

```powershell
mvn clean compile exec:java "-Dexec.args=--allure=C:\ruta\a\allure-results --output=output --ai.mode=codex --ai.baseUrl=https://api.openai.com/v1 --ai.model=codex-mini-latest --ai.apiKey=TU_API_KEY"
```

### 5. Modo Gemini (nuevo)
También podés elegir `--ai.mode=gemini` con endpoint/modelo compatibles.

```powershell
mvn clean compile exec:java "-Dexec.args=--allure=C:\ruta\a\allure-results --output=output --ai.mode=gemini --ai.baseUrl=TU_ENDPOINT_COMPATIBLE --ai.model=gemini-2.5-pro --ai.apiKey=TU_API_KEY"
```

---

## Salidas
El proceso genera:

```text
output/
 ├── executive-report-001.docx
 ├── analysis.json
 └── history/
     └── executions.jsonl
```

### executive-report-XXX.docx
Documento Word con:
- resumen ejecutivo
- totales
- top errores repetidos
- clasificación por causa probable
- detalle por caso fallido

> Cada ejecución crea un nuevo Word incremental (`executive-report-001.docx`, `executive-report-002.docx`, etc.), sin pisar el anterior.

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
| `--allure=` | ruta al directorio `allure-results` o a un archivo `*-result.json` |
| `--output=` | carpeta de salida |
| `--project=` | nombre del proyecto |
| `--env=` | ambiente |
| `--framework=` | framework usado |
| `--ai.mode=` | `mock`, `ollama`, `hollama`, `openai`, `codex`, `gemini` |
| `--ai.baseUrl=` | base URL del proveedor |
| `--ai.model=` | modelo a usar |
| `--ai.apiKey=` | api key si aplica |
| `--history=` | carpeta donde guardar `executions.jsonl` (opcional, separado de `output`) |

---

## App de escritorio (subir archivo + elegir análisis)

Además de CLI, ahora podés usar una app Swing:

```powershell
mvn clean compile exec:java "-Dexec.mainClass=com.acme.reportai.cli.ReportAiDesktopApp"
```

Si en tu shell tenés problemas con `-Dexec.mainClass`, usá esta opción robusta (sin punto en el nombre del parámetro):

```powershell
mvn clean compile exec:java "-Drun.mainClass=com.acme.reportai.cli.ReportAiDesktopApp"
```

Desde la UI podés:
- seleccionar archivo `cucumber.json` o carpeta `allure-results`
- elegir modo de análisis: `codex`, `gemini`, `ollama/hollama`, `mock`
- definir carpeta de resultados (`output`)
- definir carpeta de histórico (`history`) separada

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
Usalo primero con tu `cucumber.json` o carpeta `allure-results` real. Cuando me pases un ejemplo real o la estructura exacta, el siguiente ajuste ideal es:
- mapear mejor `feature`, `suite`, `tags`
- agregar screenshots/logs de Allure al DOCX
- comparar ejecución actual vs historial

---

## Guía completa: leer `allure-results` y crear un reporte Word "bonito"

1. Generá o ubicá la carpeta `allure-results` (debe contener archivos `*-result.json`).
2. Ejecutá la CLI con `--allure=...`.
3. Elegí IA:
   - `mock` para pruebas sin costo
   - `ollama` / `hollama` para IA local
   - `codex` (o `openai`) para endpoint OpenAI-compatible
4. Revisá la salida en `output/executive-report.docx`.

Ejemplo real de punta a punta (solo Allure + Codex):

```powershell
mvn clean compile exec:java "-Dexec.args=--allure=C:\qa\build\allure-results --output=output --project=Portal Web --env=UAT --framework=Playwright --ai.mode=codex --ai.baseUrl=https://api.openai.com/v1 --ai.model=codex-mini-latest --ai.apiKey=TU_API_KEY"
```

Qué hace con eso:
- lee todos los `*-result.json` de `allure-results`
- detecta `passed/failed/broken`
- agrupa errores repetidos por fingerprint
- clasifica causa probable (reglas + IA)
- genera:
  - `output/executive-report.docx` (reporte ejecutivo/técnico)
  - `output/analysis.json`
  - `output/history/executions.jsonl`
