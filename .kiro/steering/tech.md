# Stack Tecnológico

## Sistema de Build
- **Gradle**: Kotlin DSL (build.gradle.kts)
- **Android Gradle Plugin**: 8.9.1
- **Kotlin**: 2.1.0

## Plataforma e Compatibilidade
- **Plataforma Alvo**: Android
- **SDK Mínimo**: 26 (Android 8.0)
- **SDK Alvo**: 36
- **SDK de Compilação**: 36
- **Versão Java**: 17

## Dependências Principais
- **AndroidX AppCompat**: 1.7.0
- **Apache Commons Math3**: 3.6.1 (para operações matemáticas)
- **View Binding**: Habilitado para ambos os módulos

## Estrutura do Projeto
- Projeto Android multi-módulo
- **app**: Módulo da aplicação demo
- **visualizer**: Módulo da biblioteca principal

## Configuração de Build
- **Estilo de Código Kotlin**: Oficial
- **AndroidX**: Habilitado
- **Classe R Não-transitiva**: Habilitada para reduzir tamanho do APK
- **ProGuard**: Habilitado para builds de release com nível completo de símbolos

## Comandos Comuns

### Build
```bash
# Build limpo
./gradlew clean

# Build debug
./gradlew assembleDebug

# Build release
./gradlew assembleRelease

# Build da biblioteca
./gradlew :visualizer:build
```

### Testes e Análise
```bash
# Executar testes
./gradlew test

# Gerar relatório de lint
./gradlew lint
```

## Notas de Desenvolvimento
- Use ViewBinding para componentes de UI
- Siga o estilo oficial de código Kotlin
- Otimize para performance - alvo de 50-60 fps
- Considere uso de memória para dispositivos móveis