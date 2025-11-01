# Kotlin & Jetpack Compose Best Practices

## Jetpack Compose Principles

### 1. State Hoisting
```kotlin
// BAD: State inside leaf composable
@Composable
fun Counter() {
    var count by remember { mutableStateOf(0) }
    Button(onClick = { count++ }) {
        Text("Count: $count")
    }
}

// GOOD: State hoisted
@Composable
fun Counter(count: Int, onIncrement: () -> Unit) {
    Button(onClick = onIncrement) {
        Text("Count: $count")
    }
}
```

### 2. Remember Expensive Operations
```kotlin
// BAD: Recreated on every recomposition
@Composable
fun MyScreen() {
    val data = expensiveCalculation()
}

// GOOD: Cached with remember
@Composable
fun MyScreen() {
    val data = remember { expensiveCalculation() }
}
```

### 3. Use Keys in Lists
```kotlin
// BAD: No keys
LazyColumn {
    items(items) { item ->
        ItemRow(item)
    }
}

// GOOD: With keys
LazyColumn {
    items(items, key = { it.id }) { item ->
        ItemRow(item)
    }
}
```

### 4. Avoid Side Effects in Composables
```kotlin
// BAD: Direct side effect
@Composable
fun MyScreen() {
    loadData() // Called on every recomposition!
}

// GOOD: Use LaunchedEffect
@Composable
fun MyScreen() {
    LaunchedEffect(Unit) {
        loadData()
    }
}
```

## Material Design 3 Implementation

### Using Theme Colors
```kotlin
// BAD: Hardcoded colors
Button(
    colors = ButtonDefaults.buttonColors(
        containerColor = Color(0xFF4FC3F7)
    )
) { }

// GOOD: Theme colors
Button(
    colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.primary
    )
) { }
```

### Typography
```kotlin
// BAD: Hardcoded text size
Text(
    text = "Title",
    fontSize = 24.sp,
    fontWeight = FontWeight.Bold
)

// GOOD: Theme typography
Text(
    text = "Title",
    style = MaterialTheme.typography.headlineMedium
)
```

## MVVM Architecture Pattern

### ViewModel
```kotlin
class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun onLikeClicked() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLiked = !it.isLiked) }
        }
    }
}

data class HomeUiState(
    val isLiked: Boolean = false,
    val userName: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)
```

### Screen Composable
```kotlin
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    HomeContent(
        uiState = uiState,
        onLikeClick = viewModel::onLikeClicked
    )
}

@Composable
private fun HomeContent(
    uiState: HomeUiState,
    onLikeClick: () -> Unit
) {
    // Stateless UI
}
```

## Navigation

### Using Navigation Compose
```kotlin
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onNavigateToProfile = {
                    navController.navigate("profile")
                }
            )
        }
        composable("profile") { ProfileScreen() }
    }
}
```

## Resource Externalization

### Strings
```xml
<!-- res/values/strings.xml -->
<resources>
    <string name="app_name">ArteLab</string>
    <string name="action_like">Like</string>
    <string name="action_save">Guardar</string>
    <string name="label_access">Acceder</string>
</resources>
```

```kotlin
// Usage
Text(stringResource(R.string.action_like))
```

### Colors
```kotlin
// ui/theme/Color.kt
val LightBlue = Color(0xFF4FC3F7)
val Yellow = Color(0xFFFFEB3B)

// ui/theme/Theme.kt
private val LightColorScheme = lightColorScheme(
    primary = LightBlue,
    secondary = Yellow,
    // ... other colors
)
```

### Dimensions
```xml
<!-- res/values/dimens.xml -->
<resources>
    <dimen name="app_bar_height">56dp</dimen>
    <dimen name="spacing_small">8dp</dimen>
    <dimen name="spacing_medium">16dp</dimen>
</resources>
```

## Performance Optimization

### 1. Stability
```kotlin
// Mark data classes as stable
@Immutable
data class User(val id: String, val name: String)

// Or use @Stable for mutable classes
@Stable
class MutableUser(var name: String)
```

### 2. Derived State
```kotlin
@Composable
fun SearchScreen(items: List<Item>, query: String) {
    // BAD: Filters on every recomposition
    val filtered = items.filter { it.name.contains(query) }

    // GOOD: Only recalculates when dependencies change
    val filtered by remember(items, query) {
        derivedStateOf { items.filter { it.name.contains(query) } }
    }
}
```

### 3. Avoid Allocations in Composables
```kotlin
// BAD: Creates new object on every recomposition
@Composable
fun MyButton(onClick: () -> Unit) {
    val colors = ButtonDefaults.buttonColors(/* ... */)
}

// GOOD: Create outside or remember
private val DefaultButtonColors = ButtonDefaults.buttonColors(/* ... */)

@Composable
fun MyButton(onClick: () -> Unit) {
    // Use pre-created colors
}
```

## Accessibility

### Content Descriptions
```kotlin
Image(
    painter = painterResource(R.drawable.logo),
    contentDescription = stringResource(R.string.logo_description),
    modifier = Modifier.size(48.dp)
)
```

### Semantic Properties
```kotlin
Button(
    onClick = { /* ... */ },
    modifier = Modifier.semantics {
        contentDescription = "Like this post"
        role = Role.Button
    }
) {
    Icon(Icons.Default.Favorite, contentDescription = null)
}
```

## Error Handling

### UI State with Errors
```kotlin
sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

@Composable
fun ContentScreen(uiState: UiState<Content>) {
    when (uiState) {
        is UiState.Loading -> LoadingIndicator()
        is UiState.Success -> ContentView(uiState.data)
        is UiState.Error -> ErrorMessage(uiState.message)
    }
}
```

## Testing

### Composable Testing
```kotlin
@Test
fun testButton_displaysCorrectText() {
    composeTestRule.setContent {
        MyButton(text = "Click me", onClick = {})
    }

    composeTestRule
        .onNodeWithText("Click me")
        .assertIsDisplayed()
        .performClick()
}
```

### ViewModel Testing
```kotlin
@Test
fun `when like button clicked, state updates`() = runTest {
    val viewModel = HomeViewModel()

    viewModel.onLikeClicked()

    val state = viewModel.uiState.value
    assertTrue(state.isLiked)
}
```
