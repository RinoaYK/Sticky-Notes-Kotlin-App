# Sticky Notes 📝

Um aplicativo Android moderno e altamente personalizável que transforma sua tela inicial em um mural de post-its digitais.

Crie lembretes visuais incríveis com cores vibrantes, widgets dinâmicos e uma vasta biblioteca de stickers — tudo com uma experiência fluida e otimizada para performance.

## ✨ Principais Funcionalidades

- **Personalização de Design**  
  Controle total sobre cor de fundo (via Color Picker AmbilWarna), cor do texto e tamanho da fonte.

- **Biblioteca de Stickers Expandida**  
  Agora com **6 coleções integradas** e carregamento otimizado via TypedArray:

  - 🐙 Takopi  
  - 🐱 Mitao Cat  
  - 🐛 Bugcat Capoo  
  - ฅ^._.^ฅ💣 Exploding Kittens  
  - ₍ᐢ- ̫ -ᐢ₎ Kapibara San
  - 🐑 Cult of the Lamb
    

  > **Destaque:** Apenas a coleção Bugcat Capoo já conta com **+93 stickers**!

- **Widgets Inteligentes**  
  Formatos 4×1 e 5×2 com atualização em tempo real e suporte a múltiplos widgets simultâneos.

- **Layout Adaptável**  
  Alinhamento flexível (Esquerda / Direita) para stickers e textos, refletido instantaneamente no widget.

- **Acessibilidade Nativa**  
  Suporte completo ao TalkBack com contentDescriptions e otimização de navegação.

- **Performance de Elite**  
  Uso de Glide + ImageDecoder para garantir widgets leves e baixo consumo de bateria.

## 🛠️ Tecnologias & Arquitetura

O projeto segue as melhores práticas modernas de desenvolvimento Android:

- **Arquitetura** → MVVM + separação de camadas (data, domain, ui)  
- **Injeção de Dependência** → Hilt  
- **View System** → ViewBinding  
- **UI/UX** → Material Design 3 (BottomSheets, Cards, Chips, Ripples)  
- **Performance** → TypedArray + `baselineAligned="false"` para renderização rápida  
- **Persistência** → Room

## 🚀 Roadmap de Desenvolvimento

- [x] Suporte a 6 coleções de stickers (200+ itens)  
- [x] Implementação de Acessibilidade (TalkBack)  
- [x] Refatoração para Clean Architecture  
- [x] Implementação de Banco de Dados Local (Room)  

## 📥 Como instalar

1. Clone o repositório:

   ```bash
   git clone https://github.com/RinoaYK/Sticky-Notes-Kotlin-App.git

2. Abra o projeto no Android Studio
3. Sincronize o Gradle e execute no seu dispositivo ou emulador
