package com.securemediavault.api.model

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("users")
data class User(
    @Id
    val id: Long? = null,
    val username: String,
    val password: String,
    val roles: String // Ej: "ROLE_USER,ROLE_ADMIN"
)
