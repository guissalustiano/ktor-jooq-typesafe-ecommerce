package br.com.redosul

import com.github.javafaker.DateAndTime
import com.github.javafaker.Faker
import com.github.javafaker.PhoneNumber
import kotlinx.datetime.toKotlinInstant
import java.util.concurrent.TimeUnit

val faker = Faker()



fun DateAndTime.past() = past(1, TimeUnit.SECONDS).toInstant().toKotlinInstant()


fun PhoneNumber.fullPhoneWithoutPunctuation(): String  = "+1" + phoneNumber().replace("[^0-9]".toRegex(), "")