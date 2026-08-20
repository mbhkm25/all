# SANAD Financial Operator PoC

Android-native proof of concept for inspecting supported financial-app UI through Accessibility after explicit user opt-in.

## PoC 0.1 scope

- Android/Kotlin app scaffold
- AccessibilityService
- AccessibilityNodeInfo tree inspection
- Local bounded event log
- Arabic control screen
- No transaction execution
- No password/PIN capture or storage

## Validation target

First manual validation target: Al Busairi Mobile. The user enables the service, navigates manually through the app, then SANAD records the exposed UI tree for later deterministic adapter development.
