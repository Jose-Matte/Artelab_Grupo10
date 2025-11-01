# Quick Code Review Checklist

## Package Structure
- [ ] File is in the correct package
- [ ] Package follows Android conventions (ui.screens, ui.components, etc.)
- [ ] Theme files are only in ui.theme package

## Imports
- [ ] No unused imports
- [ ] Using correct Material3 imports
- [ ] Star imports avoided (unless justified)

## Composable Structure
- [ ] Functions follow naming convention (PascalCase)
- [ ] Stateless composables separate from stateful ones
- [ ] Preview functions provided
- [ ] Parameters ordered: modifiers, state, callbacks

## State Management
- [ ] State hoisted appropriately
- [ ] Using remember for expensive operations
- [ ] Using rememberSaveable for config changes
- [ ] derivedStateOf used for computed values
- [ ] StateFlow/State collected properly

## Side Effects
- [ ] LaunchedEffect for one-time operations
- [ ] DisposableEffect for cleanup needed
- [ ] No direct I/O or network calls in composable body
- [ ] Proper keys for LaunchedEffect dependencies

## Material Design 3
- [ ] Using MaterialTheme.colorScheme
- [ ] Using MaterialTheme.typography
- [ ] Using MaterialTheme.shapes
- [ ] No hardcoded Color() values
- [ ] No hardcoded text sizes

## Resources
- [ ] Strings extracted to strings.xml
- [ ] Colors defined in theme
- [ ] Dimensions in dimens.xml or named constants
- [ ] Images using vector drawables when possible

## Accessibility
- [ ] contentDescription provided for images
- [ ] Touch targets at least 48dp
- [ ] Semantic properties used
- [ ] Proper contrast ratios

## Performance
- [ ] Keys provided in LazyColumn/LazyRow
- [ ] No unnecessary recompositions
- [ ] Heavy operations in remember blocks
- [ ] Stable/Immutable annotations where needed

## Error Handling
- [ ] Error states defined
- [ ] Error UI implemented
- [ ] Try-catch for risky operations
- [ ] Null safety handled

## Navigation
- [ ] Navigation callbacks properly defined
- [ ] NavController not passed to composables
- [ ] Proper back stack handling

## Testing
- [ ] Unit tests for ViewModels
- [ ] Composable tests for UI
- [ ] Edge cases covered
- [ ] Error scenarios tested

## Security
- [ ] No hardcoded credentials
- [ ] Input validation present
- [ ] Permissions properly requested
- [ ] ProGuard rules defined

## Code Quality
- [ ] Functions under 50 lines
- [ ] Single responsibility principle
- [ ] Meaningful variable names
- [ ] Comments for complex logic
- [ ] No TODOs in production code

## Architecture
- [ ] MVVM pattern followed
- [ ] Business logic in ViewModel
- [ ] UI logic in Composables
- [ ] Repository pattern for data access
- [ ] Proper dependency injection
