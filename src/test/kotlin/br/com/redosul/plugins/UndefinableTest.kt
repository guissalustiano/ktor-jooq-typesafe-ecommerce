package br.com.redosul.plugins

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class UndefinableTest : FunSpec({
    context("ifPresent") {
        test("should execute predicate when present") {
            var executed = false
            Undefinable.Defined(1).ifPresent { executed = true }
            executed shouldBe true
        }
        test("should not execute predicate when not present") {
            var executed = false
            Undefinable.Undefined.ifPresent { executed = true }
            executed shouldBe false
        }
    }
})
