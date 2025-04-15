package scima.com.github.aula3103.service

import retrofit2.Response
import retrofit2.http.GET
import scima.com.github.aula3103.model.TickerResponse

interface MercadoBitcoinService {

    @GET("api/BTC/ticker/")
    suspend fun getTicker(): Response<TickerResponse>
}