package utils

// Created by ZG on 15/9/10.
// 

object Base64Utils {
  def EncodeBase64( email: String ): String =
    org.apache.commons.codec.binary.Base64.encodeBase64String( email.getBytes )

  def DecodeBase64( encodedUusername: String ): String =
    new String( org.apache.commons.codec.binary.Base64.decodeBase64( encodedUusername ) )
}