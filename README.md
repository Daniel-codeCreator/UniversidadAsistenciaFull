# Universidad Asistencia

## Descripción

Universidad Asistencia es una aplicación académica de control de asistencia construida con Java Swing y SQL Server. El acceso puede requerir verificación facial en vivo mediante una webcam local.

La biometría no es un bypass ni un modo opcional de acceso: si el servicio facial está apagado, el login falla y no se permite continuar sin verificación.

## Arquitectura

```text
┌───────────────┐
│ Java Swing    │
│ LoginForm     │
└───────┬───────┘
        │ HTTP local
        ▼
┌───────────────┐
│ FastAPI       │
│ Facial API    │
└───────┬───────┘
        │
        ├── Webcam RGB
        ├── InsightFace / ArcFace
        └── SQL Server
             │
             ├── Usuario
             ├── PerfilFacial
             └── AuditoriaFacial
```

La aplicación Java mantiene la separación `View → Controller → Service → Repository → SQL Server`. `FacialAuthClient` usa `java.net.http.HttpClient` contra `http://127.0.0.1:8765` por defecto. Se puede cambiar mediante la propiedad `-Duniversidad.facial.url` o la variable `FACIAL_API_URL`.

## Tecnologías

- JDK 21 y Maven.
- IntelliJ IDEA recomendado para los formularios Swing UI Designer (`.form`).
- Python 3.11 o 3.12 para `facial-service`.
- FastAPI, OpenCV, InsightFace/ArcFace y ONNX Runtime CPU.
- SQL Server y Microsoft ODBC Driver 18 for SQL Server.
- Docker Desktop opcional, únicamente para SQL Server.

## Sistemas soportados

- Windows 10/11 x86_64.
- macOS Intel x86_64.
- macOS Apple Silicon arm64 (M1/M2/M3/M4).
- Linux x86_64.

GPU no requerida. La configuración ONNX selecciona providers disponibles y siempre conserva `CPUExecutionProvider` como fallback.

## Requisitos

Antes de iniciar:

1. Instale JDK 21, Maven, Docker Desktop opcional y una webcam.
2. Instale Python 3.12 preferentemente. Python 3.11 también está soportado.
3. Instale Microsoft ODBC Driver 18 for SQL Server.
4. Prepare una base `UniversidadAsistenciaDB` con el script SQL principal de `src/main/java/universidad/asistencia/config/queryCompleto.sql`.
5. Configure una clave persistente para `FACIAL_ENCRYPTION_KEY`.

`requirements.txt` usa ONNX Runtime CPU 1.20.1 en Windows/Linux y 1.19.2 en macOS Intel/Apple Silicon mediante un marker de pip, porque esa versión ofrece un wheel universal2 con una base de macOS más amplia. No se fija un provider GPU.

### ODBC Driver 18

- Windows: instale Microsoft ODBC Driver 18 for SQL Server desde la documentación oficial de Microsoft.
- macOS: instale Homebrew si aún no lo tiene y ejecute `brew install msodbcsql18`. Siga las instrucciones del instalador si solicita aceptar la licencia.
- Debian/Ubuntu/Kali: agregue el repositorio de paquetes de Microsoft correspondiente a su versión, instale `msodbcsql18` y `unixodbc`.
- Otras distribuciones Linux: use el repositorio o paquete de Microsoft para esa distribución y verifique el nombre con `python -c "import pyodbc; print(pyodbc.drivers())"`.

No se incluye un comando `sudo` universal: el paquete ODBC depende de la distribución y debe instalarse con las instrucciones oficiales del sistema operativo.

## Estructura del repositorio

```text
src/main/java/                  Aplicación Java Swing
src/main/java/.../config/       Configuración y SQL principal
facial-service/app/             API, cámara, modelo, cifrado y persistencia facial
facial-service/scripts/         Migración, doctor, enrollment y utilidades
facial-service/sql/             Migración idempotente de tablas faciales
facial-service/tests/           Pruebas Python sin webcam
docker-compose.yml              SQL Server solamente
```

No se versionan `.env`, `.venv`, modelos de InsightFace, imágenes, embeddings, `target` ni cachés de Python.

## Quick Start

El orden inicial es:

