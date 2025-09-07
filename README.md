## Entendendo as Peças: O Papel de Cada Biblioteca (`pom.xml`)

Seu `pom.xml` é como a lista de ingredientes da sua receita. Cada dependência tem um papel fundamental e especializado. Vamos agrupá-las por função.

### Grupo 1: A Base (Spring Boot & Utilitários)

* `spring-boot-starter-web`: Este é o **alicerce** da sua aplicação. Ele traz tudo o que é necessário para construir uma API web: um servidor embutido (Tomcat), o Spring MVC para criar os endpoints (`@RestController`), e a capacidade de converter objetos Java para JSON automaticamente.
* `lombok`: Uma **ferramenta de produtividade**. Ele escreve código repetitivo (como getters, setters, construtores) para você através de anotações simples (`@Data`, `@AllArgsConstructor`), deixando suas classes de modelo limpas e legíveis.

---
### Grupo 2: O Núcleo da Integração (A Trindade do Google)

Estas três dependências trabalham em conjunto para realizar a comunicação com o Google Sheets. Pense nelas como um time com funções diferentes:

* `google-auth-library-oauth2-http` (O Passaporte 🛂): Esta é a biblioteca de **autenticação**. Sua única função é provar para o Google quem você é. Ela usa suas credenciais (da conta de serviço) para criar uma identidade digital segura. É o seu passaporte para entrar no ecossistema do Google.
* `google-api-services-sheets` (A Caixa de Ferramentas 🛠️): Esta é a biblioteca da **API do Sheets** propriamente dita. Uma vez que você está autenticado, ela te dá todas as ferramentas para *fazer coisas* na planilha: ler células (`.get()`), adicionar linhas (`.append()`), limpar dados (`.clear()`), etc.
* `gson` e `google-http-client-gson` (O Tradutor 🗣️): APIs se comunicam via texto (JSON). Esta dupla é o **tradutor** oficial. Ela converte seus objetos Java em JSON antes de enviá-los para o Google e converte as respostas em JSON do Google de volta para objetos Java que seu código pode entender.

---
### Grupo 3: A Configuração Segura (Dotenv)

* `dotenv-java` (O Cofre de Segredos 🔒): Esta biblioteca tem um único e importante trabalho: carregar as credenciais sensíveis (como sua chave privada) de um arquivo `.env` para dentro da aplicação. Isso garante que seus segredos nunca sejam expostos diretamente no código-fonte, uma prática de segurança essencial.

---
## O Potencial da Google Sheets API: Muito Além do CRUD

O que fizemos até agora foi usar a planilha como um simples banco de dados. Isso é apenas a ponta do iceberg. A API permite que você use o Google Sheets como uma **ferramenta completa de automação, cálculo e geração de relatórios**.

### 1. Manipulação Estrutural da Planilha
Você pode criar e modificar a estrutura da planilha dinamicamente.
* **Criar/Deletar/Renomear Abas:** Sua aplicação pode criar uma nova aba para cada mês ou para cada novo cliente, por exemplo.
* **Congelar Linhas e Colunas:** Automatize a criação de cabeçalhos congelados para facilitar a visualização.
* **Mesclar Células:** Crie títulos e cabeçalhos complexos que se estendem por várias colunas.

### 2. Formatação e Estilo Dinâmicos
Sua API não precisa apenas "cuspir" dados. Ela pode estilizá-los para criar relatórios prontos para o usuário final.
* **Mudar Cores:** Destaque linhas com valores acima de um certo limite em vermelho ou verde.
* **Aplicar Formatos:** Formate números como moeda (`R$ 1.234,56`), porcentagem ou datas.
* **Alterar Fontes:** Coloque textos em negrito, itálico ou mude o tamanho da fonte.
* **Adicionar Bordas:** Crie tabelas visualmente bem definidas.

### 3. Fórmulas e Funções Inteligentes
Talvez o recurso mais poderoso: você pode escrever **fórmulas**, não apenas valores.
* **Cálculos Automatizados:** Em vez de calcular um total em Java e escrevê-lo, você pode simplesmente escrever a fórmula `"=SOMA(C2:C100)"` em uma célula. A planilha fará o cálculo para você.
* **Funções Complexas:** Use `PROCV` (`VLOOKUP`), `SE` (`IF`), `IMPORTHTML` e praticamente qualquer outra função do Google Sheets para buscar e processar dados de forma inteligente.

