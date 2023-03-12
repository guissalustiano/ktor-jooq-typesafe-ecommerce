package br.com.redosul

import com.github.javafaker.DateAndTime
import com.github.javafaker.Faker
import kotlinx.datetime.toKotlinInstant
import java.time.Instant
import java.util.concurrent.TimeUnit

val faker = Faker()



fun DateAndTime.past() = past(1, TimeUnit.SECONDS).toInstant().toKotlinInstant()