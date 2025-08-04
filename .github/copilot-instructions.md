# Copilot Instructions for Visualizer

## Visão Geral
- Projeto Android multi-módulo: `app/` (demo) e `visualizer/` (biblioteca principal de visualização de áudio).
- Estrutura modular, separando lógica de visualização (pintores, utilitários, views customizadas) da aplicação demo.
- Código Kotlin, estilo oficial, com AndroidX e ViewBinding habilitados.

## Estrutura do Projeto
- `app/`: Aplicação demo. Código em `src/main/java/de/lemke/audiovisualizerdemo/`.
- `visualizer/`: Biblioteca. Código em `src/main/java/de/lemke/audiovisualizer/`.
  - `painters/`: Visualizações (FFT, waveform, efeitos, modificadores).
  - `utils/`: Utilitários (ex: `FrameManager.kt`, `Preset.kt`).
  - `views/`: Componentes customizados de UI.
- `preview/`: GIFs de demonstração.
- Arquivos de configuração: `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`.

## Workflows Essenciais (Android Studio)
- **Build limpo:**
  - Menu: `Build` > `Clean Project`
- **Build (debug/release):**
  - Menu: `Build` > `Make Project` ou `Build Bundle(s) / APK(s)`
  - Para release: `Build` > `Generate Signed Bundle / APK...`
- **Build da biblioteca:**
  - Clique direito no módulo `visualizer` > `Build Module 'visualizer'`
- **Testes:**
  - Menu: `Run` > `Run...` ou clique direito na classe de teste > `Run`
- **Lint:**
  - Menu: `Analyze` > `Inspect Code...` ou `Code` > `Analyze Code`

## Convenções e Padrões
- Siga o estilo oficial Kotlin (`kotlin.code.style=official`).
- Use ViewBinding para UI.
- Otimize para performance (meta: 50-60 fps, baixo uso de memória).
- ProGuard habilitado para release.
- Classe R não-transitiva (reduz tamanho do APK).
- Pacotes organizados por tipo de visualização/modificador.

## Integrações e Dependências
- AndroidX AppCompat, Apache Commons Math3, ViewBinding.
- SDK mínimo: 26, alvo: 36, compilação: 36. Java 17.

## Exemplos de Arquitetura
- Para adicionar um novo visualizador, crie uma classe em `visualizer/src/main/java/de/lemke/audiovisualizer/painters/` e implemente a interface `Painter.kt`.
- Utilitários e helpers devem ir para `utils/`.
- Componentes de UI customizados em `views/`.

## Dicas
- Sempre rode `./gradlew clean` após mudanças em dependências ou configurações.
- Use os GIFs em `preview/` para validar visualmente novas features.

Consulte `.kiro/steering/structure.md` e `.kiro/steering/tech.md` para detalhes avançados de arquitetura e stack.
