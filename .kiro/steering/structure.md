# Estrutura do Projeto

## Nível Raiz
```
├── app/                    # Módulo da aplicação demo
├── visualizer/             # Módulo da biblioteca principal
├── preview/                # GIFs de demonstração e mídia
├── .gradle/                # Cache do Gradle
├── .idea/                  # Configurações do IntelliJ/Android Studio
├── gradle/                 # Wrapper do Gradle
├── build.gradle.kts        # Configuração de build raiz
├── settings.gradle.kts     # Configuração de módulos
└── gradle.properties       # Configurações globais do Gradle
```

## Módulo App (`app/`)
Aplicação demo que demonstra a biblioteca visualizador:
```
app/
├── src/main/
│   ├── java/de/lemke/audiovisualizerdemo/  # Código fonte do app demo
│   ├── res/                                # Recursos Android
│   └── AndroidManifest.xml
├── build.gradle.kts        # Configuração de build do módulo app
└── proguard-rules.pro      # Configuração do ProGuard
```

## Biblioteca Visualizer (`visualizer/`)
Módulo da biblioteca principal com arquitetura modular:
```
visualizer/
├── src/main/java/de/lemke/audiovisualizer/
│   ├── painters/           # Implementações de visualização
│   │   ├── fft/           # Visualizações baseadas em FFT
│   │   ├── waveform/      # Visualizações de forma de onda
│   │   ├── misc/          # Efeitos diversos
│   │   ├── modifier/      # Modificadores de efeito
│   │   └── Painter.kt     # Interface base do painter
│   ├── utils/             # Classes utilitárias
│   │   ├── FrameManager.kt
│   │   ├── Preset.kt
│   │   └── VisualizerHelper.kt
│   └── views/             # Componentes de view customizados
│       └── VisualizerView.kt
├── build.gradle.kts       # Configuração de build da biblioteca
└── proguard-rules.pro     # Regras ProGuard da biblioteca
```

## Organização de Pacotes
- **Pacote Base**: `de.lemke.audiovisualizer`
- **Painters**: Componentes modulares de visualização organizados por tipo
- **Utils**: Classes auxiliares para gerenciamento de frames e processamento de áudio
- **Views**: Views Android customizadas para integração

## Convenções de Nomenclatura
- Seguir convenções padrão do Kotlin/Android
- Usar nomes descritivos para classes painter (ex: `FftBar`, `FftCircle`)
- Nomenclatura de parâmetros segue convenções específicas (ver README.md)
- Parâmetros baseados em proporção usam sufixo `R` (ex: `xR`, `yR`, `wR`, `hR`)

## Padrões de Arquitetura
- **Padrão Painter**: Componentes modulares de visualização
- **Arquitetura Baseada em View**: Views Android customizadas para integração fácil
- **Classes Utilitárias**: Funções auxiliares centralizadas
- **Separação de Responsabilidades**: Distinção clara entre app demo e biblioteca