package com.example.nifty50ops.utils

import android.content.Context
import androidx.compose.ui.graphics.GraphicsContext
import com.example.nifty50ops.controller.MarketController
import com.example.nifty50ops.controller.MarketController.FullMarketsEntity
import com.example.nifty50ops.database.MarketDatabase
import com.example.nifty50ops.repository.MarketRepository


public const val NIFTY50_ANALYSIS_PROMPT = """ 
    You are a leading stock market analyst.

Using the following data for the Nifty50 index for yesterday's trading day:

1. **Nifty50 Index Data:**
   - Timestamp: {timestamp}
   - Name: {name}
   - Last Traded Price (LTP): {ltp}
   - Open Price: {open}
   - High Price: {high}
   - Low Price: {low}
   - Close Price: {close}
   - Change Percent: {changePercent}%
   - Change Absolute: {changeAbsolute}

2. **Additional Summary Data:**  
   Please analyze the attached CSV files for:
   - Stock Summary (minute-wise cumulative data)  
   - Options Summary (20 Calls & 20 Puts cumulative data)  
   - Sentiment Summary (aggregated from stocks and options)

Please generate a detailed market analysis report for yesterday's trading day including:

- Yesterday’s Nifty50 Index Summary  
- Key Support and Resistance Levels derived from price action and option chain insights  
- Trend Analysis over the past week and month  
- Option Chain Analysis for upcoming expiry (max OI strikes, PCR overall and intraday)  
- Market Review (market behavior, drivers, investor sentiment)  
- Market Outlook for today (opening tone, intraday key levels)  
- Scenario-based Predictions for flat open, gap down, and gap up

Write the report in clear, natural language suitable for both retail and institutional investors.

---

**Note:** Use the CSV files to enrich your analysis with actual minute-level data and sentiment trends.

"""
