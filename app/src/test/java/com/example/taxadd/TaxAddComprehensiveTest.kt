package com.example.taxadd

import com.example.taxadd.data.InMemorySharedPreferences
import com.example.taxadd.domain.CurrencyRepository
import com.example.taxadd.domain.MoneyFormatter
import com.example.taxadd.domain.MoneyParser
import com.example.taxadd.domain.TaxAddCalculator
import com.example.taxadd.domain.TaxAddFormatter
import com.example.taxadd.domain.TaxAddValidator
import com.example.taxadd.domain.TaxRateFormatter
import com.example.taxadd.model.AppThemeMode
import com.example.taxadd.model.CalculationState
import com.example.taxadd.model.CurrencyOption
import com.example.taxadd.repository.SettingsRepository
import com.example.taxadd.repository.TaxAddHistoryRepository
import com.example.taxadd.ui.TaxAddViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TaxAddComprehensiveTest {

    // 1. Zero tax & CRITICAL TEST — ZERO TAX
    @Test
    fun test01_zeroTax() {
        val result = TaxAddCalculator.calculate(
            baseAmountMinorUnits = 10_000L, // $100.00
            taxRateBasisPoints = 0,          // 0%
            currency = CurrencyRepository.USD
        )
        assertEquals(10_000L, result.baseAmountMinorUnits)
        assertEquals(0L, result.taxAmountMinorUnits)
        assertEquals(10_000L, result.totalAmountMinorUnits)
        assertTrue(result.isZeroTax)
        assertEquals("$0.00", TaxAddFormatter.formatMoney(result.taxAmountMinorUnits, result.currency))
        assertEquals("$100.00", TaxAddFormatter.formatMoney(result.totalAmountMinorUnits, result.currency))
    }

    // 2. 5% tax
    @Test
    fun test02_fivePercentTax() {
        val result = TaxAddCalculator.calculate(10_000L, 500, CurrencyRepository.USD)
        assertEquals(500L, result.taxAmountMinorUnits)
        assertEquals(10_500L, result.totalAmountMinorUnits)
        assertEquals("$5.00", TaxAddFormatter.formatMoney(result.taxAmountMinorUnits, result.currency))
        assertEquals("$105.00", TaxAddFormatter.formatMoney(result.totalAmountMinorUnits, result.currency))
    }

    // 3. 10% tax
    @Test
    fun test03_tenPercentTax() {
        val result = TaxAddCalculator.calculate(10_000L, 1_000, CurrencyRepository.USD)
        assertEquals(1_000L, result.taxAmountMinorUnits)
        assertEquals(11_000L, result.totalAmountMinorUnits)
        assertEquals("$10.00", TaxAddFormatter.formatMoney(result.taxAmountMinorUnits, result.currency))
        assertEquals("$110.00", TaxAddFormatter.formatMoney(result.totalAmountMinorUnits, result.currency))
    }

    // 4. 15% tax
    @Test
    fun test04_fifteenPercentTax() {
        val result = TaxAddCalculator.calculate(10_000L, 1_500, CurrencyRepository.USD)
        assertEquals(1_500L, result.taxAmountMinorUnits)
        assertEquals(11_500L, result.totalAmountMinorUnits)
        assertEquals("$15.00", TaxAddFormatter.formatMoney(result.taxAmountMinorUnits, result.currency))
        assertEquals("$115.00", TaxAddFormatter.formatMoney(result.totalAmountMinorUnits, result.currency))
    }

    // 5. 18% tax & CRITICAL TEST — BASIC TAX
    @Test
    fun test05_eighteenPercentTax_criticalBasicTax() {
        val result = TaxAddCalculator.calculate(10_000L, 1_800, CurrencyRepository.USD)
        assertEquals(1_800L, result.taxAmountMinorUnits)
        assertEquals(11_800L, result.totalAmountMinorUnits)
        assertEquals("$18.00", TaxAddFormatter.formatMoney(result.taxAmountMinorUnits, result.currency))
        assertEquals("$118.00", TaxAddFormatter.formatMoney(result.totalAmountMinorUnits, result.currency))

        // Core experience example: $500 at 18% = $90 tax, $590 total
        val fiveHundred = TaxAddCalculator.calculate(50_000L, 1_800, CurrencyRepository.USD)
        assertEquals(9_000L, fiveHundred.taxAmountMinorUnits)
        assertEquals(59_000L, fiveHundred.totalAmountMinorUnits)
    }

    // 6. 20% tax
    @Test
    fun test06_twentyPercentTax() {
        val result = TaxAddCalculator.calculate(10_000L, 2_000, CurrencyRepository.USD)
        assertEquals(2_000L, result.taxAmountMinorUnits)
        assertEquals(12_000L, result.totalAmountMinorUnits)
        assertEquals("$20.00", TaxAddFormatter.formatMoney(result.taxAmountMinorUnits, result.currency))
        assertEquals("$120.00", TaxAddFormatter.formatMoney(result.totalAmountMinorUnits, result.currency))
    }

    // 7. 25% tax
    @Test
    fun test07_twentyFivePercentTax() {
        val result = TaxAddCalculator.calculate(10_000L, 2_500, CurrencyRepository.USD)
        assertEquals(2_500L, result.taxAmountMinorUnits)
        assertEquals(12_500L, result.totalAmountMinorUnits)
        assertEquals("$25.00", TaxAddFormatter.formatMoney(result.taxAmountMinorUnits, result.currency))
        assertEquals("$125.00", TaxAddFormatter.formatMoney(result.totalAmountMinorUnits, result.currency))
    }

    // 8. Decimal tax rate & CRITICAL TEST — DECIMAL TAX
    @Test
    fun test08_decimalTaxRates() {
        // 7.5% on $100.00 -> Tax $7.50, Total $107.50
        val res75 = TaxAddCalculator.calculate(10_000L, 750, CurrencyRepository.USD)
        assertEquals(750L, res75.taxAmountMinorUnits)
        assertEquals(10_750L, res75.totalAmountMinorUnits)
        assertEquals("$7.50", TaxAddFormatter.formatMoney(res75.taxAmountMinorUnits, res75.currency))
        assertEquals("$107.50", TaxAddFormatter.formatMoney(res75.totalAmountMinorUnits, res75.currency))

        // 12.5% on $100.00 -> Tax $12.50, Total $112.50
        val res125 = TaxAddCalculator.calculate(10_000L, 1_250, CurrencyRepository.USD)
        assertEquals(1_250L, res125.taxAmountMinorUnits)
        assertEquals(11_250L, res125.totalAmountMinorUnits)

        // 17.5% on $100.00 -> Tax $17.50, Total $117.50
        val res175 = TaxAddCalculator.calculate(10_000L, 1_750, CurrencyRepository.USD)
        assertEquals(1_750L, res175.taxAmountMinorUnits)
        assertEquals(11_750L, res175.totalAmountMinorUnits)

        // 17.75% on $100.00 -> Tax $17.75, Total $117.75
        val res1775 = TaxAddCalculator.calculate(10_000L, 1_775, CurrencyRepository.USD)
        assertEquals(1_775L, res1775.taxAmountMinorUnits)
        assertEquals(11_775L, res1775.totalAmountMinorUnits)
    }

    // 9. 100% tax & CRITICAL TEST — 100% TAX
    @Test
    fun test09_oneHundredPercentTax() {
        val result = TaxAddCalculator.calculate(10_000L, 10_000, CurrencyRepository.USD)
        assertEquals(10_000L, result.taxAmountMinorUnits)
        assertEquals(20_000L, result.totalAmountMinorUnits)
        assertEquals("$100.00", TaxAddFormatter.formatMoney(result.taxAmountMinorUnits, result.currency))
        assertEquals("$200.00", TaxAddFormatter.formatMoney(result.totalAmountMinorUnits, result.currency))
    }

    // 10. Zero base amount & CRITICAL TEST — ZERO BASE
    @Test
    fun test10_zeroBaseAmount() {
        val result = TaxAddCalculator.calculate(0L, 1_800, CurrencyRepository.USD)
        assertEquals(0L, result.baseAmountMinorUnits)
        assertEquals(0L, result.taxAmountMinorUnits)
        assertEquals(0L, result.totalAmountMinorUnits)
        assertEquals("$0.00", TaxAddFormatter.formatMoney(result.taxAmountMinorUnits, result.currency))
        assertEquals("$0.00", TaxAddFormatter.formatMoney(result.totalAmountMinorUnits, result.currency))
    }

    // 11. Negative base amount
    @Test
    fun test11_negativeBaseAmountRejected() {
        val parseResult = MoneyParser.parse("-50.00", CurrencyRepository.USD)
        assertTrue(parseResult.isFailure)
        assertEquals("Base amount cannot be negative.", parseResult.exceptionOrNull()?.message)

        try {
            TaxAddCalculator.calculate(-100L, 1_800, CurrencyRepository.USD)
            org.junit.Assert.fail("Expected IllegalArgumentException for negative base amount")
        } catch (e: IllegalArgumentException) {
            assertEquals("Base amount cannot be negative.", e.message)
        }
    }

    // 12. Negative tax rate
    @Test
    fun test12_negativeTaxRateRejected() {
        val rateValidation = TaxAddValidator.validateAndParseTaxRate("-1")
        assertTrue(rateValidation.isFailure)
        assertEquals("Tax rate cannot be negative.", rateValidation.exceptionOrNull()?.message)

        try {
            TaxAddCalculator.calculate(10_000L, -100, CurrencyRepository.USD)
            org.junit.Assert.fail("Expected IllegalArgumentException for negative tax rate")
        } catch (e: IllegalArgumentException) {
            assertEquals("Tax rate cannot be negative.", e.message)
        }
    }

    // 13. Tax rate above 100%
    @Test
    fun test13_taxRateAbove100PercentRejected() {
        val over100Point01 = TaxAddValidator.validateAndParseTaxRate("100.01")
        assertTrue(over100Point01.isFailure)
        assertEquals("Tax rate must be between 0% and 100%.", over100Point01.exceptionOrNull()?.message)

        val over101 = TaxAddValidator.validateAndParseTaxRate("101")
        assertTrue(over101.isFailure)
        assertEquals("Tax rate must be between 0% and 100%.", over101.exceptionOrNull()?.message)

        try {
            TaxAddCalculator.calculate(10_000L, 10_001, CurrencyRepository.USD)
            org.junit.Assert.fail("Expected IllegalArgumentException for tax rate > 100%")
        } catch (e: IllegalArgumentException) {
            assertEquals("Tax rate must be between 0% and 100%.", e.message)
        }
    }

    // 14. Tax calculation
    @Test
    fun test14_taxCalculationExactMinorUnits() {
        // $250.00 at 18.25% -> 25000 * 1825 / 10000 = 4562.5 -> rounds half up to 4563 ($45.63)
        val result = TaxAddCalculator.calculate(25_000L, 1_825, CurrencyRepository.USD)
        assertEquals(4_563L, result.taxAmountMinorUnits)
        assertEquals(29_563L, result.totalAmountMinorUnits)
    }

    // 15. Tax rounding & CRITICAL TEST — ROUNDING ($99.99 at 18%)
    @Test
    fun test15_taxRounding_critical9999At18Percent() {
        // Base: $99.99 (9999 cents), Tax: 18% (1800 bp)
        // Exact tax: $17.9982 -> Expected rounded tax: $18.00 (1800 cents)
        // Expected total: $117.99 (11799 cents)
        val result = TaxAddCalculator.calculate(9_999L, 1_800, CurrencyRepository.USD)
        assertEquals(9_999L, result.baseAmountMinorUnits)
        assertEquals(1_800L, result.taxAmountMinorUnits)
        assertEquals(11_799L, result.totalAmountMinorUnits)

        val formattedTax = TaxAddFormatter.formatMoney(result.taxAmountMinorUnits, result.currency)
        val formattedTotal = TaxAddFormatter.formatMoney(result.totalAmountMinorUnits, result.currency)
        assertEquals("$18.00", formattedTax)
        assertEquals("$117.99", formattedTotal)
        assertNotEquals("$117.9882", formattedTotal)
    }

    // 16. Total calculation
    @Test
    fun test16_totalCalculation() {
        val result = TaxAddCalculator.calculate(123_456L, 1_500, CurrencyRepository.USD)
        // 123456 * 1500 / 10000 = 18518.4 -> 18518
        assertEquals(18_518L, result.taxAmountMinorUnits)
        assertEquals(123_456L + 18_518L, result.totalAmountMinorUnits)
    }

    // 17. USD
    @Test
    fun test17_currencyUSD() {
        val usd = CurrencyRepository.USD
        assertEquals(2, usd.decimalPlaces)
        val result = TaxAddCalculator.calculate(10_000L, 1_800, usd)
        assertEquals("$100.00", TaxAddFormatter.formatMoney(result.baseAmountMinorUnits, usd))
        assertEquals("$18.00", TaxAddFormatter.formatMoney(result.taxAmountMinorUnits, usd))
        assertEquals("$118.00", TaxAddFormatter.formatMoney(result.totalAmountMinorUnits, usd))
    }

    // 18. EUR
    @Test
    fun test18_currencyEUR() {
        val eur = CurrencyRepository.EUR
        assertEquals(2, eur.decimalPlaces)
        val result = TaxAddCalculator.calculate(20_000L, 2_000, eur)
        assertEquals("€200.00", TaxAddFormatter.formatMoney(result.baseAmountMinorUnits, eur))
        assertEquals("€40.00", TaxAddFormatter.formatMoney(result.taxAmountMinorUnits, eur))
        assertEquals("€240.00", TaxAddFormatter.formatMoney(result.totalAmountMinorUnits, eur))
    }

    // 19. GBP
    @Test
    fun test19_currencyGBP() {
        val gbp = CurrencyRepository.GBP
        assertEquals(2, gbp.decimalPlaces)
        val result = TaxAddCalculator.calculate(15_000L, 2_000, gbp)
        assertEquals("£150.00", TaxAddFormatter.formatMoney(result.baseAmountMinorUnits, gbp))
        assertEquals("£30.00", TaxAddFormatter.formatMoney(result.taxAmountMinorUnits, gbp))
        assertEquals("£180.00", TaxAddFormatter.formatMoney(result.totalAmountMinorUnits, gbp))
    }

    // 20. PKR & CRITICAL TEST — PKR
    @Test
    fun test20_currencyPKR_criticalPKR() {
        val pkr = CurrencyRepository.PKR
        assertEquals(2, pkr.decimalPlaces)
        // Base: Rs. 1,000.00 (100_000 paisa), Tax: 18% -> Tax: Rs. 180.00, Total: Rs. 1,180.00
        val result = TaxAddCalculator.calculate(100_000L, 1_800, pkr)
        assertEquals("Rs. 1,000.00", TaxAddFormatter.formatMoney(result.baseAmountMinorUnits, pkr))
        assertEquals("Rs. 180.00", TaxAddFormatter.formatMoney(result.taxAmountMinorUnits, pkr))
        assertEquals("Rs. 1,180.00", TaxAddFormatter.formatMoney(result.totalAmountMinorUnits, pkr))
    }

    // 21. AED
    @Test
    fun test21_currencyAED() {
        val aed = CurrencyRepository.AED
        val result = TaxAddCalculator.calculate(100_000L, 500, aed)
        assertEquals("AED 1,000.00", TaxAddFormatter.formatMoney(result.baseAmountMinorUnits, aed))
        assertEquals("AED 50.00", TaxAddFormatter.formatMoney(result.taxAmountMinorUnits, aed))
        assertEquals("AED 1,050.00", TaxAddFormatter.formatMoney(result.totalAmountMinorUnits, aed))
    }

    // 22. SAR
    @Test
    fun test22_currencySAR() {
        val sar = CurrencyRepository.SAR
        val result = TaxAddCalculator.calculate(100_000L, 1_500, sar)
        assertEquals("SAR 1,000.00", TaxAddFormatter.formatMoney(result.baseAmountMinorUnits, sar))
        assertEquals("SAR 150.00", TaxAddFormatter.formatMoney(result.taxAmountMinorUnits, sar))
        assertEquals("SAR 1,150.00", TaxAddFormatter.formatMoney(result.totalAmountMinorUnits, sar))
    }

    // 23. CAD
    @Test
    fun test23_currencyCAD() {
        val cad = CurrencyRepository.CAD
        val result = TaxAddCalculator.calculate(10_000L, 1_300, cad)
        assertEquals("CA$100.00", TaxAddFormatter.formatMoney(result.baseAmountMinorUnits, cad))
        assertEquals("CA$13.00", TaxAddFormatter.formatMoney(result.taxAmountMinorUnits, cad))
        assertEquals("CA$113.00", TaxAddFormatter.formatMoney(result.totalAmountMinorUnits, cad))
    }

    // 24. AUD
    @Test
    fun test24_currencyAUD() {
        val aud = CurrencyRepository.AUD
        val result = TaxAddCalculator.calculate(10_000L, 1_000, aud)
        assertEquals("A$100.00", TaxAddFormatter.formatMoney(result.baseAmountMinorUnits, aud))
        assertEquals("A$10.00", TaxAddFormatter.formatMoney(result.taxAmountMinorUnits, aud))
        assertEquals("A$110.00", TaxAddFormatter.formatMoney(result.totalAmountMinorUnits, aud))
    }

    // 25. INR
    @Test
    fun test25_currencyINR() {
        val inr = CurrencyRepository.INR
        val result = TaxAddCalculator.calculate(100_000L, 1_800, inr)
        assertEquals("₹1,000.00", TaxAddFormatter.formatMoney(result.baseAmountMinorUnits, inr))
        assertEquals("₹180.00", TaxAddFormatter.formatMoney(result.taxAmountMinorUnits, inr))
        assertEquals("₹1,180.00", TaxAddFormatter.formatMoney(result.totalAmountMinorUnits, inr))
    }

    // 26. JPY & CRITICAL TEST — JPY
    @Test
    fun test26_currencyJPY_criticalJPY() {
        val jpy = CurrencyRepository.JPY
        assertEquals(0, jpy.decimalPlaces)
        // Base: ¥1,000, Tax: 10% -> Expected Tax: ¥100, Total: ¥1,100
        val result = TaxAddCalculator.calculate(1_000L, 1_000, jpy)
        assertEquals(100L, result.taxAmountMinorUnits)
        assertEquals(1_100L, result.totalAmountMinorUnits)
        assertEquals("¥1,000", TaxAddFormatter.formatMoney(result.baseAmountMinorUnits, jpy))
        assertEquals("¥100", TaxAddFormatter.formatMoney(result.taxAmountMinorUnits, jpy))
        assertEquals("¥1,100", TaxAddFormatter.formatMoney(result.totalAmountMinorUnits, jpy))
    }

    // 27. Zero-decimal currency
    @Test
    fun test27_zeroDecimalCurrencyPrecision() {
        val jpy = CurrencyRepository.JPY
        assertEquals(1_000L, MoneyParser.parse("1000", jpy).getOrThrow())
        val decimalAttempt = MoneyParser.parse("1000.5", jpy)
        assertTrue(decimalAttempt.isFailure)
        assertEquals("Review the amount for the selected currency.", decimalAttempt.exceptionOrNull()?.message)
    }

    // 28. Two-decimal currency
    @Test
    fun test28_twoDecimalCurrencyPrecision() {
        val cny = CurrencyRepository.CNY
        assertEquals(2, cny.decimalPlaces)
        assertEquals(125_000L, MoneyParser.parse("1250.00", cny).getOrThrow())
        assertEquals("¥1,250.00", MoneyFormatter.format(125_000L, cny))
        val threeDecimalsAttempt = MoneyParser.parse("100.125", cny)
        assertTrue(threeDecimalsAttempt.isFailure)
        assertEquals("Review the amount for the selected currency.", threeDecimalsAttempt.exceptionOrNull()?.message)
    }

    // 29. Custom currency (0-4 decimal places)
    @Test
    fun test29_customCurrencyPrecisionZeroToFourDecimals() {
        val bhd = CurrencyRepository.createCustomCurrency("BHD", "BD", "Bahraini Dinar", 3)
        assertEquals(3, bhd.decimalPlaces)
        val parsed = MoneyParser.parse("100.125", bhd).getOrThrow()
        assertEquals(100_125L, parsed)
        val result = TaxAddCalculator.calculate(parsed, 1_000, bhd) // 10%
        assertEquals(10_013L, result.taxAmountMinorUnits) // 10012.5 -> 10013
        assertEquals("BD 110.138", TaxAddFormatter.formatMoney(result.totalAmountMinorUnits, bhd))

        val clp4 = CurrencyRepository.createCustomCurrency("CL4", "C$", "Four Decimal Custom", 4)
        assertEquals(4, clp4.decimalPlaces)
        assertEquals(1_000_005L, MoneyParser.parse("100.0005", clp4).getOrThrow())
    }

    // 30. Money parsing
    @Test
    fun test30_moneyParsing() {
        assertEquals(10_000L, MoneyParser.parse("100", CurrencyRepository.USD).getOrThrow())
        assertEquals(10_000L, MoneyParser.parse("100.00", CurrencyRepository.USD).getOrThrow())
        assertEquals(10_050L, MoneyParser.parse("100.5", CurrencyRepository.USD).getOrThrow())
        assertEquals(125_000L, MoneyParser.parse("1,250.00", CurrencyRepository.USD).getOrThrow())
        assertEquals(0L, MoneyParser.parse("0", CurrencyRepository.USD).getOrThrow())
        assertTrue(MoneyParser.parse("", CurrencyRepository.USD).isFailure)
        assertTrue(MoneyParser.parse("abc", CurrencyRepository.USD).isFailure)
    }

    // 31. Money formatting
    @Test
    fun test31_moneyFormatting() {
        assertEquals("$1,250.00", MoneyFormatter.format(125_000L, CurrencyRepository.USD))
        assertEquals("Rs. 1,250.00", MoneyFormatter.format(125_000L, CurrencyRepository.PKR))
        assertEquals("¥1,250", MoneyFormatter.format(1_250L, CurrencyRepository.JPY))
    }

    // 32. Tax rate parsing
    @Test
    fun test32_taxRateParsing() {
        assertEquals(500, TaxAddValidator.validateAndParseTaxRate("5").getOrThrow())
        assertEquals(750, TaxAddValidator.validateAndParseTaxRate("7.5").getOrThrow())
        assertEquals(1_800, TaxAddValidator.validateAndParseTaxRate("18%").getOrThrow())
        assertEquals(1_825, TaxAddValidator.validateAndParseTaxRate("18.25").getOrThrow())
        assertEquals(10_000, TaxAddValidator.validateAndParseTaxRate("100").getOrThrow())
        assertTrue(TaxAddValidator.validateAndParseTaxRate("18.125").isFailure)
    }

    // 33. Tax rate formatting
    @Test
    fun test33_taxRateFormatting() {
        assertEquals("0%", TaxRateFormatter.format(0))
        assertEquals("5%", TaxRateFormatter.format(500))
        assertEquals("7.5%", TaxRateFormatter.format(750))
        assertEquals("12.25%", TaxRateFormatter.format(1_225))
        assertEquals("18%", TaxRateFormatter.format(1_800))
        assertEquals("100%", TaxRateFormatter.format(10_000))
    }

    // 34. Half-up rounding
    @Test
    fun test34_halfUpRoundingExactBoundary() {
        // $0.05 (5 cents) at 50% (5000 bp) -> 5 * 5000 / 10000 = 2.5 cents -> rounds HALF UP to 3 cents
        val halfBoundary = TaxAddCalculator.calculate(5L, 5_000, CurrencyRepository.USD)
        assertEquals(3L, halfBoundary.taxAmountMinorUnits)
        assertEquals(8L, halfBoundary.totalAmountMinorUnits)

        // $0.05 (5 cents) at 49% (4900 bp) -> 5 * 4900 / 10000 = 2.45 cents -> rounds down to 2 cents
        val belowHalf = TaxAddCalculator.calculate(5L, 4_900, CurrencyRepository.USD)
        assertEquals(2L, belowHalf.taxAmountMinorUnits)
        assertEquals(7L, belowHalf.totalAmountMinorUnits)
    }

    // 35. Large amounts & CRITICAL TEST — LARGE VALUE
    @Test
    fun test35_largeAmounts_criticalLargeValue() {
        // $10,000,000,000.00 (10 billion dollars = 1_000_000_000_000 cents) at 18%
        val baseMinor = 1_000_000_000_000L
        val result = TaxAddCalculator.calculate(baseMinor, 1_800, CurrencyRepository.USD)
        assertEquals(180_000_000_000L, result.taxAmountMinorUnits)
        assertEquals(1_180_000_000_000L, result.totalAmountMinorUnits)
        assertEquals("$11,800,000,000.00", TaxAddFormatter.formatMoney(result.totalAmountMinorUnits, CurrencyRepository.USD))
    }

    // 36. Overflow protection & CRITICAL TEST — OVERFLOW
    @Test
    fun test36_overflowProtection_criticalOverflow() {
        // Multiplication overflow in calculator
        try {
            TaxAddCalculator.calculate(Long.MAX_VALUE / 2L, 10_000, CurrencyRepository.USD)
            org.junit.Assert.fail("Expected ArithmeticException for overflow")
        } catch (e: ArithmeticException) {
            assertEquals("Amount is too large.", e.message)
        }

        // Parser overflow protection
        val hugeParse = MoneyParser.parse("999999999999999999999999.99", CurrencyRepository.USD)
        assertTrue(hugeParse.isFailure)
        assertEquals("Amount is too large.", hugeParse.exceptionOrNull()?.message)
    }

    // 37. Core total invariant & CRITICAL TEST — TOTAL INVARIANT
    @Test
    fun test37_coreTotalInvariant() {
        val sampleAmounts = listOf(0L, 1L, 5L, 99L, 100L, 9_999L, 10_000L, 123_456_789L)
        val sampleRates = listOf(0, 1, 75, 500, 750, 1_225, 1_775, 1_800, 2_500, 9_999, 10_000)

        for (base in sampleAmounts) {
            for (rate in sampleRates) {
                val res = TaxAddCalculator.calculate(base, rate, CurrencyRepository.USD)
                assertEquals(
                    "Invariant violated for base=$base, rate=$rate",
                    res.baseAmountMinorUnits + res.taxAmountMinorUnits,
                    res.totalAmountMinorUnits
                )
            }
        }
    }

    // 38. Zero tax invariant
    @Test
    fun test38_zeroTaxInvariant() {
        val amounts = listOf(0L, 1L, 9_999L, 500_000L, 99_999_999L)
        for (base in amounts) {
            val res = TaxAddCalculator.calculate(base, 0, CurrencyRepository.USD)
            assertEquals(0L, res.taxAmountMinorUnits)
            assertEquals(base, res.totalAmountMinorUnits)
        }
    }

    // 39. 100% tax calculation
    @Test
    fun test39_hundredPercentTaxInvariant() {
        val amounts = listOf(0L, 1L, 9_999L, 500_000L, 99_999_999L)
        for (base in amounts) {
            val res = TaxAddCalculator.calculate(base, 10_000, CurrencyRepository.USD)
            assertEquals(base, res.taxAmountMinorUnits)
            assertEquals(base * 2L, res.totalAmountMinorUnits)
        }
    }

    // 40. History save & 41. History load
    @Test
    fun test40_41_historySaveAndLoad() {
        val prefs = InMemorySharedPreferences()
        val repo = TaxAddHistoryRepository(prefs)

        val calc1 = TaxAddCalculator.calculate(10_000L, 1_800, CurrencyRepository.USD)
        repo.saveCalculation(calc1, id = "id-1", calculatedAt = 1000L)

        val calc2 = TaxAddCalculator.calculate(50_000L, 2_000, CurrencyRepository.EUR)
        repo.saveCalculation(calc2, id = "id-2", calculatedAt = 2000L)

        val loaded = repo.loadHistory()
        assertEquals(2, loaded.size)
        assertEquals("id-2", loaded[0].id)
        assertEquals(60_000L, loaded[0].totalAmountMinorUnits)
        assertEquals("id-1", loaded[1].id)
        assertEquals(11_800L, loaded[1].totalAmountMinorUnits)
    }

    // 42. History limit (max 10, adding 11th removes oldest)
    @Test
    fun test42_historyLimitRemovesOldest() {
        val prefs = InMemorySharedPreferences()
        val repo = TaxAddHistoryRepository(prefs)

        for (i in 1..11) {
            val calc = TaxAddCalculator.calculate(i * 1_000L, 1_800, CurrencyRepository.USD)
            repo.saveCalculation(calc, id = "entry-$i", calculatedAt = i * 1000L)
        }

        val history = repo.loadHistory()
        assertEquals(10, history.size)
        assertEquals("entry-11", history.first().id)
        assertEquals("entry-2", history.last().id)
        assertFalse(history.any { it.id == "entry-1" })
    }

    // 43. History deletion
    @Test
    fun test43_historyDeleteIndividual() {
        val prefs = InMemorySharedPreferences()
        val repo = TaxAddHistoryRepository(prefs)
        val calc1 = TaxAddCalculator.calculate(10_000L, 1_800, CurrencyRepository.USD)
        val calc2 = TaxAddCalculator.calculate(20_000L, 1_000, CurrencyRepository.USD)
        repo.saveCalculation(calc1, id = "h1")
        repo.saveCalculation(calc2, id = "h2")

        val remaining = repo.deleteCalculation("h1")
        assertEquals(1, remaining.size)
        assertEquals("h2", remaining[0].id)
    }

    // 44. History clear
    @Test
    fun test44_historyClearAll() {
        val prefs = InMemorySharedPreferences()
        val repo = TaxAddHistoryRepository(prefs)
        val calc = TaxAddCalculator.calculate(10_000L, 1_800, CurrencyRepository.USD)
        repo.saveCalculation(calc, id = "h1")
        assertEquals(1, repo.loadHistory().size)

        val afterClear = repo.clearHistory()
        assertTrue(afterClear.isEmpty())
        assertTrue(repo.loadHistory().isEmpty())
    }

    // 45. History restore
    @Test
    fun test45_historyRestoreInViewModel() {
        val prefs = InMemorySharedPreferences()
        val vm = TaxAddViewModel(TaxAddHistoryRepository(prefs), SettingsRepository(prefs))

        vm.onCurrencySelected(CurrencyRepository.PKR)
        vm.onBaseAmountChanged("1000.00")
        vm.onTaxRateChanged("18")
        assertTrue(vm.saveCurrentCalculation())
        val savedItem = vm.uiState.value.history.first()

        // Change state to something else
        vm.onCurrencySelected(CurrencyRepository.JPY)
        vm.onBaseAmountChanged("500")
        vm.onTaxRateChanged("10")

        // Restore history item
        vm.restoreFromHistory(savedItem)
        val state = vm.uiState.value
        assertEquals("PKR", state.currency.code)
        assertEquals("1000.00", state.baseAmountInput)
        assertEquals("18", state.taxRateInput)
        assertNotNull(state.result)
        assertEquals(118_000L, state.result?.totalAmountMinorUnits)
    }

    // 46. Settings persistence
    @Test
    fun test46_settingsPersistence() {
        val prefs = InMemorySharedPreferences()
        val vm1 = TaxAddViewModel(TaxAddHistoryRepository(prefs), SettingsRepository(prefs))

        vm1.updateThemeMode(AppThemeMode.DARK)
        vm1.updateDefaultCurrency(CurrencyRepository.PKR)
        vm1.updateDynamicColor(false)
        vm1.updateHapticFeedback(false)
        vm1.updateAnimations(false)

        // Simulate app close and reopen with same SharedPreferences
        val vm2 = TaxAddViewModel(TaxAddHistoryRepository(prefs), SettingsRepository(prefs))
        val restored = vm2.uiState.value
        assertEquals(AppThemeMode.DARK, restored.settings.themeMode)
        assertEquals("PKR", restored.settings.defaultCurrencyCode)
        assertEquals("PKR", restored.currency.code)
        assertFalse(restored.settings.dynamicColor)
        assertFalse(restored.settings.hapticFeedback)
        assertFalse(restored.settings.animations)
    }

    // 47. Last-calculation persistence
    @Test
    fun test47_lastCalculationPersistence() {
        val prefs = InMemorySharedPreferences()
        val vm1 = TaxAddViewModel(TaxAddHistoryRepository(prefs), SettingsRepository(prefs))

        vm1.updatePreserveLastCalculation(true)
        vm1.onCurrencySelected(CurrencyRepository.EUR)
        vm1.onBaseAmountChanged("250.00")
        vm1.onTaxRateChanged("20")

        // Reopen with preserveLastCalculation = true
        val vm2 = TaxAddViewModel(TaxAddHistoryRepository(prefs), SettingsRepository(prefs))
        assertEquals("EUR", vm2.uiState.value.currency.code)
        assertEquals("250.00", vm2.uiState.value.baseAmountInput)
        assertEquals("20", vm2.uiState.value.taxRateInput)
        assertEquals(30_000L, vm2.uiState.value.result?.totalAmountMinorUnits)

        // Disable preserveLastCalculation -> reopen uses defaults ($100.00, 18%, USD)
        vm2.updatePreserveLastCalculation(false)
        vm2.updateDefaultCurrency(CurrencyRepository.USD)
        val vm3 = TaxAddViewModel(TaxAddHistoryRepository(prefs), SettingsRepository(prefs))
        assertEquals("USD", vm3.uiState.value.currency.code)
        assertEquals("100.00", vm3.uiState.value.baseAmountInput)
        assertEquals("18", vm3.uiState.value.taxRateInput)
    }

    // 48. Reset
    @Test
    fun test48_resetRestoresDefaultsWithoutClearingHistory() {
        val prefs = InMemorySharedPreferences()
        val vm = TaxAddViewModel(TaxAddHistoryRepository(prefs), SettingsRepository(prefs))

        vm.onCurrencySelected(CurrencyRepository.GBP)
        vm.onBaseAmountChanged("400.00")
        vm.onTaxRateChanged("20")
        vm.saveCurrentCalculation()
        assertEquals(1, vm.uiState.value.history.size)

        vm.reset()
        val state = vm.uiState.value
        assertEquals("100.00", state.baseAmountInput)
        assertEquals("18", state.taxRateInput)
        assertEquals("USD", state.currency.code)
        assertEquals(11_800L, state.result?.totalAmountMinorUnits)
        assertEquals(1, state.history.size) // History preserved
    }

    // 49. Copy formatting
    @Test
    fun test49_copyFormatting() {
        val result = TaxAddCalculator.calculate(10_000L, 1_800, CurrencyRepository.USD)
        val copyText = TaxAddFormatter.formatCopyText(result)
        val expected = """
            TaxAdd Result

            Base Amount: $100.00
            Tax Rate: 18%
            Tax: $18.00

            Total Including Tax: $118.00
        """.trimIndent()
        assertEquals(expected, copyText)
    }

    // 50. Share formatting
    @Test
    fun test50_shareFormatting() {
        val result = TaxAddCalculator.calculate(10_000L, 1_800, CurrencyRepository.USD)
        val shareText = TaxAddFormatter.formatShareText(result)
        val expected = """
            TaxAdd

            Base: $100.00
            Tax: 18% = $18.00

            Total: $118.00
        """.trimIndent()
        assertEquals(expected, shareText)

        // Also verify informational tax share summary
        assertEquals("Tax is 15.25% of the final total", TaxAddFormatter.formatTaxShareSummary(result))
    }

    // 51. Quick tax presets
    @Test
    fun test51_quickTaxPresets() {
        val prefs = InMemorySharedPreferences()
        val vm = TaxAddViewModel(TaxAddHistoryRepository(prefs), SettingsRepository(prefs))
        val expectedTotals = mapOf(
            0 to 10_000L,
            500 to 10_500L,
            1_000 to 11_000L,
            1_500 to 11_500L,
            1_800 to 11_800L,
            2_000 to 12_000L,
            2_500 to 12_500L
        )
        for ((bp, expectedTotal) in expectedTotals) {
            vm.selectQuickTaxPreset(bp)
            assertEquals(expectedTotal, vm.uiState.value.result?.totalAmountMinorUnits)
        }
    }

    // 52. Quick amount presets
    @Test
    fun test52_quickAmountPresets() {
        val prefs = InMemorySharedPreferences()
        val vm = TaxAddViewModel(TaxAddHistoryRepository(prefs), SettingsRepository(prefs))
        vm.selectQuickTaxPreset(1_800) // 18%

        val expectedTotals = mapOf(
            10L to 1_180L,
            50L to 5_900L,
            100L to 11_800L,
            500L to 59_000L,
            1_000L to 118_000L
        )
        for ((majorPreset, expectedTotal) in expectedTotals) {
            vm.selectQuickBaseAmountPreset(majorPreset)
            assertEquals(expectedTotal, vm.uiState.value.result?.totalAmountMinorUnits)
        }
    }

    // 53. Currency switching (no conversion, precision notice when needed)
    @Test
    fun test53_currencySwitchingPrecisionAndNoConversion() {
        val prefs = InMemorySharedPreferences()
        val vm = TaxAddViewModel(TaxAddHistoryRepository(prefs), SettingsRepository(prefs))

        // $100.00 -> ¥100 (no conversion, adapts redundant .00 cleanly)
        vm.onBaseAmountChanged("100.00")
        vm.onTaxRateChanged("10")
        vm.onCurrencySelected(CurrencyRepository.JPY)
        assertEquals("100", vm.uiState.value.baseAmountInput)
        assertEquals(110L, vm.uiState.value.result?.totalAmountMinorUnits)

        // Switch back to USD, enter $100.25, then switch to 0-decimal JPY:
        // Must NOT silently round; must show "Review the amount for the selected currency."
        vm.onCurrencySelected(CurrencyRepository.USD)
        vm.onBaseAmountChanged("100.25")
        assertNotNull(vm.uiState.value.result)

        vm.onCurrencySelected(CurrencyRepository.JPY)
        assertEquals("100.25", vm.uiState.value.baseAmountInput)
        assertNull(vm.uiState.value.result)
        assertEquals("Review the amount for the selected currency.", vm.uiState.value.errorMessage)
        assertTrue(vm.uiState.value.calculationState is CalculationState.Error)
    }

    // 54. Rapid calculation protection (prevent duplicate saves and stale results)
    @Test
    fun test54_rapidCalculationAndDoubleSaveProtection() {
        val prefs = InMemorySharedPreferences()
        val vm = TaxAddViewModel(TaxAddHistoryRepository(prefs), SettingsRepository(prefs))

        // First save succeeds
        assertTrue(vm.saveCurrentCalculation())
        assertEquals(1, vm.uiState.value.history.size)

        // Rapid double-tap on SAVE without changing calculation is ignored
        assertFalse(vm.saveCurrentCalculation())
        assertEquals(1, vm.uiState.value.history.size)

        // Invalid input immediately clears result so stale results are never shown
        vm.onBaseAmountChanged("-10")
        assertNull(vm.uiState.value.result)
        assertEquals("Base amount cannot be negative.", vm.uiState.value.errorMessage)
    }
}
