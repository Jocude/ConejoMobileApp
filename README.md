# 🐰 Conejo Jorge — Juego Móvil Android

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![API Level](https://img.shields.io/badge/API_29%2B-blue?style=for-the-badge)

**Conejo Jorge** es un videojuego de acción para Android en el que controlas a un conejo que debe esquivar picos que caen desde arriba. ¡Sobrevive el mayor tiempo posible, acumula puntos y supera tu récord!

---

## 📥 Descarga

Descarga el APK desde la sección [**Releases**](https://github.com/Jocude/ConejoMobileApp/releases) e instálalo en tu móvil (Android 10 o superior).

---

## 📱 Capturas de pantalla

> _El juego presenta una pantalla principal con el logo del juego y un botón de inicio, seguida del tablero de juego y una pantalla de Game Over con tu puntuación y el récord personal._

---

## 🎮 Mecánicas de juego

| Mecánica | Descripción |
|---|---|
| **Control del conejo** | Desliza el dedo en la parte inferior de la pantalla para mover al conejo horizontalmente |
| **Picos** | Caen desde la parte superior en posiciones y velocidades aleatorias (3 picos simultáneos; cada uno tarda entre 1,5 y 3 s en cruzar la pantalla, igual en cualquier móvil) |
| **Puntuación** | Cada pico que toca el suelo sin golpear al conejo suma **+10 puntos** |
| **Vidas** | El jugador empieza con **3 vidas** (barra verde → amarilla → roja) |
| **Explosiones** | Cada vez que un pico llega al suelo se reproduce una animación de explosión |
| **Game Over** | Al perder las 3 vidas se navega a la pantalla de resultados |
| **Récord** | La puntuación más alta se guarda localmente con `SharedPreferences` |
| **Pausa** | Si sales de la app, la partida se congela y continúa al volver |

---

## 🏗️ Arquitectura del proyecto

```
ConejoMobileApp/
├── ConejoJorge/                          # Proyecto Android (Gradle)
│   ├── app/src/
│   │   ├── main/
│   │   │   ├── java/com/example/conejojorge/
│   │   │   │   ├── MainActivity.kt       # Pantalla de inicio; aloja la partida y la pausa/reanuda
│   │   │   │   ├── GameView.kt           # Motor del juego (bucle, lógica, dibujo y control táctil)
│   │   │   │   ├── GameSprites.kt        # Imágenes del juego, cargadas una sola vez
│   │   │   │   ├── Spike.kt              # Pico: posición, velocidad, animación y colisión
│   │   │   │   ├── Explosion.kt          # Animación de explosión
│   │   │   │   └── GameOver.kt           # Pantalla de resultados y récord
│   │   │   ├── res/
│   │   │   │   ├── layout/               # activity_main.xml, game_over.xml
│   │   │   │   └── drawable/             # Sprites: conejo, picos, explosiones, fondo, suelo…
│   │   │   └── AndroidManifest.xml
│   │   └── test/.../SpikeTest.kt         # Tests de velocidad y colisiones
│   ├── build.gradle.kts
│   └── settings.gradle.kts
└── Documentacion/
    └── Memoria-JuegoDelConejo.pdf        # Memoria técnica del proyecto
```

### Clases principales

#### `MainActivity.kt`
Actividad de lanzamiento. Mantiene la pantalla encendida (`FLAG_KEEP_SCREEN_ON`) y muestra la pantalla de inicio. Al pulsar el botón de jugar, sustituye el contenido por un `GameView` y lo pausa/reanuda en `onPause`/`onResume`.

#### `GameView.kt`
Núcleo del juego. Extiende `View` y usa `Canvas`. El bucle va con `Choreographer`, sincronizado con el refresco de la pantalla, y en cada frame:
- **Actualiza** la lógica según el tiempo real transcurrido (misma velocidad en cualquier móvil).
- **Dibuja** fondo, suelo, conejo, picos, explosiones, puntuación y barra de vida.

Además detecta las colisiones, gestiona las vidas y el control táctil, y respeta las barras del sistema (el suelo queda por encima de la barra de navegación).

#### `GameSprites.kt`
Carga todas las imágenes una sola vez por partida; las comparten todos los picos y explosiones.

#### `Spike.kt`
Pico animado de 3 frames con posición X y velocidad aleatorias. Se reinicia al tocar el suelo o al conejo. Incluye `spikeHitsRabbit`, que comprueba todo el tramo recorrido en el frame para que un pico rápido no atraviese al conejo.

#### `Explosion.kt`
Animación de 3 frames que se muestra al impactar un pico contra el suelo.

#### `GameOver.kt`
Actividad que recibe la puntuación final vía `Intent`, la muestra junto al récord (guardado en `SharedPreferences`), avisa si es un nuevo récord y ofrece reiniciar o salir.

---

## ⚙️ Requisitos

- **Android Studio** Hedgehog o superior
- **Android SDK** API 34 (compileSdk)
- **Mínimo Android** API 29 (Android 10)
- **JDK** 17
- **Kotlin** 1.9 (plugin `kotlin-android`)

---

## 🚀 Instalación y ejecución

### 1. Clona el repositorio
```bash
git clone https://github.com/Jocude/ConejoMobileApp.git
cd ConejoMobileApp/ConejoJorge
```

### 2. Abre el proyecto en Android Studio
- Selecciona **File → Open** y navega hasta la carpeta `ConejoJorge/`.
- Android Studio sincronizará automáticamente Gradle.

### 3. Ejecuta la aplicación
- Conecta un dispositivo físico Android (API 29+) o crea un emulador desde el **AVD Manager**.
- Pulsa el botón ▶ **Run 'app'** o usa el atajo `Shift + F10`.

### 4. Generar APK de release
```bash
./gradlew assembleRelease
```
El APK generado se encontrará en `app/build/outputs/apk/release/`. Los APK no se guardan en el repositorio: se publican en [Releases](https://github.com/Jocude/ConejoMobileApp/releases).

---

## 🧪 Tests

`SpikeTest` comprueba el rango de velocidades de los picos y la detección de colisiones:

```bash
# Tests unitarios (JUnit)
./gradlew test

# Tests de instrumentación (Espresso)
./gradlew connectedAndroidTest
```

---

## 📦 Dependencias

| Librería | Uso |
|---|---|
| `androidx.core:core-ktx` | Extensiones de Kotlin para Android |
| `androidx.appcompat:appcompat` | Compatibilidad de Activities |
| `com.google.android.material:material` | Componentes Material Design |
| `androidx.activity:activity` | Ciclo de vida de actividades |
| `androidx.constraintlayout:constraintlayout` | Layouts de UI |
| `junit:junit` | Tests unitarios |
| `androidx.test.ext:junit` | Extensión JUnit para Android |
| `androidx.test.espresso:espresso-core` | Tests de UI |

---

## 📄 Documentación

La memoria técnica completa del proyecto se encuentra en:

```
Documentacion/Memoria-JuegoDelConejo.pdf
```

---

## 👨‍💻 Autor

**Jorge** — Proyecto académico de desarrollo de videojuegos móviles para Android.

---

## 📝 Licencia

Este proyecto es de uso académico/educativo. Todos los derechos sobre los assets gráficos pertenecen a sus respectivos autores.
