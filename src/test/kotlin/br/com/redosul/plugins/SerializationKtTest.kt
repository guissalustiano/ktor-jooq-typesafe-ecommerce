package br.com.redosul.plugins

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class SerializationKtTest : FunSpec({
    context("Undefinable serialization") {
        @Serializable
        data class Test(val name: String, val age: Undefinable<Int>)

        context("serialize") {
            test("should serialize defined") {
                val test = Test("John", Undefinable.Defined(30))
                val json = Json.encodeToString(test)
                json shouldBe """{"name":"John","age":30}"""
            }
            test("should serialize undefined") {
                val test = Test("John", Undefinable.Undefined)
                val json = Json.encodeToString(test)
                json shouldBe """{"name":"John"}"""
            }
        }

        context("deserialize") {
            test("should deserialize defined") {
                val json = """{"name":"John","age":30}"""
                val test = Json.decodeFromString<Test>(json)
                test shouldBe Test("John", Undefinable.Defined(30))
            }
            test("should deserialize undefined") {
                val json = """{"name":"John"}"""
                val test = Json.decodeFromString<Test>(json)
                test shouldBe Test("John", Undefinable.Undefined)
            }
        }
    }
})