# ⚡ ApexLift — Android App

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9%2B-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Material_3-757575?style=for-the-badge&logo=material-design&logoColor=white)](https://m3.material.io/)
[![Hilt](https://img.shields.io/badge/Dagger_Hilt-000000?style=for-the-badge&logo=dagger&logoColor=white)](https://dagger.dev/hilt/)
[![Room](https://img.shields.io/badge/Room_DB-3DDC84?style=for-the-badge&logo=sqlite&logoColor=white)](https://developer.android.com/training/data-storage/room)

Aplicativo Android moderno e de alta performance desenvolvido em **Kotlin e Jetpack Compose** para acompanhamento e progressão de treinos de musculação. Conta com suporte **Offline-First**, gráficos customizados em tempo real, cronômetro de descanso inteligente e cálculo automático de volume/tonelagem total.

---

## 📸 Screenshots & Demonstração

| Treino Ativo | Histórico & Gráfico Canvas | Resumo de Tonelagem |
| :---: | :---: | :---: |
| *Em Breve* | *Em Breve* | *Em Breve* |

---

## ✨ Principais Funcionalidades

- **🔥 Aquecimento & Bi-Sets Inteligentes**:
  - Identificação visual clara de séries de aquecimento.
  - Transição sem descanso em exercícios conjugados do mesmo Bi-Set, disparando o timer somente no término do bloco.
- **📈 Gráfico de Evolução de Carga (Canvas)**:
  - Curva de progressão contínua com gradiente sombreado, identificação de Recorde Pessoal (PR) e cálculo dinâmico de delta ($\Delta\text{ kg}$).
- **🏆 Resumo do Treino com Tonelagem**:
  - Cálculo automático de volume total levantado ($\sum \text{Carga} \times \text{Reps}$), convertido automaticamente em toneladas ($t$).
  - Métrica de duração da sessão e detalhamento das cargas máximas por aparelho.
- **🔍 Seletor Rápido de Exercícios**:
  - Modal com busca em tempo real e filtros em chips por grupos musculares (*Peito, Pernas, Costas, Braços, etc.*).
- **⏱️ Cronômetro de Descanso com Alerta Háptico**:
  - Timer regressivo com botões de ajuste (+10s, pular) e notificação de vibração ao zerar.
- **🔄 Arquitetura Offline-First**:
  - O app salva todas as rotinas e séries pendentes no **Room (SQLite)** localmente, permitindo treinar sem conexão à internet e sincronizando assim que online.

---

## 🏗️ Arquitetura & Stack Tecnológica

O app adota o padrão **MVVM + Clean Architecture** e o fluxo reativo de dados via **StateFlow**:

- **UI / Design**: Jetpack Compose com Material Design 3 (Dark Theme customizado com detalhes em verde neon).
- **Gerenciamento de Estado**: `ViewModel`, `StateFlow` e `collectAsState`.
- **Injeção de Dependências**: **Dagger Hilt**.
- **Persistência Local (Offline-First)**: **Room Database** (Entities, DAOs, TypeConverters e Mappers).
- **Comunicação de Rede**: **Retrofit 2** + **OkHttp** + **Gson**.
- **Desenho Customizado**: **Compose Canvas** (`Path`, `Brush.verticalGradient`, `StrokeJoin`).
- **Navegação**: **Navigation Compose**.

---

## 📁 Estrutura de Pacotes

```text
com.ruan.apexlift/
├── data/
│   ├── local/          # Room Database, DAOs e Entities
│   ├── mapper/         # Mapeadores DTO <-> Entity <-> Domain
│   ├── model/          # DTOs de Request e Response (API)
│   ├── remote/         # Interfaces Retrofit e ApiServices
│   └── repository/     # Implementações dos Repositories
├── di/                 # Módulos Dagger Hilt (DataModule, NetworkModule)
├── ui/
│   ├── components/     # Componentes reutilizáveis (Gráficos, Modais, Timers)
│   ├── navigation/     # NavHost, Rotas e BottomNavigationBar
│   ├── screens/        # Telas Compose (Home, TreinoAtivo, Fichas, Exercícios)
│   ├── theme/          # Cores (Neon/Dark), Tipografia e Formas
│   ├── util/           # Helpers (Alertas, Sons, Formatadores)
│   └── viewmodel/      # ViewModels e UiStates selados (Sealed Interfaces)
