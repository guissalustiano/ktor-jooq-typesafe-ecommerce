package br.com.redosul.user

import br.com.redosul.generated.tables.records.UserRecord
import br.com.redosul.generated.tables.records.UserProprietiesRecord
import br.com.redosul.generated.tables.references.USER_PROPRIETIES
import br.com.redosul.generated.tables.references.USER
import br.com.redosul.plugins.Email
import br.com.redosul.plugins.Name
import br.com.redosul.plugins.Phone
import br.com.redosul.plugins.Undefined
import br.com.redosul.plugins.await
import br.com.redosul.plugins.map
import br.com.redosul.plugins.toKotlinInstant
import br.com.redosul.plugins.toSlug
import kotlinx.coroutines.reactive.awaitFirst
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.kotlin.coroutines.transactionCoroutine
import java.time.OffsetDateTime

class UserService(private val dsl: DSLContext) {
    suspend fun findAll() : List<UserResponse> {
        return dsl
            .select(USER.asterisk(), USER_PROPRIETIES.asterisk())
            .from(USER_PROPRIETIES).join(USER)
            .on(USER_PROPRIETIES.USER_ID.eq(USER.ID))
            .await()
            .map{r -> r.toResponse()}
    }

    suspend fun findBySlug(slugId: UserSlug) : UserResponse? {
        return rawFindBySlug(slugId)?.map{ r -> r.toResponse() }
    }

    private suspend fun rawFindBySlug(slugId: UserSlug) : Record? {
        return dsl.select(USER.asterisk(), USER_PROPRIETIES.asterisk())
            .from(USER_PROPRIETIES).join(USER)
            .on(USER_PROPRIETIES.USER_ID.eq(USER.ID))
            .where(USER.SLUG.eq(slugId.slug))
            .awaitFirstOrNull()
    }

    suspend fun create(payload: UserCreatePayload): Unit {
        val userRecord = dsl.newRecord(USER).apply {
            email = payload.email.value
        }

        val userProprietiesRecord = dsl.newRecord(USER_PROPRIETIES).apply {
            firstName = payload.name.first
            lastName = payload.name.last
            phone = payload.phone.value
        }

        return dsl.transactionCoroutine {config ->
            val dsl = config.dsl()
            val user =  dsl.insertInto(USER)
                .set(userRecord)
                .returningResult(USER.ID)
                .awaitFirst()
                .into(USER)


            val userProprieties = dsl.insertInto(USER_PROPRIETIES)
                .set(userProprietiesRecord)
                .set(USER_PROPRIETIES.USER_ID, user.id)
                .returningResult(USER_PROPRIETIES.ID)
                .awaitFirst()
                .into(USER_PROPRIETIES)

            Unit
        }
    }

    suspend fun updateBySlug(slugId: UserSlug, payload: UserUpdatePayload): Unit? {
        val userRecord = dsl.newRecord(USER)

        val userProprietiesRecord = dsl.newRecord(USER_PROPRIETIES).apply {
            payload.name.map {
                firstName = it.first
                lastName = it.last
            }
        }

        val userId= rawFindBySlug(slugId)?.into(USER)?.id ?: return null

        return dsl.transactionCoroutine {config ->
            val dsl = config.dsl()

            val user =  dsl.update(USER)
                .set(USER.UPDATED_AT, OffsetDateTime.now())
                .set(userRecord)
                .where(USER.ID.eq(userId))
                .returningResult(USER.asterisk())
                .awaitFirstOrNull()
                ?.into(USER)

            user ?: return@transactionCoroutine null

            val userProprieties = dsl.update(USER_PROPRIETIES)
                .set(userProprietiesRecord)
                .set(USER_PROPRIETIES.UPDATED_AT, OffsetDateTime.now())
                .where(USER_PROPRIETIES.ID.eq(user.id))
                .returningResult(USER_PROPRIETIES.asterisk())
                .awaitFirstOrNull()
                ?.into(USER_PROPRIETIES)

            Unit
        }
    }

    suspend fun deleteBySlug(slugId: UserSlug) : Unit? {
        val userId = rawFindBySlug(slugId)?.into(USER)?.id ?: return null

        return dsl.transactionCoroutine {config ->
            val dsl = config.dsl()

            val userProprieties = dsl.deleteFrom(USER_PROPRIETIES)
                .where(USER_PROPRIETIES.ID.eq(userId))
                .returningResult(USER_PROPRIETIES.asterisk())
                .awaitFirstOrNull()
                ?.into(USER_PROPRIETIES)!!

            val user =  dsl.deleteFrom(USER)
                .where(USER.ID.eq(userId))
                .returningResult(USER.asterisk())
                .awaitFirstOrNull()
                ?.into(USER)!!

            Unit
        }
    }

    suspend fun createOrUpdate(payload: UserCreatePayload): Unit {
        val user = findBySlug(payload.slug)

        if (user == null) {
            create(payload)
        } else {
            val updatePayload = UserUpdatePayload(
                Undefined.Defined(payload.name),
                Undefined.Defined(payload.phone),
            )
            updateBySlug(payload.slug, updatePayload)
            findBySlug(payload.slug)!!
        }
    }

}

private fun userToResponse(user: UserRecord, userProprieties: UserProprietiesRecord) = UserResponse(
    UserSlug(user.slug!!.toSlug()),
    Email(user.email!!),
    Name(userProprieties.firstName!!, userProprieties.lastName!!),
    Phone(userProprieties.phone!!),
    userProprieties.createdAt!!.toKotlinInstant(),
    userProprieties.updatedAt!!.toKotlinInstant(),
)

private fun Record.toUser(): Pair<UserRecord, UserProprietiesRecord> {
    val user = this.into(USER)
    val userProprieties = this.into(USER_PROPRIETIES)

    return Pair(user, userProprieties)
}

private fun Record.toResponse(): UserResponse {
    val (user, userProprieties) = this.toUser()
    return userToResponse(user, userProprieties)
}