1. SQL Server y la base principal.
2. Migración facial.
3. Configuración y diagnóstico del servicio.
4. API facial.
5. Enrollment inicial del usuario que se demostrará.
6. Aplicación Java.

Después del primer enrollment, el orden diario es SQL Server → API facial → Java. No se registra el rostro en cada inicio.

### Windows

Desde PowerShell:

```powershell
git clone <URL-DEL-REPOSITORIO>
cd universidadAsistencia
```

Levante SQL Server con una instancia existente o Docker Desktop. Para Docker, cree un archivo local y levante el servicio sin borrar volúmenes existentes:

```powershell
Copy-Item .env.docker.example .env.docker
# Edite .env.docker y cambie MSSQL_SA_PASSWORD por una contraseña fuerte.
docker compose --env-file .env.docker up -d sqlserver
```

Ejecute el SQL principal en SQL Server Management Studio, Azure Data Studio o `sqlcmd`, y luego configure el componente facial:

```powershell
cd facial-service
.\setup-facial.ps1
notepad .env
```

En `.env`, complete `DB_PASSWORD` y genere una clave con:

```powershell
.\.venv\Scripts\Activate.ps1
python -m scripts.generate_key
```

Pegue la salida en `FACIAL_ENCRYPTION_KEY`. Mantenga esa clave estable. Después:

```powershell
python -m scripts.migrate
python -m scripts.doctor
.\start-facial.ps1
```

Deje la API ejecutándose y, en otra ventana de PowerShell:

```powershell
cd universidadAsistencia\facial-service
.\.venv\Scripts\Activate.ps1
python -m scripts.enroll_user --username admin
```

Si el usuario no se llama `admin`, búsquelo con `python -m scripts.find_user <usuario>` y use el `userId` mostrado. La primera carga puede descargar `buffalo_l` y tardar más; el modelo queda en la caché local de InsightFace, fuera del repositorio.

Abra el proyecto en IntelliJ IDEA, configure JDK 21 y la variable `DB_PASSWORD` (o las propiedades equivalentes `universidad.db.*`) en la configuración de ejecución de Java. Ejecute `Main` y use las credenciales demo actuales `admin/admin` únicamente si ese usuario existe y conserva esas credenciales.

### macOS

```bash
git clone <URL-DEL-REPOSITORIO>
cd universidadAsistencia
uname -m
```

Instale las herramientas requeridas, por ejemplo:

```bash
brew install python@3.12
brew tap microsoft/mssql-release https://github.com/Microsoft/homebrew-mssql-release
HOMEBREW_ACCEPT_EULA=Y brew install msodbcsql18
# Si pip necesita compilar InsightFace desde fuente:
xcode-select --install
```

Instale y ejecute Docker Desktop si SQL Server se levantará localmente:

```bash
cp .env.docker.example .env.docker
# Edite .env.docker y cambie MSSQL_SA_PASSWORD.
docker compose --env-file .env.docker up -d sqlserver
```

Ejecute el SQL principal en una herramienta conectada a SQL Server y prepare el servicio:

```bash
cd facial-service
./setup-facial-macos.sh
```

El setup instala o valida Python 3.12, JDK 21, Maven, `unixODBC`, ODBC Driver 18 y
las dependencias Python nativas para Apple Silicon. También prepara el modelo
`buffalo_l`, crea o conserva `.env` con permisos privados, obtiene la conexión
local desde `DatabaseConnection.java`, toma la contraseña únicamente del
contenedor SQL Server ya existente y ejecuta la migración facial idempotente.
No imprime la contraseña ni `FACIAL_ENCRYPTION_KEY`; no recrea ni modifica el
contenedor SQL Server. Debe terminar con `SETUP COMPLETED` para continuar.

Deje la API ejecutándose y compruebe el entorno desde otra terminal:

```bash
./start-facial.sh
# En otra terminal, dentro de facial-service:
./demo-check.sh
```

Deje la API ejecutándose y, en otra terminal:

```bash
cd universidadAsistencia/facial-service
source .venv/bin/activate
./enroll-user.sh admin
```

El enrollment requiere la API arriba y una sesión física frente a la webcam.
Conceda antes el permiso en `System Settings → Privacy & Security → Camera` a
la aplicación que ejecuta Python. `demo-check.sh` termina con `READY FOR DEMO`
solo cuando el doctor, la API, SQL Server y la cámara están disponibles.

