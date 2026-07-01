# report-ai-java v2

Lector de reportes **Cucumber JSON** y **Allure results** con analisis por reglas/IA, historico y exportacion Word.

## Novedades incluidas

- Ventana visual Swing para cargar un unico archivo/carpeta.
- Selector de tipo de reporte: `Auto detectar`, `Cucumber JSON` o `Allure results`.
- Selector de carpeta destino para los resultados.
- Desplegable para elegir IA: `mock`, `ollama`, `openai`, `codex`, `gemini`.
- Selector de modo: `Con historico` o `Analisis nuevo`.
- Reporte Word Allure mas amigable, con portada, KPIs, colores, tablas ejecutivas, clusters, fallas prioritarias, detalle tecnico y plan de accion.

## Abrir la ventana visual

```powershell
mvn clean compile exec:java
```

O bien:

```powershell
.\run-gui.ps1
```

En la ventana elegis:

```text
Tipo de reporte: Allure results / Cucumber JSON / Auto detectar
Archivo o carpeta: cucumber.json o carpeta allure-results
Destino resultados: output
IA: Sin IA, Ollama, OpenAI compatible, Codex o Gemini
Modo: Con historico o Analisis nuevo
```

## Modo historico vs analisis nuevo

### Con historico

Actualiza el archivo:

```text
output/history/executions.jsonl
```

y genera un Word incremental:

```text
output/allure-report-historico-001.docx
```

### Analisis nuevo

No modifica el historico y genera un Word independiente:

```text
output/allure-report-nuevo-001.docx
```

## Ejecutar por consola con Allure

### Historico + Mock

```powershell
mvn clean compile exec:java "-Dexec.args=--allure=C:\ruta\allure-results --output=output --analysis.mode=historico --ai.mode=mock"
```

### Nuevo + Mock

```powershell
mvn clean compile exec:java "-Dexec.args=--allure=C:\ruta\allure-results --output=output --analysis.mode=nuevo --ai.mode=mock"
```

### Historico + Ollama

```powershell
mvn clean compile exec:java "-Dexec.args=--allure=C:\ruta\allure-results --output=output --analysis.mode=historico --ai.mode=ollama --ai.baseUrl=http://localhost:11434 --ai.model=llama3.1"
```

## Ejecutar con Cucumber JSON

```powershell
mvn clean compile exec:java "-Dexec.args=--cucumber=C:\ruta\cucumber.json --output=output --analysis.mode=historico --ai.mode=mock"
```

## Parametros principales

| Parametro | Descripcion |
|---|---|
| `--cucumber=` | Ruta al `cucumber.json` |
| `--allure=` | Ruta a carpeta `allure-results` o archivo `*-result.json` |
| `--output=` | Carpeta de salida |
| `--analysis.mode=` | `historico` o `nuevo` |
| `--history.mode=` | `append` o `new` |
| `--ai.mode=` | `mock`, `ollama`, `openai`, `codex`, `gemini` |
| `--ai.baseUrl=` | URL base del proveedor IA |
| `--ai.model=` | Modelo IA |
| `--ai.apiKey=` | API key si aplica |
| `--project=` | Nombre del proyecto |
| `--env=` | Ambiente |
| `--framework=` | Framework usado |

## Salidas

```text
output/
 ├── allure-report-historico-001.docx
 ├── allure-report-nuevo-001.docx
 ├── analysis.json
 └── history/
     └── executions.jsonl
```

## Estructura relevante

```text
src/main/java/com/acme/reportai/ui/ReportAiGuiApplication.java
src/main/java/com/acme/reportai/service/ReportRunner.java
src/main/java/com/acme/reportai/exporter/WordReportExporter.java
src/main/java/com/acme/reportai/ai/PromptSanitizer.java
```