### 4. Gráficos e Visualizações 📊
Sua aplicação pode gerar relatórios visuais completos.
* **Criar Gráficos:** Selecione um intervalo de dados e crie um gráfico de barras, pizza ou linha com uma única chamada de API.
* **Atualizar Gráficos:** Altere os dados de um gráfico existente ou mude seu estilo.
* **Tabelas Dinâmicas:** Crie e gerencie tabelas dinâmicas para sumarizar grandes volumes de dados.

### 5. Operações em Lote Avançadas (`batchUpdate`)
A operação `batchUpdate` é o canivete suíço da API. Ela permite que você envie uma lista de *diferentes tipos de operações* em uma única chamada. Por exemplo, em uma única requisição, você pode:
* Atualizar o valor da célula A1.
* Mudar a cor de fundo da linha 5.
* Mesclar as células de B1 a D1.
* Adicionar um gráfico.

**Conclusão:** Pense no Google Sheets não apenas como uma tabela, mas como um motor de relatórios. Com a API, sua aplicação Java pode automatizar a criação de dashboards financeiros, relatórios de vendas, controles de estoque ou qualquer outra tarefa que hoje você faria manualmente, combinando o poder de processamento do Java com a flexibilidade e a interface visual do Google Sheets.

Esta classe, `GoogleSheetsConfig`, é o coração da sua conexão. Ela atua como a "sala de máquinas" ou o "motor de autenticação" do seu projeto. Vamos entender em detalhes como ela conecta as variáveis de ambiente do seu arquivo `.env` até o objeto final que manipula a planilha.

O processo todo pode ser resumido em duas grandes etapas: **Injeção dos Segredos** e **Construção do Serviço**.

---
## Etapa 1: A Injeção dos Segredos (O Papel do `@Value`)

As variáveis no topo da sua classe são as portas de entrada para as suas credenciais.

```java
@Configuration
public class GoogleSheetsConfig {

    @Value("${google.credentials.client-id}")
    private String clientId;

    @Value("${google.credentials.client-email}")
    private String clientEmail;

    // ... e assim por diante
}
```

* **`@Configuration`**: Esta anotação diz ao Spring: "Esta classe é uma fonte de definições de beans". Beans são os objetos que formam a espinha dorsal da sua aplicação, gerenciados pelo Spring.
* **`@Value("${...}")`**: Esta é uma das anotações mais poderosas do Spring. Ela funciona como um injetor de valores. Para cada campo (`clientId`, `clientEmail`, etc.), ela executa a seguinte busca:
    1.  Ela procura por uma propriedade com o nome `google.credentials.client-id` (e os outros) nas fontes de configuração do Spring.
    2.  Nosso arquivo `application.properties` define essa propriedade e a mapeia para uma variável de ambiente: `google.credentials.client-id=${GOOGLE_CLIENT_ID}`.
    3.  A biblioteca `dotenv-java` (que configuramos na classe principal) já leu seu arquivo `.env` e carregou a variável `GOOGLE_CLIENT_ID` para dentro do ambiente da aplicação.
    4.  O Spring então conecta tudo, pegando o valor do `.env`, passando pelo `application.properties` e **injetando** o valor final diretamente no campo `private String clientId;`.

**Em resumo, a anotação `@Value` é a ponte que traz os segredos guardados no seu arquivo `.env` para dentro do seu código Java de forma segura.**

---
## Etapa 2: A Construção do Serviço (O Papel do `@Bean`)

O método `sheetsService()` é uma "fábrica". A anotação `@Bean` diz ao Spring: "Execute este método, pegue o objeto que ele retorna (um objeto `Sheets`) e guarde-o no seu contêiner. Se qualquer outra parte da aplicação (como o `ItemService`) pedir por um objeto `Sheets`, entregue este que você guardou".

Vamos dissecar o que acontece dentro dessa fábrica.

