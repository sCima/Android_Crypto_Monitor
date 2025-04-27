# Android Crypto Monitor
Este projeto Android em Kotlin consome a API do MercadoBitcoin para exibir o valor e a data da última cotação do Bitcoin.


## Descrição Geral
Este projeto exemplifica o uso de kotlinx-coroutines para requisições assíncronas, aliado ao Retrofit2 (com Gson). Ao tocar no botão “Refresh”, a interface dispara uma chamada REST que, em caso de sucesso, atualiza dois TextView exibindo o preço em reais e a data/hora convertida do timestamp Unix.

## Ferramentas e Dependências
- **Kotlin** (linguagem moderna com suporte nativo a Coroutines)
- **Gradle** (sistema de build)
- **Bibliotecas externas:**
  - `org.jetbrains.kotlinx:kotlinx-coroutines-android`
  - `com.squareup.retrofit2:retrofit`
  - `com.squareup.retrofit2:converter-gson`

## Estrutura de Pacotes
```text
src/main/java/scima/com/github/aula3103/
├── model/
│   └── TickerResponse.kt
├── service/
│   ├── MercadoBitcoinService.kt
│   └── MercadoBitcoinServiceFactory.kt
└── MainActivity.kt
```

## Componentes Principais

### 1. TickerResponse.kt
Representa o envelope retornado pela API:

```kotlin
class TickerResponse(val ticker: Ticker)

class Ticker(
  val high: String,
  val low: String,
  val vol: String,
  val last: String,
  val buy: String,
  val sell: String,
  val date: Long
)
```
- Cada propriedade é declarada como `val` para torná-las imutáveis.
- Campos como `high`, `low` e `last` chegam como `String` no JSON; convertem-se em `Double` apenas quando necessários.
- `date` armazena o timestamp Unix (em segundos), que depois será convertido em milissegundos para exibição.
---
### 2. MercadoBitcoinServiceFactory.kt
Configura e fornece instância do Retrofit:

```kotlin
fun create(): MercadoBitcoinService {
  val retrofit = Retrofit.Builder()
    .baseUrl("https://www.mercadobitcoin.net/")        // URL raiz das requisições
    .addConverterFactory(GsonConverterFactory.create()) // Gson interpreta o JSON
    .build()

  return retrofit.create(MercadoBitcoinService::class.java)
}
```
- Define a URL-base da API.
- Adiciona conversor Gson para desserializar JSON.
- **Builder Pattern:** facilita encadeamento de parâmetros.
- `addConverterFactory` injeta o conversor Gson para mapear automaticamente o payload.
- `create(...)` gera a implementação da interface de serviço.
---
### 3. MercadoBitcoinService.kt
Declara o endpoint de ticker:

```kotlin
interface MercadoBitcoinService {
  @GET("api/BTC/ticker/")
  suspend fun getTicker(): Response<TickerResponse>
}
```
- Anotação `@GET` define o caminho completo após a `baseUrl`.
- Método `suspend` usado com Coroutines para chamadas não bloqueantes.
- Retorna `Response<TickerResponse>` para tratar sucesso e falhas.
---
### 4. MainActivity.kt
Controla a interface e lógica de rede:

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
  
    configureToolbar(findViewById(R.id.toolbar_main))
  
    findViewById<Button>(R.id.btn_refresh)
        .setOnClickListener { makeRestCall() }
}
```
- `configureToolbar`: atribui título e cor à _Toolbar_ via `supportActionBar`.

```kotlin
private fun makeRestCall() {
    CoroutineScope(Dispatchers.Main).launch {
        try {
            val service = MercadoBitcoinServiceFactory().create()
            val response = service.getTicker()      // Chamada HTTP

            if (response.isSuccessful) {
                val ticker = response.body()!!.ticker

                // Conversão do valor para Double e formatação em Reais
                val lastVal = ticker.last.toDoubleOrNull()
                lastVal?.let {
                    findViewById<TextView>(R.id.lbl_value).text =
                        NumberFormat
                          .getCurrencyInstance(Locale("pt","BR"))
                          .format(it)
                }

                // Transformação do Unix timestamp em data legível
                val dateMs = ticker.date * 1000L
                val formattedDate = SimpleDateFormat(
                    "dd/MM/yyyy HH:mm:ss", Locale.getDefault()
                ).format(Date(dateMs))
                findViewById<TextView>(R.id.lbl_date).text = formattedDate

            } else {
                // Mapeia código HTTP em mensagem amigável
                val msg = when (response.code()) {
                    400 -> "Requisição inválida"
                    401 -> "Não autorizado"
                    404 -> "Endpoint não encontrado"
                    else -> "Erro desconhecido"
                }
                Toast.makeText(this@MainActivity, msg, Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            // Exibi falha de conexão, parse ou timeout
            Toast.makeText(
                this@MainActivity,
                "Falha na requisição: ${e.localizedMessage}",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
```
- `Dispatchers.Main` garante execução no _thread_ principal, permitindo atualizar vistas.
- Bloco `try/catch` captura exceções de rede e parsing.
- `toDoubleOrNull()` evita _crash_ caso o valor não seja numérico.
- Atualiza `TextView` com preço e horário.
- Lida com código de resposta e exceções, exibindo `Toast` em caso de falha.

## Evidências de Execução
![app antes da execução](images/antes.png)
![app depois da execução](images/execucao.png)

> A captura acima demonstra a interface antes da execução e após a execução, exibindo o valor formatado em reais e o timestamp convertido corretamente.