#### macOS Apple Silicon (M1/M2/M3/M4)

`uname -m` debe mostrar `arm64`. Python, NumPy, OpenCV y ONNX Runtime se instalan desde wheels compatibles cuando están disponibles. La configuración no exige CUDA; `CPUExecutionProvider` es la ruta universal. Si `onnxruntime` ofrece un provider adicional, el servicio puede seleccionarlo automáticamente y conserva CPU como fallback.

La imagen oficial de SQL Server Linux es `linux/amd64`. En Apple Silicon, Compose usa `platform: linux/amd64` y Docker Desktop puede ejecutarla mediante emulación. Esto aplica al contenedor de SQL Server, no a Java ni a la webcam. No se dockeriza la API facial porque necesita acceder directamente a la cámara local.

Conceda acceso a la cámara en `System Settings → Privacy & Security → Camera` para la aplicación desde la que se inicia Python: Terminal, iTerm o IntelliJ, según corresponda. Si la webcam no abre, revise estos permisos antes de cambiar `CAMERA_INDEX`.

La sesión interactiva de enrollment/verify corre en un proceso Python separado cuyo
`MainThread` es dueño de la ventana Cocoa. FastAPI espera un JSON estructurado y
no captura frames ni intenta ejecutar `imshow` desde su worker. `ESC` o el botón
rojo cancela y libera la cámara; si HighGUI falla, la sesión termina con
`PREVIEW_UNAVAILABLE` y no guarda un perfil parcial.

Para diagnosticar la cámara sin FastAPI:

```bash
python -m scripts.list_cameras
python -m scripts.preview_test
```

`CAMERA_INDEX` vacío activa selección automática: se prefieren cámaras integradas
(`FaceTime`, `MacBook`, `Built-in`) y Continuity Camera queda como fallback.
Configure `CAMERA_NAME_HINT` si necesita orientar esa selección; un índice explícito
siempre tiene prioridad.

La primera ejecución del servicio puede descargar `buffalo_l`; no se almacena el modelo en el repositorio. En Apple Silicon, el setup valida la arquitectura de Python, NumPy, OpenCV, InsightFace y ONNX Runtime y el doctor muestra los providers efectivos, conservando CPU como fallback.

### Linux

En Debian/Ubuntu/Kali, instale Python, venv y ODBC con paquetes de la distribución y el repositorio de Microsoft:

```bash
sudo apt update
sudo apt install python3.12 python3.12-venv unixodbc
# Instale msodbcsql18 siguiendo el repositorio Microsoft de su distribución.
```

En otras distribuciones, use el equivalente de `python3.12`, `venv`, `unixODBC` y `msodbcsql18`. Para webcam, el dispositivo suele ser `/dev/video0`; verifique pertenencia a `video` con `groups` y no otorgue permisos sudo indiscriminadamente.

```bash
cd universidadAsistencia/facial-service
./setup-facial-linux.sh
source .venv/bin/activate
# Edite .env y configure DB_PASSWORD y FACIAL_ENCRYPTION_KEY.
python -m scripts.migrate
python -m scripts.doctor
./start-facial.sh
```

Deje la API ejecutándose y, en otra terminal:

```bash
cd universidadAsistencia/facial-service
source .venv/bin/activate
python -m scripts.enroll_user --username admin
```

## SQL Server

`docker-compose.yml` levanta únicamente SQL Server, con puerto 1433 y volumen persistente. No elimina ni recrea contenedores o volúmenes existentes; revise el nombre/proyecto Compose si ya tiene un servicio en ese puerto.

```bash
cp .env.docker.example .env.docker
# Cambie MSSQL_SA_PASSWORD.
docker compose --env-file .env.docker up -d sqlserver
docker compose --env-file .env.docker ps
```

En una instancia SQL Server existente, cree `UniversidadAsistenciaDB` y ejecute el script principal. `facial-service/sql/001_create_facial_tables.sql` no reemplaza el esquema completo: solo crea de forma idempotente `PerfilFacial`, `AuditoriaFacial` y su índice, sin borrar información.

## Configuración Java

El JDK oficial es 21. `pom.xml` usa `maven.compiler.release=21`; no depende de JDK 25 ni de JDK 26 aunque estén instalados.

