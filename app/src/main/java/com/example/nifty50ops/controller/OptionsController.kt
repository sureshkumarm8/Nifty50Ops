package com.example.nifty50ops.controller

import android.content.Context
import com.example.nifty50ops.model.OptionsEntity
import com.example.nifty50ops.network.ApiService
import com.example.nifty50ops.repository.OptionsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OptionsController(private val optionRepository: OptionsRepository) {

    suspend fun fetchOptionsData(context: Context) {
        val prefs = securityIdToSymbol.keys.joinToString(",") { "NSE:$it:OPTION" }
        ApiService.fetchData(context, prefs)?.let { responseBody ->
            saveToDatabase(parseOptionResponse(responseBody))
        }
    }

    // Define this globally or in your class
    private val previousOptionMap = mutableMapOf<String, OptionsEntity>()
    private val firstOptionMap = mutableMapOf<String, OptionsEntity>()

    private fun parseOptionResponse(response: String): List<OptionsEntity> {
        val optionsList = mutableListOf<OptionsEntity>()
        val jsonObject = JSONObject(response)
        val dataArray: JSONArray = jsonObject.optJSONArray("data") ?: JSONArray()

        for (i in 0 until dataArray.length()) {
            val optionsObject = dataArray.getJSONObject(i)
            val securityId = optionsObject.optInt("security_id", -1)
            if (securityId == -1) continue

            val name = securityIdToSymbol[securityId] ?: "Unknown"
            val ltp = optionsObject.optDouble("last_price", 0.0)
            val volTraded = optionsObject.optInt("total_buy_quantity", 0)
            val buyQty = optionsObject.optInt("total_buy_quantity", 0)
            val sellQty = optionsObject.optInt("total_sell_quantity", 0)
            val oiQty = optionsObject.optInt("oi", 0)
            val oiChange = optionsObject.optDouble("change_oi", 0.0)
            val lastTradeTime = optionsObject.optLong("last_trade_time", 0)
            val timestamp = formatToHourMinute(lastTradeTime)

            val previous = previousOptionMap[name]

            val buyDiffPercent = previous?.let {
                if (it.buyQty != 0) ((buyQty - it.buyQty).toDouble() / it.buyQty) * 100 else 0.0
            } ?: 0.0

            val sellDiffPercent = previous?.let {
                if (it.sellQty != 0) ((sellQty - it.sellQty).toDouble() / it.sellQty) * 100 else 0.0
            } ?: 0.0

            // Store first record
            val first = firstOptionMap.getOrPut(name) {
                OptionsEntity(
                    name = name,
                    ltp = ltp,
                    buyQty = buyQty,
                    sellQty = sellQty,
                    volTraded = volTraded,
                    buyDiffPercent = 0.0,
                    sellDiffPercent = 0.0,
                    buyStrengthPercent = 0.0,
                    sellStrengthPercent = 0.0,
                    oiQty = oiQty,
                    oiChange = oiChange,
                    timestamp = timestamp
                )
            }

            val buyStrengthPercent = if (first.buyQty != 0) {
                ((buyQty - first.buyQty).toDouble() / first.buyQty) * 100
            } else 0.0

            val sellStrengthPercent = if (first.sellQty != 0) {
                ((sellQty - first.sellQty).toDouble() / first.sellQty) * 100
            } else 0.0

            val currentOption = OptionsEntity(
                name = name,
                ltp = ltp,
                buyQty = buyQty,
                sellQty = sellQty,
                volTraded = volTraded,
                buyDiffPercent = buyDiffPercent,
                sellDiffPercent = sellDiffPercent,
                buyStrengthPercent = buyStrengthPercent,
                sellStrengthPercent = sellStrengthPercent,
                oiQty = oiQty,
                oiChange = oiChange,
                timestamp = timestamp
            )

            previousOptionMap[name] = currentOption
            optionsList.add(currentOption)
        }

        return optionsList
    }


    private suspend fun saveToDatabase(optionsList: List<OptionsEntity>) {
        withContext(Dispatchers.IO) {
            for (option in optionsList) {
                optionRepository.insertOption(option)
            }
        }
    }

    private val securityIdToSymbol = mapOf(
        48127 to "PE22000",
        48129 to "PE22050",
        48131 to "PE22100",
        48133 to "PE22150",
        48147 to "PE22200",
        48150 to "PE22250",
        48155 to "PE22300",
        48169 to "PE22350",
        48171 to "PE22400",
        48173 to "PE22450",
        48177 to "PE22500",
        48179 to "PE22550",
        48185 to "PE22600",
        48187 to "PE22650",
        48189 to "PE22700",
        48197 to "PE22750",
        48199 to "PE22800",
        48201 to "PE22850",
        48207 to "PE22900",
        48209 to "PE22950",
        48210 to "CE23000",
        48211 to "PE23000",
        48213 to "CE23050",
        48218 to "CE23100",
        48226 to "CE23150",
        48229 to "CE23200",
        48234 to "CE23250",
        48236 to "CE23300",
        48241 to "CE23350",
        48249 to "CE23400",
        48259 to "CE23450",
        48264 to "CE23500",
        48267 to "CE23550",
        48269 to "CE23600",
        48277 to "CE23650",
        48282 to "CE23700",
        48287 to "CE23750",
        48289 to "CE23800",
        48292 to "CE23850",
        48294 to "CE23900",
        48296 to "CE23950",
        48298 to "CE24000"
    )

    fun formatToHourMinute(unixTimestamp: Long): String {
        return if (unixTimestamp > 0) {
            val date = Date(unixTimestamp * 1000) // Convert to milliseconds
            val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())
            formatter.format(date)
        } else {
            "--:--"
        }
    }
}
