 Teste da Visualização BarrasHRGB

## Implementação Concluída ✅

A nova visualização `BarrasHRGB` foi implementada com sucesso seguindo os padrões do projeto NextGenVisualizer.

### Características Implementadas:

1. **Barras Horizontais Coloridas**: 24 barras centralizadas com cores do arco-íris
2. **Suavização Exponencial**: Mistura dados novos com anteriores para transições suaves
3. **Modelo de Gravidade**: Usa o sistema de gravidade do projeto para efeitos naturais
4. **Interpolação Spline**: Suaviza os dados FFT para melhor qualidade visual
5. **Otimização de Performance**: Pula frames quando áudio está silencioso
6. **Cores Vibrantes**: Sistema HSV para cores do arco-íris distribuídas uniformemente

### Parâmetros Configuráveis:

- `numberOfBars`: 24 barras (padrão)
- `barWidth`: 10f pixels de largura
- `barMargin`: 50f pixels de espaçamento
- `amplificationFactor`: 1.4f para enfatizar altura
- `smoothingFactor`: 0.2f para suavização
- `startHz/endHz`: 0-2000Hz de frequência

### Integração:

- ✅ Arquivo criado: `visualizer/src/main/java/de/lemke/audiovisualizer/painters/fft/BarrasHRGB.kt`
- ✅ Adicionado ao MainActivity como primeira visualização da lista
- ✅ Segue padrões do projeto (Kotlin, Paint, Painter base class)
- ✅ Usa sistema de gravidade e interpolação existentes

### Como Testar:

1. Execute o app
2. A visualização BarrasHRGB será a primeira a aparecer
3. Use os botões "Next" e "Previous" para navegar entre visualizações
4. Toque música ou fale no microfone para ver as barras reagirem

### Diferenças do Código Original:

- Convertido de Java para Kotlin
- Seguiu padrões de nomenclatura do projeto
- Adicionou comentários em português
- Integrou com sistema de otimização existente (skipFrame)
- Usa enums e constantes do projeto (Interpolator.SPLINE)