`DatabaseConnection` acepta estas propiedades o variables de entorno:

```text
DB_URL=jdbc:sqlserver://127.0.0.1:1433;databaseName=UniversidadAsistenciaDB;encrypt=true;trustServerCertificate=true;sendTimeAsDatetime=false;
DB_USER=sa
DB_PASSWORD=<valor-local>
```

El valor por defecto de la URL es local; no existe una contraseña real dentro del código. `FacialAuthClient` usa `FACIAL_API_URL` o `-Duniversidad.facial.url`, con `http://127.0.0.1:8765` como default. El servicio no se expone en `0.0.0.0`.

### IntelliJ IDEA y formularios Swing

Se recomienda IntelliJ IDEA porque el proyecto usa Swing UI Designer y archivos `.form`.

Verifique en `Settings → Editor → GUI Designer` que el GUI Designer esté habilitado y que la instrumentación/generación de formularios esté activa para la compilación. Configure JDK 21 y las variables de base de datos en `Run/Debug Configuration → Environment variables`. VS Code puede editar el código, pero no es el entorno principal para procesar los `.form`; una ejecución completa debe hacerse desde IntelliJ o con un flujo de generación compatible con su instalación.

## Configuración facial-service

En `facial-service`, copie `.env.example` a `.env`. En Windows PowerShell:

```powershell
Copy-Item .env.example .env
```

En macOS/Linux:

```bash
cp .env.example .env
```

Variables principales:

```text
DB_HOST=127.0.0.1
DB_PORT=1433
DB_NAME=UniversidadAsistenciaDB
DB_USER=sa
DB_PASSWORD=<secreto-local>
DB_DRIVER=ODBC Driver 18 for SQL Server
FACIAL_HOST=127.0.0.1
FACIAL_PORT=8765
CAMERA_INDEX=
CAMERA_NAME_HINT=
FACE_MODEL=buffalo_l
FACE_MATCH_THRESHOLD=0.55
ONNX_PROVIDERS=auto
```

El umbral `0.55` es un punto inicial, no una garantía. Calibre registrando al usuario, probando varias verificaciones genuinas, probando otra persona y comparando scores antes de ajustar `FACE_MATCH_THRESHOLD`.

### Clave biométrica

**WARNING:** `FACIAL_ENCRYPTION_KEY` debe persistir en la instalación. Si cambia después de registrar perfiles, los embeddings existentes pueden dejar de descifrarse. Genere una clave nueva solo para una instalación nueva o después de un procedimiento explícito de re-enrollment. `python -m scripts.generate_key` imprime una clave segura y no modifica `.env`.

## Migraciones

Con el entorno virtual activo y SQL Server disponible:

```bash
python -m scripts.migrate
```

La ruta del SQL se resuelve con `Path(__file__).resolve()`; el comando funciona aunque se invoque desde otra carpeta siempre que se use el Python del entorno y el módulo se ejecute dentro de `facial-service`. La migración es idempotente y no elimina perfiles.

## Registro facial

La forma recomendada es resolver el usuario por nombre:

```bash
python -m scripts.find_user admin
python -m scripts.interactive_face enroll --username admin
python -m scripts.interactive_face verify --username admin
```

También se conserva el modo por ID:

```bash
python -m scripts.enroll_user --user-id 1
```

Los wrappers equivalentes son `./enroll-user.sh admin` en macOS/Linux y `.\enroll-user.ps1 1` en Windows. Para reemplazar un perfil existente, agregue `--replace` o `-Replace` explícitamente. La ventana interactiva es obligatoria; si HighGUI falla, la sesión termina con `PREVIEW_UNAVAILABLE`.

## Inicio del servicio

Los wrappers encuentran su propia carpeta y ejecutan directamente el Python del entorno virtual; no requieren activar `.venv`:

```powershell
.\start-facial.ps1
```

```bash
./start-facial.sh
```

El servicio escucha solo en `127.0.0.1`. `GET /health` devuelve `status`, `modelLoaded`, `database`, `platform` y `architecture`, sin contraseñas, claves, embeddings ni rutas sensibles.

InsightFace descarga `buffalo_l` solo cuando no encuentra la caché local y muestra un mensaje claro en los logs. Las ejecuciones siguientes reutilizan la caché. GPU no requerida.

