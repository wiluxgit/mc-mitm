| Mojauth || Client || Proxy || Server | Mojauth |
|---|---|---|---|---|---|---|---|
|||`LoginHelloC2S` | $\rarr$ | not relevant


# Login Start (C→P) `LoginHelloC2S`
| Packet Id | Field Name | Field Type | Value
|---|---|---|---|
|0x00 | Name
|| UUID

# Encryption Request (S→P) `LoginHelloS2C`
| Packet Id | Field Name | Field Type | Value | Notes
|---|---|---|---|---|
|0x01 | Server Id | String (20) | `""` | max 20 java chars bytes *bruuuuuh*
|| Public Key Length | VarInt | `_` |
|| Public Key DER    | VarInt | ~`{KEY: 0x..7BFE}` | 1024 bits rsa = 128 bytes
|| Nonce Length | VarInt | `_` |
|| Nonce        | VarInt | `_` | 16 bytes?

### Encryption Request (P→C) `LoginHelloS2C`
| Packet Id | Field Name | Field Type | Value | Notes
|---|---|---|---|---|
|0x01 | Server Id | String (20) | `0x..7BF` | max 20 java chars bytes *bruuuuuh*
|| Public Key Length | VarInt | `_` |
|| Public Key DER    | VarInt | ~`{KEY: 0xE}` | 1024 bits rsa = 128 bytes
|| Nonce Length | VarInt | `_` |
|| Nonce        | VarInt | `0x..7bfe` | 16 bytes?

noo
`hash(baseServerId.getBytes("ISO_8859_1"), secretKey.getEncoded(), publicKey.getEncoded())`