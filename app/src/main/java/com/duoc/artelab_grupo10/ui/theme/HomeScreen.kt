package com.duoc.artelab_grupo10.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.duoc.artelab_grupo10.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainView() {
    // altura que va a usar el botón del top para que el logo se vea grande
    val topBarButtonHeight = 56.dp

    Scaffold(
        // Barra de arriba (logo a la izquierda y botón de sesión a la derecha)
        topBar = {
            TopAppBar(
                // Botón que muestra el logo y el nombre de la app
                navigationIcon = {
                    Button(
                        onClick = { /* acá iría la acción del logo, por ahora nada */ },
                        modifier = Modifier
                            .height(topBarButtonHeight)
                            .padding(start = 0.dp),
                        // sin puntas redondeadas
                        shape = RectangleShape,
                        // fondo blanco, texto negro
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        // un poco de aire a los lados
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        // todo el contenido del botón va alineado al centro verticalmente
                        Row(
                            modifier = Modifier.fillMaxHeight(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // LOGO: ocupa todo el alto del botón
                            Image(
                                painter = painterResource(id = R.drawable.logo),
                                contentDescription = "Logo principal",
                                modifier = Modifier
                                    .fillMaxHeight()   // con esto ya crece
                                    .aspectRatio(1f)   // lo mantiene cuadrado
                                    .padding(end = 6.dp), // espacio entre logo y texto
                                contentScale = ContentScale.Fit
                            )
                            // texto al lado del logo
                            Text("Panel ArteLab")
                        }
                    }
                },
                // no queremos título en el centro
                title = {},
                // Botón de la derecha (podría ser login)
                actions = {
                    Button(
                        onClick = { /* acción de iniciar sesión o perfil */ },
                        modifier = Modifier
                            .height(topBarButtonHeight)
                            .padding(end = 8.dp),
                        shape = RectangleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxHeight(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Acceder")
                            Spacer(modifier = Modifier.width(8.dp))
                            // avatar del usuario
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
                }
            )
        },
        // Abajo vamos a poner dos barras, una sobre otra
        bottomBar = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                UpperActionBar()   // primera barra
                LowerActionBar()   // segunda barra
            }
        }
    ) { layoutPadding ->

        // ====== CONTENIDO CENTRAL ======
        // acá cargamos la imagen que quieras mostrar en el centro
        val contentPainter = painterResource(id = R.drawable.sample)

        // calculamos la proporción ancho/alto de la imagen para no cortarla
        val aspect =
            if (contentPainter.intrinsicSize.width > 0 && contentPainter.intrinsicSize.height > 0) {
                contentPainter.intrinsicSize.width / contentPainter.intrinsicSize.height
            } else {
                1f // si no se pudo calcular, dejamos 1:1
            }

        Box(
            modifier = Modifier
                .padding(layoutPadding) // respeta el espacio que dejan la top bar y las bottom
                .fillMaxSize()
        ) {
            // mostramos la imagen usando su relación de aspecto
            Image(
                painter = contentPainter,
                contentDescription = "Imagen principal",
                modifier = Modifier
                    .fillMaxWidth()       // ocupa todo el ancho
                    .aspectRatio(aspect), // alto se calcula según la imagen
                // con FIT no se corta la imagen
                contentScale = ContentScale.Fit
            )
        }
    }
}

/**
 * Barra de acciones de arriba (la que está justo sobre la otra).
 * Aquí pusimos:
 * - botón azul con icono like y texto
 * - botón negro con texto "Nombre" e imagen de perfil a la derecha
 */
@Composable
private fun UpperActionBar() {
    BottomAppBar(
        modifier = Modifier.height(56.dp),
        containerColor = Color(0xFFF5F5F5), // gris claro
        tonalElevation = 4.dp,
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ---------- BOTÓN 1: LIKE ----------
            Button(
                onClick = { /* acá iría la acción de like */ },
                modifier = Modifier
                    .weight(1f)        // ocupa la mitad
                    .fillMaxHeight(),  // ocupa todo el alto de la barra
                shape = RectangleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4FC3F7), // celeste
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // icono de like a la izquierda
                    Image(
                        painter = painterResource(id = R.drawable.like),
                        contentDescription = "Icono like",
                        modifier = Modifier
                            .fillMaxHeight()
                            .aspectRatio(1f),
                        contentScale = ContentScale.Fit
                    )
                    // un pequeño espacio
                    Spacer(modifier = Modifier.width(6.dp))
                    // texto grande
                    Text(
                        text = "Like",
                        color = Color.White,
                        fontSize = 23.sp
                    )
                }
            }

            // ---------- BOTÓN 2: PERFIL + NOMBRE ----------
            Button(
                onClick = { /* acá puede ir el perfil del usuario */ },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                shape = RectangleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // este spacer empuja el contenido hacia la derecha
                    Spacer(modifier = Modifier.weight(1f))
                    // texto que va a la izquierda de la imagen
                    Text(
                        text = "Nombre",
                        color = Color.White,
                        fontSize = 18.sp
                    )
                    // imagen de perfil pegada a la derecha
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
 * Barra de acciones de abajo (la última de todas).
 * Aquí pusimos:
 * - botón amarillo de "agregar"
 * - botón blanco de "guardar"
 */
@Composable
private fun LowerActionBar() {
    BottomAppBar(
        modifier = Modifier.height(56.dp),
        containerColor = Color(0xFFF5F5F5),
        tonalElevation = 4.dp,
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ---------- BOTÓN DE AGREGAR ----------
            Button(
                onClick = { /* acción para agregar algo */ },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                shape = RectangleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFEB3B), // amarillo
                    contentColor = Color.Black
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                // centramos solo el ícono
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

            // ---------- BOTÓN DE GUARDAR ----------
            Button(
                onClick = { /* acción para guardar */ },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                shape = RectangleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
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

@Preview(showBackground = true)
@Composable
fun MainViewPreview() {
    MainView()
}