## Ejecutar la aplicación Java

Con SQL Server y el servicio facial arriba, abra el proyecto en IntelliJ, seleccione JDK 21, configure `DB_PASSWORD` para Java y ejecute `universidad.asistencia.Main`. El flujo es:

1. Credenciales `admin/admin` solo si corresponden al usuario demo existente.
2. Consulta del usuario activo.
3. `FacialAuthClient → POST /face/verify`.
4. Webcam, liveness, ArcFace y comparación del embedding cifrado.
5. Apertura del `MainForm` solo si la verificación coincide.

## Scripts disponibles

| Script | Función |
| --- | --- |
| `setup-facial.ps1` | Crea `.venv`, instala dependencias y ejecuta diagnóstico en Windows. |
| `setup-facial-macos.sh` | Setup reproducible para macOS; instala Homebrew si falta y valida la toolchain arm64. |
| `setup-facial-linux.sh` | Setup reproducible para Linux; no modifica el sistema. |
| `start-facial.ps1`, `start-facial.sh`, `start-facial.bat` | Inician la API desde su propia ruta. |
| `enroll-user.ps1`, `enroll-user.sh` | Wrappers de enrollment por ID o nombre. |
| `python -m scripts.interactive_face` | Runner directo de enrollment/verify con ventana, overlay y liveness. |
| `python -m scripts.preview_test`, `scripts.list_cameras` | Prueba HighGUI y enumeración/selección de cámaras. |
| `python -m scripts.migrate` | Aplica la migración facial idempotente. |
| `python -m scripts.find_user admin` | Muestra ID, estado y perfil sin modificar datos. |
| `python -m scripts.generate_key` | Genera una clave base64 sin editar archivos. |
| `python -m scripts.doctor` | Pre-flight principalmente read-only. |
| `demo-check.ps1`, `demo-check.sh` | Doctor + `/health`; no registra rostros. |

## Doctor / diagnóstico

Antes de una demostración:

```bash
python -m scripts.doctor
```

Comprueba versión de Python, sistema/arquitectura, virtualenv, imports, OpenCV GUI, InsightFace, ONNX Runtime y CPU provider, ODBC, SQL Server, base, tablas faciales, modelo en caché, cámara seleccionada, frames, `imshow`, clave y puerto 8765. Si el modelo aún no existe muestra `WAIT`: la primera ejecución de la API lo descargará. No imprime secretos y no registra ni elimina datos; la comprobación de cámara abre y libera el dispositivo.

Cuando la API ya está arriba:

```bash
./demo-check.sh
```

El comando termina con `READY FOR DEMO` solo si doctor y `/health` están correctos.

## Troubleshooting

**No se pudo comunicar con el servicio facial** — Compruebe que `start-facial.ps1`, `start-facial.sh` o `start-facial.bat` esté ejecutándose, que `FACIAL_HOST=127.0.0.1`, `FACIAL_PORT=8765` sea el mismo valor y que ningún firewall/proceso ocupe el puerto.

**Camera/preview unavailable** — Ejecute `python -m scripts.list_cameras` y `python -m scripts.preview_test`. Revise permisos de cámara en macOS y que otra aplicación no la esté usando. Deje `CAMERA_INDEX` vacío para selección automática o configure un índice explícito; `CAMERA_NAME_HINT` puede orientar la selección. En Linux revise `/dev/video0` y el grupo `video`. Si `imshow` falla, la sesión devuelve `PREVIEW_UNAVAILABLE` y no continúa a ciegas.

**No se detectó rostro** — Asegure iluminación frontal, rostro completo, distancia suficiente y una sola persona en la imagen. El servicio exige calidad, pose admisible y liveness.

**ODBC Driver not found** — Instale ODBC Driver 18 y `unixODBC` según el sistema. Confirme con `python -c "import pyodbc; print(pyodbc.drivers())"` y compare con `DB_DRIVER`.

**SQL Server unavailable** — Revise que el contenedor/servicio esté arriba, que el puerto 1433 no esté ocupado, que la base exista y que `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER` y `DB_PASSWORD` sean correctos.

**Model buffalo_l download failed** — Compruebe Internet durante la primera carga, espacio en disco y permisos de la caché de InsightFace. No copie modelos gigantes al repositorio; vuelva a iniciar después de corregir la conectividad.

