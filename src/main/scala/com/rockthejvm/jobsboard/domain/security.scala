package com.rockthejvm.jobsboard.domain

import tsec.authentication.AugmentedJWT
import tsec.mac.jca.HMACSHA256
import tsec.authentication.JWTAuthenticator
import com.rockthejvm.jobsboard.domain.user.*

object security {
  type Crypto              = HMACSHA256
  type JwtToken            = AugmentedJWT[Crypto, String]
  type Authenticator[F[_]] = JWTAuthenticator[F, String, User, Crypto]
}
