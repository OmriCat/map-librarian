package com.omricat.result.assertk

import assertk.assertFailure
import assertk.assertThat
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import kotlin.test.Test

internal class AssertionsTest {

    @Test
    fun `isOk() succeeds for Ok`() {
        val ok: Result<String, String> = Ok("Something")
        assertThat(ok).isOk()
    }

    @Test
    fun `isOk() fails for Err`() {
        val err: Result<String, String> = Err("Error")
        assertFailure { assertThat(err).isOk() }
    }
    @Test
    fun `isErr() succeeds for Err`() {
        val err: Result<String, String> = Err("Error")
        assertThat(err).isErr()
    }
    @Test
    fun `isErr() fails for Ok`() {
        val err: Result<String, String> = Ok("Error")
        assertFailure { assertThat(err).isErr() }
    }
}