```java
@Bean
public Sheets sheetsService() throws IOException, GeneralSecurityException {

    // 1. O Canal de Comunicação
    final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

    // 2. A Montagem do "Documento" de Credenciais
    String credentialsJsonString = String.format(...);
    InputStream credentialsStream = new ByteArrayInputStream(credentialsJsonString.getBytes());

    // 3. A Criação da Identidade Digital
    GoogleCredentials credential = ServiceAccountCredentials.fromStream(credentialsStream)
            .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));

    // 4. O Assistente de Autenticação
    HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credential);

    // 5. A Montagem Final do "Controle Remoto"
    return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, requestInitializer)
            .setApplicationName("Spring Sheets CRUD")
            .build();
}
```

1.  **O Canal de Comunicação:** `GoogleNetHttpTransport.newTrustedTransport()` simplesmente estabelece um canal de comunicação seguro (HTTPS) para conversar com os servidores do Google.

2.  **A Montagem do "Documento":** Aqui, nós pegamos as variáveis injetadas (`this.clientId`, `this.clientEmail`, etc.) e usamos `String.format()` para construir um texto JSON em memória. Esse texto é uma réplica mínima, mas completa, do arquivo JSON original que o Google nos deu. Em seguida, `new ByteArrayInputStream(...)` transforma esse texto em um "fluxo de dados", que é o formato que a biblioteca de autenticação sabe ler.

3.  **A Criação da Identidade (`GoogleCredentials`):** Esta é a etapa de validação. O método `ServiceAccountCredentials.fromStream(...)` lê o fluxo de dados, analisa o JSON, valida se todos os campos necessários estão lá (`client_id`, `private_key`, etc.) e, se tudo estiver correto, cria um objeto `GoogleCredentials`. Este objeto é a representação da identidade digital e autenticada da sua aplicação. Em seguida, `.createScoped(...)` anexa as permissões a essa identidade, dizendo "esta identidade tem permissão para fazer tudo em planilhas".

4.  **O Assistente de Autenticação (`HttpCredentialsAdapter`):** Pense neste objeto como um "assistente" que acompanha todas as suas futuras requisições. O trabalho dele é interceptar cada chamada que você fizer à API (como um `get` ou `append`) e anexar automaticamente os cabeçalhos de autenticação necessários. É por causa dele que você não precisa se preocupar com autenticação no `ItemService`.

5.  **A Montagem Final (`Sheets.Builder`):** Finalmente, construímos o objeto `Sheets`. Nós entregamos ao construtor:
    * O canal de comunicação (`HTTP_TRANSPORT`).
    * O idioma que será falado (`JSON_FACTORY`, que definimos como Gson).
    * O assistente de autenticação (`requestInitializer`).

O método `.build()` junta tudo e retorna o objeto `Sheets` final: um "controle remoto" totalmente configurado, autenticado e pronto para ser usado.

### Fluxo de Dados Resumido

O caminho completo da sua credencial é este:

`Arquivo .env` → `dotenv-java` → `Variáveis de Ambiente` → `application.properties` → `@Value` nos campos da classe → `String.format()` → `InputStream` → `GoogleCredentials` → **`Objeto Sheets`** → Injetado no `ItemService`

Esta classe, `GoogleSheetsConfig`, é o coração da sua conexão. Ela atua como a "sala de máquinas" ou o "motor de autenticação" do seu projeto. Vamos entender em detalhes como ela conecta as variáveis de ambiente do seu arquivo `.env` até o objeto final que manipula a planilha.

O processo todo pode ser resumido em duas grandes etapas: **Injeção dos Segredos** e **Construção do Serviço**.

---
## Etapa 1: A Injeção dos Segredos (O Papel do `@Value`)

As variáveis no topo da sua classe são as portas de entrada para as suas credenciais.

```java
@Configuration
public class GoogleSheetsConfig {

    @Value("${google.credentials.client-id}")
    private String clientId;

    @Value("${google.credentials.client-email}")
    private String clientEmail;

    // ... e assim por diante
}
```

