# 🐰 Conejo Jorge — Juego Móvil Android

![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)
![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![API Level](https://img.shields.io/badge/API_29%2B-blue?style=for-the-badge)

**Conejo Jorge** es un videojuego de acción para Android en el que controlas a un conejo que debe esquivar picos que caen desde arriba. ¡Sobrevive el mayor tiempo posible, acumula puntos y supera tu récord!

---

## 📱 Capturas de pantalla

> _El juego presenta una pantalla principal con el logo del juego y un botón de inicio, seguida del tablero de juego y una pantalla de Game Over con tu puntuación y el récord personal._

---

## 🎮 Mecánicas de juego

| Mecánica | Descripción |
|---|---|
| **Control del conejo** | Desliza el dedo en la parte inferior de la pantalla para mover al conejo horizontalmente |
| **Picos** | Caen desde la parte superior en posiciones y velocidades aleatorias (3 picos simultáneos) |
| **Puntuación** | Cada pico que toca el suelo sin golpear al conejo suma **+10 puntos** |
| **Vidas** | El jugador empieza con **3 vidas** (barra verde → amarilla → roja) |
| **Explosiones** | Cada vez que un pico llega al suelo se reproduce una animación de explosión |
| **Game Over** | Al perder las 3 vidas se navega a la pantalla de resultados |
| **Récord** | La puntuación más alta se guarda localmente con `SharedPreferences` |

---

## 🏗️ Arquitectura del proyecto

```
ConejoJorge/
├── app/
│   └── src/
│       └── main/
│           ├── java/com/example/conejojorge/
│           │   ├── MainActivity.kt       # Actividad principal / pantalla de inicio
│           │   ├── GameView.java         # Motor de juego (Canvas, lógica, colisiones)
│           │   ├── GameOver.java         # Pantalla de resultados y récord
│           │   ├── Spike.java            # Entidad obstáculo (pico animado)
│           │   └── Explosion.java        # Entidad de animación de explosión
│           ├── res/
│           │   ├── layout/
│           │   │   ├── activity_main.xml # UI pantalla de inicio
│           │   │   └── game_over.xml     # UI pantalla Game Over
│           │   └── drawable/
│           │       ├── rabbit.png        # Sprite del conejo
│           │       ├── spike0-2.png      # Frames animados del pico
│           │       ├── explode0-2.png    # Frames animados de la explosión
│           │       ├── background.png    # Fondo del escenario
│           │       ├── ground.png        # Suelo
│           │       └── ...              # Otros assets (título, iconos, botones)
│           └── AndroidManifest.xml
├── Documentacion/
│   └── Memoria-JuegoDelConejo.pdf       # Memoria técnica del proyecto
├── build.gradle.kts
└── settings.gradle.kts
```

### Clases principales

#### `MainActivity.kt`
Actividad de lanzamiento. Mantiene la pantalla encendida (`FLAG_KEEP_SCREEN_ON`) y carga la pantalla de inicio. Al pulsar el botón de jugar, sustituye el contenido de la actividad por un `GameView`.

#### `GameView.java`
Núcleo del juego. Extiende `View` y usa `Canvas` para renderizar todos los elementos a **~33 FPS** (ciclo de 30 ms vía `Handler`). Gestiona:
- Renderizado del fondo, suelo, conejo, picos y explosiones.
- Detección de colisiones entre picos y el conejo.
- Sistema de vidas con barra de salud con cambio de color.
- Eventos táctiles para mover al conejo.

#### `Spike.java`
Representa un pico animado con 4 frames (loop). Cada pico tiene posición X aleatoria y velocidad aleatoria. Se reinicia al tocar el suelo o al colisionar con el conejo.

#### `Explosion.java`
Animación de 3 frames que se muestra al impactar un pico contra el suelo.

#### `GameOver.java`
Actividad que recibe la puntuación final vía `Intent`, la muestra junto al récord histórico (guardado en `SharedPreferences`) y ofrece opciones de reiniciar o salir.

---

## ⚙️ Requisitos

- **Android Studio** Hedgehog o superior
- **Android SDK** API 34 (compileSdk)
- **Mínimo Android** API 29 (Android 10)
- **JDK** 8 o superior
- **Kotlin** 1.x (plugin `kotlin-android`)

---

## 🚀 Instalación y ejecución

### 1. Clona el repositorio
```bash
git clone <url-del-repositorio>
cd ConejoMobileApp
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
El APK generado se encontrará en `app/build/outputs/apk/release/`.

---

## 🧪 Tests

El proyecto incluye la estructura estándar de Android Testing:

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
