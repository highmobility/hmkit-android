package com.highmobility.hmkit

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.highmobility.hmkit.error.BroadcastError
import junit.framework.Assert.fail
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.junit.runners.model.Statement

@RunWith(AndroidJUnit4::class)
class ThreadsTest {
    @Rule
    @JvmField
    var repeatRule: RepeatRule = RepeatRule()

    @RepeatTest(100)
    @Test
    fun testInitAndGetBroadcaster() {
        // create 2 threads. in one create the hmkit, and in other try to call
        // startBroadcasting
        var hmkit: HMKit? = null

        Log.d("test123", "start")

        val startBroadcastingThread = Thread {
            while (true) {
                if (hmkit != null) {
                    Log.d("test123", "hmkit $hmkit")
                    val broadcaster = hmkit?.broadcaster

                    Log.d("test123", "hmkit $hmkit")
                    break
                }
                else {
                    Log.d("test123", "hmkit is null")
                }
            }

            Log.d("test123", "1st thread has finished. ${Thread.currentThread()}")
        }

        startBroadcastingThread.start()

        val createHmkitThrad = Thread {
            val ctx: Context = ApplicationProvider.getApplicationContext()
            hmkit = HMKit.getInstance().initialise(
                "dGVzdJcGdhlYkeMKLIkjY7jPsIKmkMcJHCLZFFcP/htFBPpJITCpwrPBSBcQXU5Sqge+k179EYkbJdc79JBuSqmEhKcB27CCKnq8PsRAoiBegzQbsln9oyEnCVMXSNawr4nEUOs/1NWFeqX1cqZpy8Igm8LR9q44uTYApB0utNVZ3hf/VKHmYfxRyEJ6EMditry6N/v3z8at",
                "zo+Vy/UTH1YCB31gTuBD+7ADg4ZUSKBIyRMivq77KZc=",
                "K5mVFoq2rqKwAttWdIyPhwgVL80FNxkkNpgr/ca+ueq3JFn5iMLAMTJOKzG26qwtqrLO+z2sxxdwWNaItdBUWg==",
                ctx
            )

            Log.d("test123", "second thread has finished. ${Thread.currentThread()}")
        }

        createHmkitThrad.start()


        Thread.sleep(1000)
        hmkit?.terminate()
    }
}

class RepeatRule : TestRule {

    private class RepeatStatement(private val statement: Statement, private val repeat: Int) : Statement() {
        @Throws(Throwable::class)
        override fun evaluate() {
            for (i in 0..repeat - 1) {
                statement.evaluate()
            }
        }
    }

    override fun apply(statement: Statement, description: Description): Statement {
        var result = statement
        val repeat = description.getAnnotation(RepeatTest::class.java)
        if (repeat != null) {
            val times = repeat.value
            result = RepeatStatement(statement, times)
        }
        return result
    }
}