* **`@Configuration`**: Esta anotação diz ao Spring: "Esta classe é uma fonte de definições de beans". Beans são os objetos que formam a espinha dorsal da sua aplicação, gerenciados pelo Spring.
* **`@Value("${...}")`**: Esta é uma das anotações mais poderosas do Spring. Ela funciona como um injetor de valores. Para cada campo (`clientId`, `clientEmail`, etc.), ela executa a seguinte busca:
    1.  Ela procura por uma propriedade com o nome `google.credentials.client-id` (e os outros) nas fontes de configuração do Spring.
    2.  Nosso arquivo `application.properties` define essa propriedade e a mapeia para uma variável de ambiente: `google.credentials.client-id=${GOOGLE_CLIENT_ID}`.
    3.  A biblioteca `dotenv-java` (que configuramos na classe principal) já leu seu arquivo `.env` e carregou a variável `GOOGLE_CLIENT_ID` para dentro do ambiente da aplicação.
    4.  O Spring então conecta tudo, pegando o valor do `.env`, passando pelo `application.properties` e **injetando** o valor final diretamente no campo `private String clientId;`.

**Em resumo, a anotação `@Value` é a ponte que traz os segredos guardados no seu arquivo `.env` para dentro do seu código Java de forma segura.**

---
## Etapa 2: A Construção do Serviço (O Papel do `@Bean`)

O método `sheetsService()` é uma "fábrica". A anotação `@Bean` diz ao Spring: "Execute este método, pegue o objeto que ele retorna (um objeto `Sheets`) e guarde-o no seu contêiner. Se qualquer outra parte da aplicação (como o `ItemService`) pedir por um objeto `Sheets`, entregue este que você guardou".

Vamos dissecar o que acontece dentro dessa fábrica.

```java
@Bean
public Sheets sheetsService() throws IOException, GeneralSecurityException {

    // 1. O Canal de Comunicação
    final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

    // 2. A Montagem do "Documento" de Credenciais
    String credentialsJsonString = String.format(...);
    InputStream credentialsStream = new ByteArrayInputStream(credentialsJsonString.getBytes());

    // 3. A Criação da Identidade Digital
    GoogleCredentials credential = ServiceAccountCredentials.fromStream(credentialsStream)
            .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));

    // 4. O Assistente de Autenticação
    HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credential);

    // 5. A Montagem Final do "Controle Remoto"
    return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, requestInitializer)
            .setApplicationName("Spring Sheets CRUD")
            .build();
}
```

1.  **O Canal de Comunicação:** `GoogleNetHttpTransport.newTrustedTransport()` simplesmente estabelece um canal de comunicação seguro (HTTPS) para conversar com os servidores do Google.

2.  **A Montagem do "Documento":** Aqui, nós pegamos as variáveis injetadas (`this.clientId`, `this.clientEmail`, etc.) e usamos `String.format()` para construir um texto JSON em memória. Esse texto é uma réplica mínima, mas completa, do arquivo JSON original que o Google nos deu. Em seguida, `new ByteArrayInputStream(...)` transforma esse texto em um "fluxo de dados", que é o formato que a biblioteca de autenticação sabe ler.

3.  **A Criação da Identidade (`GoogleCredentials`):** Esta é a etapa de validação. O método `ServiceAccountCredentials.fromStream(...)` lê o fluxo de dados, analisa o JSON, valida se todos os campos necessários estão lá (`client_id`, `private_key`, etc.) e, se tudo estiver correto, cria um objeto `GoogleCredentials`. Este objeto é a representação da identidade digital e autenticada da sua aplicação. Em seguida, `.createScoped(...)` anexa as permissões a essa identidade, dizendo "esta identidade tem permissão para fazer tudo em planilhas".

4.  **O Assistente de Autenticação (`HttpCredentialsAdapter`):** Pense neste objeto como um "assistente" que acompanha todas as suas futuras requisições. O trabalho dele é interceptar cada chamada que você fizer à API (como um `get` ou `append`) e anexar automaticamente os cabeçalhos de autenticação necessários. É por causa dele que você não precisa se preocupar com autenticação no `ItemService`.

5.  **A Montagem Final (`Sheets.Builder`):** Finalmente, construímos o objeto `Sheets`. Nós entregamos ao construtor:
    * O canal de comunicação (`HTTP_TRANSPORT`).
    * O idioma que será falado (`JSON_FACTORY`, que definimos como Gson).
    * O assistente de autenticação (`requestInitializer`).

O método `.build()` junta tudo e retorna o objeto `Sheets` final: um "controle remoto" totalmente configurado, autenticado e pronto para ser usado.

