package com.example.sosjibon.ai

import com.example.sosjibon.ui.navigation.ROUTE_COMMUNITY_STORIES
import com.example.sosjibon.ui.navigation.ROUTE_DEVELOPERS
import com.example.sosjibon.ui.navigation.ROUTE_EDIT_PROFILE
import com.example.sosjibon.ui.navigation.ROUTE_EMERGENCY_CONTACTS
import com.example.sosjibon.ui.navigation.ROUTE_EMERGENCY_SOS
import com.example.sosjibon.ui.navigation.ROUTE_GPS_MAP
import com.example.sosjibon.ui.navigation.ROUTE_PRIVACY_TERMS
import com.example.sosjibon.ui.navigation.ROUTE_SECURITY
import com.example.sosjibon.ui.navigation.Screen

enum class AiIntent(val route: String) {

    HOME(Screen.Home.route),

    RESOURCES(Screen.Resources.route),

    VAULT(Screen.Vault.route),

    SETTINGS(Screen.Settings.route),

    EDIT_PROFILE(ROUTE_EDIT_PROFILE),

    SECURITY(ROUTE_SECURITY),

    PRIVACY_TERMS(ROUTE_PRIVACY_TERMS),

    DEVELOPERS(ROUTE_DEVELOPERS),

    EMERGENCY_CONTACTS(ROUTE_EMERGENCY_CONTACTS),

    EMERGENCY_SOS(ROUTE_EMERGENCY_SOS),

    GPS_MAP(ROUTE_GPS_MAP),

    COMMUNITY_STORIES(ROUTE_COMMUNITY_STORIES),

    RESOURCE_ASSESSMENT("assessment"),

    FIRST_AID(""),

    UNKNOWN(Screen.Settings.route)
}