**InsightFace import error** — Confirme Python 3.11/3.12, `.venv` correcto y `pip install -r requirements.txt`. No use Python 3.13/3.14 como entorno oficial.

**Build de InsightFace** — `insightface==0.7.3` se publica como source distribution. En macOS instale Command Line Tools (`xcode-select --install`); en Windows/Linux instale las herramientas de compilación C/C++ requeridas por su Python si pip no puede construir el paquete.

**onnxruntime provider error** — Mantenga `ONNX_PROVIDERS=auto`. `CPUExecutionProvider` es obligatorio y no requiere GPU. Si se configuró una lista manual, use únicamente providers que `python -c "import onnxruntime as o; print(o.get_available_providers())"` muestre.

**Usuario no posee perfil facial** — Ejecute `python -m scripts.find_user <usuario>` y luego enrollment con `--username` o `--user-id`. El enrollment debe hacerse con la API iniciada.

**Embedding no puede descifrarse** — Restaure exactamente la `FACIAL_ENCRYPTION_KEY` usada al registrar el perfil. No genere una clave nueva en cada inicio. Si la clave se perdió, el perfil debe eliminarse y registrarse de nuevo mediante el flujo administrativo.

**Port 8765 already in use** — Ejecute `python -m scripts.doctor`, detenga el proceso que ocupa el puerto o cambie `FACIAL_PORT` y `FACIAL_API_URL`/la propiedad Java de forma coherente.

**macOS denied camera permission** — Abra `System Settings → Privacy & Security → Camera` y habilite Terminal, iTerm, IntelliJ o Python según el proceso que inicie la API. Cierre y vuelva a abrir la aplicación si macOS no aplica el permiso inmediatamente.

**IntelliJ `btnIngresar` null** — Abra y ejecute el proyecto en IntelliJ IDEA con Swing UI Designer e instrumentación de `.form` habilitada. VS Code no es el entorno principal para procesar los formularios visuales.

## Seguridad y privacidad

No se guardan JPG, PNG ni BMP. Los frames solo viven en memoria durante captura. El embedding se normaliza, cifra con AES-GCM y se persiste en SQL Server; la auditoría guarda metadatos y no imágenes. `.env`, contraseñas, `FACIAL_ENCRYPTION_KEY`, `.venv`, modelos y caches están excluidos por `.gitignore`.

No use `DB_PASSWORD=...` o `FACIAL_ENCRYPTION_KEY=...` reales en documentación, commits o capturas. Si una clave real fue expuesta, rótela y re-registre los perfiles después de planificar la migración.

## Limitaciones biométricas

Esta implementación usa webcam RGB. No equivale a Apple Face ID y no dispone de TrueDepth, IR, mapeo 3D, Secure Enclave ni anti-spoofing comercial certificado. El liveness activo es una mitigación académica básica. Es un demo moderno de integración biométrica, no un sistema de seguridad de alta garantía.

## Desarrollo

Los scripts resuelven sus rutas con la carpeta del propio archivo y no dependen del directorio actual. Los `.sh` usan LF y los `.bat`/`.ps1` tienen reglas de checkout apropiadas en `.gitattributes`. Si se clona en macOS/Linux y el bit ejecutable no se conserva:

```bash
chmod +x facial-service/*.sh
```

El parser JSON Java actual se mantiene deliberadamente sin dependencia adicional porque consume el contrato pequeño y estable de esta API local. No se envían passwords ni imágenes; si el contrato crece, debe reemplazarse por una librería JSON y pruebas de contrato antes de cambiarlo.

## Tests y validación

Pruebas Python sin webcam:

```bash
cd facial-service
source .venv/bin/activate  # Windows: .\.venv\Scripts\Activate.ps1
pytest
```

Validación Java:

```bash
mvn clean compile test package
```

La matriz de validación debe ejecutarse en cada equipo objetivo. No se declaran aquí pruebas físicas de Windows, macOS, Linux, webcam, enrollment o verify que no hayan sido ejecutadas en ese equipo. En particular, el entorno de preparación de este paquete debe confirmar en el Mac Apple Silicon de demostración: `uname -m`, instalación de wheels Python 3.12, ODBC, Docker SQL Server, permisos de cámara, descarga del modelo, doctor, enrollment y login completo.