### Fluxo de Dados Resumido

O caminho completo da sua credencial é este:

`Arquivo .env` → `dotenv-java` → `Variáveis de Ambiente` → `application.properties` → `@Value` nos campos da classe → `String.format()` → `InputStream` → `GoogleCredentials` → **`Objeto Sheets`** → Injetado no `ItemService`

A classe `ItemService` é o coração da sua aplicação, servindo como a ponte entre a lógica de negócio e a interação direta com a Planilha Google. Ela funciona como o "braço direito" do `Controller`, executando as tarefas de manipulação de dados na planilha.

---
## Análise dos Componentes Principais

Antes de mergulhar nos métodos, vamos entender as peças que compõem a classe.

* **`@Service`**: Uma anotação do Spring que marca esta classe como um componente de serviço. Isso permite que o Spring a gerencie e a injete em outras classes, como o `Controller`.
* **`@Autowired private Sheets sheetsService;`**: Aqui acontece a **Injeção de Dependência**. O objeto `Sheets` que criamos e configuramos na `GoogleSheetsConfig` (nosso "controle remoto") é injetado aqui para que possamos usá-lo.
* **`@Value("${google.spreadsheet.id}")`**: Injeta o ID da sua planilha, que foi carregado a partir do arquivo `.env` e definido no `application.properties`.
* **`DATA_SHEET_NAME` e `LOG_SHEET_NAME`**: O uso de constantes (`static final`) para os nomes das abas é uma excelente prática. Se um dia você decidir renomear a aba na planilha, só precisará mudar o valor em um único lugar no código.

---
## Dissecando os Métodos de Serviço

Cada método público implementa uma operação da sua API, traduzindo uma requisição em uma ou mais ações na planilha.

### `create(Item item)`

* **Objetivo:** Adicionar uma nova linha de dados na aba "DB".
* **Objetos Google:**
    * `ValueRange`: Funciona como um "contêiner" para os dados. A estrutura `List.of(List.of(...))` é usada para criar uma lista de linhas contendo uma única linha, que por sua vez contém os valores do `Item`.
* **Métodos Google:**
    * `sheetsService.spreadsheets().values().append(...)`: Utiliza a operação `append`, que inteligentemente adiciona os dados após a última linha preenchida na aba especificada. É a ferramenta ideal para inserções.
    * `.setValueInputOption("RAW")`: Garante que os dados sejam inseridos como texto puro, sem que o Google Sheets tente interpretá-los (por exemplo, converter "10-12" em uma data).

### `readAll()`

* **Objetivo:** Ler todos os dados da aba "DB" e transformá-los em uma lista de objetos `Item`.
* **Objetos Google:**
    * `ValueRange`: Desta vez, é usado para *receber* os dados. O método `response.getValues()` retorna uma `List<List<Object>>`, onde cada lista interna representa uma linha da planilha.
* **Métodos Google:**
    * `sheetsService.spreadsheets().values().get(...)`: A operação de leitura fundamental. Ao passar apenas o nome da aba como `range`, ele busca todas as células preenchidas.
* **Lógica do Serviço:** O código demonstra um tratamento de dados robusto:
    * Verifica se `values` é nulo ou vazio.
    * Usa `.skip(1)` para ignorar a linha de cabeçalho.
    * Usa `try-catch` para `NumberFormatException`, garantindo que a aplicação não quebre se um ID na planilha não for um número válido.

### `update(Long id, Item item)`

* **Objetivo:** Encontrar uma linha específica pelo seu ID e substituir seu conteúdo.
* **Estratégia:** "Encontre e Substitua".
* **Objetos Google:**
    * `ValueRange`: Novamente, usado como contêiner para empacotar os novos dados do `Item` que serão escritos.
* **Métodos Google:**
    * `sheetsService.spreadsheets().values().update(...)`: A operação de atualização. Diferente do `append`, o `update` exige um `range` exato (ex: `"DB!A5:C5"`) e sobrescreve o conteúdo daquelas células.
* **Lógica do Serviço:** A inteligência aqui está na combinação com o método `findRowById` para primeiro descobrir o endereço exato da linha a ser alterada.

