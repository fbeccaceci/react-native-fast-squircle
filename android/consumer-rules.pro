# Preserve the React Native drawable internals that FastSquircle accesses
# via reflection. R8 can rename or remove these members in release builds,
# which breaks the reflective lookups.
-keepclassmembers class com.facebook.react.uimanager.drawable.BackgroundDrawable {
    *;
}

-keepclassmembers class com.facebook.react.uimanager.drawable.BorderDrawable {
    *;
}

-keepclassmembers class com.facebook.react.uimanager.drawable.OutlineDrawable {
    *;
}

-keepclassmembers class com.facebook.react.uimanager.drawable.OutsetBoxShadowDrawable {
    *;
}

-keepclassmembers class com.facebook.react.uimanager.drawable.BackgroundImageDrawable {
    *;
}
