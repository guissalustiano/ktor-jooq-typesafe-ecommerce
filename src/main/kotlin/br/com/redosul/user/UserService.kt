package br.com.redosul.user

import br.com.redosul.generated.tables.records.UserRecord
import br.com.redosul.generated.tables.records.UserProprietiesRecord
import br.com.redosul.generated.tables.references.USER_PROPRIETIES
import br.com.redosul.generated.tables.references.USER
import br.com.redosul.plugins.Email
import br.com.redosul.plugins.Name
import br.com.redosul.plugins.Phone
import br.com.redosul.plugins.await
import br.com.redosul.plugins.toKotlinInstant
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.kotlin.coroutines.transactionCoroutine
import java.time.OffsetDateTime

class UserService(private val dsl: DSLContext) {
    suspend fun findAll() : List<UserDto> = dsl
        .select(USER.asterisk(), USER_PROPRIETIES.asterisk())
        .from(USER_PROPRIETIES).join(USER)
        .on(USER_PROPRIETIES.USER_ID.eq(USER.ID))
        .await()
        .map{r -> r.toUserDto()}

    suspend fun findById(id: UserId) : UserDto? {
        return dsl.select(USER.asterisk(), USER_PROPRIETIES.asterisk())
            .from(USER_PROPRIETIES).join(USER)
            .on(USER_PROPRIETIES.USER_ID.eq(USER.ID))
            .where(USER_PROPRIETIES.ID.eq(id.value))
            .awaitFirstOrNull()
            ?.map{ r -> r.toUserDto() }
    }

    suspend fun create(payload: UserDto): UserDto {
        return dsl.transactionCoroutine {
            val dsl = it.dsl()
            val user =  dsl.insertInto(USER)
                .set(payload.toUserRecord())
                .set(USER_PROPRIETIES.ID, payload.id.value)
                .set(USER.UPDATED_AT, OffsetDateTime.now())
                .set(USER.CREATED_AT, OffsetDateTime.now())
                .returningResult(USER.asterisk())
                .awaitFirst()
                .into(USER)


            val userProprieties = dsl.insertInto(USER_PROPRIETIES)
                .set(payload.toUserProprietiesRecord())
                .set(USER_PROPRIETIES.ID, payload.id.value)
                .set(USER_PROPRIETIES.USER_ID, user.id)
                .set(USER_PROPRIETIES.UPDATED_AT, OffsetDateTime.now())
                .set(USER_PROPRIETIES.CREATED_AT, OffsetDateTime.now())
                .returningResult(USER_PROPRIETIES.asterisk())
                .awaitFirst()
                .into(USER_PROPRIETIES)

            userToDto(user, userProprieties)
        }
    }

    suspend fun updateById(id: UserId, payload: UserDto): UserDto? {
        return dsl.transactionCoroutine {
            val dsl = it.dsl()

            val userProprieties = dsl.update(USER_PROPRIETIES)
                .set(payload.toUserProprietiesRecord())
                .set(USER_PROPRIETIES.UPDATED_AT, OffsetDateTime.now())
                .where(USER_PROPRIETIES.ID.eq(id.value))
                .returningResult(USER_PROPRIETIES.asterisk())
                .awaitFirstOrNull()
                ?.into(USER_PROPRIETIES)

            userProprieties ?: return@transactionCoroutine null

            val user =  dsl.update(USER)
                .set(payload.toUserRecord())
                .set(USER.UPDATED_AT, OffsetDateTime.now())
                .where(USER.ID.eq(userProprieties.userId))
                .returningResult(USER.asterisk())
                .awaitFirstOrNull()
                ?.into(USER)!!


            userToDto(user, userProprieties)
        }
    }

    suspend fun deleteById(id: UserId) : UserDto? {
        return dsl.transactionCoroutine {
            val dsl = it.dsl()

            val userProprieties = dsl.deleteFrom(USER_PROPRIETIES)
                .where(USER_PROPRIETIES.ID.eq(id.value))
                .returningResult(USER_PROPRIETIES.asterisk())
                .awaitFirstOrNull()
                ?.into(USER_PROPRIETIES)


            userProprieties ?: return@transactionCoroutine null

            val user =  dsl.deleteFrom(USER)
                .where(USER.ID.eq(userProprieties.userId))
                .returningResult(USER.asterisk())
                .awaitFirstOrNull()
                ?.into(USER)!!


            userToDto(user, userProprieties)
        }
    }
}

private fun UserDto.toUserProprietiesRecord() = UserProprietiesRecord().also {
    it.firstName = name.first
    it.lastName = name.last
    it.phone = phone.value
}

private fun UserDto.toUserRecord() = UserRecord().also {
    it.email = email.value
}


private fun userToDto(user: UserRecord, userProprieties: UserProprietiesRecord) = UserDto(
    UserId(userProprieties.id!!),
    Email(user.email!!),
    Name(userProprieties.firstName!!, userProprieties.lastName!!),
    Phone(userProprieties.phone!!),
    userProprieties.createdAt?.toKotlinInstant(),
    userProprieties.updatedAt?.toKotlinInstant(),
)

private fun Record.toUserDto(): UserDto {
    val user = this.into(USER)
    val userProprieties = this.into(USER_PROPRIETIES)

    return userToDto(user, userProprieties)
}