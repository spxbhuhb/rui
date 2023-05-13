## Placeholders

A placeholder is an anchor the fragment uses to add/remove its children. We cannot
just add and then replace fragments because it is possible that the selected
fragment is a block or a loop. Those add an unknown number of children, thus simple
replace is impossible. For browsers the placeholder may be a simple `Node`
(Svelte uses a `Text`), for Android an actual Placeholder view exists.

Placeholders are created by the `RuiAdapter.createPlaceholder` function.