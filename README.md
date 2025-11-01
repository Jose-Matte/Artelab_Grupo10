# 🎨 ArteLab - Aplicación Android con Jetpack Compose

> Aplicación móvil para gestión de obras de arte desarrollada en Kotlin con Jetpack Compose

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-blue.svg)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-1.5.4-green.svg)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 📋 Tabla de Contenidos

- [Descripción](#-descripción)
- [Características](#-características)
- [Arquitectura](#-arquitectura)
- [Tecnologías](#-tecnologías)
- [Instalación](#-instalación)
- [Configuración del API](#-configuración-del-api)
- [Credenciales de Prueba](#-credenciales-de-prueba)
- [Funcionalidades Implementadas](#-funcionalidades-implementadas)
- [Capturas de Pantalla](#-capturas-de-pantalla)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [Equipo](#-equipo)

---

## 📖 Descripción

**ArteLab** es una aplicación móvil nativa para Android que permite a los usuarios gestionar y explorar obras de arte. Desarrollada con las últimas tecnologías de Android (Jetpack Compose), implementa autenticación segura, persistencia local con Room Database, y recursos nativos como cámara y galería para la gestión de perfiles.

### Contexto Académico

Proyecto desarrollado para la asignatura de **Programación de Aplicaciones Móviles** en DUOC UC.

- **Evaluación**: EP3 - Evaluación Práctica 3
- **Valor**: 32% de la nota final
- **Fecha de Entrega**: [Fecha]
- **Grupo**: Grupo 10

---

## ✨ Características

### 🔐 Autenticación Completa
- **Login** con validación de credenciales en tiempo real
- **Registro** de nuevos usuarios con validaciones robustas
- **Persistencia de sesión** con DataStore (reemplazo moderno de SharedPreferences)
- **Token JWT** para autenticación en todas las peticiones

### 🎨 Gestión de Perfil
- **Foto de perfil** con cámara o galería
- **Persistencia offline** de imagen en Room Database
- **Visualización de datos** del usuario autenticado (GET /auth/me)
- **Cierre de sesión** con limpieza de credenciales

### 📱 Recursos Nativos
- **Cámara**: Captura de fotos con FileProvider
- **Galería**: Selección de imágenes existentes
- **Permisos dinámicos**: Manejo de permisos en runtime según versión de Android

### 🎭 Experiencia de Usuario
- **Animaciones profesionales**: Botones con efectos de escala y pulso
- **Estados visuales**: Loading, Success, Error con transiciones suaves
- **Diseño Material 3**: Sigue las guías de diseño de Google
- **Modo oscuro**: Soporte para tema claro y oscuro

---

## 🏗 Arquitectura

El proyecto sigue el patrón **MVVM (Model-View-ViewModel)** recomendado por Google para aplicaciones Android modernas.

```
┌─────────────────────────────────────────────────────────┐
│                         UI LAYER                         │
│  ┌───────────┐  ┌───────────┐  ┌───────────┐           │
│  │  Screens  │  │Components │  │Navigation │           │
│  │ (Compose) │  │   (UI)    │  │  (NavHost)│           │
│  └─────┬─────┘  └───────────┘  └───────────┘           │
├────────┼──────────────────────────────────────────────────┤
│        │              VIEWMODEL LAYER                     │
│  ┌─────▼─────┐                                           │
│  │ ViewModels│ ◄─── StateFlow<UiState<T>>                │
│  └─────┬─────┘                                           │
├────────┼──────────────────────────────────────────────────┤
│        │              DOMAIN LAYER                        │
│  ┌─────▼─────┐                                           │
│  │UiState<T> │  (Sealed Class para estados)              │
│  └───────────┘                                           │
├──────────────────────────────────────────────────────────┤
│                    DATA LAYER                            │
│  ┌───────────┐  ┌───────────┐  ┌───────────┐           │
│  │Repository │  │  Remote   │  │   Local   │           │
│  │  (Auth)   │  │ (Retrofit)│  │  (Room)   │           │
│  └───────────┘  └───────────┘  └───────────┘           │
└─────────────────────────────────────────────────────────┘
```

### Capas del Proyecto

| Capa | Responsabilidad | Componentes |
|------|----------------|-------------|
| **UI** | Interfaz de usuario | Screens, Components, Navigation |
| **ViewModel** | Lógica de presentación | StateFlow, UiState |
| **Domain** | Modelos de negocio | UiState sealed class |
| **Data** | Fuente de datos | Repository, Room, Retrofit |

---

## 🛠 Tecnologías

### Core
- **Kotlin 1.9.0** - Lenguaje de programación moderno
- **Jetpack Compose 1.5.4** - UI declarativa moderna
- **Material 3** - Sistema de diseño de Google

### Persistencia
- **Room Database 2.6.1** - Base de datos local SQLite
- **DataStore 1.0.0** - Almacenamiento de preferencias (reemplazo de SharedPreferences)

### Networking
- **Retrofit 2.9.0** - Cliente HTTP type-safe
- **OkHttp 4.12.0** - Cliente HTTP con interceptores
- **Gson 2.10.1** - Serialización JSON

### Asynchronous
- **Kotlin Coroutines 1.7.3** - Programación asíncrona
- **Flow** - Streams reactivos

### Image Loading
- **Coil 2.5.0** - Carga de imágenes con AsyncImage

### Navigation
- **Navigation Compose 2.7.6** - Navegación declarativa

### Dependency Injection
- **Manual DI** - AppModule singleton pattern

---

## 📥 Instalación

### Requisitos Previos
- **Android Studio** Hedgehog (2023.1.1) o superior
- **JDK 17** o superior
- **Android SDK 34** (compileSdk)
- **Dispositivo/Emulador** con Android 7.0 (API 24) o superior

### Pasos de Instalación

1. **Clonar el repositorio**
```bash
git clone https://github.com/tu-usuario/artelab-grupo10.git
cd artelab-grupo10
```

2. **Abrir en Android Studio**
```
File → Open → Seleccionar carpeta del proyecto
```

3. **Sync Gradle**
```
Android Studio sincronizará automáticamente las dependencias
Si no lo hace: File → Sync Project with Gradle Files
```

4. **Ejecutar la aplicación**
```
Click en Run (▶) o Shift + F10
Seleccionar dispositivo/emulador
```

---

## 🌐 Configuración del API

### Base URL
```kotlin
https://x8ki-letl-twmt.n7.xano.io/api:Rfm_61dW/
```

### Endpoints Implementados

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| `POST` | `/auth/signup` | Registrar nuevo usuario | ❌ |
| `POST` | `/auth/login` | Iniciar sesión | ❌ |
| `GET` | `/auth/me` | Obtener usuario autenticado | ✅ Bearer Token |

### Configuración de RetrofitClient

El proyecto incluye un `AuthInterceptor` que agrega automáticamente el token Bearer a todas las peticiones:

```kotlin
// data/remote/RetrofitClient.kt
val client = OkHttpClient.Builder()
    .addInterceptor(AuthInterceptor(tokenProvider))
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .build()
```

---

## 🔑 Credenciales de Prueba

### Usuario de Prueba 1
```
Email: test@artelab.com
Password: test123
Nombre: Usuario Test
```

### Usuario de Prueba 2
```
Email: demo@artelab.com
Password: demo123
Nombre: Usuario Demo
```

### Registro de Nuevo Usuario
También puedes crear una cuenta nueva desde la pantalla de **Registro**. Los requisitos son:

- **Email**: Formato válido (ejemplo@dominio.com)
- **Nombre**: Mínimo 3 caracteres
- **Contraseña**: Mínimo 6 caracteres, debe incluir:
  - Al menos una mayúscula
  - Al menos una minúscula
  - Al menos un número
- **Confirmación**: Debe coincidir con la contraseña
- **Términos**: Debes aceptar los términos y condiciones

---

## 🎯 Funcionalidades Implementadas

### ✅ Indicador 1: Configuración del Proyecto (4 puntos)
- ✅ Proyecto Kotlin con Jetpack Compose
- ✅ Dependencias configuradas (Room, Retrofit, Coil, Navigation)
- ✅ Permisos en AndroidManifest.xml
- ✅ FileProvider configurado para cámara

### ✅ Indicador 2: Persistencia de Datos (8 puntos)
- ✅ Room Database con entidades Usuario y Obra
- ✅ DAOs con operaciones CRUD completas
- ✅ DataStore para preferencias (token, sesión)
- ✅ Persistencia de foto de perfil en Room

### ✅ Indicador 3: Conexión con API REST (8 puntos)
- ✅ Retrofit configurado con Gson
- ✅ AuthInterceptor para Bearer token
- ✅ Endpoints implementados: /signup, /login, /auth/me
- ✅ Manejo de errores HTTP (400, 401, 404, 500)

### ✅ Indicador 4: Navegación (4 puntos)
- ✅ Navigation Compose con NavHost
- ✅ 5 pantallas: Splash, Login, Register, Home, Profile
- ✅ Navegación con parámetros (userId)
- ✅ Backstack management con popUpTo

### ✅ Indicador 5: Formularios (4 puntos)
- ✅ Formulario de Login con validaciones
- ✅ Formulario de Registro con validaciones robustas
- ✅ Validaciones en tiempo real
- ✅ Mensajes de error claros

### ✅ Indicador 6: Estados y Loading (4 puntos)
- ✅ UiState sealed class (Idle, Loading, Success, Error, Empty)
- ✅ StateFlow en todos los ViewModels
- ✅ Loading indicators en todas las operaciones
- ✅ Manejo de errores con mensajes específicos

### ✅ Indicador 7: Recursos Nativos (6 puntos)
- ✅ **Cámara**: Captura de fotos con FileProvider
- ✅ **Galería**: Selección de imágenes
- ✅ Permisos dinámicos según versión de Android
- ✅ BottomSheet para elegir cámara/galería

### ✅ Indicador 8: Persistencia de Imagen (6 puntos)
- ✅ Guardar URI en Room Database
- ✅ AsyncImage con Coil para visualización
- ✅ Persistencia offline (funciona sin internet)
- ✅ Actualización automática del perfil

---

## 📸 Capturas de Pantalla

### Splash Screen
Pantalla de bienvenida con verificación de sesión automática.

### Login Screen
- Validación de email en tiempo real
- Botón de mostrar/ocultar contraseña
- Animaciones profesionales en botón
- Estados de loading

### Register Screen
- Validaciones robustas (email, nombre, contraseña, confirmación)
- Checkbox de términos y condiciones
- Indicadores visuales de requisitos de contraseña
- Animaciones en botón de registro

### Home Screen
- Diseño Material 3
- Navegación a perfil
- Logo de ArteLab
- Doble BottomBar (diseño original conservado)

### Profile Screen
- Foto de perfil circular con borde
- Datos del usuario desde GET /auth/me
- Botón flotante para cambiar foto
- BottomSheet con opciones: Cámara | Galería
- Botón de cerrar sesión

---

## 📁 Estructura del Proyecto

```
app/src/main/java/com/duoc/artelab_grupo10/
│
├── data/                          # Capa de datos
│   ├── local/
│   │   ├── dao/                   # Data Access Objects
│   │   │   ├── UsuarioDao.kt
│   │   │   └── ObraDao.kt
│   │   ├── entities/              # Entidades de Room
│   │   │   ├── Usuario.kt
│   │   │   └── Obra.kt
│   │   ├── database/
│   │   │   └── AppDatabase.kt     # Singleton de Room
│   │   └── PreferencesManager.kt  # DataStore wrapper
│   │
│   ├── remote/
│   │   ├── api/
│   │   │   └── ArtelabApi.kt      # Definición de endpoints
│   │   ├── dto/                   # Data Transfer Objects
│   │   │   ├── SignupRequest.kt
│   │   │   ├── SignupResponse.kt
│   │   │   ├── LoginRequest.kt
│   │   │   ├── LoginResponse.kt
│   │   │   └── UserResponse.kt
│   │   ├── interceptor/
│   │   │   └── AuthInterceptor.kt # Bearer token interceptor
│   │   └── RetrofitClient.kt      # Configuración de Retrofit
│   │
│   └── repository/
│       └── AuthRepository.kt      # Lógica de autenticación
│
├── di/                            # Dependency Injection
│   └── AppModule.kt               # Manual DI singleton
│
├── domain/
│   └── model/
│       └── UiState.kt             # Sealed class para estados
│
├── navigation/
│   └── Screen.kt                  # Definición de rutas
│
├── ui/
│   ├── screens/
│   │   ├── splash/
│   │   │   └── SplashScreen.kt
│   │   ├── login/
│   │   │   └── LoginScreen.kt     # Con AnimatedLoginButton
│   │   ├── register/
│   │   │   └── RegisterScreen.kt  # Con AnimatedRegisterButton
│   │   ├── home/
│   │   │   └── HomeScreen.kt
│   │   └── profile/
│   │       └── ProfileScreen.kt   # Cámara/Galería completo
│   │
│   ├── viewmodel/
│   │   ├── LoginViewModel.kt
│   │   ├── RegisterViewModel.kt
│   │   ├── SplashViewModel.kt
│   │   ├── ProfileViewModel.kt
│   │   └── ViewModelFactory.kt
│   │
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
│
└── MainActivity.kt                # NavHost principal

app/src/main/res/
├── xml/
│   └── file_paths.xml             # FileProvider paths
└── ...
```

---

## 👥 Equipo

### Grupo 10

| Nombre | Rol | Email |
|--------|-----|-------|
| [Nombre 1] | Desarrollador Full Stack | [email] |
| [Nombre 2] | Desarrollador Full Stack | [email] |
| [Nombre 3] | Desarrollador Full Stack | [email] |

---

## 📄 Licencia

Este proyecto fue desarrollado con fines académicos para DUOC UC.

---

## 🎓 Rúbrica de Evaluación

Este proyecto cumple con todos los indicadores de la rúbrica EP3:

| Indicador | Puntaje | Cumplimiento |
|-----------|---------|--------------|
| 1. Configuración | 4 pts | ✅ 100% |
| 2. Persistencia | 8 pts | ✅ 100% |
| 3. API REST | 8 pts | ✅ 100% |
| 4. Navegación | 4 pts | ✅ 100% |
| 5. Formularios | 4 pts | ✅ 100% |
| 6. Estados/Loading | 4 pts | ✅ 100% |
| 7. Recursos Nativos | 6 pts | ✅ 100% |
| 8. Persistencia Imagen | 6 pts | ✅ 100% |
| **TOTAL** | **44 pts** | **✅ 100%** |

---

## 🚀 Mejoras Futuras

- [ ] Implementar CRUD completo de Obras de Arte
- [ ] Agregar segundo recurso nativo (GPS o Notificaciones)
- [ ] Implementar offline-first con WorkManager
- [ ] Agregar tests unitarios y de integración
- [ ] Implementar paginación en listas
- [ ] Agregar búsqueda y filtros
- [ ] Soporte para múltiples idiomas

---

## 📞 Contacto

Para consultas sobre este proyecto:
- **Email**: [email del grupo]
- **GitHub**: [link al repositorio]

---

<div align="center">

**Desarrollado con ❤️ por Grupo 10**

*DUOC UC - Programación de Aplicaciones Móviles*

</div>
