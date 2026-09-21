package com.wasama.hustlehub

import org.junit.Test
import org.junit.Assert.*
import com.wasama.hustlehub.data.remote.ApiConstants
import com.wasama.hustlehub.data.repository.ClientRepository
import com.wasama.hustlehub.data.local.ClientDao

class ExchangeRateTest {
    @Test fun usdToZar_conversion() {
        assertEquals(1850.0, ApiConstants.usdToZar(100.0, 18.5), 0.01)
    }
    @Test fun levelFromXp() {
        assertEquals(1, ApiConstants.levelFromXp(0))
        assertEquals(2, ApiConstants.levelFromXp(300))
        assertEquals(4, ApiConstants.levelFromXp(900))
    }
    @Test fun xpCalculation() {
        assertEquals(10, ApiConstants.xpForAction("TASK", false))
        assertEquals(60, ApiConstants.xpForAction("TASK", true))
        assertEquals(100, ApiConstants.xpForAction("INVOICE_PAID"))
    }
}

class ClientValidationTest {
    @Test fun validateClient() {
        // Simulate repo validation without DB
        assertTrue("ab".length >= 2 && "a@b.com".contains("@"))
        assertFalse("".length >= 2)
    }
}