### `delete(Long id)`

* **Objetivo:** Limpar o conteúdo de uma linha específica.
* **Estratégia:** "Encontre e Limpe".
* **Objetos Google:**
    * `ClearValuesRequest`: Um objeto simples que sinaliza à API a intenção de limpar células.
* **Métodos Google:**
    * `sheetsService.spreadsheets().values().clear(...)`: Executa a limpeza. Ele não deleta a linha fisicamente, mas apaga todo o conteúdo dentro do `range` especificado.
* **Lógica do Serviço:** Assim como o `update`, ele depende do `findRowById` para localizar a linha correta antes de agir.

### `createBatch(List<Item> items)`

* **Objetivo:** Inserir múltiplas linhas em uma única chamada de API.
* **Objetos Google:**
    * `ValueRange`: Aqui, seu poder é mais evidente. A `List<List<Object>>` é preenchida com múltiplas linhas, uma para cada `Item` na lista de entrada.
* **Métodos Google:**
    * `sheetsService.spreadsheets().values().append(...)`: O mesmo método `append` usado na criação de um único item, mas, ao receber um `ValueRange` com várias linhas, ele as insere todas de uma vez.
* **Lógica do Serviço:** A transformação de `List<Item>` para `List<List<Object>>` usando `stream().map()` é a forma mais eficiente e moderna de preparar os dados para a API. A criação de uma `new ArrayList<>()` dentro do `.map()` resolve de forma definitiva o problema de inferência de tipos do Java.

---
### Métodos Auxiliares (`private`)

Estes métodos demonstram boas práticas de programação, como a extração de lógica repetida.

#### `findRowById(Long id)`

* **Objetivo:** Descobrir o número da linha de um item a partir do seu ID.
* **Estratégia:**
    * **Eficiência:** A busca é feita apenas na coluna A (`"DB!A:A"`), evitando o download de dados desnecessários.
    * **Robustez:** O uso de `try-catch` dentro do loop garante que, se uma célula na coluna de ID contiver texto em vez de um número, o programa não irá quebrar, apenas ignorará aquela linha e continuará a busca.

#### `logAction(String acao, String detalhes)`

* **Objetivo:** Centralizar e padronizar o registro de todas as ações.
* **Estratégia:** Encapsula a lógica de criar um timestamp e usar o método `append` na aba de "Logs". Isso evita a repetição de código e torna os métodos CRUD mais limpos e focados em sua responsabilidade principal.

## ⚙️ Configuração do Google Sheets no Spring Boot

Para se conectar ao Google Sheets de forma segura, usamos uma **Service Account** e carregamos as credenciais via **variáveis de ambiente**.  
Isso evita expor o arquivo `credentials.json` no projeto.

### Classe de Configuração (`GoogleSheetsConfig.java`)

```java
package com.crud.Sheets.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Configuration
public class GoogleSheetsConfig {

    @Value("${google.credentials.client-id}")
    private String clientId;

    @Value("${google.credentials.client-email}")
    private String clientEmail;

    @Value("${google.credentials.private-key-id}")
    private String privateKeyId;

    @Value("${google.credentials.private-key}")
    private String privateKey;

    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    @Bean
    public Sheets sheetsService() throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();

        // Corrige quebras de linha na chave privada (importante!)
        String fixedPrivateKey = privateKey.replace("\\n", "\n");

        // Monta o JSON em memória com todos os campos esperados
        String credentialsJsonString = String.format(
                "{" +
                        "\"type\": \"service_account\", " +
                        "\"client_id\": \"%s\", " +
                        "\"private_key_id\": \"%s\", " +
                        "\"private_key\": \"%s\", " +
                        "\"client_email\": \"%s\" " +
                "}",
                this.clientId,
                this.privateKeyId,
                fixedPrivateKey,
                this.clientEmail
        );

        InputStream credentialsStream = new ByteArrayInputStream(credentialsJsonString.getBytes());

        GoogleCredentials credential = ServiceAccountCredentials.fromStream(credentialsStream)
                .createScoped(Collections.singleton(SheetsScopes.SPREADSHEETS));

        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credential);

        return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, requestInitializer)
                .setApplicationName("Spring Sheets CRUD")
                .build();
    }
}

