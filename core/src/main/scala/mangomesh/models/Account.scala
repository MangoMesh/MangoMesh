package mangomesh.models

import java.time.OffsetDateTime
import java.util.UUID

import cats.implicits.*
import cats.data.{NonEmptyChain, Validated}
import cats.effect.IO
import io.getquill.*

case class Account(
    uuid: Account.AccountUUID
)

object Account:
  opaque type AccountUUID = UUID
  object AccountUUID:
    def apply(uuid: UUID): AccountUUID = uuid
    def apply(): IO[AccountUUID]       = IO.delay { UUID.randomUUID() }

  case class Email(
      accountUuid: AccountUUID,
      email: String,
      start: OffsetDateTime,
      end: Option[OffsetDateTime]
  )

  abstract class Data {
    val account: Account
    val emails: Set[Email]
  }

  object Data:

    def unapply(data: Data): Option[(Account, Set[Email])] =
      Some((data.account, data.emails))

    def apply(
        account: Account,
        emails: Set[Email]
    ): Validated[NonEmptyChain[IllegalArgumentException], Data] =
      (
        Validated.Valid(account).toValidatedNec,
        Validated
          .cond(
            emails.nonEmpty && emails.forall(_.accountUuid == account.uuid),
            emails,
            IllegalArgumentException("")
          )
          .toValidatedNec
      ).mapN { (account, emails) =>
        new Data:
          val account: Account   = account
          val emails: Set[Email] = emails
      }

    def apply(
        account: Account
    )(using
        ctx: SqliteJdbcContext[SnakeCase]
    ): IO[Validated[NonEmptyChain[IllegalArgumentException], Data]] =
      import ctx.*

      IO.delay {
        transaction {
          val emails = run(quote(query[Email].filter(_.accountUuid == lift(account.uuid))))

          apply(account, emails.toSet)
        }
      }

  enum Auth(val accountUuid: AccountUUID):
    case Password(
        override val accountUuid: AccountUUID,
        hash: String,
        start: OffsetDateTime,
        end: Option[OffsetDateTime]
    ) extends Auth(accountUuid)

    case OpenID(
        override val accountUuid: AccountUUID,
        openId: String,
        start: OffsetDateTime,
        end: Option[OffsetDateTime]
    ) extends Auth(accountUuid)

    case OAuth(
        override val accountUuid: AccountUUID,
        oauthId: String,
        start: OffsetDateTime,
        end: Option[OffsetDateTime]
    ) extends Auth(accountUuid)

    case SAML(
        override val accountUuid: AccountUUID,
        samlId: String,
        start: OffsetDateTime,
        end: Option[OffsetDateTime]
    ) extends Auth(accountUuid)
  end Auth

  object Auth:
    def authPassword(
        account: Account,
        hash: String
    )(using
        ctx: SqliteJdbcContext[SnakeCase]
    ): IO[Validated[NonEmptyChain[Exception], Data]] =
      import ctx.*

      for
        passwordMatched <- IO.delay {
          run {
            quote {
              query[Auth.Password]
                .filter(ap =>
                  ap.accountUuid == lift(account.uuid) &&
                    ap.hash == lift(hash)
                )
                .nonEmpty
            }
          }
        }
        d <-
          if passwordMatched then Data(account)
          else
            IO.pure {
              Validated.Invalid(new IllegalArgumentException("Invalid password")).toValidatedNec
            }
      yield d
end Account
