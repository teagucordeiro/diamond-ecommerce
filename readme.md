<p align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="https://i.imgur.com/vgR96El.png" />
    <source media="(prefers-color-scheme: light)" srcset="https://i.imgur.com/pYHW3Ug.png" />
    <img alt="Projeto Microserviços" src="https://i.imgur.com/pYHW3Ug.png" width="400px" />
  </picture>
</p>

# Projeto de Microserviços em Spring

Este projeto reúne quatro serviços:
- 🛒 **Ecommerce** (principal, comunica-se com os outros via REST)
- 🏬 **Store**
- 💱 **Exchange**
- 🎉 **Fidelity**

É um trabalho da disciplina de **Tolerância a Falhas em Sistemas de Software** do Metropole Digital. O objetivo é demonstrar técnicas para lidar com falhas em um cenário de ecommerce complexo, baseado em microserviços.

---

## Endpoints e Tipos de Falha

**Formato das falhas:** `Fail (Type, Probability, Duration)`

- **Type**: Tipo de falha (Omission, Error, Crash=Stop, Time)  
- **Probability**: Probabilidade de ocorrer  
- **Duration**: Quando ocorre, por quanto tempo o componente permanece em falha

> Todos os endpoints possuem um timeout de 1 segundo.

### /product
- **Falha:** `Fail (Omission, 0.2, 0s)`
- **Tolerância a falhas:** Triple Modular Redundancy (TMR)  
  - Há 3 réplicas do serviço **Store** no `docker-compose`  
  - O serviço **Ecommerce** faz a requisição para cada réplica e verifica o “voto” (response) das três  
  - Se pelo menos 2 réplicas retornarem o produto correto, o pedido é considerado bem-sucedido  

### /exchange
- **Falha:** `Fail (Crash, 0.1, _)`
- **Tolerância a falhas:** Réplicas passivas, retry e cache  
  - Existem 2 réplicas de forma passiva  
  - Se uma falhar, a outra assume  
  - Se o request falhar, o serviço **Ecommerce** tenta novamente  
  - Se falhar de novo, utiliza o último valor conhecido em cache  

### /sell
- **Falha:** `Fail (Error, 0.1, 5s)`
- **Tolerância a falhas:** Em desenvolvimento (a ser implementada)

### /bonus
- **Falha:** `Fail (Time=2s, 0.1, 30s)`
- **Tolerância a falhas:** 
  - Se o serviço **Fidelity** demorar mais de 1 segundo, o **Ecommerce** registra no banco (Postgre) o log dos requests não resolvidos  
  - Um job fica consultando (`/status` no serviço de **Fidelity**) a cada 30 segundos. Quando o serviço volta a ficar disponível, ele tenta reenviar os logs pendentes  

---

## Como Executar
1. Certifique-se de ter **Docker** e **Docker Compose** instalados.
2. Na raiz do projeto, execute:
   ```bash
   docker-compose up -d
   ```
3. Acesse cada serviço conforme configurado nos containers.
Obs.: ao executar o projeto no windows, pode ser que o nome dos containers recebam um afixo "main" e necessite adptar as variáveis de ambiente. A exemplo: "diamond-ecommerce-main-exchange-1".

Feito com muito ☕ e dedicação.
