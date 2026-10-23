# Manual de uso

Sistema de controle de pedidos e ordens de serviço. Este manual explica o que cada perfil faz no sistema.

## Acesso

1. Abra o endereço do sistema no navegador (por padrão, http://localhost:8080).
2. Informe e-mail e senha e clique em **Entrar**.
3. Se os dados estiverem errados, aparece a mensagem "E-mail ou senha incorretos. Tente de novo."
4. Para sair, use o botão **Sair** no topo da página.

O menu superior é o mesmo em todas as telas e mostra só o que o seu perfil pode usar.

## Perfis

| Perfil | O que faz |
|---|---|
| Vendedor | Cadastra clientes, cria pedidos, acompanha os próprios pedidos e ordens de serviço e pode cancelar os próprios pedidos |
| Analista | Revisa e edita pedidos, aprova (gera a OS), cancela e avança o status das ordens de serviço |
| Admin | Cria e edita usuários, vê todos os pedidos e ordens e pode cancelar pedidos |

## Vendedor

### Cadastrar um cliente

1. Menu **Clientes** e botão **Novo cliente**.
2. Preencha nome e documento (CPF ou CNPJ). E-mail e telefone são opcionais.
3. Clique em **Salvar**. Se o documento já existir, o sistema avisa no campo.

Para alterar, use **Editar** na linha do cliente. A busca por nome fica no topo da lista.

### Criar um pedido

1. Menu **Pedidos** e botão **Novo pedido**.
2. Escolha o cliente.
3. Preencha os itens (descrição, quantidade e valor unitário). Use **Adicionar item** para mais linhas e **X** para remover. O total é calculado na hora.
4. Se quiser, escreva uma observação para o analista.
5. Clique em **Salvar pedido**. O pedido nasce com o status **Criado**.

### Acompanhar e cancelar

Na lista de pedidos, clique em **Abrir** para ver os itens, o status e o histórico de alterações. Enquanto o pedido estiver Criado ou Em revisão, o botão **Cancelar pedido** fica disponível. Depois de aprovado ou cancelado, o pedido não muda mais.

## Analista

### Revisar um pedido

1. Menu **Pedidos**, filtre por status se quiser, e clique em **Abrir**.
2. Corrija itens, quantidades, valores e observação.
3. Clique em **Salvar alterações**. Um pedido Criado passa para **Em revisão** e cada mudança é registrada no histórico.

### Aprovar

Com o pedido **Em revisão**, clique em **Aprovar e gerar OS**. O pedido vira **Aprovado** e o sistema cria a ordem de serviço, uma por pedido.

### Ordens de serviço

1. Menu **Ordens de serviço**.
2. Em uma OS **Aberta**, clique em **Iniciar** (vira Em andamento).
3. Em uma OS **Em andamento**, clique em **Concluir** (vira Concluída, com a data de conclusão).

Cada avanço fica registrado no histórico do pedido.

## Admin

### Gerenciar usuários

1. Menu **Usuários** e botão **Novo usuário**.
2. Preencha nome, e-mail (usado no login), senha com no mínimo 8 caracteres e perfil.
3. Para editar, use **Editar**. Deixe a senha em branco para mantê-la. Desmarque **Usuário ativo** para bloquear o acesso.

Por segurança, o admin não consegue desativar, rebaixar nem mudar o e-mail do próprio usuário.

## Fluxo dos status

Pedido: Criado, Em revisão, Aprovado (gera a OS) ou Cancelado.
Ordem de serviço: Aberta, Em andamento, Concluída.

## Mensagens e erros comuns

| Mensagem | O que significa |
|---|---|
| E-mail ou senha incorretos | Os dados de login não conferem |
| Já existe um cliente com este documento | Documento repetido no cadastro de clientes |
| Já existe um usuário com este e-mail | E-mail repetido no cadastro de usuários |
| A senha precisa ter pelo menos 8 caracteres | Senha curta demais |
| O pedido precisa ter pelo menos um item | Pedido sem itens |
| Só é possível aprovar pedidos em revisão | O analista precisa salvar o pedido antes de aprovar |
| Pedidos aprovados ou cancelados não podem ser alterados | O pedido já foi finalizado |
| Pedido não encontrado | O pedido não existe ou pertence a outro vendedor |
| Acesso negado | O seu perfil não tem permissão para esta tela |
