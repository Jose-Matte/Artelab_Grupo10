package com.duoc.artelab_grupo10.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.duoc.artelab_grupo10.R
import com.duoc.artelab_grupo10.navigation.Screen

/**
 * Pantalla principal/home de la aplicación
 *
 * Mantiene el diseño original con mejoras:
 * - TopAppBar con logo y botón de acceso
 * - Contenido central con imagen de obra
 * - Doble BottomBar con acciones (Like, Perfil, Agregar, Guardar)
 * - Usa MaterialTheme.colorScheme en lugar de colores hardcodeados
 * - Integrado con Navigation para navegar a perfil
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    userName: String = "Usuario",
    userId: Int = 1
) {
    // Altura de los botones del top bar
    val topBarButtonHeight = 56.dp

    Scaffold(
        // ========== BARRA SUPERIOR ==========
        topBar = {
            TopAppBar(
                // Logo + nombre de la app a la izquierda
                navigationIcon = {
                    Button(
                        onClick = { /* Acción del logo */ },
                        modifier = Modifier
                            .height(topBarButtonHeight)
                            .padding(start = 0.dp),
                        shape = RectangleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxHeight(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Logo
                            Image(
                                painter = painterResource(id = R.drawable.logo),
                                contentDescription = "Logo ArteLab",
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .aspectRatio(1f)
                                    .padding(end = 6.dp),
                                contentScale = ContentScale.Fit
                            )
                            Text("Panel ArteLab")
                        }
                    }
                },
                title = {},
                // Botón de acceso/perfil a la derecha
                actions = {
                    Button(
                        onClick = {
                            // Navegar a perfil
                            navController.navigate(Screen.Profile.createRoute(userId))
                        },
                        modifier = Modifier
                            .height(topBarButtonHeight)
                            .padding(end = 8.dp),
                        shape = RectangleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxHeight(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Acceder")
                            Spacer(modifier = Modifier.width(8.dp))
                            Image(
                                painter = painterResource(id = R.drawable.perfil),
                                contentDescription = "Avatar usuario",
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .aspectRatio(1f),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        // ========== BARRAS INFERIORES ==========
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                UpperActionBar(
                    onLikeClick = { /* Acción de like */ },
                    onProfileClick = {
                        navController.navigate(Screen.Profile.createRoute(userId))
                    },
                    userName = userName
                )
                LowerActionBar(
                    onAddClick = { /* Acción de agregar obra */ },
                    onSaveClick = { /* Acción de guardar */ }
                )
            }
        }
    ) { layoutPadding ->
        // ========== CONTENIDO CENTRAL ==========
        val contentPainter = painterResource(id = R.drawable.sample)

        // Calcular aspect ratio de la imagen
        val aspect = if (contentPainter.intrinsicSize.width > 0 &&
            contentPainter.intrinsicSize.height > 0
        ) {
            contentPainter.intrinsicSize.width / contentPainter.intrinsicSize.height
        } else {
            1f
        }

        Box(
            modifier = Modifier
                .padding(layoutPadding)
                .fillMaxSize()
        ) {
            Image(
                painter = contentPainter,
                contentDescription = "Imagen principal",
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(aspect),
                contentScale = ContentScale.Fit
            )
        }
    }
}

/**
 * Barra de acciones superior (sobre la barra inferior)
 * - Botón de Like (celeste)
 * - Botón de Perfil con nombre del usuario (negro)
 */
@Composable
private fun UpperActionBar(
    onLikeClick: () -> Unit,
    onProfileClick: () -> Unit,
    userName: String
) {
    BottomAppBar(
        modifier = Modifier.height(56.dp),
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 4.dp,
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ========== BOTÓN DE LIKE ==========
            Button(
                onClick = onLikeClick,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                shape = RectangleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.like),
                        contentDescription = "Icono like",
                        modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(1f),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Like",
                        fontSize = 23.sp
                    )
                }
            }

            // ========== BOTÓN DE PERFIL + NOMBRE ==========
            Button(
                onClick = onProfileClick,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                shape = RectangleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = userName,
                        fontSize = 18.sp
                    )
                    Image(
                        painter = painterResource(id = R.drawable.perfil),
                        contentDescription = "Avatar inferior",
                        modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(1f),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

/**
 * Barra de acciones inferior (la última de todas)
 * - Botón de Agregar (amarillo)
 * - Botón de Guardar (blanco)
 */
@Composable
private fun LowerActionBar(
    onAddClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    BottomAppBar(
        modifier = Modifier.height(56.dp),
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 4.dp,
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ========== BOTÓN DE AGREGAR ==========
            Button(
                onClick = onAddClick,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                shape = RectangleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.add),
                        contentDescription = "Icono agregar",
                        modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(1f),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            // ========== BOTÓN DE GUARDAR ==========
            Button(
                onClick = onSaveClick,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                shape = RectangleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.save),
                        contentDescription = "Icono guardar",
                        modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(1f),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}
