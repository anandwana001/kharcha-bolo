package com.example

import android.content.Intent
import android.os.SystemClock
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.agora.AgoraExpenseController
import com.example.ai.AiEngineType
import com.example.ai.KharchaAgentManager
import com.example.data.AppDatabase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

/** Opt-in: uses a real Agora project and temporary backend; may incur usage. */
@RunWith(AndroidJUnit4::class)
class AgoraExpenseSmokeTest {
    @Test fun rtcAgentAndExpenseRelayRoundTrip() = runBlocking {
        assumeTrue(InstrumentationRegistry.getArguments().getString("runAgora") == "1")
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        instrumentation.uiAutomation.grantRuntimePermission(context.packageName, android.Manifest.permission.RECORD_AUDIO)
        instrumentation.startActivitySync(Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        val db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        val manager = KharchaAgentManager(context, db.expenseDao())
        lateinit var controller: AgoraExpenseController
        instrumentation.runOnMainSync { controller = AgoraExpenseController(context) { manager.execute(it, AiEngineType.LOCAL_OFFLINE) }; controller.start() }
        try {
            val deadline = SystemClock.elapsedRealtime() + 90_000
            while (!controller.active.value && SystemClock.elapsedRealtime() < deadline) {
                if (!controller.busy.value) break
                Thread.sleep(200)
            }
            assertTrue("Cloud session did not start: ${controller.status.value}", controller.active.value)
            while (!controller.snapshot.value.isAgentRtcConnected && SystemClock.elapsedRealtime() < deadline) Thread.sleep(200)
            assertTrue("Agent did not join RTC: ${controller.snapshot.value.issues}", controller.snapshot.value.isAgentRtcConnected)
            instrumentation.runOnMainSync { controller.sendText("500 ka mithai") }
            val resultDeadline = SystemClock.elapsedRealtime() + 60_000
            while (db.expenseDao().getAllExpenses().isEmpty() && SystemClock.elapsedRealtime() < resultDeadline) Thread.sleep(200)
            assertEquals("Cloud-to-phone expense relay: ${controller.status.value}", 500.0, db.expenseDao().getTotalExpense(), 0.0)
            assertEquals(1, db.expenseDao().getAllExpenses().size)
            instrumentation.runOnMainSync { controller.stop() }
            val stopDeadline = SystemClock.elapsedRealtime() + 30_000
            while (controller.busy.value && SystemClock.elapsedRealtime() < stopDeadline) Thread.sleep(200)
            assertFalse(controller.active.value)
            assertFalse(controller.busy.value)
        } finally {
            instrumentation.runOnMainSync { controller.close() }
            // Controller shutdown can have pending backend cleanup, but relay work is cancelled.
            manager.close(); db.close()
        }
    }
}
