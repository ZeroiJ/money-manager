package com.example.moneymanager.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "household_members")
data class HouseholdMember(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String
)
