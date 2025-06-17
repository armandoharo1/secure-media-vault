package com.securemediavault.api.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.util.UUID

@Table("users")
data class User(
    @Id val id: UUID? = null,
    val username: String,
    val password: String,
    val roles: